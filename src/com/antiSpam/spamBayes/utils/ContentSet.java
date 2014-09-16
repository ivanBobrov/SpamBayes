package com.antiSpam.spamBayes.utils;


import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class ContentSet {
    private Dictionary dictionary;
    private HashMap<Integer, Integer> values = new HashMap<Integer, Integer>();

    public ContentSet() {
        this.dictionary = Dictionary.getInstance();
    }

    public void addString(String string) {
        ArrayList<String> bases = dictionary.parseString(string);

        for (String base : bases) {
            Integer index = dictionary.getIndex(base);

            if (values.containsKey(index)) {
                Integer count = values.get(index);
                count++;
                values.put(index, count);
            } else {
                values.put(index, 1);
            }
        }
    }

    public int getNGramCount() {
        return values.size();
    }

    public Set<Integer> getNGramIndicesSet() {
        return values.keySet();
    }

    //Maybe many content sets with no names in the future
    public static Instances generateInstances(ContentSet hamSet, ContentSet spamSet) {
        //TODO: implement it
        return null;
    }

    public static Instances generateInstances(ContentSet set) {
        FastVector attributes = new FastVector(set.getNGramCount());

        for (Integer index : set.getNGramIndicesSet()) {
            attributes.addElement(new Attribute(Dictionary.getInstance().getBase(index)));
        }

        Instances instances = new Instances("testSet", attributes, 100);

        //Тут я забыл, что строки нужно сохранять. Нужно contentSet перепиливать.

        return null;
    }
}
