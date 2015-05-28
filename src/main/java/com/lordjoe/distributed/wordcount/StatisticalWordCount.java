package com.lordjoe.distributed.wordcount;

import com.lordjoe.distributed.spark.*;
import com.lordjoe.distributed.spark.accumulators.LongAccumulableParam;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.api.java.function.Function2;
import scala.*;

import java.lang.Long;
import java.util.*;
import java.util.regex.*;

/**
 * com.lordjoe.distributed.wordcount.StatisticalWordCount
 * Demonstration of custom accumulators
 * User: Steve
 * Date: 9/2/2014
 */
public class StatisticalWordCount {
    private static final Pattern SPACE = Pattern.compile(" ");

    /**
     * a filter for word count - drops case and punctuation
     *
     * @param inp
     * @return
     */
    public static String regularizeString(String inp) {
        String s = inp.trim();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c))
                sb.append(Character.toUpperCase(c));
        }
        return sb.toString();
    }


    /**
     * Usage - args[0] is the name of a file to count words
     * like
     * RedBadge.txt
     * @param args
     */
    public static void main(final String[] args) {

        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("StatisticalWordCount");

        Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined()) {   // use local over nothing
            sparkConf.setMaster("local[*]");
        }
        JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        // Make two accumulators using Statistics
        final Accumulator<Statistics> lineStats = ctx.accumulator(Statistics.ZERO, "Line Statistics", Statistics.PARAM_INSTANCE);
        final Accumulator<Statistics> letterStats = ctx.accumulator(Statistics.ZERO, "Letter Statistics", Statistics.PARAM_INSTANCE);

        final Accumulator<Long> totalWords = ctx.accumulator(0L, "Word Statistics", LongAccumulableParam.INSTANCE);       // Make a simple Long Accumulator
        final Accumulator<Long> totalLetters = ctx.accumulator(0L, "Letter Count", LongAccumulableParam.INSTANCE);       // Make a simple Long Accumulator

        JavaRDD<String> lines = ctx.textFile(args[0], 4);

        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(final String s) throws Exception {
                // Handle accumulator here
                totalLetters.add((long)s.length()); // count all letters
                lineStats.add(new Statistics(s.length()));    // keep statistics on letters
                String[] split = SPACE.split(s);
                List<String> ret = new ArrayList<String>();
                for (int i = 0; i < split.length; i++) {
                    String nextWord = regularizeString(split[i]);
                    if (nextWord.length() > 0) {
                        ret.add(nextWord);
                        // keep statistics
                        // Handle accumulator here
                        totalWords.add(1L);  // accumulate total words
                        letterStats.add(new Statistics(nextWord.length()));    // keep statistics on letters
                    }
                }
                return ret;
            }
        });


        // Standard go to word pairs
        JavaPairRDD<String, Integer> ones = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) {
                return new Tuple2<String, Integer>(s, 1);
            }
        });

        // add values
        JavaPairRDD<String, Integer> counts = ones.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });

        // This line is critical to force work to happen
        List<Tuple2<String, Integer>> collect = counts.collect(); // make the count happen


        Long wordCount = totalWords.value();
        System.out.println("total words = " + wordCount);

        Long letterCount = totalLetters.value();
        System.out.println("total letters including unused = " + letterCount);
        System.out.println();


        Statistics value = letterStats.value();
        System.out.println("total words from statistics = " + value.getNumber());
        System.out.println("average letters per word = " + String.format("%10.2f", value.getAverage()));
        System.out.println("Sd average letters per word = " + String.format("%10.2f", value.getStandardDeviation()));
        System.out.println("Max letters per word = " + value.getMax());
        System.out.println();

        value = lineStats.value();
        System.out.println("total lines  = " + value.getNumber());
        System.out.println("average line length = " + String.format("%10.2f", value.getAverage()));
        System.out.println("Sd average line length = " + String.format("%10.2f", value.getStandardDeviation()));
        System.out.println("Max line length = " + value.getMax());

    }
}
