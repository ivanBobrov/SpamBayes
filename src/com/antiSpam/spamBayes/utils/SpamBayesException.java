package com.antiSpam.spamBayes.utils;

import java.io.IOException;


public class SpamBayesException extends IOException {
    public SpamBayesException() {
        super();
    }

    public SpamBayesException(String message) {
        super(message);
    }

    public SpamBayesException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpamBayesException(Throwable cause) {
        super(cause);
    }
}
