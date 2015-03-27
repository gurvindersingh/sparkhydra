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

    public static final double QUANTIZATION = 0.4;

    public static int mzAsInt(double mz) {
        return (int) (0.5 + (mz / QUANTIZATION));
    }

    public static double intToMz(int mzInt) {
        return QUANTIZATION * mzInt;
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
            int pp = ((BinChargeKey) key).mzInt;
            return Math.abs(pp % numPartitions());
        }
    }


    public final int charge;
    public final int mzInt;

    public BinChargeKey(final int pCharge, final double pMz) {
        charge = pCharge;
        mzInt = mzAsInt(pMz);
    }

    @SuppressWarnings("UnusedDeclaration")
    public int getMzInt() {
        return mzInt;
    }

    public double getMz() {
          return intToMz(mzInt);
    }


    public int getCharge() {
        return charge;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final BinChargeKey that = (BinChargeKey) o;

    //    if (charge != that.charge) return false;

        if ( mzInt != that.mzInt) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = mzInt;
   //     result = 31 * result + (charge ^ (charge >>> 32));
        return result;
    }

    @Override
    public int compareTo(final BinChargeKey o) {
//        int ret = Integer.compare(charge, o.charge);
//        if (ret != 0)
//            return ret;
        int x = mzInt;
        int y = o.mzInt;
        if (x == y)
            return 0;
        return Integer.compare(x, y);
    }

    public String toString() {
        return Integer.toString(charge) + ":" + String.format("%10.3f", getMz()) + ":" + getMzInt();
    }
}
