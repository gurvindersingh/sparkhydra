package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.database.*;
import com.lordjoe.distributed.hydra.*;
import com.lordjoe.distributed.hydra.fragment.*;
import com.lordjoe.distributed.spark.*;
import com.lordjoe.distributed.tandem.*;
import com.lordjoe.utilities.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.common.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.taxonomy.*;
import scala.*;

import java.io.*;
import java.io.Serializable;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.scoring.SparkMapReduceScoringHandler
 * User: Steve
 * Date: 10/7/2014
 */
public class SparkMapReduceScoringHandler implements Serializable {

    private XTandemMain application;

    private final JXTandemStatistics m_Statistics = new JXTandemStatistics();
    private Map<Integer, Integer> sizes;
    private final BinChargeMapper binMapper;
    private SequenceUtilities sequenceUtilities;
    private transient Scorer scorer;
    private transient ITandemScoringAlgorithm algorithm;
    private transient List<IPolypeptide> currentPeptides;
    private transient BinChargeKey currentKey;
    private Map<IPolypeptide, ITheoreticalSpectrumSet> currentTheoreticalSpectra = new HashMap<IPolypeptide, ITheoreticalSpectrumSet>();
    private PeptideDatabase peptideDatabase;


    public SparkMapReduceScoringHandler(String congiguration, boolean createDb) {

        SparkUtilities.setAppName("SparkMapReduceScoringHandler");

        InputStream is = HydraSparkUtilities.readFrom(congiguration);

        application = new SparkXTandemMain(is, congiguration);
        sequenceUtilities = new SequenceUtilities(application);
        if (createDb == true)
            peptideDatabase = new PeptideDatabase(application);

            SparkConf sparkConf = SparkUtilities.getCurrentContext().getConf();
        /**
         * copy application parameters to spark context
         */
        for (String key : application.getParameterKeys()) {
            sparkConf.set(key, application.getParameter(key));
        }

        ITaxonomy taxonomy = application.getTaxonomy();
        System.err.println(taxonomy.getOrganism());

        binMapper = new BinChargeMapper(this);

    }


//    public IFileSystem getAccessor() {
//
//        return accessor;
//    }

    public Scorer getScorer() {
        if (scorer == null)
            scorer = getApplication().getScoreRunner();

        return scorer;
    }


    public ITandemScoringAlgorithm getAlgorithm() {
        if (algorithm == null)
            algorithm = application.getAlgorithms()[0];

        return algorithm;
    }

    public JXTandemStatistics getStatistics() {
        return m_Statistics;
    }



    /**
     * read any cached database parameters
     *
     * @param context     !null context
     * @param application !null application
     * @return possibly null descripotion - null is unreadable
     */
    public DigesterDescription readDigesterDescription(XTandemMain application) {
        try {
            String paramsFile = application.getDatabaseName() + ".params";
            InputStream fsin = SparkHydraUtilities.nameToInputStream(paramsFile, application);
            if (fsin == null)
                return null;
            DigesterDescription ret = new DigesterDescription(fsin);
            return ret;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }

    public boolean isDatabaseBuildRequired() {
        XTandemMain application = getApplication();
        boolean buildDatabase;
        // Validate build parameters
        DigesterDescription existingDatabaseParameters = null;
        try {
            existingDatabaseParameters = readDigesterDescription(application);
        }
        catch (Exception e) {
            return true; // bad descriptor
        }
        // we have a database
        if (existingDatabaseParameters != null) {
            DigesterDescription desired = DigesterDescription.fromApplication(application);
            if (desired.equivalent(existingDatabaseParameters)) {
                buildDatabase = false;
            }
            else {
                buildDatabase = true;
                // kill the database
                Path dpath = XTandemHadoopUtilities.getRelativePath(application.getDatabaseName());
                IFileSystem fileSystem = HydraSparkUtilities.getHadoopFileSystem();
                fileSystem.expunge(dpath.toString());
            }

        }
        else {
            return true;
        }
        Configuration configuration = SparkUtilities.getCurrentContext().hadoopConfiguration();
        Map<Integer, Integer> sizeMap = XTandemHadoopUtilities.guaranteeDatabaseSizes(application, configuration);
        if (sizeMap == null) {
            return true;
        }
        JXTandemStatistics statistics = getStatistics();
        long totalFragments = XTandemHadoopUtilities.sumDatabaseSizes(sizeMap);
        if (totalFragments < 1) {
            return true;
        }

//        long MaxFragments = XTandemHadoopUtilities.maxDatabaseSizes(sizeMap);
//        statistics.setData("Total Fragments", Long.toString(totalFragments));
//        statistics.setData("Max Mass Fragments", Long.toString(MaxFragments));

        return buildDatabase;
    }


    public XTandemMain getApplication() {
        return application;
    }




    public JavaPairRDD<BinChargeKey, IMeasuredSpectrum> mapMeasuredSpectrumToKeys(JavaRDD<IMeasuredSpectrum> inp) {
        inp = SparkUtilities.repartitionIfNeeded(inp);
        return binMapper.mapMeasuredSpectrumToKeys(inp);
    }

    public JavaPairRDD<BinChargeKey, Tuple2<BinChargeKey, IMeasuredSpectrum>> mapMeasuredSpectrumToKeySpectrumPair(JavaRDD<IMeasuredSpectrum> inp) {
        inp = SparkUtilities.repartitionIfNeeded(inp);
        JavaPairRDD<BinChargeKey, Tuple2<BinChargeKey, IMeasuredSpectrum>> allSpectrumPairs = binMapper.mapMeasuredSpectrumToKeySpectrumPair(inp);
        // debugging only remove
        // allSpectrumPairs = SparkUtilities.persistAndCount("MapSpectraTo Keys",allSpectrumPairs);
        allSpectrumPairs = peptideDatabase.filterKeysWithData(allSpectrumPairs);
        // debugging only remove
        allSpectrumPairs = SparkUtilities.persistAndCount("Filter MapSpectraTo Keys", allSpectrumPairs);
        return allSpectrumPairs;
    }

    public JavaPairRDD<BinChargeKey, IPolypeptide> mapFragmentsToKeys(JavaRDD<IPolypeptide> inp) {
        return binMapper.mapFragmentsToKeys(inp);
    }


    private static class MapToSpectrumIDKey extends AbstractLoggingPairFunction<Tuple2<IMeasuredSpectrum, IPolypeptide>, String, Tuple2<IMeasuredSpectrum, IPolypeptide>> {
        @Override
        public Tuple2<String, Tuple2<org.systemsbiology.xtandem.IMeasuredSpectrum, IPolypeptide>> doCall(final Tuple2<org.systemsbiology.xtandem.IMeasuredSpectrum, org.systemsbiology.xtandem.peptide.IPolypeptide> t) throws Exception {
            return new Tuple2<String, Tuple2<IMeasuredSpectrum, IPolypeptide>>(t._1().getId(), t);
        }
    }


    public static final double MIMIMUM_HYPERSCORE = 50;
    public static final int DEFAULT_MAX_SCORINGS_PER_PARTITION = 20000;

    private static int maxScoringPartitionSize = DEFAULT_MAX_SCORINGS_PER_PARTITION;

    public static int getMaxScoringPartitionSize() {
        return maxScoringPartitionSize;
    }

    public static void setMaxScoringPartitionSize(final int pMaxScoringPartitionSize) {
        maxScoringPartitionSize = pMaxScoringPartitionSize;
    }

    /**
     * scoring up to 16-Jan-2015
     *
     * @param binPairs
     * @return
     */
    public JavaRDD<IScoredScan> scoreBinPairsOld(JavaPairRDD<BinChargeKey, Tuple2<IPolypeptide, IMeasuredSpectrum>> binPairs) {
        ElapsedTimer timer = new ElapsedTimer();

        JavaRDD<Tuple2<IPolypeptide, IMeasuredSpectrum>> values = binPairs.values();

//       long[] totalValues = new long[1];
//       //   JavaRDD<IScoredScan> scores = binPairs.mapPartitions(new ScoreScansByCharge());
//        values = SparkUtilities.persistAndCountTuple("Scans to Score", values, totalValues);
//        long numberValues = totalValues[0];

        values = SparkUtilities.repartitionTupleIfNeeded(values);
//       int numberScoringPartitions = 1 + (int)(numberValues / getMaxScoringPartitionSize());
//        numberScoringPartitions = Math.max(SparkUtilities.getDefaultNumberPartitions(),numberScoringPartitions);
//        System.err.println("Number scoring partitions " + numberScoringPartitions);
//
//        values = values.coalesce(numberScoringPartitions,true); // force a shuffle
        //  values = values.coalesce(SparkUtilities.getDefaultNumberPartitions(),true); // force a shuffle

        JavaRDD<IScoredScan> scores = values.flatMap(new ScoreScansByCharge(getApplication()));

        //   JavaRDD<IScoredScan> scores = binPairs.mapPartitions(new ScoreScansByCharge());
        //scores = SparkUtilities.persistAndCount("Scored Scans", scores);

        JavaPairRDD<String, IScoredScan> scoreByID = scores.mapToPair(new ScoredScanToId());

        scoreByID = SparkUtilities.persistAndShowHash("Scoring", scoreByID);

        // next line is for debugging
        // boolean forceInCluster = true;
        // scoreByID = SparkUtilities.realizeAndReturn(scoreByID, forceInCluster);

        scoreByID = scoreByID.combineByKey(SparkUtilities.IDENTITY_FUNCTION, new CombineScoredScans(),
                new CombineScoredScans()
        );

        //scoreByID = SparkUtilities.persistAndCount("Scored Scans by ID - best", scoreByID);

        timer.showElapsed("built score by ids");


        // sort by id
        // scoreByID = scoreByID.sortByKey();
        // next line is for debugging
        //scoreByID = SparkUtilities.realizeAndReturn(scoreByID);


        return scoreByID.values();
    }

    /**
     * new scoring mapping by Spectrun key before scoring
     *
     * @param binPairs
     * @return
     */
    public JavaRDD<IScoredScan> scoreBinPairs(JavaPairRDD<BinChargeKey, Tuple2<IPolypeptide, IMeasuredSpectrum>> binPairs) {
        ElapsedTimer timer = new ElapsedTimer();

        JavaPairRDD<String, Tuple2<IPolypeptide, IMeasuredSpectrum>> bySpectrumId =
                binPairs.mapToPair(new MapBinChargeTupleToSpectrumIDTuple());

        bySpectrumId = SparkUtilities.repartitionIfNeeded(bySpectrumId);


        JavaPairRDD<String, IScoredScan> scores = bySpectrumId.aggregateByKey(
                getStartScan(),
                new CombineScoredScanWithScore(),
                new CombineScoredScans()
        );
        //     JavaRDD<IScoredScan> scores = bySpectrumId.mapPartitions(new ScoreSpectrumAgainstAllPeptides());


        timer.showElapsed("built score by ids");

        return scores.values();
    }


    public IScoredScan getStartScan() {
        return new ScoredScan(null);
    }

    public JavaRDD<IScoredScan> scoreSpectra(JavaPairRDD<BinChargeKey, Tuple2<BinChargeKey, IMeasuredSpectrum>> scoredSpectra) {
               throw new UnsupportedOperationException("Fix This"); // ToDo
//        JavaPairRDD<BinChargeKey, Map<String, IScoredScan>> binnedScores = scoredSpectra.combineByKey(
//                new Function<Tuple2<BinChargeKey, IMeasuredSpectrum>, Map<String, IScoredScan>>() {
//                    @Override
//                    public Map<String, IScoredScan> call(final Tuple2<BinChargeKey, IMeasuredSpectrum> scantuple) throws Exception {
//                        Map<String, IScoredScan> ret = new HashMap<String, IScoredScan>();
//                        BinChargeKey key = scantuple._1();
//                        IMeasuredSpectrum scan = scantuple._2();
//                        scoreScanInMap(key, scan, ret);
//                        return ret;
//                    }
//                },
//                new Function2<Map<String, IScoredScan>, Tuple2<BinChargeKey, IMeasuredSpectrum>, Map<String, IScoredScan>>() {
//                    @Override
//                    public Map<String, IScoredScan> call(final Map<String, IScoredScan> pStringIScoredScanMap, final Tuple2<BinChargeKey, IMeasuredSpectrum> scantuple) throws Exception {
//                        BinChargeKey key = scantuple._1();
//                        IMeasuredSpectrum scan = scantuple._2();
//                        scoreScanInMap(key, scan, pStringIScoredScanMap);
//                        return pStringIScoredScanMap;
//                    }
//                }, new Function2<Map<String, IScoredScan>, Map<String, IScoredScan>, Map<String, IScoredScan>>() {
//                    @Override
//                    public Map<String, IScoredScan> call(final Map<String, IScoredScan> map1, final Map<String, IScoredScan> map2) throws Exception {
//                        for (String s : map1.keySet()) {
//                            IScoredScan scan1 = map1.get(s);
//                            IScoredScan scan2 = map2.get(s);
//                            if (scan2 != null) {
//                                scan1.addTo(scan2);
//                            }
//                        }
//                        for (String s : map2.keySet()) {
//                            IScoredScan scan1 = map1.get(s);
//                            IScoredScan scan2 = map2.get(s);
//                            if (scan1 == null) {
//                                map1.put(s, scan2);
//                            }
//                        }
//                        return map1;
//                    }
//                });
//        JavaPairRDD<String, IScoredScan> idToScan = binnedScores.values().flatMapToPair(new ScoredScansToIds());
//        JavaPairRDD<String, IScoredScan> combinedValues = idToScan.combineByKey(
//                new Function<IScoredScan, IScoredScan>() {
//                    @Override
//                    public IScoredScan call(final IScoredScan scan) throws Exception {
//                        return scan;
//                    }
//                },
//                new Function2<IScoredScan, IScoredScan, IScoredScan>() {
//                    @Override
//                    public IScoredScan call(final IScoredScan scan1, final IScoredScan scan2) throws Exception {
//                        scan1.addTo(scan2);
//                        return scan1;
//                    }
//                },
//                new Function2<IScoredScan, IScoredScan, IScoredScan>() {
//                    @Override
//                    public IScoredScan call(final IScoredScan scan1, final IScoredScan scan2) throws Exception {
//                        scan1.addTo(scan2);
//                        return scan1;
//                    }
//                });
//
//        return combinedValues.values();
    }

    public void scoreScanInMap(BinChargeKey key, IMeasuredSpectrum scan, Map<String, IScoredScan> scores) {
        throw new UnsupportedOperationException("Fix This"); // ToDo`
//        String id = scan.getId();
//        StringBuffer sb = new StringBuffer();
////        IXMLAppender appender = new XMLAppender(sb);
////        scan.serializeAsString(appender);
//
//        List<IPolypeptide> peptides = getPeptides(key);
//        if (peptides.isEmpty())
//            return; // nothing to score
//
//
//        List<ITheoreticalSpectrumSet> peptideSpectra = new ArrayList<ITheoreticalSpectrumSet>();
//        for (IPolypeptide peptide : peptides) {
//            ITheoreticalSpectrumSet ts = TheoreticalSetGenerator.generateTheoreticalSet(peptide, TheoreticalSetGenerator.MAX_CHARGE, sequenceUtilities);
//            peptideSpectra.add(ts);
//        }
//        ITheoreticalSpectrumSet[] ts = peptideSpectra.toArray(new ITheoreticalSpectrumSet[peptideSpectra.size()]);
//        IScoredScan ret = new OriginatingScoredScan(scan);
//        IonUseCounter counter = new IonUseCounter();
//        Scorer scorer1 = getScorer();
//        scorer1.generateTheoreticalSpectra();
//        ITandemScoringAlgorithm algorithm1 = getAlgorithm();
//        int n = algorithm1.scoreScan(scorer1, counter, ts, ret);
//
//        scores.put(id, ret);
    }

    protected List<IPolypeptide> getPeptides(final BinChargeKey key) {
        if (currentKey != null) {
            if (key.equals(currentKey))
                return currentPeptides;
        }
        currentKey = key;
        currentPeptides = buildPeptides(currentKey);
        return currentPeptides;
    }

    protected List<IPolypeptide> buildPeptides(final BinChargeKey pCurrentKey) {
        List<IPolypeptide> ret = new ArrayList<IPolypeptide>();
        currentTheoreticalSpectra.clear();
        return peptideDatabase.getPeptidesWithKey(pCurrentKey);
    }

//    protected void addTheoreticalSpectrum(IPolypeptide pp)   {
//
//         ITheoreticalSpectrumSet set = new TheoreticalSpectrumSet(precursorCharge, precursorMass, pp);
//         for (int charge = 1; charge <= precursorCharge; charge++) {
//             PeptideSpectrum ps = new PeptideSpectrum(set, charge, IonType.B_ION_TYPES, sequenceUtilities);
//             ITheoreticalSpectrum conditioned = ScoringUtilities.applyConditioner(ps,
//                   new XTandemTheoreticalScoreConditioner());
//             set.setSpectrum(conditioned);
//         }
//        currentTheoreticalSpectra.put(pp,set);
//    }

    private IScoredScan getMappedScore(IMeasuredSpectrum scan, final Map<String, IScoredScan> pScores) {
        IScoredScan ret = pScores.get(scan.getId());
        if (ret == null) {
            ret = new ScoredScan(null);
            pScores.put(scan.getId(), ret);
        }
        return ret;
    }

    public JavaRDD<IScoredScan> scoreBinPairsOldCode(JavaPairRDD<BinChargeKey, Tuple2<IMeasuredSpectrum, IPolypeptide>> binPairs) {
        ElapsedTimer timer = new ElapsedTimer();

        // ==============================
        // New code - this worked this has not worked on a large case
        JavaPairRDD<String, Tuple2<IMeasuredSpectrum, IPolypeptide>> keyedScoringPairs = binPairs.mapToPair(new ToScoringTuples());
        //  END New code - this worked this has not worked on a large case

        // distribute the work
        keyedScoringPairs = SparkUtilities.guaranteePairedPartition(keyedScoringPairs);


        // ==============================
        // Old code - this worked 11/24

//        // drop the keys- we no longer need them
//        JavaRDD<Tuple2<IMeasuredSpectrum, IPolypeptide>> values = binPairs.values();
//
//
////        // next line is for debugging
////        //values = SparkUtilities.realizeAndReturn(values);
////
////        // convert to a PairRDD to keep spark happy
////        JavaPairRDD<IMeasuredSpectrum, IPolypeptide> valuePairs = SparkUtilities.mapToKeyedPairs(values);
////
////        // next line is for debugging
////        // valuePairs = SparkUtilities.realizeAndReturn(valuePairs);
////
////        // bring key (original data) into value since we need to for scoring
////        JavaPairRDD<IMeasuredSpectrum, Tuple2<IMeasuredSpectrum, IPolypeptide>> keyedScoringPairs = SparkUtilities.mapToKeyedPairs(
////                valuePairs);
//
//        //       JavaPairRDD<IMeasuredSpectrum, Tuple2<IMeasuredSpectrum, IPolypeptide>> keyedScoringPairs = SparkUtilities.mapToKeyedPairs(values);
//
//
//        JavaPairRDD<String, Tuple2<IMeasuredSpectrum, IPolypeptide>> keyedScoringPairs = values.mapToPair(new MapToSpectrumIDKey());
        // =End Old code
        // ==============================

        /// next line is for debugging
        //keyedScoringPairs = SparkUtilities.realizeAndReturn(keyedScoringPairs);

        timer.showElapsed("built scoring pairs");


        JavaPairRDD<String, IScoredScan> scoreByID = keyedScoringPairs.combineByKey(
                new GenerateFirstScore(getApplication()),
                new AddNewScore(getApplication()),
                new CombineScoredScans()
                // todo id is string use a good string partitioner
                // SparkHydraUtilities.getMeasuredSpectrumPartitioner()
        );

        timer.showElapsed("built scorings");

        /// next line is for debugging
        // scorings = SparkUtilities.realizeAndReturn(scorings);

//
//        JavaPairRDD<String, IScoredScan> scoreByID = scorings.mapToPair(new PairFunction<Tuple2<IMeasuredSpectrum, IScoredScan>, String, IScoredScan>() {
//            @Override
//            public Tuple2<String, IScoredScan> call(final Tuple2<IMeasuredSpectrum, IScoredScan> t) throws Exception {
//                return new Tuple2<String, IScoredScan>(t._1().getId(), t._2());
//            }
//        });


        // next line is for debugging
        // scoreByID = SparkUtilities.realizeAndReturn(scoreByID);

        scoreByID = scoreByID.combineByKey(SparkUtilities.IDENTITY_FUNCTION, new CombineScoredScans(),
                new CombineScoredScans()
        );

        timer.showElapsed("built score by ids");


        // sort by id
        scoreByID = scoreByID.sortByKey();
        // next line is for debugging
        //scoreByID = SparkUtilities.realizeAndReturn(scoreByID);


        return scoreByID.values();
    }


    /**
     * delegate scoring to the algorithm
     *
     * @param spec
     * @param pp
     * @return
     */
    protected static IScoredScan scoreOnePeptide(RawPeptideScan spec, IPolypeptide pp, Scorer scorer1, ITandemScoringAlgorithm algorithm1) {
        ITheoreticalSpectrumSet ts =  scorer1.generateSpectrum(pp);
        IPolypeptide[] pps = { pp };
        ITheoreticalSpectrumSet[] tts = { ts };
          //     Scorer scorer1 = getScorer();
           IScoredScan ret = algorithm1.handleScan(scorer1, spec, pps,tts);
           return ret;
    }

    public Map<Integer, Integer> getDatabaseSizes() {
        if (sizes == null) {
            LibraryBuilder libraryBuilder = new LibraryBuilder(this);
            sizes = libraryBuilder.getDatabaseSizes();
        }
        return sizes;

    }

    public JavaRDD<IPolypeptide> buildLibrary(int maxProteins) {
        //clearAllParams(getApplication());

        LibraryBuilder libraryBuilder = new LibraryBuilder(this);
        return libraryBuilder.buildLibrary(maxProteins);
    }

    private static class ToScoringTuples extends AbstractLoggingPairFunction<Tuple2<BinChargeKey, Tuple2<IMeasuredSpectrum, IPolypeptide>>, String, Tuple2<IMeasuredSpectrum, IPolypeptide>> {
        @Override
        public Tuple2<String, Tuple2<IMeasuredSpectrum, IPolypeptide>> doCall(final Tuple2<BinChargeKey, Tuple2<IMeasuredSpectrum, IPolypeptide>> t) throws Exception {
            Tuple2<IMeasuredSpectrum, IPolypeptide> tpl = t._2();

            IMeasuredSpectrum ms = tpl._1();
            String id = ms.getId();
            return new Tuple2(id, tpl);
        }
    }

    static int[] hashes = new int[100];
    static int numberProcessed;

    private static class ScoredScanToId extends AbstractLoggingPairFunction<IScoredScan, String, IScoredScan> {
        @Override
        public Tuple2<String, IScoredScan> doCall(final IScoredScan t) {
            ISpectralMatch bestMatch = t.getBestMatch();
            IMeasuredSpectrum measured = bestMatch.getMeasured();
            String id = measured.getId();

            // debugging CODE!!! todo remove
            hashes[Math.abs(id.hashCode()) % hashes.length]++;
            if (numberProcessed > 0 && numberProcessed % 10 == 0) {
                numberProcessed += 0;       // break here
            }
            if (!(id.equals("31104_Berit_BSA2.5115.5115.3"))) {
                numberProcessed += 0;   // break here

            }

            return new Tuple2<String, IScoredScan>(id, t);
        }
    }

    private static class GenerateFirstScore extends AbstractLoggingFunction<Tuple2<IMeasuredSpectrum, IPolypeptide>, IScoredScan> {
        private final XTandemMain application;

        public GenerateFirstScore(final XTandemMain pApplication) {
            application = pApplication;
        }

        @Override
        public IScoredScan doCall(final Tuple2<IMeasuredSpectrum, IPolypeptide> v1) throws Exception {
            IMeasuredSpectrum spec = v1._1();
            IPolypeptide pp = v1._2();
            if (!(spec instanceof RawPeptideScan))
                throw new IllegalStateException("We can only handle RawScans Here");
            RawPeptideScan rs = (RawPeptideScan) spec;
            IScoredScan ret = scoreOnePeptide(rs, pp, application.getScoreRunner(), application.getScorer());
            return ret;
        }
    }


    public static class AddNewScore extends AbstractLoggingFunction2<IScoredScan, Tuple2<IMeasuredSpectrum, IPolypeptide>, IScoredScan> {
        private final XTandemMain application;

        public AddNewScore(final XTandemMain pApplication) {
            application = pApplication;
        }

        @Override
        public IScoredScan doCall(final IScoredScan original, final Tuple2<IMeasuredSpectrum, IPolypeptide> v2) throws Exception {
            IMeasuredSpectrum spec = v2._1();
            IPolypeptide pp = v2._2();
            if (!(spec instanceof RawPeptideScan))
                throw new IllegalStateException("We can only handle RawScans Here");
            RawPeptideScan rs = (RawPeptideScan) spec;
            IScoredScan ret = scoreOnePeptide(rs, pp, application.getScoreRunner(), application.getScorer());
            if (original.getBestMatch() == null)
                return ret;
            if (ret.getBestMatch() == null)
                return original;


            // Add new matches - only the best will be retaind
            for (ISpectralMatch match : ret.getSpectralMatches()) {

                ((OriginatingScoredScan) original).addSpectralMatch(match);
            }
            return original;
        }
    }


    private static class CombineScoredScans extends AbstractLoggingFunction2<IScoredScan, IScoredScan, IScoredScan> {
        @Override
        public IScoredScan doCall(final IScoredScan original, final IScoredScan added) throws Exception {
            if(original.getRaw() == null || original.getBestMatch() == null)
                return added;
            if(added.getRaw() == null || added.getBestMatch() == null)
                   return original;

            // Add new matches - only the best will be retaind
            for (ISpectralMatch match : added.getSpectralMatches()) {
                ((OriginatingScoredScan) original).addSpectralMatch(match);
            }
            return original;
        }
    }

    public static class ScoredScansToIds extends AbstractLoggingPairFlatMapFunction<Map<String, IScoredScan>, String, IScoredScan> {
        @Override
        public Iterable<Tuple2<String, IScoredScan>> doCall(final Map<String, IScoredScan> t) throws Exception {
            List<Tuple2<String, IScoredScan>> ret = new ArrayList<Tuple2<String, IScoredScan>>();
            for (String s : t.keySet()) {
                ret.add(new Tuple2<String, IScoredScan>(s, t.get(s)));
            }
            return ret;
        }
    }


    public static class ScoreScansByCharge extends AbstractLoggingFlatMapFunction<Tuple2<IPolypeptide, IMeasuredSpectrum>, IScoredScan> {
        private final XTandemMain application;

        public ScoreScansByCharge(final XTandemMain pApplication) {
            super();
            SparkAccumulators.createAccumulator("NumberSavedScores");
            application = pApplication;
        }


        /**
         * do work here
         *
         * @param t@return
         */
        @Override
        public Iterable<IScoredScan> doCall(final Tuple2<IPolypeptide, IMeasuredSpectrum> t) throws Exception {
            List<IScoredScan> holder = new ArrayList<IScoredScan>();


            IMeasuredSpectrum spec = t._2();
            IPolypeptide pp = t._1();
            if (!(spec instanceof RawPeptideScan))
                throw new IllegalStateException("We can only handle RawScans Here");
            RawPeptideScan rs = (RawPeptideScan) spec;
            IScoredScan score = scoreOnePeptide(rs, pp, application.getScoreRunner(), application.getScorer());
            ISpectralMatch bestMatch = score.getBestMatch();
            if (bestMatch != null) {
                if (bestMatch.getHyperScore() > MIMIMUM_HYPERSCORE) {
                    SparkAccumulators instance = SparkAccumulators.getInstance();
                    if (instance != null)
                        instance.incrementAccumulator("NumberSavedScores");
                    holder.add(score);
                }
            }
            return holder;
        }


    }


    public static class ScoreScansByChargeFlatMap extends AbstractLoggingFlatMapFunction<Iterator<Tuple2<BinChargeKey, Tuple2<IMeasuredSpectrum, IPolypeptide>>>, IScoredScan> {
        private final XTandemMain application;

        public ScoreScansByChargeFlatMap(final XTandemMain pApplication) {
            super();
            SparkAccumulators.createAccumulator("NumberSavedScores");
            application = pApplication;
        }


        @Override
        public Iterable<IScoredScan> doCall(final Iterator<Tuple2<BinChargeKey, Tuple2<IMeasuredSpectrum, IPolypeptide>>> t) throws Exception {
            List<IScoredScan> holder = new ArrayList<IScoredScan>();

            while (t.hasNext()) {
                Tuple2<BinChargeKey, Tuple2<IMeasuredSpectrum, IPolypeptide>> vx = t.next();
                Tuple2<IMeasuredSpectrum, IPolypeptide> v1 = vx._2();
                IMeasuredSpectrum spec = v1._1();
                IPolypeptide pp = v1._2();
                if (!(spec instanceof RawPeptideScan))
                    throw new IllegalStateException("We can only handle RawScans Here");
                RawPeptideScan rs = (RawPeptideScan) spec;
                IScoredScan ret = scoreOnePeptide(rs, pp, application.getScoreRunner(), application.getScorer());
                ISpectralMatch bestMatch = ret.getBestMatch();
                if (bestMatch != null) {
                    try {
                        double hyperScore = bestMatch.getHyperScore();
                        if (hyperScore > MIMIMUM_HYPERSCORE)
                            holder.add(ret);
                    }
                    catch (Exception e) {
                        // ignore any bad stuff and do not save score

                    }
                }
            }
            SparkAccumulators instance = SparkAccumulators.getInstance();
            if (instance != null)
                instance.incrementAccumulator("NumberSavedScores", holder.size());
            return holder;
        }
    }

    public static class MapBinChargeTupleToSpectrumIDTuple extends AbstractLoggingPairFunction<Tuple2<BinChargeKey, Tuple2<IPolypeptide, IMeasuredSpectrum>>, String, Tuple2<IPolypeptide, IMeasuredSpectrum>> {
        @Override
        public Tuple2<String, Tuple2<IPolypeptide, IMeasuredSpectrum>> doCall(final Tuple2<BinChargeKey, Tuple2<IPolypeptide, IMeasuredSpectrum>> t) throws Exception {
            Tuple2<IPolypeptide, IMeasuredSpectrum> pair = t._2();
            IMeasuredSpectrum spec = pair._2();
            String id = spec.getId();
            return new Tuple2<String, Tuple2<IPolypeptide, IMeasuredSpectrum>>(id, pair);
        }
    }

    public class ScoreSpectrumAgainstAllPeptides extends AbstractLoggingFlatMapFunction<Iterator<Tuple2<String, Tuple2<IPolypeptide, IMeasuredSpectrum>>>, IScoredScan> {
        @Override
        public Iterable<IScoredScan> doCall(final Iterator<Tuple2<String, Tuple2<IPolypeptide, IMeasuredSpectrum>>> t) throws Exception {
            IScoredScan cummulativeScore = null;
            List<IScoredScan> holder = new ArrayList<IScoredScan>();
            String cummunativeId;
            while (t.hasNext()) {
                Tuple2<IPolypeptide, IMeasuredSpectrum> toScore = t.next()._2();
                IMeasuredSpectrum spec = toScore._2();
                IPolypeptide pp = toScore._1();
                if (!(spec instanceof RawPeptideScan))
                    throw new IllegalStateException("We can only handle RawScans Here");
                RawPeptideScan rs = (RawPeptideScan) spec;
                IScoredScan score = scoreOnePeptide(rs, pp, application.getScoreRunner(), application.getScorer());
                if (cummulativeScore == null) {
                    cummulativeScore = score;
                    cummunativeId = score.getId();
                    holder.add(cummulativeScore);
                }
                else {
                    String thisId = score.getId();
                    cummulativeScore.addTo(score);
                }
                ISpectralMatch bestMatch = cummulativeScore.getBestMatch();
                if (bestMatch != null) {
                    if (bestMatch.getHyperScore() > MIMIMUM_HYPERSCORE) {
                        SparkAccumulators instance = SparkAccumulators.getInstance();
                        if (instance != null)
                            instance.incrementAccumulator("NumberSavedScores");
                        holder.add(score);
                    }
                }
            }
            return holder;
        }
    }

    public class CombineScoredScanWithScore extends AbstractLoggingFunction2<IScoredScan, Tuple2<IPolypeptide, IMeasuredSpectrum>, IScoredScan> {
        @Override
        public IScoredScan doCall(final IScoredScan v1, final Tuple2<IPolypeptide, IMeasuredSpectrum> v2) throws Exception {
            Tuple2<IPolypeptide, IMeasuredSpectrum> toScore = v2;
            IMeasuredSpectrum spec = toScore._2();
            IPolypeptide pp = toScore._1();
            if (!(spec instanceof RawPeptideScan))
                throw new IllegalStateException("We can only handle RawScans Here");
            RawPeptideScan rs = (RawPeptideScan) spec;
            IScoredScan score = scoreOnePeptide(rs, pp, application.getScoreRunner(), application.getScorer());
            if (v1.getRaw() == null)
                return score;
            v1.addTo(score);
            return v1;
        }
    }


//    protected void clearAllParams(XTandemMain application) {
//        String databaseName = application.getDatabaseName();
//        String paramsFile = databaseName + ".params";
//        Path dd = XTandemHadoopUtilities.getRelativePath(paramsFile);
//        IFileSystem fs = getAccessor();
//        String hdfsPath = dd.toString();
//        if (fs.exists(hdfsPath))
//            fs.deleteFile(hdfsPath);
//
//    }


}