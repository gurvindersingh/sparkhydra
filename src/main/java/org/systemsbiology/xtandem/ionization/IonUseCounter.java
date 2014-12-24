package org.systemsbiology.xtandem.ionization;

import org.systemsbiology.xtandem.*;

/**
 * org.systemsbiology.xtandem.ionization.IonUseCounter
 * Count types of ions - this is used by TandemScoring - where
 * scode is the factorial of the number of matches
 *
 * @author Steve Lewis
 * @date Jan 13, 2011
 */
public class IonUseCounter extends IonUseScore {
    public static IonUseScore[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IonUseCounter.class;


    public IonUseCounter() {

    }

    /**
     * copy constructor
     *
     * @param !null src
     */
    public IonUseCounter(IonUseCounter src) {
        this();
        loadFrom(src);
    }


    public void addScore(IonType type, double score) {

        int index = IonType.asIndex(type);
        getScores()[index] += score;
    }

    public void addCount(IonType type) {
        if(IonType.B == type)
            XTandemUtilities.breakHere();

        int index = IonType.asIndex(type);
        getCounts()[index]++;
    }

    public void clear() {
        int[] counts = getCounts();
        double[] scores = getScores();

        for (int i = 0; i < counts.length; i++) {
            counts[i] = 0;
            scores[i] = 0;

        }
    }

    /**
     * copy the contents from another counter
     *
     * @param source
     */
    public void loadFrom(IonUseCounter source) {
        int[] counts = getCounts();
        double[] scores = getScores();
        int[] src_counts = source.getCounts();
        double[] src_scores = source.getScores();

        for (int i = 0; i < counts.length; i++) {
            counts[i] = src_counts[i];
            scores[i] = src_scores[i];

        }

    }

    /**
     * add the contents of another counter
     *
     * @param !null source counter
     */
    public void addFrom(IonUseCounter source) {
        int[] counts = getCounts();
        double[] scores = getScores();
        int[] src_counts = source.getCounts();
        double[] src_scores = source.getScores();

        for (int i = 0; i < counts.length; i++) {
            counts[i] += src_counts[i];
            scores[i] += src_scores[i];

        }

    }
}
