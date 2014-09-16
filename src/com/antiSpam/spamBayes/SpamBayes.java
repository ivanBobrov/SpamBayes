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

        ContentSet hamSet = loadSet(hamFilename, 0, 50);
        ContentSet spamSet = loadSet(spamFilename, 0, 50);
        ContentSet testSet = loadSet(testFilename, 0, 50);

        Instances trainingContent = ContentSet.generateInstances(hamSet, spamSet);
        Instances testContent = ContentSet.generateInstances(testSet);

        classifyTestContent(trainingContent, testContent);
        System.out.println("\rright: " + getPercentOfValue(testContent, 1)*100 + "%");
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

    public static void err(String argv[]) {
        String outputFilename = "result.arff";
        String hamFilename = argv[0];
        String spamFilename = argv[1];
        String testFilename = argv[2];

        Instances trainingContent = getNewContentInstances();
        loadContent(trainingContent, hamFilename, 0, 50, 0, true);
        loadContent(trainingContent, spamFilename, 0, 50, 1, true);

        //It makes attributes of training and test contents match
        Instances testContent = new Instances(trainingContent);
        testContent.delete();

        loadContent(testContent, testFilename, 0, 50, 0, false);

        classifyTestContent(trainingContent, testContent);

        saveContentToFile(testContent, outputFilename);


        //performTest(argv);
    }

    private static void performTest(String argv[]) {
        String outputFilename = "result.arff";
        String spamFilename = argv[0];
        String hamFilename = argv[1];
        String testFilename = argv[2];

        for (int i = 50; i < 500; i += 50) {
            System.out.println("loading " + i + " messages");
            Instances trainingContent = getNewContentInstances();

            System.out.print("\rloading ham");
            loadContent(trainingContent, hamFilename, 0, i, 0, true);
            System.out.print("\rloading spam");
            loadContent(trainingContent, spamFilename, 0, i, 1, true);

            //It makes attributes of training and test contents match
            Instances testContent = new Instances(trainingContent);
            testContent.delete();

            System.out.print("\rloading test");
            loadContent(testContent, testFilename, 0, 500, 0, false);

            System.out.print("\rclassifying");
            classifyTestContent(trainingContent, testContent);

            System.out.println("\rright: " + getPercentOfValue(testContent, 1)*100 + "%");
            indexMap.clear();
        }
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
            bayes.buildClassifier(trainingSet);
            for (int i = 0; i < testSet.numInstances(); i++) {
                double clsLabel = bayes.classifyInstance(testSet.instance(i));
                testSet.instance(i).setClassValue(clsLabel);
            }
        } catch (Exception e) {
            System.out.println("Classification error");
            e.printStackTrace();
        }
    }

    /*
    loadSize is the number of messages to load from file. Zero value means all data.
    classType is the predefined class of all these messages. Zero - legal message, One - spam.
     */
    private static void loadContent(Instances instances,
                                    String filename,
                                    int fromMessage, // that was loadSize
                                    int toMessage,
                                    double classType,
                                    boolean addNewAttributes) {
        //System.out.println("Loading " + filename);

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
                        addStringToData(instances, text, classType, addNewAttributes);
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
    }

    private static void addStringToData(Instances instances, String text, double classType, boolean addNewAttributes) {
        ArrayList<String> bases = splitString(text);
        SparseInstance newInstance = new SparseInstance(instances.numAttributes());

        for (String base : bases) {
            Integer index = indexMap.get(base);
            if (index == null) {
                if (addNewAttributes) {
                    index = indexMap.size();
                    indexMap.put(base, index);

                    Attribute attribute = new Attribute(base);
                    instances.insertAttributeAt(attribute, index);
                    newInstance.insertAttributeAt(index);
                    newInstance.setValue(index, 1);
                }
            } else {
                if (newInstance.isMissing(index)) {
                    newInstance.setValue(index, 1);
                } else {
                    newInstance.setValue(index, newInstance.value(index) + 1);
                }
            }

        }

        newInstance.setValue(newInstance.numAttributes() - 1, classType);
        instances.add(newInstance);
    }

    private static Instances getNewContentInstances() {
        //This big number is capacity for instances.
        Instances instances = new Instances("dataContent", new FastVector(), 1000000);

        FastVector fastVector = new FastVector(2);
        fastVector.addElement("ham");
        fastVector.addElement("spam");
        Attribute attribute = new Attribute("@@class@@", fastVector);

        instances.insertAttributeAt(attribute, 0);

        return instances;
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

    public static ArrayList<String> splitString(String string) {
        int nGramLength = 3;
        ArrayList<String> bases = new ArrayList<String>();

        if (string.length() > nGramLength) {
            for (int i = 0; i < string.length() - (nGramLength - 1); i++) {
                bases.add(string.substring(i, i + nGramLength));
            }
        }

        return bases;
    }


}
