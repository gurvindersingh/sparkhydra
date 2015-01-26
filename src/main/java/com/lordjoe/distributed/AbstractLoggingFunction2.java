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
public abstract class AbstractLoggingFunction2<T1 extends Serializable, T2 extends Serializable, R extends Serializable>
        extends AbstractLoggingFunctionBase implements Function2<T1, T2, R> {


    /**
     * override doCall
     *
     * @param v1
     * @return
     * @throws Exception
     */
    @Override
    public R call(final T1 v1, final T2 v2) throws Exception {
        reportCalls();
        long startTime = System.nanoTime();
        R ret = doCall(v1, v2);
        long estimatedTime = System.nanoTime() - startTime;
        incrementAccumulatedTime(estimatedTime);
         return ret;

    }


    /**
     * do work here
     *
     * @param v1
     * @return
     */
    public abstract R doCall(final T1 v1, final T2 v2) throws Exception;
}
