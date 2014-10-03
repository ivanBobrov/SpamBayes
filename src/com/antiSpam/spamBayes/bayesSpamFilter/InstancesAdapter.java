package com.antiSpam.spamBayes.bayesSpamFilter;

import com.antiSpam.spamBayes.utils.Dictionary;
import weka.core.Instance;
import weka.core.Instances;


public class InstancesAdapter {
    private Instances instances = null;

    public InstancesAdapter() {
        instances = Dictionary.getNewEmptyInstances();
    }

    public void add(Instance instance) {
        instances.add(instance); //TODO: implement
    }

    public Instances getRawInstances() {
        return instances;
    }
}
