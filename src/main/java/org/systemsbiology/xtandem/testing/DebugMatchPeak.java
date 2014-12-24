package org.systemsbiology.xtandem.testing;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;

/**
 * org.systemsbiology.xtandem.testing.DebugMatchPeak
 *
 * @author Steve Lewis
 * @date Mar 4, 2011
 */
public class DebugMatchPeak implements Comparable<DebugMatchPeak>, IEquivalent<DebugMatchPeak>
{
    public static DebugMatchPeak[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = DebugMatchPeak.class;



    private final double m_MassOffset;
    private final double m_Added;
    private final int m_Mass;
    private final IonType m_Type;

    public DebugMatchPeak(double pMassOffset, double pAdded, int pMass, IonType type)
    {
        m_MassOffset = pMassOffset;
        m_Added = pAdded;
        m_Mass = pMass;
        m_Type = type;
    }

    public double getMassOffset()
    {
        return m_MassOffset;
    }

    public double getAdded()
    {
        return m_Added;
    }

    public int getMass()
    {
        return m_Mass;
    }

    public IonType getType()
    {
        return m_Type;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("match:" + getType() + " ");
        String offset = String.format("%4.2f",m_MassOffset);
        sb.append(offset);

//        switch(m_MassOffset) {
//            case 1:
//                sb.append("+1");
//                break;
//            case -1:
//                sb.append("-1");
//                break;
//        }
        sb.append(" " + m_Mass);
        sb.append(" " + XTandemUtilities.formatDouble(getAdded(),6));
           return sb.toString();

    }

    /**
     * NOTE - when used as a key the value is irrelevant - this is uses to see
     * which peaks are found
     * @param o  !null tets object
     * @return   true if equals
     */
    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DebugMatchPeak that = (DebugMatchPeak) o;

        if (m_Mass != that.m_Mass) return false;
        if (m_MassOffset != that.m_MassOffset) return false;
        if (m_Type != that.m_Type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int)(100 * m_MassOffset);
        result = 31 * result + m_Mass;
        result = 31 * result + (m_Type != null ? m_Type.hashCode() : 0);
        return result;
    }

    /**
     * return true if this and o are 'close enough'
     *
     * @param o !null test object
     * @return as above
     */
    @Override
    public boolean equivalent(final DebugMatchPeak o) {
        if(getMass() != o.getMass())
            return false;
        if(getMassOffset() != o.getMassOffset())
            return false;
        if(getType() != o.getType())
            return false;
        if(Math.abs(getAdded() - o.getAdded()) > 0.005)
            return false;
        return true;
    }

    @Override
    public int compareTo(DebugMatchPeak o)
    {
         if (o == this)
            return 0;

        int ret = XTandemUtilities.compareTo(getMass(), o.getMass());
        if(ret != 0)
            return ret;
        ret =  getType().toString().compareTo( o.getType().toString());
        if(ret != 0)
            return ret;
        return ret;
    }
}
