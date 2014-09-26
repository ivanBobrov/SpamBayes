package com.antiSpam.spamBayes;


import com.antiSpam.spamBayes.utils.ContentSet;
import com.antiSpam.spamBayes.utils.Dictionary;
import com.antiSpam.spamBayes.utils.SpamBayesException;
import com.antiSpam.spamBayes.utils.XMLFileReader;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.*;
import weka.core.*;

import java.io.*;

public class SpamBayes {
    private static final String CLASSIFIER_SERIALIZED_FILENAME = "classifier.cls";

    public static void main(String args[]) {
        String hamFilename = args[0];
        String spamFilename = args[1];
        String testFilename = args[2];

        Dictionary.getInstance().setNGramLength(3);
        Dictionary.getInstance().setPreprocessEnabled(false);

        try {
            Classifier classifier = loadClassifier();
            if (classifier == null) {
                classifier = buildClassifier(hamFilename, spamFilename);
                saveClassifier(classifier);
            }

            classifyTestContent(testFilename, classifier, 100000, 200000);

        } catch (SpamBayesException generalException) {
            generalException.printStackTrace();
            System.exit(1);
        }


    }

    private static Classifier buildClassifier(String hamFilename, String spamFilename) throws SpamBayesException {
        //Не логично, что в методе loadSet грузятся н-граммы в словарь!
        System.out.print("\rLoad ham");
        ContentSet hamSet = loadSet(hamFilename, 0, 4000); //magic numbers
        System.out.print("\rLoad spam");
        ContentSet spamSet = loadSet(spamFilename, 0, 4000); //magic numbers

        System.out.print("\rConverting train");
        Instances trainingContent = Dictionary.generateInstances(hamSet, spamSet);
        return getNewClassifier(trainingContent);
    }

    private static Classifier getNewClassifier(Instances trainingSet) throws SpamBayesException {
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
            throw new SpamBayesException("Can't build classifier");
        }

        System.out.print("\rSaving to file");
        saveClassifier(bayes);

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

    private static void saveClassifier(Classifier classifier) throws SpamBayesException {
        try {
            weka.core.SerializationHelper.write(CLASSIFIER_SERIALIZED_FILENAME, classifier);
            Dictionary.getInstance().serializeDictionary();
        } catch (Exception exception) {
            throw new SpamBayesException("Can't save classifier", exception);
        }
    }

    private static ContentSet loadSet(String filename, int fromMessage, int toMessage) throws SpamBayesException {
        ContentSet resultSet = new ContentSet();
        try {
            XMLFileReader reader = new XMLFileReader(filename, fromMessage, toMessage);

            while (reader.hasNext()) {
                resultSet.addString(reader.next());
            }
        } catch (IOException exception) {
            throw new SpamBayesException("Can't read file " + filename + ".", exception);
        }

        return resultSet;
    }

    private static void classifyTestContent(String testFilename,
                                            Classifier classifier,
                                            int fromMessage,
                                            int toMessage) throws SpamBayesException {

        Instances instances = Dictionary.getNewEmptyInstances();

        try {
            XMLFileReader reader = new XMLFileReader(testFilename, fromMessage, toMessage);

            int messagesCount = 0;
            double spam = 0, ham = 0;

            while (reader.hasNext()) {
                String text = reader.next();

                Instance classifyingInstance = Dictionary.generateInstance(text);
                instances.add(classifyingInstance);
                //double label = classifier.distributionForInstance(instances.instance(instances.numInstances() - 1))[1];
                double label = classifier.classifyInstance(instances.instance(instances.numInstances() - 1));

                if (label > 0.5) {
                    spam++;
                } else {
                    ham++;
                }
                messagesCount++;

                System.out.print("\rspam: " +
                                    spam*100/messagesCount +
                                    "% | ham: " +
                                    ham*100/messagesCount +
                                    "% | done: " + (double)messagesCount*100/(double)(toMessage - fromMessage) +
                                    "%");
            }
            System.out.print("\n");

        } catch (IOException exception) {
            throw new SpamBayesException("Can't read test file " + testFilename + ".", exception);
        } catch (Exception exception) {
            throw new SpamBayesException("Classification error", exception);
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

}
