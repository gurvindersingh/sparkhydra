package com.lordjoe.distributed.output;

import org.apache.spark.api.java.function.Function;
import scala.*;

import java.io.*;
import java.io.Serializable;
import java.lang.Boolean;

/**
 * com.lordjoe.distributed.output.CloseAndRenameFilter
 * User: Steve
 * Date: 10/14/2014
 */
public class CloseAndRenameFilter implements Function<Tuple2<? extends Serializable,? extends Closeable>, Boolean>,Serializable {


     @Override
    public Boolean call(final Tuple2<? extends Serializable, ? extends Closeable> v1) throws Exception {
        v1._2().close(); // todo rename and make not temp
          return false;
    }
}
