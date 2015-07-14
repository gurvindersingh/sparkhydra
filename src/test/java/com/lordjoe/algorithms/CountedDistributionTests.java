package com.lordjoe.algorithms;

import org.junit.Assert;
import org.junit.Test;

/**
 * com.lordjoe.algorithms.CountedDistributionTests
 *
 * @author Steve Lewis
 * @date 5/28/2015
 */
public class CountedDistributionTests {
   
    @Test
    public void testdistributedCount()
    {
        CountedDistribution ds;
        for (int i = 0; i <31; i++) {
            ds =  CountedDistribution.empty();
            int test = 1 << i;
            ds.add(test) ;
            Assert.assertEquals(1, ds.getBin(i));
            ds = new CountedDistribution(test);
              Assert.assertEquals(1, ds.getBin(i));

        }

        ds = new CountedDistribution(13) ;
        Assert.assertEquals(16,ds.getMaxValue());
    }
}
