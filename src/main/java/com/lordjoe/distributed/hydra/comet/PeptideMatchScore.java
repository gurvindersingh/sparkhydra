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
    public   IPolypeptide peptide;
    public   double score;
    public   IonTypeScorer ions;
    public   String spectrumId;

    /**
     * just here for beany stuff
     */
    private PeptideMatchScore( ) {
      }


    public PeptideMatchScore(IPolypeptide peptideStr, double score,IonTypeScorer ions,String pSpectrumId) {
        this.peptide = peptideStr;
        this.score = score;
        this.ions = new LowMemoryIonScorer(ions);
        this.spectrumId = pSpectrumId;
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

    /* ========================
       Make this a bean

     */
    public IPolypeptide getPeptide() {
        return peptide;
    }

    public void setPeptide(final IPolypeptide pPeptide) {
        peptide = pPeptide;
    }

    public double getScore() {
        return score;
    }

    public void setScore(final double pScore) {
        score = pScore;
    }

    public IonTypeScorer getIons() {
        return ions;
    }

    public void setIons(final IonTypeScorer pIons) {
        ions = pIons;
    }

    public String getSpectrumId() {
        return spectrumId;
    }

    public void setSpectrumId(final String pSpectrumId) {
        spectrumId = pSpectrumId;
    }


}
