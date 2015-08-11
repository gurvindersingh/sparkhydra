package com.lordjoe.distributed;

import com.lordjoe.distributed.spark.accumulators.*;

import java.io.*;

/**
 * com.lordjoe.distributed.NofKFilter
 * a filter to take a fraction of an RDD
 * persist and then call repeatedly -
 * for example to score in 5 batches call
 *   JavaRDD<MyType> t
 *   t = t.persist();
 *   for(int i = 0; i < 5; i++ ) {
 *        JavaRDD<MyType> current = t.filter(new NofKFilter(5,i) ;
 *        do stuff
 *   }
 *
 *
 * User: Steve
 * Date: 12/2/2014
 */
public class NofKFilter<T extends Serializable> extends AbstractLoggingFunction<T,Boolean> {

    private final int setSize;
    private final int setStart;
    // use of a thread local makes sure multiple threads do not see the same
    // index and increment it
    private transient ThreadLocal<Long> index;

    public NofKFilter(final int pSetSize, final int pSetStart) {
        setSize = pSetSize;
        setStart = pSetStart;
        if(setSize == 0)
            throw new IllegalArgumentException("set size must be > 1");
        if(setStart  >= setSize )
             throw new IllegalArgumentException("set start must be < setSize");
     }

    /**
     * return the current index creating a Threadlocal as needed and
     * increment the current value
     * @return
     */
    protected long getIndex()
    {
        if(index == null) {
            index = new ThreadLocal<Long>();
            index.set(0L);
        }
        long ret = index.get();
        index.set(ret + 1);
        return ret;
    }

    /**
     * do work here
     *
     * @param v1
     * @return
     */
    @Override
    public Boolean doCall(final T v1) throws Exception {
        return (((getIndex() + setStart) % setSize) == 0);
    }
}
