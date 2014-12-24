package com.lordjoe.distributed;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.IKeyValue
 * User: Steve
 * Date: 8/25/2014
 */
public class KeyValueObject<K extends Serializable, V extends Serializable> implements Serializable, Comparable<KeyValueObject> {

    /**
     * compare by keys using String as a last resort comparator
     */
    public static final Comparator<KeyValueObject> KEY_COMPARATOR = new Comparator<KeyValueObject>() {

        @Override public int compare(final KeyValueObject o1, final KeyValueObject o2) {
            Object key = o1.key;
            if (key instanceof Comparable) {
                return ((Comparable) key).compareTo(o2.key);
            }
            else {
                return key.toString().compareTo(o2.key.toString());
            }
        }


    };
    public final K key;
    public final V value;

    public KeyValueObject(final @Nonnull K pKey, final @Nonnull V pValue) {
        key = pKey;
        value = pValue;
        if (!(key instanceof Serializable) || !(value instanceof Serializable))
            throw new IllegalArgumentException("problem"); // ToDo change
    }

    @Override public String toString() {
        return key.toString() +
                ":" + value;
    }

    /**
     * compare keys only
     * @param o
     * @return
     */
    @Override public int compareTo(final KeyValueObject o) {
        if (key instanceof Comparable) {
            return ((Comparable) key).compareTo(o.key);
        }
        else {
            return key.toString().compareTo(o.key.toString());
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final KeyValueObject that = (KeyValueObject) o;

        if (!key.equals(that.key)) return false;
        if (!value.equals(that.value)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }
}
