package com.lordjoe.distributed;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.sql.api.java.*;
import org.systemsbiology.common.*;
import org.systemsbiology.hadoop.*;
import scala.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.SparkContextGetter
 * A very useful class representing a number of static functions useful in Spark
 * User: Steve
 * Date: 8/28/2014
 */
public class SparkContextGetter {

    //  private transient static ThreadLocal<JavaSparkContext> threadContext;
    private transient static JavaSparkContext threadContext;
    //  private transient static ThreadLocal<JavaSQLContext> threadContext;
    private transient static JavaSQLContext sqlContext;
    private transient static boolean local;

    public static boolean isLocal() {
        return local;
    }

    public static void setLocal(final boolean pLocal) {
        local = pLocal;
    }

    public static final int DEFAULT_NUMBER_PARTITIONS = 20;
    private static int defaultNumberPartitions = DEFAULT_NUMBER_PARTITIONS;

    public static int getDefaultNumberPartitions() {
        return defaultNumberPartitions;
    }

    public static void setDefaultNumberPartitions(final int pDefaultNumberPartitions) {
        defaultNumberPartitions = pDefaultNumberPartitions;
    }

    public static synchronized JavaSQLContext getCurrentSQLContext() {
        if (sqlContext != null)
            return sqlContext;

        sqlContext = new JavaSQLContext(getCurrentContext());
        return sqlContext;
    }


    public static synchronized Configuration getHadoopConfiguration() {
        Configuration configuration = getCurrentContext().hadoopConfiguration();
        return configuration;
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
        sparkConf.set("spark.kryo.registrator", "com.lordjoe.distributed.hydra.HydraKryoSerializer");
        sparkConf.set("mySparkProperty", "xyzzy");
        sparkConf.setAppName("Some App");
        //  SparkUtilities.guaranteeSparkMaster(sparkConf);

        /*
         SparkContext sc = new SparkContext(sparkConf);
          System.err.println("App Name " + sc.appName() );
         String reg = sc.getConf().get("spark.kryo.registrator");
         System.out.println("Registrar = " + reg);
           */

        // if we use Kryo register classes
        // sparkConf.set("spark.kryo.registrator", "com.lordjoe.distributed.hydra.HydraKryoSerializer");


        sparkConf.set("spark.mesos.coarse", "true");
        sparkConf.set("spark.executor.memory", "2500m");

        SparkContextGetter.guaranteeSparkMaster(sparkConf);

        showSparkProperties();
        showSparkPropertiesInAnotherThread();

        ret = new JavaSparkContext(sparkConf);
        threadContext = ret;
        //      threadContext.set(ret);
        return ret;
    }

    /**
     * dump all spack properties fo System.err
     */
    public static void showSparkProperties() {
        SparkConf sparkConf = new SparkConf();
        Tuple2<String, String>[] all = sparkConf.getAll();
        for (Tuple2<String, String> prp : all) {
            System.err.println(prp._1().toString() + "=" + prp._2());
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
     * return a reference to the current hadoop file system using the currrent Spark Context
     */
    public static IFileSystem getHadoopFileSystem() {
        final IFileSystem accessor;
        try {
            JavaSparkContext ctx = getCurrentContext();
            Configuration entries = ctx.hadoopConfiguration();
            accessor = new HDFSAccessor(FileSystem.get(entries));
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
        return accessor;
    }


    /**
     * if no spark master is  defined then use "local
     *
     * @param sparkConf the configuration
     */
    public static void guaranteeSparkMaster(@Nonnull SparkConf sparkConf) {
        Option<String> option = sparkConf.getOption("spark.master");

        if (!option.isDefined()) {   // use local over nothing   {
            sparkConf.setMaster("local[*]");
            setLocal(true);
            /**
             * liquanpei@gmail.com suggests to correct
             * 14/10/08 09:36:35 ERROR broadcast.TorrentBroadcast: Reading broadcast variable 0 failed
             14/10/08 09:36:35 INFO broadcast.TorrentBroadcast: Reading broadcast variable 0 took 5.006378813 s
             14/10/08 09:36:35 INFO broadcast.TorrentBroadcast: Started reading broadcast variable 0
             14/10/08 09:36:35 ERROR executor.Executor: Exception in task 0.0 in stage 0.0 (TID 0)
             java.lang.NullPointerException
             at java.nio.ByteBuffer.wrap(ByteBuffer.java:392)
             at org.apache.spark.scheduler.ResultTask.runTask(ResultTask.scala:58)

             */
            //  sparkConf.set("spark.broadcast.factory","org.apache.spark.broadcast.HttpBroadcastFactory" );
        }
        else {
            setLocal(option.get().startsWith("local"));
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


}
