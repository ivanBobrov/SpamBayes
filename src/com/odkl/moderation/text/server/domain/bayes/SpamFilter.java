package com.odkl.moderation.text.server.domain.bayes;

import com.odkl.moderation.text.server.domain.bayes.filter.BayesSpamFilterException;

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

}
