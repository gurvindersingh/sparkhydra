package org.systemsbiology.xtandem.ionization;

import org.systemsbiology.sax.*;

import java.io.*;

/**
 * org.systemsbiology.xtandem.ionization.IonTypeScorer
 *
 * @author Steve Lewis
 * @date Feb 14, 2011
 */
public interface IonTypeScorer extends  Serializable
{
    public static IonTypeScorer[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IonTypeScorer.class;

    /**
     * weak test for equality
     * @param test !null test
     * @return  true if equivalent
     */
    public boolean equivalent(IonTypeScorer test);

    /**
       * get the total peaks matched for all ion types
       * @return
       */
      public int getNumberMatchedPeaks();


     /**
     * get the score for a given ion type
     * @param type   !null iontype
     * @return score for that type
     */
    public  double getScore(IonType type);


    /**
     * get the count for a given ion type
     * @param type   !null iontype
     * @return count for that type
     */
     public int getCount(IonType type);

    /**
      * make a form suitable to
      * 1) reconstruct the original given access to starting conditions
      *
      * @param adder !null where to put the data
      */
     public void serializeAsString(final IXMLAppender adder);


}
