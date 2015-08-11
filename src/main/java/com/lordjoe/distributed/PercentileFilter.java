package com.lordjoe.distributed;

import com.lordjoe.distributed.spark.accumulators.*;

import java.io.*;

/**
 * com.lordjoe.distributed.NofKFilter
 * a filter to take a fraction of an RDD
 * persist and then call repeatedly -
 * for example to score in 5 batches call
 * JavaRDD<MyType> t
 * t = t.persist();
 * for(int i = 0; i < 5; i++ ) {
 * JavaRDD<MyType> current = t.filter(new NofKFilter(5,i) ;
 * do stuff
 * }
 * <p/>
 * <p/>
 * User: Steve
 * Date: 12/2/2014
 */
public class PercentileFilter<T extends Serializable> extends AbstractLoggingFunction<T, Boolean> {

    public static final int PERCENTILE_DIVISION = 100;

    private final int setEnd;
    private final int setStart;
    private transient long index;

    public PercentileFilter(final int pSetEnd) {
        this(0, pSetEnd);
    }


    public PercentileFilter(final int pSetStart, final int pSetEnd) {
        setEnd = pSetEnd;
        setStart = pSetStart;
        if (setStart >= PERCENTILE_DIVISION)
            throw new IllegalArgumentException("start must be < " + PERCENTILE_DIVISION + " not " + setStart);
        if (setEnd >= PERCENTILE_DIVISION)
            throw new IllegalArgumentException("end must be <  " + PERCENTILE_DIVISION + " not " + setEnd);
        if (setStart >= setEnd)
            throw new IllegalArgumentException("set start must be < setSize " + setStart + " " + setEnd);
    }

    /**
     * do work here
     *
     * @param v1
     * @return
     */
    @Override
    public Boolean doCall(final T v1) throws Exception {
        int test = (int) (index++ % 100);
        return test >= setStart && test < setEnd;
    }
}
