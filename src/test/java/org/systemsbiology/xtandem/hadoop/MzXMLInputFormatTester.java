package org.systemsbiology.xtandem.hadoop;

import com.lordjoe.utilities.*;
import org.junit.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.hadoop.MzXMLInputFormatTester
 * User: steven
 * Date: 3/7/11
 */
public class MzXMLInputFormatTester {
    public static final MzXMLInputFormatTester[] EMPTY_ARRAY = {};


    public static final int NUMBER_SCANS = 35;
    public static final String TEST_RESOURCE = "myoControl.mzXML";
    @SuppressWarnings("UnusedDeclaration")
    public static final String TESTGZ_RESOURCE = "myoControlgz.mzXML.gz";

    private String  buildFileName()
    {
        return UUID.randomUUID().toString() + ".mzXML";
    }
    /**
     *   test reading an mzxml file
     * @throws Exception
     */
    @Test
    public void testInputFormat()  throws Exception
    {
        String fileContents = FileUtilities.readInResource(MzXMLInputFormatTester.class, TEST_RESOURCE);
        String fileName =  buildFileName();
        File output = new File(fileName);
        try {
            FileUtilities.writeFile(fileName,fileContents);
            File test = new File(fileName);
            Assert.assertTrue(test.exists());
            List<String> holder = new ArrayList<String>();

            holder.add(MzXMLInputFormat.class.getName());
            holder.add(fileName);
            holder.add("test/output");

            String[] params = new String[holder.size()];
            holder.toArray(params);
            InputFormatTestJob.runJob(params);
            String[] records = InputFormatTestJob.getRecords();

            Assert.assertEquals(NUMBER_SCANS,records.length);

            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < records.length; i++) {
                String record = records[i].trim();
                Assert.assertTrue(record.startsWith("<scan "));
                Assert.assertTrue(record.endsWith("</scan>"));
            }
        }
        finally {
              output.delete();
        }
    }
    /**
     *   test reading an mzxml.gz file
     * @throws Exception
     */
   @Test
    public void testGZInputFormat()  throws Exception
    {
        InputStream is = MzXMLInputFormatTester.class.getResourceAsStream(TEST_RESOURCE);
        MzXMLInputFormat fmt = new MzXMLInputFormat();
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

}
