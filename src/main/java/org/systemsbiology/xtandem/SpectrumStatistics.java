package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.SpectrumStatistics
 * immutable  class  with statistics on a spectrum - used for
 * normalization
 *
 * @author Steve Lewis
 * @date Jan 17, 2011
 */
public class SpectrumStatistics
{
    public static SpectrumStatistics[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = SpectrumStatistics.class;

    private final double m_TotalPeaks;
    private final double m_MaxPeak;
    private final double m_MinMass;
    private final double m_MaxMass;

    public SpectrumStatistics(ISpectrum s)
    {
        double sum = 0;
        double maxPeak = Double.MIN_VALUE;
        double maxMass = Double.MIN_VALUE;
        double minMass = Double.MAX_VALUE;
        for (ISpectrumPeak pl : s.getPeaks()) {
            final float peak = pl.getPeak();
            final double mass = pl.getMassChargeRatio();
            sum += peak;
            maxPeak = Math.max(peak, maxPeak);

            maxMass = Math.max(mass, maxMass);
            minMass = Math.min(mass, minMass);
        }
        m_TotalPeaks = sum;
        m_MaxPeak = maxPeak;
        m_MinMass = minMass;
        m_MaxMass = maxMass;

    }

    public double getTotalPeaks()
    {
        return m_TotalPeaks;
    }

    public double getMaxPeak()
    {
        return m_MaxPeak;
    }

    public double getMinMass()
    {
        return m_MinMass;
    }

    public double getMaxMass()
    {
        return m_MaxMass;
    }
}
