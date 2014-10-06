package com.antiSpam.spamBayes.bayesSpamFilter;


import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;


public class FastInstances extends Instances implements Serializable {
    public FastInstances(Reader reader) throws IOException {
        super(reader);
    }

    public FastInstances(Instances dataset) {
        super(dataset);
    }

    public FastInstances(Instances dataset, int capacity) {
        super(dataset, capacity);
    }

    public FastInstances(Instances source, int first, int toCopy) {
        super(source, first, toCopy);
    }

    public FastInstances(String name, FastVector attInfo, int capacity) {
        super(name, attInfo, capacity);
    }

    @Override
    public void add(Instance instance) {
        instance.setDataset(this);
        m_Instances.addElement(instance);
    }

    public void replaceAttributes(FastVector attributes) {
        m_Attributes = attributes;
        /*for (int i = 0; i < numAttributes(); i++) {
            attribute(i).setIndex(i);
        }*/

    }
}