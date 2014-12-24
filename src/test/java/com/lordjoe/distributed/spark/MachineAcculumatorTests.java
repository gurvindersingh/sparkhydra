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

    @Test
    public void testMachineAccumulator()
    {
        String name = SparkUtilities.getMacAddress();

        MachineUseAccumulator acc = new MachineUseAccumulator();
        MachineUseAccumulator acc2 = new MachineUseAccumulator();
        MachineUseAccumulator acc3 = new MachineUseAccumulator();
        for (int i = 0; i < NUMBER_ENTRIES; i++) {
            acc.add(1);
            acc2.add(1);
            acc3.add(1);

        }

        Assert.assertEquals(NUMBER_ENTRIES,acc.getTotal());
        Assert.assertEquals(1,acc.size());
        Assert.assertEquals(NUMBER_ENTRIES,acc.get(name));

        Assert.assertEquals(NUMBER_ENTRIES,acc2.getTotal());
          Assert.assertEquals(1,acc2.size());
        Assert.assertEquals(NUMBER_ENTRIES,acc2.get(name));

        acc.addAll(acc2);
        Assert.assertEquals(2 * NUMBER_ENTRIES,acc.getTotal());
         Assert.assertEquals(1,acc.size());
         Assert.assertEquals(2 * NUMBER_ENTRIES,acc.get(name));

        System.out.println(acc);
    }




}
