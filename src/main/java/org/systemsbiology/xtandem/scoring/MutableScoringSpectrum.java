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
public class MutableScoringSpectrum implements ITheoreticalSpectrum {
    public static MutableScoringSpectrum[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MutableScoringSpectrum.class;

    private final int m_Charge;
    private final ITheoreticalSpectrumSet m_SpectrumSet;
    private ITheoreticalPeak[] m_Peaks;

    public MutableScoringSpectrum() {
        this(0, null, ITheoreticalPeak.EMPTY_ARRAY);
    }

    public MutableScoringSpectrum(int charge, ITheoreticalSpectrumSet pPeptide, ITheoreticalPeak[] pPeaks) {
        m_SpectrumSet = pPeptide;
        m_Peaks = pPeaks;
        m_Charge = charge;
        if (m_SpectrumSet != null)
            m_SpectrumSet.setSpectrum(this);
    }

    public MutableScoringSpectrum(ITheoreticalSpectrum copy) {
        m_SpectrumSet = copy.getSpectrumSet();
        m_Charge = copy.getCharge();
        ITheoreticalPeak[] peaks = copy.getTheoreticalPeaks();
        m_Peaks = new ITheoreticalPeak[peaks.length];
        for (int i = 0; i < peaks.length; i++) {
            ITheoreticalPeak peak = peaks[i];
            m_Peaks[i] = (ITheoreticalPeak) peak.asImmutable();
        }
    }


    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(ITheoreticalSpectrum test) {
        if (!equivalent((ISpectrum) test))
            return false;
        if (getSpectrumSet() != null) {
            if (getPeptide() != null) {
                if (!getPeptide().equivalent(test.getPeptide()))
                    return false;
            }
          if (!getSpectrumSet().equivalent(test.getSpectrumSet()))
            return false;
        }
        final ISpectrumPeak[] s1 = getPeaks();
        final ISpectrumPeak[] s2 = test.getPeaks();
        if (s1.length != s2.length)
            return false;
        for (int i = 0; i < s2.length; i++) {
            ISpectrumPeak st1 = s1[i];
            ISpectrumPeak st2 = s2[i];
            if (!st1.equivalent(st2))
                return false;

        }
        return true;
    }


    /**
     * return the assumed charge
     *
     * @return
     */
    @Override
    public int getCharge() {
        return m_Charge;
    }


    /**
     * return the generating peptide before fragmentation
     *
     * @return
     */
    @Override
    public IPolypeptide getPeptide() {
        return getSpectrumSet().getPeptide();
    }

    public ITheoreticalSpectrumSet getSpectrumSet() {
        return m_SpectrumSet;
    }

    /**
     * theoretical spectra have more and different information in their peaks
     *
     * @return
     */
    @Override
    public ITheoreticalPeak[] getTheoreticalPeaks() {
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



    public void setPeaks(final ITheoreticalPeak[] pPeaks) {
        m_Peaks = pPeaks;
    }

    public synchronized void addPeak(final ITheoreticalPeak added) {
        ITheoreticalPeak[] np = new ITheoreticalPeak[m_Peaks.length + 1];
        for (int i = 0; i < m_Peaks.length; i++) {
            np[i] = m_Peaks[i];
        }
        np[m_Peaks.length] = added;
        Arrays.sort(np, ScoringUtilities.PeakMassComparatorINSTANCE);
        m_Peaks = np;
    }

    /**
     * spectrum - this might have been adjusted
     *
     * @return
     */
    @Override
    public ISpectrumPeak[] getPeaks() {
        return m_Peaks;
    }


    /**
     * get the number of peaks without returning the peaks
     *
     * @return as above
     */
    public int getPeaksCount() {
        return m_Peaks.length;
    }


    /**
     * if the spectrum is not immutable build an immutable version
     * Otherwise return this
     *
     * @return as above
     */
    @Override
    public ITheoreticalSpectrum asImmutable() {
        return new ScoringSpectrum(this);
    }

    /**
     * if the spectrum is not  mutable build an  mutable version
     * Otherwise return this
     *
     * @return as above
     */
    @Override
    public MutableScoringSpectrum asMutable() {
        return this;
    }

    @Override
    public String toString() {

        final IPolypeptide polypeptide = getPeptide();
        if (polypeptide == null)
            return super.toString();
        if (m_Peaks == null)
            return super.toString();

        return polypeptide.toString() + " npeaks=" + getPeaksCount() + " charge=" + getCharge();
    }

    /**
     * return true if this and o are 'close enough'
     *
     * @param !null o
     * @return as above
     */
    @Override
    public boolean equivalent(ISpectrum o) {
        if (this == o)
            return true;
        ISpectrum realO = o;
        final ISpectrumPeak[] peaks1 = getPeaks();
        final ISpectrumPeak[] peaks2 = realO.getPeaks();
        if (peaks1.length != peaks2.length)
            return false;
        for (int i = 0; i < peaks2.length; i++) {
            ISpectrumPeak p1 = peaks1[i];
            ISpectrumPeak p2 = peaks2[i];
            if (!p1.equivalent(p2))
                return false;


        }
        return true;
    }

    /**
      * as stated
      *
      * @return
      */
     @Override
     public double getMaxIntensity() {
         ISpectrumPeak[] pks = getPeaks();
         double ret = 0;
         for (int i = 0; i < pks.length; i++) {
             ISpectrumPeak pk = pks[i];
             ret = Math.max(pk.getPeak(),ret);
         }
         return ret;
     }

     /**
      * as stated
      *
      * @return
      */
     @Override
     public double getSumIntensity() {
         ISpectrumPeak[] pks = getPeaks();
           double ret = 0;
           for (int i = 0; i < pks.length; i++) {
               ISpectrumPeak pk = pks[i];
               ret += pk.getPeak();
           }
           return ret;
     }


}
