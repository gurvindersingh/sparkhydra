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

package com.lordjoe.distributed.test;

import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.api.java.function.Function2;
import scala.*;

import java.util.*;
import java.util.regex.*;

/**
 * org.apache.spark.examples.JavaHighMemoryWordCount
 * Just like Word Count except that instead of Integer it uses
 * HighMemoryInteger and will fail if large numbers of tuples are
 * retained in memory.
 *    Hadoop has no problem with this even if the entire HighMemoryInteger structure is serialized
 */
public final class JavaHighMemoryWordCount {
    private static final Pattern SPACE = Pattern.compile(" ");

    public static final int SPACE_TO_WASTE = 1000000;

    /**
     * behaves enough like an Integer for WordCount but takes up A LOT more memory forcing
     *  a simple word could to become a big memory problem
     */
    public static class HighMemoryInteger implements Serializable {
        public final int value;
        public final int[] wastedSpace = new int[SPACE_TO_WASTE];  // take up 4 megs so we cannot keep many in memory

        public HighMemoryInteger(final int pValue) {
            value = pValue;
        }

        public HighMemoryInteger add(final HighMemoryInteger added) {
            return new HighMemoryInteger(value + added.value);
        }
        public String toString() { return Integer.toString(value);}
    }

    public static final HighMemoryInteger ONE = new HighMemoryInteger(1);

    public static void main(String[] args) throws Exception {

        if (args.length < 1) {
            System.err.println("Usage: JavaHighMemoryWordCount <file>");
            return;
        }

        SparkConf sparkConf = new SparkConf().setAppName("JavaHighMemoryWordCount");
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

        JavaPairRDD<String, HighMemoryInteger> ones = words.mapToPair(new PairFunction<String, String, HighMemoryInteger>() {
            @Override
            public Tuple2<String, HighMemoryInteger> call(String s) {
                return new Tuple2<String, HighMemoryInteger>(s, ONE);
            }
        });

        JavaPairRDD<String, HighMemoryInteger> counts = ones.reduceByKey(new Function2<HighMemoryInteger, HighMemoryInteger, HighMemoryInteger>() {
            @Override
            public HighMemoryInteger call(HighMemoryInteger i1, HighMemoryInteger i2) {
                return i1.add(i2);
            }
        });

        List<Tuple2<String, HighMemoryInteger>> output = counts.sortByKey().collect();
        for (Tuple2<?, ?> tuple : output) {
            System.out.println(tuple._1() + ": " + tuple._2());
        }
    }
}
