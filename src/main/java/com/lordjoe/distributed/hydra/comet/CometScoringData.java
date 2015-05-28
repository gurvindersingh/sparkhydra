package com.lordjoe.distributed.hydra.comet;

import java.lang.ref.SoftReference;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringData
 * User: Steve
 * Date: 4/1/2015
 */
// Note we do not want to serialize this
public class CometScoringData {

    private static transient ThreadLocal<SoftReference<CometScoringData>> gPreallocatedData;

    public static void populateFromScan(CometScoredScan scan)
    {
        final CometScoringData scoringData = CometScoringData.getScoringData();
        // in this section we pregenerate data for a spectrum and reuse it
        scoringData.clearData();

        //CometTesting.validateIndex(scan);

        final Map<Integer, java.lang.Double> fastScoringMap = scan.getFastScoringMap();

        float[] fastXcorrDataMap = scoringData.getScoringFastXcorrData();
        for (Integer i : fastScoringMap.keySet()) {
            double aDouble = fastScoringMap.get(i);
            fastXcorrDataMap[i] = (float)aDouble;
        }

        final Map<Integer, Double> fastScoringMapNL = scan.getFastScoringMapNL();   // we used to get from commented scoring data
        float[] fastXcorrDataNL = scoringData.getFastXcorrDataNL();
        for (Integer i : fastScoringMapNL.keySet()) {
            double aDouble = fastScoringMapNL.get(i);
            fastXcorrDataNL[i] = (float)aDouble;
        }
        scoringData.currentScan = scan;
    }

    public static float[] getFastDataForScan(CometScoredScan scan)
    {
        CometScoringData dta = getScoringData() ;
        if(dta.currentScan != scan)
            throw new IllegalStateException("bad scan");
        return dta.getScoringFastXcorrData();
    }

    public static float[] getFastDataNLForScan(CometScoredScan scan)
    {
        CometScoringData dta = getScoringData() ;
        if(dta.currentScan != scan)
            throw new IllegalStateException("bad scan");
        return dta.getFastXcorrDataNL();
    }


    /**
     * tricky we we use a thread local soft erference to allow
     * @return
     */
    public static CometScoringData getScoringData() {
        if(true)
        throw new UnsupportedOperationException("fix this"); // todo add code

        synchronized (CometScoringData.class) {
            if (gPreallocatedData == null) {
                gPreallocatedData = new ThreadLocal<SoftReference<CometScoringData>>();
            }
        }
        SoftReference<CometScoringData> cometScoringDataSoftReference = gPreallocatedData.get();
        if(cometScoringDataSoftReference == null || cometScoringDataSoftReference.get() == null)
        {
            CometScoringData value = new CometScoringData();
            cometScoringDataSoftReference =  new SoftReference<CometScoringData>(value);
            gPreallocatedData.set(cometScoringDataSoftReference);
        }
        CometScoringData ret =  cometScoringDataSoftReference.get();

        return ret;
    }

    // big arrays only allocated once
    private CometScoredScan currentScan;
    private final float[] m_fFastXcorrDataNL;
    private final float[] m_ScoringFastXcorrData;
//      private final Map<Integer, Float> fastScoringMap = new HashMap<Integer, Float>();
//    private final Map<Integer, Float> fastScoringMapNL = new HashMap<Integer, Float>();

//    private final CometScoringAlgorithm comet;

    private CometScoringData() {
        m_ScoringFastXcorrData = allocateMemory();
        m_fFastXcorrDataNL = allocateMemory();
        currentScan = null;
    }



    private float[] allocateMemory() {
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
    private float[] getFastXcorrDataNL() {
        return m_fFastXcorrDataNL;
    }

    /**
     * this method is protested to allow testing
     *
     * @return
     */
    private float[] getScoringFastXcorrData() {
        return m_ScoringFastXcorrData;
    }


    /**
     * no threating issues
     */
    private void clearData() {
         Arrays.fill(m_fFastXcorrDataNL, 0);
        Arrays.fill(m_ScoringFastXcorrData, 0);
        currentScan = null;
    }


}
