package org.systemsbiology.xtandem.bioml;

/**
 * org.systemsbiology.xtandem.bioml.ProteinAssignment
 * assignment of a peptide to a protein
 * User: steven
 * Date: 8/22/11
 */
public class ProteinAssignment {
    public static final ProteinAssignment[] EMPTY_ARRAY = {};

    private final int m_Start;
    private final int m_End;
    private final String m_Pre;
    private final String m_Post;
    private final String m_Annotation;

    public ProteinAssignment(final int pStart, final int pEnd, final String pPre, final String pPost, final String pAnnotation) {
        m_Start = pStart;
        m_End = pEnd;
        m_Pre = pPre;
        m_Post = pPost;
        m_Annotation = pAnnotation;
    }

    public int getStart() {
        return m_Start;
    }

    public int getEnd() {
        return m_End;
    }

    public String getPre() {
        return m_Pre;
    }

    public String getPost() {
        return m_Post;
    }

    public String getAnnotation() {
        return m_Annotation;
    }
}
