package com.lordjoe.distributed.spark.accumulators;

import com.lordjoe.algorithms.*;

import java.io.*;

/**
 * com.lordjoe.distributed.spark.accumulators.MemoryAndBinSize
 * User: Steve
 * Date: 7/8/2015
 */
public class MemoryAndBinSize implements Comparable<MemoryAndBinSize>, Serializable {
    public final long memoryUse;
    public final int numberSpectra;
    public final int numberPeptides;

    public MemoryAndBinSize(final long pMemoryUse, final int pNumberSpectra, final int pNumberPeptides) {
        memoryUse = pMemoryUse;
        numberSpectra = pNumberSpectra;
        numberPeptides = pNumberPeptides;
    }

    /**
     * sorts highest usage first
     *
     * @param o
     * @return
     */
    @Override
    public int compareTo(final MemoryAndBinSize o) {
        int ret = Long.compare(o.memoryUse, memoryUse);
        if (ret != 0)
            return ret;
        ret = Integer.compare(o.numberPeptides, numberPeptides);
        if (ret != 0)
            ret = Integer.compare(o.numberSpectra, numberSpectra);
        if (ret != 0)
            return ret;
        return 0;
    }

    @Override
    public String toString() {
        return
                "memoryUse=" + Long_Formatter.format(memoryUse) +
                ", numberSpectra=" + numberSpectra +
                ", numberPeptides=" + numberPeptides;

    }
}
