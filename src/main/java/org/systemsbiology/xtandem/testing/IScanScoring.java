package org.systemsbiology.xtandem.testing;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.scoring.*;

/**
 * org.systemsbiology.xtandem.testing.IScanScoring
 * User: steven
 * Date: 8/8/11
 */
public interface IScanScoring extends IEquivalent<IScanScoring>, Comparable<IScanScoring> {
    public static final IScanScoring[] EMPTY_ARRAY = {};

    public String getId();

    public int getScoreCount();

    public boolean equivalent(final IScanScoring o);

    public ITheoreticalScoring[] getScorings();

    /**
     * return scors dropping thise with no score
     *
     * @return
     */
    public ITheoreticalScoring[] getValidScorings();

    /**
     * return scors dropping thise with no score
     *
     * @return
     */
    public ISpectralMatch[] getSpectralMatches();

    public ITheoreticalScoring getScoring(String sequence);

    public ITheoreticalScoring getExistingScoring(String sequence);

    public ITheoreticalIonsScoring getIonScoring(ScanScoringIdentifier key);
}
