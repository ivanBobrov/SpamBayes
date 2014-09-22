package com.antiSpam.spamBayes;


import com.antiSpam.spamBayes.utils.ContentSet;
import com.antiSpam.spamBayes.utils.Dictionary;
import com.antiSpam.spamBayes.utils.XMLFileReader;
import weka.classifiers.Classifier;
import weka.core.*;
import weka.classifiers.bayes.NaiveBayes;

import java.io.*;
import java.util.HashMap;

public class SpamBayes {
    private static HashMap<String, Integer> indexMap = new HashMap<String, Integer>();

    public static void main(String argv[]) {
        String hamFilename = argv[0];
        String spamFilename = argv[1];
        String testFilename = argv[2];

        System.out.print("\rLoad ham");
        ContentSet hamSet = loadSet(hamFilename, 0, 1000);
        System.out.print("\rLoad spam");
        ContentSet spamSet = loadSet(spamFilename, 0, 1000);

        System.out.print("\rConverting train");
        Instances trainingContent = Dictionary.generateInstances(hamSet, spamSet);

        System.out.print("\rClassyfying");
        classifyTestContent(trainingContent, testFilename, 0, 1000);

        //performTest(argv);
    }

    private static void performTest(String argv[]) {
        String hamFilename = argv[0];
        String spamFilename = argv[1];
        String testFilename = argv[2];

        for (int i = 500; i < 10001; i += 500) {
            for (int j = 50000; j < 100000; j+= 5000) {
                System.out.print("\rLoad ham");
                ContentSet hamSet = loadSet(hamFilename, 0, i);
                System.out.print("\rLoad spam");
                ContentSet spamSet = loadSet(spamFilename, 0, i);

                System.out.print("\rConverting train");
                Instances trainingContent = Dictionary.generateInstances(hamSet, spamSet);

                System.out.print("\rClassyfying");
                classifyTestContent(trainingContent, testFilename, 0, 50000);
            }
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

    private static void classifyTestContent(Instances trainingSet,
                                            String testFilename,
                                            int fromMessage,
                                            int toMessage) {
        trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
        Instances instances = Dictionary.getNewEmptyInstances();

        try {
            Classifier classifier = getClassfier(trainingSet);
            XMLFileReader reader = new XMLFileReader(testFilename);

            int currentMessage = 0;
            int messagesCount = 0;
            double spam = 0, ham = 0;

            while (reader.hasNext()) {
                if (currentMessage >= fromMessage) {
                    String text = reader.next();

                    Instance classifyingInstance = Dictionary.generateInstance(text);
                    instances.add(classifyingInstance);
                    double label = classifier.classifyInstance(instances.instance(instances.numInstances() - 1));

                    messagesCount++;
                    //Actually it's zero or one. But can't compare double for zero.
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

                    if (currentMessage > toMessage) {
                        break;
                    }

                    currentMessage++;
                }
            }

        } catch (IOException exception) {
            System.out.println("Can't read test file " + testFilename + ". Exit");
            System.exit(1);
        } catch (Exception exception) {
            System.out.println("Classification error");
            exception.printStackTrace();
        }
    }

    private static Classifier getClassfier(Instances trainingSet) throws Exception {
        NaiveBayes bayes = new NaiveBayes();

        System.out.print("\rBuilding");
        long timeStamp = System.currentTimeMillis();
        bayes.buildClassifier(trainingSet);
        long buildingTime = System.currentTimeMillis() - timeStamp;

        System.out.print("\rBuild in " + buildingTime + " milliseconds");

        return bayes;
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
