package com.lordjoe.distributed.hydra.fragment;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.fragment.BinSectionKey
 * User: Steve
 * Date: 10/30/2014
 */
public class BinSectionKey implements Serializable,Comparable<BinSectionKey> {

    private final int bin;
    private final double mz;
    private final int startCount;
    private final int endCount;

    public BinSectionKey(final int pBin, final double pMz, final int pStartCount, final int pEndCount) {
        bin = pBin;
        mz = pMz;
        startCount = pStartCount;
        endCount = pEndCount;
    }

    public int getBin() {
        return bin;
    }

    public double getMz() {
        return mz;
    }

    public int getStartCount() {
        return startCount;
    }

    public int getEndCount() {
        return endCount;
    }

    /**
     * by bin then by start count
     * @param o
     * @return
     */
     @Override
    public int compareTo(final BinSectionKey o) {
        int ret = Integer.compare(getBin(),o.getBin()) ;
        if(ret != 0)
            return ret;
        return Integer.compare(getStartCount(),o.getStartCount()) ;
    }
}
