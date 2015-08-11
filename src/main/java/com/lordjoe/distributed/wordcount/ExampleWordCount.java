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
public class ExampleWordCount {

//    public static class KryoSerializer implements KryoRegistrator, java.io.Serializable {
//         @Override
//        public void registerClasses(final Kryo kryo) {
//            kryo.register(String.class);
//        }
//    }
//
//    public static class String implements   java.io.Serializable, Comparable<String>{
//
//           public final String value;
//
//        public String(final String pValue) {
//            value = pValue;
//        }
//
//
//        @Override
//        public int compareTo(final String o) {
//            return value.compareTo(o.value);
//        }
//
//
//        @Override
//        public String toString() {
//            return value;
//        }
//
//        @Override
//        public boolean equals(final Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//           final String that = (String) o;
//            if (value != null ? !value.equals(that.value) : that.value != null) return false;
//           return true;
//        }
//
//        @Override
//        public int hashCode() {
//            return value != null ? value.hashCode() : 0;
//        }
//    }
//

    private static final Pattern SPACE = Pattern.compile(" ");

    /**
     * convert to Upper case and drop punctuation -
     * makes a more honest word count
      * @param inp original string
     * @return filtered String - might ne empty
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

    private static void usage() {
        System.out.println("java com.lordjoe.distributed.wordcount.ExampleWordCount <TextFile>");
      }

    /**
     * Usage - args[0] is the name of a file to count words
     * like
     * RedBadge.txt
     * @param args  name of input file - must have one
     */
    public static void main(final String[] args) {
        if(args.length == 0) { usage(); return; }

        org.apache.hadoop.fs.FSDataInputStream someFs = null;

        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("ExampleWordCount");

        // if not on a cluster then run locally
        Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined()) {   // use local over nothing
            sparkConf.setMaster("local[*]");
        }
        sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
     //   sparkConf.set("spark.kryo.registrator", "com.lordjoe.distributed.wordcount.ExampleWordCount$KryoSerializer");
      // sparkConf.set("spark.kryo.registrationRequired", "true");

         // make a  JavaSparkContext
        JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        // Create a Stream of Lines of text
        String testInputFile = args[0];
        JavaRDD<String> lines = ctx.textFile(testInputFile);

        Accumulator<Set<String>>  wordsUsed = ctx.accumulator(new HashSet<String>(), new SetAccumulableParam()) ;
        // Convert line into a list of words - return as an iterable
        JavaRDD<String> words = lines.flatMap(new FlatMapFunction<String, String>() {
            @Override
            public Iterable<String> call(final String s) throws Exception {
                String[] split = SPACE.split(s);
                List<String> ret = new ArrayList<String>();
                //noinspection ForLoopReplaceableByForEach
                for (int i = 0; i < split.length; i++) {
                    String nextWord = regularizeString(split[i]);
                    if (nextWord.length() > 0) {
                        ret.add(nextWord);
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

        counts = counts.sortByKey();



        // This line is critical to force work to happen
        List<Tuple2<String, Integer>> collect = counts.collect(); // make the count happen

//        // alphabetize
//        Collections.sort(collect, new Comparator<Tuple2<String, Integer>>() {
//            @Override
//            public int compare(final Tuple2<String, Integer> o1, final Tuple2<String, Integer> o2) {
//                return o1._1().compareTo(o2._1());
//            }
//        });
//
        // print the results
        for (Tuple2<String, Integer> wordAndCount : collect) {
            System.out.println(wordAndCount._1() + "\t" + wordAndCount._2());
        }

     }

    public static class SetAccumulableParam implements AccumulatorParam<Set<String>>,Serializable {
             @Override
          public Set<String> addAccumulator(final Set<String> r, final Set<String> t) {
               HashSet<String> ret = new HashSet<String>(r);
               ret.addAll(t);
              return ret;
          }
           @Override
          public Set<String> addInPlace(final Set<String> r1, final Set<String> r2) {
              return  addAccumulator(r1,r2);
          }
           @Override
          public Set<String> zero(final Set<String> initialValue) {
              return initialValue;
          }
       }


  }
