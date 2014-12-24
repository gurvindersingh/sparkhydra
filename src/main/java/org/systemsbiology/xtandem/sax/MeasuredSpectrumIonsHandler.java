package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.testing.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.sax.MeasuredSpectrumIonsHandler
 * User: steven
 * Date: 6/22/11
 */
public class MeasuredSpectrumIonsHandler extends AbstractXTandemElementSaxHandler<IMeasuredSpectrum> implements ITopLevelSaxHandler {
    public static final MeasuredSpectrumIonsHandler[] EMPTY_ARRAY = {};


    public static final String TAG = "measured_ions";

    private final MutableMeasuredSpectrum m_Spectrum;
    private IonType m_Type;

    public MeasuredSpectrumIonsHandler(IElementHandler parent, IScanScoring ss) {
        super(TAG, parent);
        DotProductScoringHandler realParent = (DotProductScoringHandler) parent;
        m_Spectrum = new MutableMeasuredSpectrum( );
        setElementObject(m_Spectrum);
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
            double score = Double.parseDouble(attributes.getValue("score"));
            ISpectrumPeak pk = new SpectrumPeak(mz, (float) score);
            m_Spectrum.addPeak(pk);
            return;
        }
        if ("scores".equals(qName)) {
            return;
        }
        if ("total_peaks".equals(qName)) {     //ignore for now
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
        if ("total_peaks".equals(el)) {     //ignore for now
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
