package com.lordjoe.distributed.spark.accumulators;

import org.apache.spark.*;

import java.io.*;

/**
 * com.lordjoe.distributed.spark.accumulators.IAccumulatorParam
 * User: Steve
 * Date: 7/14/2015
 */

public class IAccumulatorParam<T extends IAccumulator> implements AccumulatorParam<T>, Serializable {


    @Override
    public T addAccumulator(final T t1, final T t2) {
        if (t1 == null)
            return t2;
        if (t2 == null)
            return t1;
        return (T) t1.add(t2);
    }

    /**
     * Merge two accumulated values together. Is allowed to modify and return the first value
     * for efficiency (to avoid allocating objects).
     *
     * @param r1 one set of accumulated data
     * @param r2 another set of accumulated data
     * @return both data sets merged together
     */
    @Override
    public T addInPlace(final T t1, final T t2) {
        if (t1 == null)
            return t2;
        if (t2 == null)
            return t1;
        return (T) t1.add(t2);
    }

    /**
     * Return the "zero" (identity) value for an accumulator type, given its initial value. For
     * example, if R was a vector of N dimensions, this would return a vector of N zeroes.
     *
     * @param initialValue
     */
    @Override
    public T zero(final T initialValue) {
        return (T) initialValue.asZero();
    }
}
