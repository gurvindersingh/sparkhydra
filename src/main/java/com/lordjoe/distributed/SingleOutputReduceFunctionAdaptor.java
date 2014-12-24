package com.lordjoe.distributed;


import org.apache.spark.api.java.function.Function;
import scala.*;

import java.io.Serializable;

/**
 * com.lordjoe.distributed.ReduceFunctionAdaptor
 * User: Steve
 * Date: 8/28/2014
 */
public class SingleOutputReduceFunctionAdaptor<K extends Serializable, V extends Serializable, KOUT extends Serializable, VOUT extends Serializable>
        implements Function<Tuple2<K,Tuple2<K,V>>, KeyValueObject<KOUT, VOUT>>, Serializable {
    private final ISingleOutputReducerFunction<K, V, KOUT, VOUT> reducer;

    public SingleOutputReduceFunctionAdaptor(final ISingleOutputReducerFunction<K, V, KOUT, VOUT> pReducer) {
        reducer = pReducer;
    }


    @Override
    public KeyValueObject<KOUT, VOUT> call(Tuple2<K, Tuple2<K,V>> inpx) throws Exception {
        Tuple2<K,V>  inp = inpx._2();
        return reducer.handleValue(inp._1(), inp._2());
    }


}
