package com.antiSpam.spamBayes;


import com.antiSpam.spamBayes.utils.ContentSet;
import com.sun.xml.internal.stream.events.CharacterEvent;
import weka.core.*;
import weka.classifiers.bayes.NaiveBayes;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class SpamBayes {
    private static HashMap<String, Integer> indexMap = new HashMap<String, Integer>();

    public static void main(String argv[]) {
        String outputFilename = "result.arff";
        String hamFilename = argv[0];
        String spamFilename = argv[1];
        String testFilename = argv[2];

        System.out.print("\rLoad ham");
        ContentSet hamSet = loadSet(hamFilename, 0, 2000);
        System.out.print("\rLoad spam");
        ContentSet spamSet = loadSet(spamFilename, 0, 2000);
        System.out.print("\rLoad test");
        ContentSet testSet = loadSet(testFilename, 20000, 20100);

        System.out.print("\rConverting train");
        Instances trainingContent = ContentSet.generateInstances(hamSet, spamSet);
        System.out.print("\rConverting test");
        Instances testContent = ContentSet.generateInstances(testSet);

        System.out.print("\rClassyfying");
        classifyTestContent(trainingContent, testContent);
        System.out.println("\rspam: " + getPercentOfValue(testContent, 1)*100 + "%");

        //performTest(argv);
    }

    private static void performTest(String argv[]) {
        String outputFilename = "result.arff";
        String hamFilename = argv[0];
        String spamFilename = argv[1];
        String testFilename = argv[2];

        System.out.print("\rLoad test");
        ContentSet testSet = loadSet(testFilename, 0, 1000);
        Instances testContent = ContentSet.generateInstances(testSet);

        for (int i = 50; i < 1001; i += 50) {
            System.out.print("\rLoad ham");
            ContentSet hamSet = loadSet(hamFilename, 0, i);
            System.out.print("\rLoad spam");
            ContentSet spamSet = loadSet(spamFilename, 0, i);


            System.out.print("\rConverting");
            Instances trainingContent = ContentSet.generateInstances(hamSet, spamSet);

            System.out.print("\rClassifying");
            classifyTestContent(trainingContent, testContent);
            System.out.println("\rnum: " + i + ", right: " + getPercentOfValue(testContent, 1)*100 + "%");
        }
    }

    private static ContentSet loadSet(String filename, int fromMessage, int toMessage) {
        ContentSet resultSet = new ContentSet();
        try {
            FileInputStream inputStream = new FileInputStream(filename);
            try {
                XMLEventReader xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(inputStream);

                int currentLine = 0;
                while (xmlEventReader.hasNext()) {
                    XMLEvent event = xmlEventReader.nextEvent();

                    if (event.isStartElement() && event.asStartElement().getName().toString().equals("text")) {
                        XMLEvent textEvent = xmlEventReader.nextEvent();

                        if (currentLine++ < fromMessage) {
                            continue;
                        }

                        String text = ((CharacterEvent) textEvent).getData().replace("\n", " ").toLowerCase();
                        resultSet.addString(text);
                        //System.out.print("\r" + currentLine++);

                        if (toMessage != 0 && currentLine > toMessage) {
                            break;
                        }
                    }
                }
                //System.out.print("\n");

            } catch (XMLStreamException exception) {
                System.out.println("Error parsing " + filename);
                exception.printStackTrace();
            } finally {
                try {
                    inputStream.close();
                } catch (IOException exception) {
                    System.out.println("Can't close stream. Exit.");
                    System.exit(1);
                }
            }
        } catch (FileNotFoundException exception) {
            System.out.println("File " + filename + " not found. Exit.");
            System.exit(1);
        }

        return resultSet;
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

    private static void classifyTestContent(Instances trainingSet, Instances testSet) {
        trainingSet.setClassIndex(trainingSet.numAttributes() - 1);
        testSet.setClassIndex(testSet.numAttributes() - 1);

        NaiveBayes bayes = new NaiveBayes();

        try {
            System.out.print("\rBuilding");
            bayes.buildClassifier(trainingSet);
            System.out.print("\rLabeling");
            for (int i = 0; i < testSet.numInstances(); i++) {
                System.out.print("\rLabeling process: " + i*100/testSet.numInstances() + " %");
                double clsLabel = bayes.classifyInstance(testSet.instance(i));
                testSet.instance(i).setClassValue(clsLabel);
            }
        } catch (Exception e) {
            System.out.println("Classification error");
            e.printStackTrace();
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
