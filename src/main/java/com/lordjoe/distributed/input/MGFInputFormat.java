package com.lordjoe.distributed.input;

/**
 * com.lordjoe.distributed.input.MGFInputFormat
 * User: Steve
 * Date: 9/24/2014
 */

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.*;
import org.apache.hadoop.io.compress.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.util.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.hadoop.MGFInputFormat
 * Splitter that reads mgf files
 * nice enough to put the begin and end tags on separate lines
 */
public class MGFInputFormat extends FileInputFormat<String, String> implements Serializable {

    private String m_Extension = "mgf";

    public MGFInputFormat() {

    }

    @Override
    public List<InputSplit> getSplits(final JobContext job) throws IOException {

        List<InputSplit> splits = super.getSplits(job);
        // make sure not too many splits
        while(splits.size() > 200)  {
            Configuration configuration = job.getConfiguration();
            long oldMax = configuration.getLong(FileInputFormat.SPLIT_MAXSIZE,Integer.MAX_VALUE);
            configuration.setLong(FileInputFormat.SPLIT_MAXSIZE, oldMax * 2);
            splits = super.getSplits(job);
        }
        return splits;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getExtension() {
        return m_Extension;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setExtension(final String pExtension) {
        m_Extension = pExtension;
    }


    @Override
    public RecordReader<String, String> createRecordReader(InputSplit split,
                                                           TaskAttemptContext context) {
        return new MGFFileReader();
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        final String lcName = file.getName().toLowerCase();
        //noinspection RedundantIfStatement
        if (lcName.endsWith("gz"))
            return false;
        return true;
    }

    /**
     * Custom RecordReader which returns the entire file as a
     * single value with the name as a key
     * Value is the entire file
     * Key is the file name
     */
    public class MGFFileReader extends RecordReader<String, String> implements Serializable {

        private CompressionCodecFactory compressionCodecs = null;
        private long m_Start;
        private long m_End;
        private long current;
        private LineReader m_Input;
        FSDataInputStream m_RealFile;
        private String key = null;
        private String value = null;
        private Text buffer; // must be

        public Text getBuffer() {
            if (buffer == null)
                buffer = new Text();
            return buffer;
        }

        public void initialize(InputSplit genericSplit,
                               TaskAttemptContext context) throws IOException {
            FileSplit split = (FileSplit) genericSplit;
            Configuration job = context.getConfiguration();
            m_Start = split.getStart();
            m_End = m_Start + split.getLength();
            final Path file = split.getPath();
            compressionCodecs = new CompressionCodecFactory(job);
            boolean skipFirstLine = false;
            final CompressionCodec codec = compressionCodecs.getCodec(file);

            // open the file and seek to the m_Start of the split
            FileSystem fs = file.getFileSystem(job);
            // open the file and seek to the m_Start of the split
            m_RealFile = fs.open(split.getPath());
            if (codec != null) {
                CompressionInputStream inputStream = codec.createInputStream(m_RealFile);
                m_Input = new LineReader(inputStream);
                m_End = Long.MAX_VALUE;
            }
            else {
                if (m_Start != 0) {
                    skipFirstLine = true;
                    --m_Start;
                    m_RealFile.seek(m_Start);
                }
                m_Input = new LineReader(m_RealFile);
            }
            // not at the beginning so go to first line
            if (skipFirstLine) {  // skip first line and re-establish "m_Start".
                m_Start += m_Input.readLine(getBuffer());
            }

            current = m_Start;
            key = split.getPath().getName();

            current = 0;
        }

        /**
         * look for a <scan tag then read until it closes
         *
         * @return true if there is data
         * @throws java.io.IOException
         */
        public boolean nextKeyValue() throws IOException {
            int newSize;
            while (current < m_Start) {
                newSize = m_Input.readLine(buffer);
                // we are done
                if (newSize == 0) {
                    key = null;
                    value = null;
                    return false;
                }
                current = m_RealFile.getPos();
            }
            StringBuilder sb = new StringBuilder();
            newSize = m_Input.readLine(getBuffer());
            String str;
            while (newSize > 0) {
                str = buffer.toString();

                if ("BEGIN IONS".equals(str)) {
                    break;
                }
                current = m_RealFile.getPos();
                // we are done
                if (current > m_End) {
                    key = null;
                    value = null;
                    return false;

                }
                newSize = m_Input.readLine(getBuffer());
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
                if ("END IONS".equals(str)) {
                    break;
                }
                newSize = m_Input.readLine(buffer);
            }

             value = sb.toString();

            if (sb.length() == 0) {
                key = null;
                value = null;
                return false;
            }
            else {
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
            long totalBytes = m_End - m_Start;
            long totalHandled = current - m_Start;
            return ((float) totalHandled) / totalBytes;
        }


        public synchronized void close() throws IOException {
            if (m_Input != null) {
                m_Input.close();
            }
        }
    }
}