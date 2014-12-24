package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;

/**
 * org.systemsbiology.xtandem.scoring.DefaultPeakScoreAdder
 * User: steven
 * Date: 1/19/11
 */
public class DefaultPeakScoreAdder implements IPeakScoreAdder {
    public static final DefaultPeakScoreAdder[] EMPTY_ARRAY = {};

    /**
     * we really need only one of these
     */
    public static final IPeakScoreAdder INSTANCE = new DefaultPeakScoreAdder();


    private DefaultPeakScoreAdder() {
    }

    /**
     * compute a new score from the old score and two new peaks
     * usually implemented as return oldScore + peaks[0].getPeak() * peaks[1].getPeak()
     *
     * @param oldScore
     * @param peaks
     * @return
     */
    @Override
    public double addToScore(final double oldScore, final ISpectrumPeak[] peaks) {
        return oldScore + peaks[0].getPeak() * peaks[1].getPeak();
    }
}
