package org.systemsbiology.xtandem.scoring;

import java.io.*;

/**
 * org.systemsbiology.xtandem.scoring.VariableStatistics
 * recombinable class holding data to calculate mean and standard deviation
 * User: steven
 * Date: 1/31/11
 */
public class VariableStatistics implements Serializable {
    public static final VariableStatistics[] EMPTY_ARRAY = {};

    private int m_Count;
    private double m_Sum;
    private double m_SumSquares;

    public VariableStatistics() {
    }

    /**
     * build the object from the toString representation
     * - this can be useful in serialization
     * @param s  count,sum,sumsquare
     */
    public VariableStatistics(String s) {
        this();
        setFromString(s);

    }



    public void setFromString(final String s) {
        String[] items = s.split(",");
        m_Count = Integer.parseInt(items[0]);
        m_Sum = Double.parseDouble(items[1]);
        m_SumSquares = Double.parseDouble(items[2]);
    }

    /**
     * combining constructor
     *
     * @param start  first statistics to combine
     * @param others any other syayistics to combine
     */
    public VariableStatistics(VariableStatistics start, VariableStatistics... others) {
        this();
        m_Count = start.getCount();
        m_Sum = start.getSum();
        m_SumSquares = start.getSumSquares();
        for (int i = 0; i < others.length; i++) {
            VariableStatistics other = others[i];
            add(other);

        }
    }

    /**
     * remove all data
     */
    public void clear()
    {
        m_Count = 0;
         m_Sum = 0;
         m_SumSquares =  0;

    }

    /**
      * add one data value
      *
      * @param pOther
      */
     public void add(final double pOther) {
         m_Count++;
         m_Sum += pOther;
         m_SumSquares += pOther * pOther;
     }
    /**
      * add data from another statistics object
      *
      * @param pOther
      */
     public void add(final VariableStatistics pOther) {
         m_Count += pOther.getCount();
         m_Sum += pOther.getSum();
         m_SumSquares += pOther.getSumSquares();
     }

    /**
     * total number items
     * @return
     */
    public int getCount() {
        return m_Count;
    }

    /**
     * sum of values
     * @return as above
     */
    public double getSum() {
        return m_Sum;
    }

    /**
     * sum of values squared
     * @return as above
     */
    public double getSumSquares() {
        return m_SumSquares;
    }


    /**
      * mean or 0 if no data
      * @return as above
      */
    public double getMean() {
        int count = getCount();
        switch (count) {
            case 0:
                return 0;
             default:
                return getSum() /  count  ;
        }
    }

    /**
     * stanadrd deviation or Double.MAX_VALUE if count < 2
     * @return as above
     */
    public double getStandardDeviation() {
        int count = getCount();
        switch (count) {
            case 0:
            case 1:
                return Double.MAX_VALUE;
              default:
                  double mean = getMean();
                  double chiSquare = getSumSquares() / count - (mean * mean);
                  return Math.sqrt(chiSquare);
        }
    }

    /**
     * string is count,sum,sumsquares - allowing serialization
     * see string constructor
     * @return
     */
    @Override
    public String toString() {
        return Integer.toString(getCount()) + "," + getSum() + "," + getSumSquares();
    }

    public static final double ALLOWED_ERROR = 0.0000001;

    /**
     * test for equivalence
     * @param other
     * @return
     */
    public boolean equivalent(VariableStatistics other)
    {
          return equivalent(other,ALLOWED_ERROR);
    }

    /**
     * test for equivalence
     * @param other
     * @return
     */
    public boolean equivalent(VariableStatistics other,double allowedError)
    {
        if(getCount() != other.getCount())
            return false;
        if(getCount() == 0)
            return true;
        if(Math.abs(getSum() - other.getSum()) > getMean() * allowedError)
             return false;
        if(getCount() == 1)
             return true;
        if(Math.abs(getSumSquares() - other.getSumSquares()) > (getMean() * getMean() * allowedError))
             return false;

        return true;
     }
}
