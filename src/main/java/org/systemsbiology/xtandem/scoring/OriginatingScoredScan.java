package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.sax.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.testing.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.OriginatingScoredScan
 * User: steven
 * Date: 12/5/11
 */
public class OriginatingScoredScan implements IScoredScan, IAddable<IScoredScan>, IEquivalent<IScoredScan> {
    public static final int MAX_SERIALIZED_MATCHED = 8;
    public static final String TAG = "score";

    public static final Comparator<IScoredScan> ID_COMPARISON = new Comparator<IScoredScan>() {
        @Override
        public int compare(IScoredScan o1, IScoredScan o2) {
            if (o1.equals(o2))
                return 0;
            return o1.getId().compareTo(o2.getId());
        }
    };

    public static final String DEFAULT_VERSION = "1.0";
    public static final String DEFAULT_ALGORITHM = TandemKScoringAlgorithm.ALGORITHM_NAME;
    /**
     * It is expensice to compute expected value and until we are at the end of computations not worth reporting
     */
    private static boolean gReportExpectedValue = false;
    private IMeasuredSpectrum m_Raw;
    private double m_NormalizationFactor = 1;
    private final IonUseScore m_IonUse = new IonUseCounter();
    private IMeasuredSpectrum m_NormalizedRawScan;
    private IMeasuredSpectrum m_ConditionedScan;
    //    private List<ISpectralMatch> m_Matches;
    private BoundedMatchSet m_Matches = new BoundedMatchSet();
    private final HyperScoreStatistics m_HyperScores = new HyperScoreStatistics();
    protected final VariableStatistics m_ScoreStatistics = new VariableStatistics();
    private double m_ExpectedValue = Double.NaN;
    private String m_Version = DEFAULT_VERSION;
    private String m_Algorithm = DEFAULT_ALGORITHM;


    public OriginatingScoredScan(IMeasuredSpectrum pRaw) {
        this();
        m_Raw = pRaw;
    }

    public OriginatingScoredScan() {
    }

    /**
     * return algorithm name
     *
     * @return as above
     */
    @Override
    public String getAlgorithm() {
        return m_Algorithm;
    }

    public void setAlgorithm(final String pAlgorithm) {
        if (m_Algorithm != null && m_Algorithm != DEFAULT_ALGORITHM)
            throw new IllegalStateException("algorithm cannot be reset from " + m_Algorithm + " to " + pAlgorithm);
        m_Algorithm = pAlgorithm;
    }

    @Override
    public String getVersion() {
        return m_Version;
    }

    public void setVersion(final String pVersion) {
        if (pVersion != null)
            m_Version = pVersion;
    }

    public void clearMatches() {
        m_Matches = null;
    }

    public void buildMatches(Collection<ISpectralMatch> matches) {
        m_Matches = new BoundedMatchSet(matches);
    }

    public int getMatchCount() {
        return m_Matches.size();
    }

    public double getNormalizationFactor() {
        return m_NormalizationFactor;
    }

    public void setNormalizationFactor(final double pNormalizationFactor) {
        m_NormalizationFactor = pNormalizationFactor;
    }

    public IonUseScore getIonUse() {
        return m_IonUse;
    }


    public BoundedMatchSet getMatches() {
        return m_Matches;
    }

    public static boolean isReportExpectedValue() {
        return gReportExpectedValue;
    }

    public static void setReportExpectedValue(final boolean pReportExpectedValue) {
        gReportExpectedValue = pReportExpectedValue;
    }


    @Override
    public double getMassDifferenceFromBest() {
        final ISpectralMatch bestMatch = getBestMatch();
        final double mass = getMassPlusHydrogen();
        double pm = bestMatch.getPeptide().getMatchingMass();
        double del = Math.abs(pm - mass);

        return del;
    }

    /**
     * get the total peaks matched for all ion types
     *
     * @return
     */
    @Override
    public int getNumberMatchedPeaks() {
        return getIonUse().getNumberMatchedPeaks();
    }

    /**
     * get the score for a given ion type
     *
     * @param type !null iontype
     * @return score for that type
     */
    @Override
    public double getScore(IonType type) {
        return getIonUse().getScore(type);
    }

    /**
     * get the count for a given ion type
     *
     * @param type !null iontype
     * @return count for that type
     */
    @Override
    public int getCount(IonType type) {
        return getIonUse().getCount(type);
    }


    /**
     * @return
     */
    public boolean isValid() {
        final IMeasuredSpectrum raw = getRaw();
        if (raw == null)
            return false;
//        if (raw.getPrecursorMassChargeRatio() == null)
//            return false;
        if (raw.getPeaksCount() < 8) // 20)  // todo make it right
            return false;
        if (getConditionedScan() == null) {
            return false; // we have already tried and failed to condition the scan
        }
        return true;
    }

    public ISpectralMatch[] getSpectralMatches() {
        guaranteeNormalized();
        ISpectralMatch[] ret = m_Matches.getMatches();
        Arrays.sort(ret);
        return ret;
    }

    /**
     * true if some match is scored
     *
     * @return as above
     */
    @Override
    public boolean isMatchPresent() {
        return m_Matches.getMatches().length == 0;
    }

    /**
     * return true if a mass such as that of a throretical peak is
     * within the range to scpre
     *
     * @param mass positive testMass
     * @return as above
     */
    public boolean isMassWithinRange(double mass, int charge, IScoringAlgorithm scorer) {
        final IMeasuredSpectrum raw = getRaw();
        ScanPrecursorMz mz = new ScanPrecursorMz(1, raw.getPrecursorCharge(), raw.getPrecursorMassChargeRatio(), FragmentationMethod.ECD);
        return mz.isMassWithinRange(mass, charge, scorer);
    }


    /**
     * return the scan identifier
     *
     * @return as above
     */
    @Override
    public String getId() {
        IMeasuredSpectrum raw = getRaw();
        if (raw == null)
            return null;
        return raw.getId();
    }


    @Override
    public IMeasuredSpectrum getRaw() {
        return m_Raw;
    }

    @Override
    public String getRetentionTimeString() {
        IMeasuredSpectrum raw = getRaw();
        if (raw instanceof RawPeptideScan) {
            RawPeptideScan rawPeptideScan = (RawPeptideScan) raw;
            return rawPeptideScan.getRetentionTime();
        }

        return null;
    }


    /**
     * rention time as a seconds
     *
     * @return possibly null 0
     */
    @Override
    public double getRetentionTime() {
        String str = getRetentionTimeString();
        if (str != null) {
            str = str.replace("S", "");   // handle PT5.5898S"
            str = str.replace("PT", "");
            str = str.trim();
            if (str.length() > 0) {
                try {
                    double ret = Double.parseDouble(str);
                    return ret;
                }
                catch (NumberFormatException e) {
                    return 0;
                }
            }
        }
        return 0;
    }

    @Override
    public IMeasuredSpectrum getConditionedScan() {
        return m_ConditionedScan;
    }

    public void setConditionedScan(final IMeasuredSpectrum pConditionedScan) {
        m_ConditionedScan = pConditionedScan;
    }

    @Override
    public IMeasuredSpectrum conditionScan(IScoringAlgorithm alg, final SpectrumCondition sc) {
        if (m_ConditionedScan == null) {
            m_ConditionedScan = alg.conditionSpectrum(this, sc);
        }
        return m_ConditionedScan;
    }

    @Override
    public HyperScoreStatistics getHyperScores() {
        guaranteeNormalized();
        return m_HyperScores;
//        HyperScoreStatistics ret = new HyperScoreStatistics();
//        double[] items = new double[m_Matches.size()];
//        int index = 0;
//        for (ISpectralMatch m : m_Matches) {
//            double score = m.getHyperScore();
//            items[index++] = score;
//            ret.add(score);
//        }
//        Arrays.sort(items); // debug scores
//        return ret;
    }

    @Override
    public VariableStatistics getScoreStatistics() {
        guaranteeNormalized();
//        throw new UnsupportedOperationException("Fix This"); // ToDo
        return m_ScoreStatistics;
    }


    //    @Override
    public ISpectralMatch getBestMatch() {
        guaranteeNormalized();
        return m_Matches.getBest();
    }


    //      @Override
    //    public ISpectralMatch getBestMatch() {
    //        guaranteeNormalized();
//        double bestScore = Double.MIN_VALUE;
//        ISpectralMatch ret = null;
//        for (ISpectralMatch test : m_Matches) {
//            double hyperScore = test.getHyperScore();
//            if (hyperScore > bestScore) {
//                bestScore = hyperScore;
//                ret = test;
//            }
//        }
//        return ret;
//    }

//    public void setBestMatch(ISpectralMatch newBest) {
//        m_BestMatch = newBest;
//        m_ExpectedValue = 0;
//    }

    @Override
    public ISpectralMatch getNextBestMatch() {
        guaranteeNormalized();
        // m_Matches is now sorted
        if (m_Matches.size() < 2)
            return null;
        return m_Matches.getNextbest();
    }


    /**
     * key may be id: charge
     *
     * @param in
     * @return
     */
    public static int idFromKey(String in) {
        if (in.contains(":")) {
            return Integer.parseInt(in.substring(0, in.indexOf(":")));
        }
        else {
            return Integer.parseInt(in);
        }
    }

    public void setRaw(final RawPeptideScan pRaw) {
        m_Raw = pRaw;
    }

    @Override
    public int getNumberScoredPeptides() {
        guaranteeNormalized();
        return m_Matches.size();
    }

    @Override
    public void setExpectedValue(final double pExpectedValue) {
        m_ExpectedValue = pExpectedValue;
    }

//    /**
//     * return true if a mass such as that of a throretical peak is
//     * within the range to scpre
//     *
//     * @param mass positive testMass
//     * @return as above
//     */
//    public boolean isMassWithinRange(double mass) {
//        final RawPeptideScan raw = getRaw();
//        return raw.isMassWithinRange(mass);
//    }

    public static final int ID_LENGTH = 12;

    public String getKey() {
        String id = getId();
        String s = id + ":" + this.getCharge();
        while (s.length() < ID_LENGTH)
            s = "0" + s; // this allows better alphabetical sort
        return s;
    }

    /**
     * combine two scores
     *
     * @param added
     */
    public void addTo(IScoredScan added) {
        if (!added.getId().equals(getId()))
            throw new IllegalArgumentException("incompatable scan");

        ISpectralMatch newMatch = added.getBestMatch();
        ISpectralMatch myBest = getBestMatch();
//        if (myBest == null) {
//            setBestMatch(newMatch);
//            setExpectedValue(added.getExpectedValue());
//        }
//        else {
//            if (newMatch != null && newMatch.getHyperScore() > myBest.getHyperScore())
//                setBestMatch(newMatch);
//            setExpectedValue(added.getExpectedValue());
//        }
//
        HyperScoreStatistics hyperScores = added.getHyperScores();
        getHyperScores().add(hyperScores);
        VariableStatistics scoreStatistics = added.getScoreStatistics();
        getScoreStatistics().add(scoreStatistics);
//         setNumberScoredPeptides(getNumberScoredPeptides() + added.getNumberScoredPeptides());
        ISpectralMatch[] sms = added.getSpectralMatches();
        for (int i = 0; i < sms.length; i++) {
            ISpectralMatch sm = sms[i];
            addSpectralMatch(sm);
        }
    }
//
//    public double lowestHyperscoreToAdd() {
//        if (!isAtMaxCapacity())
//            return Double.MIN_VALUE;
//        return m_Matches.last().getHyperScore();
//    }
//
//
//    public boolean isAtMaxCapacity() {
//        return m_Matches.size() == m_Matches.getMaxItems();
//    }
//
//
//    public int getMaxItems() {
//        return m_Matches.getMaxItems();
//    }

    /**
     * in this version there is no overlap
     *
     * @param added
     */
    public void addSpectralMatch(ISpectralMatch added) {
//        double hyperScore = added.getHyperScore();
//        if (hyperScore <= lowestHyperscoreToAdd())
//            return; // drop unscored matches

        // Test for add with wrong spectral id
        ISpectralMatch bestMatch = getBestMatch();
        if (bestMatch != null) {
            String originalId = bestMatch.getMeasured().getId();
            String matchId = added.getMeasured().getId();
            if (originalId != null && !originalId.equals(matchId))
                throw new IllegalStateException("Trying to add " + matchId + " to scores from " + originalId);

        }

        m_Matches.addMatch(added);
    }

    /**
     * added de novo but not when reading fron XML
     *
     * @param pHyperScore
     */
    public void addHyperscore(final double pHyperScore) {
        HyperScoreStatistics hyperScores = getHyperScores();
        hyperScores.add(pHyperScore);       // if reading xml we already added the hyperscore
    }


    protected void guaranteeNormalized() {
        // nothing here this class is always normalized
    }

    public IMeasuredSpectrum getNormalizedRawScan() {
        if (m_NormalizedRawScan == null)
            m_NormalizedRawScan = getRaw();
        return m_NormalizedRawScan;
    }

    public void setNormalizedRawScan(IMeasuredSpectrum pNormalizedRawScan) {
        m_NormalizedRawScan = pNormalizedRawScan;
    }

    /**
     * return the base ion charge
     *
     * @return as above
     */
    @Override
    public int getCharge() {
        return getRaw().getPrecursorCharge();
    }

    /**
     * return the base mass plus  the mass of a proton
     *
     * @return as above
     */
    @Override
    public double getMassPlusHydrogen() {
        IMeasuredSpectrum ns = getNormalizedRawScan();
        final double mass = ns.getPrecursorMass();
        return mass;
    }

    /**
     * return
     *
     * @return as above
     */
    @Override
    public double getExpectedValue() {
        HyperScoreStatistics hyperScores = getHyperScores();
        if (m_ExpectedValue != 0 && !Double.isNaN(m_ExpectedValue))
            return m_ExpectedValue;
        ISpectralMatch bestMatch = getBestMatch();
        if (!hyperScores.isEmpty()) {
            if (bestMatch == null)
                return 1.0; // should not happen
            double hyperScore = bestMatch.getHyperScore();
            double expectedValue = hyperScores.getExpectedValue(hyperScore);
            return expectedValue;    //  delegate the work
            // when we have not set this (typical case) we get it from the hyperscores
        }
        return m_ExpectedValue;
    }

    @Override
    public double getSumIntensity() {
        final double factor = getNormalizationFactor();
        final IMeasuredSpectrum spectrum = getNormalizedRawScan();
        final double sumPeak = XTandemUtilities.getSumPeaks(spectrum) / factor;
        return sumPeak;
    }

    @Override
    public double getMaxIntensity() {
        final double factor = getNormalizationFactor();
        final IMeasuredSpectrum spectrum = getNormalizedRawScan();
        final double sumPeak = XTandemUtilities.getMaxPeak(spectrum) / factor;
        return sumPeak;
    }

    @Override
    public double getFI() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    @Override
    public String toString() {
        return "scan " + getId() + " charge " + getCharge() +
                "  precursorMass " + getMassPlusHydrogen();

    }

    @Override
    public int compareTo(final IScoredScan o) {
        if (o == this)
            return 0;
        return getId().compareTo(o.getId());

    }

    public boolean equivalent(IScoredScan scan) {
        if (scan == this)
            return true;
        if (getCharge() != scan.getCharge())
            return false;
        if (!getVersion().equals(scan.getVersion()))
            return false;
        XMLUtilities.outputLine("For Now forgiving expected value differences");
        //    if (!XTandemUtilities.equivalentDouble(getExpectedValue(), scan.getExpectedValue()))
        //        return false;
        final IMeasuredSpectrum raw1 = getRaw();
        final IMeasuredSpectrum raw2 = scan.getRaw();
        if (!raw1.equivalent(raw2))
            return false;
        final ISpectralMatch[] sm1 = getSpectralMatches();
        final ISpectralMatch[] sm2 = scan.getSpectralMatches();
        if (sm1.length != sm2.length)
            return false;
        for (int i = 0; i < sm2.length; i++) {
            ISpectralMatch m1 = sm1[0];
            ISpectralMatch m2 = sm2[i];
            // really just look
            if (m2.getPeptide().equivalent(m1.getPeptide())) {
                double score1 = m1.getScore();
                double score2 = m2.getScore();

                double rescore1 = 0;
                double rescore2 = 0;
                ITheoreticalIonsScoring[] is1 = ((ExtendedSpectralMatch) m1).getIonScoring();
                ITheoreticalIonsScoring[] is2 = ((ExtendedSpectralMatch) m2).getIonScoring();
                if (is1.length != is2.length)
                    return false;
                for (int j = 0; j < is2.length; j++) {
                    ITheoreticalIonsScoring ts1 = is1[j];
                    ITheoreticalIonsScoring ts2 = is2[j];
                    DebugMatchPeak[] scm1 = ts1.getScoringMasses();
                    DebugMatchPeak[] scm2 = ts2.getScoringMasses();
                    for (int k = 0; k < scm2.length; k++) {
                        DebugMatchPeak dm1 = scm1[k];
                        rescore1 += dm1.getAdded();
                        DebugMatchPeak dm2 = scm2[k];
                        rescore2 += dm2.getAdded();
                        if (!dm1.equivalent(dm2))
                            throw new IllegalStateException("problem"); // ToDo change
                    }
                }
                double diff = rescore1 - rescore2;

            }
        }
        for (int i = 0; i < sm2.length; i++) {
            ISpectralMatch m1 = sm1[i];
            ISpectralMatch m2 = sm2[i];
            if (!m1.equivalent(m2))
                return m1.equivalent(m2);
        }
        return true;
    }

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    public void serializeAsString(IXMLAppender adder) {
        guaranteeNormalized(); // make sort order correct

        int indent = 0;
        String tag = TAG;
        adder.openTag(tag);
        adder.appendAttribute("id", getId());
        adder.appendAttribute("version", getVersion());
        adder.appendAttribute("algorithm ", getAlgorithm());
        adder.appendAttribute("charge", getCharge());
        if (isReportExpectedValue()) {
            double expectedValue = getExpectedValue();
            adder.appendAttribute("expectedValue", expectedValue);
        }
        adder.appendAttribute("numberScoredPeptides", getNumberScoredPeptides());
        adder.endTag();
        adder.cr();

        final IMeasuredSpectrum raw = getRaw();
        if (raw != null)
            raw.serializeAsString(adder);
        final IMeasuredSpectrum conditioned = getConditionedScan();
        if (conditioned != null) {
            adder.openEmptyTag("ConditionedScan");
            adder.cr();
            conditioned.serializeAsString(adder);
            adder.closeTag("ConditionedScan");
        }
        IMeasuredSpectrum scan = getNormalizedRawScan();
        if (scan != null) {
            adder.openEmptyTag("NormalizedRawScan");
            adder.cr();
            scan.serializeAsString(adder);
            adder.closeTag("NormalizedRawScan");
        }
        HyperScoreStatistics hyperScores = getHyperScores();
        if (!hyperScores.isEmpty()) {
            adder.openEmptyTag("HyperScoreStatistics");
            hyperScores.serializeAsString(adder);
            adder.closeTag("HyperScoreStatistics");

        }

        final ISpectralMatch[] matches = getSpectralMatches();
        Arrays.sort(matches);

        for (int i = 0; i < Math.min(matches.length, MAX_SERIALIZED_MATCHED); i++) {
            ISpectralMatch match = matches[i];
            match.serializeAsString(adder);
        }
        adder.closeTag(tag);

    }

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(IonTypeScorer test) {
        if (test == this)
            return true;


        for (IonType type : IonType.values()) {
            if (getCount(type) != test.getCount(type))
                return false;
            if (!XTandemUtilities.equivalentDouble(getScore(type), test.getScore(type)))
                return false;

        }
        return true;
    }
}
