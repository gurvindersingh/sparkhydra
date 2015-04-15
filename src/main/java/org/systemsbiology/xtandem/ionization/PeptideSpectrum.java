package org.systemsbiology.xtandem.ionization;

import com.lordjoe.distributed.hydra.test.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.ionization.PeptideSpectrum
 * User: steven
 * Date: Jan 10, 2011
 */
public class PeptideSpectrum implements ITheoreticalSpectrum {
    public static final PeptideSpectrum[] EMPTY_ARRAY = {};


    private final ITheoreticalSpectrumSet m_SpectrumSet;
    private final int m_Charge;
    private final IonType[] m_IonTypes;
    private PeptideIon[] m_Spectrum;
    private final SequenceUtilities m_Utilities;
    private double m_SumIntensity;
    private double m_MaxIntensity;

    public PeptideSpectrum(final ITheoreticalSpectrumSet pPeptide, int charge, final IonType[] pIonTypes, SequenceUtilities su) {
        m_SpectrumSet = pPeptide;
        m_IonTypes = pIonTypes;
        m_Utilities = su;
        m_Charge = charge;
        m_SpectrumSet.setSpectrum(this);
        PeptideIon[] spectrum = getSpectrum();

        for (int i = 0; i < spectrum.length; i++) {
            ISpectrumPeak pk = spectrum[i];
            float peak = pk.getPeak();
            m_MaxIntensity = Math.max(peak, m_MaxIntensity);
            m_SumIntensity += peak;

        }

    }

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
        if (!getPeptide().equivalent(test.getPeptide()))
            return false;
        if (!getSpectrumSet().equivalent(test.getSpectrumSet()))
            return false;
        final ISpectrumPeak[] s1 = getPeaks();
        final ISpectrumPeak[] s2 = test.getPeaks();
        if (s1.length != s2.length)
            return false;
        for (int i = 0; i < s2.length; i++) {
            ISpectrumPeak st1 = s1[i];
            ISpectrumPeak st2 = s2[i];
            if (st1.equivalent(st2))
                return false;

        }
        return true;
    }


    public SequenceUtilities getUtilities() {
        return m_Utilities;
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
        return new MutableScoringSpectrum(this);
    }

    /**
     * get the set of all spectra at any charge from this peptide
     *
     * @return !null set
     */
    @Override
    public ITheoreticalSpectrumSet getSpectrumSet() {
        return m_SpectrumSet;
    }

    //
//    /**
//     * convert to a spectrum at a different charge
//     *
//     * @param charge
//     * @param su
//     * @return
//     */
//    @Override
//    public ITheoreticalSpectrum getSpectrumAtCharge(int charge, SequenceUtilities su)
//    {
//        if(charge == 1)
//            return this;
//        IPolypeptide pp = getPeptide();
//        final ITheoreticalPeak[] spectrumPeaks = getTheoreticalPeaks();
//        ITheoreticalPeak[] peaks = buildPeaksAtCharge(charge,  su,  spectrumPeaks);
//        return new ScoringSpectrum( charge,pp,peaks);
//    }
//
//    public  ITheoreticalPeak[] buildPeaksAtCharge(int charge, SequenceUtilities su,ITheoreticalPeak[] spectrumPeaks)
//    {
//        List<ITheoreticalPeak> holder = new ArrayList<ITheoreticalPeak>();
//        for (int i = 0; i < spectrumPeaks.length; i++) {
//            ITheoreticalPeak sp = spectrumPeaks[i];
//            final IPolypeptide pp = sp.getPeptide();
//            final IonType type = sp.getType();
//            double ionMass = getMass(su,pp.getMass(),charge,type);
//            TheoreticalPeak tp = new TheoreticalPeak(ionMass, sp.getPeak(),pp, type);
//            holder.add(tp);
//        }
//        ITheoreticalPeak[] ret = new ITheoreticalPeak[holder.size()];
//        holder.toArray(ret);
//        return ret;
//    }

    public static double getMass(SequenceUtilities su, double sequenceMass, int charge, IonType type) {
        double added = su.getAddedMass(type);
        sequenceMass += added;
        // every charge adds the mass of a proton
        sequenceMass += (charge - 1) * su.getdProton();
        return sequenceMass;
    }


    public IPolypeptide getPeptide() {
        return getSpectrumSet().getPeptide();
    }

    public IonType[] getIonTypes() {
        return m_IonTypes;
    }

    public PeptideIon[] getSpectrum() {
        if (m_Spectrum == null)
            m_Spectrum = buildSpectrum();
        return m_Spectrum;

    }

    /**
     * theoretical spectra have more and different information in their peaks
     *
     * @return
     */
    @Override
    public ITheoreticalPeak[] getTheoreticalPeaks() {
        return getSpectrum();
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
            if (peak.getPeak() > 0)
                holder.add(peak);
        }
        ISpectrumPeak[] ret = new ISpectrumPeak[holder.size()];
        holder.toArray(ret);
        return ret;

    }


    /**
     * get the number of peaks without returning the peaks
     *
     * @return as above
     */
    @Override
    public int getPeaksCount() {
        return getSpectrum().length;
    }

    /**
     * spectrum - this might have been adjusted
     *
     * @return
     */
    @Override
    public ISpectrumPeak[] getPeaks() {
        return getSpectrum();
    }

    /**
     * fragment
     *
     * @return
     */
    protected PeptideIon[] buildSpectrum() {
        IPolypeptide pp = getPeptide();
        String sequence = pp.getSequence();
        int peptideLength = pp.getSequenceLength();

        if (pp.isModified())
            XTandemUtilities.breakHere();
        // one debug case
//        if("VPETTRINYVGEPTGWVSGK".equals(sequence))
//            XTandemUtilities.breakHere();

        List<PeptideIon> holder = new ArrayList<PeptideIon>();
        int lastItem = sequence.length() - 1;
        for (int i = 0; i < lastItem + 1; i++) {
            IPolypeptide[] frags = pp.cleave(i);
            for (IonType type : getIonTypes()) {
                int charge = getCharge();

                if(charge > 1)
                    TestUtilities.breakHere();

                double sequenceMass = frags[0].getMass();
                 //          double sequenceMass = m_Utilities.getSequenceMass(frags[0]);
                double added = m_Utilities.getAddedMass(type);
                sequenceMass += added;
                 // every charge adds the mass of a proton
                sequenceMass += (charge - 1) * m_Utilities.getdProton();

                int parentIndex = i;
                if (type == IonType.Y) {
                    parentIndex = peptideLength -  i;
                }

                double mz = sequenceMass / charge;
                PeptideIon ion = new PeptideIon(frags[0], type, charge, mz, parentIndex,parentIndex);
                holder.add(ion);

                // peptide bonds < 0 is a 0 length sequence
                if (frags[1].getNumberPeptideBonds() >= 0) {
                    double sequenceMass2 = m_Utilities.getSequenceMass(frags[1]);
                    IonType otherType = type.getPair();
                    added = m_Utilities.getAddedMass(otherType);
                    sequenceMass2 += added;
                    // every charge adds the mass of a proton
                    sequenceMass2 += (charge - 1) * m_Utilities.getdProton();

                    parentIndex = i;
                    if (type == IonType.Y) {
                        parentIndex = peptideLength -  i;
                    }

                    PeptideIon peptideIon = new PeptideIon(frags[1], otherType, charge, sequenceMass2 / charge, parentIndex,parentIndex);
                    holder.add(peptideIon);
                }
            }
        }

        PeptideIon[] ret = new PeptideIon[holder.size()];
        holder.toArray(ret);
        return ret;

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


}
