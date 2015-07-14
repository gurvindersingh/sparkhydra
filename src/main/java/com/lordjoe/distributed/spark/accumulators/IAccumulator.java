package com.lordjoe.distributed.spark.accumulators;

import java.io.*;

/**
 * com.lordjoe.distributed.spark.accumulators.IAccumulator
 *
 * interface marking the class as something which can be used as an accumulator  \
 * User: Steve
 * Note it is normal for these classes to have
 * 1)
 *    public static final AccumulatorParam<MY_CLASS> PARAM_INSTANCE = new IAccumulatorParam<MY_CLASS>();

      public static MY_CLASS empty() {
         return new MY_CLASS();
      }
  2) private constructor to encourage the use of empty

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
      * given a value return it as 0
     *  default behavior os th return the value itself
       * @param added
      * @return
      */
     public K asZero();

    /**
     * like toString but might add more information than a shorter string
     * usually implemented bu appending toString
     * @param out
     */
    public void buildReport(Appendable out);

}
