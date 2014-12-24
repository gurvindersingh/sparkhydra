package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;

/**
 * org.systemsbiology.xtandem.scoring.ISpectralScore
 * User: steven
 * Date: 2/7/11
 */
public interface ISpectralScore extends Comparable<ISpectralScore>,IonTypeScorer
{
    public static final ISpectralScore[] EMPTY_ARRAY = {};

     public IMeasuredSpectrum getSpectrum();

     public ITheoreticalSpectrum getFragment();

   
     public double getScore();

     public double getHyperScore();
}
