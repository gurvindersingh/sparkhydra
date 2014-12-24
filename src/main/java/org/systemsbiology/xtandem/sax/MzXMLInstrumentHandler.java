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
public class MzXMLInstrumentHandler extends AbstractXTandemElementSaxHandler<MassSpecInstrument> {
    public static MzXMLInstrumentHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MzXMLInstrumentHandler.class;

    private String m_Id;
    private String m_Manufacturer;
    private String m_Model;
    private String m_MassAnalyzer;
    private String m_Ionization;
    private String m_Detector;
    private MassSpecSoftware m_Software;

    public MzXMLInstrumentHandler(IElementHandler parent) {
        super("msInstrument", parent);
    }


    public String getId() {
        return m_Id;
    }

    public void setId(final String pId) {
        m_Id = pId;
    }

    public String getManufacturer() {
        return m_Manufacturer;
    }

    public void setManufacturer(final String pManufacturer) {
        m_Manufacturer = pManufacturer;
    }

    public String getModel() {
        return m_Model;
    }

    public void setModel(final String pModel) {
        m_Model = pModel;
    }

    public String getMassAnalyzer() {
        return m_MassAnalyzer;
    }

    public void setMassAnalyzer(final String pMassAnalyzer) {
        m_MassAnalyzer = pMassAnalyzer;
    }

    public String getIonization() {
        return m_Ionization;
    }

    public void setIonization(final String pIonization) {
        m_Ionization = pIonization;
    }

    public String getDetector() {
        return m_Detector;
    }

    public void setDetector(final String pDetector) {
        m_Detector = pDetector;
    }

    public MassSpecSoftware getSoftware() {
        return m_Software;
    }

    public void setSoftware(final MassSpecSoftware pSoftware) {
        m_Software = pSoftware;
    }

    /**
     * Receive notification of the start of an element.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the start of
     * each element (such as allocating a new tree node or writing
     * output to a file).</p>
     *
     * @param uri        The Namespace URI, or the empty string if the
     *                   element has no Namespace URI or if Namespace
     *                   processing is not being performed.
     * @param localName  The local name (without prefix), or the
     *                   empty string if Namespace processing is not being
     *                   performed.
     * @param qName      The qualified name (with prefix), or the
     *                   empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If
     *                   there are no attributes, it shall be an empty
     *                   Attributes object.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {

        if ("msManufacturer".equals(localName)) {
            setManufacturer(attributes.getValue("value"));
            return;
        }
        if ("msModel".equals(localName)) {
            setModel(attributes.getValue("value"));
            return;
        }
        if ("msIonisation".equals(localName)) {
            setIonization(attributes.getValue("value"));
            return;
        }
        if ("msMassAnalyzer".equals(localName)) {
            setMassAnalyzer(attributes.getValue("value"));
            return;
        }
        if ("msDetector".equals(localName)) {
            setDetector(attributes.getValue("value"));
            return;
        }
        if ("software".equals(localName)) {
            MassSpecSoftware sw = new MassSpecSoftware(attributes.getValue("type"),
                    attributes.getValue("name"),
                    attributes.getValue("version"));
            setSoftware(sw);
            return;
        }
        super.startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        if ("msManufacturer".equals(el)) {
              return;
        }
        if ("msModel".equals(el)) {
              return;
        }
        if ("msIonisation".equals(el)) {
             return;
        }
        if ("msMassAnalyzer".equals(el)) {
             return;
        }
        if ("msDetector".equals(el)) {
             return;
        }
        if ("software".equals(el)) {
             return;
        }
        super.endElement(elx, localName, el);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {
        String value;

        value = attr.getValue("id");
        if (value != null)
            setId(value);

        return;
    }


    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        MassSpecInstrument ms = new MassSpecInstrument(getId());
        ms.setManufacturer(getManufacturer());
        ms.setDetector(getDetector());
        ms.setIonisation(getIonization());
        ms.setMassAnalyzer(getMassAnalyzer());
        ms.setModel(getModel());
        ms.setSoftware(getSoftware());
        setElementObject(ms);


    }
}
