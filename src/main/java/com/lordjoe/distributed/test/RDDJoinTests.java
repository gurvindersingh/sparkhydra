package com.lordjoe.distributed.test;

import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.storage.*;
import scala.*;

import java.lang.Double;
import java.lang.Long;
import java.util.*;

/**
 * com.lordjoe.distributed.test.RDDJoinTests
 * User: Steve
 * Date: 12/9/2014
 */
public class RDDJoinTests {
     public static final Random RND = new Random();
     public static int numberDoubles = 4;
     public static int itemsPerKeys = 2000;

    /**
     * just a random object but might be medium sized - say 10000 doubles
     */
    public static class MediumSizedObject implements Serializable {
        public final double[] items;
         public MediumSizedObject() {
            items = new double[numberDoubles];
            for (int i = 0; i < items.length; i++)
                items[i] = RND.nextDouble();
           }
     }

    /**
     * map a key into itemsPerKeys tuples
     */
    private static class createManyObjectsPerKey implements PairFlatMapFunction<Integer, Integer, MediumSizedObject> {
            public createManyObjectsPerKey( ) {
            }

         @Override
         public Iterable<Tuple2<Integer, MediumSizedObject>> call(final Integer key) throws Exception {
             List<Tuple2<Integer, MediumSizedObject>> ret = new ArrayList<Tuple2<Integer, MediumSizedObject>>();
             for (int i = 0; i < itemsPerKeys; i++) {
                 ret.add(new Tuple2<Integer, MediumSizedObject>(key, new MediumSizedObject()));
             }
             return ret;
         }
     }


//    /**
//       * map a key into itemsPerKeys tuples
//       */
//      private static class createManyObjectsPerKey implements PairFlatMapFunction<Integer, Integer, UUID> {
//           private final int m_itemsPerKeys;
//            public createManyObjectsPerKey(final int pItemsPerKeys) {
//               m_itemsPerKeys = pItemsPerKeys;
//           }
//
//           @Override
//           public Iterable<Tuple2<Integer, UUID>> call(final Integer key) throws Exception {
//               List<Tuple2<Integer, UUID>> ret = new ArrayList<Tuple2<Integer, UUID>>();
//               for (int i = 0; i < m_itemsPerKeys; i++) {
//                   ret.add(new Tuple2<Integer, UUID>(key, UUID.randomUUID()));
//               }
//               return ret;
//           }
//       }
//

    public static JavaSparkContext buildJavaSparContext(String appName) {
        SparkConf sparkConf = new SparkConf().setAppName(appName);
        Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined())    // use local over nothing
            sparkConf.setMaster("local[*]");

        sparkConf.set("spark.mesos.coarse", "true");  // always allow a job to be killed
        sparkConf.set("spark.ui.killEnabled", "true");  // always allow a job to be killed
        //      sparkConf.set("spark.mesos.executor.memoryOverhead","1G");
        sparkConf.set("spark.executor.memory", "12G");
        sparkConf.set("spark.task.cpus", "4");
        sparkConf.set("spark.default.parallelism", "120");
        return new JavaSparkContext(sparkConf);
    }

    /**
     * usage  RDDJoinTests <numberKeya> <doublesPerObject></doublesPerObject>
     * RDDJoinTests 3000 16
      * should make joins of 3000 keys 1000000 per key
     *
     *
     * @param args
     */
    public static void main(String[] args) {

        long start = System.currentTimeMillis();
        JavaSparkContext ctx = buildJavaSparContext("Join Test");

        int numberKeys = Integer.parseInt(args[0]);
         numberDoubles = Integer.parseInt(args[1]);

        System.err.println("number keys " + numberKeys + " items per Key " + itemsPerKeys  + " Object size " + numberDoubles * Double.SIZE);
         List<Integer> keys = new ArrayList<Integer>(numberKeys); // make a list of keys
        for (int i = 0; i < numberKeys; i++) {
            keys.add(i);
        }

        JavaRDD<Integer> keyRDD = ctx.parallelize(keys);
        keyRDD = keyRDD.persist(StorageLevel.MEMORY_ONLY());
        // make 2 sets of key valus pairs with 1000 for every key
        JavaPairRDD<Integer, MediumSizedObject> pointPair1 = keyRDD.flatMapToPair(new createManyObjectsPerKey());
        JavaPairRDD<Integer, MediumSizedObject> pointPair2 = keyRDD.flatMapToPair(new createManyObjectsPerKey());
        // join these for itemsPerKeys**2 tuples
        JavaPairRDD<Integer, Tuple2<MediumSizedObject, MediumSizedObject>> join = pointPair1.join(pointPair2);
        // we expect this many items
        long joinSize = itemsPerKeys * itemsPerKeys;
        // count by key - validate the join and get total items
        long totalItems = 0;
         Map<Integer, Object> totalValues = join.countByKey();
        for (Integer key : totalValues.keySet()) {
            Long count = (Long) totalValues.get(key);
            if (count != joinSize)
                throw new IllegalStateException("wrong join size");
            totalItems += count;
        }
        System.err.print("Total Keys " + totalValues.size()); // print results
        System.err.print(" Total Items " + totalItems /1000000 + "M");
         System.err.println(" Total Bytes " + (totalItems * numberDoubles * Double.SIZE) / 1000000 + "M");
        long end = System.currentTimeMillis();
        System.err.println("In " + (end - start) /1000 + " sec");
      }
  }
