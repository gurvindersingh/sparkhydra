package com.lordjoe.distributed;

import java.util.*;

/**
 * com.lordjoe.distributed.ContinuousIterable
 * implementation of an iterable assuming one of more threads is calling add and when finished finsh
 * and One and only one other thread is using the iterable
 * User: Steve
 * Date: 9/20/2014
 */
public class ContinuousIterable<T> implements Iterable<T>{

    private volatile boolean finished; // true after last item added
    private final Iterator<T> iterator;
    private final Queue<T> heldItems = null;

    public ContinuousIterable() {
        iterator = new Iterator<T>() {
            public boolean hasNext() {
                if (!heldItems.isEmpty()) return true; // we hold something
                if (finished) return false; // nothing held nothing coming
                try {
                    heldItems.wait();    // wait for finish or add
                    return hasNext();
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);

                }
            }

            @Override
            public T next() {
                return heldItems.remove();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("unsupported");
            }
        };
    }

    public void add(T added) {
        heldItems.add(added);
        heldItems.notifyAll();
    }

    public void finish() {
        if (finished) throw new IllegalStateException("finish can only be called once");
        finished = true;
        heldItems.notifyAll();
    }

    public Iterator<T> iterator() {
        return this.iterator;
    }


}
