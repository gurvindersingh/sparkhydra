package com.lordjoe.distributed.hydra.protein;

import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.systemsbiology.xtandem.peptide.*;

/**
 * com.lordjoe.distributed.hydra.protein.WriteLibraryFiles
 * User: Steve
 * Date: 10/16/2014
 */
public class WriteLibraryFiles {

    public static final int NUMBER_PARTITIONS = 500;
    public static JavaPairRDD<Integer,WriteLibraryFiles> asLibraryFiles(JavaPairRDD<Integer,IPolypeptide> byMZ)
    {
         return byMZ.combineByKey(new Function<IPolypeptide, WriteLibraryFiles>() {
             @Override
             public WriteLibraryFiles call(final IPolypeptide v1) throws Exception {
                 return null;
             }
         },
                 new Function2<WriteLibraryFiles, IPolypeptide, WriteLibraryFiles>() {
                     @Override
                     public WriteLibraryFiles call(final WriteLibraryFiles v1, final IPolypeptide v2) throws Exception {
                         return null;
                     }
                 }
                 ,new Function2<WriteLibraryFiles, WriteLibraryFiles, WriteLibraryFiles>() {
                     @Override
                     public WriteLibraryFiles call(final WriteLibraryFiles v1, final WriteLibraryFiles v2) throws Exception {
                         return null;
                     }
                 } ,
                 new Partitioner() {
                     @Override
                     public int numPartitions() {
                         return 0;
                     }

                     @Override
                     public int getPartition(final Object key) {
                         return (Integer)key % NUMBER_PARTITIONS;
                     }
                 }



         );
    }


}
