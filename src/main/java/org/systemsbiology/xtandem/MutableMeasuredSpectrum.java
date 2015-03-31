package org.systemsbiology.xtandem;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.ScoringMeasuredSpectrum
 *
 * @author Steve Lewis
 * @date Jan 13, 2011
 */
public class MutableMeasuredSpectrum implements IMeasuredSpectrum {
    public static MutableMeasuredSpectrum[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MutableMeasuredSpectrum.class;
    public static final double PROTON_MASS = MassCalculator.getDefaultCalculator().calcMass("H");


    private int m_PrecursorCharge;
    private double m_PrecursorMass;
    private double m_PrecursorMassChargeRatio;
    private final ISpectralScan m_Scan;
    private ISpectrumPeak[] m_Peaks;

    public MutableMeasuredSpectrum(IMeasuredSpectrum source) {
        m_Scan = source.getScanData();
        m_PrecursorCharge = source.getPrecursorCharge();
        m_PrecursorMass = source.getPrecursorMass();
        m_PrecursorMassChargeRatio = source.getPrecursorMassChargeRatio();
        // copy array making sure the peaks are ummutable
        setPeaks(source.getPeaks());
    }

    public MutableMeasuredSpectrum( ) {
        this(0, 0, null, ISpectrumPeak.EMPTY_ARRAY);
    }

    public MutableMeasuredSpectrum(int precursorCharge, double precursorMassChargeRatio, ISpectralScan scan, ISpectrumPeak[] peaks) {
        m_Scan = scan;
        m_PrecursorCharge = precursorCharge;
        m_PrecursorMassChargeRatio = precursorMassChargeRatio;
        m_PrecursorMass = (m_PrecursorMassChargeRatio - PROTON_MASS) * m_PrecursorCharge + PROTON_MASS ;
        // copy array making sure the peaks are ummutable
        setPeaks(peaks);
    }

    public double getPrecursorMassChargeRatio() {
        return m_PrecursorMassChargeRatio;
    }

    public void setPrecursorMassChargeRatio(final double pPrecursorMassChargeRatio) {
        m_PrecursorMassChargeRatio = pPrecursorMassChargeRatio;
    }

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    @Override
    public void serializeAsString(final IXMLAppender adder) {
        adder.openTag("MeasuredSpectrum");
        adder.appendAttribute("PrecursorCharge", getPrecursorCharge());
        double pm = getPrecursorMass();
        if(pm < 40)
             XTandemUtilities.breakHere();

        adder.appendAttribute("PrecursorMass", XTandemUtilities.formatDouble(pm, 3));
        adder.endTag();
        adder.cr();
        ISpectrumPeak[] peaks = getPeaks();
        adder.openEmptyTag("peaks");
        String txt = XTandemUtilities.encodePeaks(peaks, MassResolution.Bits32);
        adder.appendText(txt);
        adder.closeTag("peaks");
        adder.cr();
//        for (int i = 0; i < peaks.length; i++) {
//            adder.openTag("Peak");
//            ISpectrumPeak peak = peaks[i];
//            adder.appendAttribute("massChargeRatio", XTandemUtilities.formatDouble(peak.getMassChargeRatio(), 3));
//            adder.appendAttribute("peak", XTandemUtilities.formatDouble(peak.getPeak(), 5));
//            adder.closeTag("Peak");
//        }
        adder.closeTag("MeasuredSpectrum");


    }

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(IMeasuredSpectrum test) {
        if (!equivalent((ISpectrum) test))
            return false;
        if (!XTandemUtilities.equivalentDouble(getPrecursorCharge(), test.getPrecursorCharge()))
            return false;
        if (!XTandemUtilities.equivalentDouble(getPrecursorMass(), test.getPrecursorMass()))
            return false;
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


    public void setPrecursorCharge(final int pPrecursorCharge) {
        m_PrecursorCharge = pPrecursorCharge;
    }

    public void setPrecursorMass(final double pPrecursorMass) {
        m_PrecursorMass = pPrecursorMass;
    }

    public void setPeaks(ISpectrumPeak[] peaks) {
        ISpectrumPeak[] mypeaks = new ISpectrumPeak[peaks.length];
        for (int i = 0; i < mypeaks.length; i++) {
            mypeaks[i] = new MutableSpectrumPeak(peaks[i]);
        }
        // sort by mass
        Arrays.sort(mypeaks, ScoringUtilities.PeakMassComparatorINSTANCE);
        m_Peaks = mypeaks;
    }


    public synchronized  void addPeak(final ISpectrumPeak  added) {
        ISpectrumPeak[] np = new ISpectrumPeak[m_Peaks.length + 1];
        for (int i = 0; i < m_Peaks.length; i++) {
              np[i] = m_Peaks[i];
        }
        np[ m_Peaks.length] = added;
        Arrays.sort(np, ScoringUtilities.PeakMassComparatorINSTANCE);
         m_Peaks = np;
    }

    /**
     * return true if the spectrum is immutable
     *
     * @return
     */
    @Override
    public boolean isImmutable() {
        return false;
    }

    /**
     * if the spectrum is not immutable build an immutable version
     * Otherwise return this
     *
     * @return as above
     */
    @Override
    public IMeasuredSpectrum asImmutable() {
        return new ScoringMeasuredSpectrum(getPrecursorCharge(), getPrecursorMassChargeRatio(), getScanData(), getPeaks());
    }

    /**
     * if the spectrum is not  mutable build an  mutable version
     * Otherwise return this
     *
     * @return as above
     */
    @Override
    public MutableMeasuredSpectrum asMmutable() {
        return this;
    }

    /**
     * get run identifier
     *
     * @return
     */
    @Override
    public String getId() {
        return getScanData().getId();
    }
    /**
      * get run identifier
      *
      * @return
      */
     @Override
     public int getIndex() {
         return getScanData().getIndex();
     }

    /**
     * get the charge of the spectrum precursor
     *
     * @return
     */
    @Override
    public int getPrecursorCharge() {
        return m_PrecursorCharge;
    }

    /**
     * get the mass of the spectrum precursor
     *
     * @return as above
     */
    @Override
    public double getPrecursorMass() {
        return m_PrecursorMass;
    }


    /**
     * Mass spec characteristics
     *
     * @return
     */
    @Override
    public ISpectralScan getScanData() {
        return m_Scan;
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
     * get the number of peaks without returning the peaks
     *
     * @return as above
     */
    public int getPeaksCount() {
        return m_Peaks.length;
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
