package com.lordjoe.distributed.spark;

//import org.junit.*;
//import scala.collection.*;
//import scala.collection.Seq;
//import scala.collection.immutable.*;
//
///**
// * com.lordjoe.distributed.spark.ScalaAdaptorsTest
// * User: Steve
// * Date: 12/28/2014
// */
//public class ScalaAdaptorsTest {
//
//    public static final int ONE_K = 1024;
//    public static final int ONE_MEG = ONE_K * ONE_K;
//    public static final int ONE_GIG = ONE_K * ONE_MEG;
//
//    public static final int MAX_RANGE = 1 * ONE_MEG;
//    public static final int MAX_STREAM = 1 * ONE_MEG;  // note runs out of memory if 1 gig
//
//    @Test
//    public void testRange() throws Exception {
//        Seq<Object> items = ScalaJavaAdaptors.makeRange(MAX_RANGE);
//        Assert.assertEquals(items.size(),MAX_RANGE);
//        int index = 0;
//        Stream<Object> objectStream = items.toStream();
//        final Iterator<Object> iterator = objectStream.iterator();
//        while ( iterator.hasNext()) {
//           Integer ix = (Integer)iterator.next();
//            Assert.assertEquals(index++,(int)ix);
//        }
//    }
//    @Test
//      public void testStream() throws Exception {
//          Stream<Object> items = ScalaJavaAdaptors.makeStream(MAX_STREAM);
//          Assert.assertEquals(items.size(),MAX_STREAM);
//          int index = 0;
//          final Iterator<Object> iterator = items.iterator();
//          while ( iterator.hasNext()) {
//             Integer ix = (Integer)iterator.next();
//              Assert.assertEquals(index++,(int)ix);
//          }
//      }
//  }
