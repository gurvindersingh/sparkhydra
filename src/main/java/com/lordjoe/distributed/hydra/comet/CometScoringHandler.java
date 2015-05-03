package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.fragment.*;
import com.lordjoe.distributed.hydra.scoring.*;
import com.lordjoe.distributed.hydra.test.*;
import com.lordjoe.utilities.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringHandler
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
            if(s1.getRaw() == null)
                return s2;
            if(s1.getBestMatch() == null)
                 return s2;
             if(s2.getRaw() == null)
                return s1;
            if(s2.getBestMatch() == null)
                  return s1;
            if(!s1.getId().equals(s2.getId()))
                throw new IllegalStateException("Attempting to combine " + s1.getId() + " and " + s2.getId());
            s1.addTo(s2);
            return s1;
        }
    }

    /**
     * NOIE This class is REALLY important - ALL Comet scoring happens here
     */
    public class
            CometCombineScoredScanWithScore extends AbstractLoggingFunction2<CometScoredScan, Tuple2<ITheoreticalSpectrumSet, CometScoredScan>, CometScoredScan> {
        @Override
        public CometScoredScan doCall(final CometScoredScan v1, final Tuple2<ITheoreticalSpectrumSet, CometScoredScan> v2) throws Exception {
            Tuple2<ITheoreticalSpectrumSet, CometScoredScan> toScore = v2;
            CometScoredScan scoring = toScore._2();
            ITheoreticalSpectrumSet ts = toScore._1();

            XTandemMain application = getApplication();
            Scorer scorer = application.getScoreRunner();
             double xcorr = doRealScoring(scoring,scorer, ts, application);

            IPolypeptide peptide = ts.getPeptide();
            String id = scoring.getId();


            SpectralMatch scan = new SpectralMatch(peptide, scoring, xcorr, xcorr, xcorr, scoring, null);
            scoring.addSpectralMatch(scan);

            if(TestUtilities.isCaseLogging())  {
                  StringBuilder sb = new StringBuilder();
                double precursorMass = scoring.getPrecursorMass();

                double matchingMass = peptide.getMatchingMass();
                double del = precursorMass - matchingMass;

                sb.append(scoring.getId() + "\t" + peptide + "\t"  + precursorMass + "\t" + matchingMass + "\t" + del + "\t" + xcorr );
                 TestUtilities.logCase(sb.toString());
              }

            if (v1.getRaw() == null)
                return scoring;
            v1.addTo(scoring);
            return v1;
        }


    }

    /**
     * for testing - these are usually wrapped before serialization
     * @param spec
     * @param pp
     * @param application
     * @return
     */
    public static double doRealScoring(IMeasuredSpectrum spec, IPolypeptide pp, XTandemMain application ) {
        CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getScorer();
        Scorer scorer = application.getScoreRunner();
        CometTheoreticalBinnedSet ts = new  CometTheoreticalBinnedSet(spec.getPrecursorCharge(),spec.getPrecursorMass(),pp,comet,scorer);
        CometScoredScan scan = new CometScoredScan(spec);
        return  doRealScoring(scan,scorer, ts,  application );
    }

    /**
     * IMPORTANT the real word is done here
     * @param pScoring
     * @param pTs
     * @param application
     * @return
     */
    public static double doRealScoring(final CometScoredScan pScoring,final Scorer scorer, final ITheoreticalSpectrumSet pTs, XTandemMain application ) {
        IPolypeptide peptide = pTs.getPeptide();
        IMeasuredSpectrum spec = pScoring.getConditionedScan();
        //====================================================
        // THIS IS ALL DEBUGGGING
        if (TestUtilities.isInterestingSpectrum(pScoring)) {
            TestUtilities.breakHere();
        }
        if (TestUtilities.isInterestingPeptide(peptide)) {
            TestUtilities.breakHere();
        }
         if (TestUtilities.isInterestingScoringPair(peptide, pScoring)) {
            TestUtilities.breakHere();
            TestUtilities.setLogCalculations(application, true); // log this
        }
        else {
            String log = TestUtilities.setLogCalculations(application, false); // log off
            if (log != null)
                System.out.println(log);
        }
        //====================================================
        // END DEBUGGGING

          CometScoringAlgorithm comet = (CometScoringAlgorithm) application.getScorer();
        IonUseCounter counter = new IonUseCounter();
        List<XCorrUsedData> used = new ArrayList<XCorrUsedData>();

        if( SparkUtilities.validateDesiredUse(spec, peptide, 0) )
            TestUtilities.breakHere(); // look at these cases

        pScoring.setAlgorithm(comet);

        double mass = pScoring.getPrecursorMass();    // todo is this peptide or
        int MaxArraySize = comet.asBin(mass) + 100; // ((int) ((mass + 100) / getBinTolerance()); //  pScoring->_spectrumInfoInternal.iArraySize

//        comet.normalizeBinnedPeaks(MaxArraySize);
//        comet.normalizeForNL(MaxArraySize);

           //====================================================
        // THIS IS ALL DEBUGGGING
        List<SpectrumBinnedScore> fastScoringData = pScoring.getFastScoringData();
        List<SpectrumBinnedScore> fastScoringDataNL = pScoring.getNLScoringData();
        //====================================================
        // END DEBUGGGING

        double xcorr = comet.doXCorr((CometTheoreticalBinnedSet) pTs,scorer, counter, pScoring, used);

      //  SparkUtilities.validateDesiredUse(spec,peptide,xcorr) ;


        return xcorr;
    }


    public JavaRDD<CometScoredScan> scoreCometBinPairs(final JavaPairRDD<BinChargeKey, Tuple2<ITheoreticalSpectrumSet, CometScoredScan>> binPairs,long[] countRef) {
        ElapsedTimer timer = new ElapsedTimer();
        XTandemMain application = getApplication();
        CometScoringAlgorithm comet = (CometScoringAlgorithm)application.getAlgorithms()[0];

        JavaPairRDD<String, Tuple2<ITheoreticalSpectrumSet, CometScoredScan>> bySpectrumId =
                binPairs.flatMapToPair(new CometMapBinChargeTupleToSpectrumIDTuple(comet));


      //  bySpectrumId = SparkUtilities.persistAndCountPair("ScoredPairs", bySpectrumId, countRef);

          JavaPairRDD<String, CometScoredScan> scores = bySpectrumId.aggregateByKey(
                  new CometScoredScan(),
                  new CometCombineScoredScanWithScore(),
                  new CometCombineScoredScans()
           );
           //     JavaRDD<IScoredScan> scores = bySpectrumId.mapPartitions(new ScoreSpectrumAgainstAllPeptides());


        timer.showElapsed("built score by ids");

        return scores.values();
    }

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
            if(!pairScored) {

                if(TestUtilities.isInterestingPeptide(pp))
                    pairScored = comet.isPairScored(spec, pp); // repeat and look
                if(TestUtilities.isInterestingSpectrum(spec))
                     pairScored = comet.isPairScored(spec, pp);   // repeat and look


                return holder;
            }

            String id = spec.getId();
            holder.add(new Tuple2<String, Tuple2<ITheoreticalSpectrumSet, T>>(id, pair));
            return holder;

        }


    }


}
