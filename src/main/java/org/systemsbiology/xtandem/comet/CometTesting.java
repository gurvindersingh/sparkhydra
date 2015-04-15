package org.systemsbiology.xtandem.comet;

import java.util.*;

/**
 * org.systemsbiology.xtandem.comet.CometTesting
 * Test values for handling EG) case
 * User: Steve
 * Date: 4/1/2015
 */
public class CometTesting {
    public static void assertEquals(double expected, double value) {
        if (Math.abs(expected - value) > 0.0001) {
            // System.out.println("expected " + expected + " got " + value);
            throw new IllegalStateException("problem"); // ToDo change
        }
    }

    public static void testState(CometScoringAlgorithm alg) {
        testWeightsState(alg);
        testFastXcorrState(alg);
    }

    public static void testFastXcorrState(final CometScoringAlgorithm alg) {
        final double testIntensity;
        final double testIntensity1;
        final double testIntensity2;

        float[] pdTmpFastXcorrData = alg.getFastXcorrDataNL();
        testIntensity = pdTmpFastXcorrData[CometScoredScan.TEST_BIN];
        assertEquals(0.6656652688980103, testIntensity);
        testIntensity1 = pdTmpFastXcorrData[CometScoredScan.TEST_BIN + 1];
        assertEquals(1.0, testIntensity1);
        testIntensity2 = pdTmpFastXcorrData[CometScoredScan.TEST_BIN + 2];
        assertEquals(1.6656652688980103, testIntensity2);
    }

    public static void testUnNormalizedWeightsState(final CometScoringAlgorithm alg) {
        List<SpectrinBinnedScore> seenPeaks = getSpectrinBinnedScores(alg);
        List<SpectrinBinnedScore> cometPeaks = SpectrinBinnedScore.fromResource("/AfterLoadIons.data");
        Collections.sort(cometPeaks);
        comparePeakSets(cometPeaks, seenPeaks);
    }

    public static List<SpectrinBinnedScore> getSpectrinBinnedScores(final CometScoringAlgorithm alg) {
        float[] binnedPeaks = alg.getWeights();
        List<SpectrinBinnedScore> seenPeaks = SpectrinBinnedScore.fromBins(binnedPeaks);
        Collections.sort(seenPeaks);
        return seenPeaks;
    }

    public static List<SpectrinBinnedScore> getTmpFastXcorrData(final CometScoringAlgorithm alg) {
        float[] binnedPeaks = alg.getTmpFastXcorrData();
        List<SpectrinBinnedScore> seenPeaks = SpectrinBinnedScore.fromBins(binnedPeaks);
        Collections.sort(seenPeaks);
        return seenPeaks;
    }

    public static List<SpectrinBinnedScore> getScoringFastXcorrData(final CometScoringAlgorithm alg) {
        float[] binnedPeaks = alg.getScoringFastXcorrData();
        List<SpectrinBinnedScore> seenPeaks = SpectrinBinnedScore.fromBins(binnedPeaks);
        Collections.sort(seenPeaks);
        return seenPeaks;
    }

    public static List<SpectrinBinnedScore> getFastXcorrDataNL(final CometScoringAlgorithm alg) {
        float[] binnedPeaks = alg.getFastXcorrDataNL();
        List<SpectrinBinnedScore> seenPeaks = SpectrinBinnedScore.fromBins(binnedPeaks);
        Collections.sort(seenPeaks);
        return seenPeaks;
    }

    public static void comparePeakSets(List<SpectrinBinnedScore> cometPeaks, List<SpectrinBinnedScore> seenPeaks) {
        for (int i = 0; i < Math.min(cometPeaks.size(), seenPeaks.size()); i++) {
            SpectrinBinnedScore comet = cometPeaks.get(i);
            SpectrinBinnedScore seen = seenPeaks.get(i);
            boolean equivalent = seen.equivalent(comet);
            if (equivalent)
                continue;
            throw new IllegalStateException("problem"); // ToDo change
        }
        if (cometPeaks.size() != seenPeaks.size())
            throw new IllegalStateException("different sizes");


    }

    public static void testWeightsState(final CometScoringAlgorithm alg) {
        float[] binnedPeaks = alg.getWeights();
        double testIntensity = binnedPeaks[CometScoredScan.TEST_BIN];
        assertEquals(50, testIntensity);
        double testIntensity1 = binnedPeaks[CometScoredScan.TEST_BIN + 1];
        assertEquals(33.283268, testIntensity1);
        double testIntensity2 = binnedPeaks[CometScoredScan.TEST_BIN + 2];
    }


    public static void testWindowedIntensities(final CometScoringAlgorithm alg) {
        List<SpectrinBinnedScore> seenPeaks = getSpectrinBinnedScores(alg);
        List<SpectrinBinnedScore> cometPeaks = SpectrinBinnedScore.fromResource("/AfterMakeCorrDatRaw.data");
        Collections.sort(cometPeaks);
        comparePeakSets(cometPeaks, seenPeaks);

        seenPeaks = getTmpFastXcorrData(alg);
        cometPeaks = SpectrinBinnedScore.fromResource("/AfterMakeCorrData.data");
        Collections.sort(cometPeaks);
        comparePeakSets(cometPeaks, seenPeaks);

    }


    public static void testFastXcorrDataAtXCorr(final CometScoringAlgorithm alg) {
        List<SpectrinBinnedScore> seenPeaks = getScoringFastXcorrData(alg);
        Collections.sort(seenPeaks);
        List<SpectrinBinnedScore> cometPeaks = SpectrinBinnedScore.fromResource("/pfFastXcorrDataAtXCorr.data");
        Collections.sort(cometPeaks);
        comparePeakSets(cometPeaks, seenPeaks);

    }

    public static void testNormalizedBinnedIntensities(final CometScoringAlgorithm alg) {
        List<SpectrinBinnedScore> seenPeaks = getScoringFastXcorrData(alg);
        Collections.sort(seenPeaks);
        List<SpectrinBinnedScore> cometPeaks = SpectrinBinnedScore.fromResource("/AfterMakeXCorrSpectrum.data");
        Collections.sort(cometPeaks);
        comparePeakSets(cometPeaks, seenPeaks);

    }

    public static void testNLValues(final CometScoringAlgorithm alg) {
        List<SpectrinBinnedScore> seenPeaks = getFastXcorrDataNL(alg);
        Collections.sort(seenPeaks);
        List<SpectrinBinnedScore> cometPeaks = SpectrinBinnedScore.fromResource("/AfterAddFlankingPeaksNL.data");
        Collections.sort(cometPeaks);
        comparePeakSets(cometPeaks, seenPeaks);

        seenPeaks = getScoringFastXcorrData(alg);
        Collections.sort(seenPeaks);
        cometPeaks = SpectrinBinnedScore.fromResource("/AfterAddFlankingPeaks.data");
        Collections.sort(cometPeaks);
        comparePeakSets(cometPeaks, seenPeaks);
    }


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
        assertEquals(30.0 , pComet.getMassTolerance());


        pComet.clearData();

        assertEquals(901, pComet.iMinus17);
        assertEquals(851, pComet.iMinus18);
    }

}
