package com.lordjoe.distributed.hydra.comet;

import org.systemsbiology.xtandem.ionization.IonType;

/**
 * com.lordjoe.distributed.hydra.comet.MassDifferenceAccumulator
 *     tewst class to accumulate small mass differences
 * @author Steve Lewis
 * @date 5/27/2015
 */
public class MassDifferenceAccumulator {
    public final int[] B_Bins = new int[51];
    public final int[] Y_Bins = new int[51];

    public int accumulateDifference(double mass1,double mass2,IonType type)   {
        double diff = mass1 - mass2;
        int bin = assignBin(diff);
        switch(type)  {
            case B :
                B_Bins[bin]++;
                return bin;
            case Y :
                Y_Bins[bin]++;
                return bin;
            default:
                throw new IllegalStateException("problem"); // todo fix

        }
    }

    private int assignBin(double diff) {
        int ret = 25 + (int)(diff * 20000) ;
        ret = Math.max(0,ret);
        ret = Math.min(50,ret);
        if(ret == 25)
            return ret;
        return ret;
    }

}
