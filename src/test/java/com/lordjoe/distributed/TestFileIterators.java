package com.lordjoe.distributed;

import com.lordjoe.distributed.util.*;
import org.junit.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.TestFileIterators
 * User: Steve
 * Date: 8/25/2014
 */
public class TestFileIterators {

    public static final String POINTS =

            "1,2\n" +
                    "3,4\n" +
                    "5,6\n" +
                    "7,8\n" +
                    "9,10\n" +
                    "11,12\n" +
                    "13,14\n";

    public static final String NUMBERED_WORDS =

            "DENYING: 1\n" +
                    "DAME: 4\n" +
                    "RECONNOITER: 1\n" +
                    "TRANSACTION: 1\n" +
                    "PETERSBURGLIKE: 1\n" +
                    "DAM: 23\n" +
                    "MIDWINTER: 2";

    public static final int NUMBER_POINTS = 7;

    public static LineNumberReader buildTestReader() {
        return new LineNumberReader(new StringReader(POINTS));
    }

    public static LineNumberReader buildTestWordNumberReader() {
        return new LineNumberReader(new StringReader(NUMBERED_WORDS));
    }

    @Test
    public void testStringIterator() {
        FileLineIterator fit = new FileLineIterator(buildTestReader());
        List<String> holder = new ArrayList<String>();
        while (fit.hasNext())
            holder.add(fit.next());
        StringTokenizer stx = new StringTokenizer(POINTS, "\n");
        Assert.assertEquals(NUMBER_POINTS, holder.size());

        for (String s : holder) {
            Assert.assertEquals(s, stx.nextToken());
        }
    }


    @Test
    public void testXYPointIterator() {
        FileObjectIterator<XYPoint> fit = new FileObjectIterator<XYPoint>(buildTestReader(), new XYPointSerializer());
        List<XYPoint> holder = new ArrayList<XYPoint>();
        while (fit.hasNext())
            holder.add(fit.next());
        StringTokenizer stx = new StringTokenizer(POINTS, "\n");
        Assert.assertEquals(NUMBER_POINTS, holder.size());
        int index = 1;
        for (XYPoint s : holder) {
            String actual = stx.nextToken();
            Assert.assertEquals(index++, s.x);
            Assert.assertEquals(index++, s.y); // test point should count
            Assert.assertEquals(s, new XYPoint(actual));
        }
    }


    @Test
    public void testWordNumberIterator() {
        FileObjectIterator<WordNumber> fit = new FileObjectIterator<WordNumber>(buildTestWordNumberReader(), new WordNumberSerializer());
        List<WordNumber> holder = new ArrayList<WordNumber>();
        while (fit.hasNext())
            holder.add(fit.next());
        StringTokenizer stx = new StringTokenizer(NUMBERED_WORDS, "\n");
        Assert.assertEquals(NUMBER_POINTS, holder.size());
        int index = 1;
        for (WordNumber s : holder) {
            String actual = stx.nextToken();
            Assert.assertTrue(actual.startsWith(s.word));
        }
    }


}
