package com.lordjoe.distributed.hydra.scoring;

import org.apache.spark.api.java.function.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;

/**
* com.lordjoe.distributed.hydra.scoring.ToIndexTuple
* User: Steve
* Date: 3/11/2015
*/
public class ToIndexTuple <T extends IScoredScan> implements PairFunction<T, Integer, T> {
    @Override
    public Tuple2<Integer, T> call(final T t) throws Exception {
        return new Tuple2<Integer, T>(t.getIndex(), t);
    }
}
