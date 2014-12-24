package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;

/**
 * org.systemsbiology.xtandem.scoring.IPeakFilter
 *  interface representing a conditioner - this might be used to generate
 *     normalized peaks
 * @author Steve Lewis
 * @date Jan 17, 2011
 */
public interface IPeakConditioner
{
    public static IPeakConditioner[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IPeakConditioner.class;

    /**
     * decide whether to keep a peal or not
     * @param peak !null peak
     * @param addedData any additional data
     * @return  possibly null conditioned peak - null says ignore
     */
    public ISpectrumPeak condition(ISpectrumPeak peak, Object... addedData);
}
