package com.lordjoe.distributed.spark

import java.lang.Integer;
/**
 * com.lordjoe.distributed.spark.ScalaJavaAdaptors 
 * User: Steve
 * Date: 12/28/2014
 */
object ScalaJavaAdaptors {
  def makeRange(max:java.lang.Integer ):Seq[Int] = {
    makeRange(0,max);
  }
  def makeRange(min:java.lang.Integer,max:java.lang.Integer ):Seq[Int] = {
      List.range(min,max);
  }

  def makeStream(max:java.lang.Integer ):Stream[Int] = {
    makeStream(0,max);
   }
   def makeStream(min:java.lang.Integer,max:java.lang.Integer ):Stream[Int] = {
       Stream.range(min,max);
   }

}
