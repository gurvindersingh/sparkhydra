package com.lordjoe.distributed.hydra.comet;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;

/**
 * com.lordjoe.distributed.hydra.comet.XCorrUsedData
 * User: Steve
 * Date: 4/6/2015
 */
public class XCorrUsedData implements Comparable<XCorrUsedData>, IEquivalent<XCorrUsedData> {

    public final int bin;
    public final IonType ion;
    public final int charge;
    public final double score;

    public XCorrUsedData(int pCharge, IonType pIon, final int pBin, final double pScore) {
        bin = pBin;
        score = pScore;
        ion = pIon;
        charge = pCharge;
    }

    public XCorrUsedData(String s) {
        String[] split = s.trim().split("\t");
        int index = 0;
        charge = Integer.parseInt(split[index++]);
        ion = IonType.valueOf(split[index++]);
        bin = Integer.parseInt(split[index++]);
        score = Double.parseDouble(split[index++]);

    }


    @Override
    public int compareTo(final XCorrUsedData o) {
        int ret = Integer.compare(bin, o.bin);
        if (ret != 0)
            return ret;
        ret = Double.compare(score, o.score);
        if (ret != 0)
            return ret;
        return 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final XCorrUsedData that = (XCorrUsedData) o;

        if (bin != that.bin) return false;
        return Double.compare(that.score, score) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = bin;
        temp = Double.doubleToLongBits(score);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "" + charge + "\t" + ion + "\t" + bin + "\t" + score;
    }

    /**
     * return true if this and o are 'close enough'
     *
     * @param o !null test object
     * @return as above
     */
    @Override
    public boolean equivalent(final XCorrUsedData o) {
        if (bin != o.bin)
            return false;
        if (ion != o.ion)
            return false;
        if (charge != o.charge)
            return false;
        if (Math.abs(score - o.score) > 0.01) // 0.01)
            return false;
        return true;
    }

}
