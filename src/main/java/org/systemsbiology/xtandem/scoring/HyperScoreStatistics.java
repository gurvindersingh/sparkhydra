package org.systemsbiology.xtandem.scoring;


import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.HyperScoreStatistics
 * User: steven
 * Date: 1/31/11
 */
public class HyperScoreStatistics implements Serializable {
    public static final HyperScoreStatistics[] EMPTY_ARRAY = {};
    public static final double ALLOWED_ERROR = 0.0000001;

    /*
    * use default values if the statistics in the survival function is too meager
    */
    public static final int MINIMUM_SAMPLES_FOR_DISTRIBUTION = 200;

    public static final int MAX_INTEGER_HYPER = (int) Math.log10((double) Long.MAX_VALUE);
    public static final int MIN_INTEGER_HYPER = (int) Math.log10((double) Long.MIN_VALUE);

    public static final int MINIMUM_COMPUTABLE_SIZE = 8;
    // expected when we cannot compute it
    public static final double DEFAULT_EXPECTED_VALUE = 1.0;


    public static int[] buildModified(int[] original) {
        int[] ret = new int[original.length];
        int adjust = original[original.length - 1];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = original[i] - adjust;
        }
        ret[original.length - 1] = adjust;
        return ret;
    }


    private final List<Integer> m_Bins = new ArrayList<Integer>();
    private int m_Count;
    private VariableStatisticsXY m_Statistics;

    public HyperScoreStatistics() {
    }

    /**
     * build the object from the toString representation
     * - this can be useful in serialization
     *
     * @param s count,sum,sumsquare
     */
    public HyperScoreStatistics(String s) {
        this();
        setFromString(s);

    }

    /**
     * do we have any data
     *
     * @return
     */
    public boolean isEmpty() {
        return getCount() == 0;
    }

    public int[] getModified() {
        Integer[] original = getValues();
        int[] sums = new int[original.length];
        int total = 0;
        for (int i = original.length - 1; i >= 0; i--) {
            total += original[i];
            sums[i] = total;
        }

        int[] ret = new int[original.length];
        int adjust = sums[original.length - 1];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = sums[i] - adjust;
        }
        ret[original.length - 1] = adjust;
        return ret;

    }

    public double getExpectedValue(double bestScore) {
       double slope = getSlope();
        double intercept = getYIntercept();
        double ret = Math.pow(10.0,  (intercept + slope * fromRealScore(bestScore)));
        return ret;

    }

    protected int survivalFunction(int[] sumItems) {
        int pos = sumItems[0] / 5;
        for (int i = 0; i < sumItems.length; i++) {
            int sumItem = sumItems[i];
            if (sumItem < pos)
                return i;
        }
        return sumItems.length - 1;
    }

//
//    protected int[] getBins() {
//        double max = 0;
//        Integer[] values = getValues();
//        for (int i = 0; i < values.length; i++) {
//            max = Math.max(max, values[i]);
//        }
//        double factor = XTandemUtilities.getKScoreBinningFactor();
//        int number = 1 + fromScore((float) max);
//        int[] ret = new int[number];
//
//        for (int i = 0; i < values.length; i++) {
//            int index = fromScore(values[i]);
//            ret[index]++;
//        }
//
//        return ret;
//    }
//

    /**
     * add all scored >= i
     *
     * @param bins
     * @return
     */
    protected int[] accumulateBins(int[] bins) {
        int[] ret = new int[bins.length];
        int sum = 0;
        for (int i = bins.length - 1; i >= 0; i--) {
            sum += bins[i];
            ret[i] = sum;
        }
        return ret;
    }

    protected static int fromScore(double score) {
        return (int) (fromRealScore(score) + 0.5);
    }

    protected static double fromRealScore(double score) {
        return XTandemUtilities.getKScoreBinningFactor() * score;
    }

//    public double[] getTopSurvivingScores() {
//        Integer[] values = getValues();
//        Arrays.sort(values);
//        // place values in integer bins
//        int top = (int) (0.5 + values[values.length - 1]);
//        int[] bins = new int[top + 1];
//        for (int i = 0; i < values.length; i++) {
//            int index = (int) (0.5 + values[i]);
//            bins[index]++;
//        }
//
//        double sumNotTop = 0;
//        for (int i = 1; i < values.length; i++) {
//            sumNotTop += values[i];
//
//        }
//        double cutoff = sumNotTop / 5;
//        List<Double> holder = new ArrayList<Double>();
//        for (int i = 1; i < values.length; i++) {
//            double value = values[i];
//            if (value < cutoff)
//                break;
//            holder.add(value);
//        }
//
//        Double[] retained = new Double[holder.size()];
//        holder.toArray(retained);
//        double[] ret = new double[retained.length];
//        for (int i = 0; i < retained.length; i++) {
//            ret[i] = retained[i];
//
//        }
//        return ret;
//    }

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    public void serializeAsString(final IXMLAppender adder) {
        //      StringBuilder sb = new StringBuilder();
        //     sb.append("<HyperScoreStatistics>/n");
        Integer[] values = getValues();
        for (int i = 0; i < values.length; i++) {
            if (i > 0)
                adder.appendText(",");
            adder.appendText(values[i].toString());
        }
    }

    /**
     * String is packed floats
     *
     * @param s
     */
    public void setFromString(String s) {
        s = s.trim();
        clear();
        String[] items = s.split(",");
        for (int i = 0; i < items.length; i++) {
            int item = Integer.parseInt(items[i]);
            incrementBin(i, item);
        }
    }

    /**
     * combining constructor
     *
     * @param start  first statistics to combine
     * @param others any other syayistics to combine
     */
    public HyperScoreStatistics(HyperScoreStatistics start, HyperScoreStatistics... others) {
        this();
        clear();
        add(start);
        for (int i = 0; i < others.length; i++) {
            HyperScoreStatistics other = others[i];
            add(other);

        }
    }


    /**
     * remove all data
     */
    public void clear() {
        m_Bins.clear();
        m_Count = 0;
        m_Statistics = null;
    }

    /**
     * add one data value
     *
     * @param pOther
     */
    public void add(final double pOther) {
        if (pOther < 2)
            XTandemUtilities.breakHere();
        if (pOther > 918 && pOther < 919)
            XTandemUtilities.breakHere();

        int bin = fromScore(pOther);
        incrementBin(bin, 1);
    }

    public void incrementBin(int bin, int added) {
        if(bin < 0)
            return;    // we need to deal with negative scores todo kill them at an earlier stage
        while (m_Bins.size() < bin + 1)
            m_Bins.add(0);
        m_Bins.set(bin, m_Bins.get(bin) + added);
        incrementCount(added);
        m_Statistics = null;  // reset until needed

    }

    /**
     * add data from another statistics object
     *
     * @param pOther
     */
    public void add(final HyperScoreStatistics pOther) {
        Integer[] bins = pOther.getValues();
        for (int i = 0; i < bins.length; i++) {
            int added = bins[i];
            incrementBin(i, added);

        }
    }

    /**
     * total number items
     *
     * @return
     */
    public int getCount() {
        return m_Count;
    }

    public void incrementCount(int added) {
        m_Count += added;
    }

    public void incrementCount() {
        incrementCount(1);
    }

    protected Integer[] getValues() {
        return m_Bins.toArray(new Integer[0]);
    }

    protected VariableStatisticsXY getStatistics() {
        if (m_Statistics == null) {
            m_Statistics = buildVariableStatistic();
        }
        return m_Statistics;
    }


    protected static int toIndex(double hyper) {
        if (hyper < 1)
            return 0;
        return (int) Math.log10(hyper);
    }

    public static final int MAX_COUNT = 200;
    public static final double DEFAULT_SLOPE = -0.18;
    public static final double DEFAULT_Y_INTERCEPT = 3.5;

    public double getSlope() {
        int n = getCount();
        if (n < MAX_COUNT)
            return DEFAULT_SLOPE;
        return getStatistics().getSlope();
    }

    public double getYIntercept() {
        int n = getCount();
        if (n < MAX_COUNT)
            return DEFAULT_Y_INTERCEPT;
        return getStatistics().getYIntercept();
    }


    public VariableStatisticsXY buildVariableStatistic() {
        int[] modified = getModified();
        int lMaxLimit = (int) (0.5 + modified[0] / 2.0);
        int lMinLimit = 10;
        int start = 0;
        List<Double> xHolder = new ArrayList<Double>();
        List<Double> yHolder = new ArrayList<Double>();
        for (int i = 0; i < modified.length; i++) {
            int value = modified[i];
            if (value > lMaxLimit)
                continue;
            if (value < lMinLimit)
                continue;
            xHolder.add((double) i);
            final double yValue = Math.log10(value);
            yHolder.add(yValue);
        }
        VariableStatisticsXY vs = new VariableStatisticsXY(xHolder, yHolder);
        return vs;
    }

    /**
     * string is count,sum,sumsquares - allowing serialization
     * see string constructor
     *
     * @return
     */
    @Override
    public String toString() {
        return super.toString();
    }


    /**
     * test for equivalence
     *
     * @param other
     * @return
     */
    public boolean equivalent(VariableStatistics other) {
        return equivalent(other, ALLOWED_ERROR);
    }

    /**
     * test for equivalence
     *
     * @param other
     * @return
     */
    public boolean equivalent(VariableStatistics other, double allowedError) {
        if (getCount() != other.getCount())
            return false;
        if (true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        return true;
    }


}
