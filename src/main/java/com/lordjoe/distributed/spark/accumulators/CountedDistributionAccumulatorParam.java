package com.lordjoe.distributed.spark.accumulators;

import com.lordjoe.algorithms.CountedDistribution;
import org.apache.spark.AccumulatorParam;

import java.io.Serializable;

/**
 * com.lordjoe.distributed.spark.accumulators.CountedDistributionAccumulatorParam
 * Accumulator which keeps track of a distribution of ints
 * ...
 * myString.add("xyxzzy");
 * User: Steve
 * Date: 11/12/2014
 */
public class CountedDistributionAccumulatorParam implements AccumulatorParam<CountedDistribution>, Serializable {

    public static final AccumulatorParam<CountedDistribution> INSTANCE = new CountedDistributionAccumulatorParam();

    private CountedDistributionAccumulatorParam() {
     }


    @Override
    public CountedDistribution addAccumulator(CountedDistribution t1, CountedDistribution t2) {
        return t1.add(t2);

    }

    @Override
    public CountedDistribution addInPlace(CountedDistribution r1, CountedDistribution r2) {
        return r1.add(r2);

    }

    @Override
    public CountedDistribution zero(CountedDistribution initialValue) {
         return new CountedDistribution();
    }
}
