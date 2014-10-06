package com.antiSpam.spamBayes;


import com.antiSpam.spamBayes.utils.*;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.*;
import weka.core.*;

import java.io.*;
import java.util.Iterator;

public class SpamBayes {
    private static final String CLASSIFIER_SERIALIZED_FILENAME = "classifier.cls";
/*
    public static void main(String args[]) {
        String hamFilename = args[0];
        String spamFilename = args[1];
        String testFilename = args[2];

        Dictionary.getInstance().setNGramLength(3);
        Dictionary.getInstance().setPreprocessEnabled(false);

        try {
            Classifier classifier = loadClassifier();

            double timeStamp = System.currentTimeMillis();
            if (classifier == null) {
                classifier = buildClassifier(hamFilename, spamFilename);
                saveClassifier(classifier);
            }

            String out = String.format("\rLoading done in %f seconds", (System.currentTimeMillis() - timeStamp) / 1000);
            System.out.print(out);
            /*
            double[] levels = {1e-150, 1e-140, 1e-100, 1e-60, 1e-40, 1e-30, 1e-20, 1e-10, 1e-5, 1e-2, 0.1, 0.5,
                    1 - 1e-10, 1 - 1e-12, 1 - 1e-15, 1 - 1e-20};

            for (double level : levels) {
                classifyTestContent(testFilename, classifier, 100000, 200000, level);
            }

            //classifyTestContent("ham.xml", classifier, 0, 100000, 0.5);
            classifyTestContent("spam.json", classifier, 100000, 200000, 0.5);

        } catch (BayesSpamFilterException generalException) {
            generalException.printStackTrace();
            System.exit(1);
        }


    }

    private static Classifier buildClassifier(String hamFilename, String spamFilename) throws BayesSpamFilterException {
        //Не логично, что в методе loadSet грузятся н-граммы в словарь!
        System.out.print    ("\rLoad ham");
        ContentSet hamSet = loadSet(hamFilename, 0, 51200); //magic numbers
        System.out.print("\rLoad spam");
        ContentSet spamSet = loadSet(spamFilename, 0, 51200); //magic numbers

        System.out.print("\rConverting train");
        Instances trainingContent = Dictionary.generateInstances(hamSet, spamSet);
        return getNewClassifier(trainingContent);
    }

    private static Classifier getNewClassifier(Instances trainingSet) throws BayesSpamFilterException {
        //Classifier bayes = new DMNBtext(); //All appears to be spam.
        //Classifier bayes = new HNB();//Not numeric attributes
        //Classifier bayes = new NaiveBayesSimple();//Error: attribute с ж: standard deviation is 0 for class spam
        //Classifier bayes = new NaiveBayes();//Builds very slow. 1000 for 15 seconds
        //Classifier bayes = new NaiveBayesMultinomialUpdateable();// 50000 - 5%
        //Classifier bayes = new NaiveBayesUpdateable();

        //Classifier bayes = new BayesNet(); //25% Не хвататет памяти. Линеный рост.
        //Classifier bayes = new BayesianLogisticRegression(); //60%
        //Classifier bayes = new ComplementNaiveBayes(); //50000 - 55%; 100000 - 65%
        Classifier bayes = new NaiveBayesMultinomial();//50000 - 65%

        System.out.print("\rBuilding");
        try {
            bayes.buildClassifier(trainingSet);
        } catch (Exception exception) {
            throw new BayesSpamFilterException("Can't build classifier");
        }

            return bayes;
    }

    private static Classifier loadClassifier() {
        Classifier classifier = null;
        try {
            classifier = (Classifier) weka.core.SerializationHelper.read(CLASSIFIER_SERIALIZED_FILENAME);
            Dictionary.getInstance().deserializeDictionary();
        } catch (Exception exception) {
            System.out.println("Can't load classifier. Continue");
        }

        return classifier;
    }

    private static void saveClassifier(Classifier classifier) throws BayesSpamFilterException {
        try {
            weka.core.SerializationHelper.write(CLASSIFIER_SERIALIZED_FILENAME, classifier);
            Dictionary.getInstance().serializeDictionary();
        } catch (Exception exception) {
            throw new BayesSpamFilterException("Can't save classifier", exception);
        }
    }

    private static ContentSet loadSet(String filename, int fromMessage, int toMessage) throws BayesSpamFilterException {
        ContentSet resultSet = new ContentSet();
        try {
            Iterator<String> reader = new JSONFileReader(filename, fromMessage, toMessage);

            String text = reader.next();
            while (text != null) {
                resultSet.addString(text);
                text = reader.next();
            }
        } catch (IOException exception) {
            throw new BayesSpamFilterException("Can't read file " + filename + ".", exception);
        }

        return resultSet;
    }

    private static void classifyTestContent(String testFilename,
                                            Classifier classifier,
                                            int fromMessage,
                                            int toMessage,
                                            double level) throws BayesSpamFilterException {

        Instances instances = Dictionary.getNewEmptyInstances();

        try {
            Iterator<String> reader = new JSONFileReader(testFilename, fromMessage, toMessage);

            int messagesCount = 0;
            double spam = 0, ham = 0, spamPercent = 0, hamPercent = 0;
            double timeStamp, timeSpent = 0;

            String text = reader.next();
            while (text != null) {
                timeStamp = System.currentTimeMillis();

                Instance classifyingInstance = Dictionary.generateInstance(text);
                instances.add(classifyingInstance);

                double label = classifier.distributionForInstance(instances.instance(instances.numInstances() - 1))[1];
                //double label = classifier.classifyInstance(instances.instance(instances.numInstances() - 1));

                if (label > level) {
                    spam++;
                } else {
                    ham++;
                }
                messagesCount++;

                timeStamp -= System.currentTimeMillis();
                timeSpent += -timeStamp;


                spamPercent = spam*100/messagesCount;
                hamPercent = ham*100/messagesCount;
                double percentDone = (double)messagesCount*100/(double)(toMessage - fromMessage);
                if (percentDone % 1 == 0) {
                    System.out.print("\rspam: " + spamPercent + "% | ham: " + hamPercent +
                            "% | done: " + percentDone + "%");
                }

                /*if (timeSpent >= 1000L) {
                    break;
                }
                text = reader.next();
            }

            String out = String.format("\rspam: %.1f %% | ham %.1f %% | level: %e | Messages in second: %f | messages: %d\n",
                                       spamPercent,
                                       hamPercent,
                                       level,
                                       messagesCount * 1000 / timeSpent,
                                       messagesCount);
            System.out.print(out);

        } catch (IOException exception) {
            throw new BayesSpamFilterException("Can't read test file " + testFilename + ".", exception);
        } catch (Exception exception) {
            throw new BayesSpamFilterException("Classification error", exception);
        }
    }

    private static void saveContentToFile(Instances instances, String filename) {

        replaceAllMissing(instances);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            try {
                writer.write(instances.toString());
                writer.newLine();
                writer.flush();
            } catch (IOException exception) {
                System.out.println("Saving error");
            } finally {
                writer.close();
            }
        } catch (IOException exception) {
            System.out.println("Opening output stream error");
            exception.printStackTrace();
        }
    }

    private static void replaceAllMissing(Instances instances) {
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            for (int j = 0; j < instance.numAttributes(); j++) {
                if (instance.isMissing(j)) {
                    instance.setValue(j, 0);
                }
            }
        }
    }
*/
}
