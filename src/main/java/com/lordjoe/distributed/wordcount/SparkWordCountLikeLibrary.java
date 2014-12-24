package com.lordjoe.distributed.wordcount;

/**
 * com.lordjoe.distributed.SparkWordCount
 * User: Steve
 * Date: 9/2/2014
 */
public class SparkWordCountLikeLibrary {


//    public static void main(final String[] args) {
//        ListKeyValueConsumer<String, Integer> consumer = new ListKeyValueConsumer();
//        File config = new File(args[0]);
//        LibraryBuilder lb = new LibraryBuilder(config);
//
//        SparkMapReduce handler = new SparkMapReduce("LibraryBuilder",new ProteinMapper(lb.getApplication()), new ProteinReducer(lb.getApplication()), IPartitionFunction.HASH_PARTITION, consumer);
//   //     SparkMapReduce handler = new SparkMapReduce(new NullTandemMapper(lb.getApplication()), new NullTandemReducer(lb.getApplication()), IPartitionFunction.HASH_PARTITION, consumer);
//         //     SparkMapReduce handler = new SparkMapReduce(new WordCountMapper(),new WordCountReducer(),IPartitionFunction.HASH_PARTITION,consumer);
//        JavaSparkContext ctx = handler.getCtx();
//
//
//        String fasta = args[1];
//
//        JavaPairRDD<String, String> parsed = SparkSpectrumUtilities.parseFastaFile(fasta, ctx);
//
//       // if not commented out this line forces proteins to be realized
//      //   parsed = SparkUtilities.realizeAndReturn(parsed, ctx);
//
//         JavaRDD<KeyValueObject<String, String>> proteins = SparkUtilities.fromTuples(parsed);
//
//   //     List<KeyValueObject<String, String>> fromFasta = JavaLibraryBuilder.parseFastaFile(fasta);
//   //     JavaRDD<KeyValueObject<String, String>> proteins = ctx.parallelize(fromFasta);
//        proteins = proteins.persist(StorageLevel.MEMORY_ONLY());
//
//         proteins = SparkUtilities.realizeAndReturn(proteins, ctx);
//
//
//        handler.performSourceMapReduce(proteins);
//
//        Iterable<KeyValueObject<String, String>> answer = handler.collect();
//        for (KeyValueObject<String, String> o : answer) {
//            System.out.println(o.toString());
//        }
//
//    }
}
