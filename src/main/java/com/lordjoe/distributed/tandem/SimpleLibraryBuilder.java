package com.lordjoe.distributed.tandem;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.util.*;
import org.apache.spark.api.java.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.tandem.LibraryBuilder
 * User: Steve
 * Date: 9/24/2014
 */
public class SimpleLibraryBuilder {


    public SimpleLibraryBuilder(File congiguration) {
        SparkUtilities.setAppName("LibraryBuilder");

     }




    public static void main(String[] args) {
        if(args.length == 0)    {
             System.out.println("usage configFile fastaFile");
             return;
         }
        File config = new File(args[0]);
        String fasta = args[1] ;
        SimpleLibraryBuilder lb = new SimpleLibraryBuilder(config);

        ListKeyValueConsumer<String,String> consumer = new ListKeyValueConsumer();

        JavaSparkContext ctx = SparkUtilities.getCurrentContext();
        List<KeyValueObject<String, String>> fromFasta =  JavaLibraryBuilder.parseFastaFile(fasta);
        JavaRDD<KeyValueObject<String, String>> proteins = ctx.parallelize(fromFasta);

      // if not commented out this line forces proteins to be realized
        proteins = SparkUtilities.realizeAndReturn(proteins );


 //       ListKeyValueConsumer<String,String> consumer = new ListKeyValueConsumer();
 //        SparkMapReduce handler = new SparkMapReduce(new WordCountMapper(),new WordCountReducer(),IPartitionFunction.HASH_PARTITION,consumer);



        SparkMapReduce handler = new SparkMapReduce("Null Mapper",new NullStringMapper(),
                new NullStringReducer(),IPartitionFunction.HASH_PARTITION,consumer );
  //      SparkMapReduce handler = new SparkMapReduce(pm, pr,IPartitionFunction.HASH_PARTITION );


       //  proteins = proteins.persist(StorageLevel.MEMORY_ONLY());
     //   proteins = SparkUtilities.realizeAndReturn(proteins, ctx);

        handler.performSourceMapReduce(proteins);

        Iterable<KeyValueObject<String, String>> list = handler.collect();

        for (KeyValueObject<String, String> keyValueObject : list) {
            System.out.println(keyValueObject);
        }


    }
}
