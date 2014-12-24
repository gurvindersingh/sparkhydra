package com.lordjoe.distributed.util;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.util.FileLineIterator
 * User: Steve
 * NOTE this is NOT thread safe
 * Date: 8/25/2014
 */
public class FileLineIterator implements Iterable<String>, Iterator<String> {

    public static LineNumberReader readerFromFile(File f) {
        if (!f.exists() || !f.isFile())
             throw new IllegalArgumentException("File " + f + " is not a file");
         try {
             return new LineNumberReader(new FileReader(f));
           }
         catch (IOException e) {
             throw new RuntimeException(e);
         }

    }
    private final LineNumberReader rdr;
    private String nextLine;

    public FileLineIterator(File f) {
        this(readerFromFile(f));
    }

    public FileLineIterator(LineNumberReader f) {
        rdr = f;
        try {
            nextLine = rdr.readLine();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * Returns an iterator over elements of type {@code T}.
     *
     * @return an Iterator.
     */
    @Override public Iterator<String> iterator() {
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
        return nextLine != null;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element in the iteration
     * @throws java.util.NoSuchElementException if the iteration has no more elements
     */
    @Override public String next() {
        String ret = nextLine;
        try {
            nextLine = rdr.readLine();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
        return ret;
    }

    @Override public void remove() {
        throw new UnsupportedOperationException("Remove not allowed");
    }
}
