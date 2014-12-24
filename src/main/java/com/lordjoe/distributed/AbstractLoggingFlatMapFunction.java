package com.lordjoe.distributed;

import org.apache.spark.api.java.function.*;

import java.io.*;

/**
 * org.apache.spark.api.java.function.AbstraceLoggingFunction
 * superclass for defined functions that will log on first call making it easier to see
 * do work in doCall
 * User: Steve
 * Date: 10/23/2014
 */
public abstract class AbstractLoggingFlatMapFunction<T, R extends Serializable>
        extends AbstractLoggingFunctionBase implements FlatMapFunction<T, R> {


    /**
     * NOTE override doCall not this
     *
     * @param t
     * @return
     */
    @Override
    public final Iterable<R> call(final T t) throws Exception {
        reportCalls();
        return doCall(t);
    }

    /**
     * do work here
     *
     * @param v1
     * @return
     */

    public abstract Iterable<R> doCall(final T t) throws Exception;

}
