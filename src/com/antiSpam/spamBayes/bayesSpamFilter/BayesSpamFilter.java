package com.antiSpam.spamBayes.bayesSpamFilter;

import com.antiSpam.spamBayes.SpamFilter;
import com.antiSpam.spamBayes.utils.BayesSpamFilterException;
import com.antiSpam.spamBayes.utils.JSONFileReader;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomial;

import java.io.IOException;
import java.util.Iterator;

/*
 TODO: check for null file names in constructor
 TODO: creating new filter without ham or spam
*/
public class BayesSpamFilter implements SpamFilter {
    private String hamFilename;
    private String spamFilename;
    private InstancesAdapter instancesAdapter = null;
    private Classifier classifier = null;

    public BayesSpamFilter(String hamFilename, String spamFilename) {
        this.hamFilename = hamFilename;
        this.spamFilename = spamFilename;
    }

    public BayesSpamFilter(String hamFilename) {
        this(hamFilename, null);
    }

    @Override
    public void build() throws BayesSpamFilterException {
        instancesAdapter = new InstancesAdapter();

        try {
            Iterator<String> hamFileIterator = new JSONFileReader(hamFilename, 0, 10000);
            while (hamFileIterator.hasNext()) {
                InstanceAdapter newInstance = new InstanceAdapter(hamFileIterator.next(), 0d);
                instancesAdapter.add(newInstance.getRawInstance());
            }
            instancesAdapter.addAllAttributes();

            if (spamFilename != null) {
                Iterator<String> spamFileIterator = new JSONFileReader(spamFilename);
                while (spamFileIterator.hasNext()) {
                    InstanceAdapter newInstance = new InstanceAdapter(spamFileIterator.next(), 1d);
                    instancesAdapter.add(newInstance.getRawInstance());
                }
            }
        } catch (IOException exception) {
            throw new BayesSpamFilterException("Can't read file", exception);
        }

        try {
            classifier = new NaiveBayesMultinomial();
            classifier.buildClassifier(instancesAdapter.getRawInstances());
        } catch (Exception exception) {
            throw new BayesSpamFilterException("Can't build classifier", exception);
        }
    }

    @Override
    public void update() {

    }

    @Override
    public void reportSpam(String spamText) {

    }

    @Override
    public boolean check(String text) {
        return false;
    }
}
