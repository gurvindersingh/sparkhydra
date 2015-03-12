package com.lordjoe.distributed.test;

import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.storage.*;
import scala.*;

import java.util.*;

/**
 * com.lordjoe.distributed.test.JavaBigDataWordCount
 * This sample is written to force a sample with large amounts of data emulating some big data aspects of
 * a problem in biology I am working on -
 * <p/>
 * This is essentially WordCount
 * except that lines are filtered to just upper case words
 * then broken into String groups and all variants with different case are generated
 * so THE -> THE,ThE,THe,The,tHE,thE,tHe,the
 * when the groups get long - say 10 or 20 a LOT of variants are generated
 * <p/>
 * This sample is motivated by real problems in biology where we want to look at possible mutations in DNA fragments or
 * possible chemical modifications on amino acids in polypeptides - my largest Hadoop job does exactly that
 * <p/>
 * I have serious questions about
 * A - How to write the FlatMapFunction CaseVariationFunction as the output gets large - I think storing results in a List will not work
 * - what are other options
 * B are there other ways to do this
 */
public final class JavaBigDataWordCount {

    /**
     * drop all characters that are not letters
     *
     * @param s input string
     * @return output string
     */
    public static String dropNonLetters(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isLetter(c))
                sb.append(c);
        }

        return sb.toString();
    }

    /**
     * convert a string into a string holding only upper case letters
     *
     * @param inp input string
     * @return output string
     */
    public static String regularizeString(String inp) {
        inp = inp.trim();
        inp = inp.toUpperCase();
        return dropNonLetters(inp);
    }

    /**
     * convert a string into strings of length maxLength all letters and
     * upper case
     */
    public static class SubstringsMapFunction implements FlatMapFunction<String, String> {
        private final int maxLength;

        public SubstringsMapFunction(final int pMaxLength) {
            maxLength = pMaxLength;
        }

        public Iterable<String> call(String s) {
            s = regularizeString(s); // drop non-letters
            List<String> holder = new ArrayList<String>();
            for (int i = 0; i < s.length() - 2; i += maxLength) {
                holder.add(s.substring(i, Math.min(s.length(), i + maxLength)));
            }
            return holder;
        }
    }

    /**
     * return all cases of an upper case string so THE -> THE,ThE,THe,The,tHE,thE,tHe,the
     * In general the output is 2 to the Nth long where N is the input length
     */
    public static class CaseVariationFunction implements FlatMapFunction<String, String> {
        public Iterable<String> call(String s) {
            // HELP - I dont think a List will work for long inputs given WHAT else can I use
            List<String> holder = new ArrayList<String>();  // holds variants
            holder.add(s);
            makeVariations(s.toCharArray(), holder, 0);  // do real work filling in holder
            return holder;
        }

        /**
         * add to holder - NOTE I think a List is wrong for large inputs
         *
         * @param chars  characters input
         * @param holder - holder - or iterable holding results
         * @param index  - start changing case at this position
         */
        private void makeVariations(char[] chars, final List<String> holder, int index) {
            if (index < chars.length - 1)
                makeVariations(chars, holder, index + 1);
            if (Character.isUpperCase(chars[index])) {
                chars[index] = Character.toLowerCase(chars[index]);
                holder.add(new String(chars));
                if (index < chars.length - 1)
                    makeVariations(chars, holder, index + 1);
                chars[index] = Character.toUpperCase(chars[index]);
            }

        }
    }
//
//    // a few lines of text so we dont need to read a file is we dont want to
     public static final String GETTYSBURG =  "Fourscore and seven years ago our fathers brought forth\n" +
        " on this continent a new nation, conceived in liberty and dedicated to the proposition that all\n" +
        " men are created equal. Now we are engaged in a great civil war, testing whether that nation or\n" +
        " any nation so conceived and so dedicated can long endure. We are met on a great battlefield of\n" +
        " that war. We have come to dedicate a portion of that field as a final resting-place for those\n" +
        " who here gave their lives that that nation might live. It is altogether fitting and proper that\n" +
        " we should do this. But in a larger sense, we cannot dedicate, we cannot consecrate, we cannot\n" +
        " hallow this ground. The brave men, living and dead who struggled here have consecrated it far\n" +
        " above our poor power to add or detract. The world will little note nor long remember what we\n" +
        " say here, but it can never forget what they did here. It is for us the living rather to be\n" +
        " dedicated here to the unfinished work which they who fought here have thus far so nobly\n" +
        " advanced. It is rather for us to be here dedicated to the great task remaining before us--that\n" +
        " from these honored dead we take increased devotion to that cause for which they gave the last\n" +
        " full measure of devotion--that we here highly resolve that these dead shall not have died in\n" +
        " vain, that this nation under God shall have a new birth of freedom, and that government of the\n" +
        " people, by the people, for the people shall not perish from the earth.";


    /**
     * Main - runs essentially word count for all variants starting with length 10 (1000 variants) and
     * raising it by a factor of 4
     *
     * @param args if given a file to read - otherwise use the Gettysburg address
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        SparkConf sparkConf = new SparkConf().setAppName("JavaBigDataWordCount");
        sparkConf.set("spark.mesos.coarse", "true");
        sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer") ;

        Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined())   // use local over nothing
            sparkConf.setMaster("local");
        JavaSparkContext ctx = new JavaSparkContext(sparkConf);

        String[] linesTXT = GETTYSBURG.split("\n"); // the gettysburg address as lines of text
        JavaRDD<String> lines = ctx.parallelize(Arrays.asList(linesTXT));
        lines = lines.persist(StorageLevel.MEMORY_ONLY());

        // try for variant counts 10..maxVariantSize by 2 - Each case is 4 times the size
        int maxVariantSize = 16;
        for (int variantSize = 10; variantSize < maxVariantSize; variantSize += 2) {
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
