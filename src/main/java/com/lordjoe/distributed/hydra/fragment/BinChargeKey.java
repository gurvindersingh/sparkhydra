package com.lordjoe.distributed.hydra.fragment;

import com.lordjoe.distributed.*;
import org.apache.spark.*;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.fragment.BinChargeKey
 * User: Steve
 * Date: 10/31/2014
 */
public class BinChargeKey implements Serializable, Comparable<BinChargeKey> {

    public static int mzAsInt(double mz)  {
          return (int)(mz / QUANTIZATION);
    }
    public static Partitioner getPartitioner() {
        return new BinChargeKeyPartitioner();
    }

    protected static class BinChargeKeyPartitioner extends Partitioner {
           @Override
           public int numPartitions() {
               return SparkUtilities.getDefaultNumberPartitions();
           }

           @Override
           public int getPartition(final Object key) {
               int pp = ((BinChargeKey) key).mzAsInt();
               return Math.abs(pp % numPartitions());
           }
       }



    public static final double QUANTIZATION = 0.001;
    public final int charge;
    public final double mz;

    public BinChargeKey(final int pCharge, final double pMz) {
        charge = pCharge;
        mz = pMz;
    }

    protected int mzAsInt() {
       return mzAsInt(mz);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BinChargeKey that = (BinChargeKey) o;

        if (charge != that.charge) return false;

        if (Integer.compare(that.mzAsInt(), mzAsInt()) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = charge;
        temp = mzAsInt();
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int compareTo(final BinChargeKey o) {
        int ret = Integer.compare(charge, o.charge);
        if (ret != 0)
            return ret;
        int x = mzAsInt();
        int y = o.mzAsInt();
        if (x == y)
            return 0;
        return Integer.compare(x, y);
    }

    public String toString() {
        return Integer.toString(charge) + ":" + String.format("%10.3f", mz);
    }
}
