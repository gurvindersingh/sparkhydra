package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.fragment.*;
import com.lordjoe.distributed.hydra.scoring.*;
import com.lordjoe.distributed.hydra.test.*;
import com.lordjoe.utilities.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.comet.*;
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
            if(s2.getRaw() == null)
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

            IPolypeptide peptide = ts.getPeptide();
            IMeasuredSpectrum spec = scoring.getConditionedScan();
            //====================================================
            // THIS IS ALL DEBUGGGING
            if (TestUtilities.isInterestingSpectrum(scoring)) {
                TestUtilities.breakHere();
            }
            if (TestUtilities.isInterestingPeptide(peptide)) {
                TestUtilities.breakHere();
            }

            if (TestUtilities.isInterestingScoringPair(peptide, scoring)) {
                TestUtilities.breakHere();
                TestUtilities.setLogCalculations(getApplication(), true); // log this
            }
            else {
                String log = TestUtilities.setLogCalculations(getApplication(), false); // log off
                if (log != null)
                    System.out.println(log);
            }
            //====================================================
            // END DEBUGGGING

            Scorer scoreRunner = getApplication().getScoreRunner();
            CometScoringAlgorithm comet = (CometScoringAlgorithm) getApplication().getScorer();
            IonUseCounter counter = new IonUseCounter();
            List<XCorrUsedData> used = new ArrayList<XCorrUsedData>();

            if( SparkUtilities.validateDesiredUse(spec,peptide,0) )
                TestUtilities.breakHere(); // look at these cases

            scoring.setAlgorithm(comet);

            double mass = scoring.getPrecursorMass();    // todo is this peptide or
            int MaxArraySize = comet.asBin(mass) + 100; // ((int) ((mass + 100) / getBinTolerance()); //  pScoring->_spectrumInfoInternal.iArraySize

             comet.normalizeBinnedPeaks(MaxArraySize);
             comet.normalizeForNL(MaxArraySize);


            float[] specPeaks = comet.getScoringFastXcorrData();
            float[] specPeaksNL = comet.getFastXcorrDataNL();
             List<SpectrinBinnedScore> scoredPeaksPlain =  TestUtilities.activeValues(specPeaks);
             List<SpectrinBinnedScore> scoredPeaksNL =  TestUtilities.activeValues(specPeaksNL);

            double xcorr = comet.doXCorr((CometTheoreticalBinnedSet) ts, counter, scoring, used);

            SparkUtilities.validateDesiredUse(spec,peptide,xcorr) ;


            if (v1.getRaw() == null)
                return scoring;
            v1.addTo(scoring);
            return v1;
        }


    }


    public JavaRDD<CometScoredScan> scoreCometBinPairs(final JavaPairRDD<BinChargeKey, Tuple2<ITheoreticalSpectrumSet, CometScoredScan>> binPairs) {
        ElapsedTimer timer = new ElapsedTimer();

        JavaPairRDD<String, Tuple2<ITheoreticalSpectrumSet, CometScoredScan>> bySpectrumId =
                binPairs.mapToPair(new MapBinChargeTupleToSpectrumIDTuple());

          JavaPairRDD<String, CometScoredScan> scores = bySpectrumId.aggregateByKey(
                   new CometScoredScan(),
                  new CometCombineScoredScanWithScore(),

                   new CometCombineScoredScans()
           );
           //     JavaRDD<IScoredScan> scores = bySpectrumId.mapPartitions(new ScoreSpectrumAgainstAllPeptides());


        timer.showElapsed("built score by ids");

        return scores.values();
    }
}
