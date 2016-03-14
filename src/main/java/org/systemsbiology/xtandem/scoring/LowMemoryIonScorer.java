package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.ionization.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.LowMemoryIonScorer
 * a version  of IonTypeScorer without the memory requirements for comet scoring
 * Modified to be a bean and support only B and Y types
 */
public class LowMemoryIonScorer implements IonTypeScorer, Serializable {

    private static int ionTypeToIndex(IonType type) {
        switch (type) {
            case B:
                return 0;
            case Y:
                return 1;
            default:
                throw new IllegalStateException("only B and Y handled");
        }
    }

    private int numberMatchedPeaks;
    private int[] ionCounts = new int[2];
    private double[] ionScores = new double[2];
    //   private final Map<IonType,Integer> ionCounts = new HashMap<IonType,Integer>();
//    private final Map<IonType,Double>  ionScores = new HashMap<IonType,Double>();

    public LowMemoryIonScorer(IonTypeScorer source) {
        numberMatchedPeaks = source.getNumberMatchedPeaks();
        for (IonType byIonType : IonType.BY_ION_TYPES) {

            int count = source.getCount(byIonType);
            if (count > 0) {
                int index = ionTypeToIndex(byIonType);
                ionCounts[index] = count;
                ionScores[index] = source.getScore(byIonType);
            }
        }
    }

    public void setNumberMatchedPeaks(int numberMatchedPeaks) {
        this.numberMatchedPeaks = numberMatchedPeaks;
    }

    public int[] getIonCounts() {
        return ionCounts;
    }

    public void setIonCounts(int[] ionCounts) {
        this.ionCounts = ionCounts;
    }

    public double[] getIonScores() {
        return ionScores;
    }

    public void setIonScores(double[] ionScores) {
        this.ionScores = ionScores;
    }

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(IonTypeScorer test) {
        if (true) throw new UnsupportedOperationException("Fix This");
        return false;
    }

    /**
     * get the total peaks matched for all ion types
     *
     * @return
     */
    @Override
    public int getNumberMatchedPeaks() {
        return numberMatchedPeaks;
    }

    /**
     * get the score for a given ion type
     *
     * @param type !null iontype
     * @return score for that type
     */
    @Override
    public double getScore(IonType type) {
        int index = ionTypeToIndex(type);
        return ionScores[index];
    }

    /**
     * get the count for a given ion type
     *
     * @param type !null iontype
     * @return count for that type
     */
    @Override
    public int getCount(IonType type) {
        int index = ionTypeToIndex(type);
        return ionCounts[index];
    }

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    @Override
    public void serializeAsString(IXMLAppender adder) {
        if (true) throw new UnsupportedOperationException("Fix This");

    }
}
