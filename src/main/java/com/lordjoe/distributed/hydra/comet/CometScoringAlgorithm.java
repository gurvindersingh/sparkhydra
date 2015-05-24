package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.hydra.test.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.testing.*;

import javax.annotation.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringAlgorithm
 * User: Steve
 * Date: 1/12/12
 */
public class CometScoringAlgorithm extends AbstractScoringAlgorithm {

    public static final String MAX_PEPTIDE_LIST_PROPERTY = "comet.MaxPeptideListSize";

    private static int maximumPeptideListSize = Integer.MAX_VALUE;

     public static int getMaximumPeptideListSize() {
         return maximumPeptideListSize;
     }

     public static void setMaximumPeptideListSize(int maximumPeptideListSize) {
         CometScoringAlgorithm.maximumPeptideListSize = maximumPeptideListSize;
     }

    public static final double XCORR_CUTOFF = 1.0E-8;
    public static final int DEFAULT_CROSS_CORRELATION_PROCESSINGG_OFFSET = 75;

    public static final int NUMBER_ION_TYPES = 2; // B and I

    public static final ITandemScoringAlgorithm DEFAULT_ALGORITHM = new CometScoringAlgorithm();
    public static final ITandemScoringAlgorithm[] DEFAULT_ALGORITHMS = {DEFAULT_ALGORITHM};
    public static final String DEFAULT_VERSION = "1.0";

    public static final int MAX_MASS = 5000;
    public static final int NORMALIZATION_FACTOR = 100;

    public static final double PPM_UNITS_FACTOR = 1000000;


    public static final double LOG_PI = Math.log(Math.sqrt(2 * Math.PI));
    public static final double NORMALIZATION_MAX = 100.0;
    public static final double DEFAULT_PEPTIDE_ERROR = 1.0;
    public static final double DEFAULT_MASS_TOLERANCE = 1; // tood fix 0.025;
    public static final int DEFAULT_MINIMUM_PEAK_NUMBER = 5; // do not score smaller spectra
    public static final int PEAK_BIN_SIZE = 100;   // the maxium peak is this
    public static final int DEFAULT_MAX_FRAGMENT_CHARGE = 3;
    public static final int PEAK_BIN_NUMBER = 3;
    public static final int PEAK_NORMALIZATION_BINS = 5;
    public static final int MINIMUM_SCORED_IONS = 10;

    public static final double DEFAULT_BIN_WIDTH = 0.02;
    public static final double DEFAULT_BIN_OFFSET = 0.00;

    public static final String ALGORITHM_NAME = "Comet";

    public static int ALLOCATED_DATA_SIZE = (int)(MAX_MASS / DEFAULT_BIN_WIDTH);


    private double m_PeptideError = DEFAULT_PEPTIDE_ERROR;
    private double m_MassTolerance = DEFAULT_MASS_TOLERANCE;
    private int m_MaxFragmentCharge = DEFAULT_MAX_FRAGMENT_CHARGE;
    private int m_MinimumNumberPeaks = DEFAULT_MINIMUM_PEAK_NUMBER;
    private double m_UnitsFactor = 1; // change if ppm


    //    private IMeasuredSpectrum m_Spectrum;
//    private BinnedMutableSpectrum m_BinnedSpectrum;
    private double m_TotalIntensity;
    private double m_BinTolerance = DEFAULT_BIN_WIDTH;
    private double m_BinStartOffset = DEFAULT_BIN_OFFSET;
    private double m_OneMinusBinOffset = 1.0 - m_BinStartOffset;

    protected int iMinus17;
    protected int iMinus18;


//    if (g_StaticParams.tolerances.dFragmentBinSize == 0.0)
//       g_StaticParams.tolerances.dFragmentBinSize = DEFAULT_BIN_WIDTH;
//
//    // Set dInverseBinWidth to its inverse in order to use a multiply instead of divide in BIN macro.
//    g_StaticParams.dInverseBinWidth = 1.0 /g_StaticParams.tolerances.dFragmentBinSize;
//    g_StaticParams.dOneMinusBinOffset = 1.0 - g_StaticParams.tolerances.dFragmentBinStartOffset;
//

    public CometScoringAlgorithm() {
    }

    /**
     * for testing - these are usually wrapped before serialization
     *
     * @param spec
     * @param pp
     * @param application
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static double doRealScoring(IMeasuredSpectrum spec, IPolypeptide pp, XTandemMain application) {
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getScorer();
        Scorer scorer = application.getScoreRunner();
        CometTheoreticalBinnedSet ts = new CometTheoreticalBinnedSet(spec.getPrecursorCharge(), spec.getPrecursorMass(), pp, comet, scorer);
        CometScoredScan scan = new CometScoredScan(spec, comet);
        return doRealScoring(scan, scorer, ts, application);
    }

    /**
     * IMPORTANT the real word is done here
     *
     * @param pScoring
     * @param pTs
     * @param application
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static double doRealScoring(final CometScoredScan pScoring, final Scorer scorer, final ITheoreticalSpectrumSet pTs, XTandemMain application) {

        IPolypeptide peptide = pTs.getPeptide();
        IMeasuredSpectrum spec = pScoring.getConditionedScan();
        //====================================================
        // THIS IS ALL DEBUGGGING
        if (TestUtilities.isInterestingSpectrum(pScoring)) {
            TestUtilities.breakHere();
        }
        if (TestUtilities.isInterestingPeptide(peptide)) {
            TestUtilities.breakHere();
        }
//        if (TestUtilities.isInterestingScoringPair(peptide, pScoring)) {
//            TestUtilities.breakHere();
//            TestUtilities.setLogCalculations(application, true); // log this
//        } else {
//            String log = TestUtilities.setLogCalculations(application, false); // log off
//            if (log != null)
//                System.out.println(log);
//        }
        //====================================================
        // END DEBUGGGING

        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getScorer();
        IonUseCounter counter = new IonUseCounter();
        List<XCorrUsedData> used = new ArrayList<XCorrUsedData>();

        //if (SparkUtilities.validateDesiredUse(spec, peptide, 0))
        //    breakHere(); // look at these cases

        // TODO Put it in constructor
        //pScoring.setAlgorithm(comet);

        //double mass = pScoring.getPrecursorMass();    // todo is this peptide or
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        //int MaxArraySize = comet.asBin(mass) + 100; // ((int) ((mass + 100) / getBinTolerance()); //  pScoring->_spectrumInfoInternal.iArraySize

//        comet.normalizeBinnedPeaks(MaxArraySize);
//        comet.normalizeForNL(MaxArraySize);

        //====================================================
        // THIS IS ALL DEBUGGGING
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        //List<SpectrumBinnedScore> fastScoringData = pScoring.getFastScoringData();
        //List<SpectrumBinnedScore> fastScoringDataNL = pScoring.getNLScoringData();
        //====================================================
        // END DEBUGGGING

        double xcorr = comet.doXCorr((CometTheoreticalBinnedSet) pTs, scorer, counter, pScoring, used);

        //  SparkUtilities.validateDesiredUse(spec,peptide,xcorr) ;

        // pScoring.clearScoringData();

        return xcorr;
    }

    /**
     * use the parameters to configure local properties
     *
     * @param !null params
     */
    @Override
    public void configure(final IParameterHolder params) {
        super.configure(params);
        CometTesting.validateOneKey(); // We are hunting for when this stops working
        final String units = params.getParameter("spectrum, parent monoisotopic mass error units",
                "Daltons");
        m_BinTolerance = params.getDoubleParameter("comet.fragment_bin_tol", DEFAULT_BIN_WIDTH);
        ALLOCATED_DATA_SIZE = (int)(MAX_MASS / m_BinTolerance);

        setMaximumPeptideListSize(params.getIntParameter(MAX_PEPTIDE_LIST_PROPERTY, Integer.MAX_VALUE));


        m_BinStartOffset = params.getDoubleParameter("comet.fragment_bin_offset", DEFAULT_BIN_OFFSET);
         m_OneMinusBinOffset = 1.0 - m_BinStartOffset;


        m_MassTolerance = params.getDoubleParameter("comet.mass_tolerance", DEFAULT_MASS_TOLERANCE);
        if ("ppm".equalsIgnoreCase(units)) {
            m_UnitsFactor = PPM_UNITS_FACTOR;
            setMinusLimit(m_MassTolerance / m_UnitsFactor);      // the way the databasse works better send a wider limits
            setPlusLimit(m_MassTolerance / m_UnitsFactor);
        }

        m_MaxFragmentCharge = params.getIntParameter("comet.max_fragment_charge", DEFAULT_MAX_FRAGMENT_CHARGE);
        //    if (g_StaticParams.tolerances.dFragmentBinSize == 0.0)
        //       g_StaticParams.tolerances.dFragmentBinSize = DEFAULT_BIN_WIDTH;
        //
        //    // Set dInverseBinWidth to its inverse in order to use a multiply instead of divide in BIN macro.
        //    g_StaticParams.dInverseBinWidth = 1.0 /g_StaticParams.tolerances.dFragmentBinSize;
        //    g_StaticParams.dOneMinusBinOffset = 1.0 - g_StaticParams.tolerances.dFragmentBinStartOffset;
        //
        double h2O = MassCalculator.getDefaultCalculator().calcMass("H2O");
        iMinus17 = asBin(h2O);
        double nh3 = MassCalculator.getDefaultCalculator().calcMass("NH3");
        iMinus18 = asBin(nh3);

        CometTesting.validateOneKey(); // We are hunting for when this stops working

        Double testValue = params.getDoubleParameter("protein, cleavage N-terminal mass change", 0);
        if(Math.abs(testValue - 1.007276466) > 0.0001)
            throw new IllegalStateException("comet needs " + 1.007276466 + " not " + testValue);

        CometTesting.testCometConfiguration(this);

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

        if (Math.abs(del) < 0.3)  // look at close calls
            XTandemUtilities.breakHere();

        double averageMass = (scanMass + mass2) / 2;
        if (averageMass < 200)
            return false; // too low to score

        del /= averageMass;
        boolean ret = false;
        //       XTandemUtilities.workUntil(2012,5,3);
        // Note looks backwards but this is what they do
        double plusLimit = getPlusLimit();
        if (del > 0)
            ret = del <= Math.abs(plusLimit);
        else
            ret = -del <= Math.abs(plusLimit);

//        // Note looks backwards but this is what they do
//        if (del > 0)
//            ret = del <=  Math.abs(m_MinusLimit);
//        else
//            ret = -del <= Math.abs(m_PlusLimit);

        if (!ret)
            return false;
        return ret; // break here interesting result
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

        CometScoredScan scoring = new CometScoredScan(scan, this);
        //scoring.setAlgorithm(this);
        IonUseCounter counter = new IonUseCounter();

        int numberDotProducts = scoreScan(scorer, counter, tss, scoring);
        return scoring;
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
    public int scoreScan(final Scorer scorer, final IonUseCounter pCounter, final ITheoreticalSpectrumSet[] pSpectrums, final IScoredScan conditionedScan) {
        CometScoredScan pConditionedScan = (CometScoredScan) conditionedScan;

        final double massTolerance = getBinTolerance();
        if (massTolerance >= 0.10) //g_staticParams.tolerances.dFragmentBinSize >= 0.10)
            throw new UnsupportedOperationException("We cannot yet handle low resolution searches");


//        float[] pdTmpFastXcorrData = getTmpFastXcorrData();
//        float[] pScoringFastXcorrData = getScoringFastXcorrData();
//        float[] pfFastXcorrDataNL = getFastXcorrDataNL(); // pfFastXcorrData with NH3, H2O contributions

        IPolypeptide pp = pSpectrums[0].getPeptide();
        SpectrumCondition sc = scorer.getSpectrumCondition();

        pConditionedScan.guaranteeNormalized();
        int numberScoredSpectra = 0;
        IMeasuredSpectrum scan = conditionedScan.getRaw();

        // DEBUGGING CODE
        IMeasuredSpectrum scn = pConditionedScan.getRaw();
        boolean logCalculations = TestUtilities.isInterestingScoringPair(pp, scn);

        if (scan == null)
            return 0; // not scoring this one
        if (!canScore(scan))
            return 0; // not scoring this one
        if (!pConditionedScan.isValid())
            return 0;
        double score = 0;
        String scanid = scan.getId();
        //        if (scanid.equals("7868"))
        //            XTandemUtilities.breakHere();
        int precursorCharge = scan.getPrecursorCharge();

        double testmass = scan.getPrecursorMass();
        if (pSpectrums.length == 0)
            return 0; // nothing to do
        // for debugging isolate one case

        List<XCorrUsedData> used = new ArrayList<XCorrUsedData>();

        for (int j = 0; j < pSpectrums.length; j++) {
            ITheoreticalSpectrumSet tsSet = pSpectrums[j];
            // debugging test
            IPolypeptide peptide = tsSet.getPeptide();
            double matching = peptide.getMatchingMass();

            used.clear();
            if (scorer.isTheoreticalSpectrumScored(pConditionedScan, tsSet)) {
                score = doXCorr((CometTheoreticalBinnedSet) tsSet,scorer, pCounter, pConditionedScan, used);
                numberScoredSpectra++;
                SpectralMatch sm = new SpectralMatch(
                        pp,
                        scan,
                        score,
                        score,
                        score,
                        pConditionedScan,
                        tsSet
                );
                IonUseScore ionUSe = buildIonUseScore(used);
                ((CometScoredScan) conditionedScan).getIonUse().add(ionUSe);
                ((CometScoredScan) conditionedScan).addSpectralMatch(sm);
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

    private IonUseScore buildIonUseScore(final List<XCorrUsedData> pUsed) {
        int BUse = 0;
        int YUse = 0;
        double BScore = 0;
        double YScore = 0;
        for (XCorrUsedData x : pUsed) {
            switch (x.ion) {
                case Y:
                    YUse++;
                    YScore += x.score;
                    break;
                case B:
                    BUse++;
                    BScore += x.score;
                    break;
                default:
                    throw new UnsupportedOperationException("never get here");

            }
        }
        IonUseScore ret = new IonUseScore();
        ret.setCount(IonType.B, BUse);
        ret.setCount(IonType.Y, YUse);
        ret.setScore(IonType.B, BScore);
        ret.setScore(IonType.Y, YScore);

        return ret;
    }

    public double doXCorr(final CometTheoreticalBinnedSet pTs,final Scorer scorerData, final IonUseCounter pCounter, CometScoredScan scorer, List<XCorrUsedData> used) {
        //if(true)
        //    return Math.random();
        CometTheoreticalBinnedSet sts = pTs;
        float[] fastXcorrDataMap =  CometScoringData.getFastDataForScan(scorer);
        float[] fastXcorrDataNL =  CometScoringData.getFastDataNLForScan(scorer);

        boolean interesting = false;
        if(TestUtilities.isInterestingPeptide(sts.getPeptide())) {
            interesting = true;
            int nzScoring = TestUtilities.getNonZeroElements(fastXcorrDataMap);
            int nzNLScoring = TestUtilities.getNonZeroElements(fastXcorrDataNL);
        }

        List<BinnedChargeIonIndex> binnedIndex = sts.getBinnedIndex(this,scorerData);

           double xcorr = 0;

        int maxCharge = scorer.getCharge();
        if(maxCharge > 1 )
            maxCharge = maxCharge - 1;
        int scoredPeaks = 0;

        if(interesting) {
            CometTesting.validateOneIndexSet(binnedIndex);
        }

        for (BinnedChargeIonIndex peak : binnedIndex) {
            int index = peak.index;
            if(peak.charge > maxCharge)
                continue;

            float value = scorer.getScoredData(fastXcorrDataMap, fastXcorrDataNL,index, peak.charge);

            if (Math.abs(value) > 0.0001) {
                xcorr += value;
                scoredPeaks++;
                //if (used != null)
                //    used.add(new XCorrUsedData(peak.charge, peak.type, index, value));
                pCounter.addCount(peak.type);
            }
        }
        if (scoredPeaks > 0) {
            scorer.addHyperscore(xcorr);
              xcorr *= 0.005;
             xcorr = Math.max(XCORR_CUTOFF,xcorr);
        }

//        if(Math.abs(altAnswer - xcorr) > 0.001)
//            throw new IllegalStateException("problem"); // todo fix

        return xcorr;
    }

//    public double doXCorrWithData(final CometTheoreticalBinnedSet sts,
//                                  final Scorer scorerData,
//                                  final IonUseCounter pCounter,
//                                  CometScoredScan scorer,
//                                  float[] fastXcorrDataMap,
//                                  float[] fastXcorrDataNL) {
//        //if(true)
//        //    return Math.random();
//
//        if(TestUtilities.isInterestingPeptide(sts.getPeptide()))
//            TestUtilities.breakHere();
//
//           List<BinnedChargeIonIndex> binnedIndex = sts.getBinnedIndex(this,scorerData);
//        double xcorr = 0;
//
//        int maxCharge = scorer.getCharge();
//        if(maxCharge > 1 )
//            maxCharge = maxCharge - 1;
//        int scoredPeaks = 0;
//        for (BinnedChargeIonIndex peak : binnedIndex) {
//            int index = peak.index;
//            if(peak.charge > maxCharge)
//                continue;
//
//            float value = scorer.getScoredData(fastXcorrDataMap, fastXcorrDataNL,index, peak.charge);
//            if (Math.abs(value) > 0.001) {
//                xcorr += value;
//                scoredPeaks++;
//                //if (used != null)
//                //    used.add(new XCorrUsedData(peak.charge, peak.type, index, value));
//                pCounter.addCount(peak.type);
//            }
//        }
//        if (scoredPeaks > 0) {
//            scorer.addHyperscore(xcorr);
//             xcorr *= 0.005;
//             xcorr = Math.max(XCORR_CUTOFF,xcorr);
//        }
//        return xcorr;
//    }

    public static final int NUM_SP_IONS = 200;

    protected List<CometPeak> getHighestPeaks(final float[] pBinnedPeaks) {
        double lowestInten = 0.0;
        double maxInten = 0.001;   // so we dont get divide by 0

        Set<CometPeak> holder = new HashSet<CometPeak>();
        for (int i = 0; i < pBinnedPeaks.length; i++) {
            float peak = pBinnedPeaks[i];
            if (peak <= lowestInten)
                continue;
            maxInten = Math.max(maxInten, peak);
            CometPeak saved = new CometPeak(i, peak);
            if (holder.size() < NUM_SP_IONS) {
                holder.add(saved);
            }
            else {
                float newLowest = saved.peak;
                ;
                CometPeak lowestPeak = saved;
                for (CometPeak cometPeak : holder) {
                    newLowest = Math.min(newLowest, cometPeak.peak);
                    if (cometPeak.peak == lowestInten) {
                        lowestPeak = cometPeak;
                    }
                }
                holder.add(saved);
                holder.remove(lowestPeak);
                lowestInten = newLowest;
            }
        }
        ArrayList<CometPeak> ret = new ArrayList<CometPeak>();
        for (CometPeak cometPeak : holder) {
            ret.add(new CometPeak(cometPeak.index, (float) (cometPeak.peak / maxInten)));
        }
        Collections.sort(ret);
        return ret;
    }



          /*
              // Make fast xcorr spectrum.
      double dSum=0.0;

      dSum=0.0;
      for (i=0; i<g_staticParams.iXcorrProcessingOffset; i++)
         dSum += pdTmpCorrelationData[i];
      for (i=g_staticParams.iXcorrProcessingOffset; i < pScoring->_spectrumInfoInternal.iArraySize + g_staticParams.iXcorrProcessingOffset; i++)
      {
         if (i<pScoring->_spectrumInfoInternal.iArraySize)
            dSum += pdTmpCorrelationData[i];
         if (i>=(2*g_staticParams.iXcorrProcessingOffset + 1))
            dSum -= pdTmpCorrelationData[i-(2*g_staticParams.iXcorrProcessingOffset + 1)];
         pdTmpFastXcorrData[i-g_staticParams.iXcorrProcessingOffset] = (dSum - pdTmpCorrelationData[i-g_staticParams.iXcorrProcessingOffset])* 0.02;
      }

         */



          /*
           for (i=1; i<pScoring->_spectrumInfoInternal.iArraySize; i++)
   {
      double dTmp = pdTmpCorrelationData[i] - pdTmpFastXcorrData[i];

      pScoring->pfFastXcorrData[i] = (float)dTmp;

      // Add flanking peaks if used
      if (g_staticParams.ionInformation.iTheoreticalFragmentIons == 0)
      {
         int iTmp;

         iTmp = i-1;
         pScoring->pfFastXcorrData[i] += (float) ((pdTmpCorrelationData[iTmp] - pdTmpFastXcorrData[iTmp])*0.5);

         iTmp = i+1;
         if (iTmp < pScoring->_spectrumInfoInternal.iArraySize)
            pScoring->pfFastXcorrData[i] += (float) ((pdTmpCorrelationData[iTmp] - pdTmpFastXcorrData[iTmp])*0.5);
      }

      // If A, B or Y ions and their neutral loss selected, roll in -17/-18 contributions to pfFastXcorrDataNL
      if (g_staticParams.ionInformation.bUseNeutralLoss
            && (g_staticParams.ionInformation.iIonVal[ION_SERIES_A]
               || g_staticParams.ionInformation.iIonVal[ION_SERIES_B]
               || g_staticParams.ionInformation.iIonVal[ION_SERIES_Y]))
      {
         int iTmp;

         pScoring->pfFastXcorrDataNL[i] = pScoring->pfFastXcorrData[i];

         iTmp = i-g_staticParams.precalcMasses.iMinus17;
         if (iTmp>= 0)
         {
            pScoring->pfFastXcorrDataNL[i] += (float)((pdTmpCorrelationData[iTmp] - pdTmpFastXcorrData[iTmp]) * 0.2);
         }

         iTmp = i-g_staticParams.precalcMasses.iMinus18;
         if (iTmp>= 0)
         {
            pScoring->pfFastXcorrDataNL[i] += (float)((pdTmpCorrelationData[iTmp] - pdTmpFastXcorrData[iTmp]) * 0.2);
         }

      }
   }

         */


    @Override
    public void setMinusLimit(double pMinusLimit) {
        super.setMinusLimit(pMinusLimit);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public double getPlusLimit() {
        return super.getPlusLimit();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public String toString() {
        return getName();
    }

//    protected void setMeasuredSpectrum(IMeasuredSpectrum ms) {
//        if (ms == m_Spectrum)
//            return;
//        m_Spectrum = ms;
//        populateWeights(ms);
//    }


    /**
     * make comet bins
     *
     * @param mz
     * @return
     */
    public int asBin(double mz) {
        double d1 = mz / getBinTolerance();
        double d2 = d1 + m_OneMinusBinOffset;
        float f2 = (float) d2;
        int ret = (int) (d2);
        int ret2 = (int) (f2);
        if (ret != ret2)
            TestUtilities.breakHere();

        if (Math.abs(ret - 41970) < 2)
            TestUtilities.breakHere();

        return ret;
    }

    /**
     * invert the above so we can test
     * @param bin
     * @return
     */
    public double fromBin(int bin)
    {
        double ret = bin - m_OneMinusBinOffset;
        ret *=   getBinTolerance();
        return ret;
    }

    public static double getDefaultBinWidth() {
        return DEFAULT_BIN_WIDTH;
    }

    /**
     * make comet bins as floats so standard sprctrum structures can hold then
     * add 0.5 so they round well
     *
     * @param mz
     * @return
     */
    protected double asFBin(double mz) {
        double d1 = mz / getBinTolerance();
        double d2 = d1 + m_OneMinusBinOffset;
        return (int) d2; // used to say this but comet does not + 0.5;
    }

//    /**
//     * this method is protested to allow testing
//     *
//     * @return
//     */
//    protected CometScoringData getData() {
//        if (scoringData == null)
//            scoringData = new ThreadLocal<CometScoringData>();
//
//        CometScoringData ret = scoringData.get();
//        if (ret == null) {
//            ret = new CometScoringData(this);
//            scoringData.set(ret);
//        }
//        return ret;
//    }
//
//    /**
//     * this method is protested to allow testing
//     *
//     * @return
//     */
//    protected float[] getWeights() {
//        return getData().getWeights();
//    }
//
//
//    /**
//     * this method is protested to allow testing
//     *
//     * @return
//     */
//    public float[] getFastXcorrDataNL() {
//        return getData().getFastXcorrDataNL();
//    }
//
//    /**
//     * this method is protested to allow testing
//     *
//     * @return
//     */
//    public float[] getScoringFastXcorrData() {
//        return getData().getScoringFastXcorrData();
//    }
//
//    /**
//     * this method is protested to allow testing
//     *
//     * @return
//     */
//    protected float[] getTmpFastXcorrData() {
//        return getData().getTmpFastXcorrData();
//    }
//
//    /**
//     * this method is protested to allow testing
//     *
//     * @return
//     */
//    protected float[] getTmpFastXcorrData2() {
//        return getData().getTmpFastXcorrData2();
//    }
//
//    /**
//     * no threating issues
//     */
//    protected void clearData() {
//        getData().clearData();
//    }


    protected double computeProbability(double intensity, double massDifference) {
        double theta = getThetaFactor();
        double denom = 2 * theta * theta;
        double addedDiff = (massDifference * massDifference) / denom;
        double logIntensity = Math.log(intensity);
        double logPi = LOG_PI;
        double logTheta = Math.log(theta);
        double consts = logPi - logTheta;
        double prob = logIntensity - consts - addedDiff;
        return prob;
    }

    protected double getThetaFactor() {
        return getPeptideError() * 0.5;
    }

    public double getPeptideError() {
        return m_PeptideError;
    }

    public void setPeptideError(final double pPeptideError) {
        m_PeptideError = pPeptideError;
    }


    /**
     * return the product of the factorials of the counts
     *
     * @param counter - !null holding counts
     * @return as above
     */
    @Override
    public double getCountFactor(final IonUseScore counter) {
        return 1;
    }


    public double getTotalIntensity() {
        return m_TotalIntensity;
    }

    public int getMinimumNumberPeaks() {
        return m_MinimumNumberPeaks;
    }

    public int getMaxFragmentCharge() {
        return m_MaxFragmentCharge;
    }

    public void setMinimumNumberPeaks(int minimumNumberPeaks) {
        m_MinimumNumberPeaks = minimumNumberPeaks;
    }

    public double getUnitsFactor() {
        return m_UnitsFactor;
    }

    public void setUnitsFactor(double unitsFactor) {
        m_UnitsFactor = unitsFactor;
    }

    public void setTotalIntensity(double totalIntensity) {
        m_TotalIntensity = totalIntensity;
    }

    public double getBinTolerance() {
        return m_BinTolerance;
    }

    public double getOneMinusBinOffset() {
        return m_OneMinusBinOffset;
    }

    public void setBinTolerance(double binTolerance) {
        m_BinTolerance = binTolerance;
    }

    /**
     * return the low and high limits of a mass scan
     *
     * @param scanMass
     * @return as above
     */
    @Override    // todo fix this
    public int[] highAndLowMassLimits(double scanMass) {
        IMZToInteger defaultConverter = XTandemUtilities.getDefaultConverter();
        int[] ret = new int[2];
        // algorithm makes adjustments in the mass - this does the right thing
        // note this only looks backwards SLewis
        ret[0] = defaultConverter.asInteger(scanMass - getPlusLimit());
        ret[1] = defaultConverter.asInteger(scanMass + -getMinusLimit());

        return ret; // break here interesting result
    }

    @Override
    protected void fillSpectrumPeaks(IMeasuredSpectrum pScan) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
        //  setMeasuredSpectrum(pScan);
    }

    /**
     * score the two spectra
     *
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @return value of the score
     */
    @Override
    public double scoreSpectrum(final IMeasuredSpectrum measured, final ITheoreticalSpectrum theory, Object... otherdata) {
        IonUseCounter counter = new IonUseCounter();
        List<DebugMatchPeak> holder = new ArrayList<DebugMatchPeak>();
        double dot = dot_product(measured, theory, counter, holder);
        return dot;
    }


    /**
     * return a unique algorithm name
     *
     * @return
     */
    @Override
    public String getName() {
        return ALGORITHM_NAME;
    }

    /**
     * are we scoring mono average
     *
     * @return as above
     */
    @Override
    public MassType getMassType() {
        return null;
    }


    public double getMassTolerance() {
        return m_MassTolerance;
    }

    public void setMassTolerance(double massTolerance) {
        m_MassTolerance = massTolerance;
    }

    /**
     * alter the score from dot_product in algorithm depdndent manner
     *
     * @param score    old score
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @param counter  !null use counter
     * @return new score
     */
    @Override
    public double conditionScore(final double score, final IMeasuredSpectrum measured, final ITheoreticalSpectrumSet theory, final IonUseScore counter) {
        return score;
    }

    /**
     * find the hyperscore the score from dot_product in algorithm depdndent manner
     *
     * @param score    old score
     * @param measured !null measured spectrum
     * @param theory   !null theoretical spectrum
     * @param counter  !null use counter
     * @return hyperscore
     */
    @Override
    public double buildHyperscoreScore(final double score, final IMeasuredSpectrum measured, final ITheoreticalSpectrumSet theory, final IonUseScore counter) {
        return score;
    }


    /**
     * best leave in the air what the theoretical set is
     *
     * @param scorer
     * @param pPeptide
     * @return
     */
    public ITheoreticalSpectrumSet generateSpectrum(Scorer scorer, final IPolypeptide pPeptide) {
        if (pPeptide.isModified())
            TestUtilities.breakHere();
        if (TestUtilities.isInterestingPeptide(pPeptide))
            TestUtilities.breakHere();

        final SequenceUtilities su = scorer.getSequenceUtilities();
        double massPlusH = pPeptide.getMass() + XTandemUtilities.getProtonMass() + XTandemUtilities.getCleaveCMass() + XTandemUtilities.getCleaveNMass();
        CometTheoreticalBinnedSet set = new CometTheoreticalBinnedSet(scorer.MAX_CHARGE, massPlusH,
                pPeptide, this, scorer);

        if (TestUtilities.isInterestingPeptide(pPeptide)) {
            CometTesting.validateOneIndexSet(set.getBinnedIndex(this,scorer));
        }
        return set;
    }


    /**
     * an algorithm may choose not to score a petide - for example high resolution algorithms may
     * choose not to score ppetides too far away
     *
     * @param ts    !null peptide spectrum
     * @param pScan !null scan to score
     * @return true if scoring is desired
     */
    public boolean isTheoreticalSpectrumScored(ITheoreticalSpectrum ts, IMeasuredSpectrum pScan) {
         IPolypeptide peptide = ts.getPeptide();
         if(!isPairScored(pScan,peptide))
             return false;
        int charge = ts.getCharge();
        return true;

    }

    /**
     * test whether so score a pair if not we can save a lot of time
     * @param pSpec
     * @param pPp
     * @return
     */
    public  boolean isPairScored(@Nonnull final IMeasuredSpectrum pScan,@Nonnull  final IPolypeptide peptide)
    {
        //double matchingMass = peptide.getMatchingMass();
        double matchingMass = CometScoringAlgorithm.getCometMatchingMass(peptide) ;

         double precursorMass = pScan.getPrecursorMass();
        int precursorCharge = pScan.getPrecursorCharge();



        double del = Math.abs(matchingMass - precursorMass);
        double massTolerance = precursorMass * getMassTolerance()  / 1000000;  // assume mass is ppm
        //noinspection SimplifiableIfStatement


          if (del < massTolerance)
            return true;
        return false;
    }

    @Override
    protected int scoreOnePeptide(IonUseCounter pCounter, IScoredScan pConditionedScan, IMeasuredSpectrum pScan, double[] pPeaksByMass, int pPrecursorCharge, ITheoreticalSpectrumSet pTsSet, boolean logCalculations) {

        double scanMass = pScan.getPrecursorMass();
        double matchingMass = pTsSet.getPeptide().getMatchingMass();

        return super.scoreOnePeptide(pCounter, pConditionedScan, pScan, pPeaksByMass, pPrecursorCharge, pTsSet, logCalculations);    //To change body of overridden methods use File | Settings | File Templates.
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
    public double dot_product(final IMeasuredSpectrum measured, final ITheoreticalSpectrum theory, final IonUseCounter counter, final List<DebugMatchPeak> holder, final Object... otherData) {
//        setMeasuredSpectrum(measured);
//
//        IPolypeptide peptide = theory.getPeptide();
//
//        boolean isInteresting = TestUtilities.isInterestingPeptide();
//        if (isInteresting)
//            System.out.println("Scoring " + peptide);
//

        throw new UnsupportedOperationException("Fix This"); // ToDo
    }


    public static double getCometMatchingMass(IPolypeptide pp) {
        double matchingMass = pp.getMatchingMass();
       if(true)
           return matchingMass;
        return  matchingMass + 2 * MutableMeasuredSpectrum.HYDROGEN_MASS;
           //    MutableMeasuredSpectrum.PROTON_MASS;   // todo WHY DID I THINK I NEEDED to add this
    }

//    /**
//     * Cheat by rounding mass to the nearest int and limiting to MAX_MASS
//     * then just generate arrays of the masses and multiply them all together
//     *
//     * @param measured  !null measured spectrum
//     * @param theory    !null theoretical spectrum
//     * @param counter   !null use counter
//     * @param holder    !null holder tro receive matched peaks
//     * @param otherData anythiing else needed
//     * @return comupted dot product
//     */
//
//    public double OLDdot_product(final IMeasuredSpectrum measured, final ITheoreticalSpectrum theory, final IonUseCounter counter, final List<DebugMatchPeak> holder, final Object... otherData) {
//        setMeasuredSpectrum(measured);
//
//        IPolypeptide peptide = theory.getPeptide();
//
//        boolean isInteresting = TestUtilities.isInterestingPeptide();
//        if (isInteresting)
//            System.out.println("Scoring " + peptide);
//
//        int[] items = new int[1];
//        double peptideError = getPeptideError();
//
//        double score = 0;
//        double TotalIntensity = getTotalIntensity();
//
//        float[] weights = getWeights();
//        final ITheoreticalPeak[] tps = theory.getTheoreticalPeaks();
//
//        for (int i = 0; i < tps.length; i++) {
//            ITheoreticalPeak tp = tps[i];
//            int bin = asBin(tp.getMassChargeRatio());
//
//            IonType type = tp.getType();
//            if (17027 == bin || 18211 == bin)
//                bin = asBin(tp.getMassChargeRatio()); // break here  track two 'bad" peaks
//
//            float weight = weights[bin];
//            if (weight == 0)
//                continue;
//            counter.addCount(type);
//            if (isInteresting)
//                System.out.println(" add " + bin + " = " + String.format("%9.4f", weight));
//
//            score += weight; //  * tp.getPeak();
//        }
//        double originalScore = score;
//        if (score <= 0.0)
//            score = 0.0;
//        else
//            score *= 0.005;  // Scale intensities to 50 and divide score by 1E5.
//
//
//        if (isInteresting)
//            System.out.println(" originalScore " + String.format("%9.2f", originalScore) + " = " + String.format("%9.4f", score));
//        return (score);
//    }
//

    /**
     * return false if the algorithm will not score the spectrum
     *
     * @param !null spectrum measured
     * @return as above
     */
    public boolean canScore(IMeasuredSpectrum measured) {
        if (!super.canScore(measured))
            return false;
        //noinspection SimplifiableIfStatement
        if (measured.getPeaksCount() < getMinimumNumberPeaks())
            return false;

        return true; // override if some spectra are not scored
    }


    /**
     * test for acceptability and generate a new conditioned spectrum for
     * scoring
     * See Spectrum.truncatePeakList()
     *
     * @param in !null spectrum
     * @return null if the spectrum is to be ignored otherwise a conditioned spectrum
     */
    @Override
    public IMeasuredSpectrum conditionSpectrum(IScoredScan inx, final SpectrumCondition sc) {
        OriginatingScoredScan scan = (OriginatingScoredScan) inx;
        IMeasuredSpectrum in = scan.getNormalizedRawScan();
        if (in instanceof RawPeptideScan) {
            in = sc.normalizeSpectrum(in, scan.getNormalizationFactor());
            scan.setNormalizedRawScan(in);
        }
        in = normalize(in.asMmutable());
        return in;
    }


    @Override
    public IMeasuredSpectrum conditionSpectrum(final IMeasuredSpectrum pIn, final IMeasuredSpectrum pRaw) {
        if (true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        return null;
    }

    public static double getMassDifference(ISpectrum spec) {
        ISpectrumPeak[] peaks = spec.getPeaks();
        double lowMZ = peaks[0].getMassChargeRatio();
        double highMZ = peaks[peaks.length - 1].getMassChargeRatio();

        //System.out.println("lowMZ "+lowMZ+" highMZ "+highMZ);
        double diffMZ = 0;

        if (lowMZ == highMZ)
            diffMZ = 1f;
        else
            diffMZ = highMZ - lowMZ;


        return diffMZ;
    }


    public static double getMissScore(int numMissIons, ISpectrum spec) {
        double diffMZ = getMassDifference(spec);
        return numMissIons * (-Math.log(diffMZ));
    }


    /**
     * is this correct or should all spectra do this????? todo
     *
     * @param in
     * @return
     */
    protected IMeasuredSpectrum asFBinned(final MutableMeasuredSpectrum in) {
        ISpectrumPeak[] peaks = in.getPeaks();
        MutableSpectrumPeak[] newpeaks = new MutableSpectrumPeak[peaks.length];
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            newpeaks[i] = new MutableSpectrumPeak(asFBin(peak.getMassChargeRatio()), peak.getPeak());
        }
        MutableMeasuredSpectrum out = new MutableMeasuredSpectrum(in);
        out.setPeaks(newpeaks);
        return out; // I do not see normalization
    }


    /**
     * is this correct or should all spectra do this????? todo
     *
     * @param in
     * @return
     */
    protected IMeasuredSpectrum normalize(final MutableMeasuredSpectrum in) {
        double proton = MassCalculator.getDefaultCalculator().calcMass("H");
        ISpectrumPeak[] peaks = in.getPeaks();
        int charge = in.getPrecursorCharge();

        MutableSpectrumPeak[] newpeaks = new MutableSpectrumPeak[peaks.length];
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            double mz = peak.getMassChargeRatio();
            float pk = peak.getPeak();
            newpeaks[i] = new MutableSpectrumPeak(mz, pk);
        }
        normalizePeaks(newpeaks);
        windowedNormalize(newpeaks);
        BinnedMutableSpectrum out = new BinnedMutableSpectrum(in.getPrecursorCharge(), in.getPrecursorMassChargeRatio(),
                in.getScanData(), fbinPeaks(newpeaks));
        return out; // I do not see normalization
    }

    public static final int NUMBER_WINDOWS = 10;
    public static final double WINDOW_NORMALIZATION = 50;

    private void windowedNormalize(MutableSpectrumPeak[] peaks) {
        double maxPeak = 0;
        // peaks are in order
        double maxMz = peaks[peaks.length - 1].getMassChargeRatio();

        double windowWidth = (maxMz + 0.5) / NUMBER_WINDOWS;
        double[] maxWindow = new double[NUMBER_WINDOWS];
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            double mz = peak.getMassChargeRatio();
            int nWindow = (int) (mz / windowWidth);

            float pk = peak.getPeak();
            maxPeak = Math.max(maxPeak, pk);
            maxWindow[nWindow] = Math.max(maxWindow[nWindow], pk);
        }


        double minAffected = 0.05 * maxPeak;  // better be 5
        for (int i = 0; i < peaks.length; i++) {
            MutableSpectrumPeak peak = peaks[i];
            double mz = peak.getMassChargeRatio();
            int nWindow = (int) (mz / windowWidth);

            float pk = peak.getPeak();
            if (pk < minAffected)
                pk = 0;
            int bin = asBin(mz);
            double maxWindowPk = maxWindow[nWindow];
            if (maxWindowPk <= 0)
                continue;
            float normalizationFactor = (float) (WINDOW_NORMALIZATION / maxWindowPk);
            float normalizedValue = normalizationFactor * pk;
            peak.setPeak(normalizedValue);
            maxPeak = Math.max(maxPeak, pk);
        }


    }

    public static final int WINDOW_MAXIMUM = 50;


    public static MutableSpectrumPeak[] asMutable(final ISpectrumPeak[] peaks) {
        MutableSpectrumPeak[] ret = new MutableSpectrumPeak[peaks.length];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = new MutableSpectrumPeak(peaks[i]);
        }
        return ret;
    }

    public static void normalizePeaks(MutableSpectrumPeak[] myPeaks) {
        // Take the square root of the peak
        for (int i = 0; i < myPeaks.length; i++) {
            MutableSpectrumPeak myPeak = myPeaks[i];
            myPeak.setPeak((float) Math.sqrt(myPeak.getPeak()));
        }

        normalizePeakRange(myPeaks, 0, myPeaks.length);
        //noinspection UnnecessaryReturnStatement
        return; // so we can break
    }

    public static void normalizePeakRange(MutableSpectrumPeak[] myPeaks, int start, int end) {
        double maxValue = Double.MIN_VALUE;
        for (int i = start; i < Math.min(end, myPeaks.length); i++) {
            ISpectrumPeak peak = myPeaks[i];
            maxValue = Math.max(maxValue, peak.getPeak());
        }
        double factor = NORMALIZATION_MAX / maxValue;
        for (int i = start; i < Math.min(end, myPeaks.length); i++) {
            MutableSpectrumPeak myPeak = myPeaks[i];
            myPeak.setPeak((float) (factor * myPeak.getPeak()));

        }
    }


    public static double getMaximumIntensity(ISpectrumPeak[] peaks) {
        double ret = Double.MIN_VALUE;
        for (int i = 0; i < peaks.length; i++) {
            ISpectrumPeak peak = peaks[i];
            ret = Math.max(ret, peak.getPeak());
        }
        return ret;
    }

    protected void addHighestPeaks(final MutableMeasuredSpectrum pOut, final ISpectrumPeak[] pPeaks, final double pStart, final double pEnd) {
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        for (int i = 0; i < pPeaks.length; i++) {
            ISpectrumPeak peak = pPeaks[i];
            double mass = peak.getMassChargeRatio();
            if (Math.abs(mass - 850) < 2)
                XTandemUtilities.breakHere();

            if (mass >= pStart && mass < pEnd)
                holder.add(peak);
        }
        ISpectrumPeak[] used = new ISpectrumPeak[holder.size()];
        holder.toArray(used);
        // sort by size
        Arrays.sort(used, ScoringUtilities.PeakIntensityComparatorHiToLowINSTANCE);
        // add the top PEAK_BIN_SIZE peaks in this bin
        for (int i = 0; i < Math.min(PEAK_BIN_SIZE, used.length); i++) {
            ISpectrumPeak added = used[i];
            pOut.addPeak(added);

        }

    }


    /**
     * return the expected value for the best score
     *
     * @param scan !null scan
     * @return as above
     */
    @Override
    public double getExpectedValue(final IScoredScan scan) {
        return 0;
    }

    /**
     * modify the theoretical spectrum before scoring - this may
     * involve reweighting or dropping peaks
     *
     * @param pTs !null spectrum
     * @return !null modified spectrum
     */
    @Override
    public ITheoreticalSpectrum buildScoredScan(final ITheoreticalSpectrum pTs) {
        return pTs;
    }


    /**
     * convert peaks to binned peaks
     *
     * @param pks
     * @return
     */
    protected ISpectrumPeak[] fbinPeaks(ISpectrumPeak[] pks) {
        ISpectrumPeak[] ret = new ISpectrumPeak[pks.length];
        for (int i = 0; i < pks.length; i++) {
            ISpectrumPeak pk = pks[i];
            double fbin = asFBin(pk.getMassChargeRatio());
            ret[i] = new MutableSpectrumPeak(fbin, pk.getPeak());
        }
        return ret;
    }

    /**
     * largely to mark a spectrum as already binned
     */
    public static class BinnedMutableSpectrum extends MutableMeasuredSpectrum {

        public BinnedMutableSpectrum(int precursorCharge, double precursorMassChargeRatio, ISpectralScan scan, ISpectrumPeak[] peaks) {
            super(precursorCharge, precursorMassChargeRatio, scan, peaks);
        }

        public void populateWeights(float[] peaks) {
            Arrays.fill(peaks, 0);
            for (ISpectrumPeak pk : getPeaks()) {
                int index = (int) pk.getMassChargeRatio();
                float peak = pk.getPeak();
                peaks[index] = peak;
                peaks[index - 1] = Math.max(peaks[index - 1], peak / 2);
                peaks[index + 1] = Math.max(peaks[index + 1], peak / 2);
            }

        }
    }
}
