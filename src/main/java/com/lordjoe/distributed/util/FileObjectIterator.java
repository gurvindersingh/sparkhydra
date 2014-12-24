package com.lordjoe.distributed.util;

import com.lordjoe.distributed.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.util.FileLineIterator
 * User: Steve
 * NOTE this is NOT thread safe
 * Date: 8/25/2014
 */
public class FileObjectIterator<K> implements Iterable<K>, Iterator<K> {

    private final LineNumberReader rdr;
    private final IStringSerializer<K> seriaizer;
    private K nextObject;


    public FileObjectIterator(File f, IStringSerializer<K> ser) {
        this(FileLineIterator.readerFromFile(f), ser);
    }

    public FileObjectIterator(LineNumberReader rdx, IStringSerializer<K> ser) {
        seriaizer = ser;
        rdr = rdx;
        nextObject = seriaizer.fromReader(rdr);
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override public Iterator<K> iterator() {
        return this;
    }

    /**
     * Returns {@code true} if the iteration has more elements.
     * (In other words, returns {@code true} if {@link #next} would
     * return an element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements
     */
    @Override public boolean hasNext() {
        return nextObject != null;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws java.util.NoSuchElementException if the iteration has no more elements
     */
    @Override public K next() {
        K ret = nextObject;
        nextObject = seriaizer.fromReader(rdr);
        return ret;
    }

    @Override public void remove() {
        throw new UnsupportedOperationException("Remove not allowed");
    }
}
