package com.antiSpam.spamBayes;


import com.sun.xml.internal.stream.events.CharacterEvent;
import sun.security.jca.GetInstance;
import weka.core.*;
import weka.classifiers.bayes.NaiveBayes;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class SpamBayes {
    private static HashMap<String, Integer> indexMap = new HashMap<String, Integer>();

    public static void main(String argv[]) {
        tryoutsmth(argv);
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
        unlabeled.setClassIndex(0);
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
        System.out.println(filename);

        Instances instances = new Instances("Relation", new FastVector(), 5000000);
        InputStream input = null;
        try {
            input = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            System.out.println("Error input stream creation");
        }

        XMLInputFactory factory = XMLInputFactory.newInstance();
        try {
            int currentLine = 0;
            XMLEventReader r = factory.createXMLEventReader(input);
            while (r.hasNext() && currentLine < 210) {
                XMLEvent event = r.nextEvent();
                if (event.isStartElement() && event.asStartElement().getName().toString().equals("text")) {
                    XMLEvent text = r.nextEvent();
                    addString(((CharacterEvent) text).getData().replace("\n", " "), instances);
                    System.out.print("\r " + currentLine++);
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
            System.out.println("Something wrong :)");
        }

        replaceAllMissing(instances);

        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("result.arff"));
            writer.write(instances.toString());
            writer.newLine();
            writer.flush();
            writer.close();
        } catch (IOException exception) {
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

    public static void addString(String string, Instances instances) {
        ArrayList<String> bases = splitString(string);

        SparseInstance newInstance = new SparseInstance(instances.numAttributes());
        /*for (int i = 0; i < newInstance.numAttributes(); i++) {
            newInstance.setValue(i, 0);
        }*/

        for (String base : bases) {
            Integer index = indexMap.get(base);
            if (index == null) {
                index = indexMap.size();
                indexMap.put(base, index);

                Attribute attribute = new Attribute(base);
                instances.insertAttributeAt(attribute, index);
                newInstance.insertAttributeAt(index);
                newInstance.setValue(index, 1);
            } else {
                if (newInstance.isMissing(index)) {
                    newInstance.setValue(index, 1);
                } else {
                    newInstance.setValue(index, newInstance.value(index) + 1);
                }
            }

        }

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
