package com.lordjoe.distributed;

import java.io.*;

/**
 * com.lordjoe.distributed.IPartitionFunction
 * User: Steve
 * Date: 8/29/2014
 */
public interface IPartitionFunction<K> extends Serializable {

    /**
     * default implementation use hash code
     */
    public static final IPartitionFunction  HASH_PARTITION = new IPartitionFunction() {
        @Override public  int getPartition(final Object  inp) {
            int ret = inp.hashCode();
            if(ret < 0)
                ret = -ret; // % does not work on negative numbers
            return ret;
        }
    };

    public int getPartition(K inp);
}
