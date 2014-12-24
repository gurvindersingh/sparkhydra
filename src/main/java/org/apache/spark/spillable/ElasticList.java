package org.apache.spark.spillable;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

/**
 * org.apache.spark.examples.ElasticList
 * This is a  one shot collection which will spill data to disk when spillLimit is
 * exceeded - the idea is to allow operations like Flatmap to keep data in memory up to a limit
 * and then spill to some temporary file
 * How that spill works remains to be determined
 * NOTE only add operations are supported and once an iterator is requested these are not allowed
 * Also only one iterator is allowed
 * <p/>
 * NOTE Spill is not implemented but the current code will work as long as spill limit is not reached
 * User: Steve
 * Date: 10/9/2014
 */
public class ElasticList<K> implements Collection<K>, Serializable {

    /**
     * when the queue contains more then spillLimit is Spilling
     */
    public static final int DEFAULT_SPILL_LIMIT = 100000;

    private final int spillLimit;
    private final AtomicInteger size = new AtomicInteger(0);
    private final List<K> in_memory = new ArrayList<K>();
    private final AtomicBoolean spilled = new AtomicBoolean(false);
    // once an iterator is handed out no more adds
    private final AtomicBoolean iterating = new AtomicBoolean(false);

    public ElasticList() {
        this(DEFAULT_SPILL_LIMIT);
    }

    public ElasticList(int spillLimit) {
        this.spillLimit = spillLimit;
    }

    /**
     * if true the collection has spilled to disk and will need to use the disk iterator
     *
     * @return
     */
    protected boolean isSpilled() {
        return spilled.get();
    }

    /**
     * once iterating no further iterators are issued todo do we need this restriction
     * and no adds are allowed
     *
     * @param b
     */
    protected void setIterating(boolean b) {
        iterating.set(b);
    }

    /**
     * once iterating no further iterators are issued todo do we need this restriction
     * and no adds are allowed
     *
     * @return
     */
    protected boolean isIterating() {
        return iterating.get();
    }

    /**
     * once data is spilled to disk we treat the collection as on disk
     *
     * @param b
     */
    protected void setSpilled(boolean b) {
        spilled.set(b);
    }

    /**
     * how many items are allowed before spill to disk
     *
     * @return
     */
    public int getSpillLimit() {
        return spillLimit;
    }

    /**
     * Returns the number of elements in this list.  If this list contains
     * more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @return the number of elements in this list
     */
    @Override
    public int size() {
        return size.get();
    }


    @Override
    public synchronized boolean add(final K e) {
        if (isIterating())
            throw new IllegalStateException("addition is not supported once iteration is started");
        in_memory.add(e);
        int currentSize = size.addAndGet(1);
        if (currentSize > getSpillLimit())
            startSpill();
        return true;  // add always changes the collection
    }

    /**
     * set the system up for spill
     */
    protected synchronized void startSpill() {
        if (isSpilled())
            return;
        setSpilled(true);
        throw new UnsupportedOperationException("What Now - start Spill to disk"); // ToDo
    }

    /**
     * spill remaining items putting all data on the disk
     */
    protected synchronized void finishSpill() {
        if (isSpilled())
            return;
        // get all remaining items and spill that
        List<K> remaining = new ArrayList<K>(in_memory.size());
        in_memory.removeAll(remaining);
        for (K k : remaining) {
            spillOne(k);
        }
    }

    /**
     * spill remaining items putting all data on the disk
     */
    protected boolean spillOne(K e) {
        throw new UnsupportedOperationException("What Now - start Spill to disk"); // ToDo
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     *
     * @return an iterator over the elements in this list in proper sequence
     */
    @Override
    public synchronized Iterator<K> iterator() {
        if (isIterating())
            throw new IllegalStateException("Iteration is suooprted only once");
        // we are iterating so stop accepting data
        setIterating(true);
        if (!isSpilled())
            return in_memory.iterator();
        finishSpill();
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }


    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(final Object o) {
        throw new UnsupportedOperationException("Not supported by this class");
    }


    @Override
    public Object[] toArray() {
        throw new UnsupportedOperationException("Not supported by this class");
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        throw new UnsupportedOperationException("Not supported by this class");
    }

    @Override
    public boolean remove(final Object o) {
        return false;
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        throw new UnsupportedOperationException("Not supported by this class");
    }

    @Override
    public boolean addAll(final Collection<? extends K> c) {
        boolean ret = false;
        for (K k : c) {
            ret |= add(k);
        }
        return ret;
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException("Not supported by this class");
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException("Not supported by this class");
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported by this class");
    }


}
