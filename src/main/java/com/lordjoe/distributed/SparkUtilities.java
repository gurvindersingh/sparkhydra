package com.lordjoe.distributed;

import com.lordjoe.distributed.database.*;
import com.lordjoe.distributed.hydra.test.*;
import com.lordjoe.distributed.output.*;
import com.lordjoe.distributed.spark.*;
import com.lordjoe.distributed.spark.JavaSparkListener;
import com.lordjoe.distributed.test.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.log4j.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.sql.*;
import org.apache.spark.storage.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.peptide.*;
import parquet.org.slf4j.spi.*;
import scala.*;

import javax.annotation.*;
import java.io.*;
import java.io.Serializable;
import java.lang.Boolean;
import java.lang.Long;
import java.net.*;
import java.util.*;

/**
 * com.lordjoe.distributed.SpareUtilities
 * A very useful class representing a number of static functions useful in Spark
 * User: Steve
 * Date: 8/28/2014
 */
public class SparkUtilities implements Serializable {

    public static final String DO_DEBUGGING_CONFIG_PROPERTY = "com.lordjoe.distributed.do_debugging_count";
    private transient static LoggerFactoryBinder FORCE_LOAD = null;
    //  private transient static ThreadLocal<JavaSparkContext> threadContext;
    private transient static JavaSparkContext threadContext;
    //  private transient static ThreadLocal<JavaSQLContext> threadContext;
    private transient static SQLContext sqlContext;
    private static final Properties sparkProperties = new Properties();

    public static final String LOCAL_PROPERTIES_RESOURCE = "/DefaultSparkLocalCluster.properties";
    public static final String CLUSTER_PROPERTIES_RESOURCE = "/DefaultSparkCluster.properties";

    public static final String DEFAULT_APP_NAME = "Anonymous";
    public static final String MARKER_PROPERTY_NAME = "com.lordjoe.distributed.marker_property";
    public static final String NUMBER_PARTITIONS_PROPERTY_NAME = "com.lordjoe.distributed.number_partitions";
    public static final String LOG_FUNCTIONS_PROPERTY_NAME = "com.lordjoe.distributed.logFunctionsByDefault";
    public static final String MARKER_PROPERTY_VALUE = "spark_property_set";

    private static String appName = DEFAULT_APP_NAME;
    private static String pathPrepend = "";

    private static CometSpectraUse desiredUses;

    public static void setDesiredUse(CometSpectraUse desired)  {
        desiredUses = desired;
    }

    private static int numberNotFound;
    private static int numberFound;
     public static boolean validateDesiredUse(IMeasuredSpectrum spec,IPolypeptide pp,double score)  {

        CometSpectraUse desiredUses = SparkUtilities.desiredUses;
        if(desiredUses == null)
            return false;
        CometSpectralScoring scoring = desiredUses.getScoring(spec, pp);
        double desiredScore  = 0;
        if(scoring != null)    {
            numberFound++;
            desiredScore = scoring.score;
            double comparison = score / 0.005;
            if(Math.abs(desiredScore - comparison) > 0.1)  {
                System.out.println(scoring + "\t" + " found " + comparison);
            }
            return true;
           }
        else {
            numberNotFound++;
            return false;
        }
        // todo check now

    }


    private static transient boolean logSetToWarn;
    private static boolean local;

    public static boolean isLocal() {
        return local;
        // return getCurrentContext().isLocal();
    }


    public static void setLocal(final boolean pLocal) {
        local = pLocal;
    }

    private static final int DEFAULT_NUMBER_PARTITIONS_X = 120;

    private static int defaultNumberPartitions = DEFAULT_NUMBER_PARTITIONS_X;

    public static int getDefaultNumberPartitions() {
        if (isLocal())
            return 1;
        SparkConf sparkConf = SparkUtilities.getCurrentContext().getConf();
        return sparkConf.getInt("spark.default.parallelism", DEFAULT_NUMBER_PARTITIONS_X);
    }

    public static void setDefaultNumberPartitions(final int pDefaultNumberPartitions) {
        defaultNumberPartitions = pDefaultNumberPartitions;
    }

    /**
     * set up a sane partition scheme
     */
    public static final Partitioner DEFAULT_PARTITIONER_X = new Partitioner() {
        @Override
        public int numPartitions() {
            return getDefaultNumberPartitions();
        }

        @Override
        public int getPartition(final Object key) {
            int hsh = Math.abs(key.hashCode());
            return hsh % numPartitions();
        }
    };


    public static synchronized SQLContext getCurrentSQLContext() {
        if (sqlContext != null)
            return sqlContext;

        sqlContext = new SQLContext(getCurrentContext());
        return sqlContext;
    }


    public static synchronized Configuration getHadoopConfiguration() {
        Configuration configuration = getCurrentContext().hadoopConfiguration();
        // Pass our properties to the hadoopConfiguration
        for (String property : sparkProperties.stringPropertyNames()) {
            String value = sparkProperties.getProperty(property);
            // handle hard coded properties

            configuration.set(property, value);

        }
        return configuration;
    }

    /**
     * return the current Hadoop file System
     *
     * @return
     */
    public static synchronized FileSystem getHadoopFileSystem() {
        try {
            return FileSystem.get(getHadoopConfiguration());
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * retuirn an open OutputStream to a file of this name
     *
     * @param fileName name of a nonexistant ot to be deleted file
     * @return OutputStream
     */
    public static
    @Nonnull
    OutputStream getHadoopOutputStream(@Nonnull String fileName) {
        FileSystem fs = getHadoopFileSystem();
        Path path = XTandemHadoopUtilities.getRelativePath(fileName);
        try {
            boolean recursive = false;
            fs.delete(path, false);
            return fs.create(path);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * retuirn an open PrintWriter to a file of this name
     *
     * @param fileName name of a nonexistant ot to be deleted file
     * @return printwriter
     */
    public static
    @Nonnull
    PrintWriter getHadoopPrintWriter(@Nonnull String fileName) {
        OutputStream os = getHadoopOutputStream(fileName);
        PrintWriter ret = new PrintWriter(os);
        return ret;
    }

    /**
     * turn an RDD of Tuples into a JavaPairRdd
     *
     * @param imp
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K, V> JavaPairRDD<K, V> mapToPairs(JavaRDD<Tuple2<K, V>> imp) {
        return imp.mapToPair(new PairFunction<Tuple2<K, V>, K, V>() {
            @Override
            public Tuple2<K, V> call(final Tuple2<K, V> t) throws Exception {
                return t;
            }
        });
    }

    /**
     * turn an RDD of Tuples into a JavaPairRdd with the original key as a key
     *
     * @param imp
     * @param <K>
     * @param <V>
     * @return
     */
    public static <K extends Serializable, V extends Serializable> JavaPairRDD<K, Tuple2<K, V>> mapToKeyedPairs(JavaRDD<Tuple2<K, V>> imp) {
        return imp.mapToPair(new Tuple2Tuple2PairFunction<K, V>());
    }


    private static class Tuple2Tuple2PairFunction<K extends Serializable, V extends Serializable> extends AbstractLoggingPairFunction<Tuple2<K, V>, K, Tuple2<K, V>> {
        @Override
        public Tuple2<K, Tuple2<K, V>> doCall(final Tuple2<K, V> t) throws Exception {
            return new Tuple2<K, Tuple2<K, V>>(t._1(), t);
        }
    }


    /**
     * convert a JavaPairRDD into one with the tuples so that combine by key can know the key
     *
     * @param imp
     * @param <K>  key type
       * @param <V>  value type
      * @return
     */
    public static <K extends Serializable, V extends Serializable> JavaPairRDD<K, Tuple2<K, V>> mapToKeyedPairs(JavaPairRDD<K, V> imp) {
        return imp.mapToPair(new TupleToKeyPlusTuple<K, V>());
    }
    /**
      * convert a JavaPairRDD into pairs where the key now indexes a list of all values
        * @param imp input
      * @param <K>  key type
      * @param <V>  value type
      * @return
      */
     public static <K extends Serializable, V extends Serializable> JavaPairRDD<K, ArrayList<V>>   mapToKeyedList(JavaPairRDD<K, V> imp) {
         return imp.aggregateByKey(
                 new ArrayList<V>(),
                 new Function2<ArrayList<V>, V, ArrayList<V>>() {
                     @Override
                     public ArrayList<V> call(ArrayList<V> vs, V v) throws Exception {
                         vs.add(v);
                         return vs;
                     }
                 },
                 new Function2<ArrayList<V>, ArrayList<V>, ArrayList<V>>() {
                     @Override
                     public ArrayList<V> call(ArrayList<V> vs, ArrayList<V> vs2) throws Exception {
                         vs.addAll(vs2);
                         return vs;
                     }
                 }
         );
     }

    /**
      * convert a JavaRDD into a new RDD by casting
        * @param imp input
      * @param <K>  final type
      * @param <V>  initial type
      * @return
      */
     public static <V extends Serializable,K extends Serializable> JavaRDD<K>   castRDD(JavaRDD< V> imp,Class<? extends K> cls) {
         return imp.map(new Function<V, K>() {
             @Override
             public K call(V v) throws Exception {
                 return (K) v;
             }
         });
     }
    /**
        * convert a JavaPairRDD into a new JavaPairRDD by casting the value
          * @param imp input
        * @param <K>  final type
        * @param <V>  initial type
        * @return
        */
       public static <Q extends Serializable,V extends Serializable,K extends Serializable> JavaPairRDD<Q,K>   castRDD(JavaPairRDD<Q, V> imp,Class<? extends K> cls) {
           return imp.mapToPair(new PairFunction<Tuple2<Q, V>, Q, K>() {
               @Override
               public Tuple2<Q, K> call(Tuple2<Q, V> qvTuple2) throws Exception {
                       return new Tuple2<Q, K>(qvTuple2._1(),(K)qvTuple2._2());
               }
           });
       }


    private static class TupleToKeyPlusTuple<K extends Serializable, V extends Serializable> extends AbstractLoggingPairFunction<Tuple2<K, V>, K, Tuple2<K, V>> {
        @Override
        public Tuple2<K, Tuple2<K, V>> doCall(final Tuple2<K, V> t) throws Exception {
            return new Tuple2<K, Tuple2<K, V>>(t._1(), t);
        }
    }

    /**
     * dump all spark properties to System.err
     */
    public static void showSparkProperties() {
        showSparkProperties(System.err);
    }


    /**
     * dump all spark properties to out
     *
     * @param out
     */
    public static void showSparkProperties(Appendable out) {
        try {
            SparkConf sparkConf = new SparkConf();
            Tuple2<String, String>[] all = sparkConf.getAll();
            for (Tuple2<String, String> prp : all) {
                out.append(prp._1().toString() + "=" + prp._2());
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static void showSparkPropertiesInAnotherThread() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                showSparkProperties();
            }
        }).start();
    }

    /**
     * we set an unusual property to makr properties have been set -
     * if we find this is set we know that the conf has been set up properly
     * and does not need to be set up
     *
     * @param sparkConf
     * @return
     */
    protected static boolean isPropertiesSetAsOriginal(SparkConf sparkConf) {
        String test = sparkConf.get(MARKER_PROPERTY_NAME, "");
        return MARKER_PROPERTY_VALUE.equals(test);
    }

    /**
     * create a JavaSparkContext for the thread if none exists
     *
     * @return
     */
    public static synchronized JavaSparkContext getCurrentContext() {
//        if (threadContext == null)
//            threadContext = new ThreadLocal<JavaSparkContext>();
//        JavaSparkContext ret = threadContext.get();
        JavaSparkContext ret = threadContext;
        if (ret != null)
            return ret;
        SparkConf sparkConf = new SparkConf();


        sparkConf.setAppName(getAppName());
        // we have a configuration already set
        if (isPropertiesSetAsOriginal(sparkConf)) {
            ret = new JavaSparkContext(sparkConf);
            threadContext = ret;
            //      threadContext.set(ret);
            return ret;
        }
        // not set up so fill in app name and properties
        // mark properties so we know things have been set
        sparkConf.set(MARKER_PROPERTY_NAME, MARKER_PROPERTY_VALUE);
        SparkUtilities.guaranteeSparkMaster(sparkConf);


        Option<String> option = sparkConf.getOption("com.lordjoe.Log4jProperties");
        if (option.isDefined()) {
            String propertyFile = option.get();
            PropertyConfigurator.configure(propertyFile);
        }


        // what are we using as a serializer
        //showOption("spark.serializer",sparkConf);

        option = sparkConf.getOption("spark.serializer");
        if (!option.isDefined())
            sparkConf.set("spark.serializer", "org.apache.spark.serializer.JavaSerializer");   // todo use kryo
//        else {
//             if(option.get().equals("org.apache.spark.serializer.KryoSerializer"))
//                   sparkConf.set("spark.kryo.registrator", "com.lordjoe.distributed.hydra.HydraKryoSerializer");
//           }
        // if we use Kryo register classes

        //         Now set in properties
        //       sparkConf.set("spark.mesos.coarse", "true");
        //       sparkConf.set("spark.executor.memory", "2500m");

//        option = sparkConf.getOption("spark.default.parallelism");
//        if (option.isDefined())
//            System.err.println("Parellelism = " + option.get());
//
//        option = sparkConf.getOption("spark.executor.heartbeatInterval");
//        if (option.isDefined())
//            System.err.println("timeout = " + option.get());
        ret = new JavaSparkContext(sparkConf);

        SparkContext sparkContext = JavaSparkContext.toSparkContext(ret);

        sparkContext.addSparkListener(new JavaSparkListener());

        threadContext = ret;

        // Show spark properties
        Tuple2<String, String>[] all = sparkConf.getAll();
        System.err.println("Spark Conf Properties");
        for (int i = 0; i < all.length; i++) {
            Tuple2<String, String> prop = all[i];
            System.err.println(prop._1() + "=" + prop._2());
        }
        //      threadContext.set(ret);

        SparkAccumulators.createInstance();
        SparkBroadcastObjects.createInstance();

        System.out.println("Set Log to Warn");
        setLogToWarn();
//        LoggerRepository loggerRepository = rootLogger.getLoggerRepository();
//        Logger logger = loggerRepository.getLogger("storage.MemoryStore");
//        logger.setLevel();
        return ret;
    }

    public static void setLogToWarn() {
        if (!logSetToWarn) {
            Logger rootLogger = Logger.getRootLogger();
            rootLogger.setLevel(Level.WARN);

            // not sure why this is needed
            setNamedLoggerToWarn("spark");
            setNamedLoggerToWarn("akka");
            setNamedLoggerToWarn("sparkDriver-akka");

            logSetToWarn = true;
        }
    }

    protected static void setNamedLoggerToWarn(String name) {
        try {
            Logger spark = Logger.getLogger(name);
            if (spark != null)
                spark.setLevel(Level.WARN);
        }
        catch (Exception e) {
            // forgive errors
        }

    }


    public static void showOption(String optionName, SparkConf sparkConf) {
        Option<String> option = sparkConf.getOption(optionName);
        if (option.isDefined())
            System.err.println(optionName + "=" + getOption(optionName, sparkConf));
        else
            System.err.println(optionName + "= undefined");
    }

    public static String getOption(String optionName, SparkConf sparkConf) {
        Option<String> option = sparkConf.getOption(optionName);
        if (option.isDefined())
            return option.get();
        else
            return null;
    }

    /**
     * return the name of the current App
     *
     * @return
     */
    public static String getAppName() {
        return appName;
    }

    public static void setAppName(final String pAppName) {
        appName = pAppName;
    }

    public static Properties getSparkProperties() {
        return sparkProperties;
    }


    /**
     * a string prepended to the path =
     * might be   hdfs://daas/steve/Sample2/
     * usually reflects a mapping from user.dir to whatever files Spark is using
     * - I assume hdfs
     *
     * @return
     */
    public static String getPathPrepend() {
        return pathPrepend;
    }


    public static void setPathPrepend(final String pPathPrepend) {
        pathPrepend = pPathPrepend;
    }

    public static String mapToPath(String cannonicPath) {
        return getPathPrepend() + cannonicPath;
    }


    /**
     * read a path and return it as a LineNumber reader of the content
     * <p/>
     * /**
     * read a file with a list of desired properties
     *
     * @param fileName
     * @return
     */
    public static void readSparkProperties(String fileName) {
        try {
            File f = new File(fileName);
            //noinspection UnnecessaryLocalVariable,UnusedDeclaration,UnusedAssignment
            String path = f.getAbsolutePath();
            sparkProperties.load(new FileReader(f));  // read spark properties
        }
        catch (IOException e) {
            throw new RuntimeException(" bad spark properties file " + fileName);

        }
    }

    public static final String FORCE_LOCAL_EXECUTION_PROPERTY = "com.lordjoe.distributes.ForceLocalExecution";

    /**
     * if no spark master is  defined then use "local
     *
     * @param sparkConf the configuration
     */
    public static void guaranteeSparkMaster(@Nonnull SparkConf sparkConf) {

        // I used to force local but think that could be a mistake

        Option<String> option = sparkConf.getOption("spark.master");

        boolean forceLocalExecution = "true".equals(sparkProperties.getProperty(FORCE_LOCAL_EXECUTION_PROPERTY, "false"));

        if (forceLocalExecution || !option.isDefined()) {   // use local over nothing
            sparkConf.setMaster("local[*]");
            setLocal(true);
         }
        else {
            setLocal(option.get().startsWith("local"));
        }
        Properties defaults;
        if(isLocal())   {
            defaults = readResourceProperties(LOCAL_PROPERTIES_RESOURCE);
        }
        else {
            defaults = readResourceProperties(CLUSTER_PROPERTIES_RESOURCE);

        }

        for (String property : defaults.stringPropertyNames()) {
            if(!sparkProperties.containsKey(property))
                sparkProperties.setProperty(property,defaults.getProperty(property)) ;
         }

//        // set all properties in the SparkProperties file
        sparkConf.set("spark.ui.killEnabled", "true");  // always allow a job to be killed
        for (String property : sparkProperties.stringPropertyNames()) {
            String value = sparkProperties.getProperty(property);
            if (property.startsWith("spark.")) {
                sparkConf.set(property, value);
            }
            else if (NUMBER_PARTITIONS_PROPERTY_NAME.equals(property)) {
                setDefaultNumberPartitions(Integer.parseInt(value));
            }
            else if (LOG_FUNCTIONS_PROPERTY_NAME.equals(property)) {
                SparkAccumulators.setFunctionsLoggedByDefault(Boolean.parseBoolean(value));
            }
        }

    }


    private static Properties readResourceProperties(String s) {
        try {
            InputStream resourceAsStream = SparkUtilities.class.getResourceAsStream(s);
            Properties ret = new Properties();
            ret.load(resourceAsStream);
            resourceAsStream.close();
            return ret;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }



    /**
     * read a stream into memory and return it as an RDD
     * of lines
     *
     * @param is the stream
     * @param sc the configuration
     * @return
     */
    @SuppressWarnings("UnusedDeclaration")
    public static JavaRDD<String> fromInputStream(@Nonnull InputStream is, @Nonnull JavaSparkContext sc) {
        try {
            List<String> lst = new ArrayList<String>();
            BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
            String line = rdr.readLine();
            while (line != null) {
                lst.add(line);
                line = rdr.readLine();
            }
            rdr.close();
            return sc.parallelize(lst);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    /**
     * read a stream into memory and return it as an RDD
     * of lines
     *
     * @param is the stream
     * @param sc the configuration
     * @return
     */
    public static JavaRDD<KeyValueObject<String, String>> keysFromInputStream(@Nonnull String key, @Nonnull InputStream is, @Nonnull JavaSparkContext sc) {
        try {
            List<KeyValueObject<String, String>> lst = new ArrayList<KeyValueObject<String, String>>();
            BufferedReader rdr = new BufferedReader(new InputStreamReader(is));
            String line = rdr.readLine();
            while (line != null) {
                lst.add(new KeyValueObject<String, String>(key, line));
                line = rdr.readLine();
            }
            return sc.parallelize(lst);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static final String PATH_PREPEND_PROPERTY = "com.lordjoe.distributed.PathPrepend";

    /**
     * @param pathName given path - we may need to predend hdfs access
     * @return
     */
    public static String buildPath(final String pathName) {
        if (pathName.startsWith("hdfs://"))
            return pathName;
        String prepend = sparkProperties.getProperty(PATH_PREPEND_PROPERTY);
        if (prepend == null)
            return pathName;
        if(pathName.startsWith(prepend))
            return pathName; // already there
        return prepend + pathName;
    }


    /**
     * function that returns the values of a Tuple as an RDD
     */
    public static final TupleValues TUPLE_VALUES = new TupleValues();

    public static class TupleValues<K extends Serializable> extends AbstractLoggingFunction<Tuple2<Object, K>, K> {
        private TupleValues() {
        }

        @Override
        public K doCall(final Tuple2<Object, K> v1) throws Exception {
            return v1._2();
        }
    }

    /**
     * function that returns the original object
     */
    public static final IdentityFunction IDENTITY_FUNCTION = new IdentityFunction();

    public static class IdentityFunction<K extends Serializable> extends AbstractLoggingFunction<K, K> {
        private IdentityFunction() {
        }

        @Override
        public K doCall(final K v1) throws Exception {
            return v1;
        }
    }


    public static class KeyValueObjectToTuple2<K extends Serializable, V extends Serializable> implements FlatMapFunction2<KeyValueObject<K, V>, K, V> {
        @Override
        public Iterable<V> call(final KeyValueObject<K, V> ppk, final K pK) throws Exception {
            Object[] items = {ppk.value};
            return Arrays.asList((V[]) items);
        }

    }

    /**
     * convert anRDD of KeyValueObject to a JavaPairRDD of keys and values
     *
     * @param inp input RDD
     * @param <K> key
     * @param <V> value
     * @return
     */
    @Nonnull
    public static <K extends Serializable, V extends Serializable> JavaPairRDD<K, V> toTuples(@Nonnull JavaRDD<KeyValueObject<K, V>> inp) {
        PairFunction<KeyValueObject<K, V>, K, V> pf = new AbstractLoggingPairFunction<KeyValueObject<K, V>, K, V>() {
            @Override
            public Tuple2<K, V> doCall(KeyValueObject<K, V> kv) {
                return new Tuple2<K, V>(kv.key, kv.value);
            }
        };
        return inp.mapToPair(pf);
    }


    /**
     * convert anRDD of KeyValueObject to a JavaPairRDD of keys and values
     *
     * @param inp input RDD
     * @param <K> key
     * @param <V> value
     * @return
     */
    @Nonnull
    public static <K extends Serializable, V extends Serializable> JavaRDD<KeyValueObject<K, V>> fromTuples(@Nonnull JavaPairRDD<K, V> inp) {
        return inp.map(new AbstractLoggingFunction<Tuple2<K, V>, KeyValueObject<K, V>>() {
            @Override
            public KeyValueObject<K, V> doCall(final Tuple2<K, V> t) throws Exception {
                KeyValueObject ret = new KeyValueObject(t._1(), t._2());
                return ret;
            }
        });
    }


    public static final int NUMBER_ELEMENTS_TO_VIEW = 100;


    /**
     * This partitions data and may significantly increase speed
     *
     * @param inp original rdd
     * @return
     */
    @Nonnull
    public static JavaRDD coalesce(@Nonnull final JavaRDD inp) {
        return coalesce(inp, getDefaultNumberPartitions());
    }

    /**
     * This partitions data and may significantly increase speed
     *
     * @param inp              original rdd
     * @param numberPartitions number of partitions
     * @return
     */
    @Nonnull
    public static JavaRDD coalesce(@Nonnull final JavaRDD inp, int numberPartitions) {
        return inp.coalesce(numberPartitions, false);
    }

    /**
     * This partitions data and may significantly increase speed
     *
     * @param inp original rdd
     * @return
     */
    @Nonnull
    public static <K, V> JavaPairRDD<K, V> coalesce(@Nonnull final JavaPairRDD<K, V> inp) {
        return coalesce(inp, getDefaultNumberPartitions());
    }

    /**
     * This partitions data and may significantly increase speed
     *
     * @param inp              original rdd
     * @param numberPartitions number of partitions
     * @return
     */
    @Nonnull
    public static <K, V> JavaPairRDD<K, V> coalesce(@Nonnull final JavaPairRDD<K, V> inp, int numberPartitions) {
        return inp.coalesce(numberPartitions, false);
    }


    /**
     * force a JavaRDD to evaluate then return the results as a JavaRDD
     *
     * @param inp this is an RDD - usually one you want to examine during debugging
     * @param <T> whatever inp is a list of
     * @return non-null RDD of the same values but realized
     */
    @Nonnull
    public static JavaRDD realizeAndReturn(@Nonnull final JavaRDD inp) {
        JavaSparkContext jcx = getCurrentContext();
        if (!isLocal())     // not to use on the cluster - only for debugging
            return inp;
        List collect = inp.collect();    // break here and take a look

        TestUtilities.examineInteresting(collect);

        System.out.println("Realized with " + collect.size() + " elements");
        // look at a few elements
        for (int i = 0; i < Math.min(collect.size(), NUMBER_ELEMENTS_TO_VIEW); i++) {
            Object value = collect.get(i);
            value = null; // break hera
        }
        return jcx.parallelize(collect);
    }


    /**
     * force an RDD to have defaultNumberPartitions - it if is already partitioned do nothing
     * otherwise force partition and shuffle - NOTE this may be expensive but
     * is cheaper than a poorly partitioned implementation
     *
     * @param inp input rdd
     * @param <K> key type
     * @param <V> value type
     * @return output rdd of same type and data but partitioned
     */
    @Nonnull
    public static <K extends Serializable, V> JavaPairRDD<K, V> guaranteePairedPartition(@Nonnull final JavaPairRDD<K, V> inp) {
        List<Partition> partitions = inp.partitions();
        int defaultNumberPartitions1 = getDefaultNumberPartitions();
        if (partitions.size() == defaultNumberPartitions1)
            return inp;
        boolean doShuffle = true;
        return inp.coalesce(defaultNumberPartitions1, doShuffle);
    }

    /**
     * force an RDD to have defaultNumberPartitions - it if is already partitioned do nothing
     * otherwise force partition and shuffle - NOTE this may be expensive but
     * is cheaper than a poorly partitioned implementation
     *
     * @param inp input rdd
     * @param <V> value type
     * @return output rdd of same type and data but partitioned
     */
    @Nonnull
    public static <V extends Serializable> JavaRDD<V> guaranteePartition(@Nonnull final JavaRDD<V> inp) {
        List<Partition> partitions = inp.partitions();
        int defaultNumberPartitions1 = getDefaultNumberPartitions();
        if (partitions.size() == defaultNumberPartitions1)
            return inp;
        boolean doShuffle = true;
        return inp.coalesce(defaultNumberPartitions1, doShuffle);
    }


    /**
     * force a JavaPairRDD to evaluate then return the results as a JavaPairRDD
     *
     * @param inp this is an RDD - usually one you want to examine during debugging
     * @param <T> whatever inp is a list of
     * @return non-null RDD of the same values but realized
     */
    @Nonnull
    public static <K, V> JavaPairRDD<K, V> realizeAndReturn(@Nonnull final JavaPairRDD<K, V> inp) {
        // if not local ignore
        return realizeAndReturn(inp, false);
    }


    /**
     * force a JavaPairRDD to evaluate then return the results as a JavaPairRDD
     *
     * @param inp     this is an RDD - usually one you want to examine during debugging
     * @param handler all otuples are passed here
     * @param <T>     whatever inp is a list of
     * @return non-null RDD of the same values but realized
     */
    @Nonnull
    public static <K, V> JavaPairRDD<K, V> realizeAndReturn(@Nonnull final JavaPairRDD<K, V> inp, ObjectFoundListener<Tuple2<K, V>> handler) {
        // if not local ignore
        return realizeAndReturn(inp, false);
    }


    /**
     * force a JavaPairRDD to evaluate then return the results as a JavaPairRDD
     *
     * @param inp   this is an RDD - usually one you want to examine during debugging
     * @param force only run the function on the cluster if true
     * @param <T>   whatever inp is a list of
     * @return non-null RDD of the same values but realized
     */
    @Nonnull
    public static <K, V> JavaPairRDD<K, V> realizeAndReturn(@Nonnull final JavaPairRDD<K, V> inp, boolean force) {
        JavaSparkContext jcx = getCurrentContext();
        if (force)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        if (!isLocal() && !force)    // not to use on the cluster - only for debugging
            return inp; //
        List<Tuple2<K, V>> collect = (List<Tuple2<K, V>>) (List) inp.collect();    // break here and take a look
        System.out.println("Realized with " + collect.size() + " elements");
        // look at a few elements
        for (int i = 0; i < Math.min(collect.size(), NUMBER_ELEMENTS_TO_VIEW); i++) {
            Tuple2<K, V> value = collect.get(i);
            value = null; // break hera
        }
        return (JavaPairRDD<K, V>) jcx.parallelizePairs(collect);
    }


    /**
     * force a JavaPairRDD to evaluate then return the results as a JavaPairRDD
     *
     * @param inp     this is an RDD - usually one you want to examine during debugging
     * @param force   only run the function on the cluster if true
     * @param handler all otuples are passed here
     * @param <T>     whatever inp is a list of
     * @return non-null RDD of the same values but realized
     */
    @Nonnull
    public static <K, V> JavaPairRDD<K, V> realizeAndReturn(@Nonnull final JavaPairRDD<K, V> inp, ObjectFoundListener<Tuple2<K, V>> handler, boolean force) {
        JavaSparkContext jcx = getCurrentContext();
        if (force)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        if (!isLocal() && !force)    // not to use on the cluster - only for debugging
            return inp; //
        List<Tuple2<K, V>> collect = (List<Tuple2<K, V>>) (List) inp.collect();    // break here and take a look
        for (Tuple2<K, V> kvTuple2 : collect) {
            handler.onObjectFound(kvTuple2);
        }
        return (JavaPairRDD<K, V>) jcx.parallelizePairs(collect);
    }

    /**
     * force a JavaRDD to evaluate then return the results as a JavaRDD
     *
     * @param inp     this is an RDD - usually one you want to examine during debugging
     * @param handler all objects are passed here
     * @param <T>     whatever inp is a list of
     * @return non-null RDD of the same values but realized
     */
    @Nonnull
    public static <K, V> JavaRDD<V> realizeAndReturn(@Nonnull final JavaRDD<V> inp, ObjectFoundListener<V> handler) {
        JavaSparkContext jcx = getCurrentContext();
        if (!isLocal())    // not to use on the cluster - only for debugging
            return inp; //
        List<V> collect = (List<V>) (List) inp.collect();    // break here and take a look
        for (V value : collect) {
            handler.onObjectFound(value);
        }
        return (JavaRDD<V>) jcx.parallelize(collect);
    }


    public static int getTaskID() {
        JavaSparkContext jcx = getCurrentContext();
        SparkContext sc = jcx.sc();
        TaskContext tc;
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }

    public static final StorageLevel DEFAULT_STORAGE_LEVEL = StorageLevel.MEMORY_AND_DISK();

    /**
     * persist in the best way - saves remembering which storage level
     *
     * @param inp
     * @return
     */
    @Nonnull
    public static <V> JavaRDD<V> persist(@Nonnull final JavaRDD<V> inp) {
        StorageLevel storageLevel = inp.getStorageLevel();
        if (storageLevel == null)
            storageLevel = DEFAULT_STORAGE_LEVEL;
        return inp.persist(storageLevel);
    }

    /**
     * persist in the best way - saves remembering which storage level
     *
     * @param inp
     * @return
     */
    @Nonnull
    public static <K, V> JavaRDD<Tuple2<K, V>> persistTuple(@Nonnull final JavaRDD<Tuple2<K, V>> inp) {
        StorageLevel storageLevel = inp.getStorageLevel();
        if (storageLevel == null)
            storageLevel = DEFAULT_STORAGE_LEVEL;
        return inp.persist(storageLevel);
    }

    /**
     * persist in the best way - saves remembering which storage level
     *
     * @param inp
     * @return
     */
    @Nonnull
    public static <K, V> JavaPairRDD<K, V> persist(@Nonnull final JavaPairRDD<K, V> inp) {
        StorageLevel storageLevel = inp.getStorageLevel();
        if (storageLevel == null)
            storageLevel = DEFAULT_STORAGE_LEVEL;
        return inp.persist(storageLevel);
    }

    /**
     * persist and show how a key hashes
     *
     * @param message message to show
     * @param inp     rdd
     * @return
     */
    @Nonnull
    public static <K, V> JavaPairRDD<K, V> persistAndShowHash(@Nonnull final String message, @Nonnull final JavaPairRDD<K, V> inp) {
        return persistAndShowHash(message, System.err, inp);
    }

    /**
     * persist and show count
     *
     * @param message message to show
     * @param inp     rdd
     * @return
     */
    @Nonnull
    public static <K, V> JavaPairRDD<K, V> persistAndShowHash(@Nonnull final String message, Appendable out, @Nonnull final JavaPairRDD<K, V> inp) {
        JavaPairRDD<K, V> ret = persist(inp);

        Map<K, Object> keyCounts = ret.countByKey();
        try {
            out.append(message + "\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
        showKeyHashes(ret, out);
        return ret;
    }


    public static <K, V> Map<Integer, Long> getKeyHashes(JavaPairRDD<K, V> kvx) {
        JavaRDD<Integer> hashes = kvx.keys().map(new KeyToHash<K>());

        Map<Integer, Long> counts = hashes.countByValue();
        return counts;

    }

    public static <K, V> void showKeyHashes(JavaPairRDD<K, V> kvx, Appendable out) {
        try {
            Map<Integer, Long> hashes = getKeyHashes(kvx);
            List<Integer> keys = new ArrayList<Integer>(hashes.keySet());
            Collections.sort(keys);
            for (Integer key : keys) {
                out.append(Integer.toString(key) + "  =  " + hashes.get(key) + '\n');
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    /**
     * persist and show count
     *
     * @param message message to show
     * @param inp     rdd
     * @return
     */
    @Nonnull
    public static <V> JavaRDD<V> persistAndCount(@Nonnull final String message, @Nonnull final JavaRDD<V> inp, long[] countRef) {
        JavaRDD<V> ret = persist(inp);
        long count = ret.count();
        System.err.println(message + " has " + Long_Formatter.format(count));
        countRef[0] = count;
        return ret;
    }

    /**
     * persist and show count
     *
     * @param message message to show
     * @param inp     rdd
     * @return
     */
    @Nonnull
    public static <K, V> JavaRDD<Tuple2<K, V>> persistAndCountTuple(@Nonnull final String message, @Nonnull final JavaRDD<Tuple2<K, V>> inp, long[] countRef) {
        JavaRDD<Tuple2<K, V>> ret = persistTuple(inp);
        long count = ret.count();
        System.err.println(message + " has " + Long_Formatter.format(count));
        countRef[0] = count;
        return ret;
    }

    /**
     * persist and show count
     *
     * @param message message to show
     * @param inp     rdd
     * @return
     */
    @Nonnull
    public static <K, V> JavaPairRDD<K, V> persistAndCountPair(@Nonnull final String message, @Nonnull final JavaPairRDD<K, V> inp, long[] countRef) {
        JavaPairRDD<K, V> ret = persist(inp);
        long count = ret.count();
        System.err.println(message + " has " + Long_Formatter.format(count));
        countRef[0] = count;
        return ret;
    }


    /**
     * persist and show count
     *
     * @param message message to show
     * @param inp     rdd
     * @return
     */
    @Nonnull
    public static <V> JavaRDD<V> persistAndCount(@Nonnull final String message, @Nonnull final JavaRDD<V> inp) {
        JavaRDD<V> ret = persist(inp);
        long count = ret.count();
        System.err.println(message + " has " + Long_Formatter.format(count));
        return ret;
    }

    /**
     * persist and show count
     *
     * @param message message to show
     * @param inp     rdd
     * @return
     */
    @Nonnull
    public static <K extends Serializable, V extends Serializable> JavaPairRDD<K, V> persistAndCount(@Nonnull final String message, @Nonnull final JavaPairRDD<K, V> inp) {
        JavaPairRDD<K, V> ret = persist(inp);
        long count = ret.count();
        System.err.println(message + " has " + Long_Formatter.format(count));
        return ret;
    }

    /**
     * make an RDD from an iterable
     *
     * @param inp input iterator
     * @param ctx context
     * @param <T> type
     * @return rdd from inerator as a list
     */
    public static
    @Nonnull
    <T> JavaRDD<T> fromIterable(@Nonnull final java.lang.Iterable<T> inp) {
        JavaSparkContext ctx = SparkUtilities.getCurrentContext();

        List<T> holder = new ArrayList<T>();
        for (T k : inp) {
            holder.add(k);
        }
        return ctx.parallelize(holder);
    }

    /**
     * repartition inp of get within tolerance of numberPartitions
     *
     * @param inp
     * @param <V>
     * @return
     */
    @Nonnull
    public static <K, V> JavaRDD<Tuple2<K, V>> repartitionTupleIfNeeded(@Nonnull final JavaRDD<Tuple2<K, V>> inp) {
        return repartitionTupleIfNeeded(inp, getDefaultNumberPartitions(), 0.25);
    }

    /**
     * repartition inp of get within tolerance of numberPartitions
     *
     * @param inp
     * @param numberPartitions desired partitons
     * @param tolerance        tolerance
     * @param <V>
     * @return
     */
    @Nonnull
    public static <K, V> JavaRDD<Tuple2<K, V>> repartitionTupleIfNeeded(@Nonnull final JavaRDD<Tuple2<K, V>> inp, int numberPartitions, double tolerance) {
        int currentPartitions = inp.partitions().size();
        if (numberPartitions == currentPartitions)
            return inp;
        if (numberPartitions > currentPartitions) {
            double ratio = currentPartitions / numberPartitions;
            if (Math.abs(1.0 - ratio) < tolerance)
                return inp;
        }

        System.err.println("Repartitioning Tuple from " + currentPartitions + " to " + numberPartitions);
        boolean forceRepartition = true;
        JavaRDD<Tuple2<K, V>> ret = inp.coalesce(numberPartitions, forceRepartition);
        return ret;
    }


    /**
     * repartition inp of get within tolerance of numberPartitions
     *
     * @param inp
     * @param <V>
     * @return
     */
    @Nonnull
    public static <V> JavaRDD<V> repartitionIfNeeded(@Nonnull final JavaRDD<V> inp) {
        return repartitionIfNeeded(inp, getDefaultNumberPartitions(), 0.25);
    }

    /**
     * repartition inp of get within tolerance of numberPartitions
     *
     * @param inp
     * @param numberPartitions desired partitons
     * @param tolerance        tolerance
     * @param <V>
     * @return
     */
    @Nonnull
    public static <V> JavaRDD<V> repartitionIfNeeded(@Nonnull final JavaRDD<V> inp, int numberPartitions, double tolerance) {
        int currentPartitions = inp.partitions().size();
        if (numberPartitions == currentPartitions)
            return inp;
        if (numberPartitions > currentPartitions) {
            double ratio = currentPartitions / numberPartitions;
            if (Math.abs(1.0 - ratio) < tolerance)
                return inp;
        }

        boolean forceRepartition = true;
        System.err.println("Repartitioning from " + currentPartitions + " to " + numberPartitions);
        JavaRDD<V> ret = inp.coalesce(numberPartitions, forceRepartition);
        return ret;
    }

    /**
     * repartition inp of get within tolerance of numberPartitions
     *
     * @param inp
     * @param <V>
     * @return
     */
    @Nonnull
    public static <K, V> JavaPairRDD<K, V> repartitionIfNeeded(@Nonnull final JavaPairRDD<K, V> inp) {
        return repartitionIfNeeded(inp, getDefaultNumberPartitions(), 0.25);
    }

    /**
     * repartition inp of get within tolerance of numberPartitions
     *
     * @param inp
     * @param numberPartitions desired partitons
     * @param tolerance        tolerance
     * @param <V>
     * @return
     */
    @Nonnull
    public static <K, V> JavaPairRDD<K, V> repartitionIfNeeded(@Nonnull final JavaPairRDD<K, V> inp, int numberPartitions, double tolerance) {
        int currentPartitions = inp.partitions().size();
        if (numberPartitions == currentPartitions)
            return inp;
        if (numberPartitions > currentPartitions) {
            double ratio = (double) (currentPartitions / numberPartitions);
            System.out.println("Tolerance Ration is : " + ratio);
            if (Math.abs(1.0 - ratio) < tolerance)
                return inp;
        }

        boolean forceRepartition = true;
        System.err.println("Repartitioning Pair from " + currentPartitions + " to " + numberPartitions);
        JavaPairRDD<K, V> ret = inp.coalesce(numberPartitions, forceRepartition);
        return ret;
    }


    /**
     * collector to examine RDD
     *
     * @param inp
     * @param <K>
     */
    public static void showRDD(JavaRDD inp) {
        List collect = inp.collect();
        for (Object k : collect) {
            System.out.println(k.toString());
        }
        // now we must exit
        throw new IllegalStateException("input RDD is consumed by show");
    }

    /**
     * collector to examine JavaPairRDD
     *
     * @param inp
     * @param <K>
     */
    public static void showPairRDD(JavaPairRDD inp) {
        inp = persist(inp);
        List<Tuple2> collect = inp.collect();
        for (Tuple2 kvTuple2 : collect) {
            System.out.println(kvTuple2._1().toString() + " : " + kvTuple2._2().toString());
        }
        // now we must exit
        //  throw new IllegalStateException("input RDD is consumed by show");
    }


    /**
     * convert an iterable of KeyValueObject (never heard of Spark) into an iterable of Tuple2
     *
     * @param inp
     * @param <K> key
     * @param <V>
     * @return
     */
    public static
    @Nonnull
    <K extends java.io.Serializable, V extends java.io.Serializable> Iterable<Tuple2<K, V>> toTuples(@Nonnull Iterable<KeyValueObject<K, V>> inp) {
        final Iterator<KeyValueObject<K, V>> originalIterator = inp.iterator();
        return new Iterable<Tuple2<K, V>>() {
            @Override
            public Iterator<Tuple2<K, V>> iterator() {
                return new Iterator<Tuple2<K, V>>() {
                    @Override
                    public boolean hasNext() {
                        return originalIterator.hasNext();
                    }

                    @Override
                    public Tuple2<K, V> next() {
                        KeyValueObject<K, V> next = originalIterator.next();
                        return new Tuple2(next.key, next.value);
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("not supported");
                    }
                };
            }
        };
    }

    /**
     * convert an iterable of Tuple2 (never heard of Spark) into an iterable of KeyValueObject
     *
     * @param inp
     * @param <K> key
     * @param <V>
     * @return
     */
    public static
    @Nonnull
    <K extends java.io.Serializable, V extends java.io.Serializable> Iterable<KeyValueObject<K, V>> toKeyValueObject(@Nonnull Iterable<Tuple2<K, V>> inp) {
        final Iterator<Tuple2<K, V>> originalIterator = inp.iterator();
        return new Iterable<KeyValueObject<K, V>>() {
            @Override
            public Iterator<KeyValueObject<K, V>> iterator() {
                return new Iterator<KeyValueObject<K, V>>() {
                    @Override
                    public boolean hasNext() {
                        return originalIterator.hasNext();
                    }

                    @Override
                    public KeyValueObject<K, V> next() {
                        Tuple2<K, V> next = originalIterator.next();
                        return new KeyValueObject(next._1(), next._2());
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("not supported");
                    }
                };
            }
        };
    }


    /**
     * return a key representing the sort index of some value
     *
     * @param values
     * @param <K>
     * @return
     */
    public static <K extends Serializable> JavaPairRDD<Integer, K> indexByOrder(JavaRDD<K> values) {
        values = values.sortBy(new Function<K, K>() {
                                   @Override
                                   public K call(final K v1) throws Exception {
                                       return v1;
                                   }
                               }, true,
                getDefaultNumberPartitions()
        );
        return values.mapToPair(new PairFunction<K, Integer, K>() {
            private int index = 0;

            @Override
            public Tuple2<Integer, K> call(final K t) throws Exception {
                return new Tuple2<Integer, K>(index++, t);
            }
        });
    }

    /**
     * use as a properties file when logging
     *
     * @param mainClass
     * @param args
     * @return
     */
    public static String buildLoggingClassLoaderPropertiesFile(Class mainClass, String[] args) {
        StringBuilder sb = new StringBuilder();
        sb.append("#\n" +
                "# classpath - it is a good ides to drop any of the java jars\n");
        String classPath = System.getProperty("java.class.path");
        String classPathSeparator = System.getProperty("path.separator");
        String[] items = classPath.split(classPathSeparator);
        sb.append("classpath = ");
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            sb.append("   " + item);
            if (i < items.length - 1)
                sb.append(";\\\n");
            else
                sb.append("\n");
        }
        sb.append("\n");

        sb.append("classpath_excludes=*IntelliJ IDEA*\n" +
                "\n" +
                "#\n" +
                "# if specified this will be the main in the mainfest\n");
        sb.append("mainclass=" + mainClass.getCanonicalName() + "\n");

        sb.append("#\n" +
                "# if specified run the program using this user directory\n" +
                "user_dir=" + System.getProperty("user.dir").replace("\\", "/") + "\n");

        sb.append("#\n" +
                "# if specified the main will run with these arguments\n" +
                "arguments =");

        for (int i = 0; i < args.length; i++) {
            String item = items[i];
            sb.append(item + " ");
        }
        sb.append("\n");

        return sb.toString();
    }

    private transient static String macAddress;

    /**
     * identify the machine we are running on
     *
     * @return String representing a Mac address
     * @see http://www.mkyong.com/java/how-to-get-mac-address-in-java/
     */
    public static String getMacAddress() {
        if (macAddress != null)
            return macAddress;
        InetAddress ip;
        try {

            ip = InetAddress.getLocalHost();
            // System.out.println("Current IP address : " + ip.getHostAddress());

            NetworkInterface network = NetworkInterface.getByInetAddress(ip);

            byte[] mac = network.getHardwareAddress();

            if (mac == null) {
                mac = new byte[4];  // fake it if needed
                mac[0] = 127;
                mac[3] = 1;
            }


            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
            }
            macAddress = sb.toString();
            return macAddress;

        }
        catch (Exception e) {
            throw new RuntimeException(e); // should never happen
        }
    }

    public static final long ONE_THOUSAND = 1000L;
    public static final long ONE_MILLION = 1000 * ONE_THOUSAND;
    public static final long ONE_BILLION = 1000 * ONE_MILLION;

    /**
     * write integers in an easier way than a large number of digits
     *
     * @param n number
     * @return string might be 1234, 30K  45M ..
     */
    public static String formatLargeNumber(long realN) {
        long n = realN;

        if (n < 20 * ONE_THOUSAND)
            return java.lang.Long.toString(n);

        n /= ONE_THOUSAND;
        if (n < 20 * ONE_THOUSAND)
            return java.lang.Long.toString(n) + "K";

        n /= ONE_THOUSAND;

        if (n < 20 * ONE_THOUSAND)
            return java.lang.Long.toString(n) + "M";

        n /= ONE_THOUSAND;
        return java.lang.Long.toString(n) + "G";
    }

    public static final double MILLISEC_IN_NANOSEC = 1000 * 1000;
    public static final double SEC_IN_NANOSEC = MILLISEC_IN_NANOSEC * 1000;
    public static final double MIN_IN_NANOSEC = SEC_IN_NANOSEC * 60;
    public static final double HOUR_IN_NANOSEC = MIN_IN_NANOSEC * 60;
    public static final double DAY_IN_NANOSEC = HOUR_IN_NANOSEC * 24;

    public static String formatNanosec(long timeNanosec) {
        if (timeNanosec < 10 * SEC_IN_NANOSEC)
            return String.format("%10.2f", timeNanosec / MILLISEC_IN_NANOSEC) + " msec";
        if (timeNanosec < 10 * MIN_IN_NANOSEC)
            return String.format("%10.2f", timeNanosec / SEC_IN_NANOSEC) + " sec";
        if (timeNanosec < 10 * HOUR_IN_NANOSEC)
            return String.format("%10.2f", timeNanosec / MIN_IN_NANOSEC) + " min";
        if (timeNanosec < 10 * DAY_IN_NANOSEC)
            return String.format("%10.2f", timeNanosec / HOUR_IN_NANOSEC) + " hour";
        return String.format("%10.2f", timeNanosec / DAY_IN_NANOSEC) + " days";
    }


    public static final int MAX_COUNTS_SHOWN = 10;

    public static <K, V> void showCounts(JavaPairRDD<K, V> binPairs) {
        Map<K, Object> counts = binPairs.countByKey();
        List<CountedItem> holder = new ArrayList<CountedItem>();
        for (K key : counts.keySet()) {
            Object countObj = counts.get(key);
            String keyStr = key.toString();
            long count = java.lang.Long.parseLong(countObj.toString());
            holder.add(new CountedItem(keyStr, count));
        }
        Collections.sort(holder);
        int shown = 0;
        for (CountedItem countedItem : holder) {
            System.err.println(countedItem.getValue() + " " + countedItem.getValue());
            if (shown++ > MAX_COUNTS_SHOWN)
                break;
        }


    }


    public static <T extends Serializable> JavaRDD<T> getFirstN(final long n, JavaRDD<T> inp) {
        return inp.filter(new FilterFirstN<T>(n));
    }

    /**
     * return a fraction of the original RDD
     *
     * @param fraction
     * @param inp
     * @param <T>
     * @return
     */
    public static <T extends Serializable> JavaRDD<T> getFraction(double fraction, JavaRDD<T> inp) {
        return inp.filter(new FilterRandomFraction<T>(fraction));
    }

    private static class FilterFirstN<T extends Serializable> extends AbstractLoggingFunction<T, Boolean> {
        private final long maxSaved;
        private long numberSaved;

        private FilterFirstN(long n) {
            maxSaved = n;
        }

        @Override
        public Boolean doCall(final T v1) throws Exception {
            return maxSaved > numberSaved++;
        }
    }

    private static class FilterRandomFraction<T extends Serializable> extends AbstractLoggingFunction<T, Boolean> {
        private final double fractionSaved;
        private final Random rnd = new Random();

        private FilterRandomFraction(double n) {
            fractionSaved = n;
        }

        @Override
        public Boolean doCall(final T v1) throws Exception {
            return rnd.nextDouble() < fractionSaved;
        }
    }

    /**
     * Follows code in http://www.tutorialspoint.com/java/java_serialization.htm
     * finds object size by serializing it - do not call very ofter but
     * useful in determining Spark impact in terms of memory
     *
     * @param test
     * @return
     */
    public static int objectSize(@Nonnull final Object test) {
        try {
            ByteArrayOutputStream fileOut =
                    new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(test);
            out.close();
            return fileOut.toByteArray().length;
        }
        catch (IOException i) {
            throw new RuntimeException(i);
        }
    }

    /**
     * if there are multiple values per key choose the first
     *
     * @param inp
     * @param <K>
     * @param <R>
     * @return
     */
    public static <K extends Serializable, R extends Serializable> JavaPairRDD<K, R> chooseOneValue(JavaPairRDD<K, R> inp) {
        return inp.reduceByKey(new Function2<R, R, R>() {
            @Override
            public R call(final R v1, final R v2) throws Exception {
                return v1;
            }
        });
    }

    public static <K, V> JavaPairRDD<K, V> saveAsSequenceFile(String path, JavaPairRDD<K, V> inp) {
        try {
            inp = persist(inp);
            Tuple2<K, V> first = inp.first();
            if (first == null)
                return inp;
            Class keyClass = first._1().getClass();
            Class valueClass = first._2().getClass();
            Class outputFormatClass = SequenceFileOutputFormat.class;

            inp.saveAsNewAPIHadoopFile(path, keyClass, valueClass, outputFormatClass);
            return inp;
        }
        catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    public static <K, V> JavaPairRDD<K, V> readAsSequenceFile(String path, Class<? extends K> keyClass, Class<? extends V> valueClass) {
        try {
            Class inputFormatClass =  SequenceFileInputFormat.class;
            JavaSparkContext currentContext = getCurrentContext();
            return currentContext.newAPIHadoopFile(path, inputFormatClass, keyClass, valueClass,getHadoopConfiguration());
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <K,V> Map<K,V>  mapFromTupleList(List<Tuple2<K, V>> inp)
    {
        HashMap<K,V> ret = new HashMap<K, V>() ;
        for (Tuple2<K, V> kvTuple2 : inp) {
            ret.put(kvTuple2._1(),kvTuple2._2());
        }
        return ret;
    }


    public static <K,V> boolean  equivalent(Map<K,V> m1,Map<K,V> m2)
    {
         if(m1.size() != m2.size())
             return false;
        for (K k : m1.keySet()) {
            Object o1 = m1.get(k);
            Object o2 = m2.get(k);
            if(o2 == null)
                return false;
            if(!o1.equals(o2))
                return false;

        }
        return true;
    }

    public static class KeyToHash<K> implements Function<K, Integer> {

        @Override
        public Integer call(final K v1) throws Exception {
            int hc = v1.hashCode();
            int hash = Math.abs(hc) % getDefaultNumberPartitions();
            return hash;
        }
    }
}


