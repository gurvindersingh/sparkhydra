package com.lordjoe.distributed.input;

/**
 * com.lordjoe.distributed.input.MGFInputFormat
 * User: Steve
 * Date: 9/23/2014
 */

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.*;
import org.apache.hadoop.io.compress.*;
import org.apache.hadoop.mapred.*;
import org.apache.hadoop.util.*;

import java.io.*;

/**
 * com.lordjoe.distributed.input.MGFInputFormat
 * Splitter that reads mgf files
 * nice enough to put the begin and end tags on separate lines
 */
public class MGFOldInputFormat extends FileInputFormat<String, String> {

    private String m_Extension = "mgf";

    public MGFOldInputFormat() {
    }
     public String getExtension() {
        return m_Extension;
    }

    public void setExtension(final String pExtension) {
        m_Extension = pExtension;
    }


    @Override
    public RecordReader<String, String> getRecordReader(final InputSplit pInputSplit, final JobConf pEntries, final Reporter pReporter) throws IOException {
        MGFFileReader mgfFileReader = new MGFFileReader();
        mgfFileReader.initialize(pInputSplit, pEntries);
        return mgfFileReader;
    }


    /**
     * Custom RecordReader which returns thetext between BEGIN IONS and END IONS
     * single value with the name as a key
     * Value is the entire file
     * Key is the file name
     */
    public class MGFFileReader implements RecordReader<String, String> {

        private CompressionCodecFactory compressionCodecs = null;
        private long m_Start;
        private long m_End;
        private long current;
        private LineReader m_Input;
        FSDataInputStream m_RealFile;
        private String key = null;
        private String value = null;
//        private String buffer = new String();

        public void initialize(InputSplit genericSplit,
                               Configuration job) throws IOException {
            FileSplit split = (FileSplit) genericSplit;
            m_Start = split.getStart();
            m_End = m_Start + split.getLength();
            final Path file = split.getPath();
            compressionCodecs = new CompressionCodecFactory(job);
            boolean skipFirstLine = false;
            final CompressionCodec codec = compressionCodecs.getCodec(file);
            Text buffer = new Text();

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
                m_Start += m_Input.readLine(buffer);
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
        public boolean nextKeyValue() throws IOException
       {
            Text buffer = new Text();

            int newSize = 0;
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
            newSize = m_Input.readLine(buffer);
            String str = null;
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
                newSize = m_Input.readLine(buffer);
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

            String s = sb.toString();
            value = s;

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
        public boolean next(final String pKey, final String pValue) throws IOException {
            if (!nextKeyValue())
                return false;
              key = pKey;
            throw new UnsupportedOperationException("Fix This"); // ToDo
          //  pValue.setLength(0);
          //  pValue.append(value);
           // return true;
        }


        @Override
        public String createKey() {
            return new String();
        }

        @Override
        public String createValue() {
            return new String();
        }

        @Override
        public long getPos() throws IOException {
            return 0;
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
