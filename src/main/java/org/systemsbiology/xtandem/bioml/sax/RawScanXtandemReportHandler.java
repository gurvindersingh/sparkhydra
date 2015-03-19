package org.systemsbiology.xtandem.bioml.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.bioml.sax.BiomlSaxHandler
 * User: steven
 * Date: 8/22/11
 */
public class RawScanXtandemReportHandler extends AbstractXTandemElementSaxHandler<RawPeptideScan> {
    public static final RawScanXtandemReportHandler[] EMPTY_ARRAY = {};

    private String m_Id;
    private double m_Precursormass;
    private int m_Charge;
    private double[] m_XData;
    private double[] m_YData;
    private String[] m_StringData;


    public RawScanXtandemReportHandler(final IElementHandler pParent) {
        super("group", pParent);
    }

    @Override
    public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attr) throws SAXException {
//        setScanCount(XTandemSaxUtilities.getRequiredIntegerAttribute("scanCount", attr));
//
//          setStartTime(attr.getValue("startTime"));
//          setEndTime(attr.getValue("endTime"));

        super.handleAttributes(uri, localName, qName, attr);
    }

    public String getId() {
        return m_Id;
    }

    public void setId(final String pId) {
        m_Id = pId;
        setElementObject(new RawPeptideScan(m_Id,""));
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if ("group".equals(qName)) {
            handleSubgroup(uri, localName, qName, attributes);
        }
        if ("GAML:trace".equals(qName)) {

        }
        if ("GAML:values".equals(qName)) {
            clearIncludedText();
        }
        if ("GAML:attribute".equals(qName)) {
            AttributeHandler handler = new AttributeHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, qName, attributes);
            return;
        }
        if ("note".equals(qName)) {
            super.startElement(uri, localName, qName, attributes);
            return;
        }
        super.startElement(uri, localName, qName, attributes);
    }

    private void handleSubgroup(final String uri, final String localName, final String qName, final Attributes attr) throws SAXException {
        String type = attr.getValue("type");
        if ("support".equals(type)) {
            String label = attr.getValue("label");
            if ("fragment ion mass spectrum".equals(label)) {
                RawScanXtandemReportHandler handler = new RawScanXtandemReportHandler(this);
                getHandler().pushCurrentHandler(handler);
                handler.handleAttributes(uri, localName, qName, attr);
                return;

            }
            throw new UnsupportedOperationException("We only support the above case");

        }
    }

    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        if ("group".equals(el)) {
            finishProcessing();
            ISaxHandler handler = getHandler().popCurrentHandler();
            if (handler instanceof RawScanXtandemReportHandler) {
                RawScanXtandemReportHandler o = (RawScanXtandemReportHandler) handler;
                RawPeptideScan scan = o.getElementObject();
                setElementObject(scan);

            }
        }
        if ("GAML:trace".equals(el)) {
            return;
        }
        if ("GAML:values".equals(el)) {
            String data = getIncludedText().trim();
            data = data.replace("\n", " ");
            data = data.replace("  ", "");
            data = data.replace("  ", "");
            data = data.replace("  ", "");

            clearIncludedText();
            m_StringData = data.split(" ");
            return;
        }
        if ("GAML:Xdata".equals(el)) {
            setXData();
            return;
        }
        if ("GAML:Ydata".equals(el)) {
            setYData();
            return;
        }
        if ("GAML:attribute".equals(el)) {
            RawPeptideScan scan = getElementObject();
            AttributeHandler ah = (AttributeHandler) getHandler().popCurrentHandler();
            String type = ah.getType();
            if ("M+H".equals(type)) {
                m_Precursormass = Double.parseDouble(ah.getIncludedText());
                return;
            }
            if ("charge".equals(type)) {

                m_Charge = Integer.parseInt(ah.getIncludedText());
                return;
            }
            return;
        }
        if ("group".equals(el)) {
         //    getHandler().popCurrentHandler(); // needed since tags are nested
            BiomlScanReportHandler parent = (BiomlScanReportHandler)getParent();
            RawPeptideScan raw = getElementObject();
            parent.getElementObject().setRaw(raw);
            
            return;
        }

        super.endElement(elx, localName, el);
    }

    private void setXData() {
        m_XData = new double[m_StringData.length];
        for (int i = 0; i < m_StringData.length; i++) {
            String s = m_StringData[i];
            m_XData[i] = Double.parseDouble(s);

        }
        m_StringData = null;
    }

    private void setYData() {
        m_YData = new double[m_StringData.length];
        for (int i = 0; i < m_StringData.length; i++) {
            m_YData[i] = Double.parseDouble(m_StringData[i]);

        }
        m_StringData = null;
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        RawPeptideScan raw = getElementObject();
          double mz =   ScanPrecursorMz.chargeRatioFromMass( m_Precursormass , m_Charge);
        raw.setPrecursorMz(new ScanPrecursorMz(1,m_Charge, mz,FragmentationMethod.CID));
        double m2 = raw.getPrecursorMass();

        String Description = getNote("Description");
        if(Description != null) {
            String[] items = Description.split(" ");
            raw.setLabel(items[0]);
        }
        if(m_XData != null && m_YData != null) {
            ISpectrumPeak[] peaks = new ISpectrumPeak[m_XData.length];
            for (int i = 0; i < m_XData.length; i++) {
                double mass = m_XData[i];
                double peak = m_YData[i];
                peaks[i] = new SpectrumPeak(mass,(float)peak);

            }
            raw.setPeaks(peaks);
        }
        else {
            raw.setPeaks(ISpectrumPeak.EMPTY_ARRAY);
        }
    }
}
