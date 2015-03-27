package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.ScoredScan
 * used when combining results
 * Sorts by scan 1d
 *
 * @author Steve Lewis
 * @date Jan 11, 2011
 */
public class ScoredScan extends OriginatingScoredScan   {

    //    private ITheoreticalSpectrumSet m_Theory;
    private final Map<String, ISpectralMatch> m_PeptideToMatch = new HashMap<String, ISpectralMatch>();
//    private boolean m_Dirty;

    public ScoredScan(RawPeptideScan pRaw) {
        super( pRaw);
     }

    public ScoredScan() {
 //       setDirty(true);
    }

//
//    protected boolean isDirty() {
//        return m_Dirty;
//    }
//
//    protected void setDirty(final boolean pDirty) {
//        m_Dirty = pDirty;
//        if (pDirty)
//            clearMatches();
//    }
//
//
//    protected void guaranteeNormalized() {
//        if (!isDirty())
//            return;
//        // order by descending score
//        buildMatches(m_PeptideToMatch.values());
//     //     if (m_Matches.size() > 1) {
//    //        Collections.sort(m_Matches, ISpectralMatch.SCORE_COMPARATOR);
//   //     }
//        setDirty(false);
//    }
//
    /**
     * given what is already known build a scan from a serialization string
     *
     * @param serialization
     * @param godObject
     */
    public ScoredScan(String serialization, IMainData godObject) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    /**
     * a little debugging method to track peptides we want to watch
     * usually return false
     * @param peptide
     * @return
     */
    protected boolean isPeptideInteresting(IPolypeptide peptide)  {
        if(peptide.isModified() ) {
             String s = peptide.toString();
             if(s.contains("[7"))
                 return true;
         }
         return false;
    }

    @Override
    public void addSpectralMatch(ISpectralMatch added) {
        IPolypeptide pp = added.getPeptide();
 //       if(isPeptideInteresting(pp))
 //           pp = added.getPeptide(); // break here
        String val = pp.toString();
        ISpectralMatch oldMatch = m_PeptideToMatch.get(val);
        if (oldMatch != null) {
            oldMatch.addTo(added);
        }
        else {
           oldMatch = added;
            m_PeptideToMatch.put(val, added);
        }
        super.addSpectralMatch(oldMatch);
      }

    /**
     * added de novo but not when reading fron XML
     * @param pHyperScore
     */
    @Override
    public void addHyperscore(final double pHyperScore) {
        // do nothing we already deal with these
    }


    public double getBestHyperscore() {
        if(getBestMatch() != null)
            return getBestMatch().getHyperScore();
        else
            return 0;
    }
}
