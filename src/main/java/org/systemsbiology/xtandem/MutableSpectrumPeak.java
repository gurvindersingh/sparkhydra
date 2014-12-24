package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.MutableSpectrumPeak
 *  version of a peak used during manipulations
 * @author Steve Lewis
 * @date Dec 29, 2010
 */
public class MutableSpectrumPeak implements Comparable<ISpectrumPeak>, ISpectrumPeak
{
    public static MutableSpectrumPeak[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MutableSpectrumPeak.class;
     private   double m_Mass;
    private   float m_Peak;

    public MutableSpectrumPeak(double pMass, float pPeak)
    {
        m_Mass = pMass;
        m_Peak = pPeak;
        if ( XTandemUtilities.isCloseTo(m_Mass, 248.28)  )
            XTandemUtilities.breakHere();  // track one case

    }


    public MutableSpectrumPeak(ISpectrumPeak in)
    {
        m_Mass = in.getMassChargeRatio();
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
         return false;
    }

    /**
     * return as an immutble peak
     * @return  as above
     */
    public ISpectrumPeak asImmutable()
    {
        return new SpectrumPeak(getMassChargeRatio(),getPeak());
    }

    @Override
    public double getMassChargeRatio()
    {
        return m_Mass;
    }

    @Override
    public float getPeak()
    {
        return m_Peak;
    }

    public void setPeak(float pPeak)
    {
        if ( XTandemUtilities.isCloseTo(m_Mass, 248.28)  )
            XTandemUtilities.breakHere();  // track one case
        m_Peak = pPeak;
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
