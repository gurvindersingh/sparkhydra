package org.systemsbiology.xtandem;

import java.util.*;

/**
 * org.systemsbiology.xtandem.CoreState
 *
 * @author Steve Lewis
  */
/*
 * mscorestate is a specialty class used to store information necessary for the state machine that
 * mscore uses to generate all potentially modified peptide sequences and masses
 * This Does not seem to be used much - it is a copy From X!Tandem
 */



public class CoreState
{
    public static CoreState[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = CoreState.class;

    private boolean m_bStateS; // true if there are more potential sequences
     private double m_dSeqMHS; // M+H mass of the unmodified peptide
     private double m_dSeqMHFailedS; // the value of M+H that failed the check_parents test last time through
     private long m_lEligibleS; // number of spectra with M+H <= the current modified peptide M+H
     private long m_lEqualsS; // number of spectra with M+H within error of the current modified peptide M+H
     private long m_lCursorS; // cursor counting the position of the N-terminal modified residue
     private long m_lFilledS; // cursor index to the most C-terminal residue modified,
    //where all residues between m_lFirstS & this one are modified
     private long m_lLastS; // cursor index to the most C-terminal residue modified in a peptide,
    // effectively the length of m_ppModsS
     private long m_lStates;
     private long m_lSizeS; // maximum peptide length
    // private long m_lSizeEqualsS; // maximum length of EqualS array
     private long[] m_plEqualsS; // indexes of spectra with M+H within error of the current modified peptide M+H
     private long[] m_piMods;
     private final List<String> m_ppModsS = new ArrayList<String>(); // pointers to the potentially modified peptide residues
     private String m_pSeqS; // the unmodified peptide sequence

/*
 * set up the state machine for the next sequence
 */

    public void initialize(final String _p, final long _s)
    {
         m_pSeqS = _p;
        m_ppModsS.clear();
        m_lStates = 0;
        m_lFilledS = 0;
        m_lCursorS = 0;
        m_bStateS = true;
        m_lEligibleS = 0;
        m_dSeqMHFailedS = 0.0;
    }

    /*
    * make sure that m_lSizeS is big enough for a peptide, length _s. if not, update array sizes
    */


/*
 * create the appropriate m_plEquals array
 */

    protected void create_equals(final long _s)
    {
     }

/*
* add a pointer to a potentially modified residue to the m_ppModsS list
*/
    int add_mod(String  _p)
    {
          m_ppModsS.add(_p);
           return m_ppModsS.size();
    }

   // static unsigned long M_lMaxModStates;


    public boolean isbStateS()
    {
        return m_bStateS;
    }

    public void setbStateS(boolean pBStateS)
    {
        m_bStateS = pBStateS;
    }

    public double getdSeqMHS()
    {
        return m_dSeqMHS;
    }

    public void setdSeqMHS(double pDSeqMHS)
    {
        m_dSeqMHS = pDSeqMHS;
    }

    public double getdSeqMHFailedS()
    {
        return m_dSeqMHFailedS;
    }

    public void setdSeqMHFailedS(double pDSeqMHFailedS)
    {
        m_dSeqMHFailedS = pDSeqMHFailedS;
    }

    public long getlEligibleS()
    {
        return m_lEligibleS;
    }

    public void setlEligibleS(long pLEligibleS)
    {
        m_lEligibleS = pLEligibleS;
    }

    public long getlEqualsS()
    {
        return m_lEqualsS;
    }

    public void setlEqualsS(long pLEqualsS)
    {
        m_lEqualsS = pLEqualsS;
    }

    public long getlCursorS()
    {
        return m_lCursorS;
    }

    public void setlCursorS(long pLCursorS)
    {
        m_lCursorS = pLCursorS;
    }

    public long getlFilledS()
    {
        return m_lFilledS;
    }

    public void setlFilledS(long pLFilledS)
    {
        m_lFilledS = pLFilledS;
    }

    public long getlLastS()
    {
        return m_lLastS;
    }

    public void setlLastS(long pLLastS)
    {
        m_lLastS = pLLastS;
    }

    public long getlStates()
    {
        return m_lStates;
    }

    public void setlStates(long pLStates)
    {
        m_lStates = pLStates;
    }

    public long getlSizeS()
    {
        return m_lSizeS;
    }

    public void setlSizeS(long pLSizeS)
    {
        m_lSizeS = pLSizeS;
    }

    public long[] getPlEqualsS()
    {
        return m_plEqualsS;
    }

    public void setPlEqualsS(long[] pPlEqualsS)
    {
        m_plEqualsS = pPlEqualsS;
    }

    public long[] getPiMods()
    {
        return m_piMods;
    }

    public void setPiMods(long[] pPiMods)
    {
        m_piMods = pPiMods;
    }

    public String getpSeqS()
    {
        return m_pSeqS;
    }

    public void setpSeqS(String pPSeqS)
    {
        m_pSeqS = pPSeqS;
    }
}
