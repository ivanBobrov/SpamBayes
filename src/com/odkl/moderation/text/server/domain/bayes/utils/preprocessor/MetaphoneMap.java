package com.odkl.moderation.text.server.domain.bayes.utils.preprocessor;

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

        public static HashMap<String, String> cyrillicSingleMap = new HashMap<String, String>() {{
                //А
                put("\u0041", "А");
                put("\uFF21", "А");
                put("\u0391", "А");
                put("\u1EA0", "А");
                put("\u1FBC", "А");
                put("\u1E00", "А");
                put("\u0386", "А");
                put("\u0061", "А");
                put("\uFF41", "А");
                put("\u1EA1", "А");
                put("\u1E01", "А");
                put("\u0105", "А");
                put("\u01A8", "А");

                //Б
                put("\u0495", "Б");

                //В
                put("\u0062", "В"); //small

                //Г
                put("\u0491", "Г"); //small

                //Д
                put("\u03B4", "Д"); //small

                //Е
                put("\u0045", "Е");
                put("\u0065", "Е"); //small

                //Ж
                put("\u0497", "Ж"); //small

                //З
                put("\u0033", "З");

                //И
                put("\u0055", "И");

                //Й
                put("\u04E3", "Й"); //small
                put("\u048B", "Й"); //small
                put("\u04E3", "Й"); //small

                //К
                put("\u0138", "К");
                put("\u004B", "К");
                put("\u006B", "К"); //small

                //Л
                put("\u039B", "Л");
                put("\u03BB", "Л");

                //М
                put("ʍ", "М");
                put("\u006D", "М"); //small

                //Н
                put("\u029C", "Н");
                put("\u0048", "Н");
                put("\u0068", "Н"); //small

                //О
                put("0", "О"); //zero
                put("\u004F", "О");
                put("\u039F", "О");
                put("\u006F", "О"); //small
                put("\u00F3", "О"); //small

                //П
                put("\u03A0", "П");

                //Р
                put("\u03A1", "Р");
                put("\u0050", "Р");
                put("\u0070", "Р"); //small

                //С
                put("\u0043", "С");
                put("\u0063", "С"); //small

                //Т
                put("M", "Т");
                put("\u0054", "Т");
                put("\u0074", "Т"); //small

                //У
                put("\u0393", "У");
                put("\uFF59", "У");
                put("\u0079", "У");
                put("\u0059", "У");
                put("\u1EF5", "У");
                put("\u01B4", "У");

                //Ф

                //Х
                put("\u0058", "Х");
                put("\u0078", "Х"); //small
                put("\u03A7", "Х");

                //Ц
                //Ч
                put("\u056F", "Ч");
                //Ш
                //Щ
                //Ъ

                //Ы
                put("\u04F9", "Ы");

                //Ь
                put("\u0E52", "Ь");

                //Э
                //Ю

                //Я

        }};

}
