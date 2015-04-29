package com.lordjoe.distributed.hydra.comet;

import org.systemsbiology.xtandem.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.SpectrumBinnedScore
 * User: Steve
 * Date: 4/6/2015
 */
public class SpectrumBinnedScore implements Comparable<SpectrumBinnedScore>,IEquivalent<SpectrumBinnedScore> {

    public static List<SpectrumBinnedScore> fromBins(float[] bins) {
        List<SpectrumBinnedScore> holder = new ArrayList<SpectrumBinnedScore>();
        for (int i = 0; i < bins.length; i++) {
            float bin = bins[i];
            if (bin > 0.001) {
                holder.add(new SpectrumBinnedScore(i, bin));
            }
        }
        return holder;
    }

    public static List<SpectrumBinnedScore> fromResource(String resourceName) {
        try {
            List<SpectrumBinnedScore> holder = new ArrayList<SpectrumBinnedScore>();
            InputStream resourceAsStream = SpectrumBinnedScore.class.getResourceAsStream(resourceName);
            LineNumberReader rdr = new LineNumberReader(new InputStreamReader(resourceAsStream));
            String line = rdr.readLine();
            while (line != null) {
                holder.add(new SpectrumBinnedScore(line));
                line = rdr.readLine();
            }

            return holder;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public final int bin;
    public final double score;

    public SpectrumBinnedScore(final int pBin, final double pScore) {
        bin = pBin;
        score = pScore;
    }

    public SpectrumBinnedScore(String s) {
        String[] split = s.trim().split("\t");
        bin = Integer.parseInt(split[0]);
        score = Double.parseDouble(split[1]);

    }


    @Override
    public int compareTo(final SpectrumBinnedScore o) {
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

        final SpectrumBinnedScore that = (SpectrumBinnedScore) o;

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
        return "" + bin + "\t" + score;
    }

    /**
     * return true if this and o are 'close enough'
     *
     * @param o !null test object
     * @return as above
     */
    @Override
    public boolean equivalent(final SpectrumBinnedScore o) {
        if(bin != o.bin)
            return false;
        if(Math.abs(score - o.score) > 0.01)
             return false;
        return true;
     }
}
