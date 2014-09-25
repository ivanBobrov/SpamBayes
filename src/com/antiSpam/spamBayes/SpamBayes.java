package com.antiSpam.spamBayes;


import com.antiSpam.spamBayes.utils.ContentSet;
import com.antiSpam.spamBayes.utils.Dictionary;
import com.antiSpam.spamBayes.utils.XMLFileReader;
import com.sun.corba.se.impl.ior.ObjectAdapterIdNumber;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.*;
import weka.core.*;

import java.io.*;

public class SpamBayes {
    private static final String CLASSIFIER_SERIALIZED_FILENAME = "classifier.cls";

    public static void main(String argv[]) {
        String hamFilename = argv[0];
        String spamFilename = argv[1];
        String testFilename = argv[2];

        Classifier classifier = loadClassifier();
        if (classifier == null) {
            try {
                System.out.print("\rLoad ham");
                ContentSet hamSet = loadSet(hamFilename, 0, 51200);
                System.out.print("\rLoad spam");
                ContentSet spamSet = loadSet(spamFilename, 0, 51200);

                System.out.print("\rConverting train");
                Instances trainingContent = Dictionary.generateInstances(hamSet, spamSet);
                classifier = buildClassifier(trainingContent);

            } catch (Exception buildingException) {
                System.out.print("\rCan't build classifier\n");
                System.exit(1);
            }
        }

        System.out.print("\rClassyfying");
        try {
            classifyTestContent(testFilename, classifier, 100000, 200000);
        } catch (Exception classifyingException) {
            System.out.print("\rCan't classify\n");
            classifyingException.printStackTrace();
        }
    }

    private static ContentSet loadSet(String filename, int fromMessage, int toMessage) {
        ContentSet resultSet = new ContentSet();
        try {
            XMLFileReader reader = new XMLFileReader(filename);

            int currentMessage = 0;
            while (reader.hasNext()) {
                if (currentMessage >= fromMessage) {
                    resultSet.addString(reader.next());
                }

                if (currentMessage >= toMessage) {
                    break;
                }

                currentMessage++;
            }
        } catch (IOException exception) {
            System.out.println("Can't read file " + filename + ". Exit.");
            System.exit(1);
        }

        return resultSet;
    }

    private static void classifyTestContent(String testFilename,
                                            Classifier classifier,
                                            int fromMessage,
                                            int toMessage) {
        Instances instances = Dictionary.getNewEmptyInstances();

        try {
            XMLFileReader reader = new XMLFileReader(testFilename);

            int currentMessage = 0;
            int messagesCount = 0;
            double spam = 0, ham = 0;

            while (reader.hasNext()) {
                String text = reader.next();
                if (currentMessage >= toMessage) {
                    break;
                }

                if (currentMessage >= fromMessage) {
                    Instance classifyingInstance = Dictionary.generateInstance(text);
                    instances.add(classifyingInstance);
                    //double label = classifier.distributionForInstance(instances.instance(instances.numInstances() - 1))[1];
                    double label = classifier.classifyInstance(instances.instance(instances.numInstances() - 1));

                    messagesCount++;

                    if (label > 0.5) {
                        spam++;
                    } else {
                        ham++;
                    }

                    System.out.print("\rspam: " +
                                        spam*100/messagesCount +
                                        "% | ham: " +
                                        ham*100/messagesCount +
                                        "% | done: " + (double)messagesCount*100/(double)(toMessage - fromMessage) +
                                        "%");
                    //System.out.println(label + " ||| " + text);
                }
                currentMessage++;
            }
            System.out.print("\n");

        } catch (IOException exception) {
            System.out.println("Can't read test file " + testFilename + ". Exit");
            System.exit(1);
        } catch (Exception exception) {
            System.out.println("Classification error");
            exception.printStackTrace();
        }
    }

    private static Classifier buildClassifier(Instances trainingSet) throws Exception {
        //Classifier bayes = new DMNBtext(); //All appears to be spam.
        //Classifier bayes = new HNB();//Not numeric attributes
        //Classifier bayes = new NaiveBayesSimple();//Error: attribute с ж: standard deviation is 0 for class spam
        //Classifier bayes = new NaiveBayes();//Builds very slow. 1000 for 15 seconds
        Classifier bayes = new NaiveBayesMultinomialUpdateable();// 50000 - 5%
        //Classifier bayes = new NaiveBayesUpdateable();

        //Classifier bayes = new BayesNet(); //25% Не хвататет памяти. Линеный рост.
        //Classifier bayes = new BayesianLogisticRegression(); //60%
        //Classifier bayes = new ComplementNaiveBayes(); //50000 - 55%; 100000 - 65%
        //Classifier bayes = new NaiveBayesMultinomial();//50000 - 65%

        System.out.print("\rBuilding");
        bayes.buildClassifier(trainingSet);

        System.out.print("\rSaving to file");
        saveClassifier(bayes); //Exception IO. Catch

        return bayes;
    }

    private static void saveClassifier(Classifier classifier) throws Exception {
        weka.core.SerializationHelper.write(CLASSIFIER_SERIALIZED_FILENAME, classifier);
    }

    private static Classifier loadClassifier() {
        Classifier classifier = null;
        try {
            classifier = (Classifier) weka.core.SerializationHelper.read(CLASSIFIER_SERIALIZED_FILENAME);
        } catch (Exception e) {
            System.out.print("\rCan't load classifier\n");
        }

        return classifier;
    }

    private static double getPercentOfValue(Instances instances, double value) {
        double res = 0;
        for (int i = 0; i < instances.numInstances(); i++) {
            Instance instance = instances.instance(i);
            if (instance.value(instances.numAttributes() - 1) == value) {
                res++;
            }
        }

        return res/instances.numInstances();
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
