package com.antiSpam.spamBayes.utils;


import weka.core.*;

import java.util.*;

public class ContentSet {
    private Dictionary dictionary;
    private List<SortedMap<Integer, Integer>> stringList = new LinkedList<SortedMap<Integer, Integer>>();

    public ContentSet() {
        this.dictionary = Dictionary.getInstance();
    }

    public void addString(String string) {
        //TODO: Check for null string and string with length less than N_GRAM_LENGTH
        ArrayList<String> bases = dictionary.parseString(string);
        SortedMap<Integer, Integer> stringMap = new TreeMap<Integer, Integer>();

        for (String base : bases) {
            Integer index = dictionary.getIndex(base);

            if (stringMap.containsKey(index)) {
                Integer count = stringMap.get(index);
                count++;
                stringMap.put(index, count);
            } else {
                stringMap.put(index, 1);
            }
        }

        if (stringMap.size() > 0) {
            stringList.add(stringMap);
        }
    }

    public int getStringCount() {
        return stringList.size();
    }

    public Iterator<SortedMap<Integer, Integer>> getStringSet() {
        return stringList.iterator();
    }

    //Maybe there should be many content sets with no names in the future
    public static Instances generateInstances(ContentSet hamSet, ContentSet spamSet) {
        Dictionary dictionary = Dictionary.getInstance();
        int nGramCount = dictionary.getDictionarySize();

        FastVector attributes = new FastVector(nGramCount);

        for (String base : dictionary.getBaseSet()) {
            attributes.addElement(new Attribute(base));
        }

        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("ham");
        fvClassVal.addElement("spam");
        Attribute classAttribute = new Attribute("@@class@@", fvClassVal);
        attributes.addElement(classAttribute);

        Instances instances = new FastInstances("dataSet", attributes, nGramCount);

        fillInstances(instances, hamSet, 0);
        fillInstances(instances, spamSet, 1);

        instances.setClassIndex(instances.numAttributes() - 1);

        return instances;
    }

    public static Instances generateInstances(ContentSet set) {
        Dictionary dictionary = Dictionary.getInstance();
        int nGramCount = dictionary.getDictionarySize();

        FastVector attributes = new FastVector(nGramCount);

        for (String base : dictionary.getBaseSet()) {
            attributes.addElement(new Attribute(base));
        }

        FastVector fvClassVal = new FastVector(2);
        fvClassVal.addElement("ham");
        fvClassVal.addElement("spam");
        Attribute classAttribute = new Attribute("@@class@@", fvClassVal);
        attributes.addElement(classAttribute);

        Instances instances = new FastInstances("testSet", attributes, nGramCount);
        fillInstances(instances, set, 0);

        instances.setClassIndex(instances.numAttributes() - 1);

        return instances;
    }

    private static void fillInstances(Instances instances, ContentSet contentSet, double classValue) {
        Iterator<SortedMap<Integer, Integer>> iterator = contentSet.getStringSet();
        //long timeSpent = 0;
        //long time = System.currentTimeMillis();

        while (iterator.hasNext()) {
            SortedMap<Integer, Integer> string = iterator.next();

            //variant 3
            int[] indices = new int[string.size()];
            double[] attValues = new double[string.size()];

            int i = 0;
            for (Map.Entry<Integer, Integer> entry : string.entrySet()) {
                indices[i] = entry.getKey();
                attValues[i] = entry.getValue();
                i++;
            }

            int numAttributes = Dictionary.getInstance().getDictionarySize() + 1;
            indices[indices.length - 1] = numAttributes - 1;
            attValues[attValues.length - 1] = classValue;

            Instance newInstance = new SparseInstance(1.0, attValues, indices, numAttributes);
            instances.add(newInstance);
        }

        //timeSpent += System.currentTimeMillis() - time;
        //System.out.println(timeSpent);
    }

    /*
    It's a shell for Integer. To differ indices and count.

    private class Index {
        public Integer index;

        public Index(Integer index) {
            this.index = index;
        }
    }
    */
}
