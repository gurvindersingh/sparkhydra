package org.systemsbiology.xtandem.testing;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.scoring.*;

/**
 * org.systemsbiology.xtandem.testing.ITheoreticalScoring
 * interface for scoring a single peptide against a single scan
 * User: steven
 * Date: 8/8/11
 */
public interface ITheoreticalScoring extends Comparable<ITheoreticalScoring>,IEquivalent<ITheoreticalScoring> {
    public static final ITheoreticalScoring[] EMPTY_ARRAY = {};

    /**
     * return an implememtation of a production interface
     * @return
     */
    public ISpectralMatch asSpectralMatch();

    public IMeasuredSpectrum getMeasuredSpectrum();

    /**
     * get the scoring for all sequence/charge/type combination;
     * @return !null array
     */
    public ITheoreticalIonsScoring[] getIonScorings();

    /**
     * scoring for a specific sequence/charge/type combination
     * @param key  key for above
     * @return  !null scoring
     */
    public TheoreticalIonsScoring getIonScoring(ScanScoringIdentifier key);

    public double getMZRatio();

    /**
     * get the total score of all matches
     *
     * @return
     */
    public double getTotalScore();

    /**
     * get the way KScoring makes a total score
     *
     * @return
     */
    public double getTotalKScore();

    /**
     * get a total for matches of a specific ion type
     *
     * @param type !null type
     * @return as above
     */
    public double getTotalScore(IonType type);

    public String getSequence();

    public IScanScoring getScanScore();
}
