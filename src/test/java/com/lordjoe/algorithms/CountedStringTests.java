package com.lordjoe.algorithms;

import org.junit.*;

import java.util.*;

/**
 * com.lordjoe.algorithms.CountedStringTests
 * User: Steve
 * Date: 7/11/13
 */
public class CountedStringTests {


    @Test
    public void testNoDuplicates() throws Exception {
        String[] input = {"fee", "fie", "foe", "fum"};
        String[] desired = {"fee", "fie", "foe", "fum"};
        String[] testOutput = CountedString.getStringsByOccurance(Arrays.asList(input));
        Assert.assertArrayEquals(desired, testOutput);
    }

    /**
     * now there are duplicates but occurance is alphabetical
     *
     * @throws Exception
     */
    @Test
    public void testDuplicates() throws Exception {
        String[] input = {"fee", "fie", "fee", "fee", "fie", "fie", "foe", "foe", "fee", "fee", "fum"};
        String[] desired = {"fee", "fie", "foe", "fum"};
        String[] testOutput = CountedString.getStringsByOccurance(Arrays.asList(input));
        Assert.assertArrayEquals(desired, testOutput);
    }


    /**
     * now there are duplicates but occurance is not alphabetical
     *
     * @throws Exception
     */
    @Test
    public void testDuplicates2() throws Exception {
        String[] input = {"fee", "fie", "fum", "fum", "fie", "fie", "foe", "foe", "fum", "fum", "fum"};
        String[] desired = {"fum", "fie", "foe", "fee"};
        String[] testOutput = CountedString.getStringsByOccurance(Arrays.asList(input));
        Assert.assertArrayEquals(desired, testOutput);
    }
}
