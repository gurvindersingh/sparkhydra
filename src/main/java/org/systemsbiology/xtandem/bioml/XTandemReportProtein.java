package org.systemsbiology.xtandem.bioml;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

/**
 * org.systemsbiology.xtandem.bioml.XTandemReportProtein
 * User: steven
 * Date: 8/22/11
 */
public class XTandemReportProtein {
    public static final XTandemReportProtein[] EMPTY_ARRAY = {};

    private double m_Expect;
    private String m_Id;
    private String m_Uid;
    private String m_Label;
    private String m_File;
    private String m_Description;
    private int m_PeptideLength;
    private double m_Hyperscore;
    private double m_Delta;
    private double m_MH;
    private double m_NextScore;
    private double m_SumI;
     private IPolypeptide m_Polypeptide;
    private ProteinAssignment m_Assignment;
    IonUseScore m_Score;

    public XTandemReportProtein() {
    }

    public ISpectralMatch asSpectralMatch(IMeasuredSpectrum measured)
    {

        SpectralMatch sm = new SpectralMatch(m_Polypeptide, measured,
                         m_Hyperscore, getHyperscore(),m_Hyperscore,
                         getScore(),null);
         return sm; // todo fix
    }

    public double getDelta() {
        return m_Delta;
    }

    public void setDelta(final double pDelta) {
        m_Delta = pDelta;
    }

    public double getMH() {
        return m_MH;
    }

    public void setMH(final double pMH) {
        m_MH = pMH;
    }

    public double getHyperscore() {
        return m_Hyperscore;
    }

    public void setHyperscore(final double pHyperscore) {
        m_Hyperscore = pHyperscore;
    }

    public double getNextScore() {
        return m_NextScore;
    }

    public void setNextScore(final double pNextScore) {
        m_NextScore = pNextScore;
    }

    public IPolypeptide getPolypeptide() {
        return m_Polypeptide;
    }

    public void setPolypeptide(final IPolypeptide pPolypeptide) {
        m_Polypeptide = pPolypeptide;
        if(pPolypeptide != null && pPolypeptide instanceof Polypeptide)   {
            String label = getLabel();
            if(label != null)   {
                IProteinPosition pPos = new ProteinPosition(label,pPolypeptide,null,null,0);
                IProteinPosition[] ppp = {pPos };
                ((Polypeptide)pPolypeptide).setContainedInProteins(ppp);

            }
        }
    }

    public String getFile() {
        return m_File;
    }

    public void setFile(final String pFile) {
        m_File = pFile;
    }

    public String getDescription() {
        return m_Description;
    }

    public void setDescription(final String pDescription) {
        m_Description = pDescription;
    }

    public IonUseScore getScore() {
        return m_Score;
    }

    public void setScore(final IonUseScore pScore) {
        m_Score = pScore;
    }

    public int getPeptideLength() {
        return m_PeptideLength;
    }

    public void setPeptideLength(final int pPeptideLength) {
        m_PeptideLength = pPeptideLength;
    }

    public double getExpect() {
        return m_Expect;
    }

    public void setExpect(final double pExpect) {
        m_Expect = pExpect;
    }

    public String getId() {
        return m_Id;
    }

    public void setId(final String pId) {
        m_Id = pId;
    }

    public String getUid() {
        return m_Uid;
    }

    public void setUid(final String pUid) {
        m_Uid = pUid;
    }

    public String getLabel() {
        return m_Label;
    }

    public void setLabel(final String pLabel) {
        m_Label = pLabel;
    }

    public double getSumI() {
        return m_SumI;
    }

    public void setSumI(final double pSumI) {
        m_SumI = pSumI;
    }

    public ProteinAssignment getAssignment() {
        return m_Assignment;
    }

    public void setAssignment(final ProteinAssignment pAssignment) {
        m_Assignment = pAssignment;
    }
}
