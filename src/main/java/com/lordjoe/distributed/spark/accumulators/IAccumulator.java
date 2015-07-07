package com.lordjoe.distributed.spark.accumulators;

import java.io.*;

/**
 * com.lordjoe.distributed.spark.accumulators.IAccumulator
 *
 * interface marking the class as somthing which can be used as an accumulator
 * User: Steve
 * Date: 7/6/2015
 */
public interface IAccumulator<K> extends Serializable {
    /**
     * add the accumulated data to another instance
      * @param added
     * @return
     */
    public K add(K added);

}
