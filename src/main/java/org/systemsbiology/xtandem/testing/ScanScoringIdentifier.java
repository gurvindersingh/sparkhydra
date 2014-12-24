package org.systemsbiology.xtandem.testing;

import org.systemsbiology.xtandem.ionization.*;

import java.io.*;

/**
 * org.systemsbiology.xtandem.testing.ScanScoringIdentifier
 * identifier having sequence string, charge and iontype
 * User: steven
 * Date: 7/27/11
 */
public class ScanScoringIdentifier implements Comparable<ScanScoringIdentifier>,Serializable {
    public static final ScanScoringIdentifier[] EMPTY_ARRAY = {};

    private final String m_Sequence;
    private final int m_Charge;
     private final IonType m_Type;

    public ScanScoringIdentifier(final String pSequence, final int pCharge, final IonType pType) {
        m_Sequence = pSequence;
        m_Charge = pCharge;
        m_Type = pType;
    }

    public String getSequence() {
        return m_Sequence;
    }

    public int getCharge() {
        return m_Charge;
    }

    public IonType getType() {
        return m_Type;
    }

    @Override
    public String toString() {
        return   m_Sequence + ':' +
                   m_Charge + ':' +
                   m_Type;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ScanScoringIdentifier that = (ScanScoringIdentifier) o;

        if (m_Charge != that.m_Charge) return false;
        if (m_Sequence != null ? !m_Sequence.equals(that.m_Sequence) : that.m_Sequence != null) return false;
        if (m_Type != that.m_Type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = m_Sequence != null ? m_Sequence.hashCode() : 0;
        result = 31 * result + m_Charge;
        result = 31 * result + (m_Type != null ? m_Type.hashCode() : 0);
        return result;
    }


    @Override
    public int compareTo(final ScanScoringIdentifier o) {
        if(this == o)
            return 0;
        if(this.equals(o))
            return 0;
        int ret = getSequence().compareTo(o.getSequence()) ;
        if(ret != 0)
            return ret;
        int charge = getCharge();
        int charge1 = o.getCharge();
        if(charge != charge1)
            return charge < charge1 ? -1 : 1;
        return getType().compareTo(o.getType());
    }
}
