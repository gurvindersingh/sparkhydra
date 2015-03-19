package org.systemsbiology.xtandem.bioml.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.*;
import org.systemsbiology.xtandem.sax.*;
import org.systemsbiology.xtandem.scoring.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.bioml.sax.BiomlSaxHandler
 * User: steven
 * Date: 8/22/11
 */
public class BiomlScanReportHandler extends AbstractXTandemElementSaxHandler<ScoredScan> {
    public static final BiomlScanReportHandler[] EMPTY_ARRAY = {};

    private String m_Id;
    private Double m_MassChargeRatio;
    private Double m_Expect;
    private Double m_SumI;
    private int m_Charge;
    private String m_Label;
    private String m_File;

    public BiomlScanReportHandler(final IElementHandler pParent) {
        super("group", pParent);
        setElementObject(new ScoredScan());
    }

    public String getFile() {
        return m_File;
    }

    public void setFile(String file) {
        m_File = file;
    }

    public String getId() {
        return m_Id;
    }

    public void setId(final String pId) {
        m_Id = pId;
    }

    public Double getMassChargeRatio() {
        return m_MassChargeRatio;
    }

    public void setMassChargeRatio(final Double pMassChargeRatio) {
        m_MassChargeRatio = pMassChargeRatio;
    }

    public Double getExpect() {
        return m_Expect;
    }

    public void setExpect(final Double pExpect) {
        m_Expect = pExpect;
    }

    public Double getSumI() {
        return m_SumI;
    }

    public void setSumI(final Double pSumI) {
        m_SumI = pSumI;
    }

    public int getCharge() {
        return m_Charge;
    }

    public void setCharge(final int pCharge) {
        m_Charge = pCharge;
    }

    public String getLabel() {
        return m_Label;
    }

    public void setLabel(final String pLabel) {
        m_Label = pLabel;
    }

    @Override
    public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attr) throws SAXException {
        String idstr = attr.getValue("id");
        setId(idstr);
        String lblstr = attr.getValue("label");
        setLabel(lblstr);
        setCharge(XTandemSaxUtilities.getIntegerAttribute("z", attr, 0));
        setMassChargeRatio(XTandemSaxUtilities.getDoubleAttribute("mh", attr, 0));
        Double expect = XTandemSaxUtilities.getDoubleAttribute("expect", attr, 0);
        setExpect(expect);
        // todo there's more
        //        setScanCount(XTandemSaxUtilities.getRequiredIntegerAttribute("scanCount", attr));
//
//          setStartTime(attr.getValue("startTime"));
//          setEndTime(attr.getValue("endTime"));

        super.handleAttributes(uri, localName, qName, attr);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if ("group".equals(qName)) {
            handleSubgroup(uri, localName, qName, attributes);
            return;
        }
        if ("file".equals(qName)) {
            setFile(attributes.getValue("URL"));
            return;
        }
        if ("protein".equals(qName)) {
            ProteinXtandemReportHandler handler = new ProteinXtandemReportHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, qName, attributes);
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
                handler.setId(getId());
                getHandler().pushCurrentHandler(handler);
                handler.handleAttributes(uri, localName, qName, attr);
                return;

            }
            if ("supporting data".equals(label)) {
                NullElementHandler handler = new NullElementHandler("group", this);
                getHandler().pushCurrentHandler(handler);
                return;

            }
            throw new UnsupportedOperationException("support label " + label + " not handled");

        }
    }

    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        if ("protein".equals(el)) {
            ProteinXtandemReportHandler handler = (ProteinXtandemReportHandler) getHandler().popCurrentHandler();
            XTandemReportProtein prot = handler.getElementObject();
            IMeasuredSpectrum measured = null;
            ScoredScan scan = getElementObject();
            if (scan != null)
                measured = scan.getRaw();

            ISpectralMatch match = prot.asSpectralMatch(scan.getRaw());
            if (match != null)
                scan.addSpectralMatch(match);
            Map<String, String> notes = handler.getNotes();
            for (String s : notes.keySet()) {
                addNote(s,notes.get(s));
            }
            return;
            //      return;
        }
        if ("group".equals(el)) {
            ISaxHandler iSaxHandler = getHandler().popCurrentHandler();
            if (iSaxHandler instanceof ProteinXtandemReportHandler) {
                ProteinXtandemReportHandler handler = (ProteinXtandemReportHandler) iSaxHandler;
                XTandemReportProtein prot = handler.getElementObject();
                IMeasuredSpectrum measured = null;
                ScoredScan scan = getElementObject();
                Double expect = getExpect();
                scan.setExpectedValue(expect);
                if (scan != null)
                    measured = scan.getRaw();

                Map<String, String> notes = handler.getNotes();
                  for (String s : notes.keySet()) {
                      addNote(s,notes.get(s));
                  }

                ISpectralMatch match = prot.asSpectralMatch(scan.getRaw());
                if (match != null)
                    scan.addSpectralMatch(match);
                return;
            }
            if (iSaxHandler instanceof BiomlScanReportHandler) {
                getHandler().pushCurrentHandler(iSaxHandler); // let regular code repope #$#%#@$%# nested tags
//                  BiomlScanReportHandler handler = (BiomlScanReportHandler) iSaxHandler;  // that is us
//
//                ScoredScan prot = handler.getElementObject();
//                    IMeasuredSpectrum measured = null;
//                  ScoredScan scan = getElementObject();
//                  if (scan != null)
//                      measured = scan.getRaw();
//
//                  return;
                // normal parent handler will handle this
            }
            //                  return;
        }
        if ("file".equals(el)) {
            return;
        }
        // this case is too common to debug through
        if ("note".equals(el)) {
            super.endElement(elx, localName, el);
            return;
        }
        // better be the real end element for our element
        super.endElement(elx, localName, el);
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        ScoredScan scan = getElementObject();
        Double expect = getExpect();
        scan.setExpectedValue(expect);
    }
}
