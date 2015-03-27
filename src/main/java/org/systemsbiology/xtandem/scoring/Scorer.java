package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.testing.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.Scorer
 *  new HydraSpark code - see ScorerClassic for original code
 * @author Steve Lewis
 * @date Jan 11, 2011
 */
public class Scorer  implements Serializable  {

    // I am about to start a major refactor on scoring so this says use old code
    public static boolean USE_CLASSIC_SCORING = false; // true;

    public static final double INDEXING_FACTOR = 0.4;
    public static final double MASS_DIFFERENCE_FOR_SCORE = 25;
    public static final int DEFAULT_CHARGE = 3;
    public static final int MAX_CHARGE = 4;

    // if non-zero ise to split up spectral groups to same memory
    private int m_SpectrumBatch;
    private int m_SpectrumIndex;

    private final IMainData m_Params;
    private final SequenceUtilities m_SequenceUtilities;
    private final SpectrumCondition m_SpectrumCondition;
    private final IScoringAlgorithm m_Algorithm;

//    private final Map<String, IPolypeptide> m_Peptides = new HashMap<String, IPolypeptide>();
//    private IPolypeptide[] m_CachedPeptides;   // lazy away to get all peptide
 //   private final ScoringModifications m_Modifications;



    public Scorer(IMainData params, SequenceUtilities su, SpectrumCondition sc,
                  IScoringAlgorithm pAlgorithm ) {
        m_Algorithm = pAlgorithm;
        m_SequenceUtilities = su;
        m_SpectrumCondition = sc;

        m_Params = params;
//        m_Modifications = new ScoringModifications(params);

    }


    public IMainData getParams() {
        return m_Params;
    }

    public IScoringAlgorithm getAlgorithm() {
        return m_Algorithm;
    }



    public SequenceUtilities getSequenceUtilities() {
        return m_SequenceUtilities;
    }


    public SpectrumCondition getSpectrumCondition() {
        return m_SpectrumCondition;
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

         return experimentalScorePeptide(pCounter, pConditionedScan, pSa, pScan, pPeaksByMass, pPrecursorCharge, pTsSet);
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
            }
        return numberScoredSpectra;
    }







    private void appendScoreOfType(final IonUseCounter counter, final StringBuilder pSb, final IonType pType, String sequence) {
        if (counter.getCount(pType) > 0) {
            pSb.append(pType + "count=\"" + counter.getCount(pType) + "\" ");
            double score1 = counter.getScore(pType);
            pSb.append(pType + "score=\"" + score1 + "\" ");
            pSb.append(pType + "_Cscore=\"" + TandemKScoringAlgorithm.conditionRawScore(score1, sequence) + "\" ");

        }
    }



    public ITheoreticalSpectrumSet generateSpectrum(final IPolypeptide pPeptide) {
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
          return set;
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
