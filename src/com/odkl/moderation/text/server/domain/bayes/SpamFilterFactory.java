package com.odkl.moderation.text.server.domain.bayes;

import com.odkl.moderation.text.server.domain.bayes.filter.BayesSpamFilter;
import com.odkl.moderation.text.server.domain.bayes.filter.BayesSpamFilterException;

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
