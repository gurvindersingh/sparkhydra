package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.testing.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.sax.TheoreticalSpectrumHandler
 * User: steven
 * Date: 6/22/11
 */
public class TheoreticalSpectrumHandler extends AbstractXTandemElementSaxHandler<ITheoreticalSpectrum> implements ITopLevelSaxHandler {
    public static final TheoreticalSpectrumHandler[] EMPTY_ARRAY = {};


    public static final String TAG = "theoretical_ions";

    private final MutableScoringSpectrum m_Spectrum;
    private   IonType m_Type;

    public TheoreticalSpectrumHandler(IElementHandler parent, IScanScoring parentReport) {
        super(TAG, parent);
        DotProductScoringHandler realParent = (DotProductScoringHandler) parent;
        m_Spectrum = new MutableScoringSpectrum(realParent.getCharge(),null,ITheoreticalPeak.EMPTY_ARRAY);

        setElementObject(m_Spectrum);
     }


    public IonType getType() {
        return m_Type;
    }

    public void setType(final IonType pType) {
        m_Type = pType;
    }


    @Override
    public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        String value;
          super.handleAttributes(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if ("ion".equals(qName)) {
              double mz = Double.parseDouble(attributes.getValue("mz"));
            ITheoreticalPeak   pk = new TheoreticalPeak(mz,1,null,null);
             m_Spectrum.addPeak(pk);
            return;
        }
        if ("scores".equals(qName)) {
            return;
        }
        super.startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }


    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {

        if ("match_at_mass".equals(el)) {     //ignore for now
            return;
        }
        if ("scores".equals(el)) {     //ignore for now
             return;
         }
        if ("ion".equals(el)) {     //ignore for now
             return;
         }

        super.endElement(elx, localName, el);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {

    }
}
