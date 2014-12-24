package org.systemsbiology.xtandem.spark;

import com.lordjoe.distributed.input.*;
import org.apache.spark.api.java.*;


/**
 * org.systemsbiology.xtandem.spark.SparkTandemUtilities
 * User: Steve
 * Date: 9/22/2014
 */
public class SparkTandemUtilities {



    public static JavaPairRDD<String,String> parseSpectrumFile(String path,JavaSparkContext ctx) {

        Class inputFormatClass = MGFInputFormat.class;
        Class keyClass = String.class;
        Class valueClass = String.class;

        return ctx.hadoopFile(
                  path,
                   inputFormatClass,
                   keyClass,
                   valueClass
        );
    }


//    public static void main(String[] args) {
//        if(args.length == 0)    {
//            System.out.println("usage <file holding mgfs>");
//            return;
//        }
//        SparkConf sparkConf = new SparkConf().setAppName("JavaWordCount");
//        SparkUtilities.guaranteeSparkMaster(sparkConf);    // use local if no master provided
//
//        JavaSparkContext ctx = new JavaSparkContext(sparkConf);
//
//        JavaPairRDD<String, String> parsed =  parseSpectrumFile(args[0], ctx);
//
//        JavaRDD<Tuple2<String, IMeasuredSpectrum>> parsedSpectra =  parsed.map(new StringToSpectrum());
//
//        SparkUtilities.showRDD(parsedSpectra);
//
//
//    }
//

}
