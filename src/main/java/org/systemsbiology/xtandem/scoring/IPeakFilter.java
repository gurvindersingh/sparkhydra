package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;

/**
 * org.systemsbiology.xtandem.scoring.IPeakFilter
 *  interface representing a filtering process on peaks allowing a scorer
 *   to remove peaks
 * @author Steve Lewis
 * @date Jan 17, 2011
 */
public interface IPeakFilter
{
    public static IPeakFilter[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IPeakFilter.class;

    /**
     * decide whether to keep a peal or not
     * @param peak !null peak
     * @param addedData any additional data
     * @return  true if the peak is to be used
     */
    public boolean isPeakRetained(ISpectrumPeak peak, Object... addedData);
}
