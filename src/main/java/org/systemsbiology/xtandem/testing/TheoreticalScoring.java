package org.systemsbiology.xtandem.testing;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.testing.TheoreticalScoring
 * class for scoring a single peptide against a single scan
 * User: steven
 * Date: 6/22/11
 */
public class TheoreticalScoring implements ITheoreticalScoring {
    public static final ITheoreticalScoring[] EMPTY_ARRAY = {};

    public static final Comparator<ITheoreticalScoring> K_SCORE_COMPARATOR = new KScoreComparator();

    public static final void sortByKScore(ITheoreticalScoring[] inp) {
        Arrays.sort(inp, K_SCORE_COMPARATOR);

    }


    private static class KScoreComparator implements Comparator<ITheoreticalScoring> {
        @Override
        public int compare(final ITheoreticalScoring o1, final ITheoreticalScoring o2) {
            double score1 = o1.getTotalKScore();
            double score2 = o2.getTotalKScore();
            if (score1 > score2)
                return -1;
            if (score1 < score2)
                return 1;
            return o1.compareTo(o2);
        }
    }

    private double m_TotalKScore;
    private final String m_Sequence;
    private final IScanScoring m_ScanScore;
    //   private Map<Integer, DebugMatchPeak> m_ScoringMass = new HashMap<Integer, DebugMatchPeak>();
    private Map<ScanScoringIdentifier, ITheoreticalIonsScoring> m_ScoringIons = new HashMap<ScanScoringIdentifier, ITheoreticalIonsScoring>();


    public TheoreticalScoring(final String pSequence, final IScanScoring pScanScore) {
        m_Sequence = pSequence;
        m_ScanScore = pScanScore;
    }

    @Override
    public ISpectralMatch asSpectralMatch() {
        IScanScoring scanScore = getScanScore();
        ITheoreticalIonsScoring[] ionScorings = getIonScorings();
        int charge = 0;
        IPolypeptide peptide = Polypeptide.fromString(getSequence() );
         double massPlusH = peptide.getMass() + XTandemUtilities.getProtonMass() + XTandemUtilities.getCleaveCMass() + XTandemUtilities.getCleaveNMass();
        for (int i = 0; i < ionScorings.length; i++) {
            ITheoreticalIonsScoring is = ionScorings[i];
            charge = Math.max(charge,is.getIdentifier().getCharge());
        }
        final double pScore = getTotalKScore();
        final double pHyperScore = getTotalKScore();
        final double rawScore = getTotalScore();
        IonTypeScorer scorer = buildIonScorer();
        TheoreticalSpectrumSet ts = new TheoreticalSpectrumSet(charge,massPlusH,peptide);
        for (int i = 0; i < ionScorings.length; i++) {
             ITheoreticalIonsScoring is = ionScorings[i];
             ts.setSpectrum(is.getTheoreticalSpectrum());
         }


        IExtendedSpectralMatch ret = new ExtendedSpectralMatch(peptide, getMeasuredSpectrum(),
                pScore, pHyperScore, rawScore, scorer,ts,ionScorings);
        return ret;
    }

    protected IonTypeScorer buildIonScorer()
    {
        IonUseCounter ret = new IonUseCounter();
        TheoreticalIonsScoring[] ionScorings = getIonScorings();
        for (int i = 0; i < ionScorings.length; i++) {
            TheoreticalIonsScoring is = ionScorings[i];
            IonType type = is.getType();
            double score = is.getTotalScore(type);
            int counts = is.getMatchCount();
            for (int j = 0; j < counts; j++) {
                ret.addCount(type);
            }
             ret.addScore(type,score);
        }
        return ret;
    }

    @Override
    public IMeasuredSpectrum getMeasuredSpectrum() {
        for (ITheoreticalIonsScoring ts : m_ScoringIons.values()) {
            IMeasuredSpectrum ret = ts.getMeasuredSpectrum();
            if (ret != null)
                return ret;
        }
        return null;
    }


    public void addIonScoring(ScanScoringIdentifier key, ITheoreticalIonsScoring added) {
        m_ScoringIons.put(key, added);
    }


    public void remove(ScanScoringIdentifier removed) {
        m_ScoringIons.remove(removed);
    }

    @Override
    public TheoreticalIonsScoring[] getIonScorings() {
        return m_ScoringIons.values().toArray(new TheoreticalIonsScoring[0]);
    }

    @Override
    public TheoreticalIonsScoring getIonScoring(ScanScoringIdentifier key) {

        TheoreticalIonsScoring ret = (TheoreticalIonsScoring)m_ScoringIons.get(key);
        if (ret == null) {
            ret = new TheoreticalIonsScoring(key, this);
            m_ScoringIons.put(key, ret);
        }
        return ret;
    }

    @Override
    public double getMZRatio() {
        for(ITheoreticalIonsScoring ion : m_ScoringIons.values())  {
            double mzRatio = ion.getMZRatio();
            if(mzRatio > 0)
                return mzRatio;
        }
        return 0;
    }


    @Override
    public String toString() {
        return getSequence(); // + " " + getMatchCount();    //To change body of overridden methods use File | Settings | File Templates.
    }

//    public int getMatchCount() {
//        return m_ScoringMass.size();
//    }

    /**
     * get the total score of all matches
     *
     * @return
     */
    @Override
    public double getTotalScore() {
         TheoreticalIonsScoring[] myScores = getIonScorings();
        double ret = 0;
        for (int i = 0; i < myScores.length; i++) {
            TheoreticalIonsScoring myScore = myScores[i];
            ret += myScore.getTotalScore();
        }
          return ret;
    }

    public void setTotalKScore(double score)
    {
         m_TotalKScore = score;
    }

    /**
     * get the way KScoring makes a total score
     *
     * @return
     */
    @Override
    public double getTotalKScore() {
        if(m_TotalKScore != 0)
            return m_TotalKScore;

        double totalScore = getTotalScore();
        return TandemKScoringAlgorithm.conditionRawScore(totalScore, getSequence());
    }


    /**
     * get a total for matches of a specific ion type
     *
     * @param type !null type
     * @return as above
     */
    @Override
    public double getTotalScore(IonType type) {
        TheoreticalIonsScoring[] myScores = getIonScorings();
        double ret = 0;
        for (int i = 0; i < myScores.length; i++) {
            TheoreticalIonsScoring myScore = myScores[i];
            ret += myScore.getTotalScore(type);
        }
        return ret;
    }


    /**
     * return true if this and o are 'close enough'
     *
     * @param o !null test object
     * @return as above
     */
    @Override
    public boolean equivalent(final ITheoreticalScoring o) {
        //     if (getMatchCount() != o.getMatchCount())
        //         return false;
//        DebugMatchPeak[] myScores = getScoringMasses();
//        DebugMatchPeak[] theirScores = o.getScoringMasses();
//        for (int i = 0; i < theirScores.length; i++) {
//            DebugMatchPeak myScore = myScores[i];
//            DebugMatchPeak theirScore = theirScores[i];
//            if (!myScore.equivalent(theirScore))
//                return false;
//
//        }
        ITheoreticalIonsScoring[] ionScorings = this.getIonScorings();
        ITheoreticalIonsScoring[] ionScorings2 = o.getIonScorings();
        if (ionScorings.length != ionScorings2.length)
            return false;
        for (int i = 0; i < ionScorings2.length; i++) {
            ITheoreticalIonsScoring ts = ionScorings[i];
            if (!ts.equivalent(ionScorings2[i]))
                return false;
        }
        return true;
    }


    @Override
    public int compareTo(final ITheoreticalScoring o) {
        return getSequence().compareTo(o.getSequence());
    }
//
//    public void addScoringMass(DebugMatchPeak dp) {
//        m_ScoringMass.put(dp.getMass(), dp);
//    }
//
//
//    public DebugMatchPeak[] getScoringMasses() {
//        return m_ScoringMass.values().toArray(new DebugMatchPeak[0]);
//    }
//
//    public DebugMatchPeak getScoringMass(Integer key) {
//        return m_ScoringMass.get(key);
//    }

    @Override
    public String getSequence() {
        return m_Sequence;
    }

    @Override
    public IScanScoring getScanScore() {
        return m_ScanScore;
    }
}
