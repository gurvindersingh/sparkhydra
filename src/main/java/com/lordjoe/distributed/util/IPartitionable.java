package com.lordjoe.distributed.util;

/**
 * com.lordjoe.distributed.util.IPartitionable
 * User: Steve
 * Date: 8/28/2014
 */
public interface IPartitionable {
    /**
     * Keys which implement this function will supply a way to partition themselves
     * others will be partitioned by hash value
     * @return
     */
    public int getPartition();

}
