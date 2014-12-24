package org.systemsbiology.xml;

import org.systemsbiology.sax.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xml.DiscardingSaxParser
 * Parses by ignoring the tag
 * User: steven
 * Date: 4/27/11
 */
public class DiscardingSaxParser extends AbstractElementSaxHandler<Object> {
    public static final DiscardingSaxParser[] EMPTY_ARRAY = {};

    public DiscardingSaxParser(final String initTag, final IElementHandler pParent) {
        super(initTag, pParent);
    }

    public DiscardingSaxParser(final String initTag, final DelegatingSaxHandler pParent) {
        super(initTag, pParent);
    }

    @Override
    public void characters(char[] s, int start, int length) throws SAXException
    {
         // do nothing
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
         // do nothing

    }

    @Override
    public void endElement(String elx, String localName, String el) throws SAXException
    {
       // added slewis
        String initiatingTag = getInitiatingTag();
        if (initiatingTag.equals(el)) {
            finishProcessing();

            final IElementHandler parent = getParent();
            if (parent != null)
                parent.endElement(elx, localName, el);
            return;
        }
         // do nothing
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
    public void startElement(String uri, String localName, String qName, Attributes attributes)
            throws SAXException
    {
          // do nothing
    }

}
