package com.lordjoe.distributed.hadoop;

/**
 * User: steven
 * Date: 3/7/11
 */

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.io.compress.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.systemsbiology.hadoop.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.hadoop.XMLTagInputFormat
 * Splitter that reads scan tags from a MzXML file
 * No assumption is made about lines but tage and m_End tags MUST look like <MyTag </MyTag> with no embedded spaces
 */
public class XMLTagInputFormat extends FileInputFormat<String, String> {
    public static final XMLTagInputFormat[] EMPTY_ARRAY = {};


    private static final double SPLIT_SLOP = 1.1;   // 10% slop


    public static final int BUFFER_SIZE = 4096;

    private final String m_BaseTag;
    private final String m_StartTag;
    private final String m_EndTag;
    private String m_Extension;

    public XMLTagInputFormat(final String pBaseTag) {
        m_BaseTag = pBaseTag;
        m_StartTag = "<" + pBaseTag;
        m_EndTag = "</" + pBaseTag + ">";

    }

    public String getExtension() {
        return m_Extension;
    }

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

    protected boolean isPathAcceptable(final Path pPath1) {
        String path = pPath1.toString().toLowerCase();
        if (path.startsWith("part-r-"))
            return true;
        String extension = getExtension();
        if (extension != null && path.endsWith(extension.toLowerCase()))
            return true;
        if (extension != null && path.endsWith(extension.toLowerCase() + ".gz"))
            return true;
        //noinspection SimplifiableIfStatement,RedundantIfStatement
        if (extension == null)
            return true;
        return false;
    }

    public String getStartTag() {
        return m_StartTag;
    }

    public String getBaseTag() {
        return m_BaseTag;
    }

    public String getEndTag() {
        return m_EndTag;
    }

    @Override
    public RecordReader<String, String> createRecordReader(InputSplit split,
                                                       TaskAttemptContext context) {
        if (isSplitReadable(split))
            return new MyXMLFileReader();
        else
            return NullRecordReader.INSTANCE; // do not read
    }

    @Override
    protected boolean isSplitable(JobContext context, Path file) {
        String fname = file.getName().toLowerCase();
        //noinspection SimplifiableIfStatementf,RedundantIfStatement
        if (fname.endsWith(".gz"))
            return false;
        return true;
    }


    /**
     * List input directories.
     * Subclasses may override to, e.g., select only files matching a regular
     * expression.
     *
     * @param job the job to list input paths for
     * @return array of FileStatus objects
     * @throws IOException if zero items.
     */
    protected List<FileStatus> listStatusx(JobContext job
    ) throws IOException {
        List<FileStatus> result = new ArrayList<FileStatus>();
        Path[] xdirs = getInputPaths(job);
        Path[] dirs = new Path[xdirs.length];
        for (int i = 0; i < xdirs.length; i++) {
            Path dir = xdirs[i];
            String str = dir.toString();
            if(str.startsWith("file:/"))
                dirs[i] = new Path(str.substring("file:/".length()));
            else
               dirs[i] = dir;
        }

        if (dirs.length == 0) {
            throw new IOException("No input paths specified in job");
        }

        List<IOException> errors = new ArrayList<IOException>();

        // creates a MultiPathFilter with the hiddenFileFilter and the
        // user provided one (if any).
        List<PathFilter> filters = new ArrayList<PathFilter>();
        PathFilter jobFilter = getInputPathFilter(job);
        if (jobFilter != null) {
            filters.add(jobFilter);
        }
        PathFilter inputFilter = new MultiPathFilter(filters);

        for (int i = 0; i < dirs.length; ++i) {
            Path p = dirs[i];
            FileSystem fs = p.getFileSystem(job.getConfiguration());
            FileStatus[] matches = fs.globStatus(p, inputFilter);
            if (matches == null) {
                errors.add(new IOException("Input path does not exist: " + p));
            }
            else if (matches.length == 0) {
                errors.add(new IOException("Input Pattern " + p + " matches 0 files"));
            }
            else {
                for (FileStatus globStat : matches) {
                    if (globStat.isDir()) {
                        Collections.addAll(result, fs.listStatus(globStat.getPath(),
                                inputFilter));
                    }
                    else {
                        result.add(globStat);
                    }
                }
            }
        }

        if (!errors.isEmpty()) {
            throw new InvalidInputException(errors);
        }
  //      LOG.info("Total input paths to process : " + result.size());
        return result;
    }

    /**
     * Proxy PathFilter that accepts a path only if all filters given in the
     * constructor do. Used by the listPaths() to apply the built-in
     * hiddenFileFilter together with a user provided one (if any).
     */
    public static class MultiPathFilter implements PathFilter {
        private List<PathFilter> filters;

        public MultiPathFilter(List<PathFilter> filters) {
            this.filters = filters;
        }

        public boolean accept(Path path) {
            for (PathFilter filter : filters) {
                if (!filter.accept(path)) {
                    return false;
                }
            }
            return true;
        }
    }


    /**
     * Generate the list of files and make them into FileSplits.
     * This needs to be copied to insert a filter on acceptable data
     */
    @Override
    public List<InputSplit> getSplits(JobContext job) throws IOException {
        long minSize = Math.max(getFormatMinSplitSize(), getMinSplitSize(job));
        long maxSize = getMaxSplitSize(job);
        long desiredMappers = job.getConfiguration().getLong("org.systemsbiology.jxtandem.DesiredXMLInputMappers", 0);

        // generate splits
        List<InputSplit> splits = new ArrayList<InputSplit>();
        List<FileStatus> fileStatuses = listStatus(job);
        boolean forceNumberMappers = fileStatuses.size() == 1;
        for (FileStatus file : fileStatuses) {
            Path path = file.getPath();
            if (!isPathAcceptable(path))   // filter acceptable data
                continue;
            FileSystem fs = path.getFileSystem(job.getConfiguration());
            long length = file.getLen();
            BlockLocation[] blkLocations = fs.getFileBlockLocations(file, 0, length);
            if ((length != 0) && isSplitable(job, path)) {
                long blockSize = file.getBlockSize();
                // use desired mappers to force more splits
                if (forceNumberMappers && desiredMappers > 0)
                    maxSize = Math.min(maxSize, (length / desiredMappers));

                long splitSize = computeSplitSize(blockSize, minSize, maxSize);

                long bytesRemaining = length;
                while (((double) bytesRemaining) / splitSize > SPLIT_SLOP) {
                    int blkIndex = getBlockIndex(blkLocations, length - bytesRemaining);
                    splits.add(new FileSplit(path, length - bytesRemaining, splitSize,
                            blkLocations[blkIndex].getHosts()));
                    bytesRemaining -= splitSize;
                }

                if (bytesRemaining != 0) {
                    splits.add(new FileSplit(path, length - bytesRemaining, bytesRemaining,
                            blkLocations[blkLocations.length - 1].getHosts()));
                }
            }
            else if (length != 0) {
                splits.add(new FileSplit(path, 0, length, blkLocations[0].getHosts()));
            }
            else {
                //Create empty hosts array for zero length files
                splits.add(new FileSplit(path, 0, length, new String[0]));
            }
        }
   //     LOG.debug("Total # of splits: " + splits.size());
        HadoopUtilities.validateSplits(splits);
        return splits;
    }

    /**
     * Custom RecordReader which returns the entire file as a
     * single m_Value with the name as a m_Key
     * Value is the entire file
     * Key is the file name
     */
    public class MyXMLFileReader extends RecordReader<String, String> {

        private CompressionCodecFactory compressionCodecs = null;
        private long m_Start;
        private long m_End;
        private long m_Current;
        private BufferedReader m_Input;
        private String m_Key;
        private String m_Value = null;
        private char[] m_Buffer = new char[BUFFER_SIZE];
        StringBuilder m_Sb = new StringBuilder();

        public void initialize(InputSplit genericSplit,
                               TaskAttemptContext context) throws IOException {
            FileSplit split = (FileSplit) genericSplit;
            Configuration job = context.getConfiguration();
            m_Sb.setLength(0);
            m_Start = split.getStart();
            m_End = m_Start + split.getLength();
            final Path file = split.getPath();
            compressionCodecs = new CompressionCodecFactory(job);
            final CompressionCodec codec = compressionCodecs.getCodec(file);

            // open the file and seek to the m_Start of the split
            FileSystem fs = file.getFileSystem(job);
            //  getFileStatus fileStatus = fs.getFileStatus(split.getPath());
            //noinspection deprecation
            @SuppressWarnings(value = "deprecated")
             long length = fs.getLength(file);
            FSDataInputStream fileIn = fs.open(split.getPath());
            if(m_Start > 0)
                   fileIn.seek(m_Start);
            if (codec != null) {
                CompressionInputStream inputStream = codec.createInputStream(fileIn);
                m_Input = new BufferedReader(new InputStreamReader(inputStream));
                m_End = length;
            }
            else {
                m_Input = new BufferedReader(new InputStreamReader(fileIn));
              }
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
            if (readFromCurrentBuffer())
                return true;
            int newSize = 0;
            String startTag = getStartTag() + " ";
            String startTag2 = getStartTag() + ">";
            if (m_Current > m_End) {  // we are the the end of the split
                m_Key = null;
                m_Value = null;
                return false;
            }

            newSize = m_Input.read(m_Buffer);

            while (newSize > 0) {
                m_Current += newSize;
                m_Sb.append(m_Buffer, 0, newSize);
                if (readFromCurrentBuffer())
                    return true;
                if (m_Current > m_End) {  // we are the the end of the split
                    m_Key = null;
                    m_Value = null;
                    return false;
                }

                newSize = m_Input.read(m_Buffer);
            }
            // exit because we are at the m_End
            if (newSize <= 0) {
                m_Key = null;
                m_Value = null;
                return false;
            }
            if (m_Current > m_End) {  // we are the the end of the split
                m_Key = null;
                m_Value = null;
                return false;
            }


            return true;
        }

        protected boolean readFromCurrentBuffer() {
            String endTag = getEndTag();
            String startText = m_Sb.toString();
            if (!startText.contains(endTag))
                return false; // need more read
            String startTag = getStartTag() + " ";
            String startTag2 = getStartTag() + ">";
            int index = startText.indexOf(startTag);
            if (index == -1)
                index = startText.indexOf(startTag2);
            if (index == -1)
                return false;
            startText = startText.substring(index);
            m_Sb.setLength(0);
            m_Sb.append(startText);

            String s = startText;
            index = s.indexOf(endTag);
            if (index == -1)
                return false; // need more read
            // throw new IllegalStateException("unmatched tag " + getBaseTag());
            index += endTag.length();
            String tag = s.substring(0, index).trim();
            m_Value = tag;

            // keep the remaining text to add to the next tag
            m_Sb.setLength(0);
            String rest = s.substring(index);
            m_Sb.append(rest);
            return true;
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
            }
        }
    }
}
