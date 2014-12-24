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
public class MzXMLPeaksHandler extends AbstractXTandemElementSaxHandler<ISpectrumPeak[]> {
    public static MzXMLPeaksHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MzXMLPeaksHandler.class;

    public static final String TAG = "peaks";

    private MassResolution m_Precision = MassResolution.Bits32;
    private boolean m_CompressionUsed;
    private int m_CompressedLength;

    public MzXMLPeaksHandler(IElementHandler parent) {
        super(TAG, parent);
    }


    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {
        String value;

        setCompressionUsed(false);
        value = attr.getValue("compressionType");
        if ("zlib".equalsIgnoreCase(value))
            setCompressionUsed(true);

        value = attr.getValue("compressedLen");
        if (value != null)
            setCompressedLength(Integer.parseInt(value));

        value = attr.getValue("precision");
        if ("64".equals(value))
            setPrecision(MassResolution.Bits64);

        value = attr.getValue("network");
        if (value != null)
            if (!"network".equals(value))
                throw new UnsupportedOperationException("network is the only order we can handle");

        value = attr.getValue("pairOrder");
        if (value != null)
            if (!"m/z-int".equals(value))
                throw new UnsupportedOperationException("m/z-int is the only order we can handle");
        //noinspection UnnecessaryReturnStatement
        return;
    }

    public int getCompressedLength() {
        return m_CompressedLength;
    }

    public void setCompressedLength(final int pCompressedLength) {
        m_CompressedLength = pCompressedLength;
    }

    public MassResolution getPrecision() {
        return m_Precision;
    }

    public void setPrecision(final MassResolution pPrecision) {
        m_Precision = pPrecision;
    }

    public boolean isCompressionUsed() {
        return m_CompressionUsed;
    }

    public void setCompressionUsed(final boolean pCompressionUsed) {
        m_CompressionUsed = pCompressionUsed;
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        // finish building the object
        String includedText = getIncludedText();
        ISpectrumPeak[] spectrumPeaks;
        if (isCompressionUsed()) {
            int len = getCompressedLength();
            spectrumPeaks = XTandemUtilities.decodeCompressedPeaks(includedText, getPrecision());
        }
        else {
            spectrumPeaks = XTandemUtilities.decodePeaks(includedText, getPrecision());
        }
        setElementObject(spectrumPeaks);

    }

    @Override
    public void endElement(String elx, String localName, String el) throws SAXException {
        super.endElement(elx, localName, el);

    }
}
