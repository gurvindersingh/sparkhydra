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

    /**
     * like toString but might add more information than a shorter string
     * usually implemented bu appending toString
     * @param out
     */
    public void buildReport(Appendable out);

}
