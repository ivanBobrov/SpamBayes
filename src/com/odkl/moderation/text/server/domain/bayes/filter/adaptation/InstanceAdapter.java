package com.odkl.moderation.text.server.domain.bayes.filter.adaptation;

import com.odkl.moderation.text.server.domain.bayes.utils.Dictionary;

import gnu.trove.map.TIntShortMap;
import gnu.trove.procedure.TIntShortProcedure;
import weka.core.Instance;
import weka.core.Instances;

public class InstanceAdapter {

    private final Instance instance;

    public InstanceAdapter(String text, double classValue, boolean dictionaryModificationEnabled,
            Dictionary dictionary) {
        TIntShortMap parsedString = dictionary.parseString(text, dictionaryModificationEnabled);

        int numAttributes = dictionary.getDictionarySize() + 1;

        int[] indices;
        double[] values;

        // Если SparseInstance видит 0-значение то игнорирует его и запускает перекопирование массивов, поэтому
        // в случае 0-значения класса мы сами готовим массив необходимой длины, что позволяет избежать лишнего
        // копирования
        if (classValue != 0) {
            indices = new int[parsedString.size() + 1];
            values = new double[parsedString.size() + 1];

            indices[0] = 0;
            values[0] = classValue;

            if (parsedString.size() > 0) {
                parsedString.forEachEntry(new ArraySetter(indices, values, 1));
            }

            if (parsedString.size() > 1) {
                sort(indices, values, 1, indices.length - 1);
            }
        } else {
            indices = new int[parsedString.size()];
            values = new double[parsedString.size()];

            if (parsedString.size() > 0) {
                parsedString.forEachEntry(new ArraySetter(indices, values, 0));
            }

            if (parsedString.size() > 1) {
                sort(indices, values, 0, indices.length - 1);
            }
        }

        instance = new NoCopySparseInstance(1.0, values, indices, numAttributes);
    }


    private static void sort(int[] indicies, double[] values, final int leftLimit, final int rightLimit) {
        int leftIndex = leftLimit;
        int rightIndex = rightLimit;

        int pivot = indicies[(leftIndex + rightIndex) >>> 1];

        while (leftIndex <= rightIndex) {
            while (indicies[leftIndex] < pivot) {
                leftIndex++;
            }

            while (pivot < indicies[rightIndex]) {
                rightIndex--;
            }

            if (leftIndex <= rightIndex) {
                int tempWeight = indicies[leftIndex];
                indicies[leftIndex] = indicies[rightIndex];
                indicies[rightIndex] = tempWeight;

                double tempItem = values[leftIndex];
                values[leftIndex] = values[rightIndex];
                values[rightIndex] = tempItem;

                leftIndex++;
                rightIndex--;
            }
        }

        if (leftLimit < rightIndex) {
            sort(indicies, values, leftLimit, rightIndex);
        }

        if (leftIndex < rightLimit) {
            sort(indicies, values, leftIndex, rightLimit);
        }
    }

    public Instance getRawInstance() {
        return instance;
    }

    public void setDataset(Instances instances) {
        this.instance.setDataset(instances);
    }

    private static class ArraySetter implements TIntShortProcedure {

        private final int[] indicies;

        private final double[] values;

        private int index;

        private ArraySetter(int[] indicies, double[] values, int index) {
            this.indicies = indicies;
            this.values = values;
            this.index = index;
        }

        @Override
        public boolean execute(int a, short b) {
            indicies[index] = a;
            values[index] = b;

            index++;

            return true;
        }

    }

}
