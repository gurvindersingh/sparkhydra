package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.MScoreTerm
 *
 * @author Steve Lewis
 * @date Dec 21, 2010
 */
public class MScoreTerm
{
    public static MScoreTerm[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MScoreTerm.class;

    private boolean m_bN; // true if N terminal modification allowed
    private boolean m_bC; // true if C terminal modification allowed
    private int m_lC; // 0 if C-terminal not modified, 1 if C-terminal modified
    private int m_lN; // 0 if N-terminal not modified, 1 if N-terminal modified
    private int m_lState; // Tracks the current state of the machine: value is the binary number formed by
    // assuming m_lC and m_lN are ordered bits in a two bit number, e.g.
    // state => m_lC = 1 & m_lN = 0 => m_lState = binary number '10' = 2.
/*
 * set up the state machine for the next sequence
 */

    public void initialize(final double _n, final double _c)
    {
        m_bN = false;
        m_bC = false;
        if (Math.abs(_n) > 0.001) {
            m_bN = true;
        }
        if (Math.abs(_c) > 0.001) {
            m_bC = true;
        }
        m_lState = 0;
        m_lN = 0;
        m_lC = 0;
    }

    public boolean isbN()
    {
        return m_bN;
    }

    public boolean isbC()
    {
        return m_bC;
    }

    public int getlC()
    {
        return m_lC;
    }

    public int getlN()
    {
        return m_lN;
    }

    public int getlState()
    {
        return m_lState;
    }

    public void setlState(int pLState)
    {
        m_lState = pLState;
    }

    public void setlN(int pLN)
    {
        m_lN = pLN;
    }

    public void setbC(boolean pBC)
    {
        m_bC = pBC;
    }

    public void setlC(int pLC)
    {
        m_lC = pLC;
    }

    public void setbN(boolean pBN)
    {
        m_bN = pBN;
    }
}
