package com.lordjoe.distributed.wordcount;

import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.api.java.function.Function2;
import scala.*;

import java.util.*;
import java.util.regex.*;

/**
 * com.lordjoe.distributed.wordcount.ExampleWordCount
 * Demonstration of custom accumulators
 * User: Steve
 * Date: 9/2/2014
 */
public class ExampleSetAccumulator {
    private static final Pattern SPACE = Pattern.compile(" ");

    public static void main(final String[] args) {
        SparkConf sparkConf = new SparkConf().setAppName("Sample Accumulator");
         // if not on a cluster then run locally
        Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined()) {   // use local over nothing
            sparkConf.setMaster("local[*]");
        }
        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
        // make an accumulator
        final Accumulator<Set<String>> wordsUsed = ctx.accumulator(new HashSet<String>(),
                new SetAccumulableParam());
        JavaRDD<String> lines = ctx.textFile(args[0]);
        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            public Iterable<String> call(String s) {
                List<String> stringList = Arrays.asList(s.split(" "));
                wordsUsed.add(new HashSet<String>(stringList)); // accumulate words
                return stringList;
            }
        });
        JavaPairRDD<String, Integer> pairs = words.mapToPair(new PairFunction<String, String, Integer>() {
            public Tuple2<String, Integer> call(String s) {
                return new Tuple2<String, Integer>(s, 1);
            }
        });
        JavaPairRDD<String, Integer> counts = pairs.reduceByKey(new Function2<Integer, Integer, Integer>() {
            public Integer call(Integer a, Integer b) {
                return a + b;
            }
        });
        counts = counts.sortByKey();
        List<Tuple2<String, Integer>> collect = counts.collect(); // make the count happen
        for (Tuple2<String, Integer> wordAndCount : collect) {
            System.out.println(wordAndCount._1() + "\t" + wordAndCount._2());
        }
    }

    public static class SetAccumulableParam implements AccumulatorParam<Set<String>>, Serializable {
        @Override
        public Set<String> addAccumulator(final Set<String> r, final Set<String> t) {
            HashSet<String> ret = new HashSet<String>(r);
            ret.addAll(t);
            return ret;
        }

        @Override
        public Set<String> addInPlace(final Set<String> r1, final Set<String> r2) {
            return addAccumulator(r1, r2);
        }

        @Override
        public Set<String> zero(final Set<String> initialValue) {
            return initialValue;
        }
    }


}
