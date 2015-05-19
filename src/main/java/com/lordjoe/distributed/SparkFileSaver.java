package com.lordjoe.distributed;

import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.apache.spark.api.java.*;
import org.systemsbiology.hadoop.*;

import javax.annotation.Nonnull;
import java.io.*;
import java.util.Arrays;

/**
 * com.lordjoe.distributed.SparkFileSaver
 * User: Steve
 * Date: 1/16/2015
 */
public class SparkFileSaver {

    /**
     * save an RDD of text to a single file
     *
     * @param outFile path of the output file
     * @param data    rdd to save
     */
    public static void saveAsFile(@Nonnull String outFile, @Nonnull JavaRDD<String> data) {
        String pathStr = SparkUtilities.buildPath(outFile);
        Path path = new Path(pathStr);
        saveAsFile(path, data, null, null);
    }

    /**
     * save an RDD of text to a single file
     *
     * @param outPath path of the output file
     * @param data    rdd to save
     */
    public static void saveAsFile(@Nonnull Path outPath, @Nonnull JavaRDD<String> data, String header, String footer) {

        if(header == null || header.length()  < 1)
            header = "  ";
        if(footer == null || footer.length()  < 1)
            footer = "  ";


        String tempPathStr = outPath.toString() + ".temp";

        Path tempPath = new Path(tempPathStr);
        // Make sure the output files do not exist otherwise you will get
        //  org.apache.hadoop.mapred.FileAlreadyExistsException
        HadoopUtilities.expunge(tempPath, SparkUtilities.getHadoopFileSystem());

        data.saveAsTextFile(tempPath.toString());

        Configuration conf = SparkUtilities.getHadoopConfiguration();
        FileSystem srcFS = SparkUtilities.getHadoopFileSystem();
        boolean deleteSource = true;


        try {
            srcFS.delete(outPath,false);
            copyMerge(srcFS, tempPath, srcFS, outPath, deleteSource, conf,
                    header.substring(1, header.length()-1), footer.substring(1, footer.length()-1));
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
        HadoopUtilities.expunge(tempPath, SparkUtilities.getHadoopFileSystem());
    }

    /** Copy all files in a directory to one output file (merge). */
    public static boolean copyMerge(FileSystem srcFS, Path srcDir,
                                    FileSystem dstFS, Path dstFile,
                                    boolean deleteSource,
                                    Configuration conf, String header, String footer) throws IOException {

        if (!srcFS.getFileStatus(srcDir).isDirectory())
            return false;

        OutputStream out = dstFS.create(dstFile);

        try {
            FileStatus contents[] = srcFS.listStatus(srcDir);
            Arrays.sort(contents);
            if (header != null)
                out.write(header.getBytes("UTF-8"));
            for (int i = 0; i < contents.length; i++) {
                if (contents[i].isFile()) {
                    InputStream in = srcFS.open(contents[i].getPath());
                    try {
                        IOUtils.copyBytes(in, out, conf, false);
                    } finally {
                        in.close();
                    }
                }
            }
            if (footer != null)
                out.write(footer.getBytes("UTF-8"));
        } finally {
            out.close();
        }


        if (deleteSource) {
            return srcFS.delete(srcDir, true);
        } else {
            return true;
        }
    }


}
