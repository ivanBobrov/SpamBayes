package com.odkl.moderation.text.server.domain.bayes.utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.odkl.moderation.text.server.domain.bayes.utils.preprocessor.NewTranslitMetaphone2;
import com.odkl.moderation.text.server.domain.bayes.utils.preprocessor.NonAlphabeticSymbolsPreprocessor;
import com.odkl.moderation.text.server.domain.bayes.utils.preprocessor.SpamTextPreprocessor;

import gnu.trove.impl.Constants;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntShortMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntShortHashMap;
import gnu.trove.set.TIntSet;

public class Dictionary {

    private static final int N_GRAM_LENGTH = 3;

    private static final TIntShortMap EMPTY_MAP = new TIntShortHashMap(0);

    private boolean preprocessEnabled = false;

    private SpamTextPreprocessor[] textPreprocessor;

    private TIntIntMap indexMap = new TIntIntHashMap(1000000, Constants.DEFAULT_LOAD_FACTOR, 0, 0);

    public Dictionary() {
        this.textPreprocessor = new SpamTextPreprocessor[2];

        this.textPreprocessor[0] = new NonAlphabeticSymbolsPreprocessor();
        this.textPreprocessor[1] = new NewTranslitMetaphone2();
    }

    public int getDictionarySize() {
        return indexMap.size();
    }

    public void setPreprocessEnabled(boolean enabled) {
        this.preprocessEnabled = enabled;
    }

    public TIntShortMap parseString(String text, boolean modificationEnabled) {
        //TODO: Check for null string and string with length less than nGramLength
        String preprocessedText = preprocessEnabled ? preprocessString(text) : text;

        if (text.length() < N_GRAM_LENGTH) {
            return EMPTY_MAP;
        }

        int size = text.length() - N_GRAM_LENGTH + 1;

        int[] bases = new int[size];
        getBases(preprocessedText, bases, size);

        TIntShortMap parsedString = new TIntShortHashMap(size, Constants.DEFAULT_LOAD_FACTOR, (short) 0, (short) 0);

        for (int i = 0; i < size; i++) {
            int base = bases[i];
            if (base == 0) {
                continue;
            }

            int index = indexMap.get(base);
            if (index == 0) {
                if (modificationEnabled) {
                    index = indexMap.size() + 1;
                    indexMap.put(base, index);
                } else {
                    continue;
                }
            }

            parsedString.adjustOrPutValue(index, (short) 1, (short) 1);
        }

        return parsedString;
    }

    private void getBases(String text, int[] bases, int size) {
        if (text.length() < N_GRAM_LENGTH) {
            throw new IllegalArgumentException("Text is too small");
        }

        int base = 0;

        for (int i = 0, j = 1 - N_GRAM_LENGTH; j < size; i++, j++) {
            base <<= 12;
            base |= text.charAt(i) & 0x00000FFF;

            if (j >= 0) {
                bases[j] = base;
            }
        }
    }

    public String preprocessString(String text) {
        String res = text;
        for (SpamTextPreprocessor preprocessor : textPreprocessor) {
            res = preprocessor.convert(res);
        }

        return res;
    }

    public TIntSet getBaseSet() {
        return indexMap.keySet();
    }

    public void serializeDictionary(String filename) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(
                new FileOutputStream(filename));
        outputStream.writeObject(indexMap);
        outputStream.flush();
        outputStream.close();
    }

    public void deserializeDictionary(String filename) throws IOException {
        try {
            ObjectInputStream inputStream = new ObjectInputStream(
                    new FileInputStream(filename));
            indexMap = (TIntIntMap) inputStream.readObject();
        } catch (ClassNotFoundException exception) {
            throw new IOException("Deserialization error. Can't cast object");
        }
    }
}
