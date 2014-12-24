package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.ISpectrumPeak
 *  interface representing a single spectral peak with MZ and
 * @author Steve Lewis
 * @date Jan 13, 2011
 */
public interface ISpectrumPeak extends Comparable<ISpectrumPeak>, IEquivalent<ISpectrumPeak>
{
    public static ISpectrumPeak[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ISpectrumPeak.class;

    /**
     * MZ - mass charge ration
     * @return  as above
     */
    public double getMassChargeRatio();

    /**
      * the intensity or weight of the peak - scale is implementation specific
      * @return  as above
      */
     public float getPeak();

    /**
     * return as an immutble peak
     * @return  as above
     */
    public ISpectrumPeak asImmutable();


    /**
     * return true if the spectrum is immutable
     * @return  as above
     */
    public boolean isImmutable();



}
