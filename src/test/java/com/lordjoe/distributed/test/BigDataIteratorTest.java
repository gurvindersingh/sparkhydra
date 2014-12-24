package com.lordjoe.distributed.test;

import org.apache.commons.collections4.iterators.*;
import org.apache.spark.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;

import java.math.*;
import java.util.*;

/**
 * com.lordjoe.distributed.test.BigDataIteratorTest
 * User: Steve
 * Date: 10/2/2014
 */
public class BigDataIteratorTest {
     public static class GenerateMultiples implements FlatMapFunction<Long, BigInteger> {
            private final long maxMultiples;

            public GenerateMultiples  (final long maxMultiples ) {
                this.maxMultiples = maxMultiples ;
            }

            public Iterable<BigInteger> call(final Long l) {
                Iterator<BigInteger> ret = new Iterator<BigInteger>() {
                     long factor = 1;
                     @Override
                    public boolean hasNext() {
                        return factor <= maxMultiples;
                    }

                    @Override
                    public BigInteger next() {
                        return new BigInteger(Long.toString(factor++ * l));
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException("no remove");
                    }
                };
                return  new IteratorIterable(ret);
            }
        }

    /**
     * make an iterator with totalElements in a flatmap and sum the result
     * this if
     * @param totalElements
     * @param pCtx
     * @return
     */
    public static BigInteger generateMultiplesSum(long totalElements,final JavaSparkContext pCtx) {
        JavaRDD<Long> numberRDD = pCtx.parallelize(Arrays.asList(numbers));
        // create 10000 multiples - this will work
        JavaRDD<BigInteger> multples = numberRDD.flatMap(new GenerateMultiples(totalElements));

        BigInteger sum = multples.reduce(new Function2<BigInteger, BigInteger, BigInteger>() {
            @Override
            public BigInteger call(final BigInteger l1, final BigInteger l2) throws Exception {
                return l1.add(l2);
            }
        });

        return sum;
    }

    public static final Long[] numbers = { new Long(7),new Long(11),new Long(19),new Long(29),new Long(31),new Long(71) };

    public static final int SIZE_FACTOR = 10;
    public static final int START_FACTOR = 10000000;

    public static void main(String[] args) {

        /**
          * do the same math as the spark look without the sparky stuff
          */
         long maxMultiples = START_FACTOR;
         for (int i = 0; i < 5; i++) {  // every look we increase the number of multiples  by 100
             BigInteger sum = new BigInteger("0");
             long start = System.currentTimeMillis();
             GenerateMultiples generateMultiples = new GenerateMultiples(maxMultiples);
             for (int j = 0; j < numbers.length; j++) {
                 Iterable<BigInteger> values = generateMultiples.call(numbers[j]);
                 Iterator<BigInteger> valx = values.iterator();
                 while(valx.hasNext())
                     sum  = sum.add(valx.next()) ;
               }
             long end = System.currentTimeMillis();
             System.out.println("multiples " + maxMultiples + " sum = " + sum + " in " + (end - start) / 1000 + "sec");
              maxMultiples *= SIZE_FACTOR;
         }


        SparkConf sparkConf = new SparkConf().setAppName("BigDataIteratorTest");
        sparkConf.set("spark.mesos.coarse", "true");
        scala.Option<String> option = sparkConf.getOption("spark.master");
            if (!option.isDefined())   // use local over nothing
                sparkConf.setMaster("local");
        JavaSparkContext ctx = new JavaSparkContext(sparkConf);


        /**
         * now use spark and iterators
         */
        maxMultiples = START_FACTOR;
        for (int i = 0; i < 5; i++) {  // every look we increase the number of multiples  by 100
            long start = System.currentTimeMillis();
            BigInteger sum = generateMultiplesSum(maxMultiples,ctx);
            long end = System.currentTimeMillis();
            System.out.println("multiples " + maxMultiples + " sum = " + sum + (end - start) / 1000 + "sec");
            maxMultiples *= SIZE_FACTOR;
        }
    }

}
