package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.peptide.*;
import org.apache.spark.api.java.*;
import org.apache.spark.sql.api.java.*;
import org.systemsbiology.xtandem.peptide.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.ParquetPeptideDatabaseTest
 * User: Steve
 * Date: 10/23/2014
 */
public class ParquetPeptideDatabaseTest {

    public static void main(String[] args) {
        JavaSQLContext sqlContext = SparkUtilities.getCurrentSQLContext();
        // Read in the Parquet file created above.  Parquet files are self-describing so the schema is preserved.
        // The result of loading a parquet file is also a JavaSchemaRDD.
        String name = "E:/distributed-tools/data/Sample2/yeast_orfs_pruned";
        String parquetName = name + ".parquet";
        JavaSchemaRDD parquetFile = sqlContext.parquetFile(parquetName);

        int scanmass = 1214;
        //Parquet files can also be registered as tables and then used in SQL statements.
        parquetFile.registerAsTable("peptides");
    //     JavaSchemaRDD binCounts = sqlContext.sql("SELECT * FROM " + "peptides");
        JavaSchemaRDD binCounts = sqlContext.sql("SELECT * FROM " + "peptides" + " Where  massBin =" + scanmass);

        JavaRDD<PeptideSchemaBean> beancounts = binCounts.map(PeptideSchemaBean.FROM_ROW);
        JavaRDD<IPolypeptide> counts = beancounts.map(PeptideSchemaBean.FROM_BEAN);

        List<IPolypeptide> collect = counts.collect();
        IPolypeptide[] peptides = collect.toArray(new IPolypeptide[collect.size()]);

        for (IPolypeptide teenagerName : peptides)

        {
            System.out.println(teenagerName + " " + (int)teenagerName.getMatchingMass());
        }
    }
}


