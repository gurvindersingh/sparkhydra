package com.lordjoe.distributed;

import org.apache.spark.api.java.function.*;
import scala.*;

import java.io.Serializable;

/**
* com.lordjoe.distributed.KeyValuePairFunction
 * return a tuple of the key plus a
* User: Steve
* Date: 9/12/2014
*/
public class KeyValuePairFunction<K extends Serializable, V extends Serializable> implements PairFunction<KeyValueObject<K, V>, K, Tuple2<K, V>>,Serializable {
    @Override public Tuple2<K, Tuple2<K, V>> call(final KeyValueObject<K, V> kv) throws Exception {
          return new Tuple2<K, Tuple2<K, V>>(kv.key,new Tuple2<K, V>(kv.key,kv.value));
    }
}
