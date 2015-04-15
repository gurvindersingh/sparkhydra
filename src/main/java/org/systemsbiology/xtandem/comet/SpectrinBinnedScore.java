package org.systemsbiology.xtandem.comet;

import org.systemsbiology.xtandem.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.comet.SpectrinBinnedScore
 * User: Steve
 * Date: 4/6/2015
 */
public class SpectrinBinnedScore implements Comparable<SpectrinBinnedScore>,IEquivalent<SpectrinBinnedScore> {

    public static List<SpectrinBinnedScore> fromBins(float[] bins) {
        List<SpectrinBinnedScore> holder = new ArrayList<SpectrinBinnedScore>();
        for (int i = 0; i < bins.length; i++) {
            float bin = bins[i];
            if (bin > 0.001) {
                holder.add(new SpectrinBinnedScore(i, bin));
            }
        }
        return holder;
    }

    public static List<SpectrinBinnedScore> fromResource(String resourceName) {
        try {
            List<SpectrinBinnedScore> holder = new ArrayList<SpectrinBinnedScore>();
            InputStream resourceAsStream = SpectrinBinnedScore.class.getResourceAsStream(resourceName);
            LineNumberReader rdr = new LineNumberReader(new InputStreamReader(resourceAsStream));
            String line = rdr.readLine();
            while (line != null) {
                holder.add(new SpectrinBinnedScore(line));
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

    public SpectrinBinnedScore(final int pBin, final double pScore) {
        bin = pBin;
        score = pScore;
    }

    public SpectrinBinnedScore(String s) {
        String[] split = s.trim().split("\t");
        bin = Integer.parseInt(split[0]);
        score = Double.parseDouble(split[1]);

    }


    @Override
    public int compareTo(final SpectrinBinnedScore o) {
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

        final SpectrinBinnedScore that = (SpectrinBinnedScore) o;

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
    public boolean equivalent(final SpectrinBinnedScore o) {
        if(bin != o.bin)
            return false;
        if(Math.abs(score - o.score) > 0.01)
             return false;
        return true;
     }
}
