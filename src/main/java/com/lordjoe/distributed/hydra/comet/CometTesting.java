package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.SparkUtilities;
import com.lordjoe.distributed.hydra.fragment.BinChargeKey;
import com.lordjoe.distributed.hydra.fragment.BinChargeMapper;
import com.lordjoe.distributed.spark.GeneratingPseudoList;
import org.apache.spark.api.java.JavaPairRDD;
import org.systemsbiology.xtandem.IMeasuredSpectrum;
import org.systemsbiology.xtandem.peptide.IPolypeptide;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometTesting
 * Test values for handling EG) case
 * User: Steve
 * Date: 4/1/2015
 */
public class CometTesting {


    private static Map<Integer, List<UsedSpectrum>> cometScoredSpectra;

    public static void readCometScoredSpectra(String file) {
        File f = new File(file);
        if (!f.exists())
            return;
        try {
            cometScoredSpectra = UsedSpectrum.readUsedSpectra(new FileInputStream(f));
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }
    }

    public static BinChargeKey getPeptideKey(IPolypeptide pp) {
        double matchingMass = CometScoringAlgorithm.getCometMatchingMass(pp);
        BinChargeKey ppKey = BinChargeMapper.oneKeyFromChargeMz(1, matchingMass);
        return ppKey;
    }


    public static int validatePeptideList(IMeasuredSpectrum scan, List<IPolypeptide> scoredPeptides) {
        if (cometScoredSpectra == null)
            return -1;   // not tracking
        String id = scan.getId();
        int index = Integer.parseInt(id);
        List<UsedSpectrum> allSpectra = cometScoredSpectra.get(index);
        // make a copy so we can remove the good spectra and leave the bad
        List<UsedSpectrum> notMatching = new ArrayList<UsedSpectrum>(allSpectra);
        // both algorithms scored these
        List<UsedSpectrum> matching = new ArrayList<UsedSpectrum>();

        // for each peptide
        for (IPolypeptide pp : scoredPeptides) {
            // for each spectrum
            for (UsedSpectrum usedSpectrum : notMatching) {
                BinChargeKey testKey = getPeptideKey(usedSpectrum.peptide);

                if (UsedSpectrum.equivalentPeptide(usedSpectrum.peptide, pp)) {
                    matching.add(usedSpectrum);
                    break;
                }
            }
        }
        // now leave the ones we did not score
        notMatching.removeAll(matching);

         BinChargeKey[] keys = BinChargeMapper.keysFromSpectrum(scan);

        for (UsedSpectrum usedSpectrum : notMatching) {
            BinChargeKey testKey = BinChargeMapper.oneKeyFromChargeMz(1, usedSpectrum.peptideMass);

            System.out.println("did not score " + usedSpectrum);
        }
        return notMatching.size();
    }

    public static int validatePeptideScore(IMeasuredSpectrum scan, IPolypeptide pp, double score) {
        if (cometScoredSpectra == null)
            return -1;   // not tracking
        String id = scan.getId();
        int index = Integer.parseInt(id);
        List<UsedSpectrum> usedSpectrums = cometScoredSpectra.get(index);

        double matchingMass = CometScoringAlgorithm.getCometMatchingMass(pp);
        //    double matchingMass = pp.getMatchingMass();
        BinChargeKey ppKey = getPeptideKey(pp);
        //    double matchingMass = pp.getMatchingMass();
        BinChargeKey[] keys = BinChargeMapper.keysFromSpectrum(scan);

        for (UsedSpectrum usedSpectrum : usedSpectrums) {
            BinChargeKey testKey = getPeptideKey(usedSpectrum.peptide);

            if (UsedSpectrum.equivalentPeptide(usedSpectrum.peptide, pp)) {

                double comet_score = usedSpectrum.score;
                if (Math.abs(comet_score - score) < 0.01)
                    return 0;  // all OK
                // todo fix
                return 1; // bad score
            }
        }
        return 2;   // not scored
        //  throw new IllegalStateException("comet did not score peptide " + pp);

    }

    /**
     * for debugging - look up from  UsedSpectra.txt what the comet score is
     * @param scan
     * @param pp
     * @return
     */
    public static double getCometScore(IMeasuredSpectrum scan, IPolypeptide pp) {
        if (cometScoredSpectra == null)
            return 0;   // not tracking
        String id = scan.getId();
        int index = Integer.parseInt(id);
        List<UsedSpectrum> usedSpectrums = cometScoredSpectra.get(index);

        for (UsedSpectrum usedSpectrum : usedSpectrums) {
            if (UsedSpectrum.equivalentPeptide(usedSpectrum.peptide, pp)) {
                double comet_score = usedSpectrum.score;
                return comet_score; // bad score
            }
        }
        return 0;   // not scored
    }

    public static void validateIndex(IMeasuredSpectrum scan) {
        String id = scan.getId();
        int index = Integer.parseInt(id);

        if (cometScoredSpectra == null)
            return;
        List<UsedSpectrum> usedSpectrums = cometScoredSpectra.get(index);
        if (usedSpectrums.isEmpty())
            throw new IllegalStateException("Comet did not score Spectrum " + index);

    }

    public static void assertEquals(double expected, double value) {
        if (Math.abs(expected - value) > 0.0001) {
            StringBuilder sb = new StringBuilder();
            sb.append("Unexpected value - expected " + expected + " got " + value);
            throw new IllegalStateException(sb.toString()); // ToDo change
        }
    }

    public static void testState(CometScoringAlgorithm alg) {
//        testWeightsState(alg);
//        testFastXcorrState(alg);
    }

    public static void testFastXcorrState(final CometScoringAlgorithm alg) {
        final double testIntensity;
        final double testIntensity1;
        final double testIntensity2;

//        float[] pdTmpFastXcorrData = alg.getFastXcorrDataNL();
//        testIntensity = pdTmpFastXcorrData[CometScoredScan.TEST_BIN];
//        assertEquals(0.6656652688980103, testIntensity);
//        testIntensity1 = pdTmpFastXcorrData[CometScoredScan.TEST_BIN + 1];
//        assertEquals(1.0, testIntensity1);
//        testIntensity2 = pdTmpFastXcorrData[CometScoredScan.TEST_BIN + 2];
//        assertEquals(1.6656652688980103, testIntensity2);
    }

//    public static void testUnNormalizedWeightsState(final CometScoringAlgorithm alg) {
//        List<SpectrinBinnedScore> seenPeaks = getSpectrinBinnedScores(alg);
//        List<SpectrinBinnedScore> cometPeaks = SpectrinBinnedScore.fromResource("/AfterLoadIons.data");
//        Collections.sort(cometPeaks);
//        comparePeakSets(cometPeaks, seenPeaks);
//    }
//
//    public static List<SpectrinBinnedScore> getSpectrinBinnedScores(final CometScoringAlgorithm alg) {
//        float[] binnedPeaks = alg.getWeights();
//        List<SpectrinBinnedScore> seenPeaks = SpectrinBinnedScore.fromBins(binnedPeaks);
//        Collections.sort(seenPeaks);
//        return seenPeaks;
//    }
//
//    public static List<SpectrinBinnedScore> getTmpFastXcorrData(final CometScoringAlgorithm alg) {
//        float[] binnedPeaks = alg.getTmpFastXcorrData();
//        List<SpectrinBinnedScore> seenPeaks = SpectrinBinnedScore.fromBins(binnedPeaks);
//        Collections.sort(seenPeaks);
//        return seenPeaks;
//    }
//
//    public static List<SpectrinBinnedScore> getScoringFastXcorrData(final CometScoringAlgorithm alg) {
//        float[] binnedPeaks = alg.getScoringFastXcorrData();
//        List<SpectrinBinnedScore> seenPeaks = SpectrinBinnedScore.fromBins(binnedPeaks);
//        Collections.sort(seenPeaks);
//        return seenPeaks;
//    }
//
//    public static List<SpectrinBinnedScore> getFastXcorrDataNL(final CometScoringAlgorithm alg) {
//        float[] binnedPeaks = alg.getFastXcorrDataNL();
//        List<SpectrinBinnedScore> seenPeaks = SpectrinBinnedScore.fromBins(binnedPeaks);
//        Collections.sort(seenPeaks);
//        return seenPeaks;
//    }

    public static void comparePeakSets(List<SpectrumBinnedScore> cometPeaks, List<SpectrumBinnedScore> seenPeaks) {
        int negative = 0;
        for (int i = 0; i < Math.min(cometPeaks.size(), seenPeaks.size()); i++) {
            SpectrumBinnedScore comet = cometPeaks.get(i);
            SpectrumBinnedScore seen = seenPeaks.get(i);
            boolean equivalent = seen.equivalent(comet);
            if (equivalent)
                continue;
            throw new IllegalStateException("problem"); // ToDo change
        }
        if (cometPeaks.size() != (negative + seenPeaks.size()))    // comet did not write negative peaks
            throw new IllegalStateException("different sizes");


    }

    private static float[] getWeights(final CometScoringAlgorithm alg, CometScoredScan scan) {
        CometScoringDataForScanBuild scoringData = CometScoringDataForScanBuild.getScoringData();
        return scoringData.getWeights();
    }

    private static float[] getTmpFastXcorrData(final CometScoringAlgorithm alg, CometScoredScan scan) {
        CometScoringDataForScanBuild scoringData = CometScoringDataForScanBuild.getScoringData();
        return scoringData.getWeights();
    }

    public static void testWeightsState(final CometScoringAlgorithm alg, CometScoredScan scan) {
        float[] binnedPeaks = getWeights(alg, scan);
        double testIntensity = binnedPeaks[CometScoredScan.TEST_BIN];
        assertEquals(50, testIntensity);
        double testIntensity1 = binnedPeaks[CometScoredScan.TEST_BIN + 1];
        assertEquals(33.283268, testIntensity1);
        double testIntensity2 = binnedPeaks[CometScoredScan.TEST_BIN + 2];
    }

//
//    public static void testWindowedIntensities(final CometScoringAlgorithm alg, CometScoredScan scan) {
//        List<SpectrinBinnedScore> seenPeaks = getSpectrinBinnedScores(alg);
//        List<SpectrinBinnedScore> cometPeaks = SpectrinBinnedScore.fromResource("/AfterMakeCorrDatRaw.data");
//        Collections.sort(cometPeaks);
//        comparePeakSets(cometPeaks, seenPeaks);
//
//        cometPeaks = SpectrinBinnedScore.fromResource("/AfterMakeCorrData.data");
//        Collections.sort(cometPeaks);
//        comparePeakSets(cometPeaks, seenPeaks);
//
//    }


    public static void testFastXcorrDataAtXCorr(final CometScoringAlgorithm alg, CometScoredScan scan) {
        List<SpectrumBinnedScore> seenPeaks = scan.getFastScoringData();
        List<SpectrumBinnedScore> cometPeaks = SpectrumBinnedScore.fromResource("/pfFastXcorrDataAtXCorr.data");
        Collections.sort(cometPeaks);
        comparePeakSets(cometPeaks, seenPeaks);

    }
//
//    public static void testNormalizedBinnedIntensities(final CometScoringAlgorithm alg) {
//        List<SpectrinBinnedScore> seenPeaks = getScoringFastXcorrData(alg);
//        Collections.sort(seenPeaks);
//        List<SpectrinBinnedScore> cometPeaks = SpectrinBinnedScore.fromResource("/AfterMakeXCorrSpectrum.data");
//        Collections.sort(cometPeaks);
//        comparePeakSets(cometPeaks, seenPeaks);
//
//    }
//
//    public static void testNLValues(final CometScoringAlgorithm alg) {
//        List<SpectrinBinnedScore> seenPeaks = getFastXcorrDataNL(alg);
//        Collections.sort(seenPeaks);
//        List<SpectrinBinnedScore> cometPeaks = SpectrinBinnedScore.fromResource("/AfterAddFlankingPeaksNL.data");
//        Collections.sort(cometPeaks);
//        comparePeakSets(cometPeaks, seenPeaks);
//
//        seenPeaks = getScoringFastXcorrData(alg);
//        Collections.sort(seenPeaks);
//        cometPeaks = SpectrinBinnedScore.fromResource("/AfterAddFlankingPeaks.data");
//        Collections.sort(cometPeaks);
//        comparePeakSets(cometPeaks, seenPeaks);
//    }


    public static void testCalculations(CometScoringAlgorithm alg, final float[] pPdTmpFastXcorrData, final float[] pPfFastXcorrDataNL) {
        double testIntensity;
        double testIntensity1;
        double testIntensity2;
        testIntensity = pPdTmpFastXcorrData[CometScoredScan.TEST_BIN];
        assertEquals(0.66566539, testIntensity);
        testIntensity = pPfFastXcorrDataNL[CometScoredScan.TEST_BIN];
        assertEquals(49.334335, testIntensity);
        testIntensity1 = pPdTmpFastXcorrData[CometScoredScan.TEST_BIN + 1];
        assertEquals(1, testIntensity1);
        testIntensity1 = pPfFastXcorrDataNL[CometScoredScan.TEST_BIN + 1];
        assertEquals(32.283268, testIntensity1);
        testIntensity2 = pPdTmpFastXcorrData[CometScoredScan.TEST_BIN + 2];
        assertEquals(1.6656654, testIntensity2);
        testIntensity2 = pPfFastXcorrDataNL[CometScoredScan.TEST_BIN + 2];
        assertEquals(-1.6656654, testIntensity2);
    }

    public static void testCometConfiguration(final CometScoringAlgorithm pComet) {
        assertEquals(0.6, pComet.getOneMinusBinOffset());
        double binTolerance = pComet.getBinTolerance();
        assertEquals(0.02, binTolerance);
        assertEquals(30.0, pComet.getMassTolerance());


        //     pComet.clearData();

        assertEquals(901, pComet.iMinus17);
        assertEquals(851, pComet.iMinus18);
    }

    /**
     * validate that members of an array with values > 0.001 match a resource written by
     * comet
     *
     * @param testData
     * @param resourceToCompare
     */
    public static void validateArray(float[] testData, String resourceToCompare) {
        List<SpectrumBinnedScore> holder = new ArrayList<SpectrumBinnedScore>();
        for (int i = 0; i < testData.length; i++) {
            float v = testData[i];
            if (Math.abs(v) > 0.001)
                holder.add(new SpectrumBinnedScore(i, testData[i]));
        }
        List<SpectrumBinnedScore> cometPeaks = SpectrumBinnedScore.fromResource(resourceToCompare);
        Collections.sort(cometPeaks);
        comparePeakSets(cometPeaks, holder);

    }

    /**
     * return values from a resource file as a map with index as key
     *
     * @param resourceToCompare
     */
    public static Map<Integer, SpectrumBinnedScore> getResourceMap(String resourceToCompare) {
        List<SpectrumBinnedScore> cometPeaks = SpectrumBinnedScore.fromResource(resourceToCompare);
        return asMap(cometPeaks);
    }

    /**
     * return make a list of peaks into a map
     *
     * @param cometPeaks
     */
    public static Map<Integer, SpectrumBinnedScore> asMap(List<SpectrumBinnedScore> cometPeaks) {
        Map<Integer, SpectrumBinnedScore> ret = new HashMap<Integer, SpectrumBinnedScore>();
        for (SpectrumBinnedScore cometPeak : cometPeaks) {
            ret.put(cometPeak.bin, cometPeak);
        }
        return ret;

    }


    /**
     * return all peptides in UsedBins - used in testing
     * @param usedBins  - bins to select peptides
     * @param handler  handler to get bins
     * @return
     */
    public static List<IPolypeptide> getScoredPeptides(Set<Integer> usedBins, CometScoringHandler handler) {
        Properties sparkProperties = SparkUtilities.getSparkProperties();
        JavaPairRDD<BinChargeKey, HashMap<String, IPolypeptide>> keyedPeptides = SparkCometScanScorer.getBinChargePeptideHash(sparkProperties, usedBins, handler);

        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();

        keyedPeptides = SparkUtilities.persist(keyedPeptides);
        Map<BinChargeKey, HashMap<String, IPolypeptide>> binChargeKeyHashMapMap = keyedPeptides.collectAsMap();
        List<HashMap<String, IPolypeptide>> collect1 = keyedPeptides.values().collect();
        for (HashMap<String, IPolypeptide> hms : collect1) {
            for (IPolypeptide pp : hms.values()) {
                holder.add(pp);
            }
        }
        return holder;

    }
}
