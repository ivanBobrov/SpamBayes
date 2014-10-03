package com.antiSpam.spamBayes;


import com.antiSpam.spamBayes.utils.BayesSpamFilterException;

public interface SpamFilter {

    /*
    Adds to training set new spam message
     */
    void reportSpam(String spamText);

    /*
    Classifies new text. Returns true, if incoming message is spam
     */
    boolean check(String text);

    /*
    Builds classifier for the first time
     */
    void build() throws BayesSpamFilterException;

    /*
    Rebuilds classifier with new data
     */
    void update();

}
