package org.systemsbiology.xtandem.hadoop;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.sax.*;
import org.xml.sax.*;

/**
 * needed to work as a top level handler
 */
public class TopLevelScanHandler extends MzXMLScanHandler implements ITopLevelSaxHandler {
    public TopLevelScanHandler( ) {
        super(null);
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
            throws SAXException
    {
        if("scan".equals(el)) {
            handleAttributes(uri,localName,el,attributes);
            return;
        }
        super.startElement(uri, localName, el, attributes);

    }
}
