package com.antiSpam.spamBayes.utils;


public interface SpamTextPreprocessor {

    /*
    Changes text for better classification
     */
    String convert(String text);
}
