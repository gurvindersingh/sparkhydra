package org.systemsbiology.xtandem.comet;

import com.lordjoe.algorithms.*;
import com.lordjoe.distributed.hydra.comet.*;
import com.lordjoe.distributed.hydra.fragment.*;
import com.lordjoe.distributed.protein.*;
import org.junit.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.comet.EngTests
 * User: Steve
 * Date: 9/11/2015
 */
public class EngTests {


    public void doTest(String base, int numberProteins) throws Exception {


        XTandemMain application = CometTestingUtilities.getDefaultApplication2();
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];

        InputStream is = EngTests.class.getResourceAsStream(base + "comet.params.2015020");

        Assert.assertNotNull(is);
        CometUtilities.fromCometParameters(application, is);

        comet.reconfigure(application);

//        Double fragment_bin_tol = application.getDoubleParameter("comet.fragment_bin_tol", BinChargeMapper.getBinSize());
//        BinChargeMapper.setBinSize(fragment_bin_tol);
        // add Comet Parameters
        //CometUtilities.fromCometParameters(main, Util.asLineReader(paramsStr));
        List<RawPeptideScan> allScanFromMZXMLResource = CometTestingUtilities.getAllScanFromMZXMLResource(base + "input.mzXML");
        Assert.assertEquals(1, allScanFromMZXMLResource.size());
        RawPeptideScan scan = allScanFromMZXMLResource.get(0);
        Assert.assertNotNull(scan);

        List<CometScoredScan> scoringScans = getCometScoredScans(comet, allScanFromMZXMLResource);
        MapOfLists<BinChargeKey, CometScoredScan> scoreBins = mapToScoreBins(scoringScans);


        List<IProtein> proteins = ProteinParser.getProteinsFromResource(base + "tmp.db");
        Assert.assertEquals(numberProteins, proteins.size());

        DigestProteinFunction digester = new DigestProteinFunction(application);

        List<IPolypeptide> pps = digester.digestWithModifications(proteins);

        List<Tuple2<ITheoreticalSpectrumSet, CometScoredScan>>  tuples = buildTuples(application,pps,scoringScans) ;

        Map<String, CometScoringResult> allScores = scoreTuples(application, tuples);

//        MapOfLists<BinChargeKey, ITheoreticalSpectrumSet> biBin = mapToBins(application, pps);
//
//        MapOfLists<BinChargeKey, Tuple2<ITheoreticalSpectrumSet, CometScoredScan>> scpredPair = CometTestingUtilities.join(biBin, scoreBins);
//
//        Map<String, CometScoringResult> allScores = scoreTuples(application, scpredPair);

        for (String s : allScores.keySet()) {
            CometScoringResult res = allScores.get(s);
            ISpectralMatch bestMatch = res.getBestMatch();
            if (bestMatch != null) {
                System.out.println(s + " " + bestMatch.getPeptide());
            }
        }
        System.out.println("Done");
    }

    /**
     * make ALL pairs ignore suitability - this only works ona small sample set
     * @param application
     * @param pPps
     * @param pScoringScans
     * @return
     */
    private List<Tuple2<ITheoreticalSpectrumSet, CometScoredScan>> buildTuples(XTandemMain application,final List<IPolypeptide> pPps, final List<CometScoredScan> pScoringScans) {
        List<Tuple2<ITheoreticalSpectrumSet, CometScoredScan>> ret = new ArrayList<Tuple2<ITheoreticalSpectrumSet, CometScoredScan>>();
        Scorer scorer = application.getScoreRunner();
        for (IPolypeptide pp : pPps) {
            ITheoreticalSpectrumSet ts = scorer.generateSpectrum(pp);
            for (CometScoredScan scoringScan : pScoringScans) {
                ret.add(new Tuple2(ts,scoringScan));
            }
        }


        return ret;
    }


    public Map<String, CometScoringResult> scoreTuples(XTandemMain application, List<Tuple2<ITheoreticalSpectrumSet, CometScoredScan>> scpredPair) {
        Scorer scorer = application.getScoreRunner();
        Map<String, CometScoringResult> ret = new HashMap<String, CometScoringResult>();
        for (Tuple2<ITheoreticalSpectrumSet, CometScoredScan> tp : scpredPair) {
            CometScoredScan scan = tp._2();
            ITheoreticalSpectrumSet ts = tp._1();
            String id = scan.getId();
            CometScoringResult result = ret.get(id);
            if (result == null) {
                result = new CometScoringResult();
                ret.put(id, result);
            }

            double xcorr = CometScoringAlgorithm.doRealScoring(scan, scorer, ts, application);

            IPolypeptide peptide = ts.getPeptide();


            SpectralMatch sm = new SpectralMatch(peptide, scan, xcorr, xcorr, xcorr, scan, null);

            result.addSpectralMatch(sm);
        }
        return ret;
    }


    public Map<String, CometScoringResult> scoreTuples(XTandemMain application, MapOfLists<BinChargeKey, Tuple2<ITheoreticalSpectrumSet, CometScoredScan>> scpredPair) {
        Scorer scorer = application.getScoreRunner();
        Map<String, CometScoringResult> ret = new HashMap<String, CometScoringResult>();
        for (BinChargeKey k : scpredPair.keySet()) {
            for (Tuple2<ITheoreticalSpectrumSet, CometScoredScan> tp : scpredPair.get(k)) {
                CometScoredScan scan = tp._2();
                ITheoreticalSpectrumSet ts = tp._1();
                String id = scan.getId();
                CometScoringResult result = ret.get(id);
                if (result == null) {
                    result = new CometScoringResult();
                    ret.put(id, result);
                }

                double xcorr = CometScoringAlgorithm.doRealScoring(scan, scorer, ts, application);

                IPolypeptide peptide = ts.getPeptide();


                SpectralMatch sm = new SpectralMatch(peptide, scan, xcorr, xcorr, xcorr, scan, null);

                result.addSpectralMatch(sm);
            }
        }
        return ret;
    }

    public List<CometScoredScan> getCometScoredScans(final CometScoringAlgorithm pComet, final List<RawPeptideScan> pAllScanFromMZXMLResource) {
        List<CometScoredScan> scoringScans = new ArrayList<CometScoredScan>();
        for (RawPeptideScan rawPeptideScan : pAllScanFromMZXMLResource) {
            CometScoredScan e = new CometScoredScan(rawPeptideScan, pComet);
            e.setAlgorithm(pComet);
            scoringScans.add(e);
        }
        return scoringScans;
    }

    private MapOfLists<BinChargeKey, CometScoredScan> mapToScoreBins(final List<CometScoredScan> pPps) {
        MapOfLists<BinChargeKey, CometScoredScan> ret = new MapOfLists<BinChargeKey, CometScoredScan>();
        for (CometScoredScan pp : pPps) {
            Set<BinChargeKey> binChargeKeys = BinChargeMapper.keysFromSpectrum(pp);
            for (BinChargeKey binChargeKey : binChargeKeys) {
                ret.putItem(binChargeKey, pp);
            }
        }
        return ret;
    }

    private MapOfLists<BinChargeKey, ITheoreticalSpectrumSet> mapToBins(XTandemMain application, final List<IPolypeptide> pPps) {
        MapOfLists<BinChargeKey, ITheoreticalSpectrumSet> ret = new MapOfLists<BinChargeKey, ITheoreticalSpectrumSet>();
        for (IPolypeptide pp : pPps) {
            double matchingMass = CometScoringAlgorithm.getCometMatchingMass(pp);
            Scorer scorer = application.getScoreRunner();

            ITheoreticalSpectrumSet ts = scorer.generateSpectrum(pp);
            for (ITheoreticalSpectrum spec : ts.getSpectra()) {
                BinChargeKey key = BinChargeMapper.oneKeyFromChargeMz(spec.getCharge(), matchingMass);
                ret.putItem(key, ts);
            }

        }
        return ret;
    }


    @Test
    public void testPlain() throws Exception {
        String BaseDir = "/plain/";
        doTest(BaseDir, 1);
    }


    @Test
    public void testNoEnzyme() throws Exception {
        String BaseDir = "/noenzyme/";
        doTest(BaseDir, 66);
    }

    @Test
    public void testMultipleMods() throws Exception {
        String BaseDir = "/multiplemods/";
        doTest(BaseDir, 1);
    }

    @Test
    public void testAutodecoy1() throws Exception {
        String BaseDir = "/autodecoy1/";
        doTest(BaseDir, 3);
    }

    @Test
    public void testSemitryptic() throws Exception {
        String BaseDir = "/semi-tryptic/";
        doTest(BaseDir, 68);
    }

    @Test
    public void testTryptic() throws Exception {
        String BaseDir = "/tryptic/";
        doTest(BaseDir, 68);
    }

}
