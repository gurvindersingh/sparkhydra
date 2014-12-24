package org.systemsbiology.xtandem.scoring;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.VariableStatistics
 * recombinable class holding data to calculate mean and standard deviation
 * User: steven
 * Date: 1/31/11
 */
public class VariableStatisticsXY {
    public static final VariableStatisticsXY[] EMPTY_ARRAY = {};

    private final VariableStatistics m_X = new VariableStatistics();
    private final VariableStatistics m_Y = new VariableStatistics();
    private double m_SumXY;

    public VariableStatisticsXY() {
    }


    public VariableStatisticsXY(double[] xvalues,double[] yvalues) {
        for (int i = 0; i < xvalues.length; i++) {
            add(xvalues[i],yvalues[i]);
         }
    }


    public VariableStatisticsXY(List<Double> xvalues,List<Double> yvalues) {
        for (int i = 0; i < xvalues.size(); i++) {
            add(xvalues.get(i),yvalues.get(i));
         }
    }

    /**
     * build the object from the toString representation
     * - this can be useful in serialization
     *
     * @param s count,sum,sumsquare
     */
    public VariableStatisticsXY(String s) {
        this();
        String[] items = s.split(":");
        m_X.setFromString(items[0]);
        m_Y.setFromString(items[1]);
        m_SumXY = Double.parseDouble(items[2]);

    }

    /**
     * combining constructor
     *
     * @param start  first statistics to combine
     * @param others any other syayistics to combine
     */
    public VariableStatisticsXY(VariableStatisticsXY start, VariableStatisticsXY... others) {
        this();
        m_X.clear();
        m_X.add(start.m_Y);
        m_Y.clear();
        m_Y.add(start.m_Y);
        m_SumXY = start.getSumXY();
        for (int i = 0; i < others.length; i++) {
            VariableStatisticsXY other = others[i];
            add(other);

        }
    }

    /**
     * remove all data
     */
    public void clear() {
        m_X.clear();
        m_Y.clear();
        m_SumXY = 0;

    }


    /**
     * add one data value
     *
     * @param pOtherX
     */
    public void add(final double pOtherX, final double pOtherY) {
        m_X.add(pOtherX);
        m_Y.add(pOtherY);
        m_SumXY += pOtherX * pOtherY;
    }

    /**
     * add data from another statistics object
     *
     * @param pOther
     */
    public void add(final VariableStatisticsXY pOther) {
        m_X.add(pOther.m_X);
        m_Y.add(pOther.m_Y);
        m_SumXY += pOther.getSumXY();
    }

    /**
     * total number items
     *
     * @return
     */
    public int getCount() {
        return m_X.getCount();
    }

    /**
     * sum of values squared
     *
     * @return as above
     */
    public double getSumXY() {
        return m_SumXY;
    }


    /**
     * string is count,sum,sumsquares - allowing serialization
     * see string constructor
     *
     * @return
     */
    @Override
    public String toString() {
        return m_X.toString() + ":" + m_Y.toString() + ":" + getSumXY();
    }

    public static final double ALLOWED_ERROR = 0.0000001;

    /**
      * test for equivalence
      * @param other
      * @return
      */
     public boolean equivalent(VariableStatisticsXY other)
     {
           return equivalent(other,ALLOWED_ERROR);
     }

    /**
     * test for equivalence
     *
     * @param other
     * @return
     */
    public boolean equivalent(VariableStatisticsXY other,double allowedError) {
        if (getCount() != other.getCount())
            return false;
        if (getCount() == 0)
            return true;
        if (!m_X.equivalent(other.m_X,allowedError))
            return false;
        if (!m_Y.equivalent(other.m_Y,allowedError))
            return false;
        if( Math.abs(getSumXY()- other.getSumXY()) > allowedError)
              return false;
        if (getCount() == 1)
            return true;
        if( Math.abs(getCorrelation() - other.getCorrelation()) > allowedError)
            return false;
        if( Math.abs(getYIntercept()- other.getYIntercept()) > allowedError)
            return false;
          return true;
    }

    public double getSlope() {
        int n = getCount();
        if(n < 2)
            return 0;
        double sumX = m_X.getSum();
        double sumY = m_Y.getSum();
        double sumXSquared = m_X.getSumSquares();
        double ret = (getSumXY() * n - sumX * sumY) / (n * sumXSquared - sumX * sumX);
        if(Double.isInfinite(ret) || Double.isNaN(ret))
            return 0;
        return ret;
    }

    public double getYIntercept() {
        int n = getCount();
        if(n < 2)
            return 0;
        double sumX = m_X.getSum();
        double sumY = m_Y.getSum();
        double sumXSquared = m_X.getSumSquares();
        double ret = (sumY * sumXSquared - sumX * getSumXY()) / (n * sumXSquared - sumX * sumX);
        if(Double.isInfinite(ret) || Double.isNaN(ret))
              return 0;
          return ret;
    }

    public double getXIntercept() {
        double ret = -getYIntercept() / getSlope();
        return ret;
    }

    public double getCorrelation() {
        int n = getCount();
        if(n < 2)
            return 0;
        double sumX = m_X.getSum();
        double sumY = m_Y.getSum();
        double sumXSquared = m_X.getSumSquares();
        double sumYSquared = m_Y.getSumSquares();
        double denom = m_Y.getSumSquares();
        double xdenom = sumXSquared - (sumX * sumX / n);
        double ydenom = sumYSquared - (sumY * sumY / n);
        denom = xdenom * ydenom;
        double ret = (getSumXY() - (sumX * sumY / n)) / Math.sqrt(denom);
        if(Double.isInfinite(ret) || Double.isNaN(ret))
                return 0;

        return ret;
    }
//    http://www.pgccphy.net/Linreg/linreg_f90.txt
//    m=(n*sumxy-sumx*sumy)/(n*sumx2-sumx**2)!
//    compute slope
//    b=(sumy*sumx2-sumx*sumxy)/(n*sumx2-sumx**2)!
//    compute y
//    -
//    intercept
//            r = (sumxy - sumx * sumy / n) / & !compute
//    correlation coefficient
//
//    sqrt((sumx2-sumx**2/n)
//
//    *(sumy2-sumy**2/n))
//

}
