package org.systemsbiology.xtandem;

import java.util.*;

/**
 * org.systemsbiology.xtandem.SpectrumPeak
 *
 * @author Steve Lewis
 * @date Dec 29, 2010
 */
public class SpectrumPeak implements Comparable<ISpectrumPeak>, ISpectrumPeak
{
    public static ISpectrumPeak[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = SpectrumPeak.class;

    public static final Comparator<ISpectrumPeak> INTENSITY_COMPARATOR = new IntensityComparator();

    /**
     * sort by peak height then mass
     */
    public static final class IntensityComparator implements Comparator<ISpectrumPeak>
    {
        private IntensityComparator() {}

        @Override
        public int compare(ISpectrumPeak o1, ISpectrumPeak o2)
        {
            if (o1 == o2)
                return 0;
            double m1 = o1.getPeak();
            double m2 = o2.getPeak();
            if (m1 != m2)
                return m1 < m2 ? -1 : 1;

            m1 = o1.getMassChargeRatio();
            m2 = o2.getMassChargeRatio();
            if (m1 != m2)
                return m1 < m2 ? -1 : 1;
            return 0;
        }
    }

    private final double m_MZ;
    private final float m_Peak;

    public SpectrumPeak(double pMZ, double pPeak)
    {
        m_MZ = pMZ;
        m_Peak = (float)pPeak;

    }


    public SpectrumPeak(ISpectrumPeak in)
    {
        m_MZ = in.getMassChargeRatio();
        m_Peak = in.getPeak();
    }

    /**
     * return true if the spectrum is immutable
     *
     * @return
     */
    @Override
    public boolean isImmutable()
    {
         return true;
    }


    @Override
    public double getMassChargeRatio()
    {
        return m_MZ;
    }

    @Override
    public float getPeak()
    {
        return m_Peak;
    }

    /**
     * return as an immutble peak
     *
     * @return as above
     */
    @Override
    public ISpectrumPeak asImmutable()
    {
        return this;   // we are immutable
    }


    /**
     * sort by mass then peak height
     */
    @Override
    public int compareTo(ISpectrumPeak o)
    {
        if (this == o)
            return 0;
        double m1 = getMassChargeRatio();
        double m2 = o.getMassChargeRatio();
        if (m1 != m2)
            return m1 < m2 ? -1 : 1;

        m1 = getPeak();
        m2 = o.getPeak();
        if (m1 != m2)
            return m1 < m2 ? -1 : 1;
        return 0;
    }

    /**
     * return true if this and o are 'close enough'
     *
     * @param !null o
     * @return as above
     */
    @Override
    public boolean equivalent(ISpectrumPeak o)
    {
        if (this == o)
              return true;
          double m1 = getMassChargeRatio();
          double m2 = o.getMassChargeRatio();
          if (!XTandemUtilities.equivalentDouble(m1,m2))
              return false;

          m1 = getPeak();
          m2 = o.getPeak();
        if (!XTandemUtilities.equivalentDouble(m1,m2))
            return false;
          return true;
    }

    @Override
    public String toString()
    {
        return "mass=" + XTandemUtilities.formatDouble(getMassChargeRatio(),
                2) + ", peak=" + XTandemUtilities.formatDouble(getPeak(), 5);

    }
}
