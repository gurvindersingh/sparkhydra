package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;

/**
 * org.systemsbiology.xtandem.scoring.IPeakScoreAdder
 * This is a strategy pattern to handle how two matched peaks are scored
 *   usually implemented as return oldScore + peaks[0].getPeak() * peaks[1].getPeak()
 * User: steven
 * Date: 1/19/11
 */
public interface IPeakScoreAdder {
    public static final IPeakScoreAdder[] EMPTY_ARRAY = {};

    /**
     * compute a new score from the old score and two new peaks
     *   usually implemented as return oldScore + peaks[0].getPeak() * peaks[1].getPeak()
     * @param oldScore
     * @param peaks
     * @return
     */
    public double addToScore(double oldScore,ISpectrumPeak[] peaks);
}
