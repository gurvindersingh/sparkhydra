package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.systemsbiology.xtandem.scoring.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.SaxMzXMLHandler
 *
 * @author Steve Lewis
 * @date Dec 23, 2010
 */
public class ScanScoreHandler extends AbstractXTandemElementSaxHandler<ScoredScan> implements IMainDataHolder {
    public static ScanScoreHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ScanScoreHandler.class;

    public static final String TAG = ScoredScan.TAG;

    private final IMainData m_MainData;
    private String m_Id;
    private String m_Version;
    private String m_Algorithm;
    private int m_Charge;
    private double m_ExpectedValue;
    private RawPeptideScan m_RawScan;
    private String m_PackedHyperScores;
    private IMeasuredSpectrum m_ConditionedScan;
    private IMeasuredSpectrum m_NormalizedRawScan;
    private final List<ISpectralMatch> m_Matches = new ArrayList<ISpectralMatch>();
    private boolean m_InNormalizedRawScan;
    private boolean m_InConditionedScan;
    private int m_NumberScoredPeptides;


    public ScanScoreHandler(IMainData main, DelegatingSaxHandler parent) {
        super(TAG, parent);
        m_MainData = main;
    }


    public ScanScoreHandler(IElementHandler parent) {
        super(TAG, parent);
        m_MainData = null;  // todo  fix
    }

    public String getPackedHyperScores() {
        return m_PackedHyperScores;
    }

    public void setPackedHyperScores(final String pPackedHyperScores) {
        m_PackedHyperScores = pPackedHyperScores;
    }

    public int getCharge() {
        return m_Charge;
    }

    public void setCharge(final int pCharge) {
        m_Charge = pCharge;
    }

    public String getAlgorithm() {
        return m_Algorithm;
    }

    public void setAlgorithm(final String pAlgorithm) {
        m_Algorithm = pAlgorithm;
    }

    public String getVersion() {
        return m_Version;
    }

    public void setVersion(final String pVersion) {
        m_Version = pVersion;
    }

    public int getNumberScoredPeptides() {
        return m_NumberScoredPeptides;
    }

    public void setNumberScoredPeptides(final int pNumberScoredPeptides) {
        m_NumberScoredPeptides = pNumberScoredPeptides;
    }

    public IMeasuredSpectrum getConditionedScan() {
        return m_ConditionedScan;
    }

    public void setConditionedScan(final IMeasuredSpectrum pConditionedScan) {
        m_ConditionedScan = pConditionedScan;
    }

    public IMeasuredSpectrum getNormalizedRawScan() {
        return m_NormalizedRawScan;
    }

    public void setNormalizedRawScan(final IMeasuredSpectrum pNormalizedRawScan) {
        m_NormalizedRawScan = pNormalizedRawScan;
    }

    protected boolean isInNormalizedRawScan() {
        return m_InNormalizedRawScan;
    }

    protected void setInNormalizedRawScan(final boolean pInNormalizedRawScan) {
        m_InNormalizedRawScan = pInNormalizedRawScan;
    }

    protected boolean isInConditionedScan() {
        return m_InConditionedScan;
    }

    protected void setInConditionedScan(final boolean pInConditionedScan) {
        m_InConditionedScan = pInConditionedScan;
    }

    public void addMatch(ISpectralMatch added) {
        m_Matches.add(added);
    }


    public ISpectralMatch[] getMatchs() {
        return m_Matches.toArray(ISpectralMatch.EMPTY_ARRAY);
    }


    public String getId() {
        return m_Id;
    }

    public void setId(final String pId) {
        m_Id = pId;
    }

    public double getExpectedValue() {
        return m_ExpectedValue;
    }

    public void setExpectedValue(final double pExpectedValue) {
        m_ExpectedValue = pExpectedValue;
    }

    public IMainData getMainData() {
        return m_MainData;
    }

    public RawPeptideScan getRawScan() {
        return m_RawScan;
    }

    public void setRawScan(RawPeptideScan pRawScan) {
        m_RawScan = pRawScan;
    }

    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {
        setId(attr.getValue("id" )); // num="1"
        String version =  attr.getValue("version");
        if(version != null)
             setVersion(version);
        String algorithm =  attr.getValue("algorithm");
        if(algorithm != null)
             setAlgorithm(algorithm);
        setCharge(XTandemSaxUtilities.getRequiredIntegerAttribute("charge", attr)); // num="1"
        String expected = attr.getValue("expectedValue");
        if (expected != null) {
            double expectedValue = Double.parseDouble(expected);
            setExpectedValue(expectedValue);

        }
        String nScored = attr.getValue("numberScoredPeptides");
        if (nScored != null) {
            int numberScored = XTandemSaxUtilities.getRequiredIntegerAttribute("numberScoredPeptides", attr);
            setNumberScoredPeptides(numberScored);
        }

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
        if (TAG.equals(el)) {
            handleAttributes(uri, localName, el, attributes);
            return;
        }
        if ("match".equals(el)) {
            MatchScoreHandler handler = new MatchScoreHandler(getId(), this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, el, attributes);
            return;
        }
        if ("ConditionedScan".equals(el)) {
            setInConditionedScan(true);
            return;
        }
        if ("NormalizedRawScan".equals(el)) {
            setInNormalizedRawScan(true);
            return;
        }
        if ("HyperScoreStatistics".equals(el)) {
            return;    // handle on exit
        }

        if ("MeasuredSpectrum".equals(el)) {
            MeasuredSpectrumHandler handler = new MeasuredSpectrumHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, el, attributes);
            return;
        }
        if (RawPeptideScan.TAG.equals(el)) {
            MzXMLScanHandler handler = new MzXMLScanHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, el, attributes);
            return;
        }
        super.startElement(uri, localName, el, attributes);

    }

    @Override
    public void endElement(String elx, String localName, String el) throws SAXException {
        if (TAG.equals(el)) {
            finishProcessing();
            final IElementHandler parent = getParent();
            if (parent != null)
                parent.endElement(elx, localName, el);
            return;
        }

        if ("HyperScoreStatistics".equals(el)) {
            setPackedHyperScores(getIncludedText().trim());
            clearIncludedText();
            return;    // handle on exit
        }

        if ("MeasuredSpectrum".equals(el)) {
            MeasuredSpectrumHandler handler = (MeasuredSpectrumHandler) getHandler().popCurrentHandler();
            if (isInConditionedScan()) {
                setConditionedScan(handler.getElementObject());
                return;
            }
            if (isInNormalizedRawScan()) {
                setNormalizedRawScan(handler.getElementObject());
                return;
            }
            throw new IllegalStateException("no measured scan state");
        }
        if ("ConditionedScan".equals(el)) {
            setInConditionedScan(false);
            return;
        }
        if ("NormalizedRawScan".equals(el)) {
            setInNormalizedRawScan(false);
            return;
        }

        if ("match".equals(el)) {
            MatchScoreHandler handler = (MatchScoreHandler) getHandler().popCurrentHandler();
            SpectralMatch match = handler.getElementObject();
            addMatch(match);
            return;
        }
        if (RawPeptideScan.TAG.equals(el)) {
            MzXMLScanHandler handler = (MzXMLScanHandler) getHandler().popCurrentHandler();
            setRawScan(handler.getElementObject());
            return;
        }
        super.endElement(elx, localName, el);
    }


    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        RawPeptideScan raw = getRawScan();
        ScoredScan scan = new ScoredScan(raw);
        //       scan.setNumberScoredPeptides(getNumberScoredPeptides());
        scan.setVersion(getVersion());
        String algorithm = getAlgorithm();
        if(algorithm != null)
            scan.setAlgorithm(algorithm);
        scan.setConditionedScan(getConditionedScan());
         scan.setNormalizedRawScan(getNormalizedRawScan());
        for (ISpectralMatch match : m_Matches)
            scan.addSpectralMatch(match);
        double value = getExpectedValue();
        scan.setExpectedValue(getExpectedValue());
        String packedScores = getPackedHyperScores();
        if (packedScores != null) {
            HyperScoreStatistics hyperScores = scan.getHyperScores();
            hyperScores.setFromString(packedScores);
        }

        setElementObject(scan);
    }


}
