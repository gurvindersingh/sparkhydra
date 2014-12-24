package org.systemsbiology.xtandem.ionization;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

/**
 * org.systemsbiology.xtandem.ionization.ITheoreticalPeak
 *
 * @author Steve Lewis
 * @date Jan 13, 2011
 */
public interface ITheoreticalPeak extends ISpectrumPeak
{
    public static ITheoreticalPeak[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ITheoreticalPeak.class;


    /**
     * we always know the generating peptide of a theoretical peak
     * @return  !null peptide
     */
     public IPolypeptide getPeptide();


    /**
      * return type A,B,C,X,Y,Z
      * @return
      */
      public IonType getType();



}
