package com.lordjoe.distributed.test;

import com.lordjoe.distributed.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.*;
import org.apache.spark.storage.*;
import org.junit.*;
import scala.*;

import java.util.*;

/**
 * com.lordjoe.distributed.test.JavaBigDataWordCountTests
 * A stress test fot spark - generating all variants on case if a string this greatly expands the
 * data returned by a flatMap by 2**N
 * User: Steve
 * Date: 9/30/2014
 */
public class JavaBigDataWordCountTests implements Serializable {

    public static final int START_VARIENT_SIZE = 10;
    public static final int END_VARIENT_SIZE = 14;


    public static final String GETTYSBURG =
            "Four score and seven years ago our fathers brought forth, upon this continent, a new nation, conceived in liberty, and dedicated to the proposition that “all men are created equal.”\n" +
                    "Now we are engaged in a great civil war, testing whether that nation, or any nation so conceived, and so dedicated, can long endure. We are met on a great battle field of that war. We come to dedicate a portion of it, as a final resting place for those who died here, that the nation might live. This we may, in all propriety do.\n" +
                    "But, in a larger sense, we can not dedicate – we can not consecrate – we can not hallow, this ground – The brave men, living and dead, who struggled here, have hallowed it, far above our poor power to add or detract. The world will little note, nor long remember what we say here; while it can never forget what they did here.\n" +
                    "It is rather for us, the living, we here be dedicated to the great task remaining before us – that, from these honored dead we take increased devotion to that cause for which they here, gave the last full measure of devotion – that we here highly resolve these dead shall not have died in vain; that the nation, shall have a new birth of freedom, and that government of the people, by the people, for the people, shall not perish from the earth.\n";

    public static final String THE_VARIANTS = "THE,ThE,THe,The,tHE,thE,tHe,the";


    @Test
    public void testVariants() throws Exception {
        JavaBigDataWordCount.CaseVariationFunction caseVariationFunction = new JavaBigDataWordCount.CaseVariationFunction();

        Set<String> variants = new HashSet<String>();

        for (String s : caseVariationFunction.call("THE")) {
            Assert.assertFalse(variants.contains(s));
            variants.add(s);
        }
        // should be  THE -> THE,ThE,THe,The,tHE,tHe,the,ThE,The

        Set<String> expected = new HashSet<String>(Arrays.asList(THE_VARIANTS.split(",")));
        Assert.assertEquals(expected.size(), variants.size());
        for (String variant : variants) {
            Assert.assertTrue(expected.contains(variant));
        }

        List<String> l16 = (List) caseVariationFunction.call("FOUR");
        Assert.assertEquals(16, l16.size());
        List<String> l32 = (List) caseVariationFunction.call("ABCDE");
        Assert.assertEquals(32, l32.size());
    }

    @Test
    public void testVariantCount() throws Exception {
        SparkUtilities.setAppName("JavaBigDataWordCount");
        SparkUtilities.getSparkProperties().setProperty("spark.mesos.coarse", "true");

        JavaSparkContext ctx = SparkUtilities.getCurrentContext();

        String[] linesTXT = GETTYSBURG.split("\n"); // the gettysburg address as lines of text
        JavaRDD<String> lines = ctx.parallelize(Arrays.asList(linesTXT));
        lines = lines.persist(StorageLevel.MEMORY_ONLY());

        // try for varient counts 10..32 by 2 - Each case is 4 times the size
        for (int variantSize = START_VARIENT_SIZE; variantSize < END_VARIENT_SIZE; variantSize += 2) {
            long startMSec = System.currentTimeMillis();  // when did we start
            // Drop all non-letters - make upper case split into groups of size  variantSize
            JavaRDD<String> words = lines.flatMap(new JavaBigDataWordCount.SubstringsMapFunction(variantSize));

            // generate all variants with different case  should be  THE -> THE,ThE,THe,The,tHE,tHe,the,ThE,The
            JavaRDD<String> variants = words.flatMap(new JavaBigDataWordCount.CaseVariationFunction());

            // same as Java word count - we actually expect all counts to be 1
            JavaPairRDD<String, Integer> ones = variants.mapToPair(new PairFunction<String, String, Integer>() {
                @Override
                public Tuple2<String, Integer> call(String s) {
                    return new Tuple2<String, Integer>(s, 1);
                }
            });

            // same as Java word count - we actually expect all counts to be 1
            JavaPairRDD<String, Integer> counts = ones.reduceByKey(new Function2<Integer, Integer, Integer>() {
                @Override
                public Integer call(Integer i1, Integer i2) {
                    return i1 + i2;
                }
            });
            List<Tuple2<String, Integer>> output = counts.sortByKey().collect();
            long endMSec = System.currentTimeMillis();   // when did we finish

            System.out.println("Variant Size " + variantSize + " Size " + output.size() + " took " + (int) (endMSec - startMSec) / 1000);
        }
    }
}
