package org.systemsbiology.xtandem.peptide;

/**
 * org.systemsbiology.xtandem.peptide.IProtein
 * User: Steve
 * Date: Apr 5, 2011
 */
public interface IProtein extends IPolypeptide, Comparable<IPolypeptide> {
    public static final IProtein[] EMPTY_ARRAY = {};

    /**
     * convert position to id
     * @param start
     * @param length
     * @return
     */
     public String getSequenceId(int start,int length);

    @Override
     public String getId();

    // public int getUUid();

   
    /**
     * source file
     * @return
     */
     public String getURL();

     public String getAnnotation();

     public double getExpectationFactor();
}
