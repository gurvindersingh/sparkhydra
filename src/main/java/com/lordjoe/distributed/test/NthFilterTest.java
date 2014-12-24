package com.lordjoe.distributed.test;

import com.lordjoe.distributed.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.storage.*;
import scala.*;

import java.util.*;

/**
 * com.lordjoe.distributed.test.NthFilterTest
 * User: Steve
 * Date: 12/11/2014
 */
public class NthFilterTest {

    public static Set<Integer>  sequentialIntegers(int size) {
        Set<Integer> holder = new HashSet<Integer>();
        for (int i = 0; i < size; i++) {
            holder.add(i);
           }
          return holder;
    }
    /**
       * Main - runs essentially word count for all variants starting with length 10 (1000 variants) and
       * raising it by a factor of 4
       *
       * @param args if given a file to read - otherwise use the Gettysburg address
       * @throws Exception
       */
      public static void main(String[] args) throws Exception {
          SparkConf sparkConf = new SparkConf().setAppName("JavaBigDataWordCount");
          sparkConf.set("spark.mesos.coarse", "true");
          Option<String> option = sparkConf.getOption("spark.master");
          if (!option.isDefined())   // use local over nothing
              sparkConf.setMaster("local");
          JavaSparkContext ctx = new JavaSparkContext(sparkConf);

          int size = 1000 * 1000 * 30;
          if(args.length > 0)
              size = Integer.parseInt(args[0]);
          Set<Integer> integerSet = sequentialIntegers(size);
          Set<Integer> afterFilter = new HashSet<Integer>();
           List<Integer> values = new ArrayList<Integer>(integerSet);
          JavaRDD<Integer> ints = ctx.parallelize(values);

          ints.coalesce(24,true); // force partitioning

          ints = ints.persist(StorageLevel.MEMORY_ONLY());
          for (int i = 0; i < 10; i++) {
              JavaRDD<Integer> filtered = ints.filter(new NofKFilter(10,i));
              List<Integer> collect = filtered.collect();
              for (Integer value : collect) {
                   if(afterFilter.contains(value))
                       throw new IllegalStateException("should hane only one copy of " + value);
                  afterFilter.add(value);
              }
          }

          System.out.println(" Number before " + integerSet.size() +
            " after " + afterFilter.size());

     }

}
