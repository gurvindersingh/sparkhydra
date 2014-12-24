package org.systemsbiology.xtandem.testing;

import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.testing.ScanScoringReport
 * User: steven
 * Date: 6/22/11
 */
public class ScanScoringReport implements IEquivalent<ScanScoringReport> {
    public static final ScanScoringReport[] EMPTY_ARRAY = {};

    private final ScoringProcesstype m_Type;
    private final Map<String, IScanScoring> m_ScanScoring = new HashMap<String, IScanScoring>();

    public ScanScoringReport(ScoringProcesstype type) {
        m_Type = type;
    }

    public ScoringProcesstype getType() {
        return m_Type;
    }




    public int getTotalScoreCount()
     {
         int ret = 0;
          IScanScoring[] scorings = getScanScoring();
         for (int i = 0; i < scorings.length; i++) {
             IScanScoring scoring = scorings[i];
             ret += scoring.getScoreCount();
         }
         return ret;
     }

        /**
     * return true if this and o are 'close enough'
     *
     * @param o !null test object
     * @return as above
     */
    @Override
    public boolean equivalent(final ScanScoringReport o) {
        if(getTotalScoreCount() != o.getTotalScoreCount())
            return false;
        XMLUtilities.outputLine("<scan_report>");
        IScanScoring[] myScores = getScanScoring();
        IScanScoring[] theirScores = o.getScanScoring();
        for (int i = 0; i < theirScores.length; i++) {
            IScanScoring myScore = myScores[i];
            IScanScoring theirScore = theirScores[i];
            if(!myScore.equivalent(theirScore))
                return false;

        }
        XMLUtilities.outputLine("</scan_report>");
         return true;
    }

    public void addScanScoring(String key, IScanScoring added) {
        m_ScanScoring.put(key, added);
    }


    public void removeScanScoring(String removed) {
        m_ScanScoring.remove(removed);
    }

    public IScanScoring[] getScanScoring() {
        IScanScoring[] scanScorings = m_ScanScoring.values().toArray(new IScanScoring[0]);
        Arrays.sort(scanScorings);
        return scanScorings;
    }

    public IScanScoring getScanScoringMap(String key) {
        synchronized (m_ScanScoring) {
            IScanScoring scanScoring = m_ScanScoring.get(key);
            if(scanScoring == null)  {
                 scanScoring = buildScoreScanning(key);
                 m_ScanScoring.put(key, scanScoring);
                return scanScoring;
            }
            else {
                return scanScoring;

            }
         }

    }

    protected IScanScoring buildScoreScanning(String key)
    {
        switch(getType()) {
            case JXTandem:
                return new ScanScoring(key);
              //  throw new UnsupportedOperationException("Fix This"); // ToDo
              case XTandem:
                  return new ScanScoring(key);
        }
        throw new UnsupportedOperationException("Never get here");
    }


}
