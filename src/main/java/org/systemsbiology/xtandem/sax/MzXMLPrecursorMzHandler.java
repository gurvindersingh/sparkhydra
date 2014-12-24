package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.sax.SaxMzXMLHandler
 *
 * @author Steve Lewis
 * @date Dec 23, 2010
 */
public class MzXMLPrecursorMzHandler extends AbstractXTandemElementSaxHandler<IScanPrecursorMZ> {
    public static MzXMLPrecursorMzHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MzXMLPrecursorMzHandler.class;

    private double m_PrecursorIntensity;
    private int m_PrecursorCharge;
    private FragmentationMethod m_ActivationMethod;

    public MzXMLPrecursorMzHandler(IElementHandler parent) {
        super("precursorMz",parent);
    }

    public double getPrecursorIntensity() {
        return m_PrecursorIntensity;
    }

    public void setPrecursorIntensity(final double pPrecursorIntensity) {
        m_PrecursorIntensity = pPrecursorIntensity;
    }

    public int getPrecursorCharge() {
        return m_PrecursorCharge;
    }

    public void setPrecursorCharge(final int pPrecursorCharge) {
        m_PrecursorCharge = pPrecursorCharge;
    }

    public FragmentationMethod getActivationMethod()
    {
        return m_ActivationMethod;
    }

    public void setActivationMethod(FragmentationMethod pActivationMethod)
    {
        m_ActivationMethod = pActivationMethod;
    }

    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {
        String value;

        value = attr.getValue("precursorIntensity");
         if (value != null)
              setPrecursorIntensity(Double.parseDouble(value) );

        value = attr.getValue("activationMethod");
         if (value != null && value.length() > 0 )
              setActivationMethod(FragmentationMethod.valueOf(value));

        value = attr.getValue("precursorCharge");
        if (value != null)
             setPrecursorCharge(Integer.parseInt(value));

          return;
    }





    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        // finish building the object
        double mhz = Double.parseDouble(getIncludedText());
         IScanPrecursorMZ precursorMz = new ScanPrecursorMz(getPrecursorIntensity(), getPrecursorCharge(), mhz,getActivationMethod());
         setElementObject(precursorMz);


    }
}
