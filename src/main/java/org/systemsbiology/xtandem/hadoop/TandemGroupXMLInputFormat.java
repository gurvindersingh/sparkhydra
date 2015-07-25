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
 * org.systemsbiology.xtandem.hadoop.MzXMLInputFormat
 * Splitter that reads scan tags from a MzXML file
 */
public class TandemGroupXMLInputFormat extends XMLTagInputFormat
{
    public static final TandemGroupXMLInputFormat[] EMPTY_ARRAY = {};

    public static final String SCAN_TAG = "group";

    public TandemGroupXMLInputFormat() {
        super(SCAN_TAG);
        setExtension(".t.xml");
    }

    @Override
    public List<InputSplit> getSplits(final JobContext job) throws IOException {
        return super.getSplits(job);
    }
}
