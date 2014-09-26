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
            Integer index;
            if (dictionary.containsBase(base)) {
                 index = dictionary.getIndex(base);
            } else {
                index = dictionary.addBase(base);
            }

            if (stringMap.containsKey(index)) {
                Integer count = stringMap.get(index);
                count++;
                stringMap.put(index, count);
            } else {
                stringMap.put(index, 1);
            }
        }

        if (!stringMap.isEmpty()) {
            stringList.add(stringMap);
        }
    }

    public int getStringCount() {
        return stringList.size();
    }

    public Iterator<SortedMap<Integer, Integer>> getStringSet() {
        return stringList.iterator();
    }


}
