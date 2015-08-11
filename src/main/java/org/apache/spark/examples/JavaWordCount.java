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
import com.lordjoe.distributed.spark.accumulators.*;
import com.lordjoe.utilities.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.*;
import scala.*;

import java.util.*;
import java.util.regex.*;

/**
 * org.apache.spark.examples.JavaWordCount
 */
public final class JavaWordCount {
    private static final Pattern SPACE = Pattern.compile(" ");

    public static final int SPARK_CONFIG_INDEX = 0;
      public static final int INPUT_FILE_INDEX = 1;


    public static void main(String[] args) throws Exception {
        ElapsedTimer totalTime = new ElapsedTimer();

        if (args.length < INPUT_FILE_INDEX + 1) {
            System.err.println("Usage: SparkProperties JavaWordCount <file>");
            return;
        }
        SparkUtilities.readSparkProperties(args[SPARK_CONFIG_INDEX]);
        SparkUtilities.setAppName("JavaWordCount");

        JavaSparkContext ctx = SparkUtilities.getCurrentContext();

        // Add some accumulators
        AccumulatorUtilities.getInstance().createAccumulator("WordsMapFunction");
        AccumulatorUtilities.getInstance().createAccumulator("TotalLetters");

        JavaRDD<String> lines = ctx.textFile(args[0], 12);


         JavaRDD <String> words = lines.flatMap(new WordsMapFunction());

        words = words.coalesce(12);


//            new FlatMapFunction<String, String>() {
//      @Override
//      public Iterable<String> call(String s) {
//         String[] split = SPACE.split(s);
//         for (int i = 0; i < split.length; i++) {
//             String trim = split[i].trim();
//             split[i] = trim.toUpperCase();
//         }
//         return Arrays.asList(split);
//     }    });

        JavaPairRDD<String, Integer> ones = words.mapToPair(new PairFunction<String, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(String s) {
                return new Tuple2<String, Integer>(s, 1);
            }
        });

        //ones = ones.partitionBy(SparkUtilities.DEFAULT_PARTITIONER);

        JavaPairRDD<String, Integer> counts = ones.reduceByKey(new Function2<Integer, Integer, Integer>() {
            @Override
            public Integer call(Integer i1, Integer i2) {
                return i1 + i2;
            }
        });

        List<Tuple2<String, Integer>> output = counts.sortByKey().collect();
        for (Tuple2<?, ?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }

        SparkAccumulators.showAccumulators(totalTime);


    }
}
