package com.lordjoe.distributed.spark.accumulators;

import org.apache.spark.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.spark.accumulators.StringSetAccumulableParam
 * Accumulates a set of Strings
 * Usage
 * final Accumulator<Set<String>> myStringSet = ctx.accumulator(Collextions.emptySet(), "String Set", StringSetAccumulableParam.INSTANCE);
 * ...
 * Set<String> toAdd  = new HashSet<String>();
 * toAdd.add("xyxzzy")
 * myStringSet.add(toAdd);
 * User: Steve
 * Date: 11/12/2014
 */

public class StringSetAccumulableParam implements AccumulatorParam<Set<String>>, Serializable {

    public static final StringSetAccumulableParam INSTANCE = new StringSetAccumulableParam();

    private StringSetAccumulableParam() {
    }

    @Override
    public Set<String> addAccumulator(final Set<String> r, final Set<String> t) {
        Set<String> rx = new HashSet<String>(r);
        rx.addAll(t);
        return rx;
    }

    @Override
    public Set<String> addInPlace(final Set<String> r, final Set<String> t) {
        r.addAll(t);
        return r;
    }

    @Override
    public Set<String> zero(final Set<String> initialValue) {
        return new HashSet<String>(initialValue);
    }
}
