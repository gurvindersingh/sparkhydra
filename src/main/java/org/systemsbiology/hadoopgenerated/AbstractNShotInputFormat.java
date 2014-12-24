package org.systemsbiology.hadoopgenerated;


import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;


import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.hadoopgenerated.NShotInputFormat
 * <p/>
 * This is an example of an  InputFormat which does not read files
 * It passes N keys where N is a static property NumberKeys
 * The most obvious uses of the class are for
 * a) testing where a subclass might generate and send specific data
 * b) cases where the data is not in a file such as a brute force attack
 * on a key where test primes are generated and sent
 *
 * @author Steve Lewis
 * @date Oct 10, 2010
 */
public abstract class AbstractNShotInputFormat<K, V> extends InputFormat<K, V> {
    public static AbstractNShotInputFormat[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = AbstractNShotInputFormat.class;

    private static long gNumberKeys = 20;
    private static int gNumberSplits = 5;

    /**
     * this many keys will be sent
     *
     * @return
     */
    public static long getNumberKeys() {
        return gNumberKeys;
    }

    public static void setNumberKeys(long pNumberKeys) {
        gNumberKeys = pNumberKeys;
    }


    /**
     * this many splits will be used
     *
     * @return
     */
    public static int getNumberSplits() {
        return gNumberSplits;
    }

    public static void setNumberSplits(int pNumberSplits) {
        gNumberSplits = pNumberSplits;
    }


    /**
     * Implement to generate a ressomable key
     *
     * @param index current key index
     * @return non-null key
     */
    protected abstract K getKeyFromIndex(long index);

    /**
     * Implement to generate a reasonable key
     *
     * @param index current key index
     * @return non-null key
     */
    protected abstract V getValueFromIndex(long index);


    /**
     * Logically split the set of input files for the job.
     * <p/>
     * <p>Each {@link org.apache.hadoop.mapreduce.InputSplit} is then assigned to an individual {@link org.apache.hadoop.mapreduce.Mapper}
     * for processing.</p>
     * <p/>
     * <p><i>Note</i>: The split is a <i>logical</i> split of the inputs and the
     * input files are not physically split into chunks. For e.g. a split could
     * be <i>&lt;input-file-path, start, offset&gt;</i> tuple. The InputFormat
     * also creates the {@link org.apache.hadoop.mapreduce.RecordReader} to read the {@link org.apache.hadoop.mapreduce.InputSplit}.
     *
     * @param context job configuration.
     * @return an array of {@link org.apache.hadoop.mapreduce.InputSplit}s for the job.
     */
    @Override
    public List<InputSplit> getSplits(JobContext context)
            throws IOException, InterruptedException {
        int numSplits = getNumberSplits();

        ArrayList<InputSplit> splits = new ArrayList<InputSplit>(numSplits);
        for (int i = 0; i < numSplits; i++) {
            final NShotInputSplit inputSplit = new NShotInputSplit();
            inputSplit.setIndex(i);
            splits.add(inputSplit);
        }


        return splits;
    }


    /**
     * Create a record reader for a given split. The framework will call
     * {@link org.apache.hadoop.mapreduce.RecordReader#initialize(org.apache.hadoop.mapreduce.InputSplit, org.apache.hadoop.mapreduce.TaskAttemptContext)} before
     * the split is used.
     *
     * @param split   the split to be read
     * @param context the information about the task
     * @return a new record reader
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    @Override
    public RecordReader<K, V> createRecordReader(
            InputSplit split,
            TaskAttemptContext context)
            throws IOException, InterruptedException {
        return new NShotDataRecordReader((NShotInputSplit) split, this);
    }


    /**
     * All the work is done here to generate the keys
     * In this sample we use simple code to generate keys but a
     * different version might use a more sophistocated scheme
     */
    public static class NShotDataRecordReader<K, V> extends RecordReader<K, V> {

        private long m_NFired;
        private final NShotInputSplit m_Splitter;
        private final AbstractNShotInputFormat<K, V> m_Parent;

        public NShotDataRecordReader(NShotInputSplit split, AbstractNShotInputFormat<K, V> parent) {
            m_Parent = parent;
            m_Splitter = split;
            m_NFired = m_Splitter.getIndex(); // start each recorder at different value
        }

        /**
         * Called once at initialization.
         *
         * @param split   the split that defines the range of records to read
         * @param context the information about the task
         * @throws java.io.IOException
         * @throws InterruptedException
         */
        @Override
        public void initialize(InputSplit split, TaskAttemptContext context)
                throws IOException, InterruptedException {

        }

        public NShotInputSplit getSplitter() {
            return m_Splitter;
        }

        public long getNFired() {
            return m_NFired;
        }


        public void close() {
        }

        public float getProgress() {
            return getNFired() / (float) getNumberKeys();
        }

        public long getPos() {
            return getNFired();
        }

        public boolean next(LongWritable key, Text value) {
            if (getNFired() >= getNumberKeys())
                return false;

            m_NFired += getNumberSplits();
            return true;
        }


        /**
         * Read the next key, value pair.
         *
         * @return true if a key/value pair was read
         * @throws java.io.IOException
         * @throws InterruptedException
         */
        @Override
        public boolean nextKeyValue() throws IOException, InterruptedException {
            if (getNFired() >= getNumberKeys())
                return false;

            m_NFired += getNumberSplits();
            return true;
        }

        /**
         * Get the current key
         *
         * @return the current key or null if there is no current key
         * @throws java.io.IOException
         * @throws InterruptedException
         */
        @Override
        public K getCurrentKey() throws IOException, InterruptedException {
            return (K) m_Parent.getKeyFromIndex(m_NFired - getNumberSplits());
        }

        /**
         * Get the current value.  Override for
         *
         * @return the object that was read
         * @throws java.io.IOException
         * @throws InterruptedException
         */
        @Override
        public V getCurrentValue() throws IOException, InterruptedException {
            return (V) m_Parent.getValueFromIndex(m_NFired - getNumberSplits());
        }
    }

    @SuppressWarnings(value = "deprecated")
    public static class NShotInputSplit extends InputSplit
            // NOTE IF THE DEPRECATED INTERFACE IS NOT IMP{LEMENTED THE OBJECT WILL NOT SERIALIZE
            implements org.apache.hadoop.mapred.InputSplit {

        private long m_Index;

        public NShotInputSplit() {
        }


        public long getIndex() {
            return m_Index;
        }

        public void setIndex(long pIndex) {
            m_Index = pIndex;
        }

        public long getLength() {
            return getNumberKeys() / getNumberSplits();
        }

        public String[] getLocations() throws IOException {
            return new String[]{};
        }

        public void readFields(DataInput in) throws IOException {
            setIndex(in.readLong());
        }

        public void write(DataOutput out) throws IOException {
            out.writeLong(this.m_Index);
        }


    }

}
