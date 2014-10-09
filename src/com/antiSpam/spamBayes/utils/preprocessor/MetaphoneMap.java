package com.antiSpam.spamBayes.utils.preprocessor;

import java.util.HashMap;

//TODO: fill it
class MetaphoneMap {
    public static HashMap<String, String> tripleMap = new HashMap<String, String>() {{
        put("SCH", "SH");
    }};

    public static HashMap<String, String> doubleMap = new HashMap<String, String>() {{
        put("КН", "H");
        put("SC", "SC");
        put("SH", "SH");
    }};

    public static HashMap<String, String> singleMap = new HashMap<String, String>() {{
        put("G", "G"); //just the same
        put("N", "N");
        put("S", "S");

        put("К", "K");
        put("Г", "G");
        put("Ґ", "G");
        put("Н", "N");
        put("6", "S");
        put("С", "S");
        put("Ш", "SH");
        put("Щ", "SH");


    }};

}
