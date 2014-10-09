package com.antiSpam.spamBayes.bayesSpamFilter;

import com.antiSpam.spamBayes.SpamFilter;
import com.antiSpam.spamBayes.utils.BayesSpamFilterException;
import com.antiSpam.spamBayes.utils.Dictionary;
import com.antiSpam.spamBayes.utils.JSONFileReader;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayesMultinomial;

import java.io.*;
import java.util.Iterator;

/*
 TODO: check for null file names in constructor
 TODO: creating new filter without ham or spam (?)
 TODO: making headerInstances from trainingInstances
*/
public class BayesSpamFilter implements SpamFilter {
    public static final int LOADING_MESSAGES_COUNT = 51200;

    /*
    They are not enum only because of WEKA input parameters, which are ints.
     */
    public static final int SPAM_CLASS_INDEX = 1;
    public static final int HAM_CLASS_INDEX  = 0;
    public static final int UNDEFINED_CLASS_INDEX  = -1;

    private String hamFilename;
    private String spamFilename;
    private InstancesAdapter trainingInstances = null;
    private InstancesAdapter headerInstances = null;
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
        trainingInstances = new InstancesAdapter();
        headerInstances = new InstancesAdapter();

        /*
        It is so complicated, because WEKA always copy memory while adding new Instances and Instance's.
        I don't know why they did that, but it makes building very slow. So I added InstanceAdapter,
        InstancesAdapter and FastInstances. The latter added, because I can't modify protected fields
        m_Instances and m_Attributes.
         */

        try {
            Iterator<String> hamFileIterator = new JSONFileReader(hamFilename, 0, LOADING_MESSAGES_COUNT);
            while (hamFileIterator.hasNext()) {
                InstanceAdapter newInstance = new InstanceAdapter(hamFileIterator.next(), HAM_CLASS_INDEX, true);
                trainingInstances.add(newInstance.getRawInstance());
            }

            if (spamFilename != null) {
                Iterator<String> spamFileIterator = new JSONFileReader(spamFilename, 0, LOADING_MESSAGES_COUNT);
                while (spamFileIterator.hasNext()) {
                    InstanceAdapter newInstance = new InstanceAdapter(spamFileIterator.next(), SPAM_CLASS_INDEX, true);
                    trainingInstances.add(newInstance.getRawInstance());
                }
            }

            trainingInstances.addAllAttributes();
            headerInstances.addAllAttributes();
        } catch (IOException exception) {
            throw new BayesSpamFilterException("Can't read file", exception);
        }

        try {
            classifier = new NaiveBayesMultinomial();
            classifier.buildClassifier(trainingInstances.getRawInstances());
        } catch (Exception exception) {
            throw new BayesSpamFilterException("Can't build classifier", exception);
        }
    }

    @Override
    public void update() {
        //TODO: implement
    }

    @Override
    public void reportSpam(String spamText) {
        //TODO: implement
    }

    @Override
    public boolean check(String text) throws BayesSpamFilterException {
        //TODO: check for not built classifier (?)
        try {
            InstanceAdapter newInstance = new InstanceAdapter(text, UNDEFINED_CLASS_INDEX, false);
            headerInstances.add(newInstance.getRawInstance());

            double classLabel = classifier.distributionForInstance(newInstance.getRawInstance())[SPAM_CLASS_INDEX];

            headerInstances.removeLastInstance();

            return classLabel > 0.5; //TODO: magic number
        } catch (Exception exception) {
            throw new BayesSpamFilterException("Can't check new post message", exception);
        }
    }

    @Override
    public void storeSpamFilter() throws IOException {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("spamFilter.filter"));
            outputStream.writeObject(hamFilename);
            outputStream.writeObject(spamFilename);
            outputStream.writeObject(trainingInstances);
            outputStream.writeObject(headerInstances);
            outputStream.writeObject(classifier);
            outputStream.flush();
            outputStream.close();

            Dictionary.getInstance().serializeDictionary();
        } catch (Exception exception) {
            throw new IOException("Can't store bayesSpamFilter", exception);
        }
    }

    @Override
    public void loadSpamFilter() throws IOException {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("spamFilter.filter"));

            hamFilename         = (String)              inputStream.readObject();
            spamFilename        = (String)              inputStream.readObject();
            trainingInstances   = (InstancesAdapter)    inputStream.readObject();
            headerInstances     = (InstancesAdapter)    inputStream.readObject();
            classifier          = (Classifier)          inputStream.readObject();
            inputStream.close();

            Dictionary.getInstance().deserializeDictionary();
        } catch (Exception exception) {
            throw new IOException("Can't restore bayesSpamFilter", exception);
        }
    }
}
