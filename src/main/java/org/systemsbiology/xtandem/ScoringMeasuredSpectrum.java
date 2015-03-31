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
public class ScoringMeasuredSpectrum implements IMeasuredSpectrum {
    public static ScoringMeasuredSpectrum[] EMPTY_ARRAY = {};

    private final int m_PrecursorCharge;
    private final double m_PrecursorMass;
    private final double m_PrecursorMassChargeRatio;
    private final ISpectralScan m_Scan;
    private final ISpectrumPeak[] m_Peaks;
    private double m_SumIntensity;
    private double m_MaxIntensity;

    public ScoringMeasuredSpectrum(int precursorCharge, double precursorMz, ISpectralScan scan, ISpectrumPeak[] peaks) {
        m_Scan = scan;
        m_PrecursorCharge = precursorCharge;
        m_PrecursorMassChargeRatio = precursorMz;
        double chargeRatio = precursorMz;
        double protonMass = XTandemUtilities.getProtonMass();
        int charge = precursorCharge;
        if (charge == 0)
            charge = 1;
        m_PrecursorMass = ((chargeRatio - protonMass) * charge) + protonMass;
        if (m_PrecursorMass < 0)
            XTandemUtilities.breakHere();

        //   if(true)
        //       throw new UnsupportedOperationException("Fix This"); // ToDo
        // copy array making sure the peaks are ummutable
        ISpectrumPeak[] mypeaks = new ISpectrumPeak[peaks.length];
        for (int i = 0; i < mypeaks.length; i++) {
            mypeaks[i] = peaks[i].asImmutable();
        }
        // sort by mass
        Arrays.sort(mypeaks, ScoringUtilities.PeakMassComparatorINSTANCE);
        m_Peaks = mypeaks;
        for (int i = 0; i < m_Peaks.length; i++) {
            ISpectrumPeak pk = m_Peaks[i];
            float peak = pk.getPeak();
            m_MaxIntensity = Math.max(peak, m_MaxIntensity);
            m_SumIntensity += peak;

        }

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

    public double getPrecursorMassChargeRatio() {
        return m_PrecursorMassChargeRatio;
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
        if (pm < 40)
            XTandemUtilities.breakHere();

        adder.appendAttribute("PrecursorMass", XTandemUtilities.formatDouble(pm, 3));
        adder.endTag();
        adder.cr();
        ISpectrumPeak[] peaks = getPeaks();
        adder.openEmptyTag("peaks");
        String txt = XTandemUtilities.encodePeaks(peaks, MassResolution.Bits32);
        adder.appendText(txt);
        adder.closeTag("peaks");
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
                return st1.equivalent(st2);

        }
        return true;
    }


    /**
     * return true if the spectrum is immutable
     *
     * @return
     */
    @Override
    public boolean isImmutable() {
        return true;
    }

    /**
     * if the spectrum is not immutable build an immutable version
     * Otherwise return this
     *
     * @return as above
     */
    @Override
    public IMeasuredSpectrum asImmutable() {
        return this;
    }

    /**
     * if the spectrum is not  mutable build an  mutable version
     * Otherwise return this
     *
     * @return as above
     */
    @Override
    public MutableMeasuredSpectrum asMmutable() {
        return new MutableMeasuredSpectrum(this);
    }

    /**
     * get run identifier
     *
     * @return
     */
    @Override
    public String getId() {
        ISpectralScan scanData = getScanData();
        if(scanData == null)
            return null;
        return scanData.getId();
    }

    /**
     * get index - return 0 if unknown
     *
     * @return as above
     */
    @Override
    public int getIndex() {
        ISpectralScan scanData = getScanData();
        if(scanData == null)
            return 0;
        return scanData.getIndex();
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

    @Override
    public String toString() {
        return getId();
    }
}
