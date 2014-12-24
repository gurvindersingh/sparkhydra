package org.systemsbiology.xtandem.scoring;

import org.junit.*;

/**
 * org.systemsbiology.xtandem.scoring.VariableStatisticsTests
 * User: steven
 * Date: 1/31/11
 */
public class VariableStatisticsTests {
    public static final VariableStatisticsTests[] EMPTY_ARRAY = {};

    // seee http://www.sciencebuddies.org/science-fair-projects/project_data_analysis_variance_std_deviation.shtml

    public static final double TOLERANCE = 0.0001;
    public static final double[] DATA_SET_1 = {3, 4, 4, 5, 6, 8};
    public static final double[] DATA_SET_2 = {1, 2, 4, 5, 7, 11};
    public static final int DATA_SET_SIZE = 6;

    public static VariableStatistics getVS1() {
        VariableStatistics ret = new VariableStatistics();
        for (int i = 0; i < DATA_SET_1.length; i++) {
            double dat = DATA_SET_1[i];
            ret.add(dat);
        }
        return ret;
    }

    public static VariableStatistics getVS2() {
        VariableStatistics ret = new VariableStatistics();
        for (int i = 0; i < DATA_SET_2.length; i++) {
            double dat = DATA_SET_2[i];
            ret.add(dat);
        }
        return ret;
    }

    @Test
    public void testConstructors() {
        VariableStatistics vs1 = new VariableStatistics();
        Assert.assertEquals(vs1.getCount(), 0);
        Assert.assertEquals(vs1.getSum(), 0, TOLERANCE);
        Assert.assertEquals(vs1.getSumSquares(), 0, TOLERANCE);

        VariableStatistics set1 = getVS1();
        VariableStatistics set2 = getVS1();
        Assert.assertTrue(set1.equivalent(set2));
        Assert.assertFalse(set1.equivalent(vs1));

        Assert.assertEquals(set1.getCount(), DATA_SET_SIZE);
        Assert.assertEquals(set1.getSum(), 30, TOLERANCE);
          Assert.assertEquals(set1.getSumSquares(), 166, TOLERANCE);
        Assert.assertEquals(set1.getMean(), 5, TOLERANCE);
        double stdev = set1.getStandardDeviation();
        Assert.assertEquals(stdev, 1.63299, TOLERANCE);

        set2 = getVS2();
        Assert.assertEquals(set2.getCount(), DATA_SET_SIZE);
        Assert.assertEquals(set2.getSum(), 30, TOLERANCE);
        Assert.assertEquals(set2.getSumSquares(), 216, TOLERANCE);
        Assert.assertEquals(set2.getMean(), 5, TOLERANCE);
          stdev = set2.getStandardDeviation();
        Assert.assertEquals(stdev, 3.3166, TOLERANCE);


    }

    @Test
    public void testAdd() {

        VariableStatistics set1 = getVS1();
        VariableStatistics set2 = getVS2();

        set1.add(set2);
        Assert.assertEquals(set1.getCount(), 2 * DATA_SET_SIZE);
        Assert.assertEquals(set1.getSum(), 60, TOLERANCE);
        Assert.assertEquals(set1.getSumSquares(),216 +   166, TOLERANCE);
        Assert.assertEquals(set1.getMean(), 5, TOLERANCE);
        double stdev = set1.getStandardDeviation();
        Assert.assertEquals(stdev, 2.61406452, TOLERANCE);



        VariableStatistics setx = new VariableStatistics (set2,set2,set2,set2);
        Assert.assertEquals(setx.getCount(), 4 * DATA_SET_SIZE);
        Assert.assertEquals(setx.getSum(), 4 * 30, TOLERANCE);
          Assert.assertEquals(setx.getSumSquares(),4 *  216, TOLERANCE);
        Assert.assertEquals(setx.getMean(), 5, TOLERANCE);
         stdev = setx.getStandardDeviation();
        Assert.assertEquals(stdev, 3.3166, TOLERANCE);

    }

    @Test
    public void testStrings() {

        VariableStatistics set1 = getVS1();
        String s = set1.toString();
        VariableStatistics set2 = new VariableStatistics(s);

        Assert.assertTrue(set1.equivalent(set2));

    }
}
