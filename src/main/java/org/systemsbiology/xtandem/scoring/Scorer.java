package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.common.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.testing.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * org.systemsbiology.xtandem.scoring.Scorer
 *
 * @author Steve Lewis
 * @date Jan 11, 2011
 */
public class Scorer  implements Serializable  {
    public static Scorer[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = Scorer.class;

    // I am about to start a major refactor on scoring so this says use old code
    public static boolean USE_CLASSIC_SCORING = false; // true;

    public static final double INDEXING_FACTOR = 0.4;
    public static final double MASS_DIFFERENCE_FOR_SCORE = 25;
    public static final int DEFAULT_CHARGE = 3;
    public static final int MAX_CHARGE = 3;

    // if non-zero ise to split up spectral groups to same memory
    private int m_SpectrumBatch;
    private int m_SpectrumIndex;

    private final IMainData m_Params;
    private final SequenceUtilities m_SequenceUtilities;
    private final SpectrumCondition m_SpectrumCondition;
    private final IScoringAlgorithm m_Algorithm;
    private final IProtein[] m_Proteins;
    private IScoredScan[] m_Scans;
//    private Map<String, IScoredScan> m_ScansById = new HashMap<String, IScoredScan>();

    private final Map<String, IPolypeptide> m_Peptides = new HashMap<String, IPolypeptide>();
    private IPolypeptide[] m_CachedPeptides;   // lazy away to get all peptide
    private final ScoringModifications m_Modifications;
    private Map<IPolypeptide, ITheoreticalSpectrumSet> m_Spectrum = new HashMap<IPolypeptide, ITheoreticalSpectrumSet>();
    private volatile PrintWriter m_Logger;

    private final List<SpectrumGenerationListener> m_SpectrumGenerationListeners = new ArrayList<SpectrumGenerationListener>();
    private final List<INoticationListener> m_INoticationListeners = new CopyOnWriteArrayList<INoticationListener>();


    public Scorer(IMainData params, SequenceUtilities su, SpectrumCondition sc,
                  IScoringAlgorithm pAlgorithm, IProtein[] proteins) {
        m_Algorithm = pAlgorithm;
        m_Proteins = proteins;
        m_SequenceUtilities = su;
        m_SpectrumCondition = sc;

        m_Params = params;
        m_Modifications = new ScoringModifications(params);

    }


    /**
     * add a change listener
     * final to make sure this is not duplicated at multiple levels
     *
     * @param added non-null change listener
     */
    public final void addINoticationListener(INoticationListener added) {
        if (!m_INoticationListeners.contains(added))
            m_INoticationListeners.add(added);
    }

    /**
     * remove a change listener
     *
     * @param removed non-null change listener
     */
    public final void removeINoticationListener(INoticationListener removed) {
        while (m_INoticationListeners.contains(removed))
            m_INoticationListeners.remove(removed);
    }


    /**
     * notify any state change listeners - probably should
     * be protected but is in the interface to form an event cluster
     *
     * @param oldState
     * @param newState
     * @param commanded
     */
    public void notifyINoticationListeners() {
        if (m_INoticationListeners.isEmpty())
            return;
        for (INoticationListener listener : m_INoticationListeners) {
            listener.onNotification();
        }
    }


    /**
     * add a change listener
     * final to make sure this is not duplicated at multiple levels
     *
     * @param added non-null change listener
     */
    public final void addSpectrumGenerationListener(SpectrumGenerationListener added) {
        if (!m_SpectrumGenerationListeners.contains(added))
            m_SpectrumGenerationListeners.add(added);
    }

    /**
     * remove a change listener
     *
     * @param removed non-null change listener
     */
    public final void removeSpectrumGenerationListener(SpectrumGenerationListener removed) {
        while (m_SpectrumGenerationListeners.contains(removed))
            m_SpectrumGenerationListeners.remove(removed);
    }


    /**
     * notify any state change listeners - probably should
     * be protected but is in the interface to form an event cluster
     *
     * @param oldState
     * @param newState
     * @param commanded
     */
    public void notifySpectrumGenerationListeners(ITheoreticalSpectrumSet spec) {
        if (m_SpectrumGenerationListeners.isEmpty())
            return;
        for (SpectrumGenerationListener listener : m_SpectrumGenerationListeners) {
            listener.onSpectrumGeneration(spec);
        }
    }

    public PrintWriter getLogger() {
        return m_Logger;
    }

    public void setLogger(final PrintWriter pLogger) {
        m_Logger = pLogger;
    }

    public void appendLine(String text) {
        if (getLogger() != null)
            getLogger().append(text + "\n");

    }


    public int getSpectrumBatch() {
        return m_SpectrumBatch;
    }

    public void setSpectrumBatch(final int pSpectrumBatch) {
        m_SpectrumBatch = pSpectrumBatch;
    }

    public int getSpectrumIndex() {
        return m_SpectrumIndex;
    }

    public void setSpectrumIndex(final int pSpectrumIndex) {
        m_SpectrumIndex = pSpectrumIndex;
    }

    public void setScoredScans(IMainData params, MassSpecRun[] runs) {
        List<IScoredScan> holder = new ArrayList<IScoredScan>();
        for (int i = 0; i < runs.length; i++) {
            RawPeptideScan[] scans = runs[i].getScans();
            for (int j = 0; j < scans.length; j++) {
                RawPeptideScan scan = scans[j];
                String id = scan.getId();
                IScoredScan scoring = params.getScoring(id);
                holder.add(scoring);
            }
        }
        m_Scans = new IScoredScan[holder.size()];
        holder.toArray(m_Scans);
        Arrays.sort(m_Scans);  // order by id
//        for (int i = 0; i < m_Scans.length; i++) {
//            final IScoredScan scoredScan = m_Scans[i];
//            m_ScansById.put(scoredScan.getId(), scoredScan);
//
//        }
    }

//    public void setScoredScans(IMainData params, RawPeptideScan[] scans) {
//        List<IScoredScan> holder = new ArrayList<IScoredScan>();
//        for (int j = 0; j < scans.length; j++) {
//            RawPeptideScan scan = scans[j];
//            String id = scan.getId();
//            IScoredScan scoring = params.getScoring(id);
//            holder.add(scoring);
//        }
//        m_Scans = new IScoredScan[holder.size()];
//        holder.toArray(m_Scans);
//        Arrays.sort(m_Scans);  // order by id
//        for (int i = 0; i < m_Scans.length; i++) {
//            final IScoredScan scoredScan = m_Scans[i];
//            m_ScansById.put(scoredScan.getId(), scoredScan);
//
//        }
//    }

    public IMainData getParams() {
        return m_Params;
    }

    public IScoringAlgorithm getAlgorithm() {
        return m_Algorithm;
    }

    public IProtein[] getProteins() {
        return m_Proteins;
    }

    /**
     * return all peptides sorted by id
     *
     * @return
     */
    public synchronized IPolypeptide[] getPeptides() {
        if (m_CachedPeptides == null) {
            IPolypeptide[] iPolypeptides = m_Peptides.values().toArray(IPolypeptide.EMPTY_ARRAY);
            Arrays.sort(iPolypeptides);
            m_CachedPeptides = iPolypeptides;
        }
        return m_CachedPeptides;
    }

    /**
     * look up a peptide by  id
     *
     * @param !null id
     * @return possibly null peptide
     */
    public IPolypeptide getPeptide(String id) {
        return m_Peptides.get(id);
    }

    /**
     * usually done once this adds a known peptide fragmment
     *
     * @param pp
     */
    public synchronized void addPeptide(IPolypeptide pp) {
        m_Peptides.put(pp.getId(), pp);
        m_CachedPeptides = null;
    }

    public void clearPeptides() {
        m_Peptides.clear();
        m_CachedPeptides = null;

    }

    public IScoredScan[] getScans() {
        return m_Scans;
    }

//    /**
//     * look up a scan by id
//     *
//     * @param id
//     * @return possibly mull scan
//     */
//    public IScoredScan getScan(int id) {
//        return m_ScansById.get(id);
//    }

    public SequenceUtilities getSequenceUtilities() {
        return m_SequenceUtilities;
    }

    public void score() {
        digest();
        generateTheoreticalSpectra();
//        final ElapsedTimer elapsed = ((XTandemMain) (m_Params)).getElapsed();
//        elapsed.showElapsed("Build Spectra ", System.out);
//        elapsed.reset();
        computeScores();
    }

    public SpectrumCondition getSpectrumCondition() {
        return m_SpectrumCondition;
    }


    public void computeScores() {
        ITheoreticalSpectrumSet[] spectrums = getAllSpectra();
        IonUseCounter counter = new IonUseCounter();
        IScoredScan[] conditionedScans = getScans();
        for (int i = 0; i < conditionedScans.length; i++) {
            final IScoredScan conditionedScan = conditionedScans[i];
            scoreScan(counter, spectrums, conditionedScan);
        }


    }


    public boolean isTheoreticalSpectrumScored(final IScoredScan pConditionedScan,
                                               ITheoreticalSpectrumSet ts) {
        boolean ret = false;
        final ITheoreticalSpectrum[] spectrums = ts.getSpectra();
        if (spectrums.length == 0)
            return false;
        // frament is the wrong mass
        int charge = pConditionedScan.getCharge();
        for (int i = 0; i < spectrums.length; i++) {
            ITheoreticalSpectrum spectrum = spectrums[i];
            int specCharge = spectrum.getCharge();
            if(charge > 0 && specCharge > charge)
                continue;
            if ( isTheoreticalSpectrumMassScored(pConditionedScan, spectrum))
                return true;

        }
         return false;
    }


    protected boolean isTheoreticalSpectrumMassScored(final IScoredScan pConditionedScan,
                                                      ITheoreticalSpectrum ts) {
        int thisCharge = ts.getCharge();

        SpectrumCondition sc = getSpectrumCondition();
        final IPolypeptide pp = ts.getPeptide();
        IMeasuredSpectrum scan = pConditionedScan.conditionScan(getAlgorithm(), sc);
        int charge = scan.getPrecursorCharge();
        double testmass = scan.getPrecursorMass();


        if (charge == 0)
            charge = DEFAULT_CHARGE;
        // block charge ion charge == charge
        // HUH Why do that SLewis 8-31-2012
     //   if (thisCharge > 1 && thisCharge == charge)
    //        return false;

        double mass = pp.getMass();
        double matchmass = pp.getMatchingMass();
        //  matchmass = mass; // todo !!!!!!!!!!!!!!!!! is this good take out if not
        OriginatingScoredScan pConditionedScan1 = (OriginatingScoredScan) pConditionedScan;
        IScoringAlgorithm algorithm = getAlgorithm();
        boolean massWithinRange = pConditionedScan1.isMassWithinRange(matchmass,charge, algorithm);
        //  matchmass = mass; // todo !!!!!!!!!!!!!!!!! WHY WHY WHY DO I Need this to score all scans xtandem scores
        if (massWithinRange)
            return true;
        // if not then why not
        return pConditionedScan1.isMassWithinRange(mass,charge, algorithm);
    }

    protected boolean isTheoreticalSpectrumScored(final IScoredScan pConditionedScan,
                                                  ITheoreticalSpectrum ts) {
        int thisCharge = ts.getCharge();

        SpectrumCondition sc = getSpectrumCondition();
        final IPolypeptide pp = ts.getPeptide();
        IMeasuredSpectrum scan = pConditionedScan.conditionScan(getAlgorithm(), sc);
        int charge = scan.getPrecursorCharge();
        double testmass = scan.getPrecursorMass();


        if (charge == 0)
            charge = DEFAULT_CHARGE;
        // block charge ion charge == charge
        if (thisCharge > 1 && thisCharge == charge)
            return false;
        double mass = pp.getMass();
        double del = Math.abs((mass / thisCharge--) - testmass);
        if (del < MASS_DIFFERENCE_FOR_SCORE)
            return true;
        // test other charge states - I am not sure why we do this but XTandem does
        while (thisCharge > 0) {
            del = Math.abs((mass / thisCharge--) - testmass);
            if (del < MASS_DIFFERENCE_FOR_SCORE)
                return true;
        }
        return false;
    }

    /**
     * @param pCounter
     * @param pSpectrums
     * @param pConditionedScan
     * @return number dot products
     */
    public int scoreScan(final IonUseCounter pCounter,
                         final ITheoreticalSpectrumSet[] pSpectrums,
                         final IScoredScan pConditionedScan) {
        if (USE_CLASSIC_SCORING)
            return classicScoreScan(pCounter, pSpectrums, pConditionedScan);
        else
            return experimentalScoreScan(pCounter, pSpectrums, pConditionedScan);


        //  double expectedValue = sa.getExpectedValue(pConditionedScan);
        //  pConditionedScan.setExpectedValue(expectedValue);
        //       pConditionedScan.setNumberScoredPeptides(numberScoredSpectra);
    }

    protected int scoreOnePeptide(final IonUseCounter pCounter, final IScoredScan pConditionedScan,
                                  final IScoringAlgorithm pSa,
                                  final IMeasuredSpectrum pScan,
                                  final double[] pPeaksByMass,
                                  final int pPrecursorCharge,
                                  final ITheoreticalSpectrumSet pTsSet) {

        if (!isTheoreticalSpectrumScored(pConditionedScan, pTsSet))
            return 0;

        if (USE_CLASSIC_SCORING)
            return classicScorePeptide(pCounter, pConditionedScan, pSa, pScan, pPeaksByMass, pPrecursorCharge, pTsSet);
        else
            return experimentalScorePeptide(pCounter, pConditionedScan, pSa, pScan, pPeaksByMass, pPrecursorCharge, pTsSet);
    }


    protected int classicScoreScan(final IonUseCounter pCounter, final ITheoreticalSpectrumSet[] pSpectrums, final IScoredScan pConditionedScan) {
        SpectrumCondition sc = getSpectrumCondition();
        final IScoringAlgorithm sa = getAlgorithm();
        int numberScoredSpectra = 0;
        boolean LOG_INTERMEDIATE_RESULTS = false;

        IMeasuredSpectrum scan = pConditionedScan.conditionScan(sa, sc);
        if (scan == null)
            return 0; // not scoring this one
        ISpectrumPeak[] peaks = scan.getPeaks();
        if (peaks.length == 0)
            return 0; // not scoring this one
        double[] peaksByMass = new double[XTandemUtilities.MAX_SCORED_MASS];
        // build list of peaks as ints  with the index being the imass
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            int imass = TandemKScoringAlgorithm.massChargeRatioAsInt(peak);
            peaksByMass[imass] += peak.getPeak();
        }

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
            numberScoredSpectra += scoreOnePeptide(pCounter, pConditionedScan, sa, scan, peaksByMass, precursorCharge, tsSet);
            notifyINoticationListeners(); // maybe increment a counter
            //     XTandemUtilities.outputLine("dot product " + product + " factor " + factor + " score " + score);
        }
        return numberScoredSpectra;
    }


    public static final double[] PEAKS_BY_MASS = new double[XTandemUtilities.MAX_SCORED_MASS];

    protected int experimentalScoreScan(final IonUseCounter pCounter, final ITheoreticalSpectrumSet[] pSpectrums, final IScoredScan pConditionedScan) {
        SpectrumCondition sc = getSpectrumCondition();
        final IScoringAlgorithm sa = getAlgorithm();
        int numberScoredSpectra = 0;
        boolean LOG_INTERMEDIATE_RESULTS = false;

        IMeasuredSpectrum scan = pConditionedScan.conditionScan(sa, sc);
        if (scan == null)
            return 0; // not scoring this one
        ISpectrumPeak[] peaks = scan.getPeaks();
        if (peaks.length == 0)
            return 0; // not scoring this one
        // NOTE this is totally NOT Thread Safe
        Arrays.fill(PEAKS_BY_MASS, 0);
        // build list of peaks as ints  with the index being the imass
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            int imass = TandemKScoringAlgorithm.massChargeRatioAsInt(peak);
            PEAKS_BY_MASS[imass] += peak.getPeak();
        }

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
            numberScoredSpectra += scoreOnePeptide(pCounter, pConditionedScan, sa, scan, PEAKS_BY_MASS, precursorCharge, tsSet);
            notifyINoticationListeners(); // maybe increment a counter
            //     XTandemUtilities.outputLine("dot product " + product + " factor " + factor + " score " + score);
        }
        return numberScoredSpectra;
    }

    private int classicScorePeptide(final IonUseCounter pCounter, final IScoredScan pConditionedScan, final IScoringAlgorithm pSa, final IMeasuredSpectrum pScan, final double[] pPeaksByMass, final int pPrecursorCharge, final ITheoreticalSpectrumSet pTsSet) {
        double oldscore = 0;
        int numberScored = 0;

        Map<ScanScoringIdentifier, ITheoreticalIonsScoring> ScoringIons = new HashMap<ScanScoringIdentifier, ITheoreticalIonsScoring>();


        if (!isTheoreticalSpectrumScored(pConditionedScan, pTsSet))
            return 0;

        final ITheoreticalSpectrum[] spectrums = pTsSet.getSpectra();
        if (spectrums.length == 0)
            return 0;
        String sequence = spectrums[0].getPeptide().getSequence();
        boolean LOG_INTERMEDIATE_RESULTS = isInterestingSequence(false, sequence);
        //        isTheoreticalSpectrumScored(pConditionedScan, tsSet); // rerun
        pCounter.clear();
        final int maxCharge = pTsSet.getMaxCharge();

        String scanid = pScan.getId();

        DebugDotProduct logDotProductB = null;
        DebugDotProduct logDotProductY = null;

        SpectralPeakUsage usage = new SpectralPeakUsage();
        for (int i = 0; i < spectrums.length; i++) {
            ITheoreticalSpectrum ts = spectrums[i];


            final int charge = ts.getCharge();
            if (pPrecursorCharge != 0 && charge > pPrecursorCharge)
                continue;
            //     JXTandemLauncher.logMessage(scanid + "\t" + sequence + "\t" + charge);
            if (maxCharge < charge)  // do NOT score the maximum charge
                continue;
            if (XTandemDebugging.isDebugging()) {
                final DebugValues lvs = XTandemDebugging.getLocalValues();
                //    final int realId = XTandemUtilities.buildChargedId(scan.getId(), charge);
                final String realId = scanid;

                if (charge == 1) {
                    logDotProductB = lvs.getDebugDotProduct(realId, IonType.B, charge, ts.getPeptide().getId());
                }
                else {
                    logDotProductB = null; // k score only scores y for charge > 1
                }
                logDotProductY = lvs.getDebugDotProduct(realId, IonType.Y, charge, ts.getPeptide().getId());
                XTandemDebugging.getLocalValues().addMeasuredSpectrums(scanid, "add_mi_conditioned", pScan);
            }

            List<DebugMatchPeak> holder = new ArrayList<DebugMatchPeak>();

            // filter the theoretical peaks
            ITheoreticalSpectrum scoredScan = pSa.buildScoredScan(ts);

            XTandemUtilities.showProgress(i, 10);

            // handleDebugging(pScan, logDotProductB, logDotProductY, ts, scoredScan);

            double dot_product = 0;
            if (LOG_INTERMEDIATE_RESULTS) // wer want to walk through interesting results
                dot_product = pSa.dot_product(pScan, scoredScan, pCounter, holder, pPeaksByMass, usage);
            else
                dot_product = pSa.dot_product(pScan, scoredScan, pCounter, holder, pPeaksByMass, usage);

            if (LOG_INTERMEDIATE_RESULTS)
                XMLUtilities.outputLine("Sequence=" + sequence +
                        " charge=" + charge +
                        " score=" + dot_product
                );
            oldscore += dot_product;
            numberScored++;

            if (pCounter.hasScore())
                appendScan(pScan, scoredScan, pCounter);


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
                logDotProductY.setScore(useScore1);
            }


            if (oldscore == 0)
                continue;

        }

        if (LOG_INTERMEDIATE_RESULTS)
            XTandemUtilities.breakHere();

        double score = pSa.conditionScore(oldscore, pScan, pTsSet, pCounter);

        if (score <= 0)
            return 0; // nothing to do
        double hyperscore = pSa.buildHyperscoreScore(score, pScan, pTsSet, pCounter);
        final IonUseScore useScore = new IonUseScore(pCounter);

        ITheoreticalIonsScoring[] ionMatches = ScoringIons.values().toArray(ITheoreticalIonsScoring.EMPTY_ARRAY);
        IExtendedSpectralMatch match = new ExtendedSpectralMatch(pTsSet.getPeptide(), pScan, score, hyperscore, oldscore,
                useScore, pTsSet, ionMatches);

        match.getUsage().addTo(usage); // remember usage

        ((OriginatingScoredScan) pConditionedScan).addSpectralMatch(match);
        return numberScored;
    }

    private int experimentalScorePeptide(final IonUseCounter pCounter, final IScoredScan pConditionedScan, final IScoringAlgorithm pSa, final IMeasuredSpectrum pScan, final double[] pPeaksByMass, final int pPrecursorCharge, final ITheoreticalSpectrumSet pTsSet) {
        double oldscore = 0;
        int numberScored = 0;

        OriginatingScoredScan conditionedScan = (OriginatingScoredScan) pConditionedScan;

        Map<ScanScoringIdentifier, ITheoreticalIonsScoring> ScoringIons = new HashMap<ScanScoringIdentifier, ITheoreticalIonsScoring>();


        if (!isTheoreticalSpectrumScored(pConditionedScan, pTsSet))
            return 0;

        final ITheoreticalSpectrum[] spectrums = pTsSet.getSpectra();
        int numberSpectra = spectrums.length;
        if (numberSpectra == 0)
            return 0;
        String sequence = spectrums[0].getPeptide().getSequence();
        boolean LOG_INTERMEDIATE_RESULTS = isInterestingSequence(false, sequence);
        //        isTheoreticalSpectrumScored(pConditionedScan, tsSet); // rerun
        pCounter.clear();
        final int maxCharge = pTsSet.getMaxCharge();

        String scanid = pScan.getId();

        DebugDotProduct logDotProductB = null;
        DebugDotProduct logDotProductY = null;

        SpectralPeakUsage usage = new SpectralPeakUsage();
        for (int i = 0; i < numberSpectra; i++) {
            ITheoreticalSpectrum ts = spectrums[i];


            final int charge = ts.getCharge();
            if (pPrecursorCharge != 0 && charge > pPrecursorCharge)
                continue;
            //     JXTandemLauncher.logMessage(scanid + "\t" + sequence + "\t" + charge);
            if (maxCharge < charge)  // do NOT score the maximum charge
                continue;
//            double lowestScoreToAdd = conditionedScan.lowestHyperscoreToAdd();
//            if (XTandemDebugging.isDebugging()) {
//                final DebugValues lvs = XTandemDebugging.getLocalValues();
//                //    final int realId = XTandemUtilities.buildChargedId(scan.getId(), charge);
//                final String realId = scanid;
//
//                if (charge == 1) {
//                    logDotProductB = lvs.getDebugDotProduct(realId, IonType.B, charge, ts.getPeptide().getId());
//                }
//                else {
//                    logDotProductB = null; // k score only scores y for charge > 1
//                }
//                logDotProductY = lvs.getDebugDotProduct(realId, IonType.Y, charge, ts.getPeptide().getId());
//                XTandemDebugging.getLocalValues().addMeasuredSpectrums(scanid, "add_mi_conditioned", pScan);
//            }

            List<DebugMatchPeak> holder = new ArrayList<DebugMatchPeak>();

            // filter the theoretical peaks
            ITheoreticalSpectrum scoredScan = pSa.buildScoredScan(ts);

            XTandemUtilities.showProgress(i, 10);

            // handleDebugging(pScan, logDotProductB, logDotProductY, ts, scoredScan);

            double dot_product = 0;
//            if (LOG_INTERMEDIATE_RESULTS) // wer want to walk through interesting results
//                dot_product = pSa.dot_product(pScan, scoredScan, pCounter, holder, pPeaksByMass, usage);
//            else
            dot_product = pSa.dot_product(pScan, scoredScan, pCounter, holder, pPeaksByMass, usage);

//            if (LOG_INTERMEDIATE_RESULTS)
//                XTandemUtilities.outputLine("Sequence=" + sequence +
//                        " charge=" + charge +
//                        " score=" + dot_product
//                );

            if (dot_product == 0)
                continue; // really nothing further to do
            oldscore += dot_product;
            numberScored++;

            if (pCounter.hasScore())
                appendScan(pScan, scoredScan, pCounter);


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
                        if (XTandemDebugging.isDebugging() && logDotProductY != null) {
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

        double score = pSa.conditionScore(oldscore, pScan, pTsSet, pCounter);

        if (score <= 0)
            return 0; // nothing to do
        double hyperscore = pSa.buildHyperscoreScore(score, pScan, pTsSet, pCounter);

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
     * walk through interesting sequences
     *
     * @param pLOG_INTERMEDIATE_RESULTS
     * @param pSequence
     * @return
     */
    protected boolean isInterestingSequence(boolean pLOG_INTERMEDIATE_RESULTS, final String pSequence) {
        if (pSequence.equals("VPIGEQPPDIEK")) {
            pLOG_INTERMEDIATE_RESULTS = true;
            //     XTandemUtilities.outputLine("Sequence=" + pSequence);
        }
        if (pSequence.equals("VNLVELGVYVSDIR")) {
            pLOG_INTERMEDIATE_RESULTS = true;
            //     XTandemUtilities.outputLine("Sequence=" + pSequence);
        }
        if (pSequence.equals("SLQYLAVPIYTIFK")) {
            pLOG_INTERMEDIATE_RESULTS = true;
            //     XTandemUtilities.outputLine("Sequence=" + pSequence);
        }
//        if (pSequence.equals("ELQIKLCK")) {
//              pLOG_INTERMEDIATE_RESULTS = true;
//              //     XTandemUtilities.outputLine("Sequence=" + pSequence);
//          }
//             if (pSequence.equals("MSTATTTVTT")) {
//            pLOG_INTERMEDIATE_RESULTS = true;
//            //         XTandemUtilities.outputLine("Sequence=" + pSequence);
//        }
//        if (pSequence.equals("GNNLRAIKK")) {
//            pLOG_INTERMEDIATE_RESULTS = true;
//            //        XTandemUtilities.outputLine("Sequence=" + pSequence);
//        }
//        if (pSequence.equals("HGALQPVHR")) {
//            pLOG_INTERMEDIATE_RESULTS = true;
//            XTandemUtilities.outputLine("Sequence=" + pSequence);
//         }
//             if (pSequence.equals("LSYTEFSVPVAAMG")) {
//            pLOG_INTERMEDIATE_RESULTS = true;
//            XTandemUtilities.outputLine("Sequence=" + pSequence);
//         }
//        if (pSequence.equals("GEQVNGEKPDNASK")) {
//            pLOG_INTERMEDIATE_RESULTS = true;
//            XTandemUtilities.outputLine("Sequence=" + pSequence);
//          }
//        if (pSequence.equals("GSLYTSSSSK")) {
//             pLOG_INTERMEDIATE_RESULTS = true;
//             XTandemUtilities.outputLine("Sequence=" + pSequence);
//           }
//        if (pSequence.equals("KKDPTANIK")) {
//             pLOG_INTERMEDIATE_RESULTS = true;
//             XTandemUtilities.outputLine("Sequence=" + pSequence);
//           }
        return pLOG_INTERMEDIATE_RESULTS;
    }

    /**
     * @param pCounter
     * @param pSpectrums
     * @param pConditionedScan
     */
    public void scoreScanOLD(final IonUseCounter pCounter,
                             final ITheoreticalSpectrumSet[] pSpectrums,
                             final IScoredScan pConditionedScan) {

        SpectrumCondition sc = getSpectrumCondition();
        final IScoringAlgorithm sa = getAlgorithm();
        int numberScoredSpectra = 0;
        boolean LOG_INTERMEDIATE_RESULTS = false;

        IMeasuredSpectrum scan = pConditionedScan.conditionScan(sa, sc);

        DebugDotProduct logDotProductB = null;
        DebugDotProduct logDotProductY = null;

        if (!pConditionedScan.isValid())
            return;
        String scanid = scan.getId();
//        if (scanid.equals("7868"))
//            XTandemUtilities.breakHere();


        double[] peaksByMass = new double[XTandemUtilities.MAX_CHARGE];
        double testmass = scan.getPrecursorMass();
        for (int j = 0; j < pSpectrums.length; j++) {
            LOG_INTERMEDIATE_RESULTS = false;
            double oldscore = 0;
            ITheoreticalSpectrumSet tsSet = pSpectrums[j];
            Map<ScanScoringIdentifier, ITheoreticalIonsScoring> ScoringIons = new HashMap<ScanScoringIdentifier, ITheoreticalIonsScoring>();


            // for debugging isolate one case
            final String sequence = tsSet.getPeptide().getSequence();

            if (sequence.equals("LSYTEFSVPVAAMG")) {
                LOG_INTERMEDIATE_RESULTS = true;
                XMLUtilities.outputLine("Sequence=" + sequence);
            }
            if (sequence.equals("GEQVNGEKPDNASK")) {
                LOG_INTERMEDIATE_RESULTS = true;
                XMLUtilities.outputLine("Sequence=" + sequence);
            }
            if (sequence.equals("LAIKMCKPG")) {
                LOG_INTERMEDIATE_RESULTS = true;
                XMLUtilities.outputLine("Sequence=" + sequence);
            }


            if (!isTheoreticalSpectrumScored(pConditionedScan, tsSet))
                continue;

            //        isTheoreticalSpectrumScored(pConditionedScan, tsSet); // rerun
            pCounter.clear();
            final ITheoreticalSpectrum[] spectrums = tsSet.getSpectra();
            final int maxCharge = tsSet.getMaxCharge();
            for (int i = 0; i < spectrums.length; i++) {
                ITheoreticalSpectrum ts = spectrums[i];


                final int charge = ts.getCharge();
                //     JXTandemLauncher.logMessage(scanid + "\t" + sequence + "\t" + charge);
                if (maxCharge < charge)  // do NOT score the maximum charge
                    continue;
                if (XTandemDebugging.isDebugging()) {
                    final DebugValues lvs = XTandemDebugging.getLocalValues();
                    //    final int realId = XTandemUtilities.buildChargedId(scan.getId(), charge);
                    final String realId = scanid;

                    if (charge == 1) {
                        logDotProductB = lvs.getDebugDotProduct(realId, IonType.B, charge, ts.getPeptide().getId());
                    }
                    else {
                        logDotProductB = null; // k score only scores y for charge > 1
                    }
                    logDotProductY = lvs.getDebugDotProduct(realId, IonType.Y, charge, ts.getPeptide().getId());
                }
                if (XTandemDebugging.isDebugging()) {
                    XTandemDebugging.getLocalValues().addMeasuredSpectrums(scan.getId(), "add_mi_conditioned", scan);
                }

                List<DebugMatchPeak> holder = new ArrayList<DebugMatchPeak>();

                // filter the theoretical peaks
                ITheoreticalSpectrum scoredScan = sa.buildScoredScan(ts);

                XTandemUtilities.showProgress(i, 10);

                handleDebugging(scan, logDotProductB, logDotProductY, ts, scoredScan);

                double dot_product = sa.dot_product(scan, scoredScan, pCounter, holder);
                if (LOG_INTERMEDIATE_RESULTS)
                    XMLUtilities.outputLine("Sequence=" + sequence +
                            " charge=" + charge +
                            " score=" + dot_product
                    );
                oldscore += dot_product;
                numberScoredSpectra++;

                if (pCounter.hasScore())
                    appendScan(scan, scoredScan, pCounter);


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
                            key = new ScanScoringIdentifier(sequence, charge, IonType.B);
                            if (XTandemDebugging.isDebugging()) {
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
                    logDotProductY.setScore(useScore1);
                }


                if (oldscore == 0)
                    continue;

            }

            if (LOG_INTERMEDIATE_RESULTS)
                XTandemUtilities.breakHere();

            double score = sa.conditionScore(oldscore, scan, tsSet, pCounter);

            double hyperscore = sa.buildHyperscoreScore(score, scan, tsSet, pCounter);
            final IonUseScore useScore = new IonUseScore(pCounter);

            ITheoreticalIonsScoring[] ionMatches = ScoringIons.values().toArray(ITheoreticalIonsScoring.EMPTY_ARRAY);
            IExtendedSpectralMatch match = new ExtendedSpectralMatch(tsSet.getPeptide(), scan, score, hyperscore, oldscore,
                    useScore, tsSet, ionMatches);

            ((OriginatingScoredScan) pConditionedScan).addSpectralMatch(match);

            //     XTandemUtilities.outputLine("dot product " + product + " factor " + factor + " score " + score);
        }

        double expectedValue = sa.getExpectedValue(pConditionedScan);
        pConditionedScan.setExpectedValue(expectedValue);
        //       pConditionedScan.setNumberScoredPeptides(numberScoredSpectra);
    }


    private void handleDebugging(final IMeasuredSpectrum pScan, final DebugDotProduct pLogDotProductB, final DebugDotProduct pLogDotProductY, final ITheoreticalSpectrum pTs, final ITheoreticalSpectrum pScoredScan) {
        if (XTandemDebugging.isDebugging()) {
            XTandemDebugging.getLocalValues().getDebugDotProduct(pScan, pTs);
            final ISpectrumPeak[] pks = pScan.getPeaks();
            for (int k = 0; k < pks.length; k++) {
                ISpectrumPeak pk = pks[k];
                if (pLogDotProductB != null)
                    pLogDotProductB.addMeasuredPeak(pk);
                pLogDotProductY.addMeasuredPeak(pk);
            }

            final ISpectrumPeak[] pk2s = pScoredScan.getPeaks();
            for (int k = 0; k < pk2s.length; k++) {
                ITheoreticalPeak pk2 = (ITheoreticalPeak) pk2s[k];
                switch (pk2.getType()) {
                    case B:
                        if (pLogDotProductB != null)
                            pLogDotProductB.addTheoreticalPeak(pk2);
                        break;
                    case Y:
                        pLogDotProductY.addTheoreticalPeak(pk2);
                        break;
                }

            }
        }
    }


    private void appendScan(final IMeasuredSpectrum pScan, ITheoreticalSpectrum tsp, IonUseCounter counter) {
        if (m_Logger == null)
            return;
        if (!counter.hasScore())
            return;
        Integer id = new Integer(pScan.getId());

        StringBuilder sb = new StringBuilder();
        sb.append("<dot_product id=\"" + id + "\" ");
        IPolypeptide peptide = tsp.getPeptide();
        String sequence = peptide.getSequence();


        sb.append("sequence=\"" + sequence + "\" ");
        if (peptide instanceof IModifiedPeptide) {
            IModifiedPeptide o = (IModifiedPeptide) peptide;
        }
        sb.append("charge=\"" + tsp.getCharge() + "\" ");

        sb.append("mz=\"" + tsp.getPeptide().getMatchingMass() + "\" ");

        appendScoreOfType(counter, sb, IonType.B, sequence);

        appendScoreOfType(counter, sb, IonType.Y, sequence);

        sb.append("total_score=\"" + TandemKScoringAlgorithm.conditionRawScore(counter.getTotalScore(), sequence) + "\" ");

        sb.append(" />");
        appendLine(sb.toString());
        //    appendLine("</dot_product>");

    }

    private void appendScoreOfType(final IonUseCounter counter, final StringBuilder pSb, final IonType pType, String sequence) {
        if (counter.getCount(pType) > 0) {
            pSb.append(pType + "count=\"" + counter.getCount(pType) + "\" ");
            double score1 = counter.getScore(pType);
            pSb.append(pType + "score=\"" + score1 + "\" ");
            pSb.append(pType + "_Cscore=\"" + TandemKScoringAlgorithm.conditionRawScore(score1, sequence) + "\" ");

        }
    }


    public void generateTheoreticalSpectra() {
        IPolypeptide[] peptides = getPeptides();
      //  XMLUtilities.outputLine("Total Peptides  " + peptides.length);
        generateTheoreticalSpectra(peptides);

    }

    public void generateTheoreticalSpectra(IPolypeptide[] pPeptides) {
        // Set<IPolypeptide> unused = new HashSet(m_Spectrum.keySet());
        // int batch = getSpectrumBatch();
        // int index = getSpectrumIndex();
        for (int i = 0; i < pPeptides.length; i++) {
            IPolypeptide peptide = pPeptides[i];
            // if we are batching then skip some
            //    if (batch != 0 && i % batch != index)
            //         continue;
            //    unused.remove(peptide); // still in use
            //     if (m_Spectrum.containsKey(peptide))
            //        continue; // already done

            // for debugging isolate one case

            generateSpectrum(peptide);
            //      XTandemUtilities.printProgress(i,1000);
        }
        // drop unused peptides
        //for (IPolypeptide unusedPeptide : unused)
        //    m_Spectrum.remove(unusedPeptide);
    }


    protected void generateSpectrum(final IPolypeptide pPeptide) {
        if (pPeptide.isModified())
            XTandemUtilities.breakHere();

        final SequenceUtilities su = getSequenceUtilities();
        double massPlusH = pPeptide.getMass() + XTandemUtilities.getProtonMass() + XTandemUtilities.getCleaveCMass() + XTandemUtilities.getCleaveNMass();
        ITheoreticalSpectrumSet set = new TheoreticalSpectrumSet(MAX_CHARGE, massPlusH,
                pPeptide);
        for (int charge = 1; charge <= MAX_CHARGE; charge++) {
            ITheoreticalSpectrum spectrum = generateTheoreticalSpectra(set, charge);
            // spectrum is added to the set
        }
        notifySpectrumGenerationListeners(set);
        addSpectrum(pPeptide, set);
    }


    public void clearSpectra() {
        m_Spectrum.clear();
    }

    public void addSpectrum(IPolypeptide pep, ITheoreticalSpectrumSet added) {
        m_Spectrum.put(pep, added);
    }


    public ITheoreticalSpectrumSet[] getAllSpectra() {
        return m_Spectrum.values().toArray(new ITheoreticalSpectrumSet[0]);
    }


    public ITheoreticalSpectrum generateTheoreticalSpectra(ITheoreticalSpectrumSet peptide,
                                                           int charge) {
        PeptideSpectrum ps = new PeptideSpectrum(peptide, charge, IonType.B_ION_TYPES,
                m_SequenceUtilities);
        ITheoreticalSpectrum conditioned = ScoringUtilities.applyConditioner(ps,
                new XTandemTheoreticalScoreConditioner());
        return conditioned;

    }

    /**
     * find peptides with the indicated scanmass
     *
     * @param scanmass scanmass +/- 0.5 d
     * @return !null array
     */
    public IPolypeptide[] getPeptidesOfMass(double scanmass) {

        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (IPolypeptide pp : m_Peptides.values()) {
            if ("HAFYQSANVPAGLLDYQHR".equals(pp.getSequence()))
                XTandemUtilities.breakHere();
            if (pp.getSequence().length() < 2)
                continue;
            double test_mass = pp.getMatchingMass();
            if (!m_Algorithm.isWithinLimits(scanmass, test_mass,0))
                continue;
            holder.add(pp);
        }
        if (holder.size() == 0)
            return IPolypeptide.EMPTY_ARRAY;

        IPolypeptide[] ret = new IPolypeptide[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    public void digest() {
        // we only need to do this once
        if (m_Peptides.size() > 0)
            return;
        // get all peptide fragments
        IMainData main = getParams();
        IPeptideDigester digester = main.getDigester();

        for (IProtein p : getProteins()) {
            final IPolypeptide[] polypeptides = digester.digest(p);
            // show all fragments
            if (false) {
                XMLUtilities.outputLine("// from protein \"" + p.getSequence() + "\",");
            }
            for (int i = 0; i < polypeptides.length; i++) {
                IPolypeptide pp = polypeptides[i];
                if (false) {
                    XMLUtilities.outputLine("\"" + pp.getSequence() + "\",");
                }
                addPeptide(pp);

            }


        }
    }


    /**
     * return the product of the top two factorials of the counts
     *
     * @param counter - !null holding counts
     * @return as above
     */

    public double getCountFactor(IonUseScore counter) {

        double max = 1;
        double nextmax = 1;
        // choose top 2 factorials
        for (IonType it : IonType.values()) {
            int count = counter.getCount(it);
            double data = factorial(count);
            if (data >= max) {
                nextmax = max;
                max = data;
            }
            else {
                if (data > nextmax)
                    nextmax = data;
            }
        }

        return max * nextmax;
    }

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
         public static double factorial(int n)
         {
             try {
                 //  fast version is to lookup
                 // throws an exception for n > 16
                 return FACTORIALS[n];
             }
             catch (IndexOutOfBoundsException e) {
                 double ret = 1;
                 while(n > 1) {
                     ret *= --n;
                 }
                 return ret;
             }
         }

}
