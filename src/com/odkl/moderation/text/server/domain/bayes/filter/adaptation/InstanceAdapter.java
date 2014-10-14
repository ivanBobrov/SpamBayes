package com.odkl.moderation.text.server.domain.bayes.filter.adaptation;

import com.odkl.moderation.text.server.domain.bayes.utils.Dictionary;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SparseInstance;

import java.util.Map;
import java.util.SortedMap;

public class InstanceAdapter {
        private final SparseInstance instance;

        public InstanceAdapter(String text, double classValue, boolean dictionaryModificationEnabled) {
                Dictionary dictionary = Dictionary.getInstance();
                SortedMap<Integer, Integer> parsedString = dictionary.parseString(text, dictionaryModificationEnabled);

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

        public void setDataset(Instances instances) {
                this.instance.setDataset(instances);
        }

}
