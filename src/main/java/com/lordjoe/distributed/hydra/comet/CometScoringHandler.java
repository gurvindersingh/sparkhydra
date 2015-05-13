package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.fragment.*;
import com.lordjoe.distributed.hydra.scoring.*;
import com.lordjoe.distributed.hydra.test.*;
import com.lordjoe.utilities.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;
import scala.Function;

import java.lang.*;
import java.lang.Float;
import java.util.*;

import static com.lordjoe.distributed.hydra.test.TestUtilities.breakHere;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringHandler
 * do the real work of running the comet algorithm
 * User: Steve
 * Date: 4/10/2015
 */
public class CometScoringHandler extends SparkMapReduceScoringHandler {


    public CometScoringHandler(final String congiguration, final boolean createDb) {

        super(congiguration, createDb);
    }

    private static class CometCombineScoredScans extends AbstractLoggingFunction2<CometScoredScan, CometScoredScan, CometScoredScan> {
        @Override
        public CometScoredScan doCall(final CometScoredScan s1, final CometScoredScan s2) throws Exception {
            if (s1.getRaw() == null)
                return s2;
            if (s1.getBestMatch() == null)
                return s2;
            if (s2.getRaw() == null)
                return s1;
            if (s2.getBestMatch() == null)
                return s1;
            if (!s1.getId().equals(s2.getId()))
                throw new IllegalStateException("Attempting to combine " + s1.getId() + " and " + s2.getId());
            s1.addTo(s2);
            return s1;
        }
    }

    private static class CombineCometScoringResults extends AbstractLoggingFunction2<CometScoringResult, CometScoringResult, CometScoringResult> {
        @Override
        public CometScoringResult doCall(final CometScoringResult s1, final CometScoringResult s2) throws Exception {
            if (!s1.isValid())
                return s2;
            if (!s2.isValid())
                return s1;
            if (!s1.getId().equals(s2.getId()))
                throw new IllegalStateException("Attempting to combine " + s1.getId() + " and " + s2.getId());
            s1.addTo(s2);
            return s1;
        }
    }

    private static class scoreSpectrumAndPeptideList implements FlatMapFunction<Tuple2<CometScoredScan, ArrayList<IPolypeptide>>, IScoredScan> {

        private CometScoringAlgorithm comet;
        private Scorer scorer;

        public scoreSpectrumAndPeptideList(XTandemMain application) {
            comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
            scorer = application.getScoreRunner();

        }

        @Override
        public Iterable<IScoredScan> call(Tuple2<CometScoredScan, ArrayList<IPolypeptide>> inp) throws Exception {
            List<IScoredScan> ret = new ArrayList<IScoredScan>();
            CometScoredScan scan = inp._1();
            ArrayList<IPolypeptide> peptides = inp._2();

            final CometScoringData scoringData = CometScoringData.getScoringData();
            // in this section we pregenerate data for a spectrum and reuse it
            scoringData.clearData();

            final Map<Integer, java.lang.Float> fastScoringMap = scan.getFastScoringMap();

            float[] fastXcorrDataMap = scoringData.getTmpFastXcorrData();
            for (Integer i : fastScoringMap.keySet()) {
                fastXcorrDataMap[i] = fastScoringMap.get(i);
            }

            final Map<Integer, Float> fastScoringMapNL = scan.getFastScoringMapNL();   // we used to get from commented scoring data
            float[] fastXcorrDataNL = scoringData.getTmpFastXcorrData2();
            for (Integer i : fastScoringMapNL.keySet()) {
                fastXcorrDataNL[i] = fastScoringMapNL.get(i);
            }


            List<CometTheoreticalBinnedSet> holder = new ArrayList<CometTheoreticalBinnedSet>();


            // just populate peptide data do not count time
            for (IPolypeptide pp : peptides) {
                CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(pp);
                holder.add(ts);
            }


            // use pregenerated peptide data but not epetide data
            double maxScore = 0;
            for (CometTheoreticalBinnedSet ts : holder) {
                IonUseCounter counter = new IonUseCounter();
                double xcorr = comet.doXCorrWithData(ts, scorer, counter, scan, fastXcorrDataMap, fastXcorrDataNL);
                maxScore = Math.max(xcorr, maxScore);
            }


            return ret;
        }
    }

    /**
     * NOIE This class is REALLY important - ALL Comet scoring happens here
     */
    @SuppressWarnings("UnusedDeclaration")
    public class
            CometCombineScoredScanWithScore extends AbstractLoggingFunction2<CometScoringResult, Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan>, CometScoringResult> {
        @Override
        public CometScoringResult doCall(final CometScoringResult v1, final Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan> v2) throws Exception {
            //noinspection UnnecessaryLocalVariable
            Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan> toScore = v2;
            CometScoredScan scoring = (CometScoredScan) toScore._2();
            ITheoreticalSpectrumSet ts = toScore._1();

            XTandemMain application = getApplication();
            Scorer scorer = application.getScoreRunner();
            double xcorr = doRealScoring(scoring, scorer, ts, application);

            IPolypeptide peptide = ts.getPeptide();
            String id = scoring.getId();


            SpectralMatch scan = new SpectralMatch(peptide, scoring, xcorr, xcorr, xcorr, scoring, null);
            //   scoring.addSpectralMatch(scan);

            if (TestUtilities.isCaseLogging()) {
                StringBuilder sb = new StringBuilder();
                double precursorMass = scoring.getPrecursorMass();

                double matchingMass = peptide.getMatchingMass();
                double del = precursorMass - matchingMass;

                //noinspection StringConcatenationInsideStringBufferAppend
                sb.append(scoring.getId() + "\t" + peptide + "\t" + precursorMass + "\t" + matchingMass + "\t" + del + "\t" + xcorr);
                TestUtilities.logCase(sb.toString());
            }

//            if (v1.getRaw() == null)
//                return scoring;
//            v1.addTo(scoring);
//            return v1;

//            CometScoringResult result = new CometScoringResult(scoring.getRaw());
//            result.addSpectralMatch(scan);
            if (!v1.isValid())
                v1.setRaw(scoring.getRaw());
            v1.addSpectralMatch(scan);
            return v1;

        }


    }


    /**
     * NOIE This class is REALLY important - ALL Comet scoring happens here
     */
    public class
            ScoreSpectrumAndPeptide extends AbstractLoggingFlatMapFunction<Tuple2<ITheoreticalSpectrumSet, CometScoredScan>, CometScoringResult> {
        @Override
        public Iterable<CometScoringResult> doCall(Tuple2<ITheoreticalSpectrumSet, CometScoredScan> toScore) throws Exception {
            List<CometScoringResult> ret = new ArrayList<CometScoringResult>();
            CometScoredScan scoring = toScore._2();
            ITheoreticalSpectrumSet ts = toScore._1();

            XTandemMain application = getApplication();
            Scorer scorer = application.getScoreRunner();
            double xcorr = doRealScoring(scoring, scorer, ts, application);

            IPolypeptide peptide = ts.getPeptide();

            if (TestUtilities.isInterestingPeptide(peptide))
                TestUtilities.breakHere();

            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
            String id = scoring.getId();


            SpectralMatch scan = new SpectralMatch(peptide, scoring, xcorr, xcorr, xcorr, scoring, null);

            scoring.addSpectralMatch(scan);

            if (TestUtilities.isCaseLogging()) {
                StringBuilder sb = new StringBuilder();
                double precursorMass = scoring.getPrecursorMass();

                double matchingMass = peptide.getMatchingMass();
                double del = precursorMass - matchingMass;

                //noinspection StringConcatenationInsideStringBufferAppend
                sb.append(scoring.getId() + "\t" + peptide + "\t" + precursorMass + "\t" + matchingMass + "\t" + del + "\t" + xcorr);
                TestUtilities.logCase(sb.toString());
            }


            CometScoringResult result = new CometScoringResult(scoring.getRaw());
            result.addSpectralMatch(scan);

            ret.add(result);
            return ret;
        }
    }

//    @Override
//    public CometScoredScan doCall(final Tuple2<ITheoreticalSpectrumSet, CometScoredScan> v2) throws Exception {
//        Tuple2<ITheoreticalSpectrumSet, CometScoredScan> toScore = v2;
//        CometScoredScan scoring = toScore._2();
//        ITheoreticalSpectrumSet ts = toScore._1();
//
//        XTandemMain application = getApplication();
//        Scorer scorer = application.getScoreRunner();
//        double xcorr = doRealScoring(scoring, scorer, ts, application);
//
//        IPolypeptide peptide = ts.getPeptide();
//        String id = scoring.getId();
//
//
//        SpectralMatch scan = new SpectralMatch(peptide, scoring, xcorr, xcorr, xcorr, scoring, null);
//        scoring.addSpectralMatch(scan);
//
//        if (TestUtilities.isCaseLogging()) {
//            StringBuilder sb = new StringBuilder();
//            double precursorMass = scoring.getPrecursorMass();
//
//            double matchingMass = peptide.getMatchingMass();
//            double del = precursorMass - matchingMass;
//
//            sb.append(scoring.getId() + "\t" + peptide + "\t" + precursorMass + "\t" + matchingMass + "\t" + del + "\t" + xcorr);
//            TestUtilities.logCase(sb.toString());
//        }
//
//        if (v1.getRaw() == null)
//            return scoring;
//        v1.addTo(scoring);
//        return v1;
//    }
//


    /**
     * for testing - these are usually wrapped before serialization
     *
     * @param spec
     * @param pp
     * @param application
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static double doRealScoring(IMeasuredSpectrum spec, IPolypeptide pp, XTandemMain application) {
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getScorer();
        Scorer scorer = application.getScoreRunner();
        CometTheoreticalBinnedSet ts = new CometTheoreticalBinnedSet(spec.getPrecursorCharge(), spec.getPrecursorMass(), pp, comet, scorer);
        CometScoredScan scan = new CometScoredScan(spec, comet);
        return doRealScoring(scan, scorer, ts, application);
    }

    /**
     * IMPORTANT the real word is done here
     *
     * @param pScoring
     * @param pTs
     * @param application
     * @return
     */
    @SuppressWarnings("JavaDoc")
    public static double doRealScoring(final CometScoredScan pScoring, final Scorer scorer, final ITheoreticalSpectrumSet pTs, XTandemMain application) {

        IPolypeptide peptide = pTs.getPeptide();
        IMeasuredSpectrum spec = pScoring.getConditionedScan();
        //====================================================
        // THIS IS ALL DEBUGGGING
        /*if (TestUtilities.isInterestingSpectrum(pScoring)) {
            breakHere();
        }
        if (TestUtilities.isInterestingPeptide(peptide)) {
            breakHere();
        }
        if (TestUtilities.isInterestingScoringPair(peptide, pScoring)) {
            breakHere();
            TestUtilities.setLogCalculations(application, true); // log this
        } else {
            String log = TestUtilities.setLogCalculations(application, false); // log off
            if (log != null)
                System.out.println(log);
        }*/
        //====================================================
        // END DEBUGGGING

        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getScorer();
        IonUseCounter counter = new IonUseCounter();
        List<XCorrUsedData> used = new ArrayList<XCorrUsedData>();

        //if (SparkUtilities.validateDesiredUse(spec, peptide, 0))
        //    breakHere(); // look at these cases

        // TODO Put it in constructor
        //pScoring.setAlgorithm(comet);

        //double mass = pScoring.getPrecursorMass();    // todo is this peptide or
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        //int MaxArraySize = comet.asBin(mass) + 100; // ((int) ((mass + 100) / getBinTolerance()); //  pScoring->_spectrumInfoInternal.iArraySize

//        comet.normalizeBinnedPeaks(MaxArraySize);
//        comet.normalizeForNL(MaxArraySize);

        //====================================================
        // THIS IS ALL DEBUGGGING
        //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
        //List<SpectrumBinnedScore> fastScoringData = pScoring.getFastScoringData();
        //List<SpectrumBinnedScore> fastScoringDataNL = pScoring.getNLScoringData();
        //====================================================
        // END DEBUGGGING

        double xcorr = comet.doXCorr((CometTheoreticalBinnedSet) pTs, scorer, counter, pScoring, used);

        //  SparkUtilities.validateDesiredUse(spec,peptide,xcorr) ;

        // pScoring.clearScoringData();

        return xcorr;
    }


    public JavaRDD<? extends IScoredScan> scoreCometBinPairs(final JavaPairRDD<BinChargeKey, Tuple2<ITheoreticalSpectrumSet, CometScoredScan>> binPairs, long[] countRef) {
        ElapsedTimer timer = new ElapsedTimer();
        XTandemMain application = getApplication();
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];

        // Old code where we first distribute by spectrum then score all peptides
        // this seems to unbalance the load so we will try scoring then mapping
        JavaPairRDD<String, Tuple2<ITheoreticalSpectrumSet, ? extends IScoredScan>> bySpectrumId =
                binPairs.flatMapToPair(new CometMapBinChargeTupleToSpectrumIDTuple(comet));

        if (false)      // use when you want a sample file with the largest spectrum and peptides to score against it
            bySpectrumId = TestUtilities.saveInterestingPairs(bySpectrumId);


        long[] counts = new long[1];
        bySpectrumId = SparkUtilities.persistAndCountPair("By SpectrumID: ", bySpectrumId, counts);
        //  bySpectrumId = SparkUtilities.persistAndCountPair("ScoredPairs", bySpectrumId, countRef);

        JavaPairRDD<String, ? extends IScoredScan> scores = bySpectrumId.aggregateByKey(
                new CometScoringResult(),
                new CometCombineScoredScanWithScore(),
                new CombineCometScoringResults()
        );
        return scores.values();
    }

    public JavaRDD<? extends IScoredScan> scoreCometBinPairList(final JavaPairRDD<BinChargeKey, Tuple2<CometScoredScan, ArrayList<IPolypeptide>>> binPairs) {
        ElapsedTimer timer = new ElapsedTimer();
        XTandemMain application = getApplication();
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getAlgorithms()[0];

        //  map to a pair dropping bins
        // this is all we need to score
        JavaPairRDD<CometScoredScan, ArrayList<IPolypeptide>> values = binPairs.values().flatMapToPair(new PairFlatMapFunction<Tuple2<CometScoredScan, ArrayList<IPolypeptide>>, CometScoredScan, ArrayList<IPolypeptide>>() {
            @Override
            public Iterable<Tuple2<CometScoredScan, ArrayList<IPolypeptide>>> call(Tuple2<CometScoredScan, ArrayList<IPolypeptide>> tp) throws Exception {
                ArrayList<Tuple2<CometScoredScan, ArrayList<IPolypeptide>>> holder = new ArrayList<Tuple2<CometScoredScan, ArrayList<IPolypeptide>>>();
                CometScoredScan spectrum = tp._1();
                ArrayList<IPolypeptide> polypeptides = tp._2();
                if (polypeptides.size() > 0)
                    holder.add(new Tuple2<CometScoredScan, ArrayList<IPolypeptide>>(spectrum, polypeptides));

                return holder;
            }
        });

        JavaRDD<? extends IScoredScan> scores = values.flatMap(new scoreSpectrumAndPeptideList(application));
        return scores;
    }


    @SuppressWarnings("UnusedDeclaration")
    public static class CometMapBinChargeTupleToSpectrumIDTuple<T extends IMeasuredSpectrum> extends AbstractLoggingPairFlatMapFunction<Tuple2<BinChargeKey, Tuple2<ITheoreticalSpectrumSet, T>>, String, Tuple2<ITheoreticalSpectrumSet, T>> {
        private final CometScoringAlgorithm comet;

        public CometMapBinChargeTupleToSpectrumIDTuple(final CometScoringAlgorithm pComet) {
            comet = pComet;
        }


        @Override
        public Iterable<Tuple2<String, Tuple2<ITheoreticalSpectrumSet, T>>> doCall(final Tuple2<BinChargeKey, Tuple2<ITheoreticalSpectrumSet, T>> t) throws Exception {
            List<Tuple2<String, Tuple2<ITheoreticalSpectrumSet, T>>> holder = new ArrayList<Tuple2<String, Tuple2<ITheoreticalSpectrumSet, T>>>();

            Tuple2<ITheoreticalSpectrumSet, T> pair = t._2();
            IMeasuredSpectrum spec = pair._2();
            IPolypeptide pp = pair._1().getPeptide();

            // if we dont score give up
            boolean pairScored = comet.isPairScored(spec, pp);
            if (!pairScored) {

                if (TestUtilities.isInterestingPeptide(pp))
                    //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
                    pairScored = comet.isPairScored(spec, pp); // repeat and look
                if (TestUtilities.isInterestingSpectrum(spec))
                    //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
                    pairScored = comet.isPairScored(spec, pp);   // repeat and look


                return holder;
            }

            String id = spec.getId();
            holder.add(new Tuple2<String, Tuple2<ITheoreticalSpectrumSet, T>>(id, pair));
            return holder;

        }


    }


}
