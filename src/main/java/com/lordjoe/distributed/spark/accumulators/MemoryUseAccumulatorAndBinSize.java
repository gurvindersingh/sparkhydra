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

    public static final String BIN_ACCUMULATOR_NAME = "BinUsage";

    public static final AccumulatorParam<MemoryUseAccumulatorAndBinSize> PARAM_INSTANCE = new IAccumulatorParam<MemoryUseAccumulatorAndBinSize>();

      public static MemoryUseAccumulatorAndBinSize empty() {
         return new MemoryUseAccumulatorAndBinSize();
      }


    private transient long startAllocation;
    private transient long maxAllocated;

    private long maxHeap;
    private List<MemoryAndBinSize> usage = new ArrayList<MemoryAndBinSize>();

    /**
     * Use static method empty
     */
    private MemoryUseAccumulatorAndBinSize() {
        startAllocation = MemoryTracker.threadAllocatedBytes();
        maxHeap = startAllocation;
        maxAllocated = 0;
    }

    /**
     * given a value return it as 0
     * default behavior os th return the value itself
     *
     * @return
     */
    @Override
    public MemoryUseAccumulatorAndBinSize asZero() {
        return empty();
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

    /**
     * like toString but might add more information than a shorter string
     * usually implemented bu appending toString
     *
     * @param out
     */
    @Override
    public void buildReport(final Appendable out) {
        buildReport(out, Integer.MAX_VALUE);
    }

    /**
     * like toString but might add more information than a shorter string
     * usually implemented bu appending toString
     *
     * @param out
     */
    protected void buildReport(final Appendable out, int maxReprt) {
        int lines = 0;
        List<MemoryAndBinSize> usage = getUsage();
        Collections.sort(usage);  // get highest
        try {
            for (MemoryAndBinSize memoryAndBinSize : usage) {
                out.append(memoryAndBinSize.toString());
                out.append("\n");

                if (lines++ > maxReprt)
                    break;
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        buildReport(sb, 20);
        return sb.toString();
    }

}
