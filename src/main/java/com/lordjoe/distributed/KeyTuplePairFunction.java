package com.lordjoe.distributed;

import org.apache.spark.api.java.function.*;
import scala.*;

import java.io.Serializable;

/**
* com.lordjoe.distributed.KeyTuplePairFunction
 * return a tuple of the key plus the tuple
* User: Steve
* Date: 9/12/2014
*/
public class KeyTuplePairFunction<K extends Serializable, V extends Serializable> implements PairFunction<Tuple2<K, V>, K, Tuple2<K, V>>,Serializable {
    @Override public Tuple2<K, Tuple2<K, V>> call(final Tuple2<K, V> kv) throws Exception {
          return new Tuple2<K, Tuple2<K, V>>(kv._1(),new Tuple2<K, V>(kv._1(),kv._2()));
    }
}
