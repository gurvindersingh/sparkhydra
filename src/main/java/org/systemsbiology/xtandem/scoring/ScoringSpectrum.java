package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.ScoringSpectrum
 *
 * @author Steve Lewis
 * @date Jan 17, 2011
 */
public class ScoringSpectrum  implements ITheoreticalSpectrum
{
    public static ScoringSpectrum[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ScoringSpectrum.class;

    private final int m_Charge;
    private final  ITheoreticalSpectrumSet m_SpectrumSet;
    private final  ITheoreticalPeak[] m_Peaks;
    private   double m_SumIntensity;
    private   double m_MaxIntensity;

    public ScoringSpectrum(int charge,ITheoreticalSpectrumSet pPeptide, ITheoreticalPeak[] pPeaks)
    {
        m_SpectrumSet = pPeptide;
        m_Peaks = pPeaks;
        m_Charge= charge;
        if(m_SpectrumSet != null)
            m_SpectrumSet.setSpectrum(this);

        for (int i = 0; i < m_Peaks.length; i++) {
             ISpectrumPeak pk = m_Peaks[i];
             float peak = pk.getPeak();
             m_MaxIntensity = Math.max(peak, m_MaxIntensity);
             m_SumIntensity += peak;

         }

    }

    public ScoringSpectrum(ITheoreticalSpectrum copy)
    {
        m_SpectrumSet = copy.getSpectrumSet();
        m_Charge= copy.getCharge();
         ITheoreticalPeak[] peaks = copy.getTheoreticalPeaks();
        m_Peaks = new ITheoreticalPeak[peaks.length];
        for (int i = 0; i < peaks.length; i++) {
            ITheoreticalPeak peak = peaks[i];
             m_Peaks[i] = (ITheoreticalPeak)peak.asImmutable();
        }
        for (int i = 0; i < m_Peaks.length; i++) {
            ISpectrumPeak pk = m_Peaks[i];
            float peak = pk.getPeak();
            m_MaxIntensity = Math.max(peak, m_MaxIntensity);
            m_SumIntensity += peak;

        }
    }

    /**
     * if the spectrum is not immutable build an immutable version
     * Otherwise return this
     *
     * @return as above
     */
    @Override
    public ITheoreticalSpectrum asImmutable() {
        return this;
    }

    /**
     * if the spectrum is not  mutable build an  mutable version
     * Otherwise return this
     *
     * @return as above
     */
    @Override
    public MutableScoringSpectrum asMutable() {
        return new MutableScoringSpectrum(this);
    }

    /**
     * return the assumed charge
     *
     * @return
     */
    @Override
    public int getCharge()
    {
         return m_Charge;
    }

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(ITheoreticalSpectrum test)
    {
        if (!equivalent((ISpectrum)test))
            return false;
        if (!getPeptide().equivalent( test.getPeptide()))
            return false;
        if (!getSpectrumSet().equivalent( test.getSpectrumSet()))
            return false;
        return true;
    }

    /**
     * return the generating peptide before fragmentation
     *
     * @return
     */
    @Override
    public IPolypeptide getPeptide()
    {
          return  getSpectrumSet().getPeptide();
    }

    public ITheoreticalSpectrumSet getSpectrumSet()
    {
        return m_SpectrumSet;
    }

    /**
     * theoretical spectra have more and different information in their peaks
     *
     * @return
     */
    @Override
    public ITheoreticalPeak[] getTheoreticalPeaks()
    {
           return m_Peaks;
    }


    /**
     * get all peaks with non-zero intensity
     *
     * @return
     */
    @Override
    public ISpectrumPeak[] getNonZeroPeaks() {
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        ISpectrumPeak[] peaks = getPeaks();
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            if(peak.getPeak() > 0)
                holder.add(peak);
        }
        ISpectrumPeak[] ret = new ISpectrumPeak[holder.size()];
        holder.toArray(ret);
        return ret;

    }



    /**
     * spectrum - this might have been adjusted
     *
     * @return
     */
    @Override
    public ISpectrumPeak[] getPeaks()
    {
         return m_Peaks;
    }


    /**
     * get the number of peaks without returning the peaks
     * @return  as above
     */
    public int getPeaksCount()
    {
       return m_Peaks.length;
    }



    @Override
    public String toString() {

        final IPolypeptide polypeptide = getPeptide();
        if(polypeptide == null)
            return super.toString();
        if(m_Peaks == null)
            return super.toString();

        return polypeptide.toString()+ " npeaks=" +  getPeaksCount() + " charge=" + getCharge();
    }

    /**
     * return true if this and o are 'close enough'
     *
     * @param !null o
     * @return as above
     */
    @Override
    public boolean equivalent(ISpectrum  o)
    {
        if (this == o)
              return true;
         ISpectrum realO = o;
        final ISpectrumPeak[] peaks1 = getPeaks();
        final ISpectrumPeak[] peaks2 = realO.getPeaks();
        if(peaks1.length != peaks2.length)
            return false;
        for (int i = 0; i < peaks2.length; i++) {
            ISpectrumPeak p1 = peaks1[i];
            ISpectrumPeak p2 = peaks2[i];
            if(!p1.equivalent(p2))
                return false;


        }
        return true;
    }

    @Override
    public double getMaxIntensity() {
        return m_MaxIntensity;
    }

    /**
     * as stated
     *
     * @return
     */
    @Override
    public double getSumIntensity() {
        return m_SumIntensity;

    }



}
