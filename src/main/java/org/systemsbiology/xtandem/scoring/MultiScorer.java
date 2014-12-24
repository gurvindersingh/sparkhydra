package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.MultiScorer
 * User: Steve
 * Date: 1/12/12
 */
public class MultiScorer implements IMultiScorer  {
    public static final MultiScorer[] EMPTY_ARRAY = {};

    public static final String TAG = "MultiScorer";

    private final Map<String, IScoredScan> m_Scores = new HashMap<String, IScoredScan>();

    public MultiScorer() {
    }

    /**
       * return a list of all algorithm names
       *
       * @return !null array
       */
      @Override
      public String[] getScoringAlgorithms() {
          String[] strings = m_Scores.keySet().toArray(new String[0]);
          Arrays.sort(strings);
          return strings;
      }

    /**
       * return a list of all scors
         * @return !null array
       */
      @Override
      public IScoredScan[] getScoredScans() {
          String[] algorithms = getScoringAlgorithms();
          IScoredScan[] ret = new IScoredScan[algorithms.length];
           for (int i = 0; i < algorithms.length; i++) {
              String algorithm = algorithms[i];
              ret[i] = getScoredScan(algorithm);
          }
          return ret;
      }


    /**
     * true if some match is scored
     *
     * @return as above
     */
    @Override
    public boolean isMatchPresent() {
        for(IScoredScan scan : m_Scores.values())   {
            if(scan.isMatchPresent())
                return true;
        }
        return false;
    }


    /**
     * if present return a  IScoredScan for an algorithm
     *
     * @param algorithm
     * @return either data of blank if null
     */
    @Override
    public IScoredScan getScoredScan(final String algorithm) {
        return m_Scores.get(algorithm);
    }

    public void addAlgorithm(IScoredScan scan) {
        String key = scan.getAlgorithm();
        if (m_Scores.containsKey(key)) {
            throw new IllegalStateException("duplicate algorithm " + key);
         }
        else {
            m_Scores.put(key, scan);

        }
    }

    /**
     * combine two scores
     *
     * @param added
     */
    public void addTo(IMultiScorer added) {
        IScoredScan[] scoredScans = added.getScoredScans();
        for (int i = 0; i < scoredScans.length; i++) {
            IScoredScan addedScan = scoredScans[i];
            IScoredScan current = getScoredScan(addedScan.getAlgorithm());
            if(current == null)
                addAlgorithm(addedScan);
            else
                ((ScoredScan)current).addTo(addedScan);
        }
    }



    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    public void serializeAsString(IXMLAppender adder) {
        String[] algorithms = getScoringAlgorithms();

        String tag = TAG;
        adder.openTag(tag);
        adder.endTag();
        adder.cr();

        for (int i = 0; i < algorithms.length; i++) {
            String algorithm = algorithms[i];
            IScoredScan scoredScan = getScoredScan(algorithm);
            scoredScan.serializeAsString(adder);
        }
        adder.closeTag(tag);

    }

}
