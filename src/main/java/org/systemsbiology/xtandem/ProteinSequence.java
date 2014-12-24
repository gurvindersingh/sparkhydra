package org.systemsbiology.xtandem;

import java.util.*;

/**
 * org.systemsbiology.xtandem.ProtienSequence
 *
 * @author Steve Lewis
 * @date Dec 21, 2010
 */

/**
 * corresponds to XTandem msequence class  
 * msequence objects store information about a protein sequence. the sequence and description
 * are loaded from a list file and information about its scoring against mass spectra is
 * stored in constants and a list of domains (peptides) that have been identified.
 * NOTE: msequence.h has no corresponding .cpp file

 */
public class ProteinSequence
{
    public static ProteinSequence[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ProteinSequence.class;
    
    private int m_iRound; // the identification round that generated this sequence
    private boolean m_bForward;
    private int m_tUid; // an identification number
    private float m_fScore; // the convolution score for the protein
    private float m_fHyper; // the hyper score for the protein
    private double m_dExpect; // the expectation value for the protein
    private float m_fIntensity;
    private String m_strSeq; // the sequence of the protein in single-letter code
    private String m_strDes; // a description of the protein
    private String m_strPath; // the path name for the file that contained this sequence

//    private List<ProteinDomain>	m_vDomains =
//            new ArrayList<ProteinDomain>(); // a vector of identified domains
    private Map<String,String> m_mapMods =
            new HashMap<String,String>() ;  // a hash map containing fixed modification information


    public int getiRound()
    {
        return m_iRound;
    }

    public boolean isbForward()
    {
        return m_bForward;
    }

    public int gettUid()
    {
        return m_tUid;
    }

    public float getfScore()
    {
        return m_fScore;
    }

    public float getfHyper()
    {
        return m_fHyper;
    }

    public double getdExpect()
    {
        return m_dExpect;
    }

    public float getfIntensity()
    {
        return m_fIntensity;
    }

    public String getStrSeq()
    {
        return m_strSeq;
    }

    public String getStrDes()
    {
        return m_strDes;
    }

    public String getStrPath()
    {
        return m_strPath;
    }

//    public List<ProteinDomain> getvDomains()
//    {
//        return Collections.unmodifiableList(m_vDomains);
//    }

    public Map<String, String> getMapMods()
    {
        return Collections.unmodifiableMap(m_mapMods);
     }
}
