package com.odkl.moderation.text.server.domain.bayes.utils.preprocessor;

public interface SpamTextPreprocessor {

    /*
    Changes text for better classification
     */
    String convert(String text);
}
