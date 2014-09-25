package com.antiSpam.spamBayes.utils;

import com.sun.xml.internal.stream.events.CharacterEvent;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;


public class XMLFileReader implements Iterator<String>, Closeable {
    private static final String TAG_NAME = "text";
    private final FileInputStream inputStream;
    private XMLEventReader xmlEventReader;

    //TODO: throw own exception instead of IO?
    public XMLFileReader(String filename) throws IOException {
        try {
            this.inputStream = new FileInputStream(filename);
            this.xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader(inputStream);
        } catch (XMLStreamException exception) {
            throw new IOException("Can't read xml file");
        }
    }

    @Override
    public boolean hasNext() {
        return xmlEventReader.hasNext();
    }

    @Override
    public String next() {
        while (hasNext()) {
            try {
                XMLEvent event = xmlEventReader.nextEvent();

                if (event.isStartElement() && event.asStartElement().getName().toString().equals(TAG_NAME)) {
                    XMLEvent textEvent = xmlEventReader.nextEvent();
                    return ((CharacterEvent) textEvent).getData().replace("\n", " ").toLowerCase();
                }
            } catch (XMLStreamException exception) {
                //TODO: throw own exception
                throw new IllegalStateException("Can't read xml");
            }
        }

        throw new IllegalStateException("No next entry");
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() throws IOException {
        try {
            xmlEventReader.close();
        } catch (XMLStreamException exception) {
            throw new IOException("Can't close XMLEventReader");
        }

        inputStream.close();
    }
}