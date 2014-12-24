package com.lordjoe.distributed.hydra.fragment;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.fragment.MassBin
 * represents a key for all fragments in a mass range from baseMass to baseMass + massWidth
 * can be used to accumulate counts
 * User: Steve
 * Date: 10/30/2014
 */
public class MassBin implements Serializable, Comparable<MassBin> {

    public static final int MAX_SECTION_COUNTS = 10000;

    private final int bin;
    private final double baseMass;
    private final double massWidth;
    private int count;
    private BinSectionKey[] sections;

    public MassBin(final int pBin, final double pBaseMass, final double pMassWidth) {
        bin = pBin;
        baseMass = pBaseMass;
        massWidth = pMassWidth;
    }

    public void addCount() {
        addCount(1);
    }

    public void addCount(int added) {
        if (sections != null)
            throw new IllegalStateException("Once Sections generated count cannot change");
        count += added;
    }

    public double getBaseMass() {
        return baseMass;
    }

    public double getMassWidth() {
        return massWidth;
    }

    public int getCount() {
        return count;
    }

    public BinSectionKey[] getSections() {
        if (sections == null)
            sections = buildSections();
        return sections;
    }

    protected BinSectionKey[] buildSections() {

        if (getCount() < MAX_SECTION_COUNTS) {
            BinSectionKey oneSection = new BinSectionKey(bin, baseMass, 0, Integer.MAX_VALUE);
            BinSectionKey[] ret = {};
            return ret;
        }
        List<BinSectionKey> holder = new ArrayList<BinSectionKey>();
        for (int sectionStart = 0; sectionStart < count; sectionStart += MAX_SECTION_COUNTS) {
            BinSectionKey oneSection = new BinSectionKey(bin, baseMass, sectionStart, sectionStart + MAX_SECTION_COUNTS);
            holder.add(oneSection);
        }

        BinSectionKey[] ret = new BinSectionKey[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    @Override
    public int compareTo(final MassBin o) {
        return Double.compare(getBaseMass(), o.getBaseMass());
    }
}
