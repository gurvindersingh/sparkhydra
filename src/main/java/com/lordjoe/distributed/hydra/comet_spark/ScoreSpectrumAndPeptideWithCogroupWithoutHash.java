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
public class ScoreSpectrumAndPeptideWithCogroupWithoutHash extends AbstractLoggingFlatMapFunction<Tuple2<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<IPolypeptide>>>, IScoredScan> {

    public static final int MAX_BINS_TO_SCORE = 500;
    public static final int MAX_PEPTIDES_TO_SCORE = 5;

    private CometScoringAlgorithm comet;
    private Scorer scorer;
    private Accumulator<MemoryUseAccumulator> memoryAccululator;
    private final boolean bypassScoring;
    private final boolean doGCAfterBin;
    private final boolean keepBinStatistics;
    private final long maxMemoryAllocartion;

    private final int maxBinSize;

    private final Accumulator<Long> numberScoredAccumlator = AccumulatorUtilities.getInstance().createAccumulator(CometScoringHandler.TOTAL_SCORRED_ACCUMULATOR_NAME);
    private final Accumulator<Long> numberSpectraAccumlator = AccumulatorUtilities.getInstance().createAccumulator(CometScoringHandler.TOTAL_SCORED_SPECTRA_NAME);


    private final Accumulator<CountedDistribution> peptideDistributionCounts = AccumulatorUtilities.getInstance().createSpecialAccumulator(CometScoringHandler.PEPTIDES_ACCUMULATOR_NAME,
            CountedDistribution.PARAM_INSTANCE, CountedDistribution.empty());
    private final Accumulator<CountedDistribution> spectrumDistributionCounts = AccumulatorUtilities.getInstance().createSpecialAccumulator(CometScoringHandler.SPECTRA_ACCUMULATOR_NAME,
            CountedDistribution.PARAM_INSTANCE, CountedDistribution.empty());
    private final Accumulator<MemoryUseAccumulatorAndBinSize> binAccululator = AccumulatorUtilities.getInstance().createSpecialAccumulator(MemoryUseAccumulatorAndBinSize.BIN_ACCUMULATOR_NAME,
            MemoryUseAccumulatorAndBinSize.PARAM_INSTANCE, MemoryUseAccumulatorAndBinSize.empty());
    // track special comet memory
//        private final Accumulator<CometTemporaryMemoryAccumulator> temporaryMemoryAccululator = SparkAccumulators.createSpecialAccumulator(CometTemporaryMemoryAccumulator.COMET_MEMORY_ACCUMULATOR_NAME,
//                CometTemporaryMemoryAccumulator.PARAM_INSTANCE, CometTemporaryMemoryAccumulator.empty());
    private final Accumulator<NotScoredBins> unscoredAccumulator = AccumulatorUtilities.getInstance().createSpecialAccumulator(NotScoredBins.BIN_ACCUMULATOR_NAME,
            NotScoredBins.PARAM_INSTANCE, NotScoredBins.empty());
    private final Accumulator<LogRareEventsAccumulator> rareEvents =
            AccumulatorUtilities.getInstance().getSpecialAccumulator(LogRareEventsAccumulator.LOG_RARE_EVENTS_NAME);


    public ScoreSpectrumAndPeptideWithCogroupWithoutHash(XTandemMain application) {
        comet = (CometScoringAlgorithm) application.getAlgorithms()[0];
        scorer = application.getScoreRunner();
        ISparkAccumulators instance = AccumulatorUtilities.getInstance();
        memoryAccululator = (Accumulator<MemoryUseAccumulator>) instance.getSpecialAccumulator(SparkAccumulators.MEMORY_ACCUMULATOR_NAME);


        keepBinStatistics = application.getBooleanParameter(SparkXTandemMain.KEEP__BIN_STATISTICS_PROPERTY, false);
        doGCAfterBin = application.getBooleanParameter(SparkXTandemMain.DO_GC_AFTER_BIN, false);
        maxBinSize = application.getIntParameter(SparkXTandemMain.MAX_BIN_SIZE_PROPERTY, Integer.MAX_VALUE);
        maxMemoryAllocartion = application.getLongParameter(SparkXTandemMain.MAX_MEMORY_ALLOCATION_PROPERTY, Long.MAX_VALUE);

        // purely debigging
        bypassScoring = false; // application.getBooleanParameter(SparkXTandemMain.BYPASS_SCORING_PROPERTY, false);

    }




    @Override
    public Iterable<IScoredScan> doCall(Tuple2<BinChargeKey, Tuple2<Iterable<CometScoredScan>, Iterable<IPolypeptide>>> inp) throws Exception {

        MemoryUseAccumulator acc = MemoryUseAccumulator.empty();
        //    CometTemporaryMemoryAccumulator tempAcc = CometTemporaryMemoryAccumulator.empty();
        MemoryUseAccumulatorAndBinSize binAcc = MemoryUseAccumulatorAndBinSize.empty();


        List<IScoredScan> ret = new ArrayList<IScoredScan>();
        BinChargeKey binChargeKey = inp._1();
        Iterable<CometScoredScan> scans = inp._2()._1();
        Iterable<IPolypeptide> peptides = inp._2()._2();

//            Map<CometScoredScan, IScoredScan> spectrumToScore = new HashMap<CometScoredScan, IScoredScan>();
//            for (CometScoredScan scan : scans) {
//                spectrumToScore.put(scan, new CometScoringResult(scan.getRaw()));
//            }
        int numberpeptides = 0;
        for (IPolypeptide peptide : peptides) {
            numberpeptides++;
        }

        boolean firstPass = true;

        long numberSpectra = 0;
        for (IScoredScan peptide : scans) {
            numberSpectra++;
        }
        numberSpectraAccumlator.add(numberSpectra);
        long numberScored = 0;

        boolean memoryLimitHit = isMemoryLimitHit();

        // just count
        if (memoryLimitHit || bypassScoring) {    // for debugging and performance we can bypass scoring
            numberpeptides = registerScoringSkipped(scans, peptides, numberpeptides);
        }
        else {
            try {
                for (IPolypeptide peptide : peptides) {
                    memoryLimitHit = isMemoryLimitHit();
                    if (memoryLimitHit) {
                        registerScoringSkipped(scans, peptides, numberpeptides);
                        continue;
                    }
                    CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) scorer.generateSpectrum(peptide);
                    if (CometScoringHandler.isScanScoredByAnySpectrum(ts, scans, scorer)) {
                        for (CometScoredScan scan : scans) {
                            if (!scorer.isTheoreticalSpectrumScored(scan, ts))
                                continue;
                            if (isMemoryLimitHit())
                                continue;
                            if (!scan.isInitialized())
                                scan.setAlgorithm(comet);
                            if (isMemoryLimitHit())
                                continue;
                            if (firstPass) {                // actually scored
                                numberSpectraAccumlator.add(1L);
                            }
                            IonUseCounter counter = new IonUseCounter();
                            double xcorr = comet.doXCorr(ts, scorer, counter, scan, null);
                            numberScored++;
                            if (xcorr > CometScoringHandler.MINIMUM_ACCEPTABLE_SCORE) {
                                IMeasuredSpectrum raw = new LowMemoryIdOnlySpectrum(scan.getRaw());
                                SpectralMatch spectralMatch = new SpectralMatch(ts.getPeptide(), raw, xcorr, xcorr, xcorr, scan, null);
                                CometScoringResult res = new CometScoringResult(raw);
                                res.addSpectralMatch(spectralMatch);
                                if (res.isValidMatch())
                                    ret.add(res);
                            }
                            if (keepBinStatistics)
                                binAcc.check();
                            acc.check(); // record memory use
                        }
                        firstPass = false;  // quit counting spectra - we run through them many times
                    }
                    ts = null; // please garbage collect
                }
            }
            catch (OutOfMemoryError e) {
                System.gc();
                String msg = "Number Spectra = "  + numberSpectra + " number peptides = " + numberpeptides;
                rareEvents.add(new LogRareEventsAccumulator(msg));

            }

//            for (IScoredScan scan : result) {
//                if(scan.isValidMatch())
//                    ret.add(scan);
//            }

        }

         numberScoredAccumlator.add(numberScored);

        if (keepBinStatistics && numberpeptides > 0) {
            binAcc.saveUsage((int) numberSpectra, numberpeptides);
            binAccululator.add(binAcc);
        }
        acc.saveBins(); // get max use
        memoryAccululator.add(acc);

        //           tempAcc.check();
//          temporaryMemoryAccululator.add(tempAcc);

//            peptideDistributionCounts.add(new CountedDistribution(numberpeptides));
//            spectrumDistributionCounts.add(new CountedDistribution(numberSpectra));
        if (doGCAfterBin)
            System.gc(); // try to clean up

        return ret;
    }

    public int registerScoringSkipped(final Iterable<CometScoredScan> pScans, final Iterable<IPolypeptide> pPeptides, int pNumberpeptides) {
        NotScoredBins term = NotScoredBins.empty();
        int notScored = 0;
        for (IPolypeptide peptide : pPeptides) {
            pNumberpeptides++;
        }
        for (CometScoredScan scan : pScans) {
            notScored++;
        }
        long heap = MemoryTracker.usedBytes();
        NotScoredBins.NotScoredStatistics notScoredStatistics = new NotScoredBins.NotScoredStatistics(heap, notScored, notScored, pNumberpeptides);
        term.add(notScoredStatistics);
        unscoredAccumulator.add(term);
        return pNumberpeptides;
    }

    /**
     * if true we are using more than the maximum memory allocation
     *
     * @return
     */
    protected boolean isMemoryLimitHit() {
        if (maxMemoryAllocartion == Long.MAX_VALUE)
            return false; // not checking
        long current = MemoryTracker.usedBytes();
        return current > maxMemoryAllocartion;

    }

    private int scoreScansAgainstPeptideSet(Iterable<CometScoredScan> scans, CometTheoreticalBinnedSet ts, List<IScoredScan> results) {
        int numberScored = 0;

        //  CometScoringData scoringData = CometScoringData.getScoringData() ;

        // This section popul;ates temporary data with the spectrum
        // a lot os free space used temporarily
        for (CometScoredScan scan : scans) {
//                int numberGood = 0;
//                 // debugging why do we disagree
//                List<CometTheoreticalBinnedSet> badScore = new ArrayList<CometTheoreticalBinnedSet>();
//                List<CometTheoreticalBinnedSet> notScored = new ArrayList<CometTheoreticalBinnedSet>();
//                List<IPolypeptide> scoredPeptides = new ArrayList<IPolypeptide>();

            //    CometScoringData.populateFromScan(scan);

            // use pregenerated peptide data but not peptide data

            if (!scorer.isTheoreticalSpectrumScored(scan, ts))
                continue;

//                    if(TestUtilities.isInterestingPeptide(ts.getPeptide()))
//                        TestUtilities.breakHere();

            IonUseCounter counter = new IonUseCounter();
            double xcorr = comet.doXCorr(ts, scorer, counter, scan, null);
            numberScored++;
            if (xcorr > CometScoringHandler.MINIMUM_ACCEPTABLE_SCORE) {
                IPolypeptide peptide = ts.getPeptide();
                SpectralMatch spectralMatch = new SpectralMatch(peptide, scan.getRaw(), xcorr, xcorr, xcorr, scan, null);
                CometScoringResult res = new CometScoringResult(scan.getRaw());
                res.addSpectralMatch(spectralMatch);
                if (res.isValidMatch())
                    results.add(res);
            }

        }
        return numberScored;
    }
}
