package com.odkl.moderation.text.server.domain.bayes.utils;

import com.odkl.moderation.text.server.domain.bayes.utils.preprocessor.NewTranslitMetaphone2;
import com.odkl.moderation.text.server.domain.bayes.utils.preprocessor.NonAlphabeticSymbolsPreprocessor;
import com.odkl.moderation.text.server.domain.bayes.utils.preprocessor.SpamTextPreprocessor;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;

public class Dictionary {
        private static final String REGEX = "\\s|\\.|,|\"|-|\\s+|\\?|!|:|–|\\)|\\(|\\*|\\[|\\]|\\|/|;|»|«|#";
        private static final String DICTIONARY_SERIALIZED_FILENAME = "dictionary.serial";
        private static Dictionary instance = null;

        private int nGramLength = 3;
        private boolean preprocessEnabled = false;
        private SpamTextPreprocessor[] textPreprocessor;

        private HashMap<String, Integer> indexMap = new HashMap<String, Integer>();

        public static Dictionary getInstance() {
                if (instance == null) {
                        instance = new Dictionary();
                }

                return instance;
        }

        private Dictionary() {
                this.textPreprocessor = new SpamTextPreprocessor[2];

                this.textPreprocessor[0] = new NonAlphabeticSymbolsPreprocessor();
                this.textPreprocessor[1] = new NewTranslitMetaphone2();
        }

        public int getDictionarySize() {
                return indexMap.size();
        }

        public void setNGramLength(int nGramLength) {
                this.nGramLength = nGramLength;
        }

        public void setPreprocessEnabled(boolean enabled) {
                this.preprocessEnabled = enabled;
        }

        public boolean containsBase(String base) {
                return indexMap.containsKey(base);
        }

        public Integer addBase(String base) {
                if (indexMap.containsKey(base)) {
                        throw new IllegalStateException("Already contains base " + base);
                }

        /*
        Always bigger than anyone.

        One added, because zero is classIndex for classifier and
        index of nGram corresponds to its ordinal value in Instance.
        So, in other words, value for zero index already exists and it is classIndex.
         */
                Integer newIndex = indexMap.size() + 1;
                indexMap.put(base, newIndex);

                return newIndex;
        }

        public Integer getIndex(String base) {
                if (indexMap.containsKey(base)) {
                        return indexMap.get(base);
                }

                throw new IllegalArgumentException("No such nGram in dictionary");
        }

        public TreeMap<Integer, Integer> parseString(String text, boolean modificationEnabled) {
                //TODO: Check for null string and string with length less than nGramLength
                TreeMap<Integer, Integer> parsedString = new TreeMap<Integer, Integer>();
                String preprocessedText = preprocessEnabled ? preprocessString(text) : text;

                //String[] words = preprocessedText.split(REGEX);
                //for (String word : words) {
                ArrayList<String> bases = getBases(preprocessedText);

                for (String base : bases) {
                        Integer index;
                        if (containsBase(base)) {
                                index = getIndex(base);
                        } else if (modificationEnabled) {
                                index = addBase(base);
                        } else {
                                continue;
                        }

                        if (parsedString.containsKey(index)) {
                                Integer count = parsedString.get(index);
                                count++;
                                parsedString.put(index, count);
                        } else {
                                parsedString.put(index, 1);
                        }
                }
                //}

                return parsedString;
        }

        private ArrayList<String> getBases(String string) {
                ArrayList<String> bases = new ArrayList<String>();

                //variant without spaces before and after
                if (string.length() >= nGramLength) {
                        for (int i = 0; i < string.length() - (nGramLength - 1); i++) {
                                bases.add(string.substring(i, i + nGramLength));
                        }
                }

                return bases;
        }

        public String preprocessString(String text) {
                String res = text; //TODO: delete. It's for debug
                for (SpamTextPreprocessor preprocessor : textPreprocessor) {
                        res = preprocessor.convert(res);
                }

                return res;
        }

        public Set<String> getBaseSet() {
                return indexMap.keySet();
        }

        public void serializeDictionary() throws IOException {
                ObjectOutputStream outputStream = new ObjectOutputStream(
                        new FileOutputStream(DICTIONARY_SERIALIZED_FILENAME));
                outputStream.writeObject(indexMap);
                outputStream.flush();
                outputStream.close();
        }

        public void deserializeDictionary() throws IOException {
                try {
                        ObjectInputStream inputStream = new ObjectInputStream(
                                new FileInputStream(DICTIONARY_SERIALIZED_FILENAME));
                        indexMap = (HashMap<String, Integer>) inputStream.readObject();
                } catch (ClassNotFoundException exception) {
                        throw new IOException("Deserialization error. Can't cast object");
                }
        }
}
