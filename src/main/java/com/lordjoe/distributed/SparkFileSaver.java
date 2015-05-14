package com.lordjoe.distributed;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.spark.api.java.*;
import org.systemsbiology.hadoop.*;

import javax.annotation.Nonnull;
import java.io.*;

/**
 * com.lordjoe.distributed.SparkFileSaver
 * User: Steve
 * Date: 1/16/2015
 */
public class SparkFileSaver {

    /**
     * save an RDD of text to a single file
     *
     * @param outPath path of the output file
     * @param data    rdd to save
     */
    public static void saveAsFile(@Nonnull String outFile, @Nonnull JavaRDD<String> data) {
        String pathStr = SparkUtilities.buildPath(outFile);
        Path path = new Path(pathStr);
        saveAsFile(path, data);
    }

    /**
     * save an RDD of text to a single file
     *
     * @param outPath path of the output file
     * @param data    rdd to save
     */
    public static void saveAsFile(@Nonnull Path outPath, @Nonnull JavaRDD<String> data) {

        String tempPathStr = outPath.toString() + ".temp";

        Path tempPath = new Path(tempPathStr);
        // Make sure the output files do not exist otherwise you will get
        //  org.apache.hadoop.mapred.FileAlreadyExistsException
        HadoopUtilities.expunge(tempPath, SparkUtilities.getHadoopFileSystem());

        data.saveAsTextFile(tempPath.toString());

        if(true)
            return; // todo this is just a test

        Configuration conf = SparkUtilities.getHadoopConfiguration();
        FileSystem srcFS = SparkUtilities.getHadoopFileSystem();
        boolean deleteSource = true;
        String addString = null;


        try {
            srcFS.delete(outPath,false);
            FileUtil.copyMerge(srcFS, tempPath, srcFS, outPath, deleteSource, conf, addString);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
        HadoopUtilities.expunge(tempPath, SparkUtilities.getHadoopFileSystem());
    }

}
