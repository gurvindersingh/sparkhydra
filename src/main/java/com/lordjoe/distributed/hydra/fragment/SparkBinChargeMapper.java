package com.lordjoe.distributed.hydra.fragment;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.protein.*;
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
public class SparkBinChargeMapper implements Serializable {


    private final XTandemMain application;
    //   private final Scorer scorer;
//    private final IScoringAlgorithm algorithm;


    public SparkBinChargeMapper(SparkMapReduceScoringHandler pHandler) {
        this(pHandler.getApplication());
    }

    public SparkBinChargeMapper(XTandemMain app) {
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

    /**
     * return a list of all peptides in a bin as ITheoreticalSpectrumSet
     *
     * @param inp
     * @return
     */
    public JavaPairRDD<BinChargeKey, ArrayList<ITheoreticalSpectrumSet>> mapFragmentsToTheoreticalList(JavaRDD<IPolypeptide> inp) {
        JavaPairRDD<BinChargeKey, ITheoreticalSpectrumSet> ppBins = mapFragmentsToTheoreticalSets(inp);
        return SparkUtilities.mapToKeyedList(ppBins);

    }

    /**
     * return a list of all peptides in a bin
     *
     * @param inp
     * @return
     */
    public JavaPairRDD<BinChargeKey, ArrayList<IPolypeptide>> mapFragmentsToBinList(JavaRDD<IPolypeptide> inp, final Set<Integer> usedBins) {
        JavaPairRDD<BinChargeKey, IPolypeptide> ppBins = inp.flatMapToPair(new mapPolypeptidesToBin(application, usedBins));
        return SparkUtilities.mapToKeyedList(ppBins);
    }

    /**
     * return a list of all peptides in a bin
     *
     * @param inp
     * @return
     */
    public JavaPairRDD<BinChargeKey, HashMap<String, IPolypeptide>> mapFragmentsToBinHash(JavaRDD<IPolypeptide> inp, final Set<Integer> usedBins) {
        return mapFragmentsToBinHash(inp, usedBins, Integer.MAX_VALUE);
    }

    /**
     * return a list of all peptides in a bin
     *
     * @param inp
     * @return
     */
    public JavaPairRDD<BinChargeKey, HashMap<String, IPolypeptide>> mapFragmentsToBinHash(JavaRDD<IPolypeptide> inp, final Set<Integer> usedBins, int maxSize) {
        JavaPairRDD<BinChargeKey, IPolypeptide> ppBins = inp.flatMapToPair(new mapPolypeptidesToBin(application, usedBins));
        return mapToKeyedHash(ppBins, maxSize);
    }

    /**
     * return a list of all peptides in a bin
     *
     * @param inp
     * @return
     */
    public JavaPairRDD<BinChargeKey, IPolypeptide> mapFragmentsToBin(JavaRDD<IPolypeptide> inp, final Set<Integer> usedBins) {
        JavaPairRDD<BinChargeKey, IPolypeptide> ppBins = inp.flatMapToPair(new mapPolypeptidesToBin(application, usedBins));
        return ppBins;
    }

    /**
     * convert a JavaPairRDD into pairs where the key now indexes a list of all values
     *
     * @param imp input
     * @param <K> key typ
     * @return
     */
    public static <K extends Serializable> JavaPairRDD<K, HashMap<String, IPolypeptide>> mapToKeyedHash(JavaPairRDD<K, IPolypeptide> imp) {
        return mapToKeyedHash(imp, Integer.MAX_VALUE); // generate map of any size
    }

    /**
     * convert a JavaPairRDD into pairs where the key now indexes a list of all values
     *
     * @param imp     input
     * @param maxSize limit on generated list size - defaults to Integer.MAX_VALUE in above implementation
     * @param <K>     key type
     * @return
     */
    public static <K extends Serializable> JavaPairRDD<K, HashMap<String, IPolypeptide>> mapToKeyedHash(JavaPairRDD<K, IPolypeptide> imp, final int maxSize) {
        return imp.aggregateByKey(
                new HashMap<String, IPolypeptide>(),
                new org.apache.spark.api.java.function.Function2<HashMap<String, IPolypeptide>, IPolypeptide, HashMap<String, IPolypeptide>>() {
                    @Override
                    public HashMap<String, IPolypeptide> call(HashMap<String, IPolypeptide> vs, IPolypeptide v) throws Exception {
                        if (vs.size() >= maxSize)
                            return vs; // stop adding if limit reached
                        String key = v.toString();
                        if (!vs.containsKey(key)) {
                            vs.put(key, v);
                        } else {
                            // todo merge protiens
                            IPolypeptide old = vs.get(key);
                            IPolypeptide newPP = PolypeptideCombiner.mergeProteins(old, v);
                            vs.put(key, newPP);
                        }
                        return vs;
                    }
                },
                new org.apache.spark.api.java.function.Function2<HashMap<String, IPolypeptide>, HashMap<String, IPolypeptide>, HashMap<String, IPolypeptide>>() {
                    @Override
                    public HashMap<String, IPolypeptide> call(HashMap<String, IPolypeptide> vs, HashMap<String, IPolypeptide> vs2) throws Exception {
                        if (vs.size() >= maxSize)
                            return vs; // stop adding if limit reached

                        vs.putAll(vs2);
                        return vs;
                    }
                }
        );
    }


    public JavaPairRDD<BinChargeKey, IPolypeptide> mapFragmentsToKeys(JavaRDD<IPolypeptide> inp) {
        return inp.flatMapToPair(new mapPolypeptidesToBins());
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
                BinChargeKey key = BinChargeMapper.oneKeyFromChargeMz(charge, matchingMass / charge);
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
     * Note the parameter is an ArrayList to guarantee serializability
     */
    public static class mapPolypeptidesToBin extends AbstractLoggingPairFlatMapFunction<IPolypeptide, BinChargeKey, IPolypeptide> {

        private final XTandemMain application;
        private final Set<Integer> usedBins;

        public mapPolypeptidesToBin(final XTandemMain pApplication, Set<Integer> usedBins) {
            application = pApplication;
            this.usedBins = usedBins;
        }

        @Override
        public Iterable<Tuple2<BinChargeKey, IPolypeptide>> doCall(final IPolypeptide pp) throws Exception {
            //    double matchingMass = pp.getMatchingMass();
            List<Tuple2<BinChargeKey, IPolypeptide>> holder = new ArrayList<Tuple2<BinChargeKey, IPolypeptide>>();

            BinChargeKey key = BinChargeMapper.keyFromPeptide(pp);


            // if we don't use the bin don't get the peptide
            if (usedBins != null && usedBins.contains(key.getMzInt())) {
                holder.add(new Tuple2<BinChargeKey, IPolypeptide>(key, pp));
            }

            return holder;
        }
    }

    /**
     * peptides are only mapped once whereas spectra map to multiple  bins
     * Note the parameter is an ArrayList to guarantee serializability
     */
    public static class mapTheoreticalpeptidesToBin extends AbstractLoggingPairFlatMapFunction<IPolypeptide, BinChargeKey, CometTheoreticalBinnedSet> {

        private final XTandemMain application;
        private final Set<Integer> usedBins;

        public mapTheoreticalpeptidesToBin(final XTandemMain pApplication, Set<Integer> usedBins) {
            application = pApplication;
            this.usedBins = usedBins;
        }

        @Override
        public Iterable<Tuple2<BinChargeKey, CometTheoreticalBinnedSet>> doCall(final IPolypeptide pp) throws Exception {
            //    double matchingMass = pp.getMatchingMass();
            List<Tuple2<BinChargeKey, CometTheoreticalBinnedSet>> holder = new ArrayList<Tuple2<BinChargeKey, CometTheoreticalBinnedSet>>();

            BinChargeKey key = BinChargeMapper.keyFromPeptide(pp);

            CometTheoreticalBinnedSet ts = (CometTheoreticalBinnedSet) application.getScoreRunner().generateSpectrum(pp);
            if (TestUtilities.isInterestingPeptide(pp)) {
                CometTesting.validateOneKey(); // We are hunting for when this stops working
            }


            // if we don't use the bin don't get the peptide
            if (usedBins != null && usedBins.contains(key.getMzInt())) {
                holder.add(new Tuple2<BinChargeKey, CometTheoreticalBinnedSet>(key, ts));
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
            double matchingMass = CometScoringAlgorithm.getCometMatchingMass(pp);

            if (TestUtilities.isInterestingPeptide(pp))
                TestUtilities.breakHere();

            Scorer scorer = application.getScoreRunner();

            List<Tuple2<BinChargeKey, ITheoreticalSpectrumSet>> holder = new ArrayList<Tuple2<BinChargeKey, ITheoreticalSpectrumSet>>();
            BinChargeKey key = BinChargeMapper.oneKeyFromChargeMz(1, matchingMass);
            ITheoreticalSpectrumSet ts = scorer.generateSpectrum(pp);

            holder.add(new Tuple2<BinChargeKey, ITheoreticalSpectrumSet>(key, ts));
            return holder;
        }
    }

    private class mapMeasuredSpectraToBins<T extends IMeasuredSpectrum> extends AbstractLoggingPairFlatMapFunction<T, BinChargeKey, T> {
        @Override
        public Iterable<Tuple2<BinChargeKey, T>> doCall(final T spec) throws Exception {
//            int charge = spec.getPrecursorCharge();
//            charge = 1; // all peptides use 1 now

//            if (TestUtilities.isInterestingSpectrum(spec))
//                TestUtilities.breakHere();

            List<Tuple2<BinChargeKey, T>> holder = new ArrayList<Tuple2<BinChargeKey, T>>();

            BinChargeKey[] keys = BinChargeMapper.keysFromSpectrum(spec);

            for (int i = 0; i < keys.length; i++) {
                BinChargeKey key = keys[i];
                holder.add(new Tuple2<BinChargeKey, T>(key, spec));
            }
            if (holder.isEmpty())
                throw new IllegalStateException("problem"); // ToDo change

//            if (TestUtilities.isInterestingSpectrum(spec)) {
//                TestUtilities.saveSpectrumKey(holder);
//            }

            return holder;
        }
    }

    private class mapMeasuredSpectraToBinTuples<T extends IMeasuredSpectrum> extends AbstractLoggingPairFlatMapFunction<T, BinChargeKey, Tuple2<BinChargeKey, T>> {
        /**
         * do work here
         *
         * @param
         * @return
         */
        @Override
        public Iterable<Tuple2<BinChargeKey, Tuple2<BinChargeKey, T>>> doCall(final T spec) throws Exception {
            List<Tuple2<BinChargeKey, Tuple2<BinChargeKey, T>>> holder = new ArrayList<Tuple2<BinChargeKey, Tuple2<BinChargeKey, T>>>();
            BinChargeKey[] keys = BinChargeMapper.keysFromSpectrum(spec);
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
