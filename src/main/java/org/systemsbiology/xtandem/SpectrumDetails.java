package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.SpectrumDetails
 *
 * @author Steve Lewis
 * @date Dec 21, 2010
 */
public class SpectrumDetails implements Comparable<SpectrumDetails>
{
    public static SpectrumDetails[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = SpectrumDetails.class;

    private float m_fU; // the M+H + error for an mspectrum
    private float m_fL; // the M+H - error for an mspectrum
    private long m_lA; // the index number for an mspectrum, in the m_vSpectra vector

    public float getfU()
    {
        return m_fU;
    }

    public void setfU(float pFU)
    {
        m_fU = pFU;
    }

    public float getfL()
    {
        return m_fL;
    }

    public void setfL(float pFL)
    {
        m_fL = pFL;
    }

    public long getlA()
    {
        return m_lA;
    }

    public void setlA(long pLA)
    {
        m_lA = pLA;
    }

    @Override
    public int compareTo(SpectrumDetails o)
    {
        if (this == o)
            return 0;
        if (m_fL < o.m_fL)
            return -1;
        if (m_fU > o.m_fU)
            return 1;
        return 0;
    }

 //   I think the above implements this
///*
// * override the less than operator, so that an mspectrumdetails can be easily compared to a
// * float M+H value
// */
//    bool operator<(const float &rhs)
//    {
//        return m_fL < rhs;
//    }
///*
// * override the greater than operator, so that an mspectrumdetails can be easily compared to a
// * float M+H value
// */
//    bool operator>(const float &rhs)
//    {
//        return m_fU > rhs;
//    }
///*
// * override the equivalence operator, so that an mspectrumdetails can be easily compared to a
// * float M+H value
// */
//    bool operator ==(const float &rhs)
//    {
//        return (rhs >= m_fL && rhs <= m_fU);
//    }

}
