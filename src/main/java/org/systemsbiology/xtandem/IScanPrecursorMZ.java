package org.systemsbiology.xtandem;

import org.systemsbiology.sax.*;

import java.io.*;

/**
 * org.systemsbiology.xtandem.IScanPrecursorMZ
 * User: steven
 * Date: 6/7/11
 */
public interface IScanPrecursorMZ extends Serializable {
    public static final IScanPrecursorMZ[] EMPTY_ARRAY = {};

    /**
     * infered from the filter line if false we are given
     * @return as above
     */
    public boolean isPresumptive();

    public double getPrecursorIntensity();

   public  int getPrecursorCharge();

    public double getMassChargeRatio();

    public FragmentationMethod getMethod();

    public double getPrecursorMass();

    public double getPrecursorMass(int charge);

    /**
     * return true if a mass such as that of a throretical peak is
     * within the range to scpre
     *
     * @param mass positive testMass
     * @return as above
     */
   public boolean isMassWithinRange(double mass,int charge,IScoringAlgorithm alg);

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    public void serializeAsString(IXMLAppender adder);
}
