package com.lordjoe.distributed.spark.accumulators;

import com.lordjoe.testing.*;
import org.apache.spark.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.algorithms.MemoryUseAccumulator
 * Accululator to look at memory issues
 * to use create than call check to look at memory state
 * call saveBins() to save maximum memory use
 *
 * @author Steve Lewis
 * @date 5/28/2015
 */
public class MemoryUseAccumulatorAndBinSize implements IAccumulator<MemoryUseAccumulatorAndBinSize> {

    public static final MemoryUseAccumulatorAndBinSizeParam PARAM_INSTANCE = new MemoryUseAccumulatorAndBinSizeParam();

    public static class MemoryUseAccumulatorAndBinSizeParam implements AccumulatorParam<MemoryUseAccumulatorAndBinSize>, Serializable {
        private MemoryUseAccumulatorAndBinSizeParam() {
        }

        @Override
        public MemoryUseAccumulatorAndBinSize addAccumulator(final MemoryUseAccumulatorAndBinSize t1, final MemoryUseAccumulatorAndBinSize t2) {
            t1.add(t2);
            return t1;
        }

        /**
         * Merge two accumulated values together. Is allowed to modify and return the first value
         * for efficiency (to avoid allocating objects).
         *
         * @param r1 one set of accumulated data
         * @param r2 another set of accumulated data
         * @return both data sets merged together
         */
        @Override
        public MemoryUseAccumulatorAndBinSize addInPlace(final MemoryUseAccumulatorAndBinSize r1, final MemoryUseAccumulatorAndBinSize r2) {
            r1.add(r2);
            return r1;
        }

        /**
         * Return the "zero" (identity) value for an accumulator type, given its initial value. For
         * example, if R was a vector of N dimensions, this would return a vector of N zeroes.
         *
         * @param initialValue
         */
        @Override
        public MemoryUseAccumulatorAndBinSize zero(final MemoryUseAccumulatorAndBinSize initialValue) {
            return new MemoryUseAccumulatorAndBinSize();
        }
    }


    private class MemoryAndBinSize implements Comparable<MemoryAndBinSize> {
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
            return "MemoryAndBinSize{" +
                    "memoryUse=" + memoryUse +
                    ", numberSpectra=" + numberSpectra +
                    ", numberPeptides=" + numberPeptides +
                    '}';
        }
    }

    private transient long startAllocation;
    private transient long maxAllocated;

    private long maxHeap;
    private List<MemoryAndBinSize> usage;

    public MemoryUseAccumulatorAndBinSize() {
        startAllocation = MemoryTracker.threadAllocatedBytes();
        maxHeap = startAllocation;
        maxAllocated = 0;
    }

    public List<MemoryAndBinSize> getUsage() {
        return new ArrayList(usage);
    }


    public void check() {
        long current = MemoryTracker.threadAllocatedBytes();
        long allocated = current - startAllocation;
        maxHeap = Math.max(maxHeap, current);
        maxAllocated = Math.max(maxAllocated, allocated);
    }


    public void saveUsage(int nSpectra, int nPeptides) {
        usage.add(new MemoryAndBinSize(maxHeap, nSpectra, nPeptides));
    }


    public MemoryUseAccumulatorAndBinSize add(MemoryUseAccumulatorAndBinSize added) {

        maxHeap = Math.max(maxHeap, added.maxHeap);
        usage.addAll(added.usage);
        return this;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        if(true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        return sb.toString();
    }

}
