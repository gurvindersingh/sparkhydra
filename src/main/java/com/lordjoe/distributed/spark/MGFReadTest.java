package com.lordjoe.distributed.spark;


import org.apache.hadoop.fs.*;
import org.apache.hadoop.fs.FileSystem;

import java.io.*;
import java.util.*;


/**
 * com.lordjoe.distributed.spark.MGFReadTest
 * User: Steve
 * test mgf read for a large file
 * Date: 12/5/2014
 */
public class MGFReadTest {
    public static long targetSize; // rouhgly how many bytes in the target file
    public static final Random RND = new Random();
    private static long idIndex = 1;
    private static int fragmentLength = 100;
    public static final String[] BASES = {"A", "C", "G", "T"};


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


//    /**
//     * call with filename and filesize
//     * eg
//     * Random6GB.fasta 6000000000
//     *
//     * @param args
//     * @throws Exception
//     */
//    public static void main(String[] args) throws Exception {
//        String hdfsFileName = args[0];
//
//        System.out.println("Set Log to Warn");    // shut up the logger
//        Logger rootLogger = Logger.getRootLogger();
//        rootLogger.setLevel(Level.WARN);
//
//
//        SparkConf sparkConf = new SparkConf().setAppName("Test MGF Read");
//        Option<String> option = sparkConf.getOption("spark.master");
//        if (!option.isDefined())    // use local over nothing
//            sparkConf.setMaster("local[*]");
//
//        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
//
//        // get hfs and make get rid of older files
//        Configuration entries = ctx.hadoopConfiguration();
//        FileSystem fs = FileSystem.get(entries);
//        Path path = new Path(hdfsFileName);
//
//
//        Class inputFormatClass = MGFInputFormat.class;
//        Class keyClass = String.class;
//        Class valueClass = String.class;
//
//        JavaPairRDD<String, String> spectraAsStrings = ctx.newAPIHadoopFile(
//                hdfsFileName,
//                inputFormatClass,
//                keyClass,
//                valueClass,
//                entries
//        );
//
//        JavaPairRDD<String, IMeasuredSpectrum> spectra = spectraAsStrings.mapToPair(new MGFStringTupleToSpectrumTuple());
//
//        boolean forceShuffle = true;
//        JavaRDD<IMeasuredSpectrum> spectraToScore = spectra.values();
//     //   spectraToScore.coalesce(120, forceShuffle);
//
//        spectraToScore = spectraToScore.persist(StorageLevel.DISK_ONLY());
//
//        //  spectra = spectraAsStrings.persist(StorageLevel.MEMORY_AND_DISK());
//        //lines = lines.persist(StorageLevel.DISK_ONLY());
//        long pairs = spectraToScore.count();
//
//
//        System.out.println("read  " + pairs + " records");
//
//
//    }


}
