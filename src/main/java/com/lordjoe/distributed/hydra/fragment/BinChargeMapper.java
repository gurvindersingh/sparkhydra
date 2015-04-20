package com.lordjoe.distributed.hydra.fragment;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.scoring.*;
import com.lordjoe.distributed.hydra.test.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.xtandem.*;
import com.lordjoe.distributed.hydra.comet.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;

import java.io.Serializable;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.fragment.BinChargeMapper
 * User: Steve
 * Date: 10/31/2014
 */
public class BinChargeMapper implements Serializable {
    // how wide are the bins
    public static final double binSize = BinChargeKey.QUANTIZATION;
    // how wide to we search
    public static final double examineWidth = binSize * 7;
    public static final int MAX_CHARGE_PLUS_ONE = 5;


    private final XTandemMain application;
    //   private final Scorer scorer;
//    private final IScoringAlgorithm algorithm;


    public BinChargeMapper(SparkMapReduceScoringHandler pHandler) {
        this(pHandler.getApplication());
    }

    public BinChargeMapper(XTandemMain app) {
        application = app;
        //      scorer = application.getScoreRunner();
        //      algorithm = application.getAlgorithms()[0];
    }

    public <T extends IMeasuredSpectrum> JavaPairRDD<BinChargeKey, T> mapMeasuredSpectrumToKeys(JavaRDD<T> inp) {
        return inp.flatMapToPair(new mapMeasuredSpectraToBins());
    }

    public <T extends IMeasuredSpectrum> JavaPairRDD<BinChargeKey, Tuple2<BinChargeKey, T>> mapMeasuredSpectrumToKeySpectrumPair(JavaRDD<T> inp) {
        inp = SparkUtilities.repartitionIfNeeded(inp);
        return inp.flatMapToPair(new mapMeasuredSpectraToBinTuples());
    }


    public JavaPairRDD<BinChargeKey, ITheoreticalSpectrumSet> mapFragmentsToTheoreticalSets(JavaRDD<IPolypeptide> inp) {
        return inp.flatMapToPair(new mapPolypeptidesToTheoreticalBins(application));
    }

    public JavaPairRDD<BinChargeKey, IPolypeptide> mapFragmentsToKeys(JavaRDD<IPolypeptide> inp) {
        return inp.flatMapToPair(new mapPolypeptidesToBins());
    }

    public BinChargeKey[] keysFromChargeMz(int charge, double mz) {
        List<BinChargeKey> holder = new ArrayList<BinChargeKey>();
        double startMZ = mz - examineWidth;
        int start = BinChargeKey.mzAsInt(startMZ);
        while (BinChargeKey.intToMz(start) < mz + examineWidth) {
            holder.add(new BinChargeKey(charge, BinChargeKey.intToMz(start++)));
        }
//        double mzStart = ((int) (0.5 + ((mz - examineWidth) / binSize))) * binSize;
//        for (int i = 0; i < examineWidth / binSize; i++) {
//            double quantizedMz = (mzStart + i) * binSize;
//            holder.add(new BinChargeKey(charge, quantizedMz)); // todo add meighbors
//
//        }


        BinChargeKey[] ret = new BinChargeKey[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * create one key from change and MZ
     *
     * @param charge
     * @param mz
     * @return
     */
    static public BinChargeKey oneKeyFromChargeMz(int charge, double mz) {
//        List<BinChargeKey> holder = new ArrayList<BinChargeKey>();
//        double v = (mz) / binSize;
//        double mzStart = ((int) ( 0.5 +  v)   * binSize);
//        double quantizedMz = mzStart ;
        BinChargeKey ret = new BinChargeKey(charge, mz);
        double mzx = ret.getMz();
        if (Math.abs(mz - mzx) > binSize)
            throw new IllegalStateException("bad bin key");
        return ret;
    }

    /**
     * peptides are only mapped once whereas spectra map to multiple  bins
     */
    public static class mapPolypeptidesToBins extends AbstractLoggingPairFlatMapFunction<IPolypeptide, BinChargeKey, IPolypeptide> {
        @Override
        public Iterable<Tuple2<BinChargeKey, IPolypeptide>> doCall(final IPolypeptide pp) throws Exception {
            double matchingMass = pp.getMatchingMass();

            if (TestUtilities.isInterestingPeptide(pp))
                TestUtilities.breakHere();

            List<Tuple2<BinChargeKey, IPolypeptide>> holder = new ArrayList<Tuple2<BinChargeKey, IPolypeptide>>();
            for (int charge = 1; charge <= Scorer.MAX_CHARGE; charge++) {
                BinChargeKey key = oneKeyFromChargeMz(charge, matchingMass / charge);
                holder.add(new Tuple2<BinChargeKey, IPolypeptide>(key, pp));
            }
            if (holder.isEmpty())
                throw new IllegalStateException("problem"); // ToDo change

            if (TestUtilities.isInterestingPeptide(pp)) {
                TestUtilities.savePeptideKey(holder);
            }
            return holder;
        }
    }

    /**
     * peptides are only mapped once whereas spectra map to multiple  bins
     */
    public static class mapPolypeptidesToTheoreticalBins extends AbstractLoggingPairFlatMapFunction<IPolypeptide, BinChargeKey, ITheoreticalSpectrumSet> {

        private final XTandemMain application;

        public mapPolypeptidesToTheoreticalBins(final XTandemMain pApplication) {
            application = pApplication;
        }

        @Override
        public Iterable<Tuple2<BinChargeKey, ITheoreticalSpectrumSet>> doCall(final IPolypeptide pp) throws Exception {
            //    double matchingMass = pp.getMatchingMass();
            double matchingMass = CometScoringAlgorithm.getCometMetchingMass(pp);

            if (TestUtilities.isInterestingPeptide(pp))
                TestUtilities.breakHere();

            Scorer scorer = application.getScoreRunner();

            List<Tuple2<BinChargeKey, ITheoreticalSpectrumSet>> holder = new ArrayList<Tuple2<BinChargeKey, ITheoreticalSpectrumSet>>();
            BinChargeKey key = oneKeyFromChargeMz(1, matchingMass);
            ITheoreticalSpectrumSet ts = scorer.generateSpectrum(pp);

            holder.add(new Tuple2<BinChargeKey, ITheoreticalSpectrumSet>(key, ts));
            return holder;
        }
    }

    private class mapMeasuredSpectraToBins<T extends IMeasuredSpectrum> extends AbstractLoggingPairFlatMapFunction<T, BinChargeKey, T> {
        @Override
        public Iterable<Tuple2<BinChargeKey, T>> doCall(final T spec) throws Exception {
            int charge = spec.getPrecursorCharge();
            charge = 1; // all peptides use 1 now

            if(TestUtilities.isInterestingSpectrum(spec))
                TestUtilities.breakHere();

            List<Tuple2<BinChargeKey, T>> holder = new ArrayList<Tuple2<BinChargeKey, T>>();

            // code using MZ
            //   double specMZ = spec.getPrecursorMassChargeRatio();
            //   BinChargeKey[] keys = keysFromChargeMz(charge, specMZ);

            // code using MZ
            double matchingMass = spec.getPrecursorMass();   // todo decide whether mass or mz is better
            BinChargeKey[] keys = keysFromChargeMz(charge, matchingMass);

            for (int i = 0; i < keys.length; i++) {
                BinChargeKey key = keys[i];
                holder.add(new Tuple2<BinChargeKey, T>(key, spec));
            }
            if (holder.isEmpty())
                throw new IllegalStateException("problem"); // ToDo change

            if (TestUtilities.isInterestingSpectrum(spec)) {
                TestUtilities.saveSpectrumKey(holder);
            }

            return holder;
        }
    }

    private class mapMeasuredSpectraToBinTuples<T extends IMeasuredSpectrum> extends AbstractLoggingPairFlatMapFunction<T, BinChargeKey, Tuple2<BinChargeKey, T>> {
        /**
         * do work here
         *
         * @param t@return
         */
        @Override
        public Iterable<Tuple2<BinChargeKey, Tuple2<BinChargeKey, T>>> doCall(final T spec) throws Exception {
            double matchingMass = spec.getPrecursorMass();
            int charge = spec.getPrecursorCharge();
            List<Tuple2<BinChargeKey, Tuple2<BinChargeKey, T>>> holder = new ArrayList<Tuple2<BinChargeKey, Tuple2<BinChargeKey, T>>>();
            BinChargeKey[] keys = keysFromChargeMz(charge, matchingMass);
            for (int i = 0; i < keys.length; i++) {
                BinChargeKey key = keys[i];
                holder.add(new Tuple2<BinChargeKey, Tuple2<BinChargeKey, T>>(key, new Tuple2<BinChargeKey, T>(key, spec)));
            }
            if (holder.isEmpty())
                throw new IllegalStateException("problem"); // ToDo change

            return holder;
        }
    }
}
