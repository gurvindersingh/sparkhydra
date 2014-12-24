package com.lordjoe.algorithms;

/**
 * com.lordjoe.algorithms.IBinner
 *    Interface describing  a class that divides doubles into bins
 * @author Steve Lewis
 * @date 11/05/13
 */
public interface IBinner {

    /**
     * place the value into a bin between getMinBin()   and getMaxBin()
     * values outside the range are handled as described below
     * @param value
     * @return either a valid bin number or -1 if  isOverflowBinned() is false and the
     *     data is outside the range handled
     */
    public int asBin(double value);

    /**
     * Describe the assigned bin
     * @param value
     * @return either a valid bin number or  null if  isOverflowBinned() is false and the
     *     data is outside the range handled
     */
    public String asBinString(double value);

    /**
     *
     * @param bin between
     * @return a number which when sent to asBin will return bin
     * @throws IllegalArgumentException if no such bin is possible
     */
    public double fromBin(int bin) throws IllegalArgumentException;


    /**
     * minimum value handed - values below this may be binned as -1 or
     * getMinBin() depending in isOverflowBinned()
     * @return  as above
     */
    public double getMinValue();

    /**
     * maximim value handed - values below this may be binned as -1 or
     * getMaxBin() depending in isOverflowBinned()
     * @return  as above
     */
     public double getMaxValue();


    /**
     * minimum bin value - this is almost always 0
     * @return  as above
     */
    public int getMinBin();

    /**
     * maximim bin value - bins are alway6s LESS than this
     * an array of size getMaxBin() - getMinBin() will hold all legal bins
     * @return  as above
     */
    public int getMaxBin();

    /**
      * return the total number bins  usually this is the same as getMaxBin
      * @return
      */
     public int getNumberBins();


    /**
     *  if true values outside getMinValue() .. getMaxValue() are
     *  assigned to the highest and l;owest bins - otherwist these valuies return
     *  -1
     * @return
     */
    public boolean isOverflowBinned();

    /**
     * return this binner but with bins offset by half a bin
     * @return
     */
    public IBinner offSetHalf();
}
