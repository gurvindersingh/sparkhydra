package com.lordjoe.distributed.util;

import com.lordjoe.distributed.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.util.ListKeyValueConsumer
 * User: Steve
 * Date: 9/2/2014
 */
public class ListKeyValueConsumer<K extends Serializable,V extends Serializable> implements IKeyValueConsumer<K,V> {

    private final List<KeyValueObject<K,V>> list = new ArrayList<KeyValueObject<K,V>>();

    @Override public void consume(final KeyValueObject<K, V> kv) {
        list.add(kv);
    }

    public Iterable<KeyValueObject<K,V>> getValues() {
        return list;
    }

    public List<KeyValueObject<K,V>> getList() {
        return list;
    }

    public int size() { return list.size(); }
}
