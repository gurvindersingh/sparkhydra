package com.lordjoe.distributed.tandem;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
 * com.lordjoe.distributed.tandem.MultiScanComparison
 * class holding two  ScoredScan with for the same spectrum but different programs to compare
 * User: Steve
 * Date: 3/17/2015
 */
public class MultiScanComparison implements Comparable<MultiScanComparison> {

    public static void setNormalization(Collection<MultiScanComparison> items) {
        double sum_score1 = 0;
        double sum_score2 = 0;
        for (MultiScanComparison item : items) {
            double hs1 = hyperScore(item.scan1);
            double hs2 = hyperScore(item.scan2);
            sum_score1 += hs1;
            sum_score2 += hs2;
        }

        double ns1 = sum_score1 / items.size();
        double ns2 = sum_score2 / items.size();
        for (MultiScanComparison item : items) {
            item.setNormalizationFactor1(ns1);
            item.setNormalizationFactor2(ns2);
        }

    }

    public static double hyperScore(final ScoredScan item) {
        return item.getBestMatch().getHyperScore();
    }

    public static IPolypeptide peptide(final ScoredScan item) {
        return item.getBestMatch().getPeptide();
    }

    public final String id;
    public final ScoredScan scan1;
    public final ScoredScan scan2;
    public final IPolypeptide peptide1;
    public final IPolypeptide peptide2;
    private double score;
    private double score1;
    private double score2;
    private double normalizationFactor1;
    private double normalizationFactor2;

    public MultiScanComparison(final ScoredScan pScan1, final ScoredScan pScan2) {
        scan1 = pScan1;
        peptide1 = peptide(scan1);
        scan2 = pScan2;
        peptide2 = peptide(scan2);
        RawPeptideScan raw1 = (RawPeptideScan) scan1.getRaw();
        RawPeptideScan raw2 = (RawPeptideScan) scan2.getRaw();
        id = raw1.getLabel();
        String id2 = raw2.getLabel();
        if (!id.equals(id2))
            throw new IllegalStateException("nod same id " + id + " " + id2);
    }


    public boolean isPeptideSame()
    {
        String s1 = peptide1.toString();
        String s2 = peptide2.toString();
        return s1.equals(s2);
    }

    public double getNormalizationFactor1() {
        return normalizationFactor1;
    }

    public void setNormalizationFactor1(final double pNormalizationFactor1) {
        normalizationFactor1 = pNormalizationFactor1;
    }

    public double getNormalizationFactor2() {
        return normalizationFactor2;
    }

    public void setNormalizationFactor2(final double pNormalizationFactor2) {
        normalizationFactor2 = pNormalizationFactor2;
    }

    public double getScore() {
        if (score == 0)
            score = buildScore();
        return score;
    }

    private double buildScore() {
        score1 = hyperScore(scan1) / getNormalizationFactor1();
        score2 = hyperScore(scan2) / getNormalizationFactor2();
        return Math.max(score1, score2);
    }


    @Override
    public int compareTo(final MultiScanComparison o) {
        int ret = Double.compare(o.getScore(), getScore());
        if (ret != 0)
            return ret;
        return Integer.compare(System.identityHashCode(this), System.identityHashCode(o));
    }


    @Override
    public String toString() {
        return id + " " + String.format("%10.2f", getScore());
    }
}
