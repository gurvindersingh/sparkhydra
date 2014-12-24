package org.systemsbiology.xtandem;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.sax.*;

/**
 * org.systemsbiology.xtandem.ScanPrecursorMz
 * User: steven
 * Date: Jan 3, 2011
 */
public class ScanPrecursorMz implements IScanPrecursorMZ {
    public static final IScanPrecursorMZ[] EMPTY_ARRAY = {};

    public static final String TAG = "precursorMz";

    public static double chargeRatioFromMass(double mass,int charge) {
        double protonMass = XTandemUtilities.getProtonMass();
        return ((mass - protonMass) / charge) +  protonMass;
    }

    private final double m_PrecursorIntensity;
    private final int m_PrecursorCharge;
    private final double m_MassChargeRatio;
    private final FragmentationMethod m_Method;


    public ScanPrecursorMz(IScanPrecursorMZ from, int pPrecursorCharge ) {
        m_PrecursorIntensity = from.getPrecursorIntensity();
        m_PrecursorCharge = pPrecursorCharge;
        m_MassChargeRatio = from.getMassChargeRatio();
        m_Method = from.getMethod();
    }

    public ScanPrecursorMz(final double pPrecursorIntensity, int pPrecursorCharge,
                           final double mz, FragmentationMethod method) {
        m_PrecursorIntensity = pPrecursorIntensity;
        m_PrecursorCharge = pPrecursorCharge;
        m_MassChargeRatio = mz;
        m_Method = method;
    }

    /**
     * infered from the filter line if false we are given
     *
     * @return as above
     */
    @Override
    public boolean isPresumptive() {
        return false; // we are always so
    }

    @Override
    public double getPrecursorIntensity() {
        return m_PrecursorIntensity;
    }

    @Override
    public int getPrecursorCharge() {
        return m_PrecursorCharge;
    }

    @Override
    public double getMassChargeRatio() {
        return m_MassChargeRatio;
    }

    @Override
    public FragmentationMethod getMethod() {
        return m_Method;
    }

    @Override
    public double getPrecursorMass() {
        int precursorCharge = getPrecursorCharge();
        if (precursorCharge == 0)
            precursorCharge = 1;     // todo fix
        return getPrecursorMass(precursorCharge);
    }

    @Override
    public double getPrecursorMass(int charge) {
        double chargeRatio = getMassChargeRatio();
        double protonMass = XTandemUtilities.getProtonMass();
        final double ret = ((chargeRatio - protonMass) * charge) + protonMass;
        return ret;
        // return getMassChargeRatio() * getPrecursorCharge();
    }


    /**
     * return true if a mass such as that of a throretical peak is
     * within the range to scpre
     *
     * @param mass positive testMass
     * @return as above
     */
    @Override
    public boolean isMassWithinRange(double mass,int charge,IScoringAlgorithm alg) {
          if (m_PrecursorCharge == 0) {
            // try charge 2
            double test1 = (getMassChargeRatio() - XTandemUtilities.getProtonMass()) * 2 + XTandemUtilities.getProtonMass();
            if (alg.isWithinLimits(test1, mass,0))
                return true;
            // try charge 3
              double test2 = (getMassChargeRatio() - XTandemUtilities.getProtonMass()) * 3 + XTandemUtilities.getProtonMass();
              if (alg.isWithinLimits(test2,  mass,0))
                  return true;
            // try charge 1 -NOTE THIS IS NEW
              double test3 =  getMassChargeRatio() ;
              if (alg.isWithinLimits(test3, mass,0))
                  return true;
            return false; // give up
        }
        else {
            double test = getPrecursorMass();
            boolean withinLimits = alg.isWithinLimits(test, mass,m_PrecursorCharge);
            if (withinLimits)
                return true;
            return false; // give up
        }
    }

    /*
    <precursorMz precursorIntensity="69490.9" activationMethod="CID" >1096.63</precursorMz>
     */

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    @Override
    public void serializeAsString(IXMLAppender adder) {
        String tag = TAG;
        adder.openTag(tag);
        adder.appendAttribute("precursorIntensity",
                XTandemUtilities.formatDouble(getPrecursorIntensity(), 1));
        adder.appendAttribute("precursorCharge",Integer.toString(getPrecursorCharge()));
        FragmentationMethod method = getMethod();
        if(method == null)
            method = FragmentationMethod.CID;
        adder.appendAttribute("activationMethod", method);
        adder.endTag();
        adder.appendText(XTandemUtilities.formatDouble(getMassChargeRatio(), 3));
        adder.closeTag(tag);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ScanPrecursorMz that = (ScanPrecursorMz) o;

        if (Math.abs(that.m_MassChargeRatio - m_MassChargeRatio) > 0.001)
            return false;
        if (m_PrecursorCharge != that.m_PrecursorCharge) return false;
        if (Math.abs(that.m_PrecursorIntensity - m_PrecursorIntensity) > 0.001)
             return false;
          if (m_Method != that.m_Method) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = (long)(m_PrecursorIntensity * 1000);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + m_PrecursorCharge;
        temp = (long)(m_MassChargeRatio * 1000);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + m_Method.hashCode();
        return result;
    }

}
