package com.antiSpam.spamBayes;

import com.antiSpam.spamBayes.bayesSpamFilter.BayesSpamFilter;
import com.antiSpam.spamBayes.utils.BayesSpamFilterException;


public class MainLoader {
    public static void main(String args[]) {
        try {
            SpamFilter spamFilter = new BayesSpamFilter("ham.json");
            spamFilter.build();
        } catch (BayesSpamFilterException exception) {
            exception.printStackTrace();
        }
    }

}
