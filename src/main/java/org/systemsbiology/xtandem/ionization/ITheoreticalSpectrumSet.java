package org.systemsbiology.xtandem.ionization;

import org.systemsbiology.xtandem.peptide.*;

import java.io.*;

/**
 * org.systemsbiology.xtandem.ionization.ITheoreticalSpectrumSet
 *  Collection of all spectra from the same peptide at different charge states
 * @author Steve Lewis
 * @date Feb 21, 2011
 */
public interface ITheoreticalSpectrumSet extends Serializable
{
    public static ITheoreticalSpectrumSet[] EMPTY_ARRAY = {};


    /**
     * weak test for equality
     * @param test !null test
     * @return  true if equivalent
     */
    public boolean equivalent(ITheoreticalSpectrumSet test);

    /**
     * not sure how we know this but it is mass of OH + 2 * mass proton + fragment
     * @return
     */
     public double getMassPlusH();

    /**
     * return the assumed charge
     * @return
     */
    public int getMaxCharge();


    /**
     * return the generating peptide before fragmentation
     * @return
     */
    public IPolypeptide getPeptide();


    /**
     * return the spectgrum for a specific charge
     * @param charge
     * @return as above !null if charge <= max charge >= 1
     */
    public ITheoreticalSpectrum getSpectrum(int charge);

    /**
     * insert a new spectrum
     * @param !null spectrum
     */
    public void setSpectrum( ITheoreticalSpectrum spectrum);

    /**
     * return all spectra
     * @return  !null !empty array
     */
    public ITheoreticalSpectrum[] getSpectra();

}
