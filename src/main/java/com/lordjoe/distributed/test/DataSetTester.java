package com.lordjoe.distributed.test;

import org.apache.log4j.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.sql.*;
import scala.*;

import java.util.*;

/**
 * com.lordjoe.distributed.test.DataSetTester
 * User: Steve
 * Date: 1/15/2016
 */
public class DataSetTester {

    public static final int NUMBER_OBJECTS = 200;

    public static void main(String[] args) {
        Logger rootLogger = Logger.getRootLogger();
        rootLogger.setLevel(Level.WARN);

        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("DataSetTester");
        Option<String> option = sparkConf.getOption("spark.master");

        if (!option.isDefined()) {   // use local over nothing
            sparkConf.setMaster("local[*]");
        }

        JavaSparkContext currentContext = new JavaSparkContext(sparkConf);
        SQLContext sqlCtx = new SQLContext(currentContext);

        List<DatasetTestObject> holder = new ArrayList<DatasetTestObject>();
        for (int i = 0; i < NUMBER_OBJECTS; i++) {
            DatasetTestObject test = DatasetTestObject.generateTestObject();
            holder.add(test);
            int count = test.findCount();
             if( count != DatasetTestObject.NUMBER_ENTRIES)
                 throw new IllegalStateException("problem"); // ToDo change
         }

        JavaRDD<DatasetTestObject> asRDD = currentContext.parallelize(holder);
        Encoder<DatasetTestObject> evidence = Encoders.bean(DatasetTestObject.class);
        Dataset<DatasetTestObject> dataset = sqlCtx.createDataset(asRDD.rdd(), evidence);

        DatasetTestObject[] collect = (DatasetTestObject[]) dataset.collect();
        for (int i = 0; i < collect.length; i++) {
            DatasetTestObject test = collect[i];
            int count = test.findCount();
            if( count != DatasetTestObject.NUMBER_ENTRIES)
                throw new IllegalStateException("problem"); // ToDo change
        }

        asRDD = currentContext.parallelize(holder);
        evidence = Encoders.kryo(DatasetTestObject.class);
        dataset = sqlCtx.createDataset(asRDD.rdd(), evidence);

        collect = (DatasetTestObject[]) dataset.collect();
        for (int i = 0; i < collect.length; i++) {
            DatasetTestObject test = collect[i];
            int count = test.findCount();
            if( count != DatasetTestObject.NUMBER_ENTRIES)
                    throw new IllegalStateException("problem"); // ToDo change
        }

    }
}
