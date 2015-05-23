package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.hydra.test.TestUtilities;
import com.lordjoe.utilities.ElapsedTimer;
import com.lordjoe.utilities.FileUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.systemsbiology.xtandem.RawPeptideScan;
import org.systemsbiology.xtandem.XTandemMain;
import org.systemsbiology.xtandem.ionization.IonUseCounter;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.peptide.Polypeptide;
import org.systemsbiology.xtandem.scoring.CometHyperScoreStatistics;
import org.systemsbiology.xtandem.scoring.HyperScoreStatistics;
import org.systemsbiology.xtandem.scoring.Scorer;
import org.systemsbiology.xtandem.testing.MZXMLReader;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringAccuracyTest
 *
 * @author Steve Lewis
 * @date 5/12/2015
 */
public class CometScoringAccuracyTest {

    public static class CompareByScore implements Comparator<UsedSpectrum>    {

        @Override
        public int compare(UsedSpectrum o1, UsedSpectrum o2) {
            int ret = Double.compare(o2.score,o1.score);
            if(ret != 0 )
                return ret;
             return o1.peptide.toString().compareTo(o2.peptide.toString());
        }
    }
  //  @Test
    public void testExpectedValue()
    {
        final Class<CometScoringAccuracyTest> cls = CometScoringAccuracyTest.class;
        InputStream istr = cls.getResourceAsStream("/UsedSpectraComet.txt");
        Map<Integer, List<UsedSpectrum>> spectraMap = UsedSpectrum.readUsedSpectra(istr);
        for (Integer id : spectraMap.keySet()) {
            // this case has a good expected value
            List<UsedSpectrum> allused = spectraMap.get(id);

            double maxScore = 0;
            CometHyperScoreStatistics hyperscore = new CometHyperScoreStatistics();
            for (UsedSpectrum usedSpectrum : allused) {
                double score = usedSpectrum.score;
                maxScore = Math.max(maxScore,score);
                System.out.println(score);
                hyperscore.add(score);
            }
            double expectedValue = hyperscore.getExpectedValue(maxScore);
            if(id == 8852)   {
                Assert.assertEquals(1.407,maxScore,0.01);
                Assert.assertEquals(5.07E-004,expectedValue,0.001);
            }

        }

        // this case has a good expected value
        List<UsedSpectrum> allused = spectraMap.get(8852);

        // sort by score
        Collections.sort(allused,new CompareByScore());



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
    public void testAccuracy() {


        CometTesting.validateOneKey(); // We are hunting for when this stops working

        //   FileUtilities.writeFile("BadParametersX,xml",CometTestData.USED_PARAMETERS);
    //    FileUtilities.writeFile("GoodParameters,xml",CometTestData.COMET_XML);

         XTandemMain.setShowParameters(false);  // I do not want to see parameters

        InputStream is = new StringBufferInputStream(CometTestData.COMET_XML); //USED_PARAMETERS); // old was COMET_XML);
        XTandemMain application = new XTandemMain(is, "TANDEM_XML");
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        comet.configure(application);

        CometTesting.validateOneKey(); // We are hunting for when this stops working

        final Class<CometScoringAccuracyTest> cls = CometScoringAccuracyTest.class;
        InputStream istr = cls.getResourceAsStream("/000000008852.mzXML");

        final String scanTag = FileUtilities.readInFile(istr);
        RawPeptideScan rp = MZXMLReader.handleScan(scanTag);
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
        double xcorr1 = CometScoringHandler.doRealScoring(scan, scorer, ts1, application);
        Assert.assertEquals(2.070,xcorr1,0.002);

        IPolypeptide interestimgCase = Polypeptide.fromString("NIKPECPTLACGQPR");

        for (UsedSpectrum testCase : used) {
            IPolypeptide pp = testCase.peptide;
            if(pp.equivalent(interestimgCase))
                TestUtilities.breakHere();

            double cometScore = testCase.score;
            CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pp);
            double xcorr = CometScoringHandler.doRealScoring(scan, scorer, ts, application);


            if(maxScore < xcorr)
                maxScore = Math.max(xcorr, maxScore);

            if(Math.abs(cometScore - xcorr) < 0.01)   {
                numberCorrect++;
             }
            else {
                badlyScoredExpected.add(testCase);
                badlyScored.add(ts);
                numberInCorrect++;
            }



        }

        // now look at the bad cases in detail
        int index = 0;
        for (UsedSpectrum usedSpectrum : badlyScoredExpected) {
            CometTheoreticalBinnedSet tsx_old = badlyScored.get(index++);
            double xcorr_repeat = CometScoringHandler.doRealScoring(scan, scorer, tsx_old, application);
            double score = usedSpectrum.score;
            if(Math.abs(xcorr_repeat - score) < 0.1)
                continue;


            IPolypeptide pPeptide = tsx_old.getPeptide();
            CometTheoreticalBinnedSet tsx = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pPeptide);
           double xcorr_repeat2 = CometScoringHandler.doRealScoring(scan, scorer, tsx, application);
        }

        Assert.assertTrue(numberCorrect + 1 >= numberTested);
    //    Assert.assertEquals("Missed after " + numberCorrect + " of " + numberTested,numberTested,numberCorrect);

    }
 }

