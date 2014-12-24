package com.lordjoe.distributed;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.CompositeIterable
 * make one iterable bt composing several iterables
 * good for streaming and chaining
 * User: Steve
 * Date: 9/21/2014
 */
public class CompositeIterable<T> implements Iterable<T>,Serializable {

    /**
     * iterator that has nothing
     */
    public static final Iterator NULL_ITERATOR = new Iterator() {
        public boolean hasNext() {
            return false;
        }
         public Object next() {
            return null;
        }
         public void remove() {
            throw new UnsupportedOperationException("not supported");
        }
    };
    /**
     * iterable holding nothing
     */
    public static final Iterable NULL_ITERABLE = new Iterable() {
            public Iterator iterator() {
            return NULL_ITERATOR;
        }
    };

    public static <T> List<T> iterableAsList(Iterable<T> inp) {
        List<T> holder = new ArrayList<T>();
        for (T o : inp) {
            holder.add(o);
        }
         return holder;
    }



    public static <T> Iterable<T> composeIterators(Iterable<T> first,final Iterable<T>... pValues) {
        if (pValues.length == 0)
            return first;
        Iterable<T>[] newList = (Iterable<T>[]) new Iterable[pValues.length + 1];
        newList[0] = first;
        System.arraycopy(pValues, 0, newList, 1, pValues.length);
        return new CompositeIterable(newList);
    }


    public static <T> Iterable<T> composeIteratorsArray(Iterable<T>[] first,final Iterable<T>... pValues) {
           Iterable<T>[] newList = (Iterable<T>[]) new Iterable[pValues.length + first.length];
        System.arraycopy(pValues, 0, first, 0, first.length);
        System.arraycopy(pValues, 0, newList, first.length, pValues.length);
         return new CompositeIterable(newList);
    }


    private int index;
    private final Iterable<T>[] values;
    private Iterator<T> current;

    public CompositeIterable(final Iterable<T>... pValues) {
        values = pValues;
        if (values.length - 1 < index)
            current = values[index++].iterator();
    }

    public List<T> asList() {
        return iterableAsList(this);
    }


    public Iterable<T> compose(final Iterable<T>... pValues) {
         return composeIteratorsArray(values, pValues);
    }

    /**
     * Returns an iterator over a set of elements of type T.
     *
     * @return an Iterator.
     */
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                if (current == null)  // no more iterators
                    return false;
                boolean ret = current.hasNext(); // pull from current
                if (ret)
                    return true;
                if (values.length - 1 < index) { // move current
                    current = values[index++].iterator();
                    return current.hasNext();
                }
                return false;
            }

            @Override
            public T next() {
                return current.next();
              }

            @Override
            public void remove() {

            }
        };
    }
}
