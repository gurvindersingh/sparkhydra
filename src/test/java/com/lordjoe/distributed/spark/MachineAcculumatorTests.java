package com.lordjoe.distributed.spark;

import com.lordjoe.distributed.*;
import org.junit.*;

/**
 * com.lordjoe.distributed.spark.MachineAcculumatorTests
 * User: Steve
 * Date: 11/24/2014
 */
public class MachineAcculumatorTests {

    public static final long NUMBER_ENTRIES = 10000;
    public static final long TIME_NAMO_SEC = 1000;

    @Test
    public void testMachineAccumulator() {
        String name = SparkUtilities.getMacAddress();

        MachineUseAccumulator acc = new MachineUseAccumulator();
        MachineUseAccumulator acc2 = new MachineUseAccumulator();
        MachineUseAccumulator acc3 = new MachineUseAccumulator();
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

        acc.addAll(acc2);
        Assert.assertEquals(2 * NUMBER_ENTRIES, acc.getTotalCalls());
        Assert.assertEquals(expected, acc2.getTotalTime());
        Assert.assertEquals(1, acc.size());
        Assert.assertEquals(2 * NUMBER_ENTRIES, acc.get(name));

        System.out.println(acc);
    }


}
