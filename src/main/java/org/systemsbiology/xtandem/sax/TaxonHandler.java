package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.SaxMzXMLHandler
 *
 * @author Steve Lewis
 * @date Dec 23, 2010
 * reads a fle list from a taxon tax
 */
public class TaxonHandler extends AbstractXTandemElementSaxHandler<String[]> {
    public static TaxonHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = TaxonHandler.class;

    private String m_Label;
    private final String m_AcceptableLabel;
    private final String m_OnlyAcceptableFormat;
    private final List<String> m_Files = new ArrayList<String>();

    public TaxonHandler(IElementHandler parent, String fileType, String acceptableLabel) {
        super("taxon", parent);
        m_AcceptableLabel = acceptableLabel;
        m_OnlyAcceptableFormat = fileType;
    }

    public String getAcceptableLabel() {
        return m_AcceptableLabel;
    }

    public String getOnlyAcceptableFormat() {
        return m_OnlyAcceptableFormat;
    }

    public String getLabel() {
        return m_Label;
    }

    public void setLabel(final String pLabel) {
        m_Label = pLabel;
    }

    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {

        setLabel(attr.getValue("label"));
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
        // ignore all entried but mine
        if (!getLabel().equals(getAcceptableLabel()))
            return;
        if ("file".equals(qName)) {
            String format = attributes.getValue("format");
            String onlyAcceptableFormat = getOnlyAcceptableFormat();
            if (!onlyAcceptableFormat.equalsIgnoreCase(format)) {
                return;
                // throw new IllegalStateException("cannot support format " + format + " only " + getOnlyAcceptableFormat());
            }

            String url = attributes.getValue("URL");
            m_Files.add(url);

            return;
        }
        super.startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void endElement(String elx, String localName, String el) throws SAXException {
        // we handle this tag internally
        if ("file".equals(el)) {
            return;
        }
        // added slewis
        if (getInitiatingTag().equals(el)) {
            finishProcessing();

            final IElementHandler parent = getParent();
            if (parent != null)
                parent.endElement(elx, localName, el);
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
        setElementObject(m_Files.toArray(new String[0]));


    }
}
