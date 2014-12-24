package com.lordjoe.distributed;

import org.apache.spark.api.java.function.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.MapFunctionAdaptor
 * User: Steve
 * Date: 8/28/2014
 */
public class MapFunctionAdaptor<KEYIN extends Serializable,VALUEIN extends Serializable,KOUT extends java.io.Serializable,VOUT extends Serializable> implements FlatMapFunction<KeyValueObject<KEYIN,VALUEIN>,KeyValueObject<KOUT,VOUT>>,Serializable{

    private final IMapperFunction<KEYIN,VALUEIN,KOUT,VOUT> mapper;

    public MapFunctionAdaptor(final IMapperFunction<KEYIN, VALUEIN, KOUT, VOUT> pMapper) {
        mapper = pMapper;
    }

    @Override public Iterable<KeyValueObject<KOUT, VOUT>> call(final KeyValueObject<KEYIN,VALUEIN> t) throws Exception {
        if(t == null || t.key == null || t.value == null)
            return Collections.EMPTY_LIST;
        Iterable<KeyValueObject<KOUT, VOUT>> keyValueObjects = mapper.mapValues(t.key,t.value);
        return keyValueObjects;
        //.toTuples(keyValueObjects);
    }
}
