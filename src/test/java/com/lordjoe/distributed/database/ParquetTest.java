package com.lordjoe.distributed.database;

import com.lordjoe.distributed.SparkUtilities;
import junit.framework.Assert;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.DataFrame;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.junit.Before;
import org.junit.Test;
import scala.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

/**
 * Created by guri on 5/15/15.
 */
public class ParquetTest implements  Serializable {

    private final static String DB_FNAME = "parquetTest.parquet";
    public static class TestPeptide implements Serializable {
        int bin;
        int value;

        public TestPeptide(int bin, int value) {
            this.bin = bin;
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public int getBin() {
            return bin;
        }

        public void setBin(int bin) {
            this.bin = bin;
        }

        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestPeptide that = (TestPeptide) o;

            if (bin != that.bin) return false;
            if (value != that.value) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = bin;
            result = 31 * result + value;
            return result;
        }

        @Override
        public String toString() {
            return "TestPeptide{" +
                    "bin=" + bin +
                    ", value=" + value +
                    '}';
        }
    }

    public static class TestSpectrum implements Serializable {
        int bin;
        int value;

        public TestSpectrum(int value, int bin) {
            this.value = value;
            this.bin = bin;
        }

        public int getValue() {
            return value;
        }

        public int getBin() {
            return bin;
        }

        public void setBin(int bin) {
            this.bin = bin;
        }

        public void setValue(int value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TestSpectrum that = (TestSpectrum) o;

            if (bin != that.bin) return false;
            if (value != that.value) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = bin;
            result = 31 * result + value;
            return result;
        }

        @Override
        public String toString() {
            return "TestSpectrum{" +
                    "bin=" + bin +
                    ", value=" + value +
                    '}';
        }
    }

    public static Integer score(TestSpectrum t, List<TestPeptide> peps) {
        Integer ret = 0;
        for (TestPeptide pep : peps) {
            if(pep.getValue() % t.getValue() == 0) {
                ret = Math.max(ret, pep.getValue());
            }
        }
        return ret;
    }

    @Before
    public void dropDatabase() {
        DatabaseUtilities.removeParaquetDatabase(DB_FNAME);
    }


    public static final Random RND = new Random();
    @Test
    public void testName() throws Exception {
        List<TestSpectrum> testSpectrums = new ArrayList<TestSpectrum>();
        List<TestPeptide> testPeptides = new ArrayList<TestPeptide>();
        for (int i = 0; i < 1000000; i++) {
            TestPeptide p = new TestPeptide(RND.nextInt(200)+1, RND.nextInt(10000));
            testPeptides.add(p);
            TestSpectrum s = new TestSpectrum(RND.nextInt(200)+1, RND.nextInt(10000));
            testSpectrums.add(s);
        }
        //createDatabase(testPeptides);
        //List<TestPeptide> readP = readDatabase();
        //checkData(testPeptides, readP);
        JavaSparkContext currentContext = SparkUtilities.getCurrentContext();
        JavaRDD<TestSpectrum> spectraRDD = currentContext.parallelize(testSpectrums);
        JavaRDD<TestPeptide> peptideJavaRDD = currentContext.parallelize(testPeptides);
        JavaPairRDD<Integer, TestSpectrum> binnedSpectra = spectraRDD.mapToPair(new PairFunction<TestSpectrum, Integer, TestSpectrum>() {
            @Override
            public Tuple2<Integer, TestSpectrum> call(TestSpectrum testSpectrum) throws Exception {
                return new Tuple2<Integer, TestSpectrum>(testSpectrum.getBin(), testSpectrum);
            }
        });


        JavaPairRDD<Integer, TestPeptide> binnedPeptide = peptideJavaRDD.mapToPair(new PairFunction<TestPeptide, Integer, TestPeptide>() {
            @Override
            public Tuple2<Integer, TestPeptide> call(TestPeptide testPeptide) throws Exception {
                return new Tuple2<Integer, TestPeptide>(testPeptide.getBin(), testPeptide);
            }
        });

        JavaPairRDD<Integer, ArrayList<TestPeptide>> integerArrayListJavaPairRDD = SparkUtilities.mapToKeyedList(binnedPeptide);
        JavaPairRDD<Integer, Tuple2<TestSpectrum, ArrayList<TestPeptide>>> joinedSpectra = binnedSpectra.join(integerArrayListJavaPairRDD);

        JavaRDD<Tuple2<TestSpectrum, Integer>> result = joinedSpectra.values().map(new Function<Tuple2<TestSpectrum, ArrayList<TestPeptide>>, Tuple2<TestSpectrum, Integer>>() {
            @Override
            public Tuple2<TestSpectrum, Integer> call(Tuple2<TestSpectrum, ArrayList<TestPeptide>> v) throws Exception {
                Integer score = score(v._1(), v._2());
                return new Tuple2<TestSpectrum, Integer>(v._1(), score);
            }
        });

        List<Tuple2<TestSpectrum, Integer>> values = result.collect();
        for (Tuple2<TestSpectrum, Integer> value : values) {
      //      System.out.println(value._2());
        }

    }

    public static void createDatabase(List<TestPeptide> testPeptides) {
        JavaSparkContext sc = SparkUtilities.getCurrentContext();
        JavaRDD<TestPeptide> prdd = sc.parallelize(testPeptides);
        SQLContext sqlContext = SparkUtilities.getCurrentSQLContext();
        DataFrame schemaPrdd = sqlContext.applySchema(prdd, TestPeptide.class);
        schemaPrdd.saveAsParquetFile(DB_FNAME);
    }

    public static List<TestPeptide> readDatabase() {
        SQLContext sqlContext = SparkUtilities.getCurrentSQLContext();
        DataFrame schemaPrdd = sqlContext.load(DB_FNAME);
        List<TestPeptide> testPeptides = new ArrayList<TestPeptide>();
        List <Row> rows = schemaPrdd.collectAsList();
        for (Row row : rows) {
            testPeptides.add(new TestPeptide(row.getInt(0), row.getInt(1)));
        }
        return testPeptides;
    }

    public static void checkData(List<TestPeptide> src, List<TestPeptide> target) {
        Assert.assertEquals(src.size(),target.size());
        HashSet<TestPeptide> srcH = new HashSet<TestPeptide>(src);
        HashSet<TestPeptide> targetH = new HashSet<TestPeptide>(target);
        srcH.removeAll(targetH);
        Assert.assertTrue(srcH.isEmpty());
    }
}

