package org.systemsbiology.xtandem.ionization;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

/**
 * org.systemsbiology.xtandem.ionization.ITheoreticalSpectrum
 *
 * @author Steve Lewis
 * @date Jan 13, 2011
 */
public interface ITheoreticalSpectrum  extends ISpectrum
{
    public static ITheoreticalSpectrum[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ITheoreticalSpectrum.class;



    /**
     * weak test for equality
     * @param test !null test
     * @return  true if equivalent
     */
    public boolean equivalent(ITheoreticalSpectrum test);

    /**
     * return the assumed charge
     * @return
     */
    public int getCharge();


    /**
     * return the generating peptide before fragmentation
     * @return
     */
    public IPolypeptide getPeptide();

    /**
     * theoretical spectra have more and different information in their peaks
     * @return
     */
    public ITheoreticalPeak[] getTheoreticalPeaks();

    /**
     * get the set of all spectra at any charge from this peptide
     * @return  !null set
     */
    public ITheoreticalSpectrumSet getSpectrumSet();


    /**
     * if the spectrum is not immutable build an immutable version
     * Otherwise return this
     * @return as above
     */
    public ITheoreticalSpectrum asImmutable();

    /**
     * if the spectrum is not  mutable build an  mutable version
     * Otherwise return this
     * @return as above
     */
    public MutableScoringSpectrum asMutable();


}
