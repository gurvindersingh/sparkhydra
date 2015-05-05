package com.lordjoe.distributed.hydra.comet;

import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.comet.PeptideMatchScore
 *  hold a basic score - we can build a match from this
 * @author Steve Lewis
 * @date 5/5/2015
 */
public class PeptideMatchScore implements Serializable, Comparable<PeptideMatchScore> {
    public final IPolypeptide peptide;
    public final double score;
    public final IonTypeScorer ions;


    public PeptideMatchScore(IPolypeptide peptideStr, double score,IonTypeScorer ions) {
        this.peptide = peptideStr;
        this.score = score;
        this.ions = new LowMemoryIonScorer(ions);
    }


    @Override
    public int compareTo(PeptideMatchScore o) {
        int ret = Double.compare(o.score, score);
        if (ret != 0)
            return ret;
        return peptide.toString().compareTo(o.peptide.toString());
    }

    @Override
    public String toString() {
        return "PeptideMatchScore{" +
                "peptide='" + peptide + '\'' +
                ", score=" + score +
                '}';
    }
}
