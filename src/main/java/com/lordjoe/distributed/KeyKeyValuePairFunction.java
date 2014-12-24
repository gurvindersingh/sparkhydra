package com.lordjoe.distributed;

import org.apache.spark.api.java.function.*;
import scala.*;

import java.io.Serializable;

/**
* com.lordjoe.distributed.KeyKeyValuePairFunction
 * convert a Tuple2 into a Tuple2 with the tuple and its key
 * This allows keys to be passed into functions that combine by key without losing the key
* User: Steve
* Date: 9/12/2014
*/
public class KeyKeyValuePairFunction<K extends Serializable, V extends Serializable> implements PairFunction<Tuple2<K, V>, K, Tuple2<K, V>>,Serializable {
    @Override public Tuple2<K,  Tuple2<K, V>> call(final Tuple2<K, V> kv) throws Exception {
          return new Tuple2<K,  Tuple2<K, V>>(kv._1(),kv);
    }
}
