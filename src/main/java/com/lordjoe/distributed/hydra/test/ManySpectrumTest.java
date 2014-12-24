package com.lordjoe.distributed.hydra.test;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.spark.*;
import com.lordjoe.utilities.*;
import org.apache.log4j.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.storage.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.scoring.*;
import scala.*;

import javax.annotation.*;

/**
 * com.lordjoe.distributed.hydra.test.ManySpectrumTest
 * User: Steve
 * Date: 12/10/2014
 */
public class ManySpectrumTest {

    public static final int NUMBER_TEST_PEAKS = 50;

    public static class MeasuredSpectrumGenerator implements ObjectGenerator<IMeasuredSpectrum> {
        /**
         * create an instance of the known type
         *
         * @return
         */
        @Nonnull
        @Override
        public IMeasuredSpectrum generateObject() {
            return ScoringTestUtilities.buildTestSpectrum(NUMBER_TEST_PEAKS);
        }
    }

    public static JavaSparkContext buildJavaSparContext(String appName) {
        SparkConf sparkConf = new SparkConf().setAppName(appName);
        Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined())    // use local over nothing
            sparkConf.setMaster("local[*]");
        else
            sparkConf.set("spark.shuffle.manager", "HASH");

        sparkConf.set("spark.mesos.coarse", "true");  // always allow a job to be killed
        sparkConf.set("spark.ui.killEnabled", "true");  // always allow a job to be killed
        //      sparkConf.set("spark.mesos.executor.memoryOverhead","1G");
        sparkConf.set("spark.executor.memory", "4G");
        sparkConf.set("spark.task.cpus", "4");
        sparkConf.set("spark.default.parallelism", "120");
        return new JavaSparkContext(sparkConf);
    }

    public static final int ONE_K = 1024;
    public static final int ONE_MEG = ONE_K * 1024;
     public static final int ONE_GIG = ONE_MEG * 1024;

    /**
     * spark-submit --class com.lordjoe.distributed.hydra.test.ManySpectrumTest ~/SparkJar.jar    ~/SparkCluster.properties   1000 -executor-memory 12G


     * @param args
     */
    public static void main(String[] args) {
            ElapsedTimer timer = new ElapsedTimer();
        ElapsedTimer totalTime = new ElapsedTimer();

        if (args.length < 2 ) {
            System.out.println("usage ManySpectrumTest configFile number");
            return;
        }
        SparkUtilities.readSparkProperties(args[0]);
        int numberSpectra = ONE_K * Integer.parseInt(args[1]) ;

        System.out.println("Set Log to Warn");
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.WARN);

         JavaSparkContext ctx = buildJavaSparContext("Generate Spectra Test");

        JavaRDD<IMeasuredSpectrum> spectra = RDDGenerator.generateRDD(ctx, new MeasuredSpectrumGenerator(), numberSpectra);


        spectra = spectra.persist(StorageLevel.DISK_ONLY());
        long count = spectra.count();
        System.out.println("generated " + count / ONE_K + "k spectra ");

    }


}
