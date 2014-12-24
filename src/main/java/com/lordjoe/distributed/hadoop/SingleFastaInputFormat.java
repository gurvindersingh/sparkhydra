package com.lordjoe.distributed.hadoop;

/**
 * User: steven
 * Date: 3/7/11
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
 * org.systemsbiology.xtandem.hadoop.SingleFastaInputFormat
 * Splitter that reads a Fasta format file - the key is the label (hopefully unique)
 * this is a special version which makes each protein a separate split
 * The value is the concatinated string without breaks
 */

@SuppressWarnings("UnusedDeclaration")
public class SingleFastaInputFormat extends FileInputFormat<String, String> {
    public static final SingleFastaInputFormat[] EMPTY_ARRAY = {};


    public static final boolean FORCE_ONE_MAPPER = false;
    // todo run off a parameter
    // setting this small forces many mappers
    public static final int MAX_ENTRY_SIXE = 1024 * 1024 * 1024;


    private String m_Extension = "fasta";

    @SuppressWarnings("UnusedDeclaration")
    public SingleFastaInputFormat() {

    }


    public String getExtension() {
        return m_Extension;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setExtension(final String pExtension) {
        m_Extension = pExtension;
    }

    public boolean isSplitReadable(InputSplit split) {
        if (!(split instanceof FileSplit))
            return true;
        FileSplit fsplit = (FileSplit) split;
        Path path1 = fsplit.getPath();
        return isPathAcceptable(path1);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    protected boolean isPathAcceptable(final Path pPath1) {
        String path = pPath1.toString().toLowerCase();
        if (path.startsWith("part-r-"))
            return true;
        String extension = getExtension();
        if (extension != null && path.endsWith(extension.toLowerCase()))
            return true;
        if (extension != null && path.endsWith(extension.toLowerCase() + ".gz"))
            return true;
        return extension == null;
    }

    @Override
    public RecordReader<String, String> createRecordReader(InputSplit split,
                                                       TaskAttemptContext context) {
        if (isSplitReadable(split))
            return new OneEntryFastaFileReader();
        else
            return  NullRecordReader.INSTANCE; // do not read
    }

    @SuppressWarnings({"SimplifiableIfStatement", "PointlessBooleanExpression"})
    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        String fname = file.getName().toLowerCase();
        if (fname.endsWith(".gz"))
            return false;

        return !FORCE_ONE_MAPPER;
    }

    protected List<InputSplit> getFileSplits(JobContext job, FileStatus file) {
        try {
            List<InputSplit> holder = new ArrayList<InputSplit>();
            Path path = file.getPath();
            FileSystem fs = path.getFileSystem(job.getConfiguration());
            FSDataInputStream fileIn = fs.open(path);
            CompressionCodecFactory compressionCodecs = new CompressionCodecFactory(job.getConfiguration());
            final CompressionCodec codec = compressionCodecs.getCodec(path);
            long length = file.getLen();
            BlockLocation[] blkLocations = fs.getFileBlockLocations(file, 0, length);


            LineReader inp;

            if (codec != null) {
                inp = new LineReader(codec.createInputStream(fileIn));
            }
            else {
                inp = new LineReader(fileIn);
            }
            Text lineText = new Text();
            long splitStart = 0;
            long splitLength = 0;
            long len = inp.readLine(lineText, Integer.MAX_VALUE, MAX_ENTRY_SIXE);

            FileSplit fsplit;
            while (len > 0) {
                String line = lineText.toString();
                if (line.startsWith(">")) {
                    if (splitLength == 0) {  // not in split
                        splitLength += len; // add first split

                    }
                    else {
                        fsplit = new FileSplit(path, splitStart, splitLength,
                                blkLocations[blkLocations.length - 1].getHosts());
                        holder.add(fsplit);
                        splitStart += splitLength; // move to next
                        splitLength = len; // add this lint

                    }
                }
                else {
                    if (splitLength == 0) {  // first split
                        splitStart += len; // not at a split
                    }
                    else {
                        splitLength += len; // add to a split
                    }

                }
                len = inp.readLine(lineText, Integer.MAX_VALUE, MAX_ENTRY_SIXE); // read next line
            }
            // last protein in file
            if (splitLength > 0) {
                fsplit = new FileSplit(path, splitStart, splitLength,
                        blkLocations[blkLocations.length - 1].getHosts());
                holder.add(fsplit);


            }


            return holder;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * Generate the list of files and make them into FileSplits.
     * This needs to be copied to insert a filter on acceptable data
     */
    @SuppressWarnings("ForLoopReplaceableByForEach")
    @Override
    public List<InputSplit> getSplits(JobContext job) throws IOException {


        //   maxSize = SPLIT_BLOCK_SIZE; // force more mappers
        // generate splits
        List<InputSplit> splits = new ArrayList<InputSplit>();

        Path[] paths = getInputPaths(job);
        for (int i = 0; i < paths.length; i++) {
            Path path = paths[i];
            System.err.println("Input path " + path.toString());
        }
        List<FileStatus> fileStatuses = listStatus(job);
        // if there is only one file we may force more than the default mappers

        for (FileStatus file : fileStatuses) {
            Path path = file.getPath();
            if (!isPathAcceptable(path))   // filter acceptable data
                continue;
            long length = file.getLen();
            if ((length != 0) && isSplitable(job, path)) {
                List<InputSplit> fileSplits = getFileSplits(job, file);
                splits.addAll(fileSplits);
            }
        }
   //     LOG.debug("Total # of splits: " + splits.size());
        return splits;
    }

    /**
     * Custom RecordReader which returns the entire file as a
     * single m_Value with the name as a m_Key
     * Value is the entire file
     * Key is the file name
     */
    public class OneEntryFastaFileReader extends RecordReader<String, String> {

        private CompressionCodecFactory compressionCodecs = null;
        private long m_Start;
        private long m_End;
        private long m_Current;
        private LineReader m_Input;
        private String m_Key;
        private String m_Value = null;
        private final Text m_Line = new Text();
        private int m_MaxLineLength;
        private StringBuilder m_Sb = new StringBuilder();
        private String m_CurrentLine;
        private FSDataInputStream m_FileIn;

        public void initialize(InputSplit genericSplit,
                               TaskAttemptContext context) throws IOException {
            FileSplit split = (FileSplit) genericSplit;
            Configuration job = context.getConfiguration();
            m_MaxLineLength = job.getInt("mapred.linerecordreader.maxlength",
                    Integer.MAX_VALUE);
            m_Sb.setLength(0);
            m_Start = split.getStart();
            m_End = m_Start + split.getLength();
            final Path file = split.getPath();
            compressionCodecs = new CompressionCodecFactory(job);
            final CompressionCodec codec = compressionCodecs.getCodec(file);
            boolean skipFirstLine = false;

            // open the file and seek to the m_Start of the split
            FileSystem fs = file.getFileSystem(job);
            m_FileIn = fs.open(split.getPath());
            if (codec != null) {
                m_Input = new LineReader(codec.createInputStream(m_FileIn), job);
                m_End = Long.MAX_VALUE;
            }
            else {
                m_FileIn.seek(m_Start);
                m_Input = new LineReader(m_FileIn, job);
            }
            // not at the beginning so go to first line
            m_Current = m_Start;
             m_Key = split.getPath().getName();

        }

        /**
         * look for a <scan tag then read until it closes
         *
         * @return true if there is data
         * @throws java.io.IOException
         */
        public boolean nextKeyValue() throws IOException {

            if (m_Current > m_End) {  // we are the the end of the split
                m_Key = null;
                m_Value = null;
                return false;
            }

//            // advance to the start - probably done in initialize
//            while (m_FileIn.getPos() < m_Start) {
//                m_CurrentLine = m_Input.readLine();
//            }
            // read more data
            if (m_CurrentLine == null) {
                m_CurrentLine = readNextLine();
            }

            while (m_FileIn.getPos() < m_End && m_CurrentLine != null && !m_CurrentLine.startsWith(">")) {
                m_CurrentLine = readNextLine();
            }

            if (m_CurrentLine == null || !m_CurrentLine.startsWith(">")) {  // we are the the end of data
                m_Key = null;
                m_Value = null;
                return false;
            }

            // label = key
            String key = m_CurrentLine.substring(1);
            m_Key = key;

            m_Sb.setLength(0);
            m_CurrentLine = readNextLine();
            while (m_CurrentLine != null && !m_CurrentLine.startsWith(">")) {
                m_Sb.append(m_CurrentLine);
                m_CurrentLine = readNextLine();

            }

            if (m_Sb.length() == 0) {  // cannot read
                m_Key = null;
                m_Value = null;
                return false;
            }


            String value = m_Sb.toString();
            m_Value = value;
            m_Sb.setLength(0); // clear the buffer

            m_Current = m_FileIn.getPos();
            return true;
        }


        protected String readNextLine() throws IOException {
            int newSize = m_Input.readLine(m_Line, m_MaxLineLength,
                    Math.max(Math.min(Integer.MAX_VALUE, (int) (m_End - m_Current)),
                            m_MaxLineLength));
            m_Current += newSize;
            if (newSize == 0)
                return null;
            return m_Line.toString();
        }

        @Override
        public String getCurrentKey() {
            return m_Key;
        }

        @Override
        public String getCurrentValue() {
            return m_Value;
        }

        /**
         * Get the progress within the split
         */
        public float getProgress() {
            long totalBytes = m_End - m_Start;
            long totalhandled = m_Current - m_Start;
            return ((float) totalhandled) / totalBytes;
        }

        public synchronized void close() throws IOException {
            if (m_Input != null) {
                m_Input.close();
                m_FileIn = null;
            }
        }
    }
}
