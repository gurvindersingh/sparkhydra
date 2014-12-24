package com.lordjoe.distributed.wordcount;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.util.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;

import java.io.*;

/**
 * com.lordjoe.distributed.SparkWordCount
 * User: Steve
 * Date: 9/2/2014
 */
public class SparkWordCount {
    public static final String MY_BOOK = "/war_and_peace.txt";



    @SuppressWarnings("unchecked")
    public static void main(final String[] args) {
        ListKeyValueConsumer consumer = new ListKeyValueConsumer();

        SparkMapReduce handler = new SparkMapReduce("Spark Word Count",new NullStringMapper(),new NullStringReducer(),IPartitionFunction.HASH_PARTITION,consumer);
   //     SparkMapReduce handler = new SparkMapReduce(new WordCountMapper(),new WordCountReducer(),IPartitionFunction.HASH_PARTITION,consumer);
        JavaSparkContext ctx = SparkUtilities.getCurrentContext();


        JavaRDD<KeyValueObject<String,String>> lines;
        if(args.length == 0) {
            final InputStream is = SparkWordCount.class.getResourceAsStream(MY_BOOK);
             lines = SparkUtilities.keysFromInputStream(MY_BOOK, is, ctx);
        }
        else {
            JavaRDD<String> justLines = ctx.textFile(args[0], 1);
             lines = justLines.map(new Function<String, KeyValueObject<String, String>>() {
                 @Override
                 public KeyValueObject<String, String> call(final String s) throws Exception {
                     return new KeyValueObject<String, String>(args[0],s);
                 }
             });
        }

          handler.performSourceMapReduce(lines);

        Iterable<KeyValueObject<String,Integer>> answer = handler.collect();
        for (KeyValueObject<String,Integer> o : answer) {
            System.out.println(o.toString());
        }

     }
}
