package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.hydra.fragment.BinChargeKey;
import org.systemsbiology.xtandem.IMeasuredSpectrum;
import org.systemsbiology.xtandem.peptide.IPolypeptide;

import java.util.HashSet;
import java.util.Set;

/**
 * com.lordjoe.distributed.hydra.comet.BinChargeMapper
 *
 * @author Steve Lewis
 * @date 5/24/2015
 */
public class BinChargeMapper {

    // how wide are the bins
    private static double binSize = 0.01;
    private static int examWidthInBins = 9;



    // how wide to we search
    public static  double examineWidth = binSize * 9;
    public static final int MAX_CHARGE_PLUS_ONE = 5;


    public static double getBinSize() {
        return binSize;
    }

    public static void setBinSize(double binSize) {
        BinChargeMapper.binSize = binSize;
    }

    public static int getExamWidthInBins() {
        return examWidthInBins;
    }

    public static void setExamWidthInBins(int examWidthInBins) {
        BinChargeMapper.examWidthInBins = examWidthInBins;
    }

    public static double getExamineWidth() {
        return examineWidth;
    }

    public static void setExamineWidth(double examineWidth) {
        BinChargeMapper.examineWidth = examineWidth;
    }

    /**
     * do the work of getting keys from a spectrum as a list
     * used in testing since other code is used with rdds
     * @param spec
     * @return
     */
    public static Set<BinChargeKey> getSpectrumBins(IMeasuredSpectrum spec) {
        Set<BinChargeKey> ret = new HashSet<BinChargeKey>();
        Set<BinChargeKey> binChargeKeys = keysFromSpectrum(spec);
        ret.addAll(binChargeKeys);
        return ret;
    }

    /**
     * @param spec
     * @return
     */
    public static Set<BinChargeKey> keysFromSpectrum(IMeasuredSpectrum spec) {
        // this is the code used by BinCharge Mapper - todo make it a method
        int charge = 1; // all peptides use 1 now
        // code using MZ
        double matchingMass = spec.getPrecursorMass();   // todo decide whether mass or mz is better
        Set<BinChargeKey> keys =  keysFromChargeMzXX(charge, matchingMass);
        return keys;
    }

    public static BinChargeKey keyFromPeptide(IPolypeptide pp) {
          double matchingMass = CometScoringAlgorithm.getCometMatchingMass(pp);
        BinChargeKey  key = oneKeyFromChargeMz(1, matchingMass);
        return key;
    }

    /**
     * create one key from change and MZ
     *
     * @param charge
     * @param mz
     * @return
     */
    public static  BinChargeKey oneKeyFromChargeMz(int charge, double mz) {
//        List<BinChargeKey> holder = new ArrayList<BinChargeKey>();
//        double v = (mz) / binSize;
//        double mzStart = ((int) ( 0.5 +  v)   * binSize);
//        double quantizedMz = mzStart ;
        BinChargeKey ret = new BinChargeKey(charge, mz);
        double mzx = ret.getMz();
        if (Math.abs(mz - mzx) > binSize)
            throw new IllegalStateException("bad bin key");
        return ret;
    }

    /**
     * used to bin spectra which are sent to more than one bin
     *
     * @param charge
     * @param mz
     * @return
     */
    private static Set<BinChargeKey> keysFromChargeMzXX(int charge, double mz) {
        Set<BinChargeKey> holder = new HashSet<BinChargeKey>();
        double startMZ = mz - examineWidth;
        int start = BinChargeKey.mzAsInt(startMZ);
        while (BinChargeKey.intToMz(start) < mz + examineWidth) {
            holder.add(new BinChargeKey(charge, BinChargeKey.intToMz(start++)));
        }
//        double mzStart = ((int) (0.5 + ((mz - examineWidth) / binSize))) * binSize;
//        for (int i = 0; i < examineWidth / binSize; i++) {
//            double quantizedMz = (mzStart + i) * binSize;
//            holder.add(new BinChargeKey(charge, quantizedMz)); // todo add meighbors
//
//        }


//        BinChargeKey[] ret = new BinChargeKey[holder.size()];
//        holder.toArray(ret);
        return holder;
    }
}
