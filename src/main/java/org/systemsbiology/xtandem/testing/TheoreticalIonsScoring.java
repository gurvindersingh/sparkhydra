package org.systemsbiology.xtandem.testing;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.testing.TheoreticalScoring
 * User: steven
 * Date: 6/22/11
 */
public class TheoreticalIonsScoring implements ITheoreticalIonsScoring,Comparable<ITheoreticalIonsScoring> {
    public static final ITheoreticalIonsScoring[] EMPTY_ARRAY = {};

    private boolean m_Reused;   // not sure why we reset spectra but this marks that case
    private final ScanScoringIdentifier m_Identifier;
    private final ITheoreticalScoring m_ParentScoring;
    private ITheoreticalSpectrum m_TheoreticalSpectrum;
    private IMeasuredSpectrum  m_MeasuredSpectrum;
    private double m_MZRatio;
    private double m_Score;
    private double m_CScore;

    private Map<Integer, DebugMatchPeak> m_ScoringMass = new HashMap<Integer, DebugMatchPeak>();

    public TheoreticalIonsScoring(final String pSequence, int charge, IonType type, final ITheoreticalScoring pScanScore) {
        this(new ScanScoringIdentifier(pSequence, charge, type), pScanScore);
    }

    public TheoreticalIonsScoring(ScanScoringIdentifier id, final ITheoreticalScoring pScanScore) {
        m_Identifier = id;
        if(id == null)
            throw new IllegalArgumentException("id cannot be null");
        m_ParentScoring = pScanScore;
        if("DKLQKDIR".equals(id.getSequence()))
             XTandemUtilities.breakHere();
    }



    public double getScore() {
        return m_Score;
    }

    public void setScore(final double pScore) {
        m_Score = pScore;
    }

    public double getCScore() {
        return m_CScore;
    }

    public void setCScore(final double pCScore) {
        m_CScore = pCScore;
    }

    @Override
    public ITheoreticalSpectrum getTheoreticalSpectrum() {
        return m_TheoreticalSpectrum;
    }


    @Override
    public double getMZRatio() {
        return m_MZRatio;
    }

    public void setMZRatio(final double pMZRatio) {
        m_MZRatio = pMZRatio;
    }


    public boolean isReused() {
        return m_Reused;
    }

    public void setReused(final boolean pReused) {
        m_Reused = pReused;
    }

    public void setTheoreticalSpectrum(final ITheoreticalSpectrum pTheoreticalSpectrum) {
        if(m_TheoreticalSpectrum == pTheoreticalSpectrum) {
            setReused(true);
            return;
        }
        if(m_TheoreticalSpectrum != null)    {
            setReused(true);
            if(m_TheoreticalSpectrum.equivalent(pTheoreticalSpectrum))
                    return;
            throw new UnsupportedOperationException("Can be set only once");
          }
         m_TheoreticalSpectrum = pTheoreticalSpectrum;
    }

    @Override
    public IMeasuredSpectrum getMeasuredSpectrum() {
        return m_MeasuredSpectrum;
    }

    public void setMeasuredSpectrum(final IMeasuredSpectrum pMeasuredSpectrum) {
        if(m_MeasuredSpectrum == pMeasuredSpectrum)    {
            setReused(true);
             return;
        }
         if(m_MeasuredSpectrum != null)    {
             setReused(true);
              if(m_MeasuredSpectrum.equivalent(pMeasuredSpectrum))
                     return;
             throw new UnsupportedOperationException("Can be set only once");
         }
         m_MeasuredSpectrum = pMeasuredSpectrum;
    }

    @Override
    public ScanScoringIdentifier getIdentifier() {
        return m_Identifier;
    }

    public IonType getType() {
        return getIdentifier().getType();
    }

    public int getCharge() {
        return getIdentifier().getCharge();
    }

    @Override
    public String toString() {
        double score = getScore();
        return getIdentifier().toString()
                + " " + getMatchCount()
                + " " + XTandemUtilities.formatDouble(score, 4)
                   ;
    }

    public int getMatchCount() {
        return m_ScoringMass.size();
    }


    /**
       * get the total score of all matches
       *
       * @return
       */
      public double getTotalScore() {
          // for some data we do know
          if(getScore() > 0)
              return getScore();

          DebugMatchPeak[] myScores = getScoringMasses();
          double ret = 0;
          for (int i = 0; i < myScores.length; i++) {
              DebugMatchPeak myScore = myScores[i];
              ret += myScore.getAdded();
          }
          return ret;
      }

        /**
     * get a total for matches of a specific ion type
     *
     * @param type !null type
     * @return as above
     */
    public double getTotalScore(IonType type) {
         // for some data we do know
          if(getScore() > 0)
              return getScore();

        DebugMatchPeak[] myScores = getScoringMasses();
        double ret = 0;
        for (int i = 0; i < myScores.length; i++) {
            DebugMatchPeak myScore = myScores[i];
            if (myScore.getType() != type)
                continue;
            ret += myScore.getAdded();
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
    public boolean equivalent(final ITheoreticalIonsScoring o) {
        if (getMatchCount() != o.getMatchCount())
            return false;
        DebugMatchPeak[] myScores = getScoringMasses();
        DebugMatchPeak[] theirScores = o.getScoringMasses();
        for (int i = 0; i < theirScores.length; i++) {
            DebugMatchPeak myScore = myScores[i];
            DebugMatchPeak theirScore = theirScores[i];
            if (!myScore.equivalent(theirScore))
                return false;

        }
        return true;
    }


    @Override
    public int compareTo(final ITheoreticalIonsScoring o) {
        return getIdentifier().compareTo(o.getIdentifier());
    }

    public void addScoringMass(DebugMatchPeak dp) {
        if(isReused())     {
            XTandemUtilities.breakHere();
            DebugMatchPeak old = m_ScoringMass.get(dp.getMass());
            if(old != null && old.equivalent(dp))
                return;
            throw new UnsupportedOperationException("Huh !!!"); // ToDo
        }

        m_ScoringMass.put(dp.getMass(), dp);
    }


    @Override
    public DebugMatchPeak[] getScoringMasses() {
        return m_ScoringMass.values().toArray(new DebugMatchPeak[0]);
    }

    @Override
    public DebugMatchPeak getScoringMass(Integer key) {
        return m_ScoringMass.get(key);
    }

    @Override
    public String getSequence() {
        return getIdentifier().getSequence();
    }

    @Override
    public ITheoreticalScoring getParentScoring() {
        return m_ParentScoring;
    }
}
