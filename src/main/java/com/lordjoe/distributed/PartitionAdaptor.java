package com.lordjoe.distributed;

import org.apache.spark.*;

/**
 * com.lordjoe.distributed.PartitionAdaptor
 * User: Steve
 * Date: 9/4/2014
 */

public class PartitionAdaptor<K>  extends Partitioner {

    public static final int DEFAULT_PARTITION_COUNT = 20;
    private final IPartitionFunction<K> partitioner;
    private final int numberPartitions;

    public PartitionAdaptor(final IPartitionFunction<K> pPartitioner ) {
        this(pPartitioner,DEFAULT_PARTITION_COUNT);
    }

    public PartitionAdaptor(final IPartitionFunction<K> pPartitioner,int pnumberPartitions) {
        partitioner = pPartitioner;
        numberPartitions = pnumberPartitions;
    }

    @Override
    public int numPartitions() {
        return numberPartitions;
    }

    @Override
    public int getPartition(final Object inp) {
        int partition = partitioner.getPartition((K) inp);
        int ret = partition % numberPartitions;
        if(ret < 0)
            ret = -ret;
        return ret;
    }
}
