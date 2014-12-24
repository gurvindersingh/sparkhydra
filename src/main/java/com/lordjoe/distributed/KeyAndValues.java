package com.lordjoe.distributed;


import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import scala.*;

import java.io.Serializable;
import java.util.*;

/**
 * com.lordjoe.distributed.CombineByKeyAdaptor
 * User: Steve
 * Date: 8/28/2014
 */
public class KeyAndValues<K extends Serializable, V extends Serializable > implements Serializable {


    public static <K extends Serializable,V extends Serializable> JavaPairRDD<K,KeyAndValues<K,V>> combineByKey(JavaPairRDD<K, Tuple2<K, V>>  toCombine) {
        //noinspection UnnecessaryLocalVariable
        JavaPairRDD<K, KeyAndValues<K, V>> reducedSets = toCombine.combineByKey(new CombineStartKeyAndValues<K, V>(),
                 new CombineContinueKeyAndValues<K, V>(),
                 new CombineMergeKeyAndValues<K, V>()
         );
//
//         JavaPairRDD<K, Iterable<V>> ret = reducedSets.mapToPair(new PairFunction<Tuple2<K, KeyAndValues<K, V>>, K, Iterable<V>>() {
//            @Override
//            public Tuple2<K, Iterable<V>> call(final Tuple2<K, KeyAndValues<K, V>> t) throws Exception {
//                return new Tuple2(t._1(), t._2().getIterable());
//            }
//        });
        return reducedSets;
    }
    public final K key;
      private final List<V> values = new ArrayList<V>();

      public KeyAndValues(final K pKey, V... added) {
          key = pKey;
          values.addAll(Arrays.asList(added));
      }

      public KeyAndValues(final KeyAndValues<K, V> start, V... added) {
          //noinspection unchecked
          this(start.key);
          values.addAll(start.values);
          values.addAll(Arrays.asList(added));
      }

      public int size() {
          return values.size();
      }

//        public KeyAndValues(final KeyAndValues<K, V> start, Iterable<V> added) {
//            key = start.key;
//            values = CompositeIterable.composeIterators(start.values, added);
//            values.addAll(Arrays.asList(added));
//        }

      public Iterable<V> getIterable() {
          return values;
      }

      @SuppressWarnings("UnusedDeclaration")
      public KeyAndValues<K, V> merge(KeyAndValues<K, V> merged) {
          //noinspection unchecked
          return new KeyAndValues(this, merged);
      }
  
    

    private static class CombineStartKeyAndValues<K extends Serializable, V extends Serializable> implements Function<Tuple2<K, V>, KeyAndValues<K, V>>   {
        public KeyAndValues<K, V> call(Tuple2<K, V> x) {
                 //noinspection unchecked
              return new KeyAndValues(x._1(),x._2());
        }
    }

    private static class CombineContinueKeyAndValues<K extends Serializable, V extends Serializable> implements Function2<KeyAndValues<K, V>, Tuple2<K, V>, KeyAndValues<K, V>> {
        public KeyAndValues<K, V> call(final KeyAndValues<K, V> kvs, final Tuple2<K, V> added) throws Exception {
            //noinspection unchecked
                 return new KeyAndValues(kvs ,added._2() );
        }
    }

    private static class CombineMergeKeyAndValues<K extends Serializable, V extends Serializable> implements Function2<KeyAndValues<K, V>, KeyAndValues<K, V>, KeyAndValues<K, V>> {
        public KeyAndValues<K, V> call(final KeyAndValues<K, V> v1, final KeyAndValues<K, V> v2) throws Exception {
            //noinspection unchecked
            return new KeyAndValues(v1,v2);
          }
    }




}
