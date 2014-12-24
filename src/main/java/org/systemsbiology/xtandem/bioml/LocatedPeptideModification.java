package org.systemsbiology.xtandem.bioml;

import org.systemsbiology.xtandem.peptide.*;

/**
 * org.systemsbiology.xtandem.bioml.LocatedPeptideModification
 * User: steven
 * Date: 8/23/11
 */
public class LocatedPeptideModification {
    public static final LocatedPeptideModification[] EMPTY_ARRAY = {};

    private final PeptideModification m_Modification;
    private final int m_Location;

    public LocatedPeptideModification(final PeptideModification pModification, final int pLocation) {
        m_Modification = pModification;
        m_Location = pLocation;
    }

    public PeptideModification getModification() {
        return m_Modification;
    }

    public int getLocation() {
        return m_Location;
    }
}
