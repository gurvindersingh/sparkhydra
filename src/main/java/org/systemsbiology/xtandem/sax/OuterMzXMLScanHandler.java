package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.MzXMLScanHandler
 * handle data from a raw scan return a RawPeptideScan
 *
 * @author Steve Lewis
 * @date Dec 23, 2010
 */
public class OuterMzXMLScanHandler extends AbstractXTandemElementSaxHandler<RawPeptideScan[]> {
    public static OuterMzXMLScanHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = OuterMzXMLScanHandler.class;

    public static final String TAG = "scan";

    private String m_Id;
    private int m_MsLevel;
    private int m_ScanEvent;
    private ScanTypeEnum m_ScanType;
    private int m_PeaksCount;
    private ScanPolarity m_Polarity;
    private String m_RetentionTime;
    private FragmentationMethod m_ActivationMethod;
    private String m_FilterLine;
    private double m_LowMz;
    private double m_HighMz;
    private double m_BasePeakMz;
    private double m_BasePeakIntensity;
    private double m_TotIonCurrent;
    private double m_CompensationVoltage;
    private String m_InstrumentId;
    private ISpectrumPeak[] m_Peaks;
    private IScanPrecursorMZ m_PrecursorMz;

    // scans can be nested
    private final List<RawPeptideScan> m_EmbeddedScan = new ArrayList<RawPeptideScan>();


    public OuterMzXMLScanHandler(IElementHandler parent) {
        super(TAG, parent);
    }


    public String getId() {
        return m_Id;
    }

    public void setId(final String pId) {
        m_Id = pId;
    }

    public String getFilterLine() {
        return m_FilterLine;
    }

    public void setFilterLine(final String pFilterLine) {
        m_FilterLine = pFilterLine;
    }

    public int getScanEvent() {
        return m_ScanEvent;
    }

    public void setScanEvent(final int pScanEvent) {
        m_ScanEvent = pScanEvent;
    }

    public ScanTypeEnum getScanType() {
        return m_ScanType;
    }

    public void setScanType(final ScanTypeEnum pScanType) {
        m_ScanType = pScanType;
    }

    public int getPeaksCount() {
        return m_PeaksCount;
    }

    public void setPeaksCount(final int pPeaksCount) {
        m_PeaksCount = pPeaksCount;
    }

    public ScanPolarity getPolarity() {
        return m_Polarity;
    }

    public void setPolarity(final ScanPolarity pPolarity) {
        m_Polarity = pPolarity;
    }

    public String getRetentionTime() {
        return m_RetentionTime;
    }

    public void setRetentionTime(final String pRetentionTime) {
        m_RetentionTime = pRetentionTime;
    }

    public FragmentationMethod getActivationMethod() {
        return m_ActivationMethod;
    }

    public void setActivationMethod(final FragmentationMethod pActivationMethod) {
        m_ActivationMethod = pActivationMethod;
    }

    public double getLowMz() {
        return m_LowMz;
    }

    public void setLowMz(final double pLowMz) {
        m_LowMz = pLowMz;
    }

    public double getHighMz() {
        return m_HighMz;
    }

    public void setHighMz(final double pHighMz) {
        m_HighMz = pHighMz;
    }

    public double getBasePeakMz() {
        return m_BasePeakMz;
    }

    public void setBasePeakMz(final double pBasePeakMz) {
        m_BasePeakMz = pBasePeakMz;
    }

    public double getBasePeakIntensity() {
        return m_BasePeakIntensity;
    }

    public void setBasePeakIntensity(final double pBasePeakIntensity) {
        m_BasePeakIntensity = pBasePeakIntensity;
    }

    public double getTotIonCurrent() {
        return m_TotIonCurrent;
    }

    public void setTotIonCurrent(final double pTotIonCurrent) {
        m_TotIonCurrent = pTotIonCurrent;
    }

    public String getInstrumentId() {
        return m_InstrumentId;
    }

    public void setInstrumentId(final String pInstrumentId) {
        m_InstrumentId = pInstrumentId;
    }

    public int getMsLevel() {
        return m_MsLevel;
    }

    public void setMsLevel(final int pMsLevel) {
        m_MsLevel = pMsLevel;
    }

    public double getCompensationVoltage() {
        return m_CompensationVoltage;
    }

    public void setCompensationVoltage(final double pCompensationVoltage) {
        m_CompensationVoltage = pCompensationVoltage;
    }


    public void addEmbeddedScan(RawPeptideScan added) {
        m_EmbeddedScan.add(added);
    }


    public void removeEmbeddedScan(RawPeptideScan removed) {
        m_EmbeddedScan.remove(removed);
    }

    public RawPeptideScan[] getEmbeddedScans() {
        return m_EmbeddedScan.toArray(new RawPeptideScan[0]);
    }

    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {
        setId(XTandemSaxUtilities.getRequiredAttribute("num", attr)); // num="1"
        setMsLevel(XTandemSaxUtilities.getRequiredIntegerAttribute("msLevel", attr)); // msLevel="2"
        setScanEvent(
                XTandemSaxUtilities.getIntegerAttribute("scanEvent", attr, 0)); // scanEvent="0"
        setScanType(XTandemSaxUtilities.getEnumAttribute("scanType", attr, ScanTypeEnum.class,
                ScanTypeEnum.Full)); // scanType="FULL"

        setFilterLine(attr.getValue("filterLine"));

        setPeaksCount(XTandemSaxUtilities.getRequiredIntegerAttribute("peaksCount",
                attr)); //  peaksCount="4211"
        setPolarity(XTandemSaxUtilities.getEnumAttribute("polarity", attr,
                ScanPolarity.STRING_MAPPING)); //         polarity="+"
        setRetentionTime(attr.getValue("retentionTime")); //   retentionTime="PT0.0959S"
        setActivationMethod(XTandemSaxUtilities.getEnumAttribute("activationMethod", attr,
                FragmentationMethod.class,
                FragmentationMethod.ETD)); //          activationMethod="ETD"
        setLowMz((XTandemSaxUtilities.getDoubleAttribute("lowMz", attr,
                0)));  // lowMz="115.00006259729795"
        setHighMz((XTandemSaxUtilities.getDoubleAttribute("highMz", attr,
                0)));  //  highMz="2008.4618859586642"
        setBasePeakMz((XTandemSaxUtilities.getDoubleAttribute("basePeakMz", attr,
                0)));  //   basePeakMz="649.3494873046875"
        setBasePeakIntensity((XTandemSaxUtilities.getDoubleAttribute("basePeakIntensity", attr,
                0)));  //  basePeakIntensity="14279165"
        setTotIonCurrent((XTandemSaxUtilities.getDoubleAttribute("totIonCurrent", attr,
                0)));  //  totIonCurrent="80153336"
        setCompensationVoltage((XTandemSaxUtilities.getDoubleAttribute("compensationVoltage", attr,
                0)));  //  totIonCurrent="80153336"

        setInstrumentId(attr.getValue("msInstrumentID")); // msInstrumentID="IC1">

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
     * @param el         The qualified name (with prefix), or the
     *                   empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If
     *                   there are no attributes, it shall be an empty
     *                   Attributes object.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    @Override
    public void startElement(String uri, String localName, String el, Attributes attributes)
            throws SAXException {
        if ("precursorMz".equals(el)) {
            MzXMLPrecursorMzHandler handler = new MzXMLPrecursorMzHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, el, attributes);
            return;
        }
        if ("peaks".equals(el)) {
            MzXMLPeaksHandler handler = new MzXMLPeaksHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, el, attributes);
            return;
        }
        if ("scan".equals(el)) {
            MzXMLScanHandler handler = new MzXMLScanHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, el, attributes);
            return;
        }
        super.startElement(uri, localName, el, attributes);

    }

    @Override
    public void endElement(String elx, String localName, String el) throws SAXException {
        if ("peaks".equals(el)) {
            MzXMLPeaksHandler handler = (MzXMLPeaksHandler) getHandler().popCurrentHandler();
            setPeaks(handler.getElementObject());
            return;
        }
        else if ("precursorMz".equals(el)) {
            MzXMLPrecursorMzHandler handler = (MzXMLPrecursorMzHandler) getHandler().popCurrentHandler();
            setPrecursorMz(handler.getElementObject());
            return;
        }
        else if ("scan".equals(el)) {
            ISaxHandler current = getHandler().getCurrentHandler();
            if (current instanceof MzXMLScanHandler) {
                MzXMLScanHandler handler = (MzXMLScanHandler) getHandler().popCurrentHandler();
                addEmbeddedScan(handler.getElementObject());
                return;

            }
        }
        super.endElement(elx, localName, el);
    }

    public ISpectrumPeak[] getPeaks() {
        return m_Peaks;
    }

    public void setPeaks(final ISpectrumPeak[] pPeaks) {
        m_Peaks = pPeaks;
    }

    public IScanPrecursorMZ getPrecursorMz() {
        return m_PrecursorMz;
    }

    public void setPrecursorMz(final IScanPrecursorMZ pPrecursorMz) {
        m_PrecursorMz = pPrecursorMz;
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        if(getMsLevel() > 1)  {
            RawPeptideScan scan = new RawPeptideScan(getId(), null);  // todo fix
             scan.setActivationMethod(getActivationMethod());
             scan.setBasePeakIntensity(getBasePeakIntensity());
             scan.setBasePeakMz(getBasePeakMz());
             scan.setCompensationVoltage(getCompensationVoltage());
             scan.setHighMz(getHighMz());
             scan.setInstrumentId(getInstrumentId());
             scan.setLowMz(getLowMz());
             scan.setMsLevel(getMsLevel());
             scan.setPeaks(getPeaks());
             scan.setPolarity(getPolarity());
             scan.setPrecursorMz(getPrecursorMz());
             scan.setRetentionTime(getRetentionTime());
             scan.setScanEvent(getScanEvent());
             scan.setScanType(getScanType());
             scan.setTotIonCurrent(getTotIonCurrent());
             scan.setFilterLine(getFilterLine());
             RawPeptideScan[] data = { scan} ;
            setElementObject(data);
        }
        else {
            setElementObject(getEmbeddedScans());

        }


    }
}
