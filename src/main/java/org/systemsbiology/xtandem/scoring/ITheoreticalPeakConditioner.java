package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.ionization.*;

/**
 * org.systemsbiology.xtandem.scoring.ITheoreticalPeakConditioner
 *  interface representing a conditioner - this might be used to generate
 *     normalized peaks or adjust peak scoring
 * @author Steve Lewis
 * @date Jan 17, 2011
 */
public interface ITheoreticalPeakConditioner
{
    public static ITheoreticalPeakConditioner[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ITheoreticalPeakConditioner.class;

    /**
     * return a modified peak
     * @param peak !null peak
     * @param addedData any additional data
     * @return  possibly null conditioned peak - null says ignore
     */
    public ITheoreticalPeak condition(ITheoreticalPeak peak, Object... addedData);
}
