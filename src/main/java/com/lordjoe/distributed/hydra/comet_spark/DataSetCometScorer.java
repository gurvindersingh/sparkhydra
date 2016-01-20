package com.lordjoe.distributed.hydra.comet_spark;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.*;
import com.lordjoe.distributed.hydra.comet.*;
import com.lordjoe.distributed.spark.accumulators.*;
import com.lordjoe.utilities.*;
import org.apache.spark.api.java.*;
import org.apache.spark.sql.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.pepxml.*;

import java.util.*;

import static com.lordjoe.distributed.hydra.comet_spark.SparkCometScanScorer.*;

/**
 * com.lordjoe.distributed.hydra.comet_spark.DataSetCometScorer
 * User: Steve
 * Date: 1/16/2016
 */
public class DataSetCometScorer {



    public static class MapToCometRawSpectrum extends AbstractLoggingMapFunction<RawPeptideScan, CometScoredScan> {
         final CometScoringAlgorithm comet;

         public MapToCometRawSpectrum(final CometScoringAlgorithm pComet) {
             comet = pComet;
         }

         @Override
         public CometScoredScan doCall(final RawPeptideScan pIMeasuredSpectrum) throws Exception {
             CometScoredScan ret = new CometScoredScan(pIMeasuredSpectrum, comet);
             return ret;
         }
     }

    /**
     * score with a join of a List of peptides
     *
     * @param args
     */
    public static void scoringUsingCogroup(String[] args) {
//        Map<Integer, RawPeptideScan> mapped = CometTestingUtilities.getScanMapFromResource("/eg3_20/eg3_20.mzXML");
//        RawPeptideScan scan2 = mapped.get(2);
//
//


        long totalSpectra = 0;
        List<PairCounter> pairs = null;

        // Force PepXMLWriter to load
        PepXMLWriter foo = null;
        ElapsedTimer timer = new ElapsedTimer();
        ElapsedTimer totalTime = new ElapsedTimer();

        if (args.length < TANDEM_CONFIG_INDEX + 1) {
            System.out.println("usage sparkconfig configFile");
            return;
        }

        buildDesiredScoring(args);

        SparkUtilities.readSparkProperties(args[SPARK_CONFIG_INDEX]);

        CometScoringHandler handler = buildCometScoringHandler(args[TANDEM_CONFIG_INDEX]);

        XTandemMain scoringApplication = handler.getApplication();
        setDebuggingCountMade(scoringApplication.getBooleanParameter(SparkUtilities.DO_DEBUGGING_CONFIG_PROPERTY, false));
        CometScoringAlgorithm comet = (CometScoringAlgorithm) scoringApplication.getAlgorithms()[0];

        Properties sparkProperties = SparkUtilities.getSparkProperties();
        String spectrumPath = scoringApplication.getSpectrumPath();
        String spectra = SparkUtilities.buildPath(spectrumPath);


        //MZPartitioner partitioner = new MZPartitioner();
        JavaRDD<IMeasuredSpectrum> spectraToScoreX = SparkScanScorer.getMeasuredSpectra(timer, sparkProperties, spectra, scoringApplication);

        SQLContext sqlCtx = SparkUtilities.getCurrentSQLContext();

        JavaRDD<RawPeptideScan> spectraToScoreY  = SparkUtilities.asConcreteRDD(spectraToScoreX,RawPeptideScan.class);

        Encoder<RawPeptideScan> evidence = Encoders.kryo(RawPeptideScan.class);
        Dataset<RawPeptideScan> spectraToScore = sqlCtx.createDataset( spectraToScoreY.rdd(), evidence);


        JavaRDD<IPolypeptide> allPeptidesX = readAllPeptides(sparkProperties, handler);
        JavaRDD<Polypeptide> allPeptidesY = SparkUtilities.asConcreteRDD(allPeptidesX,Polypeptide.class);

        Encoder<Polypeptide> polyEvidence = Encoders.kryo(Polypeptide.class);
        Dataset<Polypeptide> allPeptides = sqlCtx.createDataset( allPeptidesY.rdd(), polyEvidence);


           // convert spectra into an object with scoring information
        MapToCometRawSpectrum mapToCometRawSpectrum = new MapToCometRawSpectrum(comet);

        Dataset<CometScoredScan> cometSpectraToScore = spectraToScore.map(mapToCometRawSpectrum,Encoders.kryo(CometScoredScan.class));



//        // if you want to limt do so here
//        // cometSpectraToScore = countAndLimitSpectra(cometSpectraToScore);
//
//        // Assign bins to spectra
//        JavaPairRDD<BinChargeKey, CometScoredScan> keyedSpectra = handler.mapMeasuredSpectrumToKeys(cometSpectraToScore);
//
//        keyedSpectra = SparkUtilities.persist(keyedSpectra);
//
//        // fine all bins we are scoring - this allows us to filter peptides
//        //keyedSpectra = SparkUtilities.persist(keyedSpectra);
//        //List<Tuple2<BinChargeKey, CometScoredScan>> collect = keyedSpectra.collect();
//        //  Set<Integer> usedBins = getUsedBins(keyedSpectra);
//        Map<BinChargeKey, Long> usedBinsMap = getUsedBins(keyedSpectra);
//
//        // temporary code for compatability
//        //  Set<Integer> usedBins = temporaryExpedientToExtractIntegers(usedBinsMap);
//
//        MapOfLists<Integer, BinChargeKey> splitKeys = computeBinSplit(usedBinsMap);
//
//
//
//        int maxSpectraInBin = scoringApplication.getIntParameter(BinPartitioner.MAX_SPECTRA_PARAMETER,BinPartitioner.DEFAULT_MAX_SPECTRA_IN_BIN);
//        int maxKeysInBin = scoringApplication.getIntParameter(BinPartitioner.MAX_KEYS_PARAMETER,BinPartitioner.DEFAULT_MAX_KEYS_IN_BIN) ;
//
//         /**
//         * if spectra are split remap them
//         */
//        keyedSpectra = remapSpectra(keyedSpectra, splitKeys);
//
//        keyedSpectra = SparkUtilities.persist(keyedSpectra);
//
//        usedBinsMap = getUsedBins(keyedSpectra); // use new keys
//
//        // make a smart partitioner
//        BinPartitioner partitioner = new BinPartitioner(totalSpectra, splitKeys, usedBinsMap, maxSpectraInBin, maxKeysInBin);
//
//        showBinningData(totalSpectra, splitKeys, usedBinsMap, maxSpectraInBin, maxKeysInBin);
//
//
//        // redivide
//        keyedSpectra.partitionBy(partitioner);
//
//        // read proteins - digest add modifications
//        // JavaPairRDD<BinChargeKey, HashMap<String, IPolypeptide>> keyedPeptides = getBinChargePeptideHash(sparkProperties, usedBins, handler);
//        // JavaPairRDD<BinChargeKey, IPolypeptide> keyedPeptides = getBinChargePeptide(sparkProperties, usedBins, handler);
//        //JavaPairRDD<BinChargeKey, CometTheoreticalBinnedSet> keyedTheoreticalPeptides = getBinChargeTheoreticalPeptide(sparkProperties, usedBins, handler);
//        //  JavaPairRDD<BinChargeKey, IPolypeptide> keyedPeptides = handler.mapFragmentsToBin(allPeptides, usedBins);
//        JavaPairRDD<BinChargeKey, IPolypeptide> keyedPeptides = getSplitBinChargePeptideHash(sparkProperties, splitKeys, handler);
//
//        keyedPeptides = keyedPeptides.partitionBy(partitioner);
//
//
//        //keyedPeptides.partitionBy(partitioner);
//        timer.showElapsed("Mapped Peptides", System.err);
//
//        // debugging only
////        keyedPeptides = SparkUtilities.persist(keyedPeptides);
////        Map<BinChargeKey, HashMap<String, IPolypeptide>> binChargeKeyHashMapMap = keyedPeptides.collectAsMap();
////        List<HashMap<String, IPolypeptide>> collect1 = keyedPeptides.values().collect();
////        for (HashMap<String, IPolypeptide> hms : collect1) {
////            for (IPolypeptide pp : hms.values()) {
////                if (TestUtilities.isInterestingPeptide(pp))
////                    break;
////            }
////        }
//
//
//        long[] counts = new long[1];
////        if (isDebuggingCountMade()) {
////            keyedPeptides = SparkUtilities.persistAndCountPair("Peptides as Theoretical Spectra", keyedPeptides, counts);
////        }
////
////        if (isDebuggingCountMade()) {
////            keyedPeptides = SparkUtilities.persistAndCountPair("Mapped Peptides", keyedPeptides, counts);
////        }
//        long peptidecounts = counts[0];
//
//
//        if (isDebuggingCountMade()) {
//            keyedSpectra = SparkUtilities.persistAndCountPair("Mapped Spectra", keyedSpectra, counts);
//        }
//        long keyedSpectrumCounts = counts[0];
//        // find spectra-peptide pairs to score
//        // JavaPairRDD<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<HashMap<String, IPolypeptide>>>> binP = keyedSpectra.cogroup(keyedPeptides);
//        JavaPairRDD<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<IPolypeptide>>> binP = keyedSpectra.cogroup(keyedPeptides);
//        //JavaPairRDD<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<CometTheoreticalBinnedSet>>> binP = keyedSpectra.cogroup(keyedTheoreticalPeptides);
//
////        binP = SparkUtilities.persistAndCountPair("Ready to Score",binP,counts);
////        long scoringCounts = counts[0];
//
//        // added SLewis to reduce memory stress
//
//        binP = binP.partitionBy(partitioner);
//
//
//        // NOTE this is where all the real work is done
//        //JavaRDD<? extends IScoredScan> bestScores = handler.scoreCometBinPair(binP);
//        JavaRDD<? extends IScoredScan> bestScores = handler.scoreCometBinPairPolypeptide(binP);
//
//        // once we score we can go back to normal partitions
//        // bestScores = bestScores.repartition(SparkUtilities.getDefaultNumberPartitions());
//
//
////        bestScores = SparkUtilities.persistAndCount("After Scoring",bestScores,counts);
////        long scoredCounts = counts[0];
//
//        // combine scores from same scan
//        JavaRDD<? extends IScoredScan> cometBestScores = handler.combineScanScores(bestScores);
//
////        if(false) {
////            cometBestScores = SparkUtilities.persist(cometBestScores);
////
////            List<? extends IScoredScan> collect = cometBestScores.collect();
////            for (IScoredScan iScoredScan : collect) {
////                CometScoringResult cs = (CometScoringResult) iScoredScan;
////                System.out.println(" ======================");
////                System.out.println(cs.getId());
////                ISpectralMatch[] spectralMatches = cs.getSpectralMatches();
////                for (int i = 0; i < spectralMatches.length; i++) {
////                    ISpectralMatch sm = spectralMatches[i];
////                    System.out.println(sm.getPeptide() + " " + sm.getHyperScore());
////                }
////            }
////        }
//
//        // todo combine score results from different bins
//
//        if (isDebuggingCountMade())
//            bestScores = SparkUtilities.persistAndCount("Best Scores", bestScores);
//
//        timer.showElapsed("built best scores", System.err);
//        XTandemMain application = scoringApplication;
//
//        // code using PepXMLWriter new uses tandem writer
//        PepXMLWriter pwrtr = new PepXMLWriter(application);
//        PepXMLScoredScanWriter pWrapper = new PepXMLScoredScanWriter(pwrtr);
//        SparkConsolidator consolidator = new SparkConsolidator(pWrapper, application);
//
//        //      BiomlReporter writer = new BiomlReporter(application);
//        //   SparkConsolidator consolidator = new SparkConsolidator(writer, application);
//
//
//        int numberScores = consolidator.writeScores(cometBestScores);
//        System.out.println("Total Scans Scored " + numberScores);
//
//        totalTime.showElapsed("Finished Scoring");
//
//        SparkAccumulators.showAccumulators(totalTime);
//
//        TestUtilities.closeCaseLoggers();
//        // purely debugging  code to see whether interesting peptides scored with interesting spectra
//        //TestUtilities.writeSavedKeysAndSpectra();
    }

    public static void main(String[] args) {
        scoringUsingCogroup(args);
    }

}
