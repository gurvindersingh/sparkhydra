package com.lordjoe.distributed;

import java.io.*;
import java.nio.file.*;

/**
 * com.lordjoe.distributed.ISourceFunction
 * User: Steve
 * Date: 8/28/2014
 */
public interface ISinkFunction<K extends Serializable,V extends Serializable> {

    /**
     * null sink does nothing
     */
    public static final ISinkFunction  NULL_FUNCTION = new ISinkFunction () {

        @Override public void generateOutput(final Path p, final Iterable vals) {
          }
    };
    /**
     * null sink does nothing
     */
    public static final ISinkFunction  PRINT_FUNCTION = new ISinkFunction () {

        @Override public void generateOutput(final Path p, final Iterable vals) {
            for (java.lang.Object val : vals) {
                KeyValueObject kv = (KeyValueObject)val;
                System.out.println(kv.key + ":" + kv.value);
            }
          }
    };
    /**
     * somehow define how a path is converted into an iterator
     * @param p
     * @return
     */
    public void generateOutput(Path p,Iterable<KeyValueObject<K,V>> vals);
}
