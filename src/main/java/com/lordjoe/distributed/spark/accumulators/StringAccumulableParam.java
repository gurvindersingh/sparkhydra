package com.lordjoe.distributed.spark.accumulators;

import org.apache.spark.*;

import java.io.*;

/**
* com.lordjoe.distributed.spark.accumulators.StringAccumulableParam
 * Usage
 *      final Accumulator<String> myString = ctx.accumulator("", "Letter Statistics", new StringAccumulableParam("\t");
 *     ...
 *        myString.add("xyxzzy");
* User: Steve
* Date: 11/12/2014
*/
public class StringAccumulableParam implements AccumulatorParam<String>,Serializable {

    private final String separator;

    public StringAccumulableParam(final String pSeparator) {
        separator = pSeparator;
    }
    public StringAccumulableParam( ) {
         this(",");
     }

    @Override
    public String addAccumulator(final String r, final String t) {
        if(r.isEmpty())
            return t;
        if(t.isEmpty())
             return r;
         return r +  separator + t;
    }
     @Override
    public String addInPlace(final String r , final String t) {
         if(r.isEmpty())
             return t;
         if(t.isEmpty())
              return r;
        return  r +  separator + t;
    }
     @Override
    public String zero(final String initialValue) {
        return "";
    }
}
