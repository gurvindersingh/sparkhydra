package com.lordjoe.distributed.spark;

import java.util.*;

/**
 * com.lordjoe.distributed.spark.AbstractPseudoList
 *   One of these can be used by parallelize on one machine
 *   It should not work in parallel
 * User: Steve
 * Date: 12/8/2014
 */
public abstract class AbstractPseudoList<T> implements Iterable<T>,List<T> {

    private final long listSize;
    private long released;

    public AbstractPseudoList(final long pListSize) { listSize = pListSize; }

    /**
     * generate s new element - called by the iterator  listSize times
     * @return
     */
    public abstract T generateElement();

    @Override
    public int size() {
        if (listSize < Integer.MAX_VALUE) return (int) listSize;
        throw new IllegalStateException("List size above integer max"); // ToDo change
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            public boolean hasNext() { return released < listSize; }
            public T next() {  released++; return generateElement(); }
            public void remove() {  throw new UnsupportedOperationException("Unsupported"); }
        };
    }

    // All other operations throw exceptions and are not used
   public boolean isEmpty() {  return false;  }
   public boolean contains(final Object o) {  return false; }
   public Object[] toArray() { throw new UnsupportedOperationException("Unsupported"); }
   public <T1> T1[] toArray(final T1[] a) { throw new UnsupportedOperationException("Unsupported"); }
   public boolean add(final T e)  { throw new UnsupportedOperationException("Unsupported"); }
   public boolean remove(final Object o) { throw new UnsupportedOperationException("Unsupported"); }
   public boolean containsAll(final Collection<?> c)  { throw new UnsupportedOperationException("Unsupported"); }
   public boolean addAll(final Collection<? extends T> c)  { throw new UnsupportedOperationException("Unsupported"); }
    public boolean addAll(final int index, final Collection<? extends T> c)  { throw new UnsupportedOperationException("Unsupported"); }
    public boolean removeAll(final Collection<?> c)  { throw new UnsupportedOperationException("Unsupported"); }
    public boolean retainAll(final Collection<?> c) { throw new UnsupportedOperationException("Unsupported"); }
    public void clear()  { throw new UnsupportedOperationException("Unsupported"); }
    public T get(final int index) { throw new UnsupportedOperationException("Unsupported"); }
    public T set(final int index, final T element) { throw new UnsupportedOperationException("Unsupported"); }
    public void add(final int index, final T element)  { throw new UnsupportedOperationException("Unsupported"); }
     public T remove(final int index) { throw new UnsupportedOperationException("Unsupported"); }
     public int indexOf(final Object o) { throw new UnsupportedOperationException("Unsupported"); }
     public int lastIndexOf(final Object o) { throw new UnsupportedOperationException("Unsupported"); }
     public ListIterator<T> listIterator()  { throw new UnsupportedOperationException("Unsupported"); }
     public ListIterator<T> listIterator(final int index) { throw new UnsupportedOperationException("Unsupported"); }
    public List<T> subList(final int fromIndex, final int toIndex)  { throw new UnsupportedOperationException("Unsupported"); }
}
