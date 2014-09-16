package com.antiSpam.spamBayes;


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

        Instances trainingContent = getNewContentInstances();
        loadContent(trainingContent, hamFilename, 0, 50, 0, true);
        loadContent(trainingContent, spamFilename, 0, 50, 1, true);

        //It makes attributes of training and test contents match
        Instances testContent = new Instances(trainingContent);
        testContent.delete();

        loadContent(testContent, testFilename, 0, 50, 0, false);

        classifyTestContent(trainingContent, testContent);

        replaceAllMissing(testContent);
        saveContenToFile(testContent, outputFilename);
    }

    private static void saveContenToFile(Instances instances, String filename) {
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
        System.out.println("Loading " + filename);

        try {
            FileInputStream inputStream = new FileInputStream(filename);
            try {
                XMLEventReader xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(inputStream);

                int currentLine = 0;
                while (xmlEventReader.hasNext()) {
                    XMLEvent event = xmlEventReader.nextEvent();

                    if (event.isStartElement() && event.asStartElement().getName().toString().equals("text")) {
                        XMLEvent textEvent = xmlEventReader.nextEvent();

                        if (currentLine < fromMessage) {
                            continue;
                        }

                        String text = ((CharacterEvent) textEvent).getData().replace("\n", " ").toLowerCase();
                        addStringToData(instances, text, classType, addNewAttributes);
                        System.out.print("\r" + currentLine++);

                        if (toMessage != 0 && currentLine > toMessage) {
                            break;
                        }
                    }
                }
                System.out.print("\n");

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

    public static void err(String argv[]) {
        tryoutsmth(argv);
        //readArff();
    }


    public static void readArff() {
        Instances data = null;
        Instances unlabeled = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("data.arff"));
            BufferedReader readerUnlabeled = new BufferedReader(new FileReader("data_test_unlabeled.arff"));

            data = new Instances(reader);
            unlabeled = new Instances(readerUnlabeled);

            reader.close();
            readerUnlabeled.close();

        } catch(FileNotFoundException exception) {
            System.out.println("File not found");
        } catch(IOException exception) {
            System.out.println("IOException");
        }


        data.setClassIndex(0);
        unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
        NaiveBayes bayes = new NaiveBayes();
        try {
            bayes.buildClassifier(data);
            for (int i = 0; i < unlabeled.numInstances(); i++) {
                double clsLabel = bayes.classifyInstance(unlabeled.instance(i));
                unlabeled.instance(i).setClassValue(clsLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.print("done");
    }

    public static void tryoutsmth(String argv[]) {
        String filename = argv[0];
        String goodFilename = argv[1];
        String testFilename = argv[2];


        Instances instances = new Instances("Relation", new FastVector(), 5000000);
        FastVector fastVector = new FastVector(2);
        fastVector.addElement("ham");
        fastVector.addElement("spam");
        Attribute attribute = new Attribute("@@class@@", fastVector);
        instances.insertAttributeAt(attribute, 0);

        InputStream input = null;

        System.out.println(filename);
        try {
            input = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            System.out.println("Error input stream creation");
        }

        read(instances, input, 1, 50, true);

        System.out.println(goodFilename);
        try {
            input = new FileInputStream(goodFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        read(instances, input, 0, 50, true);

        System.out.println("replacing all missing");
        replaceAllMissing(instances);

        Instances testInstances = new Instances(instances);
        testInstances.delete();

        System.out.println(testFilename);
        try {
            input = new FileInputStream(testFilename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        read(testInstances, input, 0, 400, false);

        //testInstances.

        instances.setClassIndex(instances.numAttributes() - 1);
        testInstances.setClassIndex(testInstances.numAttributes() - 1);

        NaiveBayes bayes = new NaiveBayes();
        try {
            bayes.buildClassifier(instances);
            for (int i = 0; i < testInstances.numInstances(); i++) {
                double clsLabel = bayes.classifyInstance(testInstances.instance(i));
                testInstances.instance(i).setClassValue(clsLabel);
                System.out.println(i + "\t" + clsLabel);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }



        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("result.arff"));
            writer.write(instances.toString());
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        System.out.print("done");
    }

    private static void read(Instances instances, InputStream input, double clazz, int count, boolean rewrite) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            int currentLine = 0;
            XMLEventReader r = factory.createXMLEventReader(input);
            while (r.hasNext()) {
                XMLEvent event = r.nextEvent();
                if (event.isStartElement() && event.asStartElement().getName().toString().equals("text")) {
                    XMLEvent text = r.nextEvent();
                    addString(((CharacterEvent) text).getData().replace("\n", " "), instances, clazz, rewrite);
                    System.out.print("\r " + currentLine++);

                    if (count != 0 && currentLine > count) {
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
            System.out.println("Something wrong :)");
        }
        System.out.println("\n");
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

    public static void addString(String string, Instances instances, double clazz, boolean rewrite) {
        ArrayList<String> bases = splitString(string);

        SparseInstance newInstance = new SparseInstance(instances.numAttributes());

        /*for (int i = 0; i < newInstance.numAttributes(); i++) {
            newInstance.setValue(i, 0);
        }*/

        for (String base : bases) {
            Integer index = indexMap.get(base);
            if (index == null) {
                if (rewrite) {
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

        newInstance.setValue(newInstance.numAttributes() - 1, clazz);
        instances.add(newInstance);
    }

    public static ArrayList<Integer> parseString(String string) {
        ArrayList<Integer> baseIndices = new ArrayList<Integer>();
        ArrayList<String> bases = splitString(string);

        for (String base : bases) {
            Integer index = indexMap.get(base);
            if (index == null) {
                int newIdx = indexMap.size();
                indexMap.put(base, newIdx);
                baseIndices.add(newIdx);
            } else {
                baseIndices.add(index);
            }
        }

        return baseIndices;
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
