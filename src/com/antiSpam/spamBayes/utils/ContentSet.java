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

        stringList.add(dictionary.parseString(string, true));
    }

    public int getStringCount() {
        return stringList.size();
    }

    public Iterator<SortedMap<Integer, Integer>> getStringSet() {
        return stringList.iterator();
    }


}
