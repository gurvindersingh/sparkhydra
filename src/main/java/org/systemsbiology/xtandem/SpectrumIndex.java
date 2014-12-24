package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.SPectrumIndex
 *
 * @author Steve Lewis
 * @date Dec 21, 2010
 */
public class SpectrumIndex implements Comparable<SpectrumIndex>
{
    public static SpectrumIndex[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = SpectrumIndex.class;

    public SpectrumIndex(float pFM, int pTA)
    {
        m_fM = pFM;
        m_tA = pTA;
    }

    public SpectrumIndex(SpectrumIndex o)
    {
        m_fM = o.m_fM;
        m_tA = o.m_tA;
    }

    private final float m_fM; // the M+H + error for an mspectrum
    private final int m_tA; // the index number for an mspectrum, in the m_vSpectra vector
/*
 * override the less than operator, so that an mspectrumdetails can be easily compared to a
 * float M+H value
 */

    @Override
    public int compareTo(SpectrumIndex o)
    {
        if (this == o)
            return 0;
        if (m_fM != o.m_fM)
            return m_fM < o.m_fM ? -1 : 1;
        return 0;
    }

    public float getfM()
    {
        return  m_fM;
    }
    public int gettA()
    {
        return  m_tA;
    }
}
