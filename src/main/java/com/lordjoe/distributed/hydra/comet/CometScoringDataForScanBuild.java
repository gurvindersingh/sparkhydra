package com.lordjoe.distributed.hydra.comet;

import java.util.Arrays;
import java.util.Map;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringData
 * User: Steve
 * Date: 4/1/2015
 */
// Note we do not want to serialize this
public class CometScoringDataForScanBuild {

    private static transient ThreadLocal<CometScoringDataForScanBuild> gPreallocatedData;



    public static CometScoringDataForScanBuild getScoringData() {
        synchronized (CometScoringDataForScanBuild.class) {
            if (gPreallocatedData == null) {
                gPreallocatedData = new ThreadLocal<CometScoringDataForScanBuild>();
            }
        }
        CometScoringDataForScanBuild ret = gPreallocatedData.get();
        if (ret == null) {
            ret = new CometScoringDataForScanBuild();
            gPreallocatedData.set(ret);
        }
        return ret;
    }

    // big arrays only allocated once
    private CometScoredScan currentScan;
    private final float[] m_Weightsx;
    private final float[] m_TmpFastXcorrData;
    private final float[] m_TmpFastXcorrData2;
    private final float[] m_fFastXcorrDataNL;
    private final float[] m_ScoringFastXcorrData;
//      private final Map<Integer, Float> fastScoringMap = new HashMap<Integer, Float>();
//    private final Map<Integer, Float> fastScoringMapNL = new HashMap<Integer, Float>();

//    private final CometScoringAlgorithm comet;

    public CometScoringDataForScanBuild() {
        m_Weightsx = allocateMemory();
        m_TmpFastXcorrData = allocateMemory();
       m_ScoringFastXcorrData = allocateMemory();
        m_fFastXcorrDataNL = allocateMemory();
        m_TmpFastXcorrData2 = allocateMemory();
        currentScan = null;
    }

//    public Map<Integer, Float> getFastScoringMap() {
//        return fastScoringMap;
//    }
//
//    public Map<Integer, Float> getFastScoringMapNL() {
//        return fastScoringMapNL;
//    }

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
        int n = CometScoringAlgorithm.ALLOCATED_DATA_SIZE;
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
//        fastScoringMap.clear();
//        fastScoringMapNL.clear();
    }


}