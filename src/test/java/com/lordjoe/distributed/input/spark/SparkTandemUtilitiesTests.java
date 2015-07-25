package com.lordjoe.distributed.input.spark;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.input.*;
import com.lordjoe.distributed.spectrum.*;
import org.apache.spark.api.java.*;

/**
 * org.systemsbiology.xtandem.spark.SparkTandemUtilitiesTests
 * User: Steve
 * Date: 9/22/2014
 */
public class SparkTandemUtilitiesTests {


    public static JavaPairRDD<String,String> parseSpectrumFile(String path,JavaSparkContext ctx) {

        if(path.toLowerCase().endsWith(".mgf"))
              return parseAsMGF(path,ctx);
        if(path.toLowerCase().endsWith(".mgf"))
            return parseAsMGF(path,ctx);
          if(path.toLowerCase().endsWith(".mzxml"))
            return parseAsMZXML(path,ctx);
        throw new UnsupportedOperationException("Cannot understand extension " + path);
     }

    public static JavaPairRDD<String,String> parseAsMZXML(final String path, final JavaSparkContext ctx) {
        Class inputFormatClass = SparkSpectrumUtilities.MZXMLInputFormat.class;
         Class keyClass = String.class;
         Class valueClass = String.class;

         return ctx.newAPIHadoopFile(
                   path,
                 inputFormatClass,
                     keyClass,
                    valueClass,
                  ctx.hadoopConfiguration()
         );
    }



    public static JavaPairRDD<String,String> parseAsOldMGF(final String path, final JavaSparkContext ctx) {
        Class inputFormatClass = MGFOldInputFormat.class;
         Class keyClass = String.class;
         Class valueClass = String.class;

         return ctx.hadoopFile(
                   path,
                 inputFormatClass,
                     keyClass,
                    valueClass
           );
    }


    public static JavaPairRDD<String,String> parseAsTextMGF(final String path, final JavaSparkContext ctx) {
        Class inputFormatClass = MGFTextInputFormat.class;
         Class keyClass = String.class;
         Class valueClass = String.class;

         return ctx.newAPIHadoopFile(
                   path,
                 inputFormatClass,
                     keyClass,
                    valueClass,
                  ctx.hadoopConfiguration()
         );
    }

    public static JavaPairRDD<String,String> parseAsMGF(final String path, final JavaSparkContext ctx) {
        Class inputFormatClass = MGFInputFormat.class;
         Class keyClass = String.class;
         Class valueClass = String.class;

         return ctx.newAPIHadoopFile(
                   path,
                 inputFormatClass,
                     keyClass,
                    valueClass,
                  ctx.hadoopConfiguration()
         );
    }


    public static JavaPairRDD<String,String> parseSpectrumFileOld(String path,JavaSparkContext ctx) {

        Class inputFormatClass = MGFOldInputFormat.class;
        Class keyClass = String.class;
        Class valueClass = String.class;

        return ctx.hadoopFile(
                  path,
                   inputFormatClass,
                   keyClass,
                   valueClass
        );
    }

    public static void main(String[] args) {
        if(args.length == 0)    {
            System.out.println("usage <file holding mgfs>");
            return;
        }
        SparkUtilities.setAppName("MGF Parser");

        JavaSparkContext ctx = SparkUtilities.getCurrentContext();

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            JavaPairRDD<String, String> parsed =  parseSpectrumFile(args[i], ctx);
            SparkUtilities.showPairRDD(parsed);

        }



    }

}
