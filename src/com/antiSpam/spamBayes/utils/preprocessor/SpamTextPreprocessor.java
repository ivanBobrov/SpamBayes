package com.antiSpam.spamBayes.utils.preprocessor;


public interface SpamTextPreprocessor {

    /*
    Changes text for better classification
     */
    String convert(String text);
}
