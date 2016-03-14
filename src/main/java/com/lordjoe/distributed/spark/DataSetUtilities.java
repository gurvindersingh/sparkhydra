package com.lordjoe.distributed.spark;

import com.lordjoe.distributed.test.*;

import java.util.*;

/**
 * com.lordjoe.distributed.spark.DataSetUtilities
 * utilities to help with datasets
 * User: Steve
 * Date: 1/26/2016
 */
public class DataSetUtilities {


    /**
     * convert a list of tuples into a map
     * @param values
     * @param <K>
     * @param <T>
     * @return
     */
    public static <K, T> Map<K, T> obtainMap(List<KeyValue<K, T>> values) {
        Map<K, T> ret = new HashMap<K, T>();
        for (KeyValue<K, T> value : values) {
            ret.put(value.getKey(), value.getValue());
        }
        return ret;
    }

    /**
     * convert a map to a list of tuples
     * @param values
     * @param <K>
     * @param <T>
     * @return
     */
    public static <K, T> List<KeyValue<K, T>> obtainValues(Map<K, T> values) {
        List<KeyValue<K, T>> ret = new ArrayList<KeyValue<K, T>>() ;
        for (K value : values.keySet()) {
            ret.add(new KeyValue<K, T>(value,values.get(value)));
        }
        return ret;
    }





}
