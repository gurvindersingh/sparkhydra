package com.lordjoe.distributed.spark.accumulators;

import org.apache.spark.*;
import java.io.*;

/**
* com.lordjoe.distributed.spark.accumulators.LongAccumulableParam
 * Usage
 *      Accumulator<Long> myCount = context.accumulator(0L,"MyAccumulatorName",LongAccumulableParam.INSTANCE);
 *      myCount.add(1L);
* User: Steve
* Date: 11/12/2014
*/
public class LongAccumulableParam implements AccumulatorParam<Long>,Serializable {
    public static final LongAccumulableParam INSTANCE = new LongAccumulableParam();
    private LongAccumulableParam() {}
     @Override
    public Long addAccumulator(final Long r, final Long t) {
        return r + t;
    }
     @Override
    public Long addInPlace(final Long r1, final Long r2) {
        return  r1 + r2;
    }
     @Override
    public Long zero(final Long initialValue) {
        return 0L;
    }
}
