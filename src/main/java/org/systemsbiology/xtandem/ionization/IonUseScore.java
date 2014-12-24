package org.systemsbiology.xtandem.ionization;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.sax.*;

/**
 * org.systemsbiology.xtandem.ionization.IonUseScore
 * immutable version of an IonUseCounter used after the score is accumulated
 *
 * @author Steve Lewis
 * @date Feb 14, 2011
 */
public class IonUseScore implements IonTypeScorer {
    public static IonUseScore[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IonUseScore.class;

    public static final IonType[] COMMON_TYPES = { IonType.B ,  IonType.Y };

    private final int[] m_Counts = new int[IonType.values().length];
    private final double[] m_Scores = new double[IonType.values().length];

    public IonUseScore() {
    }

    public IonUseScore(IonUseScore template) {
        this();
        add(template);

    }

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(IonTypeScorer test) {
        if (test == this)
            return true;


        for (IonType type : IonType.values()) {
            if (getCount(type) != test.getCount(type))
                return false;
            if (!XTandemUtilities.equivalentDouble(getScore(type), test.getScore(type)))
                return false;

        }
        return true;
    }

    public void add(IonUseScore template) {

        int[] counts = template.getCounts();
        double[] scores = template.getScores();

        for (int i = 0; i < counts.length; i++) {
            m_Counts[i] += counts[i];
            m_Scores[i] += scores[i];

        }

    }

    public void set(IonUseScore template) {

        int[] counts = template.getCounts();
        double[] scores = template.getScores();

        for (int i = 0; i < counts.length; i++) {
            m_Counts[i] = counts[i];
            m_Scores[i] = scores[i];

        }

    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < COMMON_TYPES.length; i++) {
            IonType type =  COMMON_TYPES[i];
            sb.append(type.toString() + ":(" + getCount(type) + ")" +
                XTandemUtilities.formatDouble(getScore(type),3));

        }
        return sb.toString();
    }

    protected int[] getCounts() {
        return m_Counts;
    }

    protected double[] getScores() {
        return m_Scores;
    }

    /**
     * get the total peaks matched for all ion types
     *
     * @return
     */
    public int getNumberMatchedPeaks() {
        int ret = 0;
        for (int i = 0; i < m_Counts.length; i++) {
            ret += m_Counts[i];

        }
        return ret;
    }
    /**
     * get the total score of all ion types
      * @return sum of all scores
     */
    public double getTotalScore() {
        double ret = 0;
        for (int i = 0; i < m_Scores.length; i++) {
            ret += m_Scores[i];

        }
        return ret;
    }

    /**
     * true if something is scored
     * @return
     */
    public boolean hasScore() {
         for (int i = 0; i < m_Scores.length; i++) {
             if(m_Counts[i] > 0)
                 return true;
             if(m_Scores[i] > 0)
                 return true;
        }
        return false;
    }

    @Override
    public double getScore(IonType type) {
        int index = IonType.asIndex(type);
        return getScores()[index];
    }

    @Override
    public int getCount(IonType type) {
        int index = IonType.asIndex(type);
        return getCounts()[index];
    }

    public void setScore(IonType type, double score) {
        int index = IonType.asIndex(type);
        getScores()[index] = score;

    }

    public void setCount(IonType type, int count) {
        int index = IonType.asIndex(type);
        getCounts()[index] = count;

    }

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    @Override
    public void serializeAsString(final IXMLAppender adder) {
        adder.openTag("IonScore");
        IonType[] values = IonType.values();
        for (int i = 0; i < values.length; i++) {
            IonType value = values[i];
            String type = value.toString();
            adder.appendAttribute(type + "_count", getCount(value));
            String scoreStr = XTandemUtilities.formatDouble(getScore(value), 5);
            adder.appendAttribute(type + "_score", scoreStr);
        }
        adder.closeTag("IonScore");

    }

}
