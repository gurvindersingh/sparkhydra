package org.systemsbiology.xtandem.testing;

import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.testing.ScanScoring
 * This class represents the scoring of a single scan - it has more information that the production report
 * User: steven
 * Date: 6/22/11
 */
public class ScanScoring implements IScanScoring {
    public static final IScanScoring[] EMPTY_ARRAY = {};

    private final String m_Id;

    private Map<String, ITheoreticalScoring> m_Scoring = new HashMap<String, ITheoreticalScoring>();


    public ScanScoring(final String pId) {
        m_Id = pId;
    }

    public String getId() {
        return m_Id;
    }


    /**
     * return true if this and o are 'close enough'
     *
     * @param o !null test object
     * @return as above
     */
    @Override
    public boolean equivalent(final IScanScoring o) {
        if (getScoreCount() != o.getScoreCount())
            return false;
        ITheoreticalScoring[] myScores = getScorings();
        ITheoreticalScoring[] theirScores = o.getScorings();
        XMLUtilities.outputLine("<scored_id id=\"" + o.getId() + "\"  >");
        for (int i = 0; i < theirScores.length; i++) {
            ITheoreticalScoring myScore = myScores[i];
            ITheoreticalScoring theirScore = theirScores[i];
            if (!myScore.equivalent(theirScore))
                return false;
            double totalKScore = theirScore.getTotalKScore();
            XMLUtilities.outputLine("<scored_fragment seguence=\"" + myScore.getSequence() + "\" score=" + totalKScore + "\" />");

        }
        XMLUtilities.outputLine("</scored_id>");

        return true;
    }


    @Override
    public int compareTo(final IScanScoring o) {
        if (this == o)
            return 0;
        int ret = getId().compareTo(o.getId());
        if (ret != 0)
            return ret;

        return 0;
    }


    @Override
    public String toString() {
        return getId().toString() + " " + getScoreCount();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public int getScoreCount() {
        return m_Scoring.size();
    }


    public void addScoring(String key, ITheoreticalScoring added) {
        // what happens on dupoicates
        if (m_Scoring.containsKey(key))
            throw new UnsupportedOperationException("Fix This"); // ToDo
        m_Scoring.put(key, added);
    }


    public void removeScoring(String removed) {
        m_Scoring.remove(removed);
    }

    @Override
    public ITheoreticalScoring[] getScorings() {
        ITheoreticalScoring[] scorings = m_Scoring.values().toArray(new ITheoreticalScoring[0]);
        Arrays.sort(scorings);
        return scorings;
    }

    /**
     * return scores dropping thise with no score
      * @return
     */
    @Override
    public ITheoreticalScoring[] getValidScorings() {
        ITheoreticalScoring[] scorings = getScorings();
        List<ITheoreticalScoring> holder = new ArrayList<ITheoreticalScoring>();

        for (int i = 0; i < scorings.length; i++) {
            ITheoreticalScoring scoring = scorings[i];
            if (scoring.getIonScorings().length == 0)
                continue;
            holder.add(scoring);
        }
        ITheoreticalScoring[] ret = new ITheoreticalScoring[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * return scors dropping thise with no score
     *
     * @return
     */
    @Override
    public ISpectralMatch[] getSpectralMatches() {
        ITheoreticalScoring[] tss = getValidScorings();
        List<ISpectralMatch> holder = new ArrayList<ISpectralMatch>();
        for (int i = 0; i < tss.length; i++) {
            ITheoreticalScoring ts = tss[i];
            holder.add(ts.asSpectralMatch());
        }
        ISpectralMatch[] ret = new ISpectralMatch[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    @Override
    public ITheoreticalScoring getScoring(String sequence) {
        synchronized (m_Scoring) {
            ITheoreticalScoring scanScoring = m_Scoring.get(sequence);
            if (scanScoring == null) {
                scanScoring = new TheoreticalScoring(sequence, this);
                m_Scoring.put(sequence, scanScoring);
                return scanScoring;
            }
            else {
                return scanScoring;

            }
        }

    }

    @Override
    public ITheoreticalScoring getExistingScoring(String sequence) {
        synchronized (m_Scoring) {
            ITheoreticalScoring scanScoring = m_Scoring.get(sequence);
            return scanScoring;
        }

    }


    @Override
    public ITheoreticalIonsScoring getIonScoring(ScanScoringIdentifier key) {
        synchronized (m_Scoring) {
            String sequence = key.getSequence();
            ITheoreticalScoring ts = getScoring(sequence);
            return ts.getIonScoring(key);
        }

    }

}
