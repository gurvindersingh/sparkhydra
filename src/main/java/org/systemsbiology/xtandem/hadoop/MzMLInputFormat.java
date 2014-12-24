package org.systemsbiology.xtandem.hadoop;

/**
 * User: steven
 * Date: 3/7/11
 */

import org.apache.hadoop.mapreduce.*;
import org.systemsbiology.hadoop.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.hadoop.MzMLInputFormat
 * Splitter that reads spectrum tags from a MzML file
 *
 */
public class MzMLInputFormat extends XMLTagInputFormat
{
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
}
