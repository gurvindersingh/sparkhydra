package com.lordjoe.distributed.hydra.comet;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometPeak
 * User: Steve
 * Date: 3/31/2015
 */
public class CometPeak implements Comparable<CometPeak>,Serializable {

    public final int index;
    public final float peak;

    public CometPeak(final int pIndex, final float pPeak) {
        index = pIndex;
        peak = pPeak;
    }

    /**
     * highest first
     * @param o
     * @return
     */
    @Override
    public int compareTo(final CometPeak o) {
        int ret = Float.compare(o.peak,peak);
        if(ret != 0)
            return ret;
        return Integer.compare(index,index);
    }

    @Override
    public String toString() {
        return "" +
                "index=" + index +
                ", peak=" + peak +
                '}';
    }
}
