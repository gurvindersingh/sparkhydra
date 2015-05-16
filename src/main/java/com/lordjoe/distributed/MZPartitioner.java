package com.lordjoe.distributed;

import com.lordjoe.distributed.hydra.fragment.BinChargeKey;
import org.apache.spark.Partitioner;

/**
 * Created by guri on 5/16/15.
 */
public class MZPartitioner extends Partitioner {
    @Override
    public int numPartitions() {
        return SparkUtilities.getDefaultNumberPartitions();
    }

    @Override
    public int getPartition(final Object key) {
        int pp = ((BinChargeKey) key).mzInt;
        return Math.abs(pp % numPartitions());
    }
}
