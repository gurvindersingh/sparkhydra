package org.systemsbiology.xtandem.testing;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;

/**
 * org.systemsbiology.xtandem.testing.ITheoreticalIonsScoring
 * User: steven
 * Date: 8/8/11
 */
public interface ITheoreticalIonsScoring extends Comparable<ITheoreticalIonsScoring>,IEquivalent<ITheoreticalIonsScoring> {
    public static final ITheoreticalIonsScoring[] EMPTY_ARRAY = {};

    public int getMatchCount();

    public ITheoreticalSpectrum getTheoreticalSpectrum();

    public double getMZRatio();

    public IMeasuredSpectrum getMeasuredSpectrum();

    public ScanScoringIdentifier getIdentifier();

    public DebugMatchPeak[] getScoringMasses();

    public DebugMatchPeak getScoringMass(Integer key);

    public String getSequence();

    public ITheoreticalScoring getParentScoring();
}
