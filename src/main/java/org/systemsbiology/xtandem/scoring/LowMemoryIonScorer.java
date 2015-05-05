package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.ionization.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.LowMemoryIonScorer
 * a version  of IonTypeScorer without the memory requirements for comet scoring
 */
public class LowMemoryIonScorer implements IonTypeScorer,Serializable {

    private final int numberMatchedPeaks;
    private final Map<IonType,Integer> ionCounts = new HashMap<IonType,Integer>();
    private final Map<IonType,Double>  ionScores = new HashMap<IonType,Double>();

    public LowMemoryIonScorer(IonTypeScorer source) {
        numberMatchedPeaks = source.getNumberMatchedPeaks();
        for (IonType byIonType : IonType.BY_ION_TYPES) {
            ionCounts.put(byIonType,source.getCount(byIonType));
            ionScores.put(byIonType,source.getScore(byIonType));
        }
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
         return ionScores.get(type);
    }

    /**
     * get the count for a given ion type
     *
     * @param type !null iontype
     * @return count for that type
     */
    @Override
    public int getCount(IonType type) {
         return ionCounts.get(type);
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
