package com.lordjoe.testing;

import org.junit.*;

/**
 * com.lordjoe.testing.MemoryTrackerTests
 * User: Steve
 * Date: 7/2/2015
 */
public class MemoryTrackerTests {

    public static final int ADDED_ALLOC_MEMORY = 2000; // enough to make up for overhead

    /**
     * make sure  MemoryTracker.threadAllocatedBytes tracks allocated memory
     * @throws Exception
     */
    @Test
    public void testMemoryTrack() throws Exception {
        long start = MemoryTracker.usedBytes();

        int allocSize = 1000000;
        int[] allocated1 = new int[allocSize];
        long used1 = MemoryTracker.usedBytes();

        long delMemory = used1 - start;
        int expectedDel = 4 * allocSize;
        Assert.assertTrue(delMemory > expectedDel);
        Assert.assertTrue(delMemory < ADDED_ALLOC_MEMORY + expectedDel);

        allocSize = 100000000;
        int[] allocated2 = new int[allocSize];
        long used2 = MemoryTracker.usedBytes();

       expectedDel = 4 * allocSize;
        long delMemory2 = used2 - used1;
        Assert.assertTrue(delMemory2 > expectedDel);
        Assert.assertTrue(delMemory2 < ADDED_ALLOC_MEMORY + expectedDel);

    }


}
