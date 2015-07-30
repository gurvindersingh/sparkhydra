package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.spark.accumulators.*;
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
public class NotScoredBins implements IAccumulator<NotScoredBins> {

    public static final String BIN_ACCUMULATOR_NAME = "NotScoredBins";

    public static final AccumulatorParam<NotScoredBins> PARAM_INSTANCE = new IAccumulatorParam<NotScoredBins>();

    public static NotScoredBins empty() {
        return new NotScoredBins();
    }

    public static class NotScoredStatistics implements Comparable<NotScoredStatistics>, Serializable {
        public final long maxHeap;
        public final int totalSpectra;
        public final int notScoredSpectra;
        public final int peptides;

        public NotScoredStatistics(final long pMaxHeap, final int pTotalSpectra, final int pNotScoredSpectra, final int pPeptides) {
            maxHeap = pMaxHeap;
            totalSpectra = pTotalSpectra;
            notScoredSpectra = pNotScoredSpectra;
            peptides = pPeptides;
        }

        @Override
        public String toString() {
            return "NotScoredStatistics{" +
                    "maxHeap=" + maxHeap +
                    ", totalSpectra=" + totalSpectra +
                    ", notScoredSpectra=" + notScoredSpectra +
                    ", peptides=" + peptides +
                    '}';
        }


        @Override
        public int compareTo(final NotScoredStatistics o) {
            int ret;
            ret = Long.compare(o.maxHeap, maxHeap);
            if (ret != 0)
                return ret;
            ret = Integer.compare(o.totalSpectra, totalSpectra);
            if (ret != 0)
                return ret;
            ret = Integer.compare(o.notScoredSpectra, notScoredSpectra);
            if (ret != 0)
                return ret;
            ret = Integer.compare(o.peptides, peptides);
            if (ret != 0)
                return ret;
            return 0;
        }
    }


    private final List<NotScoredStatistics> usage = new ArrayList<NotScoredStatistics>();

    /**
     * Use static method empty
     */
    private NotScoredBins() {
    }

    /**
     * given a value return it as 0
     * default behavior os th return the value itself
     *
     * @return
     */
    @Override
    public NotScoredBins asZero() {
        return empty();
    }

    public List<NotScoredStatistics> getUsage() {
        return new ArrayList(usage);
    }


    public NotScoredBins add(NotScoredStatistics added) {

        usage.add(added);
        return this;
    }


    public NotScoredBins add(NotScoredBins added) {

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
            if (usage.isEmpty())
                return;

            List<NotScoredStatistics> usage = getUsage();
            Collections.sort(usage);  // get highest

            int pct75 = (int) ((usage.size() * 0.75) / usage.size());
            int pct50 = (int) ((usage.size() * 0.50) / usage.size());
            int pct25 = (int) ((usage.size() * 0.25) / usage.size());

            if (usage.size() == 0)
                return;
            int totalUnscored = 0;
            for (NotScoredStatistics notScoredStatistics : usage) {
                totalUnscored += notScoredStatistics.notScoredSpectra;
            }
            out.append("Total Unscored = " + totalUnscored);
            out.append("\n");
            NotScoredStatistics memoryAndBinSize1 = usage.get(0);
            out.append("max " + memoryAndBinSize1.toString());
            out.append("\n");
            NotScoredStatistics ns75 = usage.get(pct75);
            if (ns75 != memoryAndBinSize1) {
                out.append("0.75 " + ns75.toString());
                out.append("\n");
            }
            NotScoredStatistics ns50 = usage.get(pct50);
            if (ns50 != memoryAndBinSize1) {
                out.append("0.50 " + ns75.toString());
                out.append("\n");
            }
            NotScoredStatistics ns25 = usage.get(pct25);
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
