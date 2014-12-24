package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.util.*;
import com.lordjoe.distributed.wordcount.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.examples.*;
import org.systemsbiology.xtandem.hadoop.*;
import scala.*;

import java.util.*;


/**
 * com.lordjoe.distributed.hydra.SimpleLoadTest
 * User: Steve
 * Date: 10/7/2014
 */
public class SimpleLoadTest {


    public static final int SPARK_CONFIG_INDEX = 0;
    public static final int INPUT_FILE_INDEX = 1;

    /**
     * call with args like or20080320_s_silac-lh_1-1_11short.mzxml in Sample2
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {

        if (args.length < INPUT_FILE_INDEX + 1) {
            System.out.println("usage SimpleLoadTest Spark.properties textToCount");
            return;
        }
       SparkUtilities.readSparkProperties(args[SPARK_CONFIG_INDEX]);
        Properties sparkProperties = SparkUtilities.getSparkProperties();
        String pathPrepend = sparkProperties.getProperty("com.lordjoe.distributed.PathPrepend") ;
        if(pathPrepend != null)
            XTandemHadoopUtilities.setDefaultPath(pathPrepend);

        SparkUtilities.setAppName("Test Largely to make sure cluster can load");

         JavaSparkContext ctx = SparkUtilities.getCurrentContext();

         String inputPath = SparkUtilities.buildPath(args[INPUT_FILE_INDEX] );
         JavaRDD<String> lines = ctx.textFile(inputPath, 1);

         // use my function not theirs
         JavaRDD<String> words = lines.flatMap(new WordsMapFunction());


         JavaPairRDD<String, Integer> ones = words.mapToPair(new PairFunction<String, String, Integer>() {
             @Override
             public Tuple2<String, Integer> call(String s) {
                 return new Tuple2<String, Integer>(s, 1);
             }
         });

          JavaPairRDD<String, Integer> sorted = ones.sortByKey();
         JavaRDD<WordNumber> answer = sorted.mapPartitions(new WordCountFlatMapFunction());

         List<WordNumber> objects = answer.toArray();
         for (WordNumber o : objects) {
             System.out.println(o);
         }

    }


}
