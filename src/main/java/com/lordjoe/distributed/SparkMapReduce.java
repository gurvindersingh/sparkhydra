package com.lordjoe.distributed;

import com.lordjoe.distributed.spark.accumulators.*;
import com.lordjoe.distributed.util.*;
import com.lordjoe.distributed.wordcount.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import scala.*;

import javax.annotation.*;
import java.io.Serializable;
import java.nio.file.*;

/**
 * com.lordjoe.distributed.SparkMapReduce
 * User: Steve
 * Date: 8/25/2014
 */
public class SparkMapReduce<KEYIN extends Serializable, VALUEIN extends Serializable, K extends Serializable, V extends Serializable, KOUT extends Serializable, VOUT extends Serializable>
        extends AbstractMapReduceEngine<KEYIN, VALUEIN, K, V, KOUT, VOUT> implements Serializable {


    public static final MapReduceEngineFactory FACTORY = new MapReduceEngineFactory() {

        /**
         * build an engine having been passed a
         *
         * @param name
         * @param pMapper  map function
         * @param pRetucer reduce function
         * @return return a constructed instance
         */
        @Override
        public <KEYIN extends Serializable, VALUEIN extends Serializable, K extends Serializable, V extends Serializable, KOUT extends Serializable, VOUT extends Serializable> IMapReduce<KEYIN, VALUEIN, KOUT, VOUT>
        buildMapReduceEngine(@Nonnull final String name, @Nonnull final IMapperFunction<KEYIN, VALUEIN, K, V> pMapper, @Nonnull final IReducerFunction<K, V, KOUT, VOUT> pRetucer) {
            return new SparkMapReduce(name, pMapper, pRetucer);
        }

        /**
         * build an engine having been passed a
         *
         * @param name
         * @param pMapper      map function
         * @param pRetucer     reduce function
         * @param pPartitioner partition function default is HashPartition
         * @return return a constructed instance
         */
        @Override
        public <KEYIN extends Serializable, VALUEIN extends Serializable, K extends Serializable, V extends Serializable, KOUT extends Serializable, VOUT extends Serializable> IMapReduce<KEYIN, VALUEIN, KOUT, VOUT>
        buildMapReduceEngine(@Nonnull final String name, @Nonnull final IMapperFunction<KEYIN, VALUEIN, K, V> pMapper, @Nonnull final IReducerFunction<K, V, KOUT, VOUT> pRetucer, final IPartitionFunction<K> pPartitioner) {
            return new SparkMapReduce(name, pMapper, pRetucer, pPartitioner);
        }
    };
    // NOTE these are not serializable so they must be transient or an exception will be thrown on serialization
    private JavaRDD<KeyValueObject<KOUT, VOUT>> output;

    public SparkMapReduce(final String name, final IMapperFunction<KEYIN, VALUEIN, K, V> mapper, final IReducerFunction<K, V, KOUT, VOUT> reducer) {
        //noinspection unchecked
        this(name, mapper, reducer, IPartitionFunction.HASH_PARTITION);
    }

    public SparkMapReduce(final String name, final IMapperFunction<KEYIN, VALUEIN, K, V> mapper,
                          final IReducerFunction<K, V, KOUT, VOUT> reducer,
                          IPartitionFunction<K> pPartitioner,
                          IKeyValueConsumer<KOUT, VOUT>... pConsumer) {

        this(new SparkConf(), name, mapper, reducer, IPartitionFunction.HASH_PARTITION);
    }

    public SparkMapReduce(final SparkConf conf, final String name, final IMapperFunction<KEYIN, VALUEIN, K, V> pMapper,
                          final IReducerFunction<K, V, KOUT, VOUT> pRetucer,
                          IPartitionFunction<K> pPartitioner,
                          IKeyValueConsumer<KOUT, VOUT>... pConsumer) {
        setMap(pMapper);
        setReduce(pRetucer);
        setPartitioner(pPartitioner);

        for (int i = 0; i < pConsumer.length; i++) {
            IKeyValueConsumer<KOUT, VOUT> cns = pConsumer[i];
            addConsumer(cns);

        }
        conf.setAppName(name);

    }


    public JavaRDD<KeyValueObject<KOUT, VOUT>> getOutput() {
        return output;
    }

    protected Partitioner sparkPartitioner = new Partitioner() {
        @Override
        public int numPartitions() {
            return getNumberReducers();
        }

        @Override
        public int getPartition(final Object key) {
            IPartitionFunction<K> partitioner = getPartitioner();
            int value = partitioner.getPartition((K) key);
            return Math.abs(value % numPartitions());
        }
    };


    public static void guaranteePathReader() {
        IPathReader reader = PathUtilities.getReader(); // todo this works but might not in the cluster
        if (reader == null)
            throw new UnsupportedOperationException("Add a Sparky reader");
    }


    /**
     * all the work is done here
     *
     * @param source
     * @param sink
     */
    //@Override
    public void performSingleReturnMapReduce(JavaRDD<KeyValueObject<KEYIN, VALUEIN>> pInputs) {
        // if not commented out this line forces mappedKeys to be realized
        //    pInputs = SparkUtilities.realizeAndReturn(pInputs,getCtx());
          JavaPairRDD<K, Tuple2<K, V>> kkv = performMappingPart(pInputs);

           // if not commented out this line forces kvJavaPairRDD to be realized
        kkv = SparkUtilities.realizeAndReturn(kkv );

        PartitionAdaptor<K> prt = new PartitionAdaptor<K>(getPartitioner());
        kkv = kkv.partitionBy(prt);

        IReducerFunction reduce = getReduce();
        /**
         * we can guarantee one output per input
         */
        SingleOutputReduceFunctionAdaptor<K, V, KOUT, VOUT> f = new SingleOutputReduceFunctionAdaptor((ISingleOutputReducerFunction) reduce);
        JavaRDD<KeyValueObject<KOUT, VOUT>> reduced = kkv.map(f);

         // if not commented out this line forces kvJavaPairRDD to be realized
        reduced = SparkUtilities.realizeAndReturn(reduced );

        output = reduced;
    }

    /**
     * Perform a Hadoop style map and return a sorted list of keys
     *
     * @param pInputs
     * @return
     */
    protected JavaPairRDD<K, Tuple2<K, V>> performMappingPart(JavaRDD<KeyValueObject<KEYIN, VALUEIN>> pInputs) {
        guaranteePathReader();
        IMapperFunction map = getMap();

        MapFunctionAdaptor<KEYIN, VALUEIN, K, V> ma = new MapFunctionAdaptor<KEYIN, VALUEIN, K, V>(map);

        JavaRDD<KeyValueObject<K, V>> mappedKeys = pInputs.flatMap(ma);
        JavaPairRDD<K, Tuple2<K, V>> kkv = mappedKeys.mapToPair(new KeyValuePairFunction<K, V>());
        kkv = kkv.sortByKey();
        return kkv;
    }


    /**
     * all the work is done here
     *
     * @param source
     * @param sink
     */
    //@Override
    public void performSourceMapReduce(JavaRDD<KeyValueObject<KEYIN, VALUEIN>> pInputs) {
        // if not commented out this line forces mappedKeys to be realized
        //    pInputs = SparkUtilities.realizeAndReturn(pInputs,getCtx());
        JavaSparkContext ctx2 = SparkUtilities.getCurrentContext();
        System.err.println("Starting Score Mapping");
        JavaPairRDD<K, Tuple2<K, V>> kkv = performMappingPart(pInputs);
        //      kkv = SparkUtilities.realizeAndReturn(kkv, ctx2);

//        mappedKeys = mappedKeys.persist(StorageLevel.MEMORY_AND_DISK_2());
//        // if not commented out this line forces mappedKeys to be realized
//        mappedKeys = SparkUtilities.realizeAndReturn(mappedKeys, ctx2);
//
//        // convert to tuples
//     //   JavaPairRDD<K, Tuple2<K, V>> kkv = mappedKeys.mapToPair(new KeyValuePairFunction<K, V>());
//
//        kkv = kkv.persist(StorageLevel.MEMORY_AND_DISK_2());
//        // if not commented out this line forces mappedKeys to be realized
//       kkv = SparkUtilities.realizeAndReturn(kkv, ctx2);

        // if not commented out this line forces kvJavaPairRDD to be realized
       // kkv = SparkUtilities.realizeAndReturn(kkv );

        System.err.println("Starting Score Reduce");
        IReducerFunction reduce = getReduce();
        // for some reason the compiler thnks K or V is not Serializable
        JavaPairRDD<K, Tuple2<K, V>> kkv1 = kkv;

        // JavaPairRDD<? extends Serializable, Tuple2<? extends Serializable, ? extends Serializable>> kkv1 = (JavaPairRDD<? extends Serializable, Tuple2<? extends Serializable, ? extends Serializable>>)kkv;
        //noinspection unchecked
        JavaPairRDD<K, KeyAndValues<K, V>> reducedSets = (JavaPairRDD<K, KeyAndValues<K, V>>) KeyAndValues.combineByKey(kkv1);


        // if not commented out this line forces kvJavaPairRDD to be realized
        reducedSets = SparkUtilities.realizeAndReturn(reducedSets );

        PartitionAdaptor<K> prt = new PartitionAdaptor<K>(getPartitioner());
        reducedSets = reducedSets.partitionBy(prt);
        reducedSets = reducedSets.sortByKey();


        // if not commented out this line forces kvJavaPairRDD to be realized
        reducedSets = SparkUtilities.realizeAndReturn(reducedSets );

        ReduceFunctionAdaptor f = new ReduceFunctionAdaptor(ctx2, reduce);

        JavaRDD<KeyValueObject<KOUT, VOUT>> reducedOutput = reducedSets.flatMap(f);


        //  JavaPairRDD<K, V> kvJavaPairRDD = asTuples.partitionBy(sparkPartitioner);

        // if not commented out this line forces kvJavaPairRDD to be realized
        //kvJavaPairRDD = SparkUtilities.realizeAndReturn(kvJavaPairRDD,getCtx());


        // if not commented out this line forces kvJavaPairRDD to be realized
        //  reducedOutput = SparkUtilities.realizeAndReturn(reducedOutput, ctx2);

        output = reducedOutput;
    }
    //
//        SparkUtilities.showRDD(javaRDD); // stop and look

    /**
     * sources may be very implementation specific
     *
     * @param source    some source of data - might be a hadoop directory or a Spark RDD - this will be cast internally
     * @param otherData
     */
    @Override
    public void mapReduceSource(@Nonnull final Object source, final Object... otherData) {
        if (source instanceof JavaRDD) {
            performSourceMapReduce((JavaRDD) source);
            return;
        }
        if (source instanceof Path) {
            performMapReduce((Path) source);
            return;
        }
        if (source instanceof java.lang.Iterable) {
            performSourceMapReduce(SparkUtilities.fromIterable((Iterable) source));
            return;

        }
        throw new IllegalArgumentException("cannot handle source of class " + source.getClass());
    }

    protected void performMapReduce(final Path pSource) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    /**
     * take the results of another engine and ues it as the input
     *
     * @param source some other engine - usually this will be cast to a specific type
     */
    @Override
    public void chain(@Nonnull final IMapReduce source) {
        performSourceMapReduce(((SparkMapReduce) source).output);
    }

    /**
     * the last step in mapReduce - returns the output as an iterable
     *
     * @return
     */
    @Nonnull
    @Override
    public Iterable<KeyValueObject<KOUT, VOUT>> collect() {
        return output.collect();
    }

    //
//    protected void performReduce(Iterable<List<KeyValueObject<K, V>>> partitions) {
//        for (List<KeyValueObject<K, V>> partition : partitions) {
//            handlePartition(partition);
//        }
//    }
//
//
//    protected void handlePartition(Iterable<KeyValueObject<K, V>> partition) {
//        IReducerFunction reduce = getReduce();
//        List<IKeyValueConsumer<K, V>> consumers1 = getConsumers();
//        IKeyValueConsumer<K, V>[] consumers = consumers1.toArray(new IKeyValueConsumer[consumers1.size()]);
//          K key = null;
//        List<V> holder = new ArrayList();
//        for (KeyValueObject<K, V> kv : partition) {
//            if (!kv.key.equals(key)) {
//                if (!holder.isEmpty()) {
//                    reduce.handleValues(key, holder, consumers);// todo this is values
//                }
//                holder.clear();
//                key = kv.key;
//            }
//            holder.add(kv.value);
//        }
//    }
//
//
//    protected Iterable<KeyValueObject<K, V>> performMap(final Path source) {
//        ISourceFunction source1 = getSource();
//        Iterable<VALUEIN> inputs = source1.readInput(source);
//        return performSourceMap(inputs);
//    }
//
//    protected Iterable<KeyValueObject<K, V>> performSourceMap(final Iterable<VALUEIN> pInputs) {
//        IMapperFunction map = getMap();
//        List<KeyValueObject<K, V>> holder = new ArrayList();
//        for (VALUEIN input : pInputs) {
//            Iterable<KeyValueObject<K, V>> iterable = map.mapValues(input);
//            for (KeyValueObject<K, V> kv : iterable) {
//                holder.add(kv);
//            }
//        }
//        return holder;
//    }
//
//    protected List<KeyValueObject<K, V>>[] buildPartitions() {
//        int numberReducers = getNumberReducers();
//        List<KeyValueObject<K, V>>[] partitions = new List[numberReducers];
//        for (int i = 0; i < partitions.length; i++) {
//            partitions[i] = new ArrayList<KeyValueObject<K, V>>();
//
//        }
//        return partitions;
//    }
//
//    /**
//     * perform shuffle in memory end up with
//     *
//     * @param maps
//     * @return
//     */
//    protected Iterable<List<KeyValueObject<K, V>>> partition(Iterable<KeyValueObject<K, V>> maps) {
//        IPartitionFunction partitioner = getPartitioner();
//        int numberReducers = getNumberReducers();
//        List<KeyValueObject<K, V>>[] partitions = buildPartitions();
//
//        for (KeyValueObject<K, V> kv : maps) {
//            int index = partitioner.getPartition(kv.key) % numberReducers;
//            partitions[index].add(kv);
//        }
//        for (List<KeyValueObject<K, V>> partition : partitions) {
//            Collections.sort(partition, KeyValueObject.KEY_COMPARATOR);
//        }
//        return Arrays.asList(partitions);
//    }


}
