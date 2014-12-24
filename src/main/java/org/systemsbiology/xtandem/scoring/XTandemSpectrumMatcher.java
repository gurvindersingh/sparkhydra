package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;

/**
 * org.systemsbiology.xtandem.scoring.XTandemSpectrumMatcher
 * User: steven
 * Date: 1/19/11
 */
public class XTandemSpectrumMatcher implements ISpectrumMatcher {
    public static final XTandemSpectrumMatcher[] EMPTY_ARRAY = {};


    private IMeasuredSpectrum m_Measured;
    private ITheoreticalSpectrum m_Theory;
    private ISpectrumPeak[] m_MeasuredPeaks;
    private ITheoreticalPeak[] m_TheoryPeaks;


    // pair of peaks to return
    private final ISpectrumPeak[] m_Return = new ISpectrumPeak[2];

    private int m_MeasuredIndex;
    private int m_TheoryIndex;

    private double m_MaxDistanceForMatch = 0.4;


    /**
     * prepare to compare a measured and a theoretical spectrum
     *
     * @param measured !null measured spectrum
     * @param test     !null theoretical spectrum
     */
    @Override
    public void load(final IMeasuredSpectrum measured, final ITheoreticalSpectrum theory) {
        m_Measured = measured;
        m_MeasuredPeaks = m_Measured.getPeaks();
        m_Theory = theory;
        m_TheoryPeaks = m_Theory.getTheoreticalPeaks();
        m_MeasuredIndex = 0;
        m_TheoryIndex = 0;


    }

    protected boolean isMatch(ISpectrumPeak measured, ISpectrumPeak theory) {
        return Math.abs(measured.getMassChargeRatio() - theory.getMassChargeRatio()) < m_MaxDistanceForMatch;
    }

    /**
     * return an array with the next two matching peaks - return
     * null when there are no more matches
     *
     * @return
     */
    @Override
    public ISpectrumPeak[] nextMatch() {
        boolean matchFound = false;
        ISpectrumPeak measuredPeak = null;
        ISpectrumPeak theoreticalPeak = null;
        while (m_MeasuredIndex < m_MeasuredPeaks.length && m_TheoryIndex < m_TheoryPeaks.length) {
            measuredPeak = m_MeasuredPeaks[m_MeasuredIndex];
            theoreticalPeak = m_TheoryPeaks[m_TheoryIndex];
            if (matchFound = isMatch(measuredPeak, theoreticalPeak)) {
                if (measuredPeak.getMassChargeRatio() < theoreticalPeak.getMassChargeRatio())
                    m_MeasuredIndex++;
            }
            else {
                m_TheoryIndex++;
                if (matchFound)
                    break;
            }
        }

        if (matchFound) {
            m_Return[0] = measuredPeak;
            m_Return[1] = theoreticalPeak;
            return m_Return;
        }
        return null; // no match

    }
}
