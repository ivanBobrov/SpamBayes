package com.antiSpam.spamBayes.bayesSpamFilter;

import com.antiSpam.spamBayes.utils.Dictionary;
import weka.core.Instance;
import weka.core.SparseInstance;
import java.util.Map;
import java.util.SortedMap;


public class InstanceAdapter {
    private SparseInstance instance;

    public InstanceAdapter(String text, double classValue) {
        Dictionary dictionary = Dictionary.getInstance();
        SortedMap<Integer, Integer> parsedString = dictionary.parseString(text, true);

        /*
        As mentioned at stackOverflow.com, there is no better way to convert Integer[] to int[]
         */
        int[] indices = new int[parsedString.size() + 1];
        double[] attValues = new double[parsedString.size() + 1];

        int numAttributes = dictionary.getDictionarySize() + 1;
        indices[0] = 0;
        attValues[0] = classValue;

        int i = 1;
        for (Map.Entry<Integer, Integer> entry : parsedString.entrySet()) {
            indices[i] = entry.getKey();
            attValues[i] = entry.getValue();
            i++;
        }



        instance = new SparseInstance(1.0, attValues, indices, numAttributes);
    }

    public Instance getRawInstance() {
        return instance;
    }

}
