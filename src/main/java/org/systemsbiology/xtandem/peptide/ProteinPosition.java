package org.systemsbiology.xtandem.peptide;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.sax.*;

/**
 * org.systemsbiology.xtandem.peptide.ProteinPosition
 * User: steven
 * Date: 2/8/12
 */
public class ProteinPosition implements IProteinPosition {
    public static final ProteinPosition[] EMPTY_ARRAY = {};

    public static final String TAG = "ProteinPosition";
    public static final String SERIALIZATION_SEPARATOR = "|";
    public static final String SERIALIZATION_SEPARATOR_SPILTTER = "\\" + SERIALIZATION_SEPARATOR;


    public static IProteinPosition asDecoy(IPolypeptide peop,ProteinPosition pp)
    {
         return new  ProteinPosition(pp, peop,true);
    }
    /**
     * give a sub peptide of the original peptide at offset build the new position objects
     *
     * @param newPeptide   new sub peptide
     * @param offset       offset >= 0
     * @param oldpositions - !null previous position objects
     * @return
     */
    public static IProteinPosition[] buildPeptidePositions(final IPolypeptide newPeptide, int offset, IProteinPosition[] oldpositions) {
        IProteinPosition[] ret = new IProteinPosition[oldpositions.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new ProteinPosition(newPeptide, offset, oldpositions[i]);

        }
        return ret;
    }

    /**
     * geive a sub peptide of the original peptide at offset build the new position objects
     *
     * @param newPeptide   new sub peptide
     * @param offset       offset >= 0
     * @param oldpositions - !null previous position objects
     * @return
     */
    public static IProteinPosition[] mergePeptidePositions(final IPolypeptide newPeptide, IProteinPosition[] startPositions, IProteinPosition[] endPositions) {
        IProteinPosition[] ret = new IProteinPosition[startPositions.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new ProteinPosition(newPeptide, startPositions[i], endPositions[i]);

        }
        return ret;
    }

    private final String m_Protein;
     private final IPolypeptide m_Peptide;
    private final FastaAminoAcid m_Before;
    private final FastaAminoAcid m_After;
    private final int m_StartPosition;
    private final boolean m_Decoy;

    public ProteinPosition(final String pProtein, final IPolypeptide pPeptide, final FastaAminoAcid pBefore, final FastaAminoAcid pAfter, final int pStartPosition) {
        m_Protein = XTandemUtilities.conditionProteinLabel(pProtein);
        m_Peptide = pPeptide;
        m_Before = pBefore;
        m_After = pAfter;
        m_StartPosition = pStartPosition;
        m_Decoy = false;
    }


    private ProteinPosition(ProteinPosition pp,IPolypeptide ppx,boolean decoy) {
        m_Protein = "DECOY_" + pp.getProtein() ;
         m_Peptide = ppx;
        m_Before = null;
        m_After = null;
        m_StartPosition = pp.getStartPosition();
        if(!decoy)
            throw new IllegalArgumentException("This constructor is only toe making decoys");
        m_Decoy = decoy;
    }



    /**
     * build a position from a sub peptide of the original
     *
     * @param newPeptide !null sub peptide
     * @param offset     offset of new peptide from original
     * @param oldpos     old position
     */
    public ProteinPosition(final IPolypeptide newPeptide, int offset, IProteinPosition oldpos) {
        m_Protein = XTandemUtilities.conditionProteinLabel(oldpos.getProtein());
         m_Peptide = newPeptide;
        String oldSequence = oldpos.getPeptide().getSequence();
      String newSequence = newPeptide.getSequence();

        if (offset <= 0)
            m_Before = oldpos.getBefore();
        else
            m_Before = FastaAminoAcid.asAminoAcidOrNull(oldSequence.charAt(offset - 1));

        if (newSequence.length() + offset >= oldSequence.length())
            m_After = oldpos.getAfter();
        else
            m_After = FastaAminoAcid.asAminoAcidOrNull(oldSequence.charAt(newSequence.length()));
        m_StartPosition = oldpos.getStartPosition() + offset;
        m_Decoy = false;
    }

    /**
     * build a position merging two peptides
     *
     * @param mergedPeptide !null sub peptide
     * @param offset        offset of new peptide from original
     * @param oldpos        old position
     */
    public ProteinPosition(final IPolypeptide mergedPeptide, IProteinPosition startPos, IProteinPosition endpos) {
        m_Protein = startPos.getProtein();
         m_Peptide = mergedPeptide;

        m_Before = startPos.getBefore();
        m_After = endpos.getAfter();

        m_StartPosition = startPos.getStartPosition();
        m_Decoy = false;
    }

    public ProteinPosition(final IPolypeptide pPeptide, String str) {
        String[] items = str.split(SERIALIZATION_SEPARATOR_SPILTTER );
        try {
            int index = 0;
            m_Protein = XTandemUtilities.conditionProteinLabel(items[index++]);
             m_Peptide = pPeptide;
             index++; // next is peptide
            if (items.length > index)
                m_Before = FastaAminoAcid.asAminoAcidOrNull(items[index++]);
            else
                m_Before = null;
            if (items.length > index)
                m_After = FastaAminoAcid.asAminoAcidOrNull(items[index++]);
            else
                m_After = null;
            if (items.length > index)
                m_StartPosition = Integer.parseInt(items[index++]);
            else
                m_StartPosition = 0;
        }
        catch (Exception e) {
            throw new RuntimeException(e);

        }
        m_Decoy = false;
    }


    /**
     * position representing an entire protein
     *
     * @param prot
     */
    public ProteinPosition(IProtein prot) {
        String id = prot.getId();
        //     String id = prot.getAnnotation();
        m_Protein = XTandemUtilities.conditionProteinLabel(id);
         m_Peptide = prot;
        m_Before = null;
        m_After = null;
        m_StartPosition = 0;
        m_Decoy = false;
    }

    @Override
    public String getProtein() {
        return m_Protein;
    }

    /**
     * unique id - descr up to first space
     *
     * @return
     */
    @Override
    public String getProteinId() {
        if (m_Protein != null) {
            int index = m_Protein.indexOf(" ");
            if (index > -1)
                return m_Protein.substring(0, index);
        }
        return m_Protein;
    }

    @Override
    public int getStartPosition() {
        return m_StartPosition;
    }

    @Override
    public FastaAminoAcid getAfter() {
        return m_After;
    }

    @Override
    public FastaAminoAcid getBefore() {
        return m_Before;
    }

    @Override
    public IPolypeptide getPeptide() {
        return m_Peptide;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ProteinPosition that = (ProteinPosition) o;

        if (m_StartPosition != that.m_StartPosition) return false;
        if (m_After != that.m_After) return false;
        if (m_Before != that.m_Before) return false;
       if (m_Peptide != null ? !m_Peptide.equals(that.m_Peptide) : that.m_Peptide != null) return false;
        if (m_Protein != null ? !m_Protein.equals(that.m_Protein) : that.m_Protein != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = m_Protein != null ? m_Protein.hashCode() : 0;
         result = 31 * result + (m_Peptide != null ? m_Peptide.hashCode() : 0);
        result = 31 * result + (m_Before != null ? m_Before.hashCode() : 0);
        result = 31 * result + (m_After != null ? m_After.hashCode() : 0);
        result = 31 * result + m_StartPosition;
        return result;
    }

    // NOTE Don not serialize peptide
    @Override
    public String toString() {
        return
                m_Protein +
                        SERIALIZATION_SEPARATOR +  //m_Peptide +
                        SERIALIZATION_SEPARATOR + m_Before +
                        SERIALIZATION_SEPARATOR + m_After +
                        SERIALIZATION_SEPARATOR + m_StartPosition
                ;
    }

    @Override
    public String asPeptidePosition() {
        StringBuilder sb = new StringBuilder();

        String protein = getProtein();
        sb.append(protein);
        sb.append(SERIALIZATION_SEPARATOR);

        FastaAminoAcid before = getBefore();
        if (before != null)
            sb.append(before);
        sb.append(SERIALIZATION_SEPARATOR);
        FastaAminoAcid after = getAfter();
        if (after != null)
            sb.append(after);
        sb.append(SERIALIZATION_SEPARATOR);
        sb.append(Integer.toString(getStartPosition()));
        return sb.toString();
    }

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     * 2) serialize
     *
     * @param adder !null where to put the data
     */
    public void serializeAsString(IXMLAppender adder) {
        adder.openTag(TAG);
        adder.appendAttribute("id", getProtein());

        FastaAminoAcid before = getBefore();
        if (before != null)
            adder.appendAttribute("before_aa", before);

        FastaAminoAcid after = getAfter();
        if (after != null)
            adder.appendAttribute("after_aa", after);

        adder.appendAttribute("start", getStartPosition());

        adder.closeTag(TAG);
    }

}
