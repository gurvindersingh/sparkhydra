package org.systemsbiology.xtandem.ionization;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

/**
 * org.systemsbiology.xtandem.ionization.TheoreticalPeak
 *    Immutable class representing a single peak in a theoretical spectrum
 * @author Steve Lewis
 * @date Jan 17, 2011
 */
public class TheoreticalPeak extends SpectrumPeak implements ITheoreticalPeak,Comparable<ISpectrumPeak>
{
    public static TheoreticalPeak[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = TheoreticalPeak.class;

    private final IPolypeptide m_Peptide;
    private final IonType m_Type;

    public TheoreticalPeak(double pMassChargeRatio, float pPeak,IPolypeptide peptide, IonType pType)
    {
        super(pMassChargeRatio, pPeak);
        m_Type = pType;
        m_Peptide = peptide;
    }

    public TheoreticalPeak(ISpectrumPeak in,IPolypeptide peptide, IonType pType)
     {
         super(in);
         m_Type = pType;
         m_Peptide = peptide;
      }

    public TheoreticalPeak(ITheoreticalPeak in )
     {
         super(in);
         m_Type = in.getType();
         m_Peptide = in.getPeptide();
      }

    /**
     * we always know the generating peptide of a theoretical peak
     *
     * @return !null peptide
     */
    @Override
    public IPolypeptide getPeptide() {
        return m_Peptide;
    }

    /**
     * return type A,B,C,X,Y,Z
     *
     * @return
     */
    @Override
    public IonType getType()
    {
         return m_Type;
    }

    @Override
    public String toString()
    {
        return getPeptide() + ":" + getType() + " " + super.toString();

    }


    @Override
    public int compareTo(final ISpectrumPeak o) {
        if(o == this) return 0;
        int ret = Double.compare(getMassChargeRatio(),o.getMassChargeRatio());
        if(ret != 0)
            return ret;
        ret = Double.compare(getPeak(),o.getPeak());
        if(ret != 0)
            return ret;
        return 0;
    }
}
