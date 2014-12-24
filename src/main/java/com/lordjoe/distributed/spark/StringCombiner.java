package com.lordjoe.distributed.spark;

import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;

/**
 * com.lordjoe.distributed.spark.StringCombiner
 * User: Steve
 * Date: 10/13/2014
 */
public class StringCombiner {

    /**
     *  combine values using a separator
     * @param inp
     * @param separtator
     * @return
     */
    public static JavaPairRDD<String, String> concatValuesWithSeparator(JavaPairRDD<String, String> inp, String separtator) {
        return inp.combineByKey(STARTER,new CONTINUER(separtator) ,new CONTINUER( separtator) ) ;
    }

    public static final Function<String, String> STARTER = new Function<String, String>() {
        @Override
        public String call(final String v1) throws Exception {
            return v1;
        }
    };

    public static class CONTINUER implements Function2<String, String, String> {
        private final String sepatator;

        public CONTINUER(String s) {
            sepatator = s;
        }

        @Override
        public String call(final String v1, final String v2) throws Exception {
            return v1 + sepatator + v2;
        }
    }

    ;

    public static final Function2<String, String, String> ENDER = new Function2<String, String, String>() {
        @Override
        public String call(final String v1, final String v2) throws Exception {
            return v1 + "," + v2;
        }
    };

}
