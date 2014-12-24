package com.lordjoe.algorithms;

/**
 * com.lordjoe.algorithms.LinearBinner
 * implementation of IBinner as a logarithmic set of bins
 *
 * @author Steve Lewis
 * @date 11/05/13
 */
@SuppressWarnings("UnusedDeclaration")
public class LogBinner implements IBinner {
    public static LogBinner[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = LogBinner.class;

    private final double m_MinValue;
    private final double m_MaxValue;
    private final int m_MinBin;
    private final double m_BinSize;
    private final int m_NumberBins;
    private final boolean m_OverFlowBinned;
    private final double m_MinValueLog;
    @SuppressWarnings("FieldCanBeLocal")
    private final double m_MaxValueLog;
    private final double m_DelValueLog;


    @SuppressWarnings("UnusedDeclaration")
    public LogBinner(double maxValue, double minValue, int binSize) {
        this(maxValue, minValue, binSize, true, 0);
    }

    @SuppressWarnings("UnusedDeclaration")
    public LogBinner(double maxValue, double minValue, int binSize, boolean overFlowBinned) {
        this(maxValue, minValue, binSize, overFlowBinned, 0);
    }

    /**
     * cerate a binner
     *
     * @param maxValue       maximim value
     * @param minValue       minimum value
     * @param binSize        size of bin
     * @param overFlowBinned if true outside range  is binned
     * @param minBin         minimum bin value - usually 0
     */
    public LogBinner(double maxValue, double minValue, int binSize, boolean overFlowBinned, int minBin) {
        if (maxValue <= minValue)
            throw new IllegalArgumentException("bad bins");
        if (binSize <= 0)
            throw new IllegalArgumentException("bad bins");

        m_MinValue = minValue;
        m_MinValueLog = Math.log10(m_MinValue);

        m_MaxValue = maxValue;
        m_MaxValueLog = Math.log10(m_MaxValue);

        m_BinSize = binSize;

        m_OverFlowBinned = overFlowBinned;
        m_MinBin = minBin;

        m_NumberBins = (int) (maxValue - minValue + 0.5) / binSize;

        m_DelValueLog = (m_MaxValueLog - m_MinValueLog) / m_NumberBins;
    }

    /**
     * place the value into a bin between getMinBin()   and getMaxBin()
     * values outside the range are handled as described below
     *
     * @param value
     * @return either a valid bin number or -1 if  isOverflowBinned() is false and the
     *         data is outside the range handled
     */
    @Override
    public int asBin(double value) {
        if (value < getMinValue()) {
            if (isOverflowBinned())
                return getMinBin();
            else
                return -1; // out of range
        }
        if (value < getMinValue()) {
            if (isOverflowBinned())
                return getMaxBin() - 1;
            else
                return -1; // out of range
        }
        double scoreLog = Math.log10(value);
        scoreLog -= m_MinValueLog;
        //noinspection UnusedDeclaration,UnnecessaryLocalVariable
        int ret = (int) (scoreLog / m_DelValueLog);
        return ret;
    }

    /**
        * Describe the assigned bin
        *
        * @param value
        * @return either a valid bin number or  null if  isOverflowBinned() is false and the
        *         data is outside the range handled
        */
       @Override
       public String asBinString(final double value) {
           int bin = asBin(value);
           if(bin == -1)
               return null;

           return formatBin(bin);

       }

       /**
        * turn a bin into a string
        * @param pBin
        * @return
        */
       protected String formatBin(final int pBin) {
           if(pBin == -1)
               return null;
           double bnv = fromBin(pBin);
           // todo this is wrong
           double minv = bnv -  getBinSize() / 2;
           double maxv = bnv +  getBinSize() / 2;
           StringBuilder sb = new StringBuilder();
           sb.append(formatBinValue(minv));
           sb.append("-");
           sb.append(formatBinValue(maxv));
           return sb.toString();
       }

      protected String formatBinValue(double value)  {
          return String.format("%10.3f",value).trim();
      }




    @SuppressWarnings("UnusedDeclaration")
    public double getBinSize() {
        return m_BinSize;
    }

    /**
     * @param bin between
     * @return a number which when sent to asBin will return bin
     * @throws IllegalArgumentException if no such bin is possible
     */
    @Override
    public double fromBin(int bin) throws IllegalArgumentException {
         if (bin < -1)
            throw new IllegalArgumentException("Illegal bin " + bin);
        if (bin == -1) {
            if (!isOverflowBinned())
                return getMinValue() - 1;
            else
                throw new IllegalArgumentException("Illegal bin " + bin);
        }
        if (bin < getMaxBin() || bin >= getMaxBin())
            throw new IllegalArgumentException("Illegal bin " + bin);
        // return the bin midpoint
        return getMinValue() * Math.pow(10, m_DelValueLog * bin);

    }

    /**
     * minimum value handed - values below this may be binned as -1 or
     * getMinBin() depending in isOverflowBinned()
     *
     * @return as above
     */
    @Override
    public double getMinValue() {
        return m_MinValue;
    }

    /**
     * maximim value handed - values below this may be binned as -1 or
     * getMaxBin() depending in isOverflowBinned()
     *
     * @return as above
     */
    @Override
    public double getMaxValue() {
        return m_MaxValue;
    }

    /**
     * minimum bin value - this is almost always 0
     *
     * @return as above
     */
    @Override
    public int getMinBin() {
        return m_MinBin;
    }

    /**
     * maximim bin value - bins are alway6s LESS than this
     * an array of size getMaxBin() - getMinBin() will hold all legal bins
     *
     * @return as above
     */
    @Override
    public int getMaxBin() {
        return getMinBin() + getNumberBins();
    }

    /**
     * if true values outside getMinValue() .. getMaxValue() are
     * assigned to the highest and l;owest bins - otherwist these valuies return
     * -1
     *
     * @return
     */
    @Override
    public boolean isOverflowBinned() {
        return m_OverFlowBinned;
    }

    /**
     * return the total number bins
     *
     * @return
     */
    public int getNumberBins() {
        return m_NumberBins;
    }

    /**
      * return this binner but with bins offset by half a bin
      *
      * @return
      */
     @Override
     public IBinner offSetHalf() {
          throw new UnsupportedOperationException("Fix This"); // ToDo
     }


}
