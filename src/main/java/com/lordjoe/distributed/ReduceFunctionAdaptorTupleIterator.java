package com.lordjoe.distributed;


import org.apache.spark.api.java.function.*;
import scala.*;

import java.io.Serializable;
import java.util.*;

/**
 * com.lordjoe.distributed.ReduceFunctionAdaptor
 * User: Steve
 * Date: 8/28/2014
 */
public class ReduceFunctionAdaptorTupleIterator<K extends Serializable, V extends Serializable,
        KOUT extends Serializable, VOUT extends Serializable>
        implements FlatMapFunction<Iterator<Tuple2<K, V>>, KeyValueObject<KOUT, VOUT>>, Serializable {
    private final IReducerFunction<K, V,KOUT, VOUT> reducer;
    private   Tuple2<K, V> first;

    public ReduceFunctionAdaptorTupleIterator(final IReducerFunction<K, V,KOUT, VOUT> pReducer) {
        reducer = pReducer;
    }

    @Override public Iterable<KeyValueObject<KOUT, VOUT>> call(final Iterator<Tuple2<K, V>> itr) throws Exception {
        first = null;
        final List<KeyValueObject<KOUT, VOUT>> holder = new ArrayList<KeyValueObject<KOUT, VOUT>>();
         if(!itr.hasNext())
            return holder;

         first = itr.next();

        final Iterator<V> itx = new Iterator<V>() {
            Tuple2<K, V> current = first;
              @Override public boolean hasNext() {
                 if(current == null)
                     return false;
                 return true;
              }

            @Override public V next() {
                V ret = current._2();
                if(itr.hasNext()) {
                    Tuple2<K, V> test = itr.next();
                    if(test._1().equals(first._1()))
                        current = test;
                    else {
                        first = test;     // different key
                        current = null;
                    }
                }
                else
                    current = null;
                return ret;
            }


            @Override public void remove() {
                throw new UnsupportedOperationException("Fix This"); // ToDo
            }
        };

        final Iterable<V> vals = new Iterable<V>() {
            @Override public Iterator<V> iterator() {
                return itx;
            }
        };
        final IKeyValueConsumer<KOUT, VOUT> consumer = new IKeyValueConsumer<KOUT, VOUT>() {
            @Override public void consume(final KeyValueObject<KOUT, VOUT> kv) {
                holder.add(kv);
            }
        };
        K key = (K) first._1();
        reducer.handleValues(key, vals, consumer);
         return holder;
    }

    protected void  handleKeyValues(final Iterator<Tuple2<K, V>> itr,IKeyValueConsumer<K, V> ... consumer)
    {
       if(first == null)
               return;
        final K key = first._1();

        final Iterator<V> itx = new Iterator<V>() {
            Tuple2<K, V> current = first;
               @Override public boolean hasNext() {
                 return current != null;
              }

            @Override public V next() {
                V ret = current._2();
                if(itr.hasNext()) {
                    current = itr.next();
                    if(!current._1().equals(key))   {
                        first = current;
                        current = null;
                    }
                }
                else
                    current = null;
                return ret;
            }


            @Override public void remove() {
                throw new UnsupportedOperationException("Fix This"); // ToDo
            }
        };


    }

}
