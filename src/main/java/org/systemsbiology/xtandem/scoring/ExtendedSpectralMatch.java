package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.testing.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.ExtendedSpectralMatch
 * this version has more information about the details of the fit - used primarily for development
 * User: Steve
 * Date: 8/9/11
 */
public class ExtendedSpectralMatch extends SpectralMatch implements IExtendedSpectralMatch {
    public static final ExtendedSpectralMatch[] EMPTY_ARRAY = {};

    private final ITheoreticalIonsScoring[] m_IonScoring;

    public ExtendedSpectralMatch(final IPolypeptide pp, final IMeasuredSpectrum pMeasured, final double pScore, final double pHyperScore, final double rawScore, final IonTypeScorer scorer, final ITheoreticalSpectrumSet theory, final ITheoreticalIonsScoring[] pIonScoring) {
        super(pp, pMeasured, pScore, pHyperScore, rawScore, scorer, theory);
        List<ITheoreticalIonsScoring> holder = new ArrayList<ITheoreticalIonsScoring>();
        // drop any sets with no matches
        for (int i = 0; i < pIonScoring.length; i++) {
            ITheoreticalIonsScoring ts = pIonScoring[i];
            if(ts.getMatchCount() > 0)
                holder.add(ts);
        }
        m_IonScoring = new ITheoreticalIonsScoring[holder.size()];
        holder.toArray(m_IonScoring);
        Arrays.sort(m_IonScoring);
       }

    @Override
    public ITheoreticalIonsScoring[] getIonScoring() {
        return m_IonScoring;
    }

    /**
     * return all scoring at a specific charge
     * @param charge charge
     * @return
     */
    @Override
    public ITheoreticalIonsScoring[] getIonScoring(final int charge) {
        List<ITheoreticalIonsScoring> holder = new ArrayList<ITheoreticalIonsScoring>();
        for (int i = 0; i < m_IonScoring.length; i++) {
            ITheoreticalIonsScoring test = m_IonScoring[i];
            if(test.getIdentifier().getCharge() == charge)
                holder.add(test);
        }
        ITheoreticalIonsScoring[] ret = new ITheoreticalIonsScoring[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * return the max charge where there is a score
     *
     * @return as above
     */
    @Override
    public int getMaxScoredCharge() {
        int ret = 0;
        for (int i = 0; i < m_IonScoring.length; i++) {
            ITheoreticalIonsScoring test = m_IonScoring[i];
            ret = Math.max(test.getIdentifier().getCharge(),ret);
          }
         return ret;
     }

    /**
     * return all scoring at a specific charge
     * @param charge charge
     * @return
     */
    @Override
    public DebugMatchPeak[] getMatchesAtCharge(final int charge) {
        ITheoreticalIonsScoring[] scores = getIonScoring(charge);
        List<DebugMatchPeak> holder = new ArrayList<DebugMatchPeak>();
        for (int i = 0; i < scores.length; i++) {
            ITheoreticalIonsScoring test = scores[i];
            DebugMatchPeak[] scoringMasses = test.getScoringMasses();
            holder.addAll(Arrays.asList(scoringMasses));
        }
        DebugMatchPeak[] ret = new DebugMatchPeak[holder.size()];
        holder.toArray(ret);
        Arrays.sort(ret);
        return ret;
    }
}
