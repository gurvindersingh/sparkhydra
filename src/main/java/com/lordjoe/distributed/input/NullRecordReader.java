package com.lordjoe.distributed.input;

import org.apache.hadoop.mapreduce.*;

import java.io.*;

/**
 * com.lordjoe.distributed.input.NullRecordReader
 *  This is a recordReader to use when there is no Data - usually when
 *  the normal record reader would be messy
 * User: Steve
 * Date: 9/24/2014
 */

public class NullRecordReader<K> extends RecordReader<String, K> {

    // only one ever built
    public static RecordReader<String, String> INSTANCE = new NullRecordReader();

    public NullRecordReader() {
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
    public void initialize(final InputSplit split, final TaskAttemptContext context) throws IOException, InterruptedException {

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
        return false;
    }

    /**
     * Get the current key
     *
     * @return the current key or null if there is no current key
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    @Override
    public String getCurrentKey() throws IOException, InterruptedException {
        return null;
    }

    /**
     * Get the current value.
     *
     * @return the object that was read
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    @Override
    public K getCurrentValue() throws IOException, InterruptedException {
        return null;
    }

    /**
     * The current progress of the record reader through its data.
     *
     * @return a number between 0.0 and 1.0 that is the fraction of the data read
     * @throws java.io.IOException
     * @throws InterruptedException
     */
    @Override
    public float getProgress() throws IOException, InterruptedException {
        return 0;
    }

    /**
     * Close the record reader.
     */
    @Override
    public void close() throws IOException {

    }
}
 