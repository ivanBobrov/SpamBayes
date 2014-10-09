package com.antiSpam.spamBayes.utils.preprocessor;


import com.antiSpam.spamBayes.utils.preprocessor.SpamTextPreprocessor;


public class MetaphonePreprocessor implements SpamTextPreprocessor {

    @Override
    public String convert(String text) {
        return text;
    }

    public String convert(StringBuffer text) {
        return text.toString();
    }

    private String encodeThreeSymbol(String string) {
        for (int i = 0; i < string.length() - 3; i++) {
            String base = string.substring(i, i + 3).toUpperCase();

        }

        return string;
    }

}
