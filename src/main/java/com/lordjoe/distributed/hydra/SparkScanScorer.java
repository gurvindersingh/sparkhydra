package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.fragment.*;
import com.lordjoe.distributed.hydra.scoring.*;
import com.lordjoe.distributed.output.*;
import com.lordjoe.distributed.spark.*;
import com.lordjoe.distributed.spectrum.*;
import com.lordjoe.utilities.*;
import org.apache.hadoop.conf.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.pepxml.*;
import org.systemsbiology.xtandem.reporting.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;

import java.io.*;
import java.lang.Boolean;
import java.lang.Long;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.SparkScanScorer
 * Attempt to exploit Spark - not converted Hadoop design for scoring
 * User: Steve
 * Date: 10/7/2014
 */
public class SparkScanScorer {

    public static final boolean EXIT_BEFORE_SCORINE = false;
//    public static final String[] InterestingPeptides = {
//            "WYEK[79.966]AAGNEDK[79.966]",
//            "KH[79.966]FAATEK",
//            "GRGVSDNK",
//            "LALKAPPSSK",
//            "T[79.966]MT[79.966]SFESGMDQESLPK",
//            "HAIAVIK"
//
//    };

    private static boolean debuggingCountMade = true;

    public static boolean isDebuggingCountMade() {
        return debuggingCountMade;
    }

    public static void setDebuggingCountMade(final boolean pIsDebuggingCountMade) {
        debuggingCountMade = pIsDebuggingCountMade;
    }


    public static class writeScoresMapper extends AbstractLoggingFunction<Tuple2<String, IScoredScan>, Tuple2<String, String>> {

        final BiomlReporter reporter;

        private writeScoresMapper(final BiomlReporter pReporter) {
            reporter = pReporter;
        }


        @Override
        public Tuple2<String, String> doCall(final Tuple2<String, IScoredScan> v1) throws Exception {
            IScoredScan scan = v1._2();
            StringWriter sw = new StringWriter();
            Appendable out = new PrintWriter(sw);
            reporter.writeScanScores(scan, out, 1);
            return new Tuple2(v1._1(), sw.toString());
        }
    }

    public static class ScanKeyMapper implements PairFlatMapFunction<Iterator<KeyValueObject<String, IScoredScan>>, String, IScoredScan> {
        @Override
        public Iterable<Tuple2<String, IScoredScan>> call(final Iterator<KeyValueObject<String, IScoredScan>> t) throws Exception {
            List<Tuple2<String, IScoredScan>> mapped = new ArrayList<Tuple2<String, IScoredScan>>();
            while (t.hasNext()) {
                KeyValueObject<String, IScoredScan> kscan = t.next();
                IScoredScan value = kscan.value;
                String id = value.getId(); //  now we match scans
                mapped.add(new Tuple2(id, value));
            }
            return mapped;
        }
    }

    public static class DropNoMatchScansFilter extends AbstractLoggingFunction<KeyValueObject<String, IScoredScan>, java.lang.Boolean> {

        @Override
        public java.lang.Boolean doCall(final KeyValueObject<String, IScoredScan> v1) throws Exception {
            IScoredScan vx = v1.value;
            return v1.value.isMatchPresent();
        }
    }

    public static class chooseBestScanScore extends AbstractLoggingFunction2<IScoredScan, IScoredScan, IScoredScan> {

        @Override
        public IScoredScan doCall(final IScoredScan v1, final IScoredScan v2) throws Exception {
            ISpectralMatch match1 = v1.getBestMatch();
            ISpectralMatch match2 = v2.getBestMatch();
            if (match1.getHyperScore() > match2.getHyperScore())
                return v1;
            else
                return v2;
        }
    }

    /**
     * create an output writer
     *
     * @param pApplication
     * @return
     * @throws java.io.IOException
     */
    public static PrintWriter buildWriter(final XTandemMain pApplication) throws IOException {
        String outputPath = BiomlReporter.buildDefaultFileName(pApplication);
        outputPath = outputPath.replace(".xml", ".pep.xml");
        return SparkHydraUtilities.nameToPrintWriter(outputPath, pApplication);
    }


    public static final int SPARK_CONFIG_INDEX = 0;
    public static final int TANDEM_CONFIG_INDEX = 1;
    public static final int SPECTRA_INDEX = 2;
    public static final int SPECTRA_TO_SCORE = Integer.MAX_VALUE;
    public static final String MAX_PROTEINS_PROPERTY = "com.lordjoe.distributed.hydra.MaxProteins";
    public static final String MAX_SPECTRA_PROPERTY = "com.lordjoe.distributed.hydra.MaxSpectra";
    public static final String SKIP_SCORING_PROPERTY = "com.lordjoe.distributed.hydra.SkipScoring";
    public static final String SCORING_PARTITIONS_SCANS_NAME = "com.lordjoe.distributed.max_scoring_partition_scans";
    public static final long MAX_SPECTRA_TO_SCORE_IN_ONE_PASS = Long.MAX_VALUE;


    public static JavaPairRDD<BinChargeKey, IPolypeptide> getBinChargePeptides(final Properties pSparkProperties, final SparkMapReduceScoringHandler pHandler) {
        int max_proteins = 0;
        if (pSparkProperties.containsKey(MAX_PROTEINS_PROPERTY)) {
            max_proteins = Integer.parseInt(pSparkProperties.getProperty(MAX_PROTEINS_PROPERTY));
            System.err.println("Max Proteins " + max_proteins);
        }
        System.err.println("Max Proteins " + max_proteins);


        // handler.buildLibraryIfNeeded();
        // find all polypeptides and modified polypeptides
        JavaRDD<IPolypeptide> databasePeptides = pHandler.buildLibrary(max_proteins);

        if(isDebuggingCountMade())
            databasePeptides = SparkUtilities.persistAndCount("Database peptides", databasePeptides);


        databasePeptides = SparkUtilities.repartitionIfNeeded(databasePeptides);

        // Map peptides into bins
        JavaPairRDD<BinChargeKey, IPolypeptide> keyedPeptides = pHandler.mapFragmentsToKeys(databasePeptides);

        keyedPeptides = SparkUtilities.repartitionIfNeeded(keyedPeptides);

        return keyedPeptides;
    }

    public static JavaRDD<IMeasuredSpectrum> getMeasuredSpectra(final ElapsedTimer pTimer, final Properties pSparkProperties, final String pSpectra) {
        int max_spectra = SPECTRA_TO_SCORE;
        if (pSparkProperties.containsKey(MAX_SPECTRA_PROPERTY)) {
            max_spectra = Integer.parseInt(pSparkProperties.getProperty(MAX_SPECTRA_PROPERTY));
        }
        System.err.println("Max Spectra " + max_spectra);
        // next line is for debugging
        // databasePeptides = SparkUtilities.realizeAndReturn(databasePeptides,new FindInterestingPeptides());
        // System.out.println("Scoring " + databasePeptides.count() + " Peptides");

        // read spectra
        JavaPairRDD<String, IMeasuredSpectrum> scans = SparkSpectrumUtilities.parseSpectrumFile(pSpectra);

        System.out.println(scans.take(5));
        System.err.println("Scans Partitions " + scans.partitions().size());
        JavaRDD<IMeasuredSpectrum> spectraToScore = scans.values();
        System.out.println(spectraToScore.take(5));

        // drop bad ids
        spectraToScore = spectraToScore.filter(new Function<IMeasuredSpectrum, Boolean>() {
            @Override
            public Boolean call(final IMeasuredSpectrum v1) throws Exception {
                String id = v1.getId();
                return id != null && id.length() > 0;
            }
        });

        if(isDebuggingCountMade()) {
            long[] spectraCountRef = new long[1];
            spectraToScore = SparkUtilities.persistAndCount("Spectra  to Score", spectraToScore, spectraCountRef);

            long spectraCount = spectraCountRef[0];

            // filter to fewer spectra todo place in loop
            if (max_spectra > 0 && spectraCount > 0) {
                int countPercentile = 1 + (int) (100 * max_spectra / spectraCount);  // try scoring about 1000
                if (countPercentile < 100) {
                    spectraToScore = spectraToScore.filter(new PercentileFilter<IMeasuredSpectrum>(countPercentile)); // todo make a loop
                    spectraToScore = SparkUtilities.persistAndCount("Filtered Spectra  to Score", spectraToScore, spectraCountRef);
                }
            }
        }


        // next line is for debugging
        // spectraToScore = SparkUtilities.realizeAndReturn(spectraToScore);
        pTimer.showElapsed("got Spectra to Score");
        return spectraToScore;
    }

    /**
     * call with args like or20080320_s_silac-lh_1-1_11short.mzxml in Sample2
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {

        // code to run class loader
        //String runner = SparkUtilities.buildLoggingClassLoaderPropertiesFile(ScanScorer.class  , args);
        //System.out.println(runner);
        ElapsedTimer timer = new ElapsedTimer();
        ElapsedTimer totalTime = new ElapsedTimer();

        if (args.length < TANDEM_CONFIG_INDEX + 1) {
            System.out.println("usage sparkconfig configFile fastaFile");
            return;
        }
        SparkUtilities.readSparkProperties(args[SPARK_CONFIG_INDEX]);

        SparkUtilities.setLogToWarn();


        Properties sparkProperties = SparkUtilities.getSparkProperties();
        String pathPrepend = sparkProperties.getProperty("com.lordjoe.distributed.PathPrepend");
        if (pathPrepend != null)
            XTandemHadoopUtilities.setDefaultPath(pathPrepend);

        String maxScoringPartitionSize = sparkProperties.getProperty(SCORING_PARTITIONS_SCANS_NAME);
        if (maxScoringPartitionSize != null)
            SparkMapReduceScoringHandler.setMaxScoringPartitionSize(Integer.parseInt(maxScoringPartitionSize));


        String configStr = SparkUtilities.buildPath(args[TANDEM_CONFIG_INDEX]);

        //Configuration hadoopConfiguration = SparkUtilities.getHadoopConfiguration();
        //hadoopConfiguration.setLong(org.apache.hadoop.mapreduce.lib.input.FileInputFormat.SPLIT_MAXSIZE, 64 * 1024L * 1024L);

        //Configuration hadoopConfiguration2 = SparkUtilities.getHadoopConfiguration();  // did we change the original or a copy

        String spectra = SparkUtilities.buildPath(args[SPECTRA_INDEX]);
        JavaRDD<IMeasuredSpectrum> spectraToScore = getMeasuredSpectra(timer, sparkProperties, spectra);
        System.err.println("number partitions " + spectraToScore.partitions().size());

        /*if(isDebuggingCountMade()) {
            long[] spectraCounts = new long[1];
            SparkUtilities.persistAndCount("Read Spectra", spectraToScore, spectraCounts);

            long spectraCount = spectraCounts[0];
            if (spectraCount > MAX_SPECTRA_TO_SCORE_IN_ONE_PASS) {
                int percentileKept = (int) ((100L * MAX_SPECTRA_TO_SCORE_IN_ONE_PASS) / spectraCount);
                System.err.println("Keeping " + percentileKept + "% spectra");
                spectraToScore = spectraToScore.filter(new PercentileFilter(percentileKept));
            }
        }*/


        SparkMapReduceScoringHandler handler = new SparkMapReduceScoringHandler(configStr);

        JavaPairRDD<BinChargeKey, IPolypeptide> keyedPeptides = getBinChargePeptides(sparkProperties, handler);
        timer.showElapsed("Mapped Peptides", System.err);
        System.out.println(keyedPeptides.take(5));
        //System.exit(0);
        long[] counts = new long[1];
        if(isDebuggingCountMade()) {
            keyedPeptides = SparkUtilities.persistAndCountPair("Mapped Peptides", keyedPeptides, counts);
        }
        long peptidecounts =  counts[0];

        JavaPairRDD<BinChargeKey, IMeasuredSpectrum> keyedSpectra = handler.mapMeasuredSpectrumToKeys(spectraToScore);
        if(isDebuggingCountMade()) {
            keyedSpectra = SparkUtilities.persistAndCountPair("Mapped Spectra", keyedSpectra, counts);
        }


        if(isDebuggingCountMade()) {
            showBinPairSizes(keyedPeptides, keyedSpectra);
        }
        long spectracounts =  counts[0];

        timer.showElapsed("Counted Scoring pairs", System.err);


        // next line is for debugging
        // keyedSpectra = SparkUtilities.realizeAndReturn(keyedSpectra);

        // find spectra-peptide pairs to score
        JavaPairRDD<BinChargeKey, Tuple2<IPolypeptide,IMeasuredSpectrum>> binPairs = keyedPeptides.join(keyedSpectra,
                SparkUtilities.DEFAULT_PARTITIONER);

        // next line is for debugging
        /// binPairs = SparkUtilities.realizeAndReturn(binPairs);

        System.out.println("number partitions after join" + binPairs.partitions().size());



        // next line is for debugging
        // binPairs = SparkUtilities.realizeAndReturn(binPairs);


        timer.reset();
        if(isDebuggingCountMade())
            binPairs = SparkUtilities.persistAndCount("Binned Pairs", binPairs);
        timer.showElapsed("Joined Pairs",System.err);


        if (EXIT_BEFORE_SCORINE) {
            System.err.println("Exiting before scoring peptidecounts " + peptidecounts + " spectra counts " + spectracounts);
            return;
        }


        String skipScoring = sparkProperties.getProperty(SKIP_SCORING_PROPERTY);
        if ("true".equals(skipScoring)) {
            System.err.println("Skipped scoring" );
        }
        else {
            timer.reset();
            // now produce all peptide spectrum scores where spectrum and peptide are in the same bin
            JavaRDD<IScoredScan> bestScores = handler.scoreBinPairs(binPairs);

            if(isDebuggingCountMade())
                bestScores = SparkUtilities.persistAndCount("Best Scores", bestScores);

            timer.showElapsed("built best scores",System.err);
            //bestScores =  bestScores.persist(StorageLevel.MEMORY_AND_DISK());
            // System.out.println("Total Scores " + bestScores.count() + " Scores");

            XTandemMain application = handler.getApplication();
            PepXMLWriter pwrtr = new PepXMLWriter(application);
            PepXMLScoredScanWriter pWrapper = new PepXMLScoredScanWriter(pwrtr);
            SparkConsolidator consolidator = new SparkConsolidator(pWrapper, application);


            PrintWriter out = buildWriter(application);
            consolidator.writeScores(out, bestScores);
            out.close();
        }


        SparkAccumulators.showAccumulators();
        totalTime.showElapsed("Finished Scoring");

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
            return Long.compare(o.product,  product);
        }

        public String toString() {
            return key.toString() + "spectra " + Long_Formatter.format(v1) + " peptides " + Long_Formatter.format(v2)  +
                    " product " + Long_Formatter.format(product);

        }
    }

    public static void showBinPairSizes(final JavaPairRDD<BinChargeKey, IPolypeptide> keyedPeptides,
                                        final JavaPairRDD<BinChargeKey, IMeasuredSpectrum> keyedSpectra) {
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
            pairCount +=  pairCounter.product;
        }

        Collections.sort(pairs);
        List<PairCounter> pairCounters = pairs.subList(0, Math.min(200,pairs.size()));
        for (PairCounter pairCounter : pairCounters) {
            System.err.println(pairCounter.toString());
        }

        System.err.println("Total Spectra " + Long_Formatter.format(specCount) +
                        " peptides " + Long_Formatter.format(peptideCount) +
                        " bins " + keys.size() +
                 " pairs " + Long_Formatter.format(pairCount)

        );
    }
}
