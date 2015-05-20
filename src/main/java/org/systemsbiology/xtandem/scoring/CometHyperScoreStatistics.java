package org.systemsbiology.xtandem.scoring;


import org.systemsbiology.sax.IXMLAppender;
import org.systemsbiology.xtandem.XTandemUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * org.systemsbiology.xtandem.scoring.CometHyperScoreStatistics
 * User: steven
 * Date: 1/31/11
 */
public class CometHyperScoreStatistics implements Serializable {
    public static final double ALLOWED_ERROR = 0.0000001;

    /*
    * use default values if the statistics in the survival function is too meager
    */

    private final Map<Integer, Integer> cummulative = new HashMap<Integer, Integer>();
    private int count;
    private transient VariableStatisticsXY m_Statistics;

    public CometHyperScoreStatistics() {
    }


    /**
     * do we have any data
     *
     * @return
     */
    public boolean isEmpty() {
        return getCount() == 0;
    }


    public double getExpectedValue(double bestScore) {
        CometHyperScoreStatistics.RegressionObject reg = linearRegression();
        double b = reg.intercept + (10 * reg.slope) * bestScore;
        double ret = Math.pow(10.0, b);
        return ret;

    }


    protected static int fromScore(double score) {
        return (int) ((10 * score) + 0.5);
    }


    /**
     * combining constructor
     *
     * @param start  first statistics to combine
     * @param others any other syayistics to combine
     */
    public CometHyperScoreStatistics(CometHyperScoreStatistics start, CometHyperScoreStatistics... others) {
        this();
        clear();
        add(start);
        for (int i = 0; i < others.length; i++) {
            CometHyperScoreStatistics other = others[i];
            add(other);

        }
    }


    /**
     * remove all data
     */
    public void clear() {
        cummulative.clear();
        count = 0;
        m_Statistics = null;
    }


    public int getCount() {
        return count;
    }

    /**
     * add one data value
     *
     * @param pOther
     */
    public void add(final double pOther) {
        int bin = fromScore(pOther);
        incrementBin(bin, 1);
    }

    public static final int MAX_BIN = 10000;

    public void incrementBin(int bin, int added) {
        bin = Math.max(0, bin);
        bin = Math.min(MAX_BIN, bin);

        if (!cummulative.containsKey(bin)) {
            cummulative.put(bin, added);
        } else {
            cummulative.put(bin, cummulative.get(bin) + added);
        }
        m_Statistics = null;  // reset until needed
        count += added;

    }

    /**
     * add data from another statistics object
     *
     * @param pOther
     */
    public void add(final CometHyperScoreStatistics pOther) {
        for (Integer bin : cummulative.keySet()) {
            incrementBin(bin, cummulative.get(bin));
        }
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
//        int[] modified = getModified();
//        int lMaxLimit = (int) (0.5 + modified[0] / 2.0);
//        int lMinLimit = 10;
//        int start = 0;
//        List<Double> xHolder = new ArrayList<Double>();
//        List<Double> yHolder = new ArrayList<Double>();
//        for (int i = 0; i < modified.length; i++) {
//            int value = modified[i];
//            if (value > lMaxLimit)
//                continue;
//            if (value < lMinLimit)
//                continue;
//            xHolder.add((double) i);
//            final double yValue = Math.log10(value);
//            yHolder.add(yValue);
//        }
//        VariableStatisticsXY vs = new VariableStatisticsXY(xHolder, yHolder);
//        return vs;
        throw new UnsupportedOperationException("fix this"); // todo add code
    }


    public static class RegressionObject {
        public double slope;
        public double intercept;
        public int iMaxXcorr;
        public int iStartXcorr;
        public int iNextXcorr;

    }

    /**
     * ok totally copied from comet
      * @return
     */
    public RegressionObject linearRegression() {
        int maxBin = 0;
        for (Integer integer : cummulative.keySet()) {
            maxBin= Math.max(integer,maxBin);
        }
        int[] piHistogram = new int[maxBin + 1];
        for (Integer integer : cummulative.keySet()) {
            piHistogram[integer]  = cummulative.get(integer);
        }
        return linearRegression( piHistogram);
    }
        /**
         * ok totally copied from comet
         * @param piHistogram
         * @return
         */
    private RegressionObject linearRegression(int[] piHistogram) {
        double Sx, Sxy;      // Sum of square distances.
        double Mx, My;       // means
        double b, a;
        double SumX, SumY;   // Sum of X and Y values to calculate mean.
        RegressionObject ret = new RegressionObject();

        double dCummulative[] = new double[piHistogram.length];  // Cummulative frequency at each xcorr value.

        int i;
        int iNextCorr;    // 2nd best xcorr index
        int iMaxCorr = 0;   // max xcorr index
        int iStartCorr;
        int iNumPoints;

        // Find maximum correlation score index.
        for (i = piHistogram.length - 2; i >= 0; i--) {
            if (piHistogram[i] > 0)
                break;
        }
        iMaxCorr = i;

        iNextCorr = 0;
        for (i = 0; i < iMaxCorr; i++) {
            if (piHistogram[i] == 0) {
                // register iNextCorr if there's a histo value of 0 consecutively
                if (piHistogram[i + 1] == 0 || i + 1 == iMaxCorr) {
                    if (i > 0)
                        iNextCorr = i - 1;
                    break;
                }
            }
        }

        if (i == iMaxCorr) {
            iNextCorr = iMaxCorr;
            if (iMaxCorr > 12)
                iNextCorr = iMaxCorr - 2;
        }

        // Create cummulative distribution function from iNextCorr down, skipping the outliers.
        dCummulative[iNextCorr] = piHistogram[iNextCorr];
        for (i = iNextCorr - 1; i >= 0; i--) {
            dCummulative[i] = dCummulative[i + 1] + piHistogram[i];
            if (piHistogram[i + 1] == 0)
                dCummulative[i + 1] = 0.0;
        }

        // log10
        for (i = iNextCorr; i >= 0; i--) {
            piHistogram[i] = (int) dCummulative[i];  // First store cummulative in histogram.
            dCummulative[i] = Math.log10(dCummulative[i]);
        }

        iStartCorr = 0;
        if (iNextCorr >= 30)
             iStartCorr = (int)(iNextCorr - iNextCorr * 0.25);
        else if (iNextCorr >= 15)
            iStartCorr = (int)(iNextCorr - iNextCorr * 0.5);

        Mx = My = a = b = 0.0;

        while (iStartCorr >= 0) {
            Sx = Sxy = SumX = SumY = 0.0;
            iNumPoints = 0;

            // Calculate means.
            for (i = iStartCorr; i <= iNextCorr; i++) {
                if (piHistogram[i] > 0) {
                    SumY += (float) dCummulative[i];
                    SumX += i;
                    iNumPoints++;
                }
            }

            if (iNumPoints > 0) {
                Mx = SumX / iNumPoints;
                My = SumY / iNumPoints;
            } else
                Mx = My = 0.0;

            // Calculate sum of squares.
            for (i = iStartCorr; i <= iNextCorr; i++) {
                if (dCummulative[i] > 0) {
                    double dX;
                    double dY;

                    dX = i - Mx;
                    dY = dCummulative[i] - My;

                    Sx += dX * dX;
                    Sxy += dX * dY;
                }
            }

            if (Sx > 0)
                b = Sxy / Sx;   // slope
            else
                b = 0;

            if (b < 0.0)
                break;
            else
                iStartCorr--;
        }

        a = My - b * Mx;  // y-intercept

        ret.slope = b;
        ret.intercept = a;
        ret.iMaxXcorr = iMaxCorr;
        ret.iStartXcorr = iStartCorr;
        ret.iNextXcorr = iNextCorr;

        return ret;
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
