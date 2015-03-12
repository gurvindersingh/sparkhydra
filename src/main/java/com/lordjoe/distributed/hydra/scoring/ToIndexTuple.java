package com.lordjoe.distributed.hydra.scoring;

import org.apache.spark.api.java.function.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;

/**
* com.lordjoe.distributed.hydra.scoring.ToIndexTuple
* User: Steve
* Date: 3/11/2015
*/
public class ToIndexTuple implements PairFunction<IScoredScan, Integer, IScoredScan> {
    @Override
    public Tuple2<Integer, IScoredScan> call(final IScoredScan t) throws Exception {
        return new Tuple2<Integer, IScoredScan>(t.getIndex(), t);
    }
}
