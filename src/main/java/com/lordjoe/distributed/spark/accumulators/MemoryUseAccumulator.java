package com.lordjoe.distributed.spark.accumulators;

import com.lordjoe.algorithms.*;
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
public class MemoryUseAccumulator implements IAccumulator<MemoryUseAccumulator> {

    public static final int MEG_40 = 10000000; // 10 MB
    public static final int MAX_TRACKED_10_MEG_CHUNKS = 2000; // up to 20 gb
    public static final int MEG_4 = 4000000; // 1 MB
    public static final int MAX_TRACKED_4_MEG_CHUNKS = 2000; // up to 20 gb

    public static final MemoryUseAccumulatorParam PARAM_INSTANCE = new MemoryUseAccumulatorParam();

    public static class MemoryUseAccumulatorParam implements AccumulatorParam<MemoryUseAccumulator>, Serializable {
        private MemoryUseAccumulatorParam() {
        }

        @Override
        public MemoryUseAccumulator addAccumulator(final MemoryUseAccumulator t1, final MemoryUseAccumulator t2) {
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
        public MemoryUseAccumulator addInPlace(final MemoryUseAccumulator r1, final MemoryUseAccumulator r2) {
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
        public MemoryUseAccumulator zero(final MemoryUseAccumulator initialValue) {
            return new MemoryUseAccumulator();
        }
    }


    private transient long startAllocation;
    private transient long maxAllocated;

    private long maxHeap;
    private final int[] bins = new int[MAX_TRACKED_10_MEG_CHUNKS];
    private final int[] allocated = new int[MAX_TRACKED_4_MEG_CHUNKS];

    public MemoryUseAccumulator() {
        startAllocation = MemoryTracker.threadAllocatedBytes();
        maxHeap = startAllocation;
        maxAllocated = 0;
    }

    public int getBin(int bin) {
        return bins[bin];
    }

    public int[] getBins() {
        int[] ret = new int[bins.length];
        System.arraycopy(bins, 0, ret, 0, bins.length);
        return ret;
    }

    public int[] getAllocatedBins() {
        int[] ret = new int[allocated.length];
        System.arraycopy(allocated, 0, ret, 0, allocated.length);
        return ret;
    }

    public void check() {
        long current = MemoryTracker.threadAllocatedBytes();
        long allocated = current - startAllocation;
        maxHeap = Math.max(maxHeap, current);
        maxAllocated = Math.max(maxAllocated, allocated);
    }


    public void saveBins() {
        int iBin = (int) (maxAllocated / MEG_4);
        iBin = Math.min(allocated.length - 1,iBin);
        allocated[iBin]++;
        iBin = (int) (maxHeap / MEG_40);
        iBin = Math.min(bins.length - 1, iBin);
        bins[iBin]++;
    }


    public MemoryUseAccumulator add(MemoryUseAccumulator added) {

        maxHeap = Math.max(maxHeap,added.maxHeap);
        for (int i = 0; i < bins.length; i++) {
            bins[i] += added.bins[i];
        }
        for (int i = 0; i < allocated.length; i++) {
            allocated[i] += added.allocated[i];
        }
        return this;
    }


    /**
           * like toString but might add more information than a shorter string
           * usually implemented bu appending toString
           *
           * @param out
           */
          @Override
          public void buildReport(final Appendable out) {
              try {
                  out.append(toString());
              }
              catch (IOException e) {
                  throw new RuntimeException(e);

              }
          }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Max Allocation " + Long_Formatter.format(maxHeap));
        sb.append("\n");
        long index = MEG_40;
        for (int i = 0; i < bins.length; i++) {
            int bin = bins[i];
            if (bin > 0) {
                sb.append(Long_Formatter.format(index) + "\t" + Long_Formatter.format(bin));
                sb.append("\n");
            }
            index += MEG_40;
        }

        sb.append("Allocated\n");

         index = MEG_4;
           for (int i = 0; i < allocated.length; i++) {
               int bin = allocated[i];
               if (bin > 0) {
                   sb.append(Long_Formatter.format(index) + "\t" + Long_Formatter.format(bin));
                   sb.append("\n");
               }
               index += MEG_4;
           }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MemoryUseAccumulator that = (MemoryUseAccumulator) o;

        return Arrays.equals(bins, that.bins);

    }

    @Override
    public int hashCode() {
        return bins != null ? Arrays.hashCode(bins) : 0;
    }
}
