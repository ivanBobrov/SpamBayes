package com.odkl.moderation.text.server.domain.bayes.filter;

import java.util.Iterator;

import com.odkl.moderation.text.server.domain.bayes.SpamFilter;
import com.odkl.moderation.text.server.domain.bayes.filter.adaptation.InstanceAdapter;
import com.odkl.moderation.text.server.domain.bayes.filter.adaptation.InstancesAdapter;
import com.odkl.moderation.text.server.domain.bayes.utils.ArrayIterator;
import com.odkl.moderation.text.server.domain.bayes.utils.Dictionary;

import weka.classifiers.bayes.NaiveBayesMultinomialUpdateable;

/*
 TODO: creating new filter without ham or spam (?)
 TODO: making headerInstances from trainingInstances and training instances from something packed
*/
public class BayesSpamFilter implements SpamFilter {

    /*
    They are not enum only because of WEKA input parameters, which are ints.
     */
    public static final int SPAM_CLASS_VALUE = 1;
    public static final int HAM_CLASS_VALUE = 0;
    public static final int UNDEFINED_CLASS_VALUE = -1;

    public static final double DEFAULT_THRESHOLD = 0.5;

    private final InstancesAdapter trainingInstances;
    private final InstancesAdapter headerInstances;
    private final NaiveBayesMultinomialUpdateable classifier;
    private final Dictionary dictionary;

    public BayesSpamFilter(String[] hamSet, String[] spamSet) throws BayesSpamFilterException {
        this(new ArrayIterator<String>(hamSet), new ArrayIterator<String>(spamSet));
    }

    public BayesSpamFilter(Iterator<String> hamSetIterator, Iterator<String> spamSetIterator)
            throws BayesSpamFilterException {
        /*
        It is so complicated, because WEKA always copy memory while adding new Instances and Instance's.
        I don't know why they did that, but it makes building very slow. So I added InstanceAdapter,
        InstancesAdapter and FastInstances. The latter added, because I can't modify protected fields
        m_Instances and m_Attributes.
         */

        trainingInstances = new InstancesAdapter();
        headerInstances = new InstancesAdapter();
        dictionary = new Dictionary();

        while (hamSetIterator.hasNext()) {
            String next = hamSetIterator.next();
            if (next != null) {
                InstanceAdapter newInstance
                        = new InstanceAdapter(next, HAM_CLASS_VALUE, true, dictionary);
                trainingInstances.add(newInstance.getRawInstance());
            }
        }

        while (spamSetIterator.hasNext()) {
            String next = spamSetIterator.next();
            if (next != null) {
                InstanceAdapter newInstance
                        = new InstanceAdapter(next, SPAM_CLASS_VALUE, true, dictionary);
                trainingInstances.add(newInstance.getRawInstance());
            }
        }

        trainingInstances.addAllAttributes(dictionary);
        headerInstances.addAllAttributes(dictionary);

        try {
            classifier = new NaiveBayesMultinomialUpdateable();
            classifier.buildClassifier(trainingInstances.getRawInstances());
        } catch (Exception exception) {
            throw new BayesSpamFilterException("Can't build classifier", exception);
        }
    }

    @Override
    public void reportSpam(String spamText) throws BayesSpamFilterException {
        //TODO: remove old instances?
        try {
            InstanceAdapter newInstance
                    = new InstanceAdapter(spamText, SPAM_CLASS_VALUE, false, dictionary);
            trainingInstances.add(newInstance.getRawInstance());
            classifier.updateClassifier(newInstance.getRawInstance());
        } catch (Exception exception) {
            throw new BayesSpamFilterException("Can't update classifier with new spam instance", exception);
        }
        //TODO:implement
    }

    @Override
    public boolean check(String text) throws BayesSpamFilterException {
        return check(text, DEFAULT_THRESHOLD);
    }

    @Override
    public boolean check(String text, double level) throws BayesSpamFilterException {
        //TODO: check for not built classifier (?)
        try {
            InstanceAdapter newInstance
                    = new InstanceAdapter(text, UNDEFINED_CLASS_VALUE, false, dictionary);
            newInstance.setDataset(headerInstances.getRawInstances());

            double classLabel = classifier
                    .distributionForInstance(newInstance.getRawInstance())[SPAM_CLASS_VALUE];

            return classLabel > level;
        } catch (Exception exception) {
            throw new BayesSpamFilterException("Can't check new post message", exception);
        }
    }

    /*
    public void storeSpamFilter() throws IOException {
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("spamFilter.filter"));
            //outputStream.writeObject(trainingInstances);
            //outputStream.writeObject(headerInstances);
            outputStream.writeObject(classifier);
            outputStream.flush();
            outputStream.close();

            Dictionary.getInstance().serializeDictionary();
        } catch (Exception exception) {
            throw new IOException("Can't store filter", exception);
        }
    }

    public void loadSpamFilter() throws IOException {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("spamFilter.filter"));

            //trainingInstances   = (InstancesAdapter)    inputStream.readObject();
            //headerInstances     = (InstancesAdapter)    inputStream.readObject();
            classifier          = (NaiveBayesMultinomialUpdateable)          inputStream.readObject();
            inputStream.close();

            Dictionary.getInstance().deserializeDictionary();
        } catch (Exception exception) {
            throw new IOException("Can't restore filter", exception);
        }
    }
    */
}
