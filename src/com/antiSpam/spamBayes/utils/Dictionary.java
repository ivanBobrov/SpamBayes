package com.antiSpam.spamBayes.utils;


import weka.core.*;

import java.util.*;

public class Dictionary {
    private static final int N_GRAM_LENGTH = 3;
    private static Dictionary instance = null;

    private HashMap<String, Integer> indexMap = new HashMap<String, Integer>();

    public static Dictionary getInstance() {
        if (instance == null) {
            instance = new Dictionary();
        }

        return instance;
    }


    private Dictionary() {

    }

    public int getDictionarySize() {
        return indexMap.size();
    }

    public Integer getIndex(String base) {
        if (indexMap.containsKey(base)) {
            return indexMap.get(base);
        }

        throw new IllegalArgumentException("No such nGram in dictionary");
    }

    public Set<String> getBaseSet() {
        return indexMap.keySet();
    }

    public Integer addBase(String base) {
        /*
        if (indexMap.containsKey(base)) {
            throw something
        }
        */

        Integer newIndex = indexMap.size(); //always bigger than anyone
        indexMap.put(base, newIndex);

        return newIndex;
    }

    public ArrayList<String> parseString(String string) {
        ArrayList<String> bases = new ArrayList<String>();

        if (string.length() > N_GRAM_LENGTH) {
            for (int i = 0; i < string.length() - (N_GRAM_LENGTH - 1); i++) {
                bases.add(string.substring(i, i + N_GRAM_LENGTH));
            }
        }

        return bases;
    }

    public boolean containsBase(String base) {
        return indexMap.containsKey(base);
    }

    //Maybe there should be many content sets with no names in the future
    public static Instances generateInstances(ContentSet hamSet, ContentSet spamSet) {
        Instances instances = getNewEmptyInstances();

        fillInstances(instances, hamSet, 0);
        fillInstances(instances, spamSet, 1);

        instances.setClassIndex(instances.numAttributes() - 1);

        return instances;
    }

    public static Instances getNewEmptyInstances() {
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

        Instances result = new FastInstances("Relation", attributes, nGramCount);
        result.setClassIndex(result.numAttributes() - 1);
        return result;
    }

    public static Instances generateInstances(ContentSet set, double classValue) {
        Instances instances = getNewEmptyInstances();
        fillInstances(instances, set, classValue);

        instances.setClassIndex(instances.numAttributes() - 1);

        return instances;
    }

    //It is awful. TODO: do something with this.
    public static Instance generateInstance(String text) {
        Dictionary dictionary = Dictionary.getInstance();
        int nGramCount = dictionary.getDictionarySize();

        ArrayList<String> bases = dictionary.parseString(text);

        //For uniqueness of nGram in the set
        SortedMap<Integer, Double> nGrams = new TreeMap<Integer, Double>();

        for (String base : bases) {
            if (dictionary.containsBase(base)) {
                Integer index = dictionary.getIndex(base);
                Double count = nGrams.get(index);
                if (count == null) {
                    count = 0d;
                }

                count++;
                nGrams.put(index, count);
            }
        }

        int[] indices = new int[nGrams.size() + 1];
        double[] attValues = new double[nGrams.size() + 1];

        int i = 0;
        for (Map.Entry<Integer, Double> entry : nGrams.entrySet()) {
            indices[i] = entry.getKey();
            attValues[i] = entry.getValue();
            i++;
        }

        indices[indices.length - 1] = nGramCount; //AttributeVectorLength - 1
        attValues[attValues.length - 1] = 0d;

        return new SparseInstance(1.0, attValues, indices, nGramCount + 1);
    }

    private static void fillInstances(Instances instances, ContentSet contentSet, double classValue) {
        Iterator<SortedMap<Integer, Integer>> iterator = contentSet.getStringSet();

        while (iterator.hasNext()) {
            SortedMap<Integer, Integer> string = iterator.next();

            int[] indices = new int[string.size() + 1];
            double[] attValues = new double[string.size() + 1];

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
    }
}
