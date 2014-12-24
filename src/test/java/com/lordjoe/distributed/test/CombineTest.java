package com.lordjoe.distributed.test;

import com.lordjoe.distributed.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import scala.*;

import java.util.*;
import java.util.regex.*;

/**
 * com.lordjoe.distributed.test.CombineTest
 * User: Steve
 * Date: 10/8/2014
 */
public class CombineTest {
    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main(String[] args) {

        if (args.length < 1) {
            System.err.println("Usage: CombineTest <file>");
            return;
        }


        SparkConf sparkConf = new SparkConf().setAppName("CombineTest");
        sparkConf.set("spark.mesos.coarse", "true");
        scala.Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined())   // use local over nothing
            sparkConf.setMaster("local");


        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        JavaRDD<String> lines = ctx.textFile(args[0], 1);
        // use my function not theirs
        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(String s) {
                String[] split = SPACE.split(s);
                for (int i = 0; i < split.length; i++) {
                    String trim = split[i].trim();
                    split[i] = trim.toUpperCase();
                }
                return Arrays.asList(split);
            }
        });

        /**
         * Now we have words and ones like WordCount likes
         */
        JavaPairRDD<String, Integer> ones = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) {
                return new Tuple2<String,Integer>(s,1);
            }
        });

        JavaPairRDD<String, Tuple2<String, Integer>> kkv = ones.mapToPair(new KeyTuplePairFunction<String, Integer>());

        JavaPairRDD<String, KeyAndValues<String, Integer>> reducedSets = KeyAndValues.combineByKey(kkv);


        JavaPairRDD<String, KeyAndValues<String, Integer>> grouped = reducedSets.sortByKey();


        JavaPairRDD<String,Integer> summed = grouped.mapToPair(new PairFunction<Tuple2<String, KeyAndValues<String, Integer>>, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(final Tuple2<String, KeyAndValues<String, Integer>> t) throws Exception {
                KeyAndValues<String, Integer> values = t._2();
                return new Tuple2(t._1(), values.size());
            }
        });

        List<Tuple2<String, Integer>> collect = summed.collect();
        for (Tuple2<?, ?> tuple : collect) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }

    }

}
