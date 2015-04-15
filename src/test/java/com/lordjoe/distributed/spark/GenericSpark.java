package com.lordjoe.distributed.spark;

import org.apache.spark.*;
import org.apache.spark.api.java.*;
import scala.*;

import java.util.*;

/**
 * com.lordjoe.distributed.spark.GenericSpark
 * User: Steve
 * Date: 4/10/2015
 */
public class GenericSpark {

    public static class Foo {
        public final String str;

        public Foo(final String pStr) {
            str = pStr;
        }
    }
    public static class Bar extends Foo {
        public Bar(final String pStr) {
            super(pStr);
        }
    }

    public static Bar[] INIT_BAR =  { new Bar("Fee"),new Bar("Fie"),new Bar("Foe"),new Bar("Fum"),};

    /**
       * Usage - args[0] is the name of a file to count words
       * like
       * RedBadge.txt
       * @param args  name of input file - must have one
       */
      public static void main(final String[] args) {

          SparkConf sparkConf = new SparkConf();
          sparkConf.setAppName("Test of Generics");

          // if not on a cluster then run locally
          Option<String> option = sparkConf.getOption("spark.master");
          if (!option.isDefined()) {   // use local over nothing
              sparkConf.setMaster("local[*]");
          }
               // make a  JavaSparkContext
          JavaSparkContext ctx = new JavaSparkContext(sparkConf);

          List<Bar> bars = Arrays.asList(INIT_BAR);
          List<? extends Foo> foos = Arrays.asList(INIT_BAR);

          // Create a Stream of Lines of text
            JavaRDD<Bar> lines = ctx.parallelize(bars);



           // All this fails

          // Standard go to word pairs
//          JavaPairRDD<String, Foo> ones = foos.mapToPair(new PairFunction<Foo, String, Foo>() {
//              @Override
//              public Tuple2<String, Foo> call(final Foo pBar) throws Exception {
//                  return null;
//              }
//          }) ;



       }

}
