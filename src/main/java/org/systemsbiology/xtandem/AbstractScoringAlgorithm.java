package org.systemsbiology.xtandem;

import com.lordjoe.distributed.hydra.test.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.testing.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.AbstractScoringAlgorithm
 * User: Steve
 * Date: 1/20/12
 */
public abstract class AbstractScoringAlgorithm implements ITandemScoringAlgorithm {

    /**
     * fast lookup table for factorial
     */
    public static double[] FACTORIALS = {
            1, // 0
            1, // 1
            2, // 2
            6, // 3
            24, // 4
            120, // 5
            720, // 6
            5040, // 7
            40320, // 8
            362880, // 9
            3628800, // 10
            39916800, // 11
            479001600,  // 12
            6227020800.0,  // 13
            87178291200.0,  // 14
            1307674368000.0,  // 15
    };

    /**
     * return n! - usually does a lookup
     *
     * @param n positive n
     * @return n!
     */
    public static double factorial(int n) {
        try {
            //  fast version is to lookup
            // throws an exception for n > 16
            return FACTORIALS[n];
        }
        catch (IndexOutOfBoundsException e) {
            return generateFactorial(n);

        }
    }

    public static double generateFactorial(final int n) {
        if (n < 2)
            return 1;
        // for large cases compute
        double ret = 1;
        for (int i = 2; i <= n; i++) {
            ret *= i;
        }
        return ret;
    }


    public static final AbstractScoringAlgorithm[] EMPTY_ARRAY = {};
    public static final int MAX_MASS = 10000;
    protected MassType m_MassType = MassType.average;
    private boolean m_SemiTryptic;
    private double m_PlusLimit = 4.1;
    private double m_MinusLimit = 2.1;
    //    private boolean m_ScoringXIons;
//    private boolean m_ScoringYIons;
//    private boolean m_ScoringAIons;
//    private boolean m_ScoringBIons;
//    private boolean m_ScoringCIons;
//    protected boolean m_ScoringCyclicPermutation;
//    protected boolean m_ScoringIncludeReverse;
    //   protected boolean m_RefineSpectrumSynthesis;
    // in c code is m_ferr
    protected float m_SpectrumMassError;

    /**
     * return a unique algorithm name
     *
     * @return
     */
    @Override
    public String getName() {
        return "Tandem";
    }

    @Override
    public MassType getMassType() {
        return m_MassType;
    }

    /**
     * return false if the algorithm will not score the spectrum
     *
     * @param !null spectrum measured
     * @return as above
     */
    public boolean canScore(IMeasuredSpectrum measured) {
        if (measured.getPeaksCount() == 0)
            return false;
        return true; // override if some spectra are not scored
    }


    public boolean isSemiTryptic() {
        return m_SemiTryptic;
    }

    public void setSemiTryptic(final boolean pSemiTryptic) {
        m_SemiTryptic = pSemiTryptic;
    }

    public double getPlusLimit() {
        return m_PlusLimit;
    }

    public void setPlusLimit(final double pPlusLimit) {
        m_PlusLimit = pPlusLimit;
    }

    public double getMinusLimit() {
        return m_MinusLimit;
    }

    public void setMinusLimit(final double pMinusLimit) {
        m_MinusLimit = pMinusLimit;
    }

    //    @Override
//    public boolean isScoringXIons()
//    {
//        return m_ScoringXIons;
//    }
//
//    public void setScoringXIons(boolean pScoringXIons)
//    {
//        m_ScoringXIons = pScoringXIons;
//    }
//
//    @Override
//    public boolean isScoringYIons()
//    {
//        return m_ScoringYIons;
//    }
//
//    public void setScoringYIons(boolean pScoringYIons)
//    {
//        m_ScoringYIons = pScoringYIons;
//    }
//
//    @Override
//    public boolean isScoringAIons()
//    {
//        return m_ScoringAIons;
//    }
//
//    public void setScoringAIons(boolean pScoringAIons)
//    {
//        m_ScoringAIons = pScoringAIons;
//    }
//
//    @Override
//    public boolean isScoringBIons()
//    {
//        return m_ScoringBIons;
//    }
//
//    public void setScoringBIons(boolean pScoringBIons)
//    {
//        m_ScoringBIons = pScoringBIons;
//    }
//
//    @Override
//    public boolean isScoringCIons()
//    {
//        return m_ScoringCIons;
//    }
//
//    public void setScoringCIons(boolean pScoringCIons)
//    {
//        m_ScoringCIons = pScoringCIons;
//    }

    @Override
    public float getSpectrumMassError() {
        return m_SpectrumMassError;
    }

    public void setAcceptableMassLimits(double plusLimit, double minusLimit) {
        m_PlusLimit = plusLimit;
        m_MinusLimit = minusLimit;
    }

    /**
     * true if scanMass within mass2 + gPlusLimit mass2 - gMinusLimit
     *
     * @param scanMass test scanMass
     * @param mass2
     * @return
     */
    @Override
    public boolean isWithinLimits(double scanMass, double mass2, int charge) {
        double del = scanMass - mass2;
        if (charge > 1)
            del /= charge;

        if (Math.abs(del) < 30)
            XTandemUtilities.breakHere();

        boolean ret = false;
        //       XTandemUtilities.workUntil(2012,5,3);
        // Note looks backwards but this is what they do
        if (del > 0)
            ret = del <= Math.abs(m_PlusLimit);
        else
            ret = -del <= Math.abs(m_MinusLimit);

//        // Note looks backwards but this is what they do
//        if (del > 0)
//            ret = del <=  Math.abs(m_MinusLimit);
//        else
//            ret = -del <= Math.abs(m_PlusLimit);

        if (!ret)
            return false;
        return ret; // break here interesting result
    }

//    /**
//     * return the low and high limits of a mass scan
//     *
//     * @param scanMass
//     * @return as above
//     */
//    @Override
//    public   int[] highAndLowMassLimits(double scanMass) {
//
//        int[] ret = new int[2];
//        // note this only looks backwards SLewis
//        ret[0] = (int) (scanMass - m_PlusLimit);
//        ret[1] = (int) (0.999999 + scanMass + m_MinusLimit);
//
//        return ret; // break here interesting result
//    }

    /**
     * return the low and high limits of a mass scan
     *
     * @param scanMass
     * @return as above
     */
    @Override
    public int[] allSearchedMasses(double scanMass) {
        int[] highAndLow = highAndLowMassLimits(scanMass);
        int low = highAndLow[0];
        int high = highAndLow[1];
        int numberItems = high - low;
        int[] ret = new int[numberItems];
        for (int i = 0; i < numberItems; i++) {
            ret[i] = low + i;
        }
        return ret;
    }

    /**
     * test for acceptability and generate a new conditioned spectrum for
     * scoring
     *
     * @param in !null spectrum
     * @return null if the spectrum is to be ignored otherwise a conditioned spectrum
     */
    public IMeasuredSpectrum conditionSpectrum(IScoredScan in, final SpectrumCondition sc) {
        return sc.conditionSpectrum(in, 200);
    }


    /**
     * alter the score from dot_product in algorithm dependent manner
     *
     * @param score    old score
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @param counter  !null use counter
     * @return new score
     */
    @Override
    public double conditionScore(double score, IMeasuredSpectrum measured,
                                 ITheoreticalSpectrumSet theory, IonUseScore counter) {
        return score;
    }

    /**
     * find the hyperscore the score from dot_product in algorithm depdndent manner
     *
     * @param score    old score
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @param counter  !null use counter
     * @return new score
     */
    @Override
    public double buildHyperscoreScore(double score, IMeasuredSpectrum measured,
                                       ITheoreticalSpectrumSet theory, IonUseScore counter) {
        double factor = 1;
        for (IonType type : IonType.values()) {
            final int count = counter.getCount(type);
            double df = factorial(count);
            factor *= df;
        }
        return score * factor;
    }

    /**
     * return the expected value for the best score
     *
     * @param scan !null scan
     * @return as above
     */
    @Override
    public double getExpectedValue(IScoredScan scan) {
        return scan.getExpectedValue();
    }


    /**
     * use the parameters to configure local properties
     *
     * @param !null params
     */
    @Override
    public void configure(final IParameterHolder params) {

        // define the limits allowed for mass error
        double plusMassDifferenceAllowed = params.getDoubleParameter(
                "spectrum, parent monoisotopic mass error plus", 4.1);
        // allow 100 ppm error
        double minusMassDifferenceAllowed = params.getDoubleParameter(
                "spectrum, parent monoisotopic mass error minus", 2.1);

        final String units = params.getParameter("spectrum, parent monoisotopic mass error units",
                "Daltons");
        //    if (!"Daltons".equals(units))
        //        throw new IllegalStateException("We can only deal with Daltons at this time");
        // put then where anyone can get to them
        setAcceptableMassLimits(plusMassDifferenceAllowed,
                minusMassDifferenceAllowed);
        // todo which way is this???
//        setAcceptableMassLimits(minusMassDifferenceAllowed,
//                plusMassDifferenceAllowed);

    }


//    @Override
//    public boolean isScoringCyclicPermutation() {
//        return false;
//    }
//
//    @Override
//    public boolean isScoringIncludeReverse() {
//        return false;
//    }
//
//    @Override
//    public boolean isRefineSpectrumSynthesis() {
//        return false;
//    }
//
//    @Override
//    public float getSpectrumMonoIsotopicMassError() {
//        return 0;
//    }
//
//    @Override
//    public float getSpectrumMonoIsotopicMassErrorMinus() {
//        return 0;
//    }
//
//    @Override
//    public float getSpectrumHomologyError() {
//        return 0;
//    }

    /**
     * return the product of the factorials of the counts
     *
     * @param counter - !null holding counts
     * @return as above
     */
    @Override
    public double getCountFactor(final IonUseScore counter) {
        return 0;
    }

    /**
     * score the two spectra
     *
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @return value of the score
     */
    @Override
    public abstract double scoreSpectrum(final IMeasuredSpectrum measured, final ITheoreticalSpectrum theory, final Object... otherdata);


    /**
     * an algorithm may choose not to score a petide - for example high resolution algorithms may
     * choose not to score ppetides too far away
     *
     * @param ts    !null peptide spectrum
     * @param pScan !null scan to score
     * @return true if scoring is desired
     */
    @Override
    public boolean isTheoreticalSpectrumScored(ITheoreticalSpectrum ts, IMeasuredSpectrum pScan) {
        return true; // override for better filtering
    }


    /**
     * actually do the scorring
     *
     * @param scorer !null scorrer
     * @param scan   !null scan to score
     * @param pPps   !null set of peptides ot score
     * @return !null score
     */
    @Override
    public IScoredScan handleScan(final Scorer scorer, final IMeasuredSpectrum scan, final IPolypeptide[] pPps, ITheoreticalSpectrumSet[] tss) {
        String id = scan.getId();
//        for (int i = 0; i < pPps.length; i++) {
//            IPolypeptide pp = pPps[i];
//            System.out.println("      \"" + pp + "\"," + " //" + id);
//        }
        OriginatingScoredScan scoring = new OriginatingScoredScan(scan);
        scoring.setAlgorithm(getName());
        IonUseCounter counter = new IonUseCounter();

        int numberDotProducts = scoreScan(scorer, counter, tss, scoring);
        return scoring;
    }

    /**
     * fill bins - by default these are global but they do not have to be
     *
     * @param peaks
     */
    protected void fillSpectrumPeaks(final IMeasuredSpectrum pScan) {
        ISpectrumPeak[] peaks = pScan.getPeaks();
        Arrays.fill(Scorer.PEAKS_BY_MASS, 0);
        // build list of peaks as ints  with the index being the imass
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            int imass = TandemKScoringAlgorithm.massChargeRatioAsInt(peak);
            Scorer.PEAKS_BY_MASS[imass] += peak.getPeak();
        }
    }

    protected int scoreOnePeptide(final IonUseCounter pCounter, final IScoredScan pConditionedScan, final IMeasuredSpectrum pScan, final double[] pPeaksByMass, final int pPrecursorCharge, final ITheoreticalSpectrumSet pTsSet,boolean logCalculations) {
        double oldscore = 0;
        int numberScored = 0;

        Boolean doLogging = logCalculations; // this allows this to be passd to the ... clause in dot_product


        OriginatingScoredScan conditionedScan = (OriginatingScoredScan) pConditionedScan;

        Map<ScanScoringIdentifier, ITheoreticalIonsScoring> ScoringIons = new HashMap<ScanScoringIdentifier, ITheoreticalIonsScoring>();


        final ITheoreticalSpectrum[] spectrums = pTsSet.getSpectra();
        int numberSpectra = spectrums.length;
        if (numberSpectra == 0)
            return 0;
        String sequence = spectrums[0].getPeptide().getSequence();
        pCounter.clear();
        final int maxCharge = pTsSet.getMaxCharge();

        String scanid = pScan.getId();

        DebugDotProduct logDotProductB = null;
        DebugDotProduct logDotProductY = null;

        SpectralPeakUsage usage = new SpectralPeakUsage();
        for (int i = 0; i < numberSpectra; i++) {
            ITheoreticalSpectrum ts = spectrums[i];

            //   double lowestScoreToAdd = conditionedScan.lowestHyperscoreToAdd();

            final int charge = ts.getCharge();
            if (pPrecursorCharge != 0 && charge > pPrecursorCharge)
                continue;
            //     JXTandemLauncher.logMessage(scanid + "\t" + sequence + "\t" + charge);
            if (maxCharge <= charge) // if (maxCharge <= charge)  // do NOT score the maximum charge
                continue;

            if (!isTheoreticalSpectrumScored(ts, pScan))
                continue;

            List<DebugMatchPeak> holder = new ArrayList<DebugMatchPeak>();

            // filter the theoretical peaks
            ITheoreticalSpectrum scoredScan = buildScoredScan(ts);

            XTandemUtilities.showProgress(i, 10);

            // handleDebugging(pScan, logDotProductB, logDotProductY, ts, scoredScan);

            double dot_product = 0;
            dot_product = dot_product(pScan, scoredScan, pCounter, holder, pPeaksByMass, usage,doLogging);

            if (dot_product == 0)
                continue; // really nothing further to do
            oldscore += dot_product;
            numberScored++;


            final IonUseScore useScore1 = new IonUseScore(pCounter);
            for (DebugMatchPeak peak : holder) {
                ScanScoringIdentifier key = null;

                switch (peak.getType()) {
                    case B:
                        key = new ScanScoringIdentifier(sequence, charge, IonType.B);
                        if (XTandemDebugging.isDebugging()) {
                            if (logDotProductB != null)
                                logDotProductB.addMatchedPeaks(peak);
                        }
                        break;

                    case Y:
                        key = new ScanScoringIdentifier(sequence, charge, IonType.Y);
                        if (XTandemDebugging.isDebugging()) {
                            if (logDotProductY != null)
                                logDotProductY.addMatchedPeaks(peak);
                        }
                        break;
                }
                TheoreticalIonsScoring tsi = (TheoreticalIonsScoring) ScoringIons.get(key);
                if (tsi == null) {
                    tsi = new TheoreticalIonsScoring(key, null);
                    ScoringIons.put(key, tsi);
                }
                tsi.addScoringMass(peak);


            }
            if (XTandemDebugging.isDebugging()) {
                if (logDotProductB != null)
                    logDotProductB.setScore(useScore1);
                if (logDotProductY != null)
                    logDotProductY.setScore(useScore1);
            }


            if (oldscore == 0)
                continue;

        }

//        if (LOG_INTERMEDIATE_RESULTS)
//            XTandemUtilities.breakHere();

        double score = conditionScore(oldscore, pScan, pTsSet, pCounter);
        String algorithm = getName();
        if (score <= 0)
            return 0; // nothing to do
        double hyperscore = buildHyperscoreScore(score, pScan, pTsSet, pCounter);

        final IonUseScore useScore = new IonUseScore(pCounter);

        ITheoreticalIonsScoring[] ionMatches = ScoringIons.values().toArray(ITheoreticalIonsScoring.EMPTY_ARRAY);
        IExtendedSpectralMatch match = new ExtendedSpectralMatch(pTsSet.getPeptide(), pScan, score, hyperscore, oldscore,
                useScore, pTsSet, ionMatches);

        SpectralPeakUsage matchUsage = match.getUsage();
        matchUsage.addTo(usage); // remember usage


        double hyperScore = match.getHyperScore();
        if (hyperScore > 0) {
            conditionedScan.addHyperscore(hyperScore);
            conditionedScan.addSpectralMatch(match);
        }
        return numberScored;
    }


    /**
     * @param scorer
     * @param sa
     * @param pCounter
     * @param pSpectrums
     * @param pConditionedScan
     * @return
     */
    @Override
    public int scoreScan(final Scorer scorer, final IonUseCounter pCounter, final ITheoreticalSpectrumSet[] pSpectrums, final IScoredScan pConditionedScan) {
        int numberScoredSpectra = 0;
        SpectrumCondition sc = scorer.getSpectrumCondition();
         IMeasuredSpectrum scan = pConditionedScan.conditionScan(this, sc);

        // DEBUGGING CODE
        IPolypeptide pp = pSpectrums[0].getPeptide();
        IMeasuredSpectrum scn  = pConditionedScan.getRaw();
        boolean logCalculations = TestUtilities.isInterestingScoringPair(pp, scn);

         if (scan == null)
            return 0; // not scoring this one
        if (!canScore(scan))
            return 0; // not scoring this one


        // NOTE this is totally NOT Thread Safe
        fillSpectrumPeaks(scan);

        //        DebugDotProduct logDotProductB = null;
        //        DebugDotProduct logDotProductY = null;

        if (!pConditionedScan.isValid())
            return 0;
        String scanid = scan.getId();
        //        if (scanid.equals("7868"))
        //            XTandemUtilities.breakHere();
        int precursorCharge = scan.getPrecursorCharge();

        double testmass = scan.getPrecursorMass();
        if (pSpectrums.length == 0)
            return 0; // nothing to do
        // for debugging isolate one case

        for (int j = 0; j < pSpectrums.length; j++) {
            ITheoreticalSpectrumSet tsSet = pSpectrums[j];
            // debugging test
            IPolypeptide peptide = tsSet.getPeptide();
            double matching = peptide.getMatchingMass();



            if (scorer.isTheoreticalSpectrumScored(pConditionedScan, tsSet)) {
                numberScoredSpectra += scoreOnePeptide(pCounter, pConditionedScan, scan, Scorer.PEAKS_BY_MASS, precursorCharge, tsSet,logCalculations);
            }

            // debugging code -t look at why some specrta are NOT scored
//             else {
//                 // for the moment reiterate the code to find out why not scored
//                 if (XTandemHadoopUtilities.isNotScored(pConditionedScan.getRaw())) {
//                     XTandemUtilities.breakHere();
//                     boolean notDone = scorer.isTheoreticalSpectrumScored(pConditionedScan, tsSet);
//                     ScoringReducer.gNumberNotScored++;
//
//                 }
//             }
        }
        return numberScoredSpectra;
    }


    /**
     * Cheat by rounding mass to the nearest int and limiting to MAX_MASS
     * then just generate arrays of the masses and multiply them all together
     *
     * @param measured  !null measured spectrum
     * @param theory    !null theoretical spectrum
     * @param counter   !null use counter
     * @param holder    !null holder tro receive matched peaks
     * @param otherData anythiing else needed
     * @return comupted dot product
     */
    @Override
    public abstract double dot_product(final IMeasuredSpectrum measured, final ITheoreticalSpectrum theory, final IonUseCounter counter, final List<DebugMatchPeak> holder, final Object... otherData);

    /**
     * modify the theoretical spectrum before scoring - this may
     * involve reweighting or dropping peaks
     *
     * @param pTs !null spectrum
     * @return !null modified spectrum
     */
    @Override
    public abstract ITheoreticalSpectrum buildScoredScan(final ITheoreticalSpectrum pTs);
}
