package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.testing.*;

/**
 * org.systemsbiology.xtandem.scoring.IExtendedSpectralMatch
 * a march containing detailed information about the match
 * User: Steve
 * Date: 8/9/11
 */
public interface IExtendedSpectralMatch extends ISpectralMatch, Comparable<ISpectralMatch> {
    public static final IExtendedSpectralMatch[] EMPTY_ARRAY = {};

    /**
      * get all scoring
       * @return !null array
      */
      public ITheoreticalIonsScoring[] getIonScoring();

    /**
     * get all scoring at a specific charge
     * @param charge the charge
     * @return !null array
     */
    public ITheoreticalIonsScoring[] getIonScoring(int charge);

    /**
      * return all scoring at a specific charge
      * @param charge charge
      * @return
      */
      public DebugMatchPeak[] getMatchesAtCharge(final int charge);

    /**
     * return the max charge where there is a score
     * @return  as above
     */
    public int getMaxScoredCharge();


}
