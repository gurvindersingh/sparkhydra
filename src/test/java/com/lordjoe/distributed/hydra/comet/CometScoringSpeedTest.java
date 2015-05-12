package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.hydra.*;
import com.lordjoe.utilities.*;
import org.junit.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.sax.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.testing.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.Co
 *
 * @author Steve Lewis
 * @date 5/12/2015
 */
public class CometScoringSpeedTest {

    public static void main(String[] args) {

        XTandemMain.setShowParameters(false);  // I do not want to see parameters


        InputStream is = new StringBufferInputStream(CometTestData.COMET_XML);
        XTandemMain application = new XTandemMain(is, "TANDEM_XML");
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        comet.configure(application);

        final Class<CometScoringSpeedTest> cls = CometScoringSpeedTest.class;
        InputStream istr = cls.getResourceAsStream("/000000006774.mzXML");

        final String scanTag = FileUtilities.readInFile(istr);
        RawPeptideScan rp = MZXMLReader.handleScan(scanTag);
        CometScoredScan spec = new CometScoredScan(rp, comet);

        Scorer scorer = application.getScoreRunner();

        istr = cls.getResourceAsStream("/000000006774.peptide");
        List<IPolypeptide> peptides = readPeptides(istr);


        ElapsedTimer timer = new ElapsedTimer();

        CometScoredScan scan = new CometScoredScan(spec, comet);

        final CometScoringData scoringData = CometScoringData.getScoringData();


        double maxScore = 0;
        int index = 0;
        long totalTime = 0;
        double secPerPeptide = 0;
        double timePerMillionScores = 0;

        if (true) {
            for (IPolypeptide pp : peptides) {
                CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pp);
                IonUseCounter counter = new IonUseCounter();
                double xcorr = CometScoringHandler.doRealScoring(scan, scorer, ts, application);
                maxScore = Math.max(xcorr, maxScore);
            }
            totalTime = timer.getElapsedMillisec();
            secPerPeptide = totalTime / (1000.0 * peptides.size());
            timePerMillionScores = 1000000 * secPerPeptide;
            timer.showElapsed("Map and generate ts Scored " + peptides.size() + " peptides at " + secPerPeptide + " for 50million " + (50 * timePerMillionScores / 3600) + " hours" + " max " + maxScore);
        }

        // in this section we pregenerate data for a spectrum and reuse it
        scoringData.clearData();

        final Map<Integer, Float> fastScoringMap = scan.getFastScoringMap();

        float[] fastXcorrDataMap = scoringData.getTmpFastXcorrData();
        for (Integer i : fastScoringMap.keySet()) {
            fastXcorrDataMap[i] = fastScoringMap.get(i);
        }

        final Map<Integer, Float> fastScoringMapNL = scan.getFastScoringMapNL();   // we used to get from commented scoring data
        float[] fastXcorrDataNL = scoringData.getTmpFastXcorrData2();
        for (Integer i : fastScoringMapNL.keySet()) {
            fastXcorrDataNL[i] = fastScoringMapNL.get(i);
        }



        // use pregenerated spectrum data but not epetide data
        List<CometTheoreticalBinnedSet> holder = new ArrayList<CometTheoreticalBinnedSet>();
        if (true) {
            for (IPolypeptide pp : peptides) {
                CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pp);
                IonUseCounter counter = new IonUseCounter();
                double xcorr = comet.doXCorrWithData(ts, scorer, counter, scan, fastXcorrDataMap, fastXcorrDataNL);
                maxScore = Math.max(xcorr, maxScore);
            }

            totalTime = timer.getElapsedMillisec();
            secPerPeptide = totalTime / (1000.0 * peptides.size());
            timePerMillionScores = 1000000 * secPerPeptide;
            timer.showElapsed("generate ts  Scored " + peptides.size() + " peptides at " + secPerPeptide + " for 50million " + (50 * timePerMillionScores / 3600) + " hours" + " max " + maxScore);
            ;
        }


        // just populate peptide data do not count time
        for (IPolypeptide pp : peptides) {
            CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pp);
            holder.add(ts);
        }

        timer.reset();

        // use pregenerated peptide data but not epetide data

        for (CometTheoreticalBinnedSet ts : holder) {
            IonUseCounter counter = new IonUseCounter();
            double xcorr = comet.doXCorrWithData(ts, scorer, counter, scan, fastXcorrDataMap, fastXcorrDataNL);
            maxScore = Math.max(xcorr, maxScore);
        }


        totalTime = timer.getElapsedMillisec();
        secPerPeptide = totalTime / (1000.0 * peptides.size());
        timePerMillionScores = 1000000 * secPerPeptide;
        timer.showElapsed("no map cache ts Scored " + peptides.size() + " peptides at " + secPerPeptide + " for 50million " + (50 * timePerMillionScores / 3600) + " hours" + " max " + maxScore);
    }


    public static List<IPolypeptide> readPeptides(InputStream is) {
        List<IPolypeptide> ret = new ArrayList<IPolypeptide>();
        try {
            LineNumberReader rdr = new LineNumberReader(new InputStreamReader(is));
            String line = rdr.readLine();
            while (line != null) {
                IPolypeptide pp = Polypeptide.fromString(line);
                ret.add(pp);
                line = rdr.readLine();
            }
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                // forgive
            }
        }
        return ret;
    }
}

