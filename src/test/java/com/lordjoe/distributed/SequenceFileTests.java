package com.lordjoe.distributed;

import org.apache.hadoop.fs.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.junit.*;
import scala.*;

import java.util.*;

/**
 * com.lordjoe.distributed.SequenceFileTests
 * User: Steve
 * Date: 2/2/2015
 */
public class SequenceFileTests implements Serializable {

    public static Integer[]  values = { 1,1,2,3,5,8,13,21 };

    public static final String PATH = "testPairs";

    //@Test
    public void testSequenceFile() throws Exception {

        Path path = new Path(PATH);
        JavaSparkContext ctx = SparkUtilities.getCurrentContext();
        FileSystem fileSystem = SparkUtilities.getHadoopFileSystem();
        fileSystem.delete(path,true);

        JavaRDD<Integer> iValues  = (JavaRDD<Integer>) ctx.parallelize(Arrays.asList(SequenceFileTests.values));
        JavaPairRDD<String, Integer> pairs = iValues.mapToPair(new PairFunction<Integer, String, Integer>() {
            @Override
            public Tuple2<String, Integer> call(final Integer t) throws Exception {
                return new Tuple2<String, Integer>(Integer.toString(t), t);
            }
        });

        pairs = SparkUtilities.saveAsSequenceFile(PATH,pairs);

        List<Tuple2<String, Integer>> pairsWritten = pairs.collect();
        Map<String, Integer> writtenMap = SparkUtilities.mapFromTupleList(pairsWritten);

        JavaPairRDD<String, Integer> pairsRead = SparkUtilities.readAsSequenceFile(PATH, String.class, Integer.class);

        List<Tuple2<String, Integer>> pairsReadList = pairsRead.collect();
        Map<String, Integer> readMap = SparkUtilities.mapFromTupleList(pairsReadList);

        Assert.assertTrue(SparkUtilities.equivalent(writtenMap,readMap) );
        fileSystem.delete(path,true);

    }
}
