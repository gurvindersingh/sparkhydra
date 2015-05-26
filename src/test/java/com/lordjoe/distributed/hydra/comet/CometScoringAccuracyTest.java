package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.hydra.fragment.BinChargeKey;
import com.lordjoe.distributed.hydra.test.TestUtilities;
import com.lordjoe.distributed.protein.ProteinParser;
import org.junit.Assert;
import org.junit.Test;
import org.systemsbiology.xtandem.RawPeptideScan;
import org.systemsbiology.xtandem.XTandemMain;
import org.systemsbiology.xtandem.peptide.IPeptideDigester;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.peptide.PeptideBondDigester;
import org.systemsbiology.xtandem.peptide.Polypeptide;
import org.systemsbiology.xtandem.scoring.CometHyperScoreStatistics;
import org.systemsbiology.xtandem.scoring.Scorer;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringAccuracyTest
 *
 * @author Steve Lewis
 * @date 5/12/2015
 */
public class CometScoringAccuracyTest {

    public static class CompareByScore implements Comparator<UsedSpectrum> {

        @Override
        public int compare(UsedSpectrum o1, UsedSpectrum o2) {
            int ret = Double.compare(o2.score, o1.score);
            if (ret != 0)
                return ret;
            return o1.peptide.toString().compareTo(o2.peptide.toString());
        }
    }


    //  @Test
    public void testExpectedValue() {
        Map<Integer, List<UsedSpectrum>> spectraMap =
                CometTestingUtilities.readUsedSpectraFromResource("/UsedSpectraComet.txt");
        for (Integer id : spectraMap.keySet()) {
            // this case has a good expected value
            List<UsedSpectrum> allused = spectraMap.get(id);

            double maxScore = 0;
            CometHyperScoreStatistics hyperscore = new CometHyperScoreStatistics();
            for (UsedSpectrum usedSpectrum : allused) {
                double score = usedSpectrum.score;
                maxScore = Math.max(maxScore, score);
                System.out.println(score);
                hyperscore.add(score);
            }
            double expectedValue = hyperscore.getExpectedValue(maxScore);
            if (id == 8852) {
                Assert.assertEquals(1.407, maxScore, 0.01);
                Assert.assertEquals(5.07E-004, expectedValue, 0.001);
            }

        }

        // this case has a good expected value
        List<UsedSpectrum> allused = spectraMap.get(8852);

        // sort by score
        Collections.sort(allused, new CompareByScore());



        /*
         <search_score name="xcorr" value="1.407"/>
    <search_score name="deltacn" value="0.915"/>
    <search_score name="deltacnstar" value="0.000"/>
    <search_score name="spscore" value="82.6"/>
    <search_score name="sprank" value="1"/>
    <search_score name="expect" value="5.07E-004"/>

         */


    }

    @Test
    public void test_20Accuracy() {


        CometTesting.validateOneKey(); // We are hunting for when this stops working

        XTandemMain application = CometTestingUtilities.getDefaultApplication();
        CometScoringAlgorithm comet = CometTestingUtilities.getComet(application);
        Scorer scorer = application.getScoreRunner();

        CometTesting.validateOneKey(); // We are hunting for when this stops working
        // todo add more
        Map<Integer, List<UsedSpectrum>> cometUses = CometTestingUtilities.readUsedSpectraFromResource("/eg3_20/UsedSpectra_20.txt");

        List<UsedSpectrum> forScan2 = cometUses.get(2);


        IPeptideDigester digester = PeptideBondDigester.getDefaultDigester();
        digester.setNumberMissedCleavages(2);

        List<IPolypeptide> originalPeptides = ProteinParser.getPeptidesFromResource("/eg3_20/select_20.fasta", digester,
                CometTestingUtilities.MS_ONLY);

        Map<Integer, RawPeptideScan> mapped = CometTestingUtilities.getScanMapFromResource("/eg3_20/eg3_20.mzXML");

        RawPeptideScan scan2 = mapped.get(2);
        CometScoredScan spec = new CometScoredScan(scan2, comet);

        Set<BinChargeKey> spectrumBins = BinChargeMapper.getSpectrumBins(scan2);

        IPolypeptide cometBest = Polypeptide.fromString("SADAMS[79.966331]S[79.966331]DK");
        BinChargeKey ppk = BinChargeMapper.keyFromPeptide(cometBest);
        Assert.assertTrue(spectrumBins.contains(ppk));


        CometScoringData.populateFromScan(spec);

        CometTheoreticalBinnedSet cometTs = (CometTheoreticalBinnedSet) scorer.generateSpectrum(cometBest);

        double cometBestScore = CometScoringAlgorithm.doRealScoring(spec, scorer, cometTs, application);
        Assert.assertEquals(0.152, cometBestScore, 0.01);

        double bestScore = 0;
        IPolypeptide bestPeptide = null;
        List<IPolypeptide> inBins = CometUtilities.getPeptidesInKeyBins(originalPeptides, spectrumBins);
        for (IPolypeptide inBin : inBins) {
            if (cometBest.equals(inBin)) {
                cometTs = (CometTheoreticalBinnedSet) scorer.generateSpectrum(inBin);
                cometBestScore = CometScoringAlgorithm.doRealScoring(spec, scorer, cometTs, application);
                Assert.assertEquals(0.152, cometBestScore, 0.01);
            }
            CometTheoreticalBinnedSet ts1 = (CometTheoreticalBinnedSet) scorer.generateSpectrum(inBin);

            double xcorr1 = CometScoringAlgorithm.doRealScoring(spec, scorer, ts1, application);
            if (xcorr1 > bestScore) {
                bestScore = xcorr1;
                bestPeptide = inBin;
            }
        }
        Assert.assertTrue(bestPeptide.equivalent(cometBest));
    }

    @Test
    public void testAccuracy() {

        XTandemMain application = CometTestingUtilities.getDefaultApplication();
        CometScoringAlgorithm comet = CometTestingUtilities.getComet(application);

        CometTesting.validateOneKey(); // We are hunting for when this stops working


        RawPeptideScan rp = CometTestingUtilities.getScanFromMZXMLResource("/000000008852.mzXML");
        CometScoredScan spec = new CometScoredScan(rp, comet);

        Scorer scorer = application.getScoreRunner();

        //st
        List<UsedSpectrum> allused = CometTestingUtilities.getSpectrumUsed(8852);

        List<UsedSpectrum> used = new ArrayList<UsedSpectrum>();
        for (UsedSpectrum usedSpectrum : allused) {
            IPolypeptide peptide = usedSpectrum.peptide;
            //     if (!peptide.isModified())
            used.add(usedSpectrum);

        }


        CometScoredScan scan = new CometScoredScan(spec, comet);


        double maxScore = 0;
        int numberCorrect = 0;
        int numberInCorrect = 0;
        int numberTested = used.size();

        List<CometTheoreticalBinnedSet> badlyScored = new ArrayList<CometTheoreticalBinnedSet>();
        List<UsedSpectrum> badlyScoredExpected = new ArrayList<UsedSpectrum>();

        IPolypeptide intersting = Polypeptide.fromString("M[15.995]PCTEDYLSLILNR");

        CometTheoreticalBinnedSet ts1 = (CometTheoreticalBinnedSet) scorer.generateSpectrum(intersting);

        CometScoringData.populateFromScan(scan);
        double xcorr1 = CometScoringAlgorithm.doRealScoring(scan, scorer, ts1, application);
        Assert.assertEquals(2.070, xcorr1, 0.002);

        IPolypeptide interestimgCase = Polypeptide.fromString("NIKPECPTLACGQPR");

        for (UsedSpectrum testCase : used) {
            IPolypeptide pp = testCase.peptide;
            if (pp.equivalent(interestimgCase))
                TestUtilities.breakHere();

            double cometScore = testCase.score;
            CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pp);
            double xcorr = CometScoringAlgorithm.doRealScoring(scan, scorer, ts, application);


            if (maxScore < xcorr)
                maxScore = Math.max(xcorr, maxScore);

            if (Math.abs(cometScore - xcorr) < 0.01) {
                numberCorrect++;
            } else {
                badlyScoredExpected.add(testCase);
                badlyScored.add(ts);
                numberInCorrect++;
            }


        }

        // now look at the bad cases in detail
        int index = 0;
        for (UsedSpectrum usedSpectrum : badlyScoredExpected) {
            CometTheoreticalBinnedSet tsx_old = badlyScored.get(index++);
            double xcorr_repeat = CometScoringAlgorithm.doRealScoring(scan, scorer, tsx_old, application);
            double score = usedSpectrum.score;
            if (Math.abs(xcorr_repeat - score) < 0.1)
                continue;


            IPolypeptide pPeptide = tsx_old.getPeptide();
            CometTheoreticalBinnedSet tsx = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pPeptide);
            double xcorr_repeat2 = CometScoringAlgorithm.doRealScoring(scan, scorer, tsx, application);
        }

        Assert.assertTrue(numberCorrect + 1 >= numberTested);
        //    Assert.assertEquals("Missed after " + numberCorrect + " of " + numberTested,numberTested,numberCorrect);

    }
}

