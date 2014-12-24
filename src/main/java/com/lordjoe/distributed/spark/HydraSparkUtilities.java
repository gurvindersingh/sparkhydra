package com.lordjoe.distributed.spark;

import com.lordjoe.distributed.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.spark.api.java.*;
import org.systemsbiology.common.*;
import org.systemsbiology.hadoop.*;

import javax.annotation.*;
import java.io.*;

/**
 * com.lordjoe.distributed.spark.HydraSparkUtilities
 * User: Steve
 * Date: 12/19/2014
 */
public class HydraSparkUtilities {
    private static JobSizeEnum jobSize = JobSizeEnum.Medium;

    public static JobSizeEnum getJobSize() {
        return jobSize;
    }

    public static void setJobSize(final JobSizeEnum pJobSize) {
        jobSize = pJobSize;
    }

    /**
      * return a reference to the current hadoop file system using the currrent Spark Context
      */
     public static IFileSystem getHadoopFileSystem() {
         final IFileSystem accessor;
         try {
             JavaSparkContext ctx = SparkUtilities.getCurrentContext();
             Configuration entries = ctx.hadoopConfiguration();
             accessor = new HDFSAccessor(FileSystem.get(entries));
         }
         catch (IOException e) {
             throw new RuntimeException(e);

         }
         return accessor;
     }

    /** Needed to fake reading a file
      *
      * @param
    ctx
      * @param path
      * @return
      */
     public static InputStream readFrom(String path) {
         IFileSystem accessor = getHadoopFileSystem();
         path = SparkUtilities.mapToPath(path); // make sure we understand the path
         return accessor.openPath(path);
     }

    /**
       * return the content of an existing file in the path
       *
       * @param path
       * @return the content of what is presumed to be a text file as an strings  one per line
       */
      public static
      @Nonnull
      String[] pathLines(@Nonnull String path) {
          String wholeFile = getPathContent(path);
          return wholeFile.split("\n");
      }

      /**
       * return the content of an existing file in the path
       *
       * @param path
       * @return the content of what is presumed to be a text file as a string
       */
      public static
      @Nonnull
      String getPathContent(@Nonnull String path) {
          IFileSystem accessor = getHadoopFileSystem();
          path = SparkUtilities.mapToPath(path); // make sure we understand the path
          return accessor.readFromFileSystem(path);
      }


}
