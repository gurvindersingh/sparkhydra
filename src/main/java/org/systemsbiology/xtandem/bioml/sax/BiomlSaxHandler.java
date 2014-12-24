package org.systemsbiology.xtandem.bioml.sax;

import com.lordjoe.lib.xml.*;
import org.systemsbiology.sax.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.*;
import org.systemsbiology.xtandem.scoring.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.bioml.sax.BiomlSaxHandler
 * User: steven
 * Date: 8/22/11
 */
public class BiomlSaxHandler extends AbstractXTandemElementSaxHandler<XTandemScoringReport> implements ITopLevelSaxHandler {
    public static final BiomlSaxHandler[] EMPTY_ARRAY = {};

    private int m_NumberGroups;

    public BiomlSaxHandler(final DelegatingSaxHandler pParent) {
        super("bioml", pParent);
        XTandemScoringReport report = new XTandemScoringReport();
        setElementObject(report);
    }

    /**
     * Receive notification of the beginning of the document.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the beginning
     * of a document (such as allocating the root node of a tree or
     * creating an output file).</p>
     *
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.ContentHandler#startDocument
     */
    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        m_NumberGroups= 0;
    }

    public int getNumberGroups() {
        return m_NumberGroups;
    }

    public void incrementNumberGroups()
    {
         m_NumberGroups++;
        if(m_NumberGroups %100 == 0)
            XMLUtilities.outputText(".");
        if(m_NumberGroups % 8000 == 0)
            XMLUtilities.outputLine(".");
    }

    @Override
    public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        super.handleAttributes(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if ("group".equals(qName)) {
            incrementNumberGroups();
            BiomlScanReportHandler handler = new BiomlScanReportHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, qName, attributes);
            return;
        }
        super.startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        if ("group".equals(el)) {
            ISaxHandler ch = getHandler().popCurrentHandler();
            BiomlScanReportHandler handler = (BiomlScanReportHandler) ch;
            ScoredScan scan = handler.getElementObject();
            if (scan != null && scan.getId() != null) {
                XTandemScoringReport report = getElementObject();
                report.addScan(scan);
            }
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
        // nothing more to do
    }
}
