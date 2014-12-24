package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;

/**
 * org.systemsbiology.xtandem.scoring.ISpectrumMatcher
 *   This is a  strategy pattern to implement how spectra are
 *     traversed looking for matching peaks -  because the spectra
 *     can be reloaded, the presumption is that any object will be reused
 *     and any state will be reset by a new load
 * User: steven
 * Date: 1/19/11
 */
public interface ISpectrumMatcher {
    public static final ISpectrumMatcher[] EMPTY_ARRAY = {};

    /**
     * prepare tp compare a measured and a theoretical spectrum
     * @param measured  !null measured spectrum
     * @param test !null theoretical spectrum
     */
    public void load(IMeasuredSpectrum measured, ITheoreticalSpectrum test);

    /**
     * return an array with the next two matching peaks - return
     *   null when there are no more matches
     * @return
     */
    public ISpectrumPeak[] nextMatch();

}
