package com.lordjoe.distributed.spark;

import org.apache.spark.*;
import org.apache.spark.api.java.*;
import scala.*;

import javax.annotation.*;
import java.util.*;

/**
 * com.lordjoe.distributed.spark.RDDGeneratorTests
 * User: Steve
 * Date: 12/9/2014
 */
public class RDDGeneratorTests {
    public static class XYPoint implements Serializable {
        public final double x;
        public final double y;

        public XYPoint(final double pX, final double pY) {
            x = pX;
            y = pY;
        }

        public double radius() {
            return Math.sqrt(x * x + y * y);
        }

        public String toString() {
            return String.format("%10.3f", x) + "," + String.format("%10.3f", y);
        }
    }

    public static class RadiusComparator implements Comparator<XYPoint>,Serializable {
        @Override
        public int compare(final XYPoint o1, final XYPoint o2) {
            double r1 = o1.radius();
            double r2 = o1.radius();
            return java.lang.Double.compare(r1, r2);
        }
    }

    public static final Random RND = new Random();

    public static class PointGenerator implements ObjectGenerator<XYPoint> {
        /**
         * create an instance of the known type
         *
         * @return
         */
        @Nonnull
        @Override
        public XYPoint generateObject() {
            return new XYPoint(RND.nextGaussian(), RND.nextGaussian());
        }
    }

    public static JavaSparkContext buildJavaSparContext(String appName) {
        SparkConf sparkConf = new SparkConf().setAppName(appName);
        Option<String> option = sparkConf.getOption("spark.master");
        if (!option.isDefined())    // use local over nothing
            sparkConf.setMaster("local[*]");
        else
            sparkConf.set("spark.shuffle.manager", "HASH");

        sparkConf.set("spark.mesos.coarse", "true");  // always allow a job to be killed
        sparkConf.set("spark.ui.killEnabled", "true");  // always allow a job to be killed
        //      sparkConf.set("spark.mesos.executor.memoryOverhead","1G");
        sparkConf.set("spark.executor.memory", "4G");
        sparkConf.set("spark.task.cpus", "4");
          sparkConf.set("spark.default.parallelism", "120");
        return new JavaSparkContext(sparkConf);
    }

    public static final int ONE_MEG = 1024 * 1024;
    public static final int ONE_GIG = ONE_MEG * 1024;

    public static void main(String[] args) {
        JavaSparkContext ctx = buildJavaSparContext("Generate RDD Test");

        JavaRDD<XYPoint> points = RDDGenerator.generateRDD(ctx, new PointGenerator(), ONE_GIG);

      //  points = points.persist(StorageLevel.DISK_ONLY());
      //  long count = points.count();

        XYPoint maxPoint = points.max(new RadiusComparator());
        System.out.println(maxPoint);
    }


}
