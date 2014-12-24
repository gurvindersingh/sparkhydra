package com.lordjoe.distributed.test;

import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.hadoopgenerated.*;

import javax.annotation.*;
import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.test.GenerateAndSort
 * User: Steve
 * Date: 10/6/2014
 */
public class GenerateAndSort {

    public static final Random RND = new Random();

    public class GenerateRandomNumbers implements Serializable, Iterator<Integer> {
        private final long maxToGenerate;
        private long generated;
        private final Random RND = new Random();

        public GenerateRandomNumbers(final int pMaxToGenerate) {
            maxToGenerate = pMaxToGenerate;
        }

        @Override
        public boolean hasNext() {
            return generated++ >= maxToGenerate;
        }

        @Override
        public Integer next() {
            return RND.nextInt();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("unsupported");
        }
    }

    public static class NShotInputFormat extends AbstractNShotInputFormat<Integer, Integer> {
        private static final int maxValue = 1000;
        private static final long numberGenerated = 10  * 1000 * 1000 * 1000L;

        private final Random RND = new Random();
        public NShotInputFormat() {
            setNumberKeys(numberGenerated);
        }

        /**
         * Implement to generate a reasonable key
         *
         * @param index current key index
         * @return non-null key
         */
        @Override
        protected Integer getKeyFromIndex(long index) {
            return RND.nextInt(maxValue);
        }

        /**
         * Implement to generate a ressomable key
         *
         * @param index current key index
         * @return non-null key
         */
        @Override
        protected Integer getValueFromIndex(long index) {
            return 1;
        }

    }


    @Nonnull
    public static JavaPairRDD<Integer, Integer> generateIntegers(  @Nonnull final JavaSparkContext ctx) {
        Class inputFormatClass = NShotInputFormat.class;
        Class keyClass = Integer.class;
        Class valueClass = Integer.class;
        String path = System.getProperty("user.dir");

        JavaPairRDD<Integer, Integer> generatedIntegers = ctx.newAPIHadoopFile(
                path,
                inputFormatClass,
                keyClass,
                valueClass,
                ctx.hadoopConfiguration()
        );
        return generatedIntegers;
    }

    public static void main(String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("GenerateAndSort");
        sparkConf.set("spark.mesos.coarse", "true");
        scala.Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined())   // use local over nothing
            sparkConf.setMaster("local");


        JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        long start = System.currentTimeMillis();

        JavaPairRDD<Integer, Integer> randoms = generateIntegers( ctx);
        Map<Integer, Object> integerObjectMap = randoms.countByKey();
        long end = System.currentTimeMillis();
        System.out.println("Finished in " + (end - start) /1000 + " sec" );
        for (Integer key : integerObjectMap.keySet()) {
            System.out.println(key + " " + integerObjectMap.get(key));
        }
    }

}
