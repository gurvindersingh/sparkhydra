package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.*;
import com.lordjoe.distributed.hydra.fragment.*;
import com.lordjoe.distributed.hydra.scoring.*;
import com.lordjoe.distributed.hydra.test.*;
import com.lordjoe.distributed.output.*;
import com.lordjoe.distributed.spark.*;
import com.lordjoe.distributed.tandem.LibraryBuilder;
import com.lordjoe.distributed.test.*;
import com.lordjoe.utilities.*;
import org.apache.spark.api.java.*;
import org.apache.spark.storage.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.pepxml.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;

import java.io.*;
import java.lang.Long;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.SparkCometScanScorer
 * a Copy of  SparkScanScorer specialized for Comet analysis
 * User: Steve
 * Date: 10/7/2014
 */
public class SparkCometScanScorer {

    public static final boolean DO_DEBUGGING_COUNT = true;

    private static boolean debuggingCountMade = DO_DEBUGGING_COUNT;

    public static boolean isDebuggingCountMade() {
        return debuggingCountMade;
    }

    public static void setDebuggingCountMade(final boolean pIsDebuggingCountMade) {
        debuggingCountMade = pIsDebuggingCountMade;
    }


    public static final int SPARK_CONFIG_INDEX = 0;
    public static final int TANDEM_CONFIG_INDEX = 1;
    public static final int SPECTRA_INDEX = 2;
    public static final int SPECTRA_TO_SCORE = Integer.MAX_VALUE;
    public static final String MAX_PROTEINS_PROPERTY = "com.lordjoe.distributed.hydra.MaxProteins";
    @SuppressWarnings("UnusedDeclaration")
    public static final String MAX_SPECTRA_PROPERTY = "com.lordjoe.distributed.hydra.MaxSpectra";
    @SuppressWarnings("UnusedDeclaration")
    public static final String SKIP_SCORING_PROPERTY = "com.lordjoe.distributed.hydra.SkipScoring";
    public static final String SCORING_PARTITIONS_SCANS_NAME = "com.lordjoe.distributed.max_scoring_partition_scans";
    public static final long MAX_SPECTRA_TO_SCORE_IN_ONE_PASS = Long.MAX_VALUE;


    public static JavaPairRDD<BinChargeKey, ITheoreticalSpectrumSet> getBinChargePeptides(final Properties pSparkProperties, final SparkMapReduceScoringHandler pHandler) {
        JavaRDD<IPolypeptide> databasePeptides = getiPolypeptideJavaRDD(pSparkProperties, pHandler);

        // Map peptides into bins
        JavaPairRDD<BinChargeKey, ITheoreticalSpectrumSet> keyedPeptides = pHandler.mapFragmentsToTheoreticalSets(databasePeptides);

        return keyedPeptides;
    }


    /**
     * return all peptides associated with a key as an ArrayList (so Serializable is implemented)
     *
     * @param pSparkProperties
     * @param pHandler
     * @return
     */
    public static JavaPairRDD<BinChargeKey, HashMap<String, IPolypeptide>> getBinChargePeptideHash(final Properties pSparkProperties, final Set<Integer> usedBins, final SparkMapReduceScoringHandler pHandler) {
        JavaRDD<IPolypeptide> databasePeptides = getiPolypeptideJavaRDD(pSparkProperties, pHandler);
        // Map peptides into bins
        JavaPairRDD<BinChargeKey, HashMap<String, IPolypeptide>> keyedPeptidesList = pHandler.mapFragmentsToBinHash(databasePeptides, usedBins);
        return keyedPeptidesList;
    }

    /**
     * return all peptides associated with a key as an ArrayList (so Serializable is implemented)
     *
     * @param pSparkProperties
     * @param pHandler
     * @return
     */
    public static JavaPairRDD<BinChargeKey, ArrayList<IPolypeptide>> getBinChargePeptideList(final Properties pSparkProperties, final Set<Integer> usedBins, final SparkMapReduceScoringHandler pHandler) {
        JavaRDD<IPolypeptide> databasePeptides = getiPolypeptideJavaRDD(pSparkProperties, pHandler);
        // Map peptides into bins
        JavaPairRDD<BinChargeKey, ArrayList<IPolypeptide>> keyedPeptidesList = pHandler.mapFragmentsToBinList(databasePeptides, usedBins);
        return keyedPeptidesList;
    }


    public static JavaRDD<IPolypeptide> getiPolypeptideJavaRDD(Properties pSparkProperties, SparkMapReduceScoringHandler pHandler) {
        int max_proteins = 0;
        if (pSparkProperties.containsKey(MAX_PROTEINS_PROPERTY)) {
            max_proteins = Integer.parseInt(pSparkProperties.getProperty(MAX_PROTEINS_PROPERTY));
            System.err.println("Max Proteins " + max_proteins);
        }
        System.err.println("Max Proteins " + max_proteins);


        // handler.buildLibraryIfNeeded();
        // find all polypeptides and modified polypeptides
        JavaRDD<IPolypeptide> databasePeptides = pHandler.buildLibrary(max_proteins);

        // DEBUGGING why do we see more than one instance of interesting peptide
        //List<IPolypeptide> interesting1 = new ArrayList<IPolypeptide>();
        //databasePeptides = TestUtilities.findInterestingPeptides(databasePeptides, interesting1);

        if (isDebuggingCountMade())
            databasePeptides = SparkUtilities.persistAndCount("Database peptides", databasePeptides);

        // DEBUGGING why do we see more than one instance of interesting peptide
        //List<IPolypeptide> interesting2 = new ArrayList<IPolypeptide>();
        // databasePeptides = TestUtilities.findInterestingPeptides(databasePeptides, interesting2);

        return databasePeptides;
    }


    public static JavaRDD<IMeasuredSpectrum> indexSpectra(JavaRDD<IMeasuredSpectrum> pSpectraToScore) {

        JavaPairRDD<IMeasuredSpectrum, Long> indexed = pSpectraToScore.zipWithIndex();

        pSpectraToScore = indexed.map(new AddIndexToSpectrum());
        return pSpectraToScore;
    }


    public static class PairCounter implements Comparable<PairCounter> {
        public final BinChargeKey key;
        public final long v1;
        public final long v2;
        public final long product;

        public PairCounter(BinChargeKey pkey, final long pV1, final long pV2) {
            v1 = pV1;
            v2 = pV2;
            key = pkey;
            product = v1 * v2;
        }

        @Override
        public int compareTo(final PairCounter o) {
            return Long.compare(o.product, product);
        }

        public String toString() {
            return key.toString() + "spectra " + Long_Formatter.format(v1) + " peptides " + Long_Formatter.format(v2) +
                    " product " + Long_Formatter.format(product);

        }
    }

    public static <T extends IMeasuredSpectrum> List<PairCounter> showBinPairSizes(final JavaPairRDD<BinChargeKey, ITheoreticalSpectrumSet> keyedPeptides,
                                                                                   final JavaPairRDD<BinChargeKey, T> keyedSpectra) {
        // Map spectra into bins
        Map<BinChargeKey, Object> spectraCountsMap = keyedSpectra.countByKey();
        Map<BinChargeKey, Object> peptideCounts = keyedPeptides.countByKey();
        List<BinChargeKey> keys = new ArrayList(peptideCounts.keySet());
        List<PairCounter> pairs = new ArrayList<PairCounter>();

        long specCount = 0;
        long peptideCount = 0;
        long pairCount = 0;

        Collections.sort(keys);
        for (BinChargeKey key : keys) {
            Object spectralCount = spectraCountsMap.get(key);
            Object peptideCountObj = peptideCounts.get(key);
            if (spectralCount == null || peptideCountObj == null)
                continue;
            long spCount = Long.parseLong(spectralCount.toString());
            specCount += spCount;
            long pepCount = Long.parseLong(peptideCountObj.toString());
            peptideCount += pepCount;
            PairCounter pairCounter = new PairCounter(key, spCount, pepCount);
            pairs.add(pairCounter);
            pairCount += pairCounter.product;
        }

        Collections.sort(pairs);
        List<PairCounter> pairCounters = pairs.subList(0, Math.min(200, pairs.size()));
        for (PairCounter pairCounter : pairCounters) {
            System.err.println(pairCounter.toString());
        }

        System.err.println("Total Spectra " + Long_Formatter.format(specCount) +
                        " peptides " + Long_Formatter.format(peptideCount) +
                        " bins " + keys.size() +
                        " pairs " + Long_Formatter.format(pairCount)

        );
        return pairs;
    }


    public static CometScoringHandler buildCometScoringHandler(String arg) {
        Properties sparkPropertiesX = SparkUtilities.getSparkProperties();

        String pathPrepend = sparkPropertiesX.getProperty(SparkUtilities.PATH_PREPEND_PROPERTY);
        if (pathPrepend != null)
            XTandemHadoopUtilities.setDefaultPath(pathPrepend);

        String maxScoringPartitionSize = sparkPropertiesX.getProperty(SCORING_PARTITIONS_SCANS_NAME);
        if (maxScoringPartitionSize != null)
            SparkMapReduceScoringHandler.setMaxScoringPartitionSize(Integer.parseInt(maxScoringPartitionSize));


        String configStr = SparkUtilities.buildPath(arg);

        //Configuration hadoopConfiguration = SparkUtilities.getHadoopConfiguration();
        //hadoopConfiguration.setLong(org.apache.hadoop.mapreduce.lib.input.FileInputFormat.SPLIT_MAXSIZE, 64 * 1024L * 1024L);

        //Configuration hadoopConfiguration2 = SparkUtilities.getHadoopConfiguration();  // did we change the original or a copy
        return new CometScoringHandler(configStr, false);
    }

    private static void buildDesiredScoring(final String[] pArgs) {
        if (pArgs.length > TANDEM_CONFIG_INDEX + 1) {
            String fileName = pArgs[TANDEM_CONFIG_INDEX + 1];
            File file = new File(fileName);
            CometSpectraUse desired = new CometSpectraUse(file);
            SparkUtilities.setDesiredUse(desired);
        }
        // shut up the most obnoxious logging
        SparkUtilities.setLogToWarn();

    }

    private static void showAnalysisTotals(final long totalSpectra,
                                           final long peptidecounts,
                                           final long keyedSpectrumCounts,
                                           final long scoringCounts,
                                           final List<PairCounter> pPairs) {
        System.out.println("=========================================");
        System.out.println("========    Totals              =========");
        System.out.println("=========================================");
        System.out.println("Total Spectra " + totalSpectra);
        System.out.println("Keyed Spectra " + keyedSpectrumCounts);
        System.out.println("Total Peptides " + peptidecounts);
        long pairCount = 0;
        for (PairCounter pair : pPairs) {
            pairCount += pair.product;
        }
        System.out.println("Total Pairs " + SparkUtilities.formatLargeNumber(pairCount));
        System.out.println("Scored Pairs " + scoringCounts);
        System.out.println("Spectra times Peptides " + SparkUtilities.formatLargeNumber(totalSpectra * peptidecounts));

    }

    public static class MapToCometSpectrum extends AbstractLoggingFunction<IMeasuredSpectrum, CometScoredScan> {
        final CometScoringAlgorithm comet;

        public MapToCometSpectrum(final CometScoringAlgorithm pComet) {
            comet = pComet;
        }

        @Override
        public CometScoredScan doCall(final IMeasuredSpectrum pIMeasuredSpectrum) throws Exception {
            CometScoredScan ret = new CometScoredScan(pIMeasuredSpectrum, comet);
            return ret;
        }
    }


    /**
     * score with a join of individual items
     *
     * @param args
     */
    public static void pairedScoring(String[] args) {
        long totalSpectra = 0;
        List<PairCounter> pairs = null;

        //  System.setProperty("log4j.configuration","conf/log4j.properties") ;


        // for debugging show class path
        // String property = System.getProperty("java.class.path");
        //System.out.println(property);

        // Force PepXMLWriter to load
        PepXMLWriter foo = null;
        // code to run class loader
        //String runner = SparkUtilities.buildLoggingClassLoaderPropertiesFile(ScanScorer.class  , args);
        //System.out.println(runner);
        ElapsedTimer timer = new ElapsedTimer();
        ElapsedTimer totalTime = new ElapsedTimer();

        if (args.length < TANDEM_CONFIG_INDEX + 1) {
            System.out.println("usage sparkconfig configFile");
            return;
        }

        // debug logging
        //    TestUtilities.setCaseLogger(new PrintWriter(new FileWriter("TestedPairs.data")));

        buildDesiredScoring(args);

        SparkUtilities.readSparkProperties(args[SPARK_CONFIG_INDEX]);

        CometScoringHandler handler = buildCometScoringHandler(args[TANDEM_CONFIG_INDEX]);

        XTandemMain scoringApplication = handler.getApplication();
        setDebuggingCountMade(scoringApplication.getBooleanParameter(SparkUtilities.DO_DEBUGGING_CONFIG_PROPERTY, false));
        CometScoringAlgorithm comet = (CometScoringAlgorithm) scoringApplication.getAlgorithms()[0];


        Properties sparkProperties = SparkUtilities.getSparkProperties();
        String spectrumPath = scoringApplication.getSpectrumPath();
        String spectra = SparkUtilities.buildPath(spectrumPath);
        JavaRDD<IMeasuredSpectrum> spectraToScore = SparkScanScorer.getMeasuredSpectra(timer, sparkProperties, spectra, scoringApplication);

        JavaRDD<CometScoredScan> cometSpectraToScore = spectraToScore.map(new MapToCometSpectrum(comet));

        cometSpectraToScore = countAndLimitSpectra(cometSpectraToScore);

        JavaPairRDD<BinChargeKey, ITheoreticalSpectrumSet> keyedPeptides = getBinChargePeptides(sparkProperties, handler);
        timer.showElapsed("Mapped Peptides", System.err);

        long[] counts = new long[1];
        if (isDebuggingCountMade()) {
            keyedPeptides = SparkUtilities.persistAndCountPair("Peptides as Theoretical Spectra", keyedPeptides, counts);
        }

        if (isDebuggingCountMade()) {
            keyedPeptides = SparkUtilities.persistAndCountPair("Mapped Peptides", keyedPeptides, counts);
        }
        long peptidecounts = counts[0];

        JavaPairRDD<BinChargeKey, CometScoredScan> keyedSpectra = handler.mapMeasuredSpectrumToKeys(cometSpectraToScore);


        if (isDebuggingCountMade()) {
            keyedSpectra = SparkUtilities.persistAndCountPair("Mapped Spectra", keyedSpectra, counts);
        }
        long keyedSpectrumCounts = counts[0];


        if (isDebuggingCountMade()) {
            pairs = showBinPairSizes(keyedPeptides, keyedSpectra);
        }
        long spectracounts = counts[0];

        timer.showElapsed("Counted Scoring pairs", System.err);

        // find spectra-peptide pairs to score
        JavaPairRDD<BinChargeKey, Tuple2<ITheoreticalSpectrumSet, CometScoredScan>> binPairs = keyedPeptides.join(keyedSpectra);

        timer.reset();
        if (isDebuggingCountMade())
            binPairs = SparkUtilities.persistAndCountPair("Binned Pairs", binPairs, counts);
        long joincounts = counts[0];
        timer.showElapsed("Joined Pairs", System.err);


        timer.reset();
        // now produce all peptide spectrum scores where spectrum and peptide are in the same bin
        JavaRDD<? extends IScoredScan> bestScores = handler.scoreCometBinPairs(binPairs, counts);  //  todo fix and restore
        long scoringCounts = counts[0];


        if (isDebuggingCountMade())
            bestScores = SparkUtilities.persistAndCount("Best Scores", bestScores);

        timer.showElapsed("built best scores", System.err);

        XTandemMain application = scoringApplication;

        // code using PepXMLWriter new uses tandem writer
        PepXMLWriter pwrtr = new PepXMLWriter(application);
        PepXMLScoredScanWriter pWrapper = new PepXMLScoredScanWriter(pwrtr);
        SparkConsolidator consolidator = new SparkConsolidator(pWrapper, application);

        //      BiomlReporter writer = new BiomlReporter(application);
        //   SparkConsolidator consolidator = new SparkConsolidator(writer, application);


        int numberScores = consolidator.writeScores(bestScores);
        System.out.println("Total Scans Scored " + numberScores);

        SparkAccumulators.showAccumulators(totalTime);
        if (isDebuggingCountMade())
            showAnalysisTotals(totalSpectra, peptidecounts, keyedSpectrumCounts, scoringCounts, pairs);

        totalTime.showElapsed("Finished Scoring");

        TestUtilities.closeCaseLoggers();
        // purely debugging  code to see whether interesting peptides scored with interesting spectra
        //TestUtilities.writeSavedKeysAndSpectra();
    }

    /**
     * score with a join of a List of peptides
     *
     * @param args
     */
    public static void scoringUsingLists(String[] args) {
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

        // debugging code set to  check data
        if(SparkUtilities.isLocal())    {
            String usedSpactra =  SparkUtilities.buildPath("UsedSpectra.txt");
            CometTesting.readCometScoredSpectra(usedSpactra);
        }

        JavaRDD<IMeasuredSpectrum> spectraToScore = SparkScanScorer.getMeasuredSpectra(timer, sparkProperties, spectra, scoringApplication);

        JavaRDD<CometScoredScan> cometSpectraToScore = spectraToScore.map(new MapToCometSpectrum(comet));

        // if you want to limt do so here
        cometSpectraToScore = countAndLimitSpectra(cometSpectraToScore);

        // these are spectra
        JavaPairRDD<BinChargeKey, CometScoredScan> keyedSpectra = handler.mapMeasuredSpectrumToKeys(cometSpectraToScore);


        keyedSpectra = SparkUtilities.persist(keyedSpectra);

        Set<Integer> usedBins = getUsedBins(keyedSpectra);


        JavaPairRDD<BinChargeKey, HashMap<String, IPolypeptide>> keyedPeptides = getBinChargePeptideHash(sparkProperties, usedBins, handler);
        timer.showElapsed("Mapped Peptides", System.err);

        keyedPeptides = SparkUtilities.persist(keyedPeptides);
        List<Tuple2<BinChargeKey, HashMap<String, IPolypeptide>>> collect1 = keyedPeptides.collect();

        long[] counts = new long[1];
        if (isDebuggingCountMade()) {
            keyedPeptides = SparkUtilities.persistAndCountPair("Peptides as Theoretical Spectra", keyedPeptides, counts);
        }

        if (isDebuggingCountMade()) {
            keyedPeptides = SparkUtilities.persistAndCountPair("Mapped Peptides", keyedPeptides, counts);
        }
        long peptidecounts = counts[0];


        if (isDebuggingCountMade()) {
            keyedSpectra = SparkUtilities.persistAndCountPair("Mapped Spectra", keyedSpectra, counts);
        }
        long keyedSpectrumCounts = counts[0];


//        if (isDebuggingCountMade()) {
//            pairs = showBinPairSizes(keyedPeptides, keyedSpectra);
//        }

        // find spectra-peptide pairs to score
        JavaPairRDD<BinChargeKey, Tuple2<CometScoredScan, HashMap<String, IPolypeptide>>> binPairs = keyedSpectra.join(keyedPeptides);

        if (isDebuggingCountMade())
            binPairs = SparkUtilities.persistAndCountPair("Binned Pairs", binPairs, counts);

        //binPairs = binPairs.persist(StorageLevel.MEMORY_AND_DISK_SER());   // force comuptation before score
        //binPairs.count(); // force action to happen now

        // now produce all peptide spectrum scores where spectrum and peptide are in the same bin
        JavaRDD<? extends IScoredScan> bestScores = handler.scoreCometBinPairHash(binPairs);  //  todo fix and restore

        // combine scores from same scan
        JavaRDD<? extends IScoredScan> cometBestScores = handler.combineScanScores(bestScores);

        //cometBestScores = cometBestScores.persist(StorageLevel.MEMORY_AND_DISK_SER());   // force comuptation after score
        //cometBestScores.count(); // force action to happen now


        // todo combine score results from different bins

        if (isDebuggingCountMade())
            bestScores = SparkUtilities.persistAndCount("Best Scores", bestScores);

        timer.showElapsed("built best scores", System.err);
        //bestScores =  bestScores.persist(StorageLevel.MEMORY_AND_DISK());
        // System.out.println("Total Scores " + bestScores.count() + " Scores");

        XTandemMain application = scoringApplication;

        // code using PepXMLWriter new uses tandem writer
        PepXMLWriter pwrtr = new PepXMLWriter(application);
        PepXMLScoredScanWriter pWrapper = new PepXMLScoredScanWriter(pwrtr);
        SparkConsolidator consolidator = new SparkConsolidator(pWrapper, application);

        //      BiomlReporter writer = new BiomlReporter(application);
        //   SparkConsolidator consolidator = new SparkConsolidator(writer, application);


        int numberScores = consolidator.writeScores(cometBestScores);
        System.out.println("Total Scans Scored " + numberScores);

        SparkAccumulators.showAccumulators(totalTime);

        totalTime.showElapsed("Finished Scoring");

        TestUtilities.closeCaseLoggers();
        // purely debugging  code to see whether interesting peptides scored with interesting spectra
        //TestUtilities.writeSavedKeysAndSpectra();
    }

    /**
     * score with a join of a List of peptides
     *
     * @param args
     */
    public static void scoringUsingCogroup(String[] args) {
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


        // debugging code set to  check data
        if(SparkUtilities.isLocal())    {
            String usedSpactra =  SparkUtilities.buildPath("UsedSpectra.txt");
            CometTesting.readCometScoredSpectra(usedSpactra);
        }

        MZPartitioner partitioner = new MZPartitioner();
        JavaRDD<IMeasuredSpectrum> spectraToScore = SparkScanScorer.getMeasuredSpectra(timer, sparkProperties, spectra, scoringApplication);

        JavaRDD<CometScoredScan> cometSpectraToScore = spectraToScore.map(new MapToCometSpectrum(comet));

        // if you want to limt do so here
        // cometSpectraToScore = countAndLimitSpectra(cometSpectraToScore);

        // these are spectra
        JavaPairRDD<BinChargeKey, CometScoredScan> keyedSpectra = handler.mapMeasuredSpectrumToKeys(cometSpectraToScore);
        keyedSpectra.partitionBy(partitioner);


        keyedSpectra = SparkUtilities.persist(keyedSpectra);

        Set<Integer> usedBins = getUsedBins(keyedSpectra);


        JavaPairRDD<BinChargeKey, HashMap<String, IPolypeptide>> keyedPeptides = getBinChargePeptideHash(sparkProperties, usedBins, handler);
        keyedPeptides.partitionBy(partitioner);
        timer.showElapsed("Mapped Peptides", System.err);

        long[] counts = new long[1];
        if (isDebuggingCountMade()) {
            keyedPeptides = SparkUtilities.persistAndCountPair("Peptides as Theoretical Spectra", keyedPeptides, counts);
        }

        if (isDebuggingCountMade()) {
            keyedPeptides = SparkUtilities.persistAndCountPair("Mapped Peptides", keyedPeptides, counts);
        }
        long peptidecounts = counts[0];


        if (isDebuggingCountMade()) {
            keyedSpectra = SparkUtilities.persistAndCountPair("Mapped Spectra", keyedSpectra, counts);
        }
        long keyedSpectrumCounts = counts[0];
        // find spectra-peptide pairs to score
        JavaPairRDD<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<HashMap<String, IPolypeptide>>>> binP = keyedSpectra.cogroup(keyedPeptides);

        if (isDebuggingCountMade())
            binP = SparkUtilities.persistAndCountPair("Binned Pairs", binP, counts);

        JavaRDD<? extends IScoredScan> bestScores = handler.scoreCometBinPair(binP);

        // combine scores from same scan
        JavaRDD<? extends IScoredScan> cometBestScores = handler.combineScanScores(bestScores);

        // todo combine score results from different bins

        if (isDebuggingCountMade())
            bestScores = SparkUtilities.persistAndCount("Best Scores", bestScores);

        timer.showElapsed("built best scores", System.err);
        XTandemMain application = scoringApplication;

        // code using PepXMLWriter new uses tandem writer
        PepXMLWriter pwrtr = new PepXMLWriter(application);
        PepXMLScoredScanWriter pWrapper = new PepXMLScoredScanWriter(pwrtr);
        SparkConsolidator consolidator = new SparkConsolidator(pWrapper, application);

        //      BiomlReporter writer = new BiomlReporter(application);
        //   SparkConsolidator consolidator = new SparkConsolidator(writer, application);


        int numberScores = consolidator.writeScores(cometBestScores);
        System.out.println("Total Scans Scored " + numberScores);

        SparkAccumulators.showAccumulators(totalTime);

        totalTime.showElapsed("Finished Scoring");

        TestUtilities.closeCaseLoggers();
        // purely debugging  code to see whether interesting peptides scored with interesting spectra
        //TestUtilities.writeSavedKeysAndSpectra();
    }

    /**
     * get all keys we use for scoring
     *
     * @param keyedSpectra
     * @return
     */
    private static Set<Integer> getUsedBins(JavaPairRDD<BinChargeKey, CometScoredScan> keyedSpectra) {
        final Set<Integer> ret = new HashSet<Integer>();
        JavaRDD<BinChargeKey> keys = keyedSpectra.keys();
        List<BinChargeKey> collect = keys.collect();
        for (BinChargeKey binChargeKey : collect) {
            ret.add(binChargeKey.getMzInt());
        }

        return ret;
    }


    /**
     * score with a join of a List of peptides
     *
     * @param args
     */
    public static void scoringUsingTheoreticalLists(String[] args) {
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
        JavaRDD<IMeasuredSpectrum> spectraToScore = SparkScanScorer.getMeasuredSpectra(timer, sparkProperties, spectra, scoringApplication);

        JavaRDD<CometScoredScan> cometSpectraToScore = spectraToScore.map(new MapToCometSpectrum(comet));


        cometSpectraToScore = countAndLimitSpectra(cometSpectraToScore);


        JavaPairRDD<BinChargeKey, ITheoreticalSpectrumSet> binChargePeptidesX = getBinChargePeptides(sparkProperties, handler);

        // this really just does a cast
        JavaPairRDD<BinChargeKey, CometTheoreticalBinnedSet> binChargePeptides = SparkUtilities.castRDD(binChargePeptidesX, CometTheoreticalBinnedSet.class);

        JavaPairRDD<BinChargeKey, ArrayList<CometTheoreticalBinnedSet>> keyedPeptides = SparkUtilities.mapToKeyedList(binChargePeptides);

        timer.showElapsed("Mapped Peptides", System.err);

        long[] counts = new long[1];
        if (isDebuggingCountMade()) {
            keyedPeptides = SparkUtilities.persistAndCountPair("Peptides as Theoretical Spectra", keyedPeptides, counts);
        }

        if (isDebuggingCountMade()) {
            keyedPeptides = SparkUtilities.persistAndCountPair("Mapped Peptides", keyedPeptides, counts);
        }
        long peptidecounts = counts[0];

        // these are spectra
        JavaPairRDD<BinChargeKey, CometScoredScan> keyedSpectra = handler.mapMeasuredSpectrumToKeys(cometSpectraToScore);


        if (isDebuggingCountMade()) {
            keyedSpectra = SparkUtilities.persistAndCountPair("Mapped Spectra", keyedSpectra, counts);
        }
        long keyedSpectrumCounts = counts[0];


        //        if (isDebuggingCountMade()) {
        //            pairs = showBinPairSizes(keyedPeptides, keyedSpectra);
        //        }

        // find spectra-peptide pairs to score
        JavaPairRDD<BinChargeKey, Tuple2<CometScoredScan, ArrayList<CometTheoreticalBinnedSet>>> binPairs = keyedSpectra.join(keyedPeptides);

        if (isDebuggingCountMade())
            binPairs = SparkUtilities.persistAndCountPair("Binned Pairs", binPairs, counts);

        binPairs = binPairs.persist(StorageLevel.MEMORY_AND_DISK_SER());   // force comuptation before score
        binPairs.count(); // force action to happen now

        // now produce all peptide spectrum scores where spectrum and peptide are in the same bin
        JavaRDD<? extends IScoredScan> bestScores = handler.scoreCometBinTheoreticalPairList(binPairs);  //  todo fix and restore

        // combine scores from same scan
        JavaRDD<? extends IScoredScan> cometBestScores = handler.combineScanScores(bestScores);

        cometBestScores = cometBestScores.persist(StorageLevel.MEMORY_AND_DISK_SER());   // force comuptation after score
        cometBestScores.count(); // force action to happen now


        // todo combine score results from different bins

        if (isDebuggingCountMade())
            bestScores = SparkUtilities.persistAndCount("Best Scores", bestScores);

        timer.showElapsed("built best scores", System.err);
        //bestScores =  bestScores.persist(StorageLevel.MEMORY_AND_DISK());
        // System.out.println("Total Scores " + bestScores.count() + " Scores");

        XTandemMain application = scoringApplication;

        // code using PepXMLWriter new uses tandem writer
        PepXMLWriter pwrtr = new PepXMLWriter(application);
        PepXMLScoredScanWriter pWrapper = new PepXMLScoredScanWriter(pwrtr);
        SparkConsolidator consolidator = new SparkConsolidator(pWrapper, application);

        //      BiomlReporter writer = new BiomlReporter(application);
        //   SparkConsolidator consolidator = new SparkConsolidator(writer, application);


        int numberScores = consolidator.writeScores(cometBestScores);
        System.out.println("Total Scans Scored " + numberScores);

        SparkAccumulators.showAccumulators(totalTime);

        totalTime.showElapsed("Finished Scoring");

        TestUtilities.closeCaseLoggers();
        // purely debugging  code to see whether interesting peptides scored with interesting spectra
        //TestUtilities.writeSavedKeysAndSpectra();
    }


    public static JavaRDD<CometScoredScan> countAndLimitSpectra(JavaRDD<CometScoredScan> spectraToScore) {
        if (isDebuggingCountMade()) {
            long[] spectraCounts = new long[1];
            SparkUtilities.persistAndCount("Read Spectra", spectraToScore, spectraCounts);
            long spectraCount = spectraCounts[0];
            if (spectraCount > MAX_SPECTRA_TO_SCORE_IN_ONE_PASS) {
                int percentileKept = (int) ((100L * MAX_SPECTRA_TO_SCORE_IN_ONE_PASS) / spectraCount);
                System.err.println("Keeping " + percentileKept + "% spectra");
                spectraToScore = spectraToScore.filter(new PercentileFilter(percentileKept));
            }
        }
        return spectraToScore;
    }


    /**
     * call with args like or20080320_s_silac-lh_1-1_11short.mzxml in Sample2
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        //  pairedScoring(args);
        if (LibraryBuilder.USE_PARQUET_DATABASE)
            scoringUsingLists(args);
        else
            scoringUsingCogroup(args);
        //
        //   scoringUsingTheoreticalLists(args);
    }

}
