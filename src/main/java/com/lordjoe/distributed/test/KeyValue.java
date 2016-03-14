package com.lordjoe.distributed.test;

import java.io.*;

/**
 * com.lordjoe.distributed.test.KeyValue
 * User: Steve
 * Date: 1/26/2016
 */
public class KeyValue<K, V> implements Serializable {
    private K key;
    private V value;

    public KeyValue() {
    }

    public KeyValue(final K pKey, final V pValue) {
        this();
        key = pKey;
        value = pValue;
    }

    public K getKey() {
        return key;
    }

    public void setKey(final K pKey) {
        key = pKey;
    }

    public V getValue() {
        return value;
    }

    public void setValue(final V pValue) {
        value = pValue;
    }
}
