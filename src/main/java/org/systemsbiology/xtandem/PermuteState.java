package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.PermuteState
 *
 * @author Steve Lewis
 * @date Dec 21, 2010
 */
public class PermuteState
{
    public static PermuteState[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = PermuteState.class;

    private int m_tPos;
    private int m_tEnd;
    private String m_pSeq;
    private String m_pPerm;
    private int m_lSize;
    private boolean m_bRev;

    public int gettPos()
    {
        return m_tPos;
    }

    public void settPos(int pTPos)
    {
        m_tPos = pTPos;
    }

    public int gettEnd()
    {
        return m_tEnd;
    }

    public void settEnd(int pTEnd)
    {
        m_tEnd = pTEnd;
    }

    public String getpSeq()
    {
        return m_pSeq;
    }

    public void setpSeq(String pPSeq)
    {
        m_pSeq = pPSeq;
    }

    public String getpPerm()
    {
        return m_pPerm;
    }

    public void setpPerm(String pPPerm)
    {
        m_pPerm = pPPerm;
    }

    public int getlSize()
    {
        return m_lSize;
    }

    public void setlSize(int pLSize)
    {
        m_lSize = pLSize;
    }

    public boolean isbRev()
    {
        return m_bRev;
    }

    public void setbRev(boolean pBRev)
    {
        m_bRev = pBRev;
    }
}
