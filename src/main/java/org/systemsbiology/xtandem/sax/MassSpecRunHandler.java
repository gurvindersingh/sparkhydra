package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.SaxMzXMLHandler
 *
 * @author Steve Lewis
 * @date Dec 23, 2010
 */
public class MassSpecRunHandler extends AbstractXTandemElementSaxHandler<MassSpecRun> {
    public static MassSpecRunHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MassSpecRunHandler.class;


    private int m_ScanCount;
    private String m_StartTime;
    private String m_EndTime;
    private Map<String, MassSpecInstrument> m_Instrument = new HashMap<String, MassSpecInstrument>();
    private Map<String, RawPeptideScan> m_Scan = new HashMap<String, RawPeptideScan>();


    public MassSpecRunHandler(IElementHandler parent) {
        super("msRun", parent);
    }


    public void addScan(RawPeptideScan added) {
        // ignore too many scans to keep from blowing memory
        if(m_Scan.size() > XTandemUtilities.getMaxHandledScans())
            return;
        m_Scan.put(added.getId(), added);

        /**
         if(m_Scan.size() % 1000 == 0)
         XTandemUtilities.outputText(".");
         if(m_Scan.size() % 50000 == 0)
         XTandemUtilities.outputLine( );
         */
        // for debugging we may restrict the number of scans
        if (m_Scan.size() > XTandemMain.getMaxScans())
            throw new MaximumDataReachedException();
    }


    public RawPeptideScan[] getScans() {
        return m_Scan.values().toArray(new RawPeptideScan[0]);
    }

    public RawPeptideScan getScan(Integer key) {
        return m_Scan.get(key);
    }


    public void addInstrument(MassSpecInstrument added) {
        m_Instrument.put(added.getId(), added);
    }


    public MassSpecInstrument[] getInstruments() {
        return m_Instrument.values().toArray(new MassSpecInstrument[0]);
    }

    public MassSpecInstrument getInstrument(String key) {
        return m_Instrument.get(key);
    }


    public int getScanCount() {
        return m_ScanCount;
    }

    public void setScanCount(final int pScanCount) {
        m_ScanCount = pScanCount;
    }

    public String getStartTime() {
        return m_StartTime;
    }

    public void setStartTime(final String pStartTime) {
        m_StartTime = pStartTime;
    }

    public String getEndTime() {
        return m_EndTime;
    }

    public void setEndTime(final String pEndTime) {
        m_EndTime = pEndTime;
    }

    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {


        setScanCount(XTandemSaxUtilities.getRequiredIntegerAttribute("scanCount", attr));

        setStartTime(attr.getValue("startTime"));
        setEndTime(attr.getValue("endTime"));

        return;
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

        if ("dataProcessing".equals(qName)) {
            return;
        }
        else if ("software".equals(qName)) {
            return;
        }
        else if ("parentFile".equals(qName)) {
            return;  // ignore
        }
        else if ("scan".equals(qName)) {
            OuterMzXMLScanHandler handler = new OuterMzXMLScanHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, qName, attributes);
            return;
        }
        if ("msInstrument".equals(qName)) {
            MzXMLInstrumentHandler handler = new MzXMLInstrumentHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, qName, attributes);
            return;
        }

        super.startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        if ("msInstrument".equals(el)) {
            MzXMLInstrumentHandler handler = (MzXMLInstrumentHandler) getHandler().popCurrentHandler();
            addInstrument(handler.getElementObject());
            return;
        }
        else if ("scan".equals(el)) {
            ISaxHandler handler1 = getHandler().popCurrentHandler();
            if (handler1 instanceof MzXMLScanHandler) {
                MzXMLScanHandler handler = (MzXMLScanHandler) handler1;
                addScan(handler.getElementObject());
            }
            if (handler1 instanceof OuterMzXMLScanHandler) {
                OuterMzXMLScanHandler handler = (OuterMzXMLScanHandler) handler1;
                RawPeptideScan[] scans = handler.getElementObject();
                for (int i = 0; i < scans.length; i++) {
                    RawPeptideScan scan = scans[i];
                    addScan(scan);

                }
            }
            else {
                throw new BadEndTagException("End scan tag is not Scan tag");
            }
            return;
        }
        else if ("dataProcessing".equals(el)) {
            return;
        }
        else if ("software".equals(el)) {
            return;
        }
        else if ("parentFile".equals(el)) {
            return;    // ignore
        }
        super.endElement(elx, localName, el);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public static class BadEndTagException extends RuntimeException {
        /**
         * Constructs a new runtime exception with the specified detail message.
         * The cause is not initialized, and may subsequently be initialized by a
         * call to {@link #initCause}.
         *
         * @param message the detail message. The detail message is saved for
         *                later retrieval by the {@link #getMessage()} method.
         */
        public BadEndTagException(final String message) {
            super(message);
        }
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        // finish building the object
        MassSpecRun item = new MassSpecRun();
        item.setScanCount(getScanCount());
        item.setStartTime(getStartTime());
        item.setEndTime(getEndTime());
        for (MassSpecInstrument ins : m_Instrument.values())
            item.addInstrument(ins);
        for (RawPeptideScan ins : m_Scan.values())
            item.addScan(ins);

        setElementObject(item);

    }
}
