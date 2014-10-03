package com.antiSpam.spamBayes.bayesSpamFilter;

import com.antiSpam.spamBayes.utils.Dictionary;

import weka.core.Instance;
import weka.core.SparseInstance;

import java.util.Map;
import java.util.SortedMap;


public class InstanceAdapter {
    private Instance instance;

    public InstanceAdapter(String text, double classValue) {
        /*
        SortedMap<Integer, Integer> string = Dictionary.getInstance().parseString(text);

        //TODO: find best way to convert sorted map to array
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

        instance = new SparseInstance(1.0, attValues, indices, numAttributes);
        */
        instance = Dictionary.generateInstance(text);
    }

    public Instance getRawInstance() {
        return instance;
    }

}
