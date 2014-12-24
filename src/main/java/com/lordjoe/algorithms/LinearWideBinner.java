package com.lordjoe.algorithms;

/**
 * com.lordjoe.algorithms.LinearWideBinner
 * User: Steve
 * Date: 6/28/13
 */
public class LinearWideBinner extends LinearBinner implements IWideBinner {

    private final int m_NumberOverlapBins;

    public LinearWideBinner(final double maxValue, final double binSize, final double minValue, final boolean overFlowBinned) {
        this(maxValue, binSize, minValue, overFlowBinned, 1);
    }

    public LinearWideBinner(final double maxValue, final double binSize, final double minValue, final boolean overFlowBinned,int numberOverlap) {
        super(maxValue, binSize, minValue, overFlowBinned);
        m_NumberOverlapBins = numberOverlap;
    }

    protected int getNumberOverlapBins() {
        return m_NumberOverlapBins;
    }

    /**
     * give a list of all bins that the value may be assigned to
     *
     * @param value value to test
     * @return !null array of bins
     */
    @Override
    public int[] asBins(final double value) {
        int mainBin = asBin(value);
        int lower = Math.max(getMinBin(), mainBin - getNumberOverlapBins());
        int upper = Math.min(getMaxBin() - 1, mainBin + getNumberOverlapBins());
        int[] ret = new int[upper - lower + 1];
        for (int i = lower; i <= upper; i++) {
            ret[i - lower] = i ;
          }
        return ret;
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
         if(true )
             throw new UnsupportedOperationException("Fix This"); // ToDo
         double v = getBinSize() / 2 ;// + getOverlapWidth();
         double minv = bnv - v;
         double maxv = bnv + v;
         StringBuilder sb = new StringBuilder();
         sb.append(formatBinValue(minv));
         sb.append("-");
         sb.append(formatBinValue(maxv));
         return sb.toString();
     }

    /**
     * Describe the assigned bins
     *
     * @param value
     * @return either a valid bin number or  null if  isOverflowBinned() is false and the
     *         data is outside the range handled
     */
    @Override
    public String[] asBinStrings(final double value) {
        int[] bins = asBins(value);
        String[] ret = new String[bins.length];
        for (int i = 0; i < ret.length; i++) {
             ret[i] = formatBin(bins[i]);

        }
        return ret;
    }

    /**
     * return this binner but with bins offset by half a bin
     *
     * @return
     */
    @Override
    public IBinner offSetHalf() {
        return new LinearWideBinner(getMaxValue(),getBinSize(),getMinValue() - getBinSize() / 2,isOverflowBinned(),getNumberOverlapBins());

    }
}
