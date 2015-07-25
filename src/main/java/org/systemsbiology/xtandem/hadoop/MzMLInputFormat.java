package org.systemsbiology.xtandem.hadoop;

/**
 * User: steven
 * Date: 3/7/11
 */

import com.lordjoe.distributed.input.*;
import org.apache.hadoop.mapreduce.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.hadoop.MzMLInputFormat
 * Splitter that reads spectrum tags from a MzML file
 *
 */
public class MzMLInputFormat extends XMLTagInputFormat
{
    public static final int MAX_MZML_TAG_LENGTH = 100000;
    public static final MzMLInputFormat[] EMPTY_ARRAY = {};

    public static final String SCAN_TAG = "spectrum";

    public MzMLInputFormat() {
        super(SCAN_TAG);
        setExtension(".mzml");
    }

    @Override
    public List<InputSplit> getSplits(final JobContext job) throws IOException {
        return super.getSplits(job);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String getStartTag() {
        return super.getStartTag();    //To change body of overridden methods use File | Settings | File Templates.
    }


    @Override
      public RecordReader<String, String> createRecordReader(final InputSplit split, final TaskAttemptContext context) {
          if (isSplitReadable(split))
             return new MyXMLFileReader(MAX_MZML_TAG_LENGTH);   // retrict max tag length
         else
             return NullRecordReader.INSTANCE; // do not read
        }

}
