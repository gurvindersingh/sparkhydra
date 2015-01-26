package org.systemsbiology.xtandem.fdr;

import java.util.*;

/**
 * org.systemsbiology.xtandem.fdr.StupidDiscoveryDataHolder
 * a simple implementation of  IDiscoveryDataHolder
 * hard coded to handle scores of 0 to 100
 *
 * @author Steve Lewis
 * @date 09/05/13
 */
public class StupidDiscoveryDataHolder implements IDiscoveryDataHolder {
    public static StupidDiscoveryDataHolder[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = StupidDiscoveryDataHolder.class;

    public static final int MAX_SIZE = 100;

    private int[] m_TrueBin = new int[MAX_SIZE];
    private int[] m_FalseBin = new int[MAX_SIZE];

    /**
     * convert a score into an index into  m_TrueBin or  m_FalseBin
     *
     * @param score
     * @return the index >= 0 <  MAX_SIZE
     */
    protected int asBin(double score) {
        if (score <= 0)
            return 0;
        return Math.min((int) score, MAX_SIZE - 1);
    }

    /**
     * drop all data
     */
    @Override
    public void clear() {
        Arrays.fill(m_TrueBin, 0);
        Arrays.fill(m_FalseBin, 0);
    }

    /**
     * add a discovery from true data
     *
     * @param score score - should be > 0
     */
    @Override
    public void addTrueDiscovery(double score) {
        int index = asBin(score);
        m_TrueBin[index]++;

    }

    /**
     * add a discovery from decoy data
     *
     * @param score score - should be > 0
     */
    @Override
    public void addFalseDiscovery(double score) {
        int index = asBin(score);
        m_FalseBin[index]++;

    }

    /**
     * return FDR
     *
     * @param score score - should be > 0
     */
    @Override
    public double computeFDR(double score) {
        int trueCount = getNumberTruePositivesAbove(score);
        int falseCount = getNumberFalsePositivesAbove(score);
        return ((double) falseCount) / (trueCount + falseCount);
    }

    /**
     * number of true samples above score
     *
     * @param score score - should be > 0
     */
    @Override
    public int getNumberTruePositivesAbove(double score) {
        int index = asBin(score);
        int ret = 0;
        for (int i = index; i < m_TrueBin.length; i++) {
            ret += m_TrueBin[i];

        }
        return ret;
    }

    /**
     * number of false samples above score
     *
     * @param score score - should be > 0
     */
    @Override
    public int getNumberFalsePositivesAbove(double score) {
        int index = asBin(score);
        int ret = 0;
        for (int i = index; i < m_FalseBin.length; i++) {
            ret += m_FalseBin[i];

        }
        return ret;
    }

    /**
     * return the lowest score with values
     * Note this may be an approximation due to binning
     *
     * @return
     */
    @Override
    public double getFirstScore() {
        for (int i = 0; i < m_TrueBin.length; i++) {
            if (m_TrueBin[i] > 0)
                return i;
            if (m_FalseBin[i] > 0)
                return i;

        }
        return  m_TrueBin.length;
    }

    /**
     * return the hihgest score with values
     * Note this may be an approximation due to binning
     *
     * @return
     */
    @Override
    public double getLastScore() {
        for (int i =  m_TrueBin.length - 1; i >= 0; i--) {
            if (m_TrueBin[i] > 0)
                return i;
            if (m_FalseBin[i] > 0)
                return i;

        }
        return  m_TrueBin.length;
     }
}
