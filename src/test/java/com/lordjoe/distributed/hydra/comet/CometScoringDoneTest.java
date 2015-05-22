package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.SparkUtilities;
import com.lordjoe.distributed.hydra.fragment.BinChargeKey;
import com.lordjoe.distributed.hydra.fragment.BinChargeMapper;
import com.lordjoe.distributed.hydra.test.TestUtilities;
import com.lordjoe.utilities.FileUtilities;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.junit.Assert;
import org.systemsbiology.xtandem.RawPeptideScan;
import org.systemsbiology.xtandem.XTandemMain;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.scoring.IScoredScan;
import org.systemsbiology.xtandem.scoring.ISpectralMatch;
import org.systemsbiology.xtandem.scoring.Scorer;
import org.systemsbiology.xtandem.testing.MZXMLReader;
import scala.Tuple2;

import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringDoneTest
 * test whether a set of peptides will score against a specific spectrum
 * using the list from comet as a gold standard
 *
 * @author Steve Lewis
 * @date 5/12/2015
 */
public class CometScoringDoneTest {

    /**
     * find the spectgrum matching the polypeptide
     * @param pp
     * @param specrta
     * @return
     */
    public static UsedSpectrum getUsedSpectrum(IPolypeptide pp, List<UsedSpectrum> specrta) {
        for (UsedSpectrum usedSpectrum : specrta) {
            if (UsedSpectrum.equivalentPeptide(usedSpectrum.peptide, pp))
                return usedSpectrum;
        }
        return null;
    }

    public static Set<Integer> getSpectrumBinsIntegers(Set<BinChargeKey> used) {
        Set<Integer> ret = new HashSet<Integer>();
        for (BinChargeKey binChargeKey : used) {
            ret.add(binChargeKey.getMzInt());
        }
        return ret;
    }


    public static boolean willHydraScorePeptide(final Scorer scorer, CometTheoreticalBinnedSet ts, CometScoredScan spec, Set<BinChargeKey> usedBins) {
        BinChargeKey key = BinChargeMapper.keyFromPeptide(ts.getPeptide());
        if (!usedBins.contains(key))
            return false;

        if (!scorer.isTheoreticalSpectrumScored(spec, ts))
            return false;

        return true;
    }

    public static List<IPolypeptide>   getFromBinnedHash(List<Tuple2<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<HashMap<String, IPolypeptide>>>>> collect1)
    {
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (Tuple2<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<HashMap<String, IPolypeptide>>>> tx : collect1) {
            Tuple2<Iterable<CometScoredScan>, Iterable<HashMap<String, IPolypeptide>>> txx = tx._2();
            Iterable<HashMap<String, IPolypeptide>> hashMaps = txx._2();
            for (HashMap<String, IPolypeptide> txxx : hashMaps) {
                for (IPolypeptide iPolypeptide : txxx.values()) {
                    holder.add(iPolypeptide);
                }
            }
        }
        return holder;
    }

    public static void advancedScoringDoneTest(String[] args) {
        XTandemMain.setShowParameters(false);  // I do not want to see parameters


        InputStream is = new StringBufferInputStream(CometTestData.COMET_XML);
        XTandemMain application = new XTandemMain(is, "TANDEM_XML");
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        comet.configure(application);
        Scorer scorer = application.getScoreRunner();
        SparkUtilities.readSparkProperties(args[SparkCometScanScorer.SPARK_CONFIG_INDEX]);

        CometScoringHandler handler = SparkCometScanScorer.buildCometScoringHandler(args[SparkCometScanScorer.TANDEM_CONFIG_INDEX]);

        JavaSparkContext ctx = SparkUtilities.getCurrentContext();

        final Class<CometScoringDoneTest> cls = CometScoringDoneTest.class;
        InputStream istr = cls.getResourceAsStream("/000000008852.mzXML");

        final String scanTag = FileUtilities.readInFile(istr);
        RawPeptideScan rp = MZXMLReader.handleScan(scanTag);

        CometScoredScan spec = new CometScoredScan(rp, comet);

        List<CometScoredScan> scans = new ArrayList<CometScoredScan>();
                scans.add(spec);

        JavaRDD<CometScoredScan> cometSpectraToScore = ctx.parallelize(scans);
        // these are spectra
        JavaPairRDD<BinChargeKey, CometScoredScan> keyedSpectra = handler.mapMeasuredSpectrumToKeys(cometSpectraToScore);

        Set<BinChargeKey> usedBins = BinChargeMapper.getSpectrumBins(spec);
        Set<Integer> spectrumBinsIntegers = getSpectrumBinsIntegers(usedBins);

        Properties sparkProperties = SparkUtilities.getSparkProperties();
        JavaPairRDD<BinChargeKey, HashMap<String, IPolypeptide>> keyedPeptides = SparkCometScanScorer.getBinChargePeptideHash(sparkProperties, spectrumBinsIntegers, handler);

        JavaPairRDD<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<HashMap<String, IPolypeptide>>>> binP = keyedSpectra.cogroup(keyedPeptides);

        List<Tuple2<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<HashMap<String, IPolypeptide>>>>> collect1 = binP.collect();

        List<IPolypeptide> fronBinnedHash =  getFromBinnedHash( collect1);
        List<IPolypeptide> scored = CometTesting.getScoredPeptides(spectrumBinsIntegers, handler);

        boolean found = false;
        for (IPolypeptide pp : scored) {
            if(TestUtilities.isInterestingPeptide(pp))    {
                 found = true;
                break;
              }
          }
        Assert.assertTrue(found);

        Assert.assertEquals(fronBinnedHash.size(),scored.size());

        fronBinnedHash.removeAll(scored);

        Assert.assertTrue(fronBinnedHash.isEmpty());

        Assert.assertEquals(usedBins.size(),collect1.size()); // better be just one

        // read what comet scored
        List<UsedSpectrum> used = CometTestingUtilities.getSpectrumUsed(8852);


        CometScoredScan scan = new CometScoredScan(spec, comet);


        int numberCorrect = 0;
        int numberInCorrect = 0;
        int numberTested = used.size();

        List<IPolypeptide> notComet = new ArrayList<IPolypeptide>();
        List<IPolypeptide> notScored = new ArrayList<IPolypeptide>();
        List<UsedSpectrum> peptideNotFound = new ArrayList<UsedSpectrum>();


        // first cut - how many peptides tit comet not score
        for (IPolypeptide pp : scored) {
            if(getUsedSpectrum(pp,used) == null) {  // comet did not score
                CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pp);
                if (willHydraScorePeptide(scorer, ts, scan, usedBins))
                    notComet.add(pp); // comet did not score this
            }
        }

        // how many scored paptides not found
        for (UsedSpectrum spc : used) {
            boolean peptideFound = false;
            for (IPolypeptide pp : scored) {
                if(getUsedSpectrum(pp,used) != null)       {
                    peptideFound = true;
                    break;
                }
            }
            if(!peptideFound)
                peptideNotFound.add(spc);

        }
        // assert we everything we score is scored by comet
        Assert.assertTrue(notComet.isEmpty());
        // assert we score everything comet scores
        Assert.assertTrue(peptideNotFound.isEmpty());
    }

    public static void withFullScoring(String[] args) {
        XTandemMain.setShowParameters(false);  // I do not want to see parameters


        InputStream is = new StringBufferInputStream(CometTestData.COMET_XML);
        XTandemMain application = new XTandemMain(is, "TANDEM_XML");
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        comet.configure(application);
        Scorer scorer = application.getScoreRunner();
        SparkUtilities.readSparkProperties(args[SparkCometScanScorer.SPARK_CONFIG_INDEX]);

        CometScoringHandler handler = SparkCometScanScorer.buildCometScoringHandler(args[SparkCometScanScorer.TANDEM_CONFIG_INDEX]);

        JavaSparkContext ctx = SparkUtilities.getCurrentContext();

        final Class<CometScoringDoneTest> cls = CometScoringDoneTest.class;
        InputStream istr = cls.getResourceAsStream("/000000008852.mzXML");

        final String scanTag = FileUtilities.readInFile(istr);
        RawPeptideScan rp = MZXMLReader.handleScan(scanTag);

        CometScoredScan spec = new CometScoredScan(rp, comet);

        List<CometScoredScan> scans = new ArrayList<CometScoredScan>();
        scans.add(spec);

        JavaRDD<CometScoredScan> cometSpectraToScore = ctx.parallelize(scans);
        // these are spectra
        JavaPairRDD<BinChargeKey, CometScoredScan> keyedSpectra = handler.mapMeasuredSpectrumToKeys(cometSpectraToScore);

        Set<BinChargeKey> usedBins = BinChargeMapper.getSpectrumBins(spec);
        Set<Integer> spectrumBinsIntegers = getSpectrumBinsIntegers(usedBins);

        Properties sparkProperties = SparkUtilities.getSparkProperties();
        JavaPairRDD<BinChargeKey, HashMap<String, IPolypeptide>> keyedPeptides = SparkCometScanScorer.getBinChargePeptideHash(sparkProperties, spectrumBinsIntegers, handler);




        JavaPairRDD<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<HashMap<String, IPolypeptide>>>> binP = keyedSpectra.cogroup(keyedPeptides);

        JavaRDD<? extends IScoredScan> bestScores = handler.scoreCometBinPair(binP);

        List<? extends IScoredScan> scoredScans = bestScores.collect();

        boolean found = false;
        for (IScoredScan scoredScan : scoredScans) {
            ISpectralMatch bestMatch = scoredScan.getBestMatch();
            if(TestUtilities.isInterestingPeptide(bestMatch.getPeptide()))     {
                found = true;
                break;
            }
          }
        Assert.assertTrue(found);

        List<Tuple2<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<HashMap<String, IPolypeptide>>>>> collect1 = binP.collect();

        List<IPolypeptide> fronBinnedHash =  getFromBinnedHash( collect1);
        List<IPolypeptide> scored = CometTesting.getScoredPeptides(spectrumBinsIntegers, handler);

        Assert.assertEquals(fronBinnedHash.size(),scored.size());

        fronBinnedHash.removeAll(scored);

        Assert.assertTrue(fronBinnedHash.isEmpty());

        Assert.assertEquals(usedBins.size(),collect1.size()); // better be just one

        // read what comet scored
        List<UsedSpectrum> used = CometTestingUtilities.getSpectrumUsed(8852);


        CometScoredScan scan = new CometScoredScan(spec, comet);


        int numberCorrect = 0;
        int numberInCorrect = 0;
        int numberTested = used.size();

        List<IPolypeptide> notComet = new ArrayList<IPolypeptide>();
        List<IPolypeptide> notScored = new ArrayList<IPolypeptide>();
        List<UsedSpectrum> peptideNotFound = new ArrayList<UsedSpectrum>();


        // first cut - how many peptides tit comet not score
        for (IPolypeptide pp : scored) {
            if(getUsedSpectrum(pp,used) == null) {  // comet did not score
                CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pp);
                if (willHydraScorePeptide(scorer, ts, scan, usedBins))
                    notComet.add(pp); // comet did not score this
            }
        }

        // how many scored paptides not found
        for (UsedSpectrum spc : used) {
            boolean peptideFound = false;
            for (IPolypeptide pp : scored) {
                if(getUsedSpectrum(pp,used) != null)       {
                    peptideFound = true;
                    break;
                }
            }
            if(!peptideFound)
                peptideNotFound.add(spc);

        }
        // assert we everything we score is scored by comet
        Assert.assertTrue(notComet.isEmpty());
        // assert we score everything comet scores
        Assert.assertTrue(peptideNotFound.isEmpty());
    }


    public static void basicScoringDoneTest(String[] args) {
        XTandemMain.setShowParameters(false);  // I do not want to see parameters


        InputStream is = new StringBufferInputStream(CometTestData.COMET_XML);
        XTandemMain application = new XTandemMain(is, "TANDEM_XML");
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        comet.configure(application);
        Scorer scorer = application.getScoreRunner();
        SparkUtilities.readSparkProperties(args[SparkCometScanScorer.SPARK_CONFIG_INDEX]);

        CometScoringHandler handler = SparkCometScanScorer.buildCometScoringHandler(args[SparkCometScanScorer.TANDEM_CONFIG_INDEX]);


        final Class<CometScoringDoneTest> cls = CometScoringDoneTest.class;
        InputStream istr = cls.getResourceAsStream("/000000008852.mzXML");

        final String scanTag = FileUtilities.readInFile(istr);
        RawPeptideScan rp = MZXMLReader.handleScan(scanTag);
        CometScoredScan spec = new CometScoredScan(rp, comet);


        // read what comet scored
        List<UsedSpectrum> used = CometTestingUtilities.getSpectrumUsed(8852);


        CometScoredScan scan = new CometScoredScan(spec, comet);

        Set<BinChargeKey> usedBins = BinChargeMapper.getSpectrumBins(spec);

        int numberCorrect = 0;
        int numberInCorrect = 0;
        int numberTested = used.size();

        List<IPolypeptide> notComet = new ArrayList<IPolypeptide>();
        List<IPolypeptide> notScored = new ArrayList<IPolypeptide>();
        List<UsedSpectrum> peptideNotFound = new ArrayList<UsedSpectrum>();

        List<IPolypeptide> scored = CometTesting.getScoredPeptides(getSpectrumBinsIntegers(usedBins), handler);

        // first cut - how many peptides tit comet not score
        for (IPolypeptide pp : scored) {
            if(getUsedSpectrum(pp,used) == null) {  // comet did not score
                CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pp);
               if (willHydraScorePeptide(scorer, ts, scan, usedBins))
                    notComet.add(pp); // comet did not score this
            }
        }

        // how many scored paptides not found
        for (UsedSpectrum spc : used) {
            boolean peptideFound = false;
            for (IPolypeptide pp : scored) {
                if(getUsedSpectrum(pp,used) != null)       {
                    peptideFound = true;
                    break;
                }
              }
            if(!peptideFound)
                peptideNotFound.add(spc);

        }
        // assert we everything we score is scored by comet
        Assert.assertTrue(notComet.isEmpty());
        // assert we score everything comet scores
        Assert.assertTrue(peptideNotFound.isEmpty());
    }


    /**
     * call with the following arguments
     *  SparkLocalClusterEg3Test.properties  input_searchGUISample.xml
     *  user-dir = C:\sparkhydra\data
     * @param args
     */
    public static void main(String[] args) {
       //  withFullScoring(args);
        basicScoringDoneTest(args);
    //    advancedScoringDoneTest(args);

    }


}

