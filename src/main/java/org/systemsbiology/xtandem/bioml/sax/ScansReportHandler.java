package org.systemsbiology.xtandem.bioml.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.*;
import org.systemsbiology.xtandem.sax.*;
import org.systemsbiology.xtandem.scoring.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.bioml.sax.BiomlSaxHandler
 * User: steven
 * Date: 8/22/11
 */
public class ScansReportHandler extends AbstractXTandemElementSaxHandler<XTandemScoringReport> implements ITopLevelSaxHandler {
    public static final ScansReportHandler[] EMPTY_ARRAY = {};


    public ScansReportHandler(final DelegatingSaxHandler pParent) {
        super("scans", pParent);
        XTandemScoringReport report = new XTandemScoringReport();
        setElementObject(report);
    }


    @Override
    public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.handleAttributes(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if (ScoredScan.TAG.equals(qName)) {
             ScanScoreHandler handler = new ScanScoreHandler(this);
             getHandler().pushCurrentHandler(handler);
             handler.handleAttributes(uri, localName, qName, attributes);
             return;
         }
        if (MultiScorer.TAG.equals(qName)) {
               return;     // todo FIX when we need to deal with multiple scan types
         }

        super.startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        if (ScoredScan.TAG.equals(el)) {
            ISaxHandler ch = getHandler().popCurrentHandler();
            ScanScoreHandler handler = (ScanScoreHandler) ch;
            ScoredScan scan = handler.getElementObject();
            if (scan != null) {
                XTandemScoringReport report = getElementObject();
                report.addScan(scan);
            }
            return;
        }
         if (MultiScorer.TAG.equals(el)) {
               return;   // todo FIX when we need to deal with multiple scan types
         }
        super.endElement(elx, localName, el);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        // nothing more to do
    }

    public static void main(String[] args) {
        XTandemScoringReport report = XTandemUtilities.readScanScoring(args[0]);
        ScoredScan[] scans = report.getScans();
        for (int i = 0; i < scans.length; i++) {
            ScoredScan scan = scans[i];
            scan = null;
        }
    }
}
