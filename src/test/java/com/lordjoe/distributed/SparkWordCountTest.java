package com.lordjoe.distributed;

import com.lordjoe.distributed.test.*;
import org.junit.*;

/**
 * com.lordjoe.distributed.SparkWordCount
 * User: Steve
 * Date: 9/12/2014
 */
public class SparkWordCountTest {

    // works but runs too long
    //@Test
    public void testWordCount() {
         WordCountOperator.validateWordCount(SparkMapReduce.FACTORY);
    }


}

