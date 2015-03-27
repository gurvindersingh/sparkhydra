package com.lordjoe.distributed.database;

import com.lordjoe.distributed.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.sql.*;
import org.junit.*;
import org.systemsbiology.hadoop.*;

import java.io.*;
import java.util.*;

//import org.apache.spark.sql.api.java.*;

/**
 * com.lordjoe.distributed.database.PersonDatabaseTest
 * User: Steve
 *
 * @see https://spark.apache.org/docs/1.0.2/sql-programming-guide.html#parquet-files
 * Date: 10/16/2014
 */
public class PersonDatabaseTest {

//    public static void main(String[] args)  throws Exception {
//        fixPeople("People1.txt") ;
//        fixPeople("People2.txt") ;
//        fixPeople("People3.txt") ;
//        fixPeople("People4.txt") ;
//    }
//
//    /**
//     * convert a file with alist of names into a file of names and ages
//     * @param s
//     * @throws Exception
//     */
//    private static void fixPeople(final String s) throws Exception {
//        List<String> holder = new ArrayList<String>();
//        File f = new File(s);
//        LineNumberReader rdr = new LineNumberReader(new FileReader(f)) ;
//        String line = rdr.readLine();
//        while(line != null)   {
//            holder.add(line);
//            line = rdr.readLine();
//        }
//        rdr.close();
//        PrintWriter writer = new PrintWriter(new FileWriter(f));
//        for (String s1 : holder) {
//            writer.append(buildPersonLine(s1)) ;
//        }
//        writer.close();
//
//       // return holder;
//
//    }
//
//    public static final Random RND = new Random();
//
//    private static String buildPersonLine(final String pS1) {
//        int age = RND.nextInt(70);
//        return pS1 + "," + age + "\n";
//    }

    public static final int TEENAGER_COUNT = 8815;

    private static void showTeenAgersCount(final JavaSparkContext sc, final String name) {
        SQLContext sqlContext = SparkUtilities.getCurrentSQLContext();
         // Read in the Parquet file created above.  Parquet files are self-describing so the schema is preserved.
         // The result of loading a parquet file is also a JavaSchemaRDD.
         String parquetName = name + ".parquet";
         DataFrame parquetFile = sqlContext.parquetFile(parquetName);

         //Parquet files can also be registered as tables and then used in SQL statements.
         parquetFile.registerTempTable(name);
         DataFrame teenagers = sqlContext.sql("SELECT COUNT(*) FROM " +  name + "  WHERE age >= 13 AND age <= 19");
         List<String> teenagerNames = teenagers.toJavaRDD().map(new Function<Row, String>() {
             public String call(Row row) {
                 int length = row.length();
                 for (int i = 0; i <  length; i++) {
                     Object o = row.get(i);
                     String value = o.toString();
                     long count = row.getLong(i);
                 }
       //          String name = row.getString(0);
                 long count = row.getLong(0);
                  return "Count: " + count;
             }
         }).collect();

        String s = teenagerNames.get(0);
        int actual = Integer.parseInt(s.replace("Count: ",""));
        Assert.assertEquals(TEENAGER_COUNT, actual);

         for (String teenagerName : teenagerNames)

         {
             System.out.println(teenagerName);
         }
     }

    private static void showTeenAgers(final JavaSparkContext sc, final String name) {
        SQLContext sqlContext = SparkUtilities.getCurrentSQLContext();
        // Read in the Parquet file created above.  Parquet files are self-describing so the schema is preserved.
        // The result of loading a parquet file is also a JavaSchemaRDD.
        String parquetName = name + ".parquet";
        DataFrame parquetFile = sqlContext.parquetFile(parquetName);

        //Parquet files can also be registered as tables and then used in SQL statements.
        parquetFile.registerTempTable(name);
        DataFrame teenagers = sqlContext.sql("SELECT name FROM "  + name + "  WHERE age >= 13 AND age <= 19");
        List<String> teenagerNames = teenagers.toJavaRDD().map(new Function<Row, String>() {
            public String call(Row row) {
                String name = row.getString(0);
                return "Name: " + name;
            }
        }).collect();

        for (String teenagerName : teenagerNames)

        {
            System.out.println(teenagerName);
        }
    }

    public static void buildParaquetDatabase(JavaSparkContext sc , String name) {
        try {
            SQLContext sqlContext = SparkUtilities.getCurrentSQLContext();
            String fileName = name + ".txt";
            JavaRDD < Person > people = sc.textFile(fileName).map(
                    new ReadPeopleFile());

            // Apply a schema to an RDD of JavaBeans and register it as a table.
            DataFrame schemaPeople = sqlContext.applySchema(people, Person.class);

            HDFSAccessor accessor = new HDFSAccessor(FileSystem.get(sc.hadoopConfiguration()));
            String parquetName = name + ".parquet";
            accessor.expunge(parquetName);

            // JavaSchemaRDDs can be saved as Parquet files, maintaining the schema information.
            schemaPeople.saveAsParquetFile(parquetName);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    public static void guaranteeParaquetDatabase(JavaSparkContext sc , String name) {
        try {
             HDFSAccessor accessor = new HDFSAccessor(FileSystem.get(sc.hadoopConfiguration()));
            String parquetName = name + ".parquet";
            if(accessor.exists(parquetName))
                return; // already built

            buildParaquetDatabase(  sc ,   name);
         }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }


    public static void main(String[] args) throws Exception {
        JavaSparkContext sc = SparkUtilities.getCurrentContext();
        // sc is an existing JavaSparkContext.
        guaranteeParaquetDatabase(sc, "ManyPeople");
        showTeenAgersCount(sc, "ManyPeople");

//        for (int i = 1; i < 5; i++) {
//            buildParaquetDatabase(sc, "people" + i);
//            showTeenAgers(sc, "people" + i);
//         }
    }


}
