package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.MeasuredSpectrumHandler
 * User: steven
 * Date: 3/28/11
 */
public class MeasuredSpectrumHandler  extends AbstractXTandemElementSaxHandler<IMeasuredSpectrum> {
    public static final MeasuredSpectrumHandler[] EMPTY_ARRAY = {};
    public static final String TAG = "MeasuredSpectrum" ;

    private final List<ISpectrumPeak> m_Peaks = new ArrayList<ISpectrumPeak>();
    private int m_PrecursorCharge;
    private double m_PrecursorMassChargeRatio;

    public MeasuredSpectrumHandler(  final IElementHandler pParent) {
        super(TAG, pParent);
    }

    public MeasuredSpectrumHandler(final String initTag, final DelegatingSaxHandler pParent) {
        super(initTag, pParent);
    }

    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        if ("peaks".equals(el)) {
            MzXMLPeaksHandler handler = (MzXMLPeaksHandler) getHandler().popCurrentHandler();
            ISpectrumPeak[] peaks = handler.getElementObject();
            for (int i = 0; i < peaks.length; i++) {
                ISpectrumPeak peak = peaks[i];
                addSpectralPeak(peak);
            }
             return;
            }
        if ("Peak".equals(el)) {
                 return;
            }
         super.endElement(elx, localName, el);
    }

    public void addSpectralPeak(ISpectrumPeak pk)  {
        m_Peaks.add(pk);
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
    public void startElement(final String uri, final String localName, final String qName, final Attributes attr) throws SAXException {
        if ("Peak".equals(qName)) {
             double massChargeRatio = XTandemSaxUtilities.getRequiredDoubleAttribute("massChargeRatio", attr);
             float peak = XTandemSaxUtilities.getRequiredFloatAttribute("peak", attr);
             SpectrumPeak pk = new SpectrumPeak(massChargeRatio, peak);
             addSpectralPeak(pk);
             return;
         }
        if ("peaks".equals(qName)) {
            MzXMLPeaksHandler handler = new MzXMLPeaksHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, qName, attr);
            return;
           }
         super.startElement(uri, localName, qName, attr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Divides the handling of a start element into handling the element and handling
     * Attributes
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
    public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attr) throws SAXException {

        String value;
        value = attr.getValue("PrecursorMZ");
         if (value != null)
              setPrecursorMassChargeRatio(Double.parseDouble(value) );


        value = attr.getValue("PrecursorCharge");
        if (value != null)
             setPrecursorCharge(Integer.parseInt(value));

        value = attr.getValue("PrecursorMass");
         if (value != null)   {
             try {
                 double v = Double.parseDouble(value);
                 setPrecursorMassChargeRatio(v / getPrecursorCharge());
             }
             catch (NumberFormatException e) {

             }
         }

        super.handleAttributes(uri, localName, qName, attr);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public int getPrecursorCharge() {
        return m_PrecursorCharge;
    }

    public void setPrecursorCharge(final int pPrecursorCharge) {
        m_PrecursorCharge = pPrecursorCharge;
    }

    public double getPrecursorMassChargeRatio() {
        return m_PrecursorMassChargeRatio;
    }

    public void setPrecursorMassChargeRatio(final double pPrecursorMass) {
        m_PrecursorMassChargeRatio = pPrecursorMass;
    }



    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        ISpectrumPeak[] peaks = m_Peaks.toArray(ISpectrumPeak.EMPTY_ARRAY);
        int precursorCharge = getPrecursorCharge();
        double precursorMz = getPrecursorMassChargeRatio();

        ScoringMeasuredSpectrum spce = new  ScoringMeasuredSpectrum(precursorCharge, precursorMz,null, peaks);
        setElementObject(spce);
    }
}
