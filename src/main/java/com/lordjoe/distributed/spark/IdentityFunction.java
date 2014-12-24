package com.lordjoe.distributed.spark;

import org.apache.spark.api.java.function.*;

/**
* com.lordjoe.distributed.spark.IdentityFunction
 * A function which returns the object - used in combineByKey when
 * the original obejct cna be merged
* User: Steve
* Date: 10/14/2014
*/
public class IdentityFunction<K> implements Function<K, K> {

    public static final IdentityFunction INSTANCE = new IdentityFunction();

    private  IdentityFunction() {}
    @Override
    public K call(final K v1) throws Exception {
        return v1;
    }
}
