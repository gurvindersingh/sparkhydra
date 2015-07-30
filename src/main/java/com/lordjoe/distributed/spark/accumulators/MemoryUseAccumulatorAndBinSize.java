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

    public static final String BIN_ACCUMULATOR_NAME = "MemoryUseAccumulatorAndBinSize";

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
        startAllocation = MemoryTracker.usedBytes();
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
        long current = MemoryTracker.usedBytes();
        long allocated = current - startAllocation;
        maxHeap = Math.max(maxHeap, current);
        maxAllocated = Math.max(maxAllocated, allocated);
    }


    public void saveUsage(int nSpectra, int nPeptides) {
        usage.add(new MemoryAndBinSize(maxHeap, maxAllocated, nSpectra, nPeptides));
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
        try {
            int lines = 0;
            List<MemoryAndBinSize> usage = getUsage();
            Collections.sort(usage);  // get highest

            int pct75 = (int) ((usage.size() * 0.75) / usage.size());
            int pct50 = (int) ((usage.size() * 0.50) / usage.size());
            int pct25 = (int) ((usage.size() * 0.25) / usage.size());

            if(usage.size() == 0)
                return;
            MemoryAndBinSize memoryAndBinSize1 = usage.get(0);
            out.append("max " + memoryAndBinSize1.toString());
            out.append("\n");
            out.append("max " + memoryAndBinSize1.toString());
              out.append("\n");
            MemoryAndBinSize ns75 = usage.get(pct75);
              if (ns75 != memoryAndBinSize1) {
                  out.append("0.75 " + ns75.toString());
                  out.append("\n");
              }
            MemoryAndBinSize ns50 = usage.get(pct50);
              if (ns50 != memoryAndBinSize1) {
                  out.append("0.50 " + ns75.toString());
                  out.append("\n");
              }
            MemoryAndBinSize ns25 = usage.get(pct25);
              if (ns50 != memoryAndBinSize1) {
                  out.append("0.25 " + ns25.toString());
                  out.append("\n");
              }

  //            for (MemoryAndBinSize memoryAndBinSize : usage) {
//                out.append(memoryAndBinSize.toString());
//                out.append("\n");
//
//                if (lines++ > maxReprt)
//                    break;
//            }
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
