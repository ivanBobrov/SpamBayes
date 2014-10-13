package com.odkl.moderation.text.server.domain.bayes.utils.io;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;


public class JSONFileReader implements Iterator<String>, Closeable {
    private static final String FIELD_NAME = "text";
    private final FileInputStream inputStream;
    private final JsonParser parser;

    private Integer fromMessage = null;
    private Integer toMessage = null;
    private Integer currentMessage = 0;
    private String currentText = null;
    private String nextText = null;

    public JSONFileReader(String filename) throws IOException {
        this.inputStream = new FileInputStream(filename);
        parser = new JsonFactory().createParser(inputStream);
        nextText = readNext();
    }

    public JSONFileReader(String filename, Integer fromMessage, Integer toMessage) throws IOException {
        this(filename);
        this.fromMessage = fromMessage;
        this.toMessage = toMessage;
    }

    private void readNextToken() {
        currentText = nextText;
        nextText = readNext();
    }

    @Override
    public boolean hasNext() {
        return (nextText != null) && (toMessage == null || currentMessage - 1 < toMessage);
    }

    @Override
    public String next() {
        readNextToken();

        return currentText;
    }

    private String readNext() {
        try {
            JsonToken token = parser.nextToken();
            while (token != null) {
                if (JsonToken.FIELD_NAME.equals(token) && FIELD_NAME.equals(parser.getCurrentName())) {
                    parser.nextToken();

                    currentMessage++;
                    if (fromMessage != null && currentMessage - 1 < fromMessage) {
                        token = parser.nextToken();
                        continue;
                    }

                    String text = parser.getText().replace("\n", " ").toLowerCase();
                    return text;
                }

                token = parser.nextToken();
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Can't read next entry in json file", exception);
        }

        return null;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Can't remove from json file.");
    }

    @Override
    public void close() throws IOException {
        if (!parser.isClosed()) {
            parser.close();
        }
        inputStream.close();
    }
}
