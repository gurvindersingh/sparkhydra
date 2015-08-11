/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.examples;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.spark.*;
import com.lordjoe.distributed.spark.accumulators.*;
import com.lordjoe.distributed.util.*;
import com.lordjoe.utilities.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import scala.*;

import java.util.*;
import java.util.regex.*;

public final class
        JavaLargeWordCount {
    private static final Pattern SPACE = Pattern.compile(" ");

    public static class PartitionByStart extends Partitioner {
        @Override
        public int numPartitions() {
            return 26;
        }

        @Override
        public int getPartition(final Object key) {
            String s = (String) key;
            if (s.length() == 0)
                throw new IllegalStateException("problem"); // ToDo change
            int ret = s.charAt(0) - 'A';
            ret = Math.min(25, ret);
            ret = Math.max(0, ret);
            return  ret;
        }
    }

    public static final int SPARK_CONFIG_INDEX = 0;
    public static final int INPUT_FILE_INDEX = 1;

    public static final int NUMBER_PARTITIONS = 11;
    /**
     * spark-submit --class  org.apache.spark.examples.JavaLargeWordCount SparkJar.jar   SparkCluster.properties  war_and_peace.txt
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        ElapsedTimer totalTime = new ElapsedTimer();
        if (args.length < INPUT_FILE_INDEX + 1) {
            System.err.println("Usage: SparkProperties JavaWordCount <file>");
            return;
        }

        System.out.println("Running on " + SparkUtilities.getMacAddress());

        SparkUtilities.readSparkProperties(args[SPARK_CONFIG_INDEX]);
        SparkUtilities.setAppName("JavaWordCount");

        JavaSparkContext ctx = SparkUtilities.getCurrentContext();


        // Add some accumulators  NOTE functions extending AbstractLoggingFunctionBase register automatically
        //   SparkAccumulators.createAccumulator("WordsMapFunction");
        AccumulatorUtilities.getInstance().createAccumulator("TotalLetters");
        final Accumulator<Statistics> letterStats = ctx.accumulator(Statistics.ZERO, "Letter Statistics", Statistics.PARAM_INSTANCE);
        final Accumulator<java.lang.Long> totalWords = ctx.accumulator(0L, "Word Statistics", LongAccumulableParam.INSTANCE);

        String inputPath = SparkUtilities.buildPath(args[INPUT_FILE_INDEX]);
        JavaRDD<String> lines = ctx.textFile(inputPath, 1);


  //     lines = SparkUtilities.persistAndCount("lines",lines);

   //     lines = lines.repartition(NUMBER_PARTITIONS);

        // use my function not theirs
        JavaRDD<String> words = lines.flatMap(new WordsMapFunction());

        words = words.repartition(NUMBER_PARTITIONS);



        JavaPairRDD<String, Integer> ones = words.flatMapToPair(new PairFlatMapFunction<String, String, Integer>() {
            @Override
            public Iterable<Tuple2<String, Integer>> call(final String t) throws Exception {
                List<Tuple2<String, Integer>> holder = new ArrayList<Tuple2<String, Integer>>();
                if(!t.startsWith("Z"))
                    holder.add(new Tuple2<String, Integer>(t, 1));
                      return holder;
            }
        });


//        JavaPairRDD<String, Integer> ones = words.mapToPair(new PairFunction<String, String, Integer>() {
//            @Override
//            public Tuple2<String, Integer> call(String s) {
//                return new Tuple2<String, Integer>(s, 1);
//            }
//        });


        ones = ones.partitionBy(new PartitionByStart());
        JavaPairRDD<String, Integer> sorted = ones.sortByKey();
        sorted = sorted.partitionBy(new PartitionByStart());
        //  JavaRDD<WordNumber> answer = sorted.mapPartitions(new WordCountFlatMapFunction());
        JavaRDD<WordNumber> answer = sorted.mapPartitions(
                new FlatMapFunction<Iterator<Tuple2<String, Integer>>, WordNumber>() {
                    @Override
                    public Iterable<WordNumber> call(final Iterator<Tuple2<String, Integer>> t) throws Exception {
                        int sum = 0;
                        String word = null;
                        List<WordNumber> ret = new ArrayList<WordNumber>();
                        while (t.hasNext()) {

                            Tuple2<String, Integer> next = t.next();
                            String s = next._1();
                            if (s.length() == 0)
                                continue;
                            if (word == null) {
                                word = s;
                            }
                            else {
                                if (!word.equals(s)) {
                                    ret.add(new WordNumber(word, sum));
                                    totalWords.add(1L);  // accumulate total words
                                    letterStats.add(new Statistics(s.length()));    // keep statistics on letters

                                    sum = 1;
                                    word = s;
                                }
                            }
                            sum += next._2();
                        }
                        return ret;
                    }
                }
        );

        List<WordNumber> answers = answer.collect();

        List<WordNumber> objects = answer.toArray();
        for (WordNumber o : objects) {
            System.out.println(o);
        }


        SparkAccumulators.showAccumulators(totalTime);
        java.lang.Long wordCount = totalWords.value();
        System.out.println("total words = " + wordCount);

        Statistics value = letterStats.value();
        System.out.println("total words = " + value.getNumber());
        System.out.println("total letters = " + value.getSum());
         System.out.println("average letters = " + String.format("%10.2f", value.getAverage()));
        System.out.println("Sd average letters = " + String.format("%10.2f", value.getStandardDeviation()));
        System.out.println("Max letters = " + value.getMax());

    }

}

