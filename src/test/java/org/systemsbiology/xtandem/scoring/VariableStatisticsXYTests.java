package org.systemsbiology.xtandem.scoring;

import org.junit.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.VariableStatisticsTests
 * User: steven
 * Date: 1/31/11
 */
public class VariableStatisticsXYTests {
    public static final VariableStatisticsXYTests[] EMPTY_ARRAY = {};

    // seee http://www.sciencebuddies.org/science-fair-projects/project_data_analysis_variance_std_deviation.shtml

    public static final Random RND = new Random();
    public static final double TOLERANCE = 0.0001;
    public static final double[] DATA_SET_1 = {3, 4, 4, 5, 6, 8};
    public static final double[] DATA_SET_2 = {1, 2, 4, 5, 7, 11};
    public static final int DATA_SET_SIZE = 6;

    public static final double SLOPE = 1.6;
    public static final double YINTERCEPY = 0.5;

    public static double getYValue(double x) {
        return YINTERCEPY + SLOPE * x + RND.nextDouble() * TOLERANCE;
    }

    public static VariableStatisticsXY getVS1() {
        VariableStatisticsXY ret = new VariableStatisticsXY();
        for (int i = 0; i < DATA_SET_1.length; i++) {
            double dat = DATA_SET_1[i];
            ret.add(dat, getYValue(dat));
        }
        return ret;
    }

    public static VariableStatisticsXY getVS2() {
        VariableStatisticsXY ret = new VariableStatisticsXY();
        for (int i = 0; i < DATA_SET_2.length; i++) {
            double dat = DATA_SET_2[i];
            ret.add(dat, getYValue(dat));
        }
        return ret;
    }

    @Test
    public void testConstructors() {
        VariableStatisticsXY vs1 = new VariableStatisticsXY();
        Assert.assertEquals(vs1.getCount(), 0);
        Assert.assertEquals(vs1.getSumXY(), 0, TOLERANCE);

        VariableStatisticsXY set1 = getVS1();
        VariableStatisticsXY set2 = getVS1();
        Assert.assertTrue(set1.equivalent(set2,TOLERANCE * 10));
        Assert.assertFalse(set1.equivalent(vs1));

        Assert.assertEquals(set1.getCount(), DATA_SET_SIZE);
        double slope = set1.getSlope();
        Assert.assertEquals(slope, SLOPE, TOLERANCE);
        double yIntercept = set1.getYIntercept();
        Assert.assertEquals(yIntercept, YINTERCEPY, 10 * TOLERANCE);
        double r = set1.getCorrelation();
        Assert.assertEquals(r, 1.0, TOLERANCE);

        set2 = getVS2();
        Assert.assertEquals(set2.getCount(), DATA_SET_SIZE);
        double slope1 = set2.getSlope();
        Assert.assertEquals(slope1, SLOPE, TOLERANCE);
        double yIntercept1 = set2.getYIntercept();
        Assert.assertEquals(yIntercept1, YINTERCEPY, TOLERANCE);
        r = set2.getCorrelation();
        Assert.assertEquals(r, 1.0, TOLERANCE);


    }

    @Test
    public void testAdd() {

        VariableStatisticsXY set1 = getVS1();
        VariableStatisticsXY set2 = getVS2();

        set1.add(set2);
        Assert.assertEquals(set1.getCount(), 2 * DATA_SET_SIZE);
        Assert.assertEquals(set1.getSlope(), SLOPE, TOLERANCE);
        double yIntercept = set1.getYIntercept();
        Assert.assertEquals(yIntercept, YINTERCEPY, 10 * TOLERANCE);
        double r = set1.getCorrelation();
        Assert.assertEquals(r, 1.0, TOLERANCE);


    }

    @Test
    public void testStrings() {

        VariableStatisticsXY set1 = getVS1();
        String s = set1.toString();
        VariableStatisticsXY set2 = new VariableStatisticsXY(s);

        Assert.assertTrue(set1.equivalent(set2));

    }
}
