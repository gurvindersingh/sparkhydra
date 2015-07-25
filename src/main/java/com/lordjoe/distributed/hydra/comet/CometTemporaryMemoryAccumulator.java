package com.lordjoe.distributed.hydra.comet;


import com.lordjoe.algorithms.*;
import com.lordjoe.distributed.spark.accumulators.*;
import org.apache.spark.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometTemporaryMemoryAccumulator
 * Accululator to look at memory issues with comet temporary memory
 * to use create than call check to look at memory state
 * call saveBins() to save maximum memory use
 *
 * @author Steve Lewis
 */
public class CometTemporaryMemoryAccumulator implements IAccumulator<CometTemporaryMemoryAccumulator> {

    public static final AccumulatorParam<CometTemporaryMemoryAccumulator> PARAM_INSTANCE = new IAccumulatorParam<CometTemporaryMemoryAccumulator>();
    public static final String COMET_MEMORY_ACCUMULATOR_NAME = "CometTemporaryMemoryAccumulator" ;

    public static class TemporaryMemoryAllocation implements Serializable,Comparable<TemporaryMemoryAllocation> {
        public final int numberAllocations;
        public final long totalAllocation;

        public TemporaryMemoryAllocation(final int pNumberAllocations, final long pTotalAllocation) {
            numberAllocations = pNumberAllocations;
            totalAllocation = pTotalAllocation;
        }

        public TemporaryMemoryAllocation add(TemporaryMemoryAllocation added) {
            return new TemporaryMemoryAllocation(numberAllocations + added.numberAllocations,
                    totalAllocation + added.totalAllocation);
        }

        @Override
        public int compareTo(final TemporaryMemoryAllocation o) {
            int ret;
            ret = Long.compare(o.totalAllocation,totalAllocation); // highest first
            if(ret != 0)
                return ret;
            ret = Integer.compare(o.numberAllocations,numberAllocations); // highest first
             if(ret != 0)
                  return ret;
            return 0;
        }

        @Override
        public String toString() {
            return
                    "numberAllocations=" + Long_Formatter.format(numberAllocations)  +
                            ", totalAllocation=" + Long_Formatter.format(totalAllocation)
                    ;
        }
    }


    public static CometTemporaryMemoryAccumulator empty() {
        return new CometTemporaryMemoryAccumulator();
    }

    private final Map<Integer, TemporaryMemoryAllocation> allocations = new HashMap<Integer, TemporaryMemoryAllocation>();

    /**
     * Use static method empty
     */
    private CometTemporaryMemoryAccumulator() {
    }

    /**
     * given a value return it as 0
     * default behavior os th return the value itself
     *
     * @return not null empty
     */
    @Override
    public CometTemporaryMemoryAccumulator asZero() {
        return empty();
    }

    public void check() {
        Integer allocationIdentifier = CometScoringDataForScanBuild.getAllocationIdentifier();
        int times = CometScoringDataForScanBuild.getNumberTimesMemoryAllocated();
        long allocated = CometScoringDataForScanBuild.getTotalTemporaryMemoryAllocated();
        allocations.put(allocationIdentifier,new TemporaryMemoryAllocation(times,allocated));
     }


    /**
     * build a bew structure merging the two
     *
     * @param added  item to add
     * @return merged
     */
    public CometTemporaryMemoryAccumulator add(CometTemporaryMemoryAccumulator added) {

        CometTemporaryMemoryAccumulator ret = empty();

        Set<Integer> allKeys = new HashSet<Integer>(added.allocations.keySet());
        allKeys.addAll(allocations.keySet());
        for (Integer allKey : allKeys) {
            TemporaryMemoryAllocation id1 = allocations.get(allKey);
            TemporaryMemoryAllocation id2 = added.allocations.get(allKey);
            if (id1 == null) {
                ret.allocations.put(allKey, id2); // id2 nust not be null
            }
            else {
                if (id2 == null)
                    ret.allocations.put(allKey, id1);
                else
                    ret.allocations.put(allKey, id1.add(id2));

            }

        }
        return ret;
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

        List<TemporaryMemoryAllocation> holder = new ArrayList<TemporaryMemoryAllocation>();
        for (Integer key : allocations.keySet()) {
            holder.add(allocations.get(key));
        }
        Collections.sort(holder);

        for (TemporaryMemoryAllocation r : holder) {
            sb.append(r.toString()) ;
            sb.append("\n") ;
          }


        return sb.toString();
    }

}
