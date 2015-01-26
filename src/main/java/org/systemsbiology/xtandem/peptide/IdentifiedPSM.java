package org.systemsbiology.xtandem.peptide;

/**
 * org.systemsbiology.xtandem.peptide.IdentifiedPSM
 * an unmodifiable PSM where the spectrum is identified by its id
 * User: Steve
 * Date: 3/16/14
 */
public class IdentifiedPSM implements Comparable<IdentifiedPSM> {

    private final String m_Id;
    private final IPolypeptide m_Peptide;

    public IdentifiedPSM(final String pId, final IPolypeptide pPeptide) {
        m_Id = pId;
        m_Peptide = pPeptide;
    }

    public String getId() {
        return m_Id;
    }

    public IPolypeptide getPeptide() {
        return m_Peptide;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final IdentifiedPSM that = (IdentifiedPSM) o;
         //noinspection SimplifiableIfStatement,PointlessBooleanExpression,ConstantConditions,RedundantIfStatement
        if (!m_Id.equals(that.m_Id))
            return false;
         //noinspection SimplifiableIfStatement,PointlessBooleanExpression,ConstantConditions,RedundantIfStatement
        if (!m_Peptide.equals(that.m_Peptide))
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = m_Id.hashCode();
        result = 31 * result + m_Peptide.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return
                 m_Id + ":" + m_Peptide ;
    }


    @SuppressWarnings("NullableProblems")
    @Override
    public int compareTo(final IdentifiedPSM o) {
        int ret = getId().compareTo(o.getId())  ;
        if(ret != 0 )
            return ret;
        return getPeptide().toString().compareTo(o.getPeptide().toString());
    }
}
