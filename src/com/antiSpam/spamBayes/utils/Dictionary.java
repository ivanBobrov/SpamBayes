package com.antiSpam.spamBayes.utils;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public Integer getIndex(String base) {
        if (indexMap.containsKey(base)) {
            return indexMap.get(base);
        } else {
            return addBase(base);
        }
    }

    public String getBase(int index) {
        for (Map.Entry<String, Integer> entry : indexMap.entrySet()) {
            if (entry.getValue() == index) {
                return entry.getKey();
            }
        }

        return null;//throw smthg
    }

    private Integer addBase(String base) {
        if (indexMap.containsKey(base)) {
            //throw something
        }

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

}
