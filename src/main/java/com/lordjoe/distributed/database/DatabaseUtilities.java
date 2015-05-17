package com.lordjoe.distributed.database;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.peptide.PeptideSchemaBean;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.sql.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.peptide.Polypeptide;

import java.io.*;

/**
 * com.lordjoe.distributed.database.DatabaseUtilities
 * User: Steve
 * Date: 10/20/2014
 */
public class DatabaseUtilities {

    public static <K> void buildParaquetDatabase( String name,JavaRDD  data,Class  bean) {
        try {
            JavaSparkContext sc = SparkUtilities.getCurrentContext();
            SQLContext sqlContext = SparkUtilities.getCurrentSQLContext();

            // Apply a schema to an RDD of JavaBeans and register it as a table.
            DataFrame frame = sqlContext.applySchema(data, bean);

            Configuration conf = sc.hadoopConfiguration();
             FileSystem fs = FileSystem.get(conf);
             HDFSAccessor accessor = new HDFSAccessor(fs);
             accessor.expunge(name);

            // JavaSchemaRDDs can be saved as Parquet files, maintaining the schema information.
            frame.saveAsParquetFile(name);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static JavaRDD<IPolypeptide> readParquetDatabase(String name) {
        JavaSparkContext sc = SparkUtilities.getCurrentContext();
        SQLContext sqlContext = SparkUtilities.getCurrentSQLContext();

        // Apply a schema to an RDD of JavaBeans and register it as a table.
        JavaRDD<PeptideSchemaBean> beansRDD = sqlContext.load(name).toJavaRDD().map(PeptideSchemaBean.FROM_ROW);
        JavaRDD<IPolypeptide> peptides = beansRDD.map(PeptideSchemaBean.FROM_BEAN);
        return peptides;
    }

    public static void removeParaquetDatabase( String name) {
        try {
            JavaSparkContext sc = SparkUtilities.getCurrentContext();
            Configuration conf = sc.hadoopConfiguration();
            FileSystem fs = FileSystem.get(conf);
            HDFSAccessor accessor = new HDFSAccessor(fs);
            accessor.expunge(name);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static<K  extends IDatabaseBean> void guaranteeParaquetDatabase( String name,JavaRDD<K> data,Class<? extends K> bean) {
        try {
            JavaSparkContext sc = SparkUtilities.getCurrentContext();
            Configuration conf = sc.hadoopConfiguration();
            FileSystem fs = FileSystem.get(conf);
            HDFSAccessor accessor = new HDFSAccessor(fs);
             if(accessor.exists(name))
                return; // already built

            buildParaquetDatabase(name,data,bean);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public static boolean isParquetDatabaseExist(String name) {
        boolean result = false;
        try {
            JavaSparkContext sc = SparkUtilities.getCurrentContext();
            Configuration conf = sc.hadoopConfiguration();
            FileSystem fs = FileSystem.get(conf);
            HDFSAccessor accessor = new HDFSAccessor(fs);
            if(accessor.exists(name))
                result = true;
        }
        catch (IOException e) {
            return result;
        }
        return result;
    }
}
