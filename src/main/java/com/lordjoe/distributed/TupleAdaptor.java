package com.lordjoe.distributed;

import org.apache.spark.api.java.function.*;
import scala.*;

import java.io.Serializable;

/**
 * com.lordjoe.distributed.MapFunctionAdaptor
 * User: Steve
 * Date: 8/28/2014
 */
public class TupleAdaptor< KOUT extends Serializable,VOUT extends Serializable> implements FlatMapFunction<Tuple2<KOUT,VOUT>, KeyValueObject<KOUT,VOUT>>{


    public TupleAdaptor( ) {

    }


    @Override public Iterable<KeyValueObject<KOUT, VOUT>> call(final Tuple2<KOUT, VOUT> tp) throws Exception {
        return null;
    }


}
