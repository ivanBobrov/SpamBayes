package com.antiSpam.spamBayes.bayesSpamFilter;

import java.io.IOException;


public class BayesSpamFilterException extends Exception {
    public BayesSpamFilterException() {
        super();
    }

    public BayesSpamFilterException(String message) {
        super(message);
    }

    public BayesSpamFilterException(String message, Throwable cause) {
        super(message, cause);
    }

    public BayesSpamFilterException(Throwable cause) {
        super(cause);
    }
}
