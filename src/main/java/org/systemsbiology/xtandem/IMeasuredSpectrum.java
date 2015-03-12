package org.systemsbiology.xtandem;

import org.systemsbiology.sax.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.IMeasuredSpectrum
 *   A spedctrum used for scoring - characteristics of the
 * run are in alother object
 * @author Steve Lewis
  */
public interface IMeasuredSpectrum extends ISpectrum
{

    public static Comparator<IMeasuredSpectrum> COMPARE_BY_ID = new IMeasuredSpectrumComparator();
    /**
      * make a form suitable to
      * 1) reconstruct the original given access to starting conditions
      *
      * @param adder !null where to put the data
      */
     public void serializeAsString(IXMLAppender adder);

    /**
     * weak test for equality
     * @param test !null test
     * @return  true if equivalent
     */
    public boolean equivalent(IMeasuredSpectrum test);
    
    /**
     * return true if the spectrum is immutable
     * @return
     */
    public boolean isImmutable();

    /**
     * if the spectrum is not immutable build an immutable version
     * Otherwise return this
     * @return as above
     */
    public IMeasuredSpectrum asImmutable();

    /**
     * if the spectrum is not  mutable build an  mutable version
     * Otherwise return this
     * @return as above
     */
    public MutableMeasuredSpectrum asMmutable();


    /**
     * get the charge of the spectrum precursor
     * @return   as above
     */
    public int getPrecursorCharge();

    /**
     * get the mass of the spectrum precursor
     * @return  as above
     */
    public double getPrecursorMass();

    /**
     * get the mz of the spectrum precursor
     * @return  as above
     */
    public double getPrecursorMassChargeRatio();



    /**
     * get run identifier
     * @return  as above
     */
    public String getId();

    /**
     * get index - return 0 if unknown
     * @return  as above
     */
    public int getIndex();

    /**
     * Mass spec characteristics
     * @return  as above
     */
    public  ISpectralScan getScanData();

    public static class IMeasuredSpectrumComparator implements Comparator<IMeasuredSpectrum> ,Serializable {
        @Override
             public int compare(final IMeasuredSpectrum o1, final IMeasuredSpectrum o2) {
                 return o1.getId().compareTo(o2.getId());
             }

    }
}
