package com.lordjoe.distributed;


import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import scala.*;

import java.io.Serializable;
import java.util.*;

/**
 * com.lordjoe.distributed.ReduceFunctionAdaptor
 * User: Steve
 * Date: 8/28/2014
 */
public class ReduceFunctionAdaptor<K extends Serializable, V extends Serializable,KOUT extends Serializable, VOUT extends Serializable>
        implements FlatMapFunction<Tuple2<K,KeyAndValues<K, V>>, KeyValueObject<KOUT, VOUT>>, Serializable {
      private final IReducerFunction<K, V,KOUT,VOUT> reducer;

    public ReduceFunctionAdaptor(JavaSparkContext pContext,final IReducerFunction<K, V,KOUT,VOUT> pReducer) {
        reducer = pReducer;
      }
    
    

    @Override
    public Iterable<KeyValueObject<KOUT, VOUT>> call(Tuple2<K,KeyAndValues<K, V>> inp) throws Exception {
        final KeyAndValues<K, V> itr =  inp._2();
          final List<KeyValueObject<KOUT, VOUT>> holder = new ArrayList<KeyValueObject<KOUT, VOUT>>();

        Iterable<V> iterable = itr.getIterable();
        K key = itr.key;

        final IKeyValueConsumer<KOUT, VOUT> consumer = new IKeyValueConsumer<KOUT, VOUT>() {
            @Override
            public void consume(final KeyValueObject<KOUT, VOUT> kv) {
                holder.add(kv);
            }
        };
        //noinspection unchecked
        reducer.handleValues( key, iterable, consumer);
        return holder;
    }


}
