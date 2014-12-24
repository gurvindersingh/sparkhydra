package com.lordjoe.distributed;

import org.apache.spark.api.java.function.*;

import java.io.*;

/**
 * com.lordjoe.distributed.MapFunctionAdaptor
 * User: Steve
 * Date: 8/28/2014
 */
public class MapTupleFunctionAdaptor<KEYIN extends Serializable,VALUEIN extends Serializable,KOUT extends Serializable,VOUT extends Serializable> implements FlatMapFunction<KeyValueObject<KEYIN,VALUEIN>,KeyValueObject<KOUT,VOUT>>,Serializable{

    private final IMapperFunction<KEYIN,VALUEIN,KOUT,VOUT> mapper;

    public MapTupleFunctionAdaptor(final IMapperFunction<KEYIN,VALUEIN, KOUT, VOUT> pMapper) {
        mapper = pMapper;
    }

    @Override public Iterable<KeyValueObject<KOUT, VOUT>> call(final KeyValueObject<KEYIN,VALUEIN> t) throws Exception {
        Iterable<KeyValueObject<KOUT, VOUT>> keyValueObjects = mapper.mapValues(t.key,t.value);
        return keyValueObjects;
        //.toTuples(keyValueObjects);
    }
}
