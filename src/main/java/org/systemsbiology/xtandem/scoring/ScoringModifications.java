package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.peptide.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.ScoringModifications
 *
 * @author Steve Lewis
 * @date Jan 11, 2011
 */
public class ScoringModifications  implements Serializable {

    public static final String HARDCODED_MODIFICATIONS_PROPERTY = "org.systemsbiology.xtandem.HardCodeModifications";

      public static Class THIS_CLASS = ScoringModifications.class;

    private float m_CTerminalModificationMass;
    private float m_NTerminalModificationMass;
    private float m_CTerminalCharge;
    private float m_NTerminalCharge;
    private String[] m_ResidueModifications;
    private String m_ResiduePotentialModifications;
    private List<PeptideModification>  m_ModificationsList = new ArrayList<PeptideModification>();

    public ScoringModifications(IParameterHolder app) {
        m_NTerminalModificationMass = app.getFloatParameter(
                "protein, N-terminal residue modification mass", 0);
        m_CTerminalModificationMass = app.getFloatParameter(
                "protein, C-terminal residue modification mass", 0);
        m_NTerminalCharge = app.getFloatParameter("protein, cleavage N-terminal mass change", 0);
        m_CTerminalCharge = app.getFloatParameter("protein, cleavage C-terminal mass change", 0);

        m_ResidueModifications = app.getIndexedParameters("residue, modification mass");

        m_ResiduePotentialModifications = app.getParameter("residue, potential modification mass");


        buildModifications(app);
    }

    protected void buildModifications(IParameterHolder app) {
        if (!m_ModificationsList.isEmpty())
            return;

        List<PeptideModification> holder = new ArrayList<PeptideModification>();

        if (m_ResiduePotentialModifications != null && m_ResiduePotentialModifications.length() > 0) {
            PeptideModification[] peptideModifications = PeptideModification.fromListString(m_ResiduePotentialModifications, PeptideModificationRestriction.Global, false);
            holder.addAll(Arrays.asList(peptideModifications));
        }
        boolean doHardCoded = app.getBooleanParameter( HARDCODED_MODIFICATIONS_PROPERTY,true);
        PeptideModification.setHardCodeModifications(doHardCoded);
        if(doHardCoded)    {
            holder.add(PeptideModification.getCysteinModification());
        }

        m_ModificationsList.addAll(holder);
    }

    public void addModification(PeptideModification added) {
         m_ModificationsList.add(added);
    }


    public PeptideModification[] getModifications() {
        return m_ModificationsList.toArray(new PeptideModification[m_ModificationsList.size()]);
    }

    public float getCTerminalModificationMass() {
        return m_CTerminalModificationMass;
    }

    public float getCTerminalCharge() {
        return m_CTerminalCharge;
    }

    public float getNTerminalModificationMass() {
        return m_NTerminalModificationMass;
    }

    public float getNTerminalCharge() {
        return m_NTerminalCharge;
    }

    public String[] getResidueModifications() {
        return m_ResidueModifications;
    }

    public String getResiduePotentialModifications() {
        return m_ResiduePotentialModifications;
    }
}
