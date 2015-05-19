package com.lordjoe.distributed;

import com.lordjoe.distributed.test.*;
import org.apache.spark.api.java.*;
import org.junit.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.SparkFileSaverTest
 * User: Steve
 * Date: 1/16/2015
 */
public class SparkFileSaverTest {

    public static final String GETTYSBURG_FILE_NAME = "gettysburg.txt";

    @Test
    public void testFileSaver() throws Exception {
        File file = new File(GETTYSBURG_FILE_NAME);
        file.delete();

        File fileCRC = new File("." + GETTYSBURG_FILE_NAME + ".crc");
           Assert.assertTrue(!file.exists() );
        String gettysburg = JavaBigDataWordCount.GETTYSBURG;
        String[] lines = gettysburg.split("\n");
        for (int i = 0; i < lines.length; i++) {
           lines[i] = lines[i].trim();
         }
        JavaSparkContext currentContext = SparkUtilities.getCurrentContext();

        JavaRDD<String> gLines = currentContext.parallelize(Arrays.asList(lines));

        SparkFileSaver.saveAsFile(GETTYSBURG_FILE_NAME,gLines);

        Assert.assertTrue(file.exists() );

        LineNumberReader rdr = new LineNumberReader(new FileReader(GETTYSBURG_FILE_NAME));
        String line = rdr.readLine();
        int index = 0;
        while(line != null) {
            Assert.assertEquals(lines[index++].trim(),line.trim());
            line = rdr.readLine();
        }
        rdr.close();

        fileCRC.delete();
        file.delete();
        Assert.assertTrue(!file.exists() );

    }
}
