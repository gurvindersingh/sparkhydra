package org.systemsbiology.xtandem.ionization;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.ionization.TheoreticalSpectrumSet
 *
 * @author Steve Lewis
 * @date Feb 21, 2011
 */
public class TheoreticalSpectrumSet  implements ITheoreticalSpectrumSet
{

    private final int m_MaxCharge;
    private final double m_MassPlusH;
    private final IPolypeptide m_Peptide;
    private final ITheoreticalSpectrum[] m_Spectra;

    public TheoreticalSpectrumSet(int pMaxCharge,double mPlusH, IPolypeptide pPeptide)
    {
        m_MaxCharge = pMaxCharge;
        m_Peptide = pPeptide;
        m_MassPlusH = mPlusH;
        m_Spectra = new ITheoreticalSpectrum[pMaxCharge + 1];
    }

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(ITheoreticalSpectrumSet test)
    {
        if(test == this)
               return true;
        if(getMaxCharge() != test.getMaxCharge())
             return false;
        if(XTandemUtilities.equivalentDouble(getMassPlusH(),test.getMassPlusH()))
             return false;
        if(getPeptide().equivalent(test.getPeptide()) )
             return false;
        final ITheoreticalSpectrum[] s1 = getSpectra();
        final ITheoreticalSpectrum[] s2 = test.getSpectra();
        if(s1.length != s2.length)
               return false;
        for (int i = 0; i < s2.length; i++) {
            ITheoreticalSpectrum st1 = s1[i];
            ITheoreticalSpectrum st2 = s2[i];
            if(st1.equivalent(st2) )
                 return false;
            
        }
           return true;
    }

    /**
     * not sure how we know this but it is mass of OH + 2 * mass proton + fragment
     * @return
     */    
    public double getMassPlusH()
    {
        return m_MassPlusH;
    }

    /**
     * return the assumed charge
     *
     * @return
     */
    @Override
    public int getMaxCharge()
    {
          return m_MaxCharge;
    }

    /**
     * return the generating peptide before fragmentation
     *
     * @return
     */
    @Override
    public IPolypeptide getPeptide()
    {
          return m_Peptide;
    }

    /**
     * return the spectgrum for a specific charge
     *
     * @param charge
     * @return as above !null if charge <= max charge >= 1
     */
    @Override
    public ITheoreticalSpectrum getSpectrum(int charge)
    {
        if(charge == 0 || charge >= m_Spectra.length)
                throw new IllegalArgumentException("bad charge " + charge);
          return m_Spectra[charge];
    }

    /**
     * insert a new spectrum
     * @param !null spectrum
     */
    public void setSpectrum( ITheoreticalSpectrum spectrum)
    {
        final int charge = spectrum.getCharge();
        if(charge == 0 || charge >= m_Spectra.length)
                throw new IllegalArgumentException("bad charge " + charge);
        m_Spectra[charge] = spectrum;
    }

    /**
     * return all spectra
     *
     * @return !null !empty array
     */
    @Override
    public ITheoreticalSpectrum[] getSpectra()
    {
        List<ITheoreticalSpectrum> holder = new ArrayList<ITheoreticalSpectrum>();
        for (int i = 0; i < m_Spectra.length; i++) {
            final ITheoreticalSpectrum spectrum = m_Spectra[i];
            if(spectrum == null)
                continue;
            holder.add(spectrum);
        }
        ITheoreticalSpectrum[] ret = new ITheoreticalSpectrum[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    @Override
    public String toString() {
        return getPeptide().toString();    //To change body of overridden methods use File | Settings | File Templates.
    }
}
