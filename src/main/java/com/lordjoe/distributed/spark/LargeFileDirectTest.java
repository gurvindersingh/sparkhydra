package com.lordjoe.distributed.spark;


import com.lordjoe.distributed.input.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.log4j.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function;
import scala.*;

import java.io.*;
import java.io.Serializable;
import java.util.*;


/**
 * com.lordjoe.spark.LargeFileDirectTest
 * User: Steve
 * Creates a target fasta file - looks like
 * >1
 * TCCTTACGGAGTTCGGGTGTTTATCTTACTTATCGCGGTTCGCTGCCGCTCCGGGAGCCCGGATAGGCTGCGTTAATACCTAAGGAGCGCGTATTGAAAA
 * >2
 * GTCTGATCTAAATGCGACGACGTCTTTAGTGCTAAGTGGAACCCAATCTTAAGACCCAGGCTCTTAAGCAGAAACAGACCGTCCCTGCCTCCTGGAGTAT
 * >3
 * etc but containing gigabytes of random data
 * call
 * filename desiredSize <saveasTextFile>
 * like
 * myfile.fasta 5000000000
 * should make a 5G file
 * call
 * Submit like
 * spark-submit --class  LargeFileTest  ~/SparkJar.jar    Test6G.fasta 60000000000-executor-memory 12G
 * Date: 12/5/2014
 */
public class LargeFileDirectTest {
    public static long targetSize; // rouhgly how many bytes in the target file
    public static final Random RND = new Random();
    private static long idIndex = 1;
    private static int fragmentLength = 100;
    public static final String[] BASES = {"A", "C", "G", "T"};


    /**
       * //https://issues.apache.org/jira/browse/SPARK-2466
       * // there is a bug when two mesos processes are started in the same second
       * Got two different block manager registrations on 20140711-081617-711206558-5050-2543-13
       */
      public static final int SLEEP_MILLISEC = 10000;

      public static void guaranteeDifferentMesosID() {
          System.err.println("Wait 10 sec for new MESOS ID");
          try {
              Thread.sleep(SLEEP_MILLISEC);
          }
          catch (InterruptedException e) {
              throw new RuntimeException(e);

          }

      }

    /**
     * data such as would be seem in a fasta file to test large file processing
     */
    public static class DNAFragment implements Serializable {
        public final String id;  //  something like 55643
        public final String data; // something like AGGCAATTAGA... 100 long

        public DNAFragment(final String pId, final String pData) {
            id = pId;
            data = pData;
        }

        // Fasta files look like this
        public String toString() {
            return ">" + id + "\n" + data;
        }
    }

    // make up a DNA something like AGGCAATTAGA... fragmentLength long
    public static String makeFragment(int fragmentLength) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fragmentLength; i++) {
            sb.append(BASES[RND.nextInt(BASES.length)]);
        }
        return sb.toString();
    }




    // make up a DNA fragment fragmentLength long
    public static DNAFragment makeDNAFragment() {
        String id = java.lang.Long.toString(idIndex++);
        String dnaData = makeFragment(fragmentLength);
        return new DNAFragment(id, dnaData);
    }

//    /**
//     * //https://issues.apache.org/jira/browse/SPARK-2466
//     * // there is a bug when two mesos processes are started in the same second
//     * Got two different block manager registrations on 20140711-081617-711206558-5050-2543-13
//     */
//    public static final int SLEEP_MILLISEC = 10000;
//
//    public static void guaranteeDifferentMesosID() {
//        System.err.println("Wait 10 sec for new MESOS ID");
//        try {
//            Thread.sleep(SLEEP_MILLISEC);
//        }
//        catch (InterruptedException e) {
//            throw new RuntimeException(e);
//
//        }
//
//    }


    /**
     * true if the file exists
     *
     * @param hdfsPath !null path - probably of an existing file
     * @return
     */
    public static long fileLength(Path src, FileSystem fs) {
        try {
            if (!fs.exists(src))
                return 0;
            ContentSummary contentSummary = fs.getContentSummary(src);
            if (contentSummary == null)
                return 0;
            return contentSummary.getLength();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * create a file with name hdfsFileName on HDFS file system
     * of size at least TARGET_FILE_SIZE
     *
     * @param hdfsFileName
     * @param ctx
     * @return number bytes written very roughly
     * @throws java.io.IOException
     */
    public static long createLargeFastaFile(String hdfsFileName, JavaSparkContext ctx, long numberItems) throws Exception {

        // write directly to a simgle HDFS File
        Configuration entries = ctx.hadoopConfiguration();
        FileSystem fs = FileSystem.get(entries);
        Path path = new Path(hdfsFileName);

        long currentLength = fileLength(path, fs);
        if (currentLength >= numberItems * fragmentLength)
            return currentLength / fragmentLength;  // already exists and is big enough

        fs.delete(path, true); // kill any existing file

        FSDataOutputStream fsDataOutputStream = fs.create(path);
        PrintWriter out = new PrintWriter(fsDataOutputStream);
        for (int i = 0; i < numberItems; i++) {
            out.println(makeDNAFragment());
        }
        out.close();
        System.err.println("Wrote " + numberItems + " records");

        guaranteeDifferentMesosID();
        return numberItems; // number created

    }


    /**
     * call with filename and filesize
     * eg
     * Random6GB.fasta 6000000000
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String hdfsFileName = args[0];
        targetSize = java.lang.Long.parseLong(args[1]);
        long numberLines = 1 + targetSize / fragmentLength;
        System.err.println("Handling " + targetSize + " with numberLines " + numberLines);

        System.out.println("Set Log to Warn");    // shut up the logger
            Logger rootLogger = Logger.getRootLogger();
            rootLogger.setLevel(Level.WARN);


        SparkConf sparkConf = new SparkConf().setAppName("TestLargeFiles");
        Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined())    // use local over nothing
            sparkConf.setMaster("local[*]");
        else
            sparkConf.set("spark.shuffle.manager","HASH");

        sparkConf.set("spark.mesos.coarse", "true");  // always allow a job to be killed
        sparkConf.set("spark.ui.killEnabled", "true");  // always allow a job to be killed
         sparkConf.set("spark.mesos.executor.memoryOverhead","1G");
        sparkConf.set("spark.executor.memory","12G");
        sparkConf.set("spark.task.cpus","4");
        sparkConf.set("spark.shuffle.memoryFraction","0.5");
        sparkConf.set("spark.default.parallelism","120");

        JavaSparkContext ctx = new JavaSparkContext(sparkConf);


        // get hfs and make get rid of older files
        Configuration entries = ctx.hadoopConfiguration();
        FileSystem fs = FileSystem.get(entries);
        Path path = new Path(hdfsFileName);
        fs.delete(path, true); //

        //   number records
        long numberCreated = createLargeFastaFile(hdfsFileName, ctx, numberLines);

        System.err.println("Created " + numberCreated + "records");

        /*
        // now read as lines with textFile  - later we will read with a Hadoop InputFormat
        JavaRDD<String> lines = ctx.textFile(hdfsFileName);

        long numberReadLines = lines.count();


        // each record should be 2 lines
        long diff = (numberReadLines / 2) - numberCreated;

        System.out.println("using textFile lines " + numberLines + " numberCreated " + numberCreated + " difference " + diff);

        guaranteeDifferentMesosID();
         */
        // now read using a Hadoop InputFormat

        Class inputFormatClass = FastaInputFormat.class;
        Class keyClass = String.class;
        Class valueClass = String.class;

        JavaPairRDD<String, String> keyValues = ctx.newAPIHadoopFile(
                hdfsFileName,
                inputFormatClass,
                keyClass,
                valueClass,
                entries
        );

       // keyValues = keyValues.persist(StorageLevel.MEMORY_AND_DISK());
                //lines = lines.persist(StorageLevel.DISK_ONLY());
        //long pairs = keyValues.count();


        JavaRDD<DNAFragment> dnaData = keyValues.map(new Function<Tuple2<String, String>, DNAFragment>() {
            @Override
            public DNAFragment call(final Tuple2<String, String> v1) throws Exception {
                return new DNAFragment(v1._1(), v1._2());
            }
        });


       boolean forceShuffle = true;
        dnaData.coalesce(120, forceShuffle);

      //  dnaData = dnaData.persist(StorageLevel.DISK_ONLY());



              //lines = lines.persist(StorageLevel.DISK_ONLY());
        long numberFragments = dnaData.count();

        // each record should be 2 lines
        long diffFragments = numberFragments - numberCreated;

        System.out.println("using FastaInputFormat lines " + numberLines + " numberCreated " + numberCreated + " difference " + diffFragments);



    }


}
