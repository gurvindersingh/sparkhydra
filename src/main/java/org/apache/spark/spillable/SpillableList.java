package org.apache.spark.spillable;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

 /**
 * org.apache.spark.spillable.SpillableTupleList
 * SpillableTupleList is a simple {@link Iterable} object that can store an unlimited number of   instances by spilling
 * excess to a temporary disk file.
  *
 * Code stolen from cascading.tuple.SpillableTupleList;
 * User: Steve
 * Date: 10/9/2014
 */

public class SpillableList<K extends Serializable> implements Iterable<K>, Serializable {

    /**
     * if not otherwise specificed we store this many objects in memory before spilling
     */
    public static final long DEFAULT_THRESHOLD = 100000L;

    /**
     * Field threshold
     */
    private final long threshold;
    /**
     * Field files
     */
    private final List<File> files = new LinkedList<File>();
    /**
     * Field current
     */
    private final List<K> current = new LinkedList<K>();
    /**
     * Field size
     */
    private final AtomicLong size = new AtomicLong(0);

    /**
     * Constructor SpillableTupleList creates a new SpillableTupleList instance.
     * uses  DEFAULT_THRESHOLDfor when to spill
     */
    public SpillableList() {
        this(DEFAULT_THRESHOLD);
    }

    /**
     * Constructor SpillableTupleList creates a new SpillableTupleList instance using the given threshold
     * value.
      * @param threshold  when more than Threshold items are added a spill file will be created
     *                    This can be adjusted based on available memory and item size
     */
    public SpillableList(long threshold) {
        this.threshold = threshold;
    }

    protected long getThreshold() {
        return threshold;
    }

    protected List<File> getFiles() {
        return files;
    }

    protected List<K> getCurrent() {
        return current;
    }




    /**
     * Method add will add the given {@link K} to this list.
     *
     * @param tuple of type K
     */
    public synchronized void add(K tuple) {
        current.add(tuple);
        size.getAndIncrement();
        // spill if needed
        doSpill();
    }


    /**
     * Method size returns the size of this list.
     *
     * @return long
     */
    public long size() {
        return size.get();
    }

    /**
     * Method getNumFiles returns the number of files this list has spilled to.
     *
     * @return the numFiles (type int) of this SpillableTupleList object.
     */
    @SuppressWarnings("UnusedDeclaration")
    public int getNumFiles() {
        return files.size();
    }

    protected final void doSpill() {
        if (current.size() != threshold)
            return;


        File file = createTempFile();
        ObjectOutputStream dataOutputStream = createTupleOutputStream(file);

        try {
            writeList(dataOutputStream, current);
        }
        finally {
            flushSilent(dataOutputStream);
            closeSilent(dataOutputStream);
        }

        files.add(file);
        current.clear();
    }

    protected void flushSilent(Flushable flushable) {
        try {
            flushable.flush();
        }
        catch (IOException exception) {
            // ignore
        }
    }

    protected void closeSilent(Closeable closeable) {
        try {
            closeable.close();
        }
        catch (IOException exception) {
            // ignore
        }
    }

    protected void writeList(ObjectOutputStream dataOutputStream, List<K> list) {
        try {
            dataOutputStream.writeLong(list.size());

            for (K tuple : list)
                dataOutputStream.writeObject(tuple);
        }
        catch (IOException exception) {
            throw new RuntimeException("unable to write to file output stream", exception);
        }
    }

    protected ObjectOutputStream createTupleOutputStream(File file) {
        try {
            return new ObjectOutputStream(new FileOutputStream(file));
        }
        catch (IOException exception) {
            throw new RuntimeException("unable to create temporary file input stream", exception);
        }
    }

    protected List<K> readList(ObjectInputStream tupleInputStream) {
        try {
            long size = tupleInputStream.readLong();
            List<K> list = new LinkedList<K>();

            for (int i = 0; i < size; i++)
                //noinspection unchecked
                list.add((K) tupleInputStream.readObject());

            return list;
        }
        catch (Exception exception) {
            throw new RuntimeException("unable to read from file output stream", exception);
        }
    }

    protected ObjectInputStream createTupleInputStream(File file) {
        try {
            return new ObjectInputStream(new FileInputStream(file));
        }
        catch (IOException exception) {
            throw new RuntimeException("unable to create temporary file output stream", exception);
        }
    }

    protected File createTempFile() {
        try {
            File file = File.createTempFile("cascading-spillover", null);
            file.deleteOnExit();

            return file;
        }
        catch (IOException exception) {
            throw new RuntimeException("unable to create temporary file", exception);
        }
    }


    /**
     * Method iterator returns a K Iterator of all the values in this collection.
     *
     * @return Iterator<K>
     */
    public Iterator<K> iterator() {
        if (files.isEmpty())
            return current.iterator();

        return new SpilledListIterator();
    }

//  /**
//   * Method entryIterator returns a TupleEntry Iterator of all the alues in this collection.
//   * @return Iterator<TupleEntry>
//   */
//  public Iterator<TupleEntry> entryIterator()
//    {
//    if( files.isEmpty() )
//      return new TupleEntryIterator( fields, current.iterator() );
//
//    return new TupleEntryIterator( fields, new SpilledListIterator() );
//    }

    protected class SpilledListIterator implements Iterator<K> {
        int fileIndex = 0;
        List<K> currentList;
        protected Iterator<K> iterator;

        protected SpilledListIterator() {
            getNextList();
        }

        protected void getNextList() {
            if (fileIndex < files.size())
                currentList = getListFor(files.get(fileIndex++));
            else
                currentList = current;

            iterator = currentList.iterator();
        }

        protected List<K> getListFor(File file) {
            ObjectInputStream dataInputStream = createTupleInputStream(file);

            try {
                return readList(dataInputStream);
            }
            finally {
                closeSilent(dataInputStream);
            }
        }

        public boolean hasNext() {
            if (currentList == current)
                return iterator.hasNext();

            if (iterator.hasNext())
                return true;

            getNextList();

            return hasNext();
        }

        public K next() {
            if (currentList == current || iterator.hasNext())
                return iterator.next();

            getNextList();

            return next();
        }

        public void remove() {
            throw new UnsupportedOperationException("remove is not supported");
        }
    }
}