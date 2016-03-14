package com.lordjoe.distributed.hydra.comet_spark;

import com.lordjoe.algorithms.CountedDistribution;
import com.lordjoe.distributed.hydra.SparkXTandemMain;
import com.lordjoe.distributed.hydra.comet.*;
import com.lordjoe.distributed.hydra.fragment.BinChargeKey;
import com.lordjoe.distributed.spark.accumulators.*;
import com.lordjoe.testing.MemoryTracker;
import org.apache.spark.Accumulator;
import org.systemsbiology.xtandem.IMeasuredSpectrum;
import org.systemsbiology.xtandem.XTandemMain;
import org.systemsbiology.xtandem.ionization.IonUseCounter;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.scoring.IScoredScan;
import org.systemsbiology.xtandem.scoring.LowMemoryIdOnlySpectrum;
import org.systemsbiology.xtandem.scoring.Scorer;
import org.systemsbiology.xtandem.scoring.SpectralMatch;
import scala.Tuple2;

import java.util.ArrayList;
import java.util.List;

/**
 * NOIE This class is REALLY important - ALL Comet with peptide lists scoring happens here
 * WE CURRENTLY USE THIS CLASS
 */
public class ScoreSpectrumAndPeptide extends AbstractLoggingFlatMapFunction<Tuple2<IPolypeptide, CometScoredScan>, IScoredScan> {

    public static final int MAX_BINS_TO_SCORE = 500;
    public static final int MAX_PEPTIDES_TO_SCORE = 5;

    private CometScoringAlgorithm comet;
    private Scorer scorer;


    public ScoreSpectrumAndPeptide(XTandemMain application) {
        comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        scorer = application.getScoreRunner();
    }


    @Override
    public Iterable<IScoredScan> doCall(Tuple2<IPolypeptide, CometScoredScan> inp) throws Exception {

        MemoryUseAccumulator acc = MemoryUseAccumulator.empty();
        //    CometTemporaryMemoryAccumulator tempAcc = CometTemporaryMemoryAccumulator.empty();
        MemoryUseAccumulatorAndBinSize binAcc = MemoryUseAccumulatorAndBinSize.empty();

        IPolypeptide peptide = inp._1();
        CometScoredScan scan = inp._2();
        List<IScoredScan> ret = new ArrayList<IScoredScan>();


        CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(peptide);
        IonUseCounter counter = new IonUseCounter();
        double xcorr = comet.doXCorr(ts, scorer, counter, scan, null);
        if (xcorr > CometScoringHandler.MINIMUM_ACCEPTABLE_SCORE) {
            IMeasuredSpectrum raw = new LowMemoryIdOnlySpectrum(scan.getRaw());
            SpectralMatch spectralMatch = new SpectralMatch(ts.getPeptide(), raw, xcorr, xcorr, xcorr, scan, null);
            CometScoringResult res = new CometScoringResult(raw);
            res.addSpectralMatch(spectralMatch);
            if (res.isValidMatch())
                ret.add(res);
        }
        return ret;
    }


}
