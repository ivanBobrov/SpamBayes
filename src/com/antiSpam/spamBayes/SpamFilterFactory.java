package com.antiSpam.spamBayes;

import com.antiSpam.spamBayes.bayesSpamFilter.BayesSpamFilter;
import com.antiSpam.spamBayes.bayesSpamFilter.BayesSpamFilterException;

import java.util.Iterator;


public class SpamFilterFactory {

    public SpamFilter createSpamFilter(String[] hamSet, String[] spamSet) throws BayesSpamFilterException {
        SpamFilter newSpamFilter = new BayesSpamFilter();
        newSpamFilter.build(hamSet, spamSet);
        return newSpamFilter;
    }

    public SpamFilter createSpamFilter(Iterator<String> hamSetIterator, Iterator<String> spamSetIterator)
            throws BayesSpamFilterException {
        SpamFilter newSpamFilter = new BayesSpamFilter();
        newSpamFilter.build(hamSetIterator, spamSetIterator);
        return newSpamFilter;
    }
}
