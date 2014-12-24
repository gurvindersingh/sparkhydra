package com.lordjoe.distributed.hadoop;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.io.compress.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.util.*;

import java.io.*;

/**
 * org.systemsbiology.hadoop.KeyValueXMLTagInputFormat
 * parses the output of a text output file as key\t<MultilineXMLFragment
 * User: steven
 * Date: 3/28/11
 */
public class KeyValueXMLTagInputFormat extends XMLTagInputFormat {
    public static final KeyValueXMLTagInputFormat[] EMPTY_ARRAY = {};

    public KeyValueXMLTagInputFormat(final String pBaseTag) {
        super(pBaseTag);
    }


    @Override
    public RecordReader<String, String> createRecordReader(InputSplit split,
                                                       TaskAttemptContext context) {
        return new XMLTagReaderWithKey();
    }


    /**
     * Custom RecordReader which returns the entire file as a
     * single value with the name as a key
     * Value is the entire file
     * Key is the file name
     */
    public class XMLTagReaderWithKey extends RecordReader<String, String> {

        private CompressionCodecFactory compressionCodecs = null;
        private long start;
        private long end;
        private long current;
        private LineReader in;
        private String key = null;
        private String value = null;
        private Text buffer = new Text();

        public void initialize(InputSplit genericSplit,
                               TaskAttemptContext context) throws IOException {
            FileSplit split = (FileSplit) genericSplit;
            Configuration job = context.getConfiguration();
            start = split.getStart();
            end = start + split.getLength();
            final Path file = split.getPath();
            compressionCodecs = new CompressionCodecFactory(job);
            final CompressionCodec codec = compressionCodecs.getCodec(file);

            // open the file and seek to the start of the split
            FileSystem fs = file.getFileSystem(job);
            FSDataInputStream fileIn = fs.open(split.getPath());
            if (start > 0)
                fileIn.seek(start);

            if (codec != null) {
                in = new LineReader(codec.createInputStream(fileIn), job);
                end = Long.MAX_VALUE;
            } else {
                in = new LineReader(fileIn, job);
            }
            current = start;
              key = split.getPath().getName();

        }

        /**
         * look for a <scan tag then read until it closes
         *
         * @return true if there is data
         * @throws java.io.IOException
         */
        public boolean nextKeyValue() throws IOException {
            int newSize = 0;
            StringBuilder sb = new StringBuilder();
            newSize = in.readLine(buffer);
            String str = null;
            while (newSize > 0) {
                str = buffer.toString();
                if (str.contains(getStartTag()))
                    break;
                newSize = in.readLine(buffer);
            }
            if (newSize == 0) {
                key = null;
                value = null;
                return false;

            }
            while (newSize > 0) {
                str = buffer.toString();
                sb.append(str);
                sb.append("\n");
                if (str.contains(getEndTag()))
                    break;
                newSize = in.readLine(buffer);
            }

            String s = sb.toString();
            // up to tab is the key
            int index = s.indexOf("\t");
            key = s.substring(0, index);
            value = s.substring(index + 1);

            if (sb.length() == 0) {
                key = null;
                value = null;
                return false;
            } else {
                return true;
            }
        }

        @Override
        public String getCurrentKey() {
            return key;
        }

        @Override
        public String getCurrentValue() {
            return value;
        }

        /**
         * Get the progress within the split
         */
        public float getProgress() {
            long totalBytes = end - start;
            long totalhandled = current - start;
            return ((float) totalhandled) / totalBytes;
        }


        public synchronized void close() throws IOException {
            if (in != null) {
                in.close();
            }
        }
    }

}
