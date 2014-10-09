package com.antiSpam.spamBayes;

import com.antiSpam.spamBayes.utils.BayesSpamFilterException;

import java.io.IOException;
import java.io.Serializable;


public interface SpamFilter extends Serializable {

    /*
    Adds to training set new spam message (but don't rebuilds classifier!).
     */
    void reportSpam(String spamText);

    /*
    Classifies new text. Returns true, if incoming message is spam
     */
    boolean check(String text) throws BayesSpamFilterException;

    /*
    Builds classifier for the first time
     */
    void build() throws BayesSpamFilterException;

    /*
    Rebuilds classifier with new data
     */
    void update();

    /*
    Stores spam filter to disk.
     */
    void storeSpamFilter() throws IOException;

    /*
    Restore spam filter
     */
    void loadSpamFilter() throws IOException;
}
