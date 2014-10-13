package com.odkl.moderation.text.server.domain.bayes.utils;

import java.util.Iterator;


public class ArrayIterator<T> implements Iterator<T> {
    private T[] array;
    private int currentIndex;

    public ArrayIterator(T[] array) {
        this.array = array;
        this.currentIndex = 0;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < array.length;
    }

    @Override
    public T next() {
        return array[currentIndex++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
