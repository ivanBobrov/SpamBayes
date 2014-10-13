package com.antiSpam.spamBayes;

import com.antiSpam.spamBayes.bayesSpamFilter.BayesSpamFilterException;

import java.io.Serializable;
import java.util.Iterator;


public interface SpamFilter extends Serializable {

    /*
    Adds to training set new spam message
     */
    void reportSpam(String spamText) throws BayesSpamFilterException;

    /*
    Classifies new text. Returns true, if incoming message is spam.
    Level [0...1]. Level means threshold of classification.
        0 means all spam, 1 means all ham
     */
    boolean check(String text, double level) throws BayesSpamFilterException;

    boolean check(String text) throws BayesSpamFilterException;

    /*
    Builds classifier for the first time
     */
    void build(String[] hamSet, String[] spamSet) throws BayesSpamFilterException;

    void build(Iterator<String> hamSetIterator, Iterator<String> spamSetIterator)
            throws BayesSpamFilterException;
}
