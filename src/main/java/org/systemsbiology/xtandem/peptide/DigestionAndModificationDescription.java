package org.systemsbiology.xtandem.peptide;

import java.util.*;

/**
 * org.systemsbiology.xtandem.peptide.DigestionAndModificationDescription
 * describe the digestion and modifications of peptides
 * User: steven
 * Date: 9/16/11
 */
public class DigestionAndModificationDescription {
    public static final DigestionAndModificationDescription[] EMPTY_ARRAY = {};

    private final String m_Digester;
    private final boolean m_Semitryptic;
    private final int m_MaxMissedCleavages;
    private final PeptideModification[] m_Modifications;

    public DigestionAndModificationDescription(String xml)
    {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public String getDigester() {
        return m_Digester;
    }

    public boolean isSemitryptic() {
        return m_Semitryptic;
    }

    public int getMaxMissedCleavages() {
        return m_MaxMissedCleavages;
    }

    public PeptideModification[] getModifications() {
        return m_Modifications;
    }

    public String asXML()   {
        StringBuilder sb = new StringBuilder();
         if(true)
             throw new UnsupportedOperationException("Fix This"); // ToDo
        return sb.toString();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final DigestionAndModificationDescription that = (DigestionAndModificationDescription) o;

        if (m_MaxMissedCleavages != that.m_MaxMissedCleavages) return false;
        if (m_Semitryptic != that.m_Semitryptic) return false;
        if (m_Digester != null ? !m_Digester.equals(that.m_Digester) : that.m_Digester != null) return false;
        if (!Arrays.equals(m_Modifications, that.m_Modifications)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = m_Digester != null ? m_Digester.hashCode() : 0;
        result = 31 * result + (m_Semitryptic ? 1 : 0);
        result = 31 * result + m_MaxMissedCleavages;
        result = 31 * result + (m_Modifications != null ? Arrays.hashCode(m_Modifications) : 0);
        return result;
    }
}
