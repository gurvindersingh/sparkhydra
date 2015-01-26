package org.systemsbiology.xtandem.fdr;

import java.util.*;

/**
 * org.systemsbiology.xtandem.fdr.FalseDiscoveryDataHolder
 * a simple implementation of  IDiscoveryDataHolder
 * hard coded to handle scores of 0 to 100
 *
 * @author Steve Lewis
 * @date 09/05/13
 */
public class FalseDiscoveryDataHolder implements IDiscoveryDataHolder {
    public static FalseDiscoveryDataHolder[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = FalseDiscoveryDataHolder.class;

    public static final int MAX_SIZE = 100;

    private final String m_AlgorithmName;
    private final boolean m_HighIsBetter;

    private final int[] m_TrueBin = new int[FDRUtilities.NUMBER_BINS];
    private final int[] m_FalseBin = new int[FDRUtilities.NUMBER_BINS];

    private final int[] m_TrueSumBin = new int[FDRUtilities.NUMBER_BINS];
    private final int[] m_FalseSumBin = new int[FDRUtilities.NUMBER_BINS];

    private boolean m_Dirty;

    public FalseDiscoveryDataHolder() {
        this(null, true);
    }


    public FalseDiscoveryDataHolder(String algorithmName) {
        this(algorithmName, true);
    }

    public FalseDiscoveryDataHolder(String algorithmName, boolean highIsBetter) {
        m_AlgorithmName = algorithmName;
        m_HighIsBetter = highIsBetter;
    }

    public String getAlgorithmName() {
        return m_AlgorithmName;
    }

    public boolean isHighIsBetter() {
        return m_HighIsBetter;
    }

    protected boolean isDirty() {
        return m_Dirty;
    }

    protected void setDirty(boolean dirty) {
        m_Dirty = dirty;
    }

    /**
     * drop all data
     */
    @Override
    public void clear() {
        Arrays.fill(m_TrueBin, 0);
        Arrays.fill(m_FalseBin, 0);
        Arrays.fill(m_TrueSumBin, 0);
        Arrays.fill(m_FalseSumBin, 0);
    }

    /**
     * add a discovery from true data
     *
     * @param score score - should be > 0
     */
    @Override
    public void addTrueDiscovery(double score) {
        int index = FDRUtilities.asBin(score); // convert a score to an index
        m_TrueBin[index]++;
        setDirty(true); // reset sums

    }

    /**
     * add a discovery from decoy data
     *
     * @param score score - should be > 0
     */
    @Override
    public void addFalseDiscovery(double score) {
        int index = FDRUtilities.asBin(score); // convert a score to an index
        m_FalseBin[index]++;
        setDirty(true); // reset sums

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

        final int total = trueCount + falseCount;
        if (total == 0)
            return 0;
        return ((double) falseCount) / total;
    }

    /**
     * number of true samples above score
     *
     * @param score score - should be > 0
     */
    @Override
    public int getNumberTruePositivesAbove(double score) {
        guaranteeSumsIfDirty();     // compute sums if needed
        int index = FDRUtilities.asBin(score); // convert a score to an index
        return m_TrueSumBin[index];
    }

    /**
     * fill in  the  m_TrueSumBin  and  m_FalseSumBin values
     * these will be valif until more data is added
     */
    protected void guaranteeSumsIfDirty() {
        if (isDirty()) {
            Arrays.fill(m_TrueSumBin, 0);
            Arrays.fill(m_FalseSumBin, 0);
            int trueSum = 0;
            int falseSum = 0;

            if (isHighIsBetter()) {
                // sum up high is better
                int i = m_FalseBin.length - 1;
                for (; i >= 0; i--) {
                    if (m_FalseBin[i] > 0) {
                        falseSum += m_FalseBin[i];
                    }
                    m_FalseSumBin[i] = falseSum;

                    if (m_TrueBin[i] > 0) {
                        trueSum += m_TrueBin[i];
                    }
                    m_TrueSumBin[i] = trueSum;

                }
            } else {
                // low is better so sum backwards
                for (int i = 0; i < m_FalseBin.length; i++) {
                    falseSum += m_FalseBin[i];
                    m_FalseSumBin[i] = falseSum;

                    trueSum += m_TrueBin[i];
                    m_TrueSumBin[i] = trueSum;

                }

            }
            setDirty(false); // nopt dirty until we add more data
         }
    }

    /**
     * number of false samples above score
     *
     * @param score score - should be > 0
     */
    @Override
    public int getNumberFalsePositivesAbove(double score) {
        guaranteeSumsIfDirty(); // compute sums if needed
        int index = FDRUtilities.asBin(score); // convert a score to an index
        return m_FalseSumBin[index];
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
                return FDRUtilities.fromBin(i);
            if (m_FalseBin[i] > 0)
                return FDRUtilities.fromBin(i);

        }
        return FDRUtilities.fromBin(FDRUtilities.NUMBER_BINS);
    }

    /**
     * return the hihgest score with values
     * Note this may be an approximation due to binning
     *
     * @return
     */
    @Override
    public double getLastScore() {
        for (int i = m_TrueBin.length - 1; i >= 0; i--) {
            if (m_TrueBin[i] > 0)
                return FDRUtilities.fromBin(i);
            if (m_FalseBin[i] > 0)
                return FDRUtilities.fromBin(i);
        }
        return FDRUtilities.fromBin(FDRUtilities.NUMBER_BINS);
    }
}

