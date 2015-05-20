package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.utilities.ElapsedTimer;
import com.lordjoe.utilities.FileUtilities;
import org.junit.Assert;
import org.junit.Test;
import org.systemsbiology.xtandem.RawPeptideScan;
import org.systemsbiology.xtandem.XTandemMain;
import org.systemsbiology.xtandem.ionization.IonUseCounter;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.peptide.Polypeptide;
import org.systemsbiology.xtandem.scoring.Scorer;
import org.systemsbiology.xtandem.testing.MZXMLReader;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringSpeedTest
 *
 * @author Steve Lewis
 * @date 5/12/2015
 */
public class CometScoringAccuracyTest {

    @Test
    public void testAccuracy() {
        XTandemMain.setShowParameters(false);  // I do not want to see parameters

        InputStream is = new StringBufferInputStream(CometTestData.COMET_XML);
        XTandemMain application = new XTandemMain(is, "TANDEM_XML");
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        comet.configure(application);

        final Class<CometScoringAccuracyTest> cls = CometScoringAccuracyTest.class;
        InputStream istr = cls.getResourceAsStream("/000000008852.mzXML");

        final String scanTag = FileUtilities.readInFile(istr);
        RawPeptideScan rp = MZXMLReader.handleScan(scanTag);
        CometScoredScan spec = new CometScoredScan(rp, comet);

        Scorer scorer = application.getScoreRunner();

        istr = cls.getResourceAsStream("/UsedSpectraComet.txt");
        Map<Integer, List<UsedSpectrum>> spectraMap = UsedSpectrum.readUsedSpectra(istr);
        //st
        List<UsedSpectrum> allused = spectraMap.get(8852);
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

        for (UsedSpectrum testCase : used) {
            IPolypeptide pp = testCase.peptide;

            double cometScore = testCase.score;
            CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pp);
            IonUseCounter counter = new IonUseCounter();
            double xcorr = CometScoringHandler.doRealScoring(scan, scorer, ts, application);
            maxScore = Math.max(xcorr, maxScore);

            if(Math.abs(cometScore - xcorr) < 0.01)   {
                numberCorrect++;
             }
            else {
                numberInCorrect++;
            }


        }
        Assert.assertEquals("Missed after " + numberCorrect + " of " + numberTested,numberTested,numberCorrect);

    }
 }

