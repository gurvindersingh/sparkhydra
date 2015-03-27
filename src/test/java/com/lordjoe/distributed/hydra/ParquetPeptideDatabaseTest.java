package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.fragment.*;
import com.lordjoe.distributed.hydra.peptide.*;
import org.apache.spark.api.java.*;
import org.apache.spark.sql.*;
import org.systemsbiology.xtandem.peptide.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.ParquetPeptideDatabaseTest
 * User: Steve
 * Date: 10/23/2014
 */
public class ParquetPeptideDatabaseTest {

    public static final String PARQUET_NAME = "E:/SparkHydra/trunk/data/Sample2/yeast_orfs_all_REV.20060126.short";

    public static void main(String[] args) {
        SQLContext sqlContext = SparkUtilities.getCurrentSQLContext();
        // Read in the Parquet file created above.  Parquet files are self-describing so the schema is preserved.
        // The result of loading a parquet file is also a JavaSchemaRDD.
        String name = PARQUET_NAME;
        String parquetName = name + ".parquet";
        DataFrame parquetFile = sqlContext.parquetFile(parquetName);

        int scanmass = 1214;
        //Parquet files can also be registered as tables and then used in SQL statements.
        parquetFile.registerTempTable("peptides");
    //     JavaSchemaRDD binCounts = sqlContext.sql("SELECT * FROM " + "peptides");
   //     JavaSchemaRDD binCounts = sqlContext.sql("SELECT * FROM " + "peptides");
        DataFrame binCounts = sqlContext.sql("SELECT * FROM " + "peptides" + " Where  massBin BETWEEN " +
                BinChargeKey.mzAsInt(1214.0) + " AND " +  BinChargeKey.mzAsInt(1214.5)) ;

        JavaRDD<PeptideSchemaBean> beancounts = binCounts.toJavaRDD().map(PeptideSchemaBean.FROM_ROW);
        beancounts = SparkUtilities.persist(beancounts);
        List<PeptideSchemaBean> beanlist = beancounts.collect();

        JavaRDD<IPolypeptide> counts = beancounts.map(PeptideSchemaBean.FROM_BEAN);

        List<IPolypeptide> collect = counts.collect();
        IPolypeptide[] peptides = collect.toArray(new IPolypeptide[collect.size()]);

        for (IPolypeptide teenagerName : peptides)

        {
            System.out.println(teenagerName + " " + (int)teenagerName.getMatchingMass());
        }
    }
}


