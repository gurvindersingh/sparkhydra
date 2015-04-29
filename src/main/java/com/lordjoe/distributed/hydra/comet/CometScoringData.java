package com.lordjoe.distributed.hydra.comet;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringData
 * User: Steve
 * Date: 4/1/2015
 */
// Note we do not want to serialize this
public class CometScoringData {
    // big arrays only allocated once
    private final float[] m_Weightsx;
    private final float[] m_TmpFastXcorrData;
    private final float[] m_TmpFastXcorrData2;
    private final float[] m_ScoringFastXcorrData;
    private final float[] m_fFastXcorrDataNL;
    private final CometScoringAlgorithm comet;

    public CometScoringData(final CometScoringAlgorithm pComet) {
        comet = pComet;
        m_Weightsx = allocateMemory();
        m_TmpFastXcorrData = allocateMemory();
        m_ScoringFastXcorrData = allocateMemory();
        m_fFastXcorrDataNL = allocateMemory();
        m_TmpFastXcorrData2 = allocateMemory();
    }

    /**
     * this method is protested to allow testing
     *
     * @return
     */
    protected float[] getWeights() {
        return m_Weightsx;
    }

    protected float[] allocateMemory() {
        final float[] ret;
        double massTolerance = comet.getBinTolerance();
        int n = (int) (comet.MAX_MASS / massTolerance);
        ret = new float[n];
        return ret;
    }


    /**
     * this method is protested to allow testing
     *
     * @return
     */
    protected float[] getFastXcorrDataNL() {
        return m_fFastXcorrDataNL;
    }

    /**
     * this method is protested to allow testing
     *
     * @return
     */
    protected float[] getScoringFastXcorrData() {
        return m_ScoringFastXcorrData;
    }

    /**
     * this method is protested to allow testing
     *
     * @return
     */
    protected float[] getTmpFastXcorrData() {
        return m_TmpFastXcorrData;
    }

    /**
     * this method is protested to allow testing
     *
     * @return
     */
    protected float[] getTmpFastXcorrData2() {
        return m_TmpFastXcorrData2;
    }

    /**
     * no threating issues
     */
    protected void clearData() {
        Arrays.fill(m_Weightsx, 0);
        Arrays.fill(m_fFastXcorrDataNL, 0);
        Arrays.fill(m_TmpFastXcorrData, 0);
        Arrays.fill(m_TmpFastXcorrData2, 0);
        Arrays.fill(m_ScoringFastXcorrData, 0);
    }


}
