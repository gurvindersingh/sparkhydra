package org.systemsbiology.xtandem.bioml;

import com.lordjoe.utilities.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.scoring.*;

import java.util.*;
/**
 * org.systemsbiology.xtandem.bioml.XTandemScoringReport
 * User: steven
 * Date: 8/22/11
 */
public class XTandemScoringReport {
    public static final XTandemScoringReport[] EMPTY_ARRAY = {};

    private Map<String, ScoredScan> m_Scan = new HashMap<String, ScoredScan>();


    public Map<String, ScoredScan> getScansMap() {
        return new HashMap<String, ScoredScan>(m_Scan);
    }

    public Map<String, ScoredScan> getScoredScansMap() {
        Map<String, ScoredScan> ret = new HashMap<String, ScoredScan>() ;
        for (String key : m_Scan.keySet()) {
            ScoredScan test = getScan(  key) ;
            if(isScored(test))  {
                ret.put(key,test) ;
            }
        }
        return ret;
      }

    public static final double FAKE_HYPERSCORE = 1000;
    private boolean isScored(final ScoredScan pTest) {
        if (!pTest.isValidMatch() )
            return false;
        ISpectralMatch bestMatch = pTest.getBestMatch();
        double hyperScore = bestMatch.getHyperScore();
        return FAKE_HYPERSCORE != hyperScore;
    }

    public void addScan(  ScoredScan added) {
        String id = added.getKey();
        String algorithm = added.getAlgorithm();
        if(!TandemKScoringAlgorithm.ALGORITHM_NAME.equals(algorithm))
            return; // ignore alternate algorithms
        if(id == null)
            return;
        if(m_Scan.containsKey(id))
            throw new IllegalStateException("multiple add");
        m_Scan.put(id, added);
    }


    public ScoredScan[] getScans() {
        ScoredScan[] scoredScans = m_Scan.values().toArray(new ScoredScan[0]);
        Arrays.sort(scoredScans);
        return scoredScans;
    }

    public ScoredScan getScan(String key) {
        return m_Scan.get(key);
    }

    /**
     * read all tandem files in a directory - this tests read stability
     * @param args
     */
    public static void main(String[] args) {

        String[] tandemFiles = FileUtilities.getAllFilesWithExtension(".", ".tandem");
        for (int i = 0; i < tandemFiles.length; i++) {
            String tandemFile = tandemFiles[i];
            XMLUtilities.outputLine(tandemFile);
            XTandemScoringReport report = XTandemUtilities.readXTandemFile(tandemFile);
            ScoredScan[] scans = report.getScans();
            for (int j = 0; j < scans.length; j++) {
                ScoredScan scan = scans[j];
                scan = null;
            }
            XMLUtilities.outputLine( );

        }
     }

}
