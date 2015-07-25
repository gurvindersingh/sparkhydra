package com.lordjoe.distributed.hydra.comet;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoringData
 * User: Steve
 * Date: 4/1/2015
 */
// Note we do not want to serialize this
public class CometScoringDataForScanBuild {

    //  private static transient ThreadLocal<SoftReference<CometScoringDataForScanBuild>> gPreallocatedData;

    public static final Random RND = new Random();

    //  private static CometScoringDataForScanBuild preallocatedData = new CometScoringDataForScanBuild();

    private static final /* transient */ Stack<CometScoringDataForScanBuild> pool = new Stack<CometScoringDataForScanBuild>();


    private static transient Integer allocationIdentifier;
    private static transient int numberTimesMemoryAllocated;
    private static transient long totalTemporaryMemoryAllocated;

    public static Integer getAllocationIdentifier() {
        guaranteeAllocationIdentifier();
        return allocationIdentifier;

    }

    public static int getNumberTimesMemoryAllocated() {
        guaranteeAllocationIdentifier();
        return numberTimesMemoryAllocated;
    }

    private static void incrementMemoryAllocated(long allocated) {
        guaranteeAllocationIdentifier();
        numberTimesMemoryAllocated++;
        totalTemporaryMemoryAllocated += allocated;
    }

    public static long getTotalTemporaryMemoryAllocated() {
        guaranteeAllocationIdentifier();
        return totalTemporaryMemoryAllocated;
    }

    private static void guaranteeAllocationIdentifier() {
        if (allocationIdentifier == null)
            allocationIdentifier = RND.nextInt();
    }

    public static CometScoringDataForScanBuild borrowScoringMemory() {
        synchronized (pool) {
  //          synchronized (CometScoringDataForScanBuild.class) {
      //       if (pool == null)
   //             pool = new Stack<CometScoringDataForScanBuild>();
            if (pool.isEmpty())
                pool.push(new CometScoringDataForScanBuild());
            CometScoringDataForScanBuild ret = pool.pop();
            ret.clearData();
            return ret;
         }
    }


    public static void releaseScoringMemory(CometScoringDataForScanBuild released) {
          pool.push(released);
     }

//    public static CometScoringDataForScanBuild borrowScoringMemory() {
//        return preallocatedData;
//
//

//        synchronized (CometScoringDataForScanBuild.class) {
//            if (gPreallocatedData == null) {
//                gPreallocatedData = new ThreadLocal<SoftReference<CometScoringDataForScanBuild>>();
//            }
//        }
//        CometScoringDataForScanBuild ret = gPreallocatedData.get();
//        if (ret == null) {
//            ret = new CometScoringDataForScanBuild();
//            gPreallocatedData.set(ret);
//        }
//        return ret;
//        synchronized (CometScoringData.class) {
//            if (gPreallocatedData == null) {
//                gPreallocatedData = new ThreadLocal<SoftReference<CometScoringDataForScanBuild>>();
//            }
//        }


//        SoftReference<CometScoringDataForScanBuild> cometScoringDataSoftReference = gPreallocatedData.get();
//        if(cometScoringDataSoftReference == null || cometScoringDataSoftReference.get() == null)
//        {
//            CometScoringDataForScanBuild value = new CometScoringDataForScanBuild();
//            cometScoringDataSoftReference =  new SoftReference<CometScoringDataForScanBuild>(value);
//            gPreallocatedData.set(cometScoringDataSoftReference);
//        }
//        CometScoringDataForScanBuild ret =  cometScoringDataSoftReference.get();

//        return ret;
//    }


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

    private CometScoringDataForScanBuild() {
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
        incrementMemoryAllocated(n * Float.SIZE);
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
