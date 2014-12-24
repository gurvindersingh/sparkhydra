package com.lordjoe.distributed.spark;

import com.lordjoe.distributed.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;

import java.util.*;

/**
 * com.lordjoe.distributed.spark.RDDGenerator
 * functions top greate RDDS usinsg a generating function
 * User: Steve
 * Date: 12/9/2014
 */
public class RDDGenerator {
    // at this size we do not need flatmap
    public static final int MAXIMUM_ONE_STEP_SIZE = 100000;
    // this many objects may be held in memory as a list
    public static final int MAXIMUM_LIST_SIZE = 1000;

    /**
     * @param sc         context
     * @param generator  function to generate objects
     * @param size       desired number of objects
     * @param partitions number partitions
     * @param <T>        object type
     * @return an rdd with approximately size objects of type T
     */
    public static <T> JavaRDD<T> generateRDD(JavaSparkContext sc, final ObjectGenerator<T> generator, long size, int partitions) {
        // small case - make a list of the paoper size and use map
        if (size < MAXIMUM_ONE_STEP_SIZE) {
            Byte[] values = new Byte[(int) size];
            for (int i = 0; i < values.length; i++)  // make non-null
                values[i] = 0;
            JavaRDD<Byte> itemList = sc.parallelize(Arrays.asList(values));
            itemList = itemList.coalesce(partitions, true);
            return itemList.map(new Function<Byte, T>() {
                @Override
                public T call(final Byte v1) throws Exception {
                    return generator.generateObject();
                }
            });
        }
        else { // large case - sise is within 1000 and not over 2 trillion - use flatmap
            final int listSize = 1 + (int) (size / MAXIMUM_LIST_SIZE);
            Byte[] values = new Byte[MAXIMUM_LIST_SIZE];
            for (int i = 0; i < values.length; i++)  // make non-null
                values[i] = 0;
            JavaRDD<Byte> itemList = sc.parallelize(Arrays.asList(values));
            itemList = itemList.coalesce(partitions, true);
            return itemList.flatMap(new FlatMapFunction<Byte, T>() {
                @Override
                public Iterable<T> call(final Byte t) throws Exception {
                    List<T> holder = new ArrayList<T>();
                    for (int i = 0; i < listSize; i++) {
                        holder.add(generator.generateObject());
                    }
                    return holder;
                }
            });

        }
    }

    /**
     * use   SparkUtilities.getDefaultNumberPartitions() as number partitons
     *
     * @param sc        context
     * @param generator function to generate objects
     * @param size      desired number of objects
     * @param <T>       object type
     * @return an rdd with approximately size objects of type T
     */
    public static <T> JavaRDD<T> generateRDD(JavaSparkContext sc, ObjectGenerator<T> generator, long size) {
        return generateRDD(sc, generator, size, SparkUtilities.getDefaultNumberPartitions());
    }

}
