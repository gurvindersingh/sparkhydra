package com.lordjoe.distributed.spark.accumulators;

import com.lordjoe.distributed.*;
import org.junit.*;

import java.util.*;

/**
 * com.lordjoe.distributed.spark.accumulators.MachineAcculumatorTests
 * User: Steve
 * Date: 11/24/2014
 */
public class MachineAcculumatorTests {

    public static final long NUMBER_ENTRIES = 10000;
    public static final long TIME_NAMO_SEC = 1000;
    public static final Random RND = new Random();

    @Test
    public void testVariance() {
        long[] counts = new long[12];
        for (int i = 0; i < counts.length; i++) {
            long count = (long) ((RND.nextGaussian() * 1000) + 10000);
            counts[i] = count;
        }

        String s = MachineUseAccumulator.generateVariance(counts);
    }


    @Test
    public void testMachineAccumulator() {
        String name = SparkUtilities.getMacAddress();

        MachineUseAccumulator acc = MachineUseAccumulator.empty();
        MachineUseAccumulator acc2 = MachineUseAccumulator.empty();
        MachineUseAccumulator acc3 = MachineUseAccumulator.empty();
        for (int i = 0; i < NUMBER_ENTRIES; i++) {
            acc.add(1, TIME_NAMO_SEC);
            acc2.add(1, TIME_NAMO_SEC);
            acc3.add(1, TIME_NAMO_SEC);

        }

        Assert.assertEquals(NUMBER_ENTRIES, acc.getTotalCalls());
        long expected = NUMBER_ENTRIES * TIME_NAMO_SEC;
        long totalTime = acc.getTotalTime();
        Assert.assertEquals(expected, totalTime);
        Assert.assertEquals(1, acc.size());
        Assert.assertEquals(NUMBER_ENTRIES, acc.get(name));

        Assert.assertEquals(NUMBER_ENTRIES, acc2.getTotalCalls());
        Assert.assertEquals(NUMBER_ENTRIES * TIME_NAMO_SEC, acc2.getTotalTime());
        Assert.assertEquals(1, acc2.size());
        Assert.assertEquals(NUMBER_ENTRIES, acc2.get(name));

        acc.add(acc2);
        Assert.assertEquals(2 * NUMBER_ENTRIES, acc.getTotalCalls());
        Assert.assertEquals(expected, acc2.getTotalTime());
        Assert.assertEquals(1, acc.size());
        Assert.assertEquals(2 * NUMBER_ENTRIES, acc.get(name));

        System.out.println(acc);
    }


}
