package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.CorePAM
 *
 * @author Steve Lewis
 * @date Dec 21, 2010
 */
/*
 * mscorepam is a specialty class used to store information necessary for the state machine that
 * mscore uses to generate all potential point mutations
 */

public class ScorePAM
{
    public static ScorePAM[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ScorePAM.class;

    public static final String ALL_PEPTIDES = "ACDEFGHKLMNPQRSTVWY";

    private int m_tLength;
    private int m_tPos;
    private int m_tAa;
    private int m_tCount;
    private char m_cCurrent;
    private int m_tAaTotal = ALL_PEPTIDES.length();
    private String m_pAa = ALL_PEPTIDES;
    private StringBuffer m_pSeqTrue = new StringBuffer();
    private int m_tSeqTrue;
    private float m_fSeqTrue;

    public void initialize(final String _p,final int _s,final float _f)
    {
        if(m_tSeqTrue < _s)	{
            m_tSeqTrue = _s;
        }
         m_pSeqTrue.setLength(0);
        m_pSeqTrue.append(_p);
        m_fSeqTrue = _f;
        m_tLength = _p.length();
        m_tPos = 0;
        m_tAa = 0;
        m_tCount = 0;
        m_cCurrent = '\0';
     }

    public int gettLength()
    {
        return m_tLength;
    }

    public void settLength(int pTLength)
    {
        m_tLength = pTLength;
    }

    public int gettPos()
    {
        return m_tPos;
    }

    public void settPos(int pTPos)
    {
        m_tPos = pTPos;
    }

    public int gettAa()
    {
        return m_tAa;
    }

    public void settAa(int pTAa)
    {
        m_tAa = pTAa;
    }

    public int gettCount()
    {
        return m_tCount;
    }

    public void settCount(int pTCount)
    {
        m_tCount = pTCount;
    }

    public char getcCurrent()
    {
        return m_cCurrent;
    }

    public void setcCurrent(char pCCurrent)
    {
        m_cCurrent = pCCurrent;
    }

    public int gettAaTotal()
    {
        return m_tAaTotal;
    }

    public void settAaTotal(int pTAaTotal)
    {
        m_tAaTotal = pTAaTotal;
    }

    public String getpAa()
    {
        return m_pAa;
    }

    public void setpAa(String pPAa)
    {
        m_pAa = pPAa;
    }

    public StringBuffer getpSeqTrue()
    {
        return m_pSeqTrue;
    }

    public void setpSeqTrue(StringBuffer pPSeqTrue)
    {
        m_pSeqTrue = pPSeqTrue;
    }

    public int gettSeqTrue()
    {
        return m_tSeqTrue;
    }

    public void settSeqTrue(int pTSeqTrue)
    {
        m_tSeqTrue = pTSeqTrue;
    }

    public float getfSeqTrue()
    {
        return m_fSeqTrue;
    }

    public void setfSeqTrue(float pFSeqTrue)
    {
        m_fSeqTrue = pFSeqTrue;
    }
}
