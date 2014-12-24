package com.lordjoe.distributed.spark;

/**
 * com.lordjoe.distributed.spark.LargeFileTest
 * User: Steve
 * Date: 12/5/2014
 */

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import scala.*;

import java.io.*;
import java.io.Serializable;
import java.util.*;


/**
 * com.lordjoe.spark.LargeFileSample
 * User: Steve
 * Creates a target fasta file - looks like
 *   >1
 TCCTTACGGAGTTCGGGTGTTTATCTTACTTATCGCGGTTCGCTGCCGCTCCGGGAGCCCGGATAGGCTGCGTTAATACCTAAGGAGCGCGTATTGAAAA
 >2
 GTCTGATCTAAATGCGACGACGTCTTTAGTGCTAAGTGGAACCCAATCTTAAGACCCAGGCTCTTAAGCAGAAACAGACCGTCCCTGCCTCCTGGAGTAT
 >3
 etc but containing gigabytes of random data
 call
    filename desiredSize <saveasTextFile>
     like
     myfile.fasta 5000000000
    should make a 5G file
 * call
 * Submit like
 *  spark-submit --class  LargeFileTest  ~/SparkJar.jar    Test6G.fasta 60000000000-executor-memory 12G
 * Date: 12/5/2014
 */
public class LargeFileTest {

    public static final int COLLECTION_SIZE = 500; // 00;

    public static long targetSize; // rouhgly how many bytes in the target file
    public static final Random RND = new Random();
    private static long idIndex = 1;
    private static int fragmentLength = 100;
    public static final String[] BASES = {"A", "C", "G", "T"};

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
     * function to boost the number of elements by multiplyer
     */
    private static class MultiplyFragments implements FlatMapFunction<DNAFragment, DNAFragment> {
        private final int multiplyer;

        private MultiplyFragments(final int pMultiplyer) {
            multiplyer = pMultiplyer;
        }

        @Override
        public Iterable<DNAFragment> call(final DNAFragment t) throws Exception {
            List<DNAFragment> newSet = new ArrayList<DNAFragment>();
            for (int i = 0; i < multiplyer - 1; i++) {
                newSet.add(makeDNAFragment());
            }
            newSet.add(t);
            return newSet;
        }
    }

    /**
     * create a file with name hdfsFileName on HDFS file system
     * of size at least TARGET_FILE_SIZE
     *
     * @param hdfsFileName
     * @param ctx
     * @return  number bytes written very roughly
     * @throws IOException
     */
    public static long createLargeFastaFile(String hdfsFileName, JavaSparkContext ctx, boolean useSaveAsText) throws Exception {
        JavaRDD<DNAFragment> mySet = getDnaFragmentJavaRDD(ctx);


        if (useSaveAsText) {
            guaranteeDifferentMesosID();
            mySet.saveAsTextFile(hdfsFileName);
            return idIndex - 1;
        }
        else {
            // write directly to a simgle HDFS File
            Configuration entries = ctx.hadoopConfiguration();
            FileSystem fs = FileSystem.get(entries);
            Path path = new Path(hdfsFileName);
            fs.delete(path, true); //

            FSDataOutputStream fsDataOutputStream = fs.create(path);
            PrintWriter out = new PrintWriter(fsDataOutputStream);
            guaranteeDifferentMesosID();
            Iterator<DNAFragment> dnaFragmentIterator = mySet.toLocalIterator();
            long numberWritten = 0;
            while (dnaFragmentIterator.hasNext()) {
                out.println(dnaFragmentIterator.next());
                numberWritten++;
            }
            out.close();
            System.err.println("Wrote " + numberWritten + " records");
            return numberWritten; // number created

        }

    }

    /**
     * make am RDD with 25 million (5000 * 5000) fragments
     * @param ctx
     * @return
     */
    public static JavaRDD<DNAFragment> getDnaFragmentJavaRDD(final JavaSparkContext ctx) {
        // Seet with a number of DNA Fragments
        List<DNAFragment> firstSet = new ArrayList<DNAFragment>();
        for (int i = 0; i < COLLECTION_SIZE; i++) {
            firstSet.add(makeDNAFragment());
        }
        JavaRDD<DNAFragment> mySet = ctx.parallelize(firstSet);

        boolean forceShuffle = true;
        mySet = mySet.coalesce(100, forceShuffle); // do work on partitions

        // grow by a factor of ten until we exceed the desired size
        long dataHeld = COLLECTION_SIZE * fragmentLength;
        mySet = mySet.flatMap(new MultiplyFragments(COLLECTION_SIZE));
        return mySet;
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
        fragmentLength = (int) (1 + (targetSize / (COLLECTION_SIZE * COLLECTION_SIZE)));
        System.err.println("Writing " + targetSize + " with fragment length " + fragmentLength);

        boolean useSaveAsText = false;
        if (args.length > 2)
            useSaveAsText = "true".equals(args[2]);

        SparkConf sparkConf = new SparkConf().setAppName("TestLargeFiles");
        Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined())    // use local over nothing
            sparkConf.setMaster("local[*]");

        JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        // get hfs and make get rid of older files
        Configuration entries = ctx.hadoopConfiguration();
        FileSystem fs = FileSystem.get(entries);
        Path path = new Path(hdfsFileName);
        fs.delete(path, true); //

        //   number records
        long numberCreated = createLargeFastaFile(hdfsFileName, ctx, useSaveAsText);

        System.err.println("Created " + numberCreated + "records");

        // delay to fix a bug
        guaranteeDifferentMesosID();

        // now read as lines  - later we will read with a Hadoop InputFormat
        JavaRDD<String> lines = ctx.textFile(hdfsFileName);

        //lines = lines.persist(StorageLevel.DISK_ONLY());
        long numberLines = lines.count();

         // each record should be 2 lines
        long diff = (numberLines / 2) - numberCreated;

        System.out.println("lines " + numberLines + " numberCreated " + numberCreated + " difference " + diff);
    }


}
