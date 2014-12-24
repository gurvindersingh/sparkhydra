package org.systemsbiology.sax;

import org.systemsbiology.xml.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.sax.DelegatingSaxHandler
 * This class
 * User: steven
 * Date: Jan 3, 2011
 */
public class DelegatingSaxHandler extends AbstractSaxParser
{
    public static final DelegatingSaxHandler[] EMPTY_ARRAY = {};

    private final Stack<ISaxHandler> m_Handlers = new Stack<ISaxHandler>();

    public DelegatingSaxHandler() {
    }

    /**
     * constructor taking a base handler as a start item
     * @param handler  !null handler
     */
    public DelegatingSaxHandler(ISaxHandler handler) {
        this();
        pushCurrentHandler(handler);
    }

    public ISaxHandler getCurrentHandler() {
        return m_Handlers.peek();
    }

    public ISaxHandler popCurrentHandler() {
        return m_Handlers.pop();
    }

    public void pushCurrentHandler(ISaxHandler handler) {
          m_Handlers.push(handler);
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
        getCurrentHandler().startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
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
    public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        ISaxHandler saxHandler = getCurrentHandler();
        saxHandler.handleAttributes(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Receive notification of the end of an element.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the end of
     * each element (such as finalising a tree node or writing
     * output to a file).</p>
     *
     * @param uri       The Namespace URI, or the empty string if the
     *                  element has no Namespace URI or if Namespace
     *                  processing is not being performed.
     * @param localName The local name (without prefix), or the
     *                  empty string if Namespace processing is not being
     *                  performed.
     * @param qName     The qualified name (with prefix), or the
     *                  empty string if qualified names are not available.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.ContentHandler#endElement
     */
    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException {
        ISaxHandler saxHandler = getCurrentHandler();
        saxHandler.endElement(uri, localName, qName);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * Receive notification of character data inside an element.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method to take specific actions for each chunk of character data
     * (such as adding the data to a node or buffer, or printing it to
     * a file).</p>
     *
     * @param ch     The characters.
     * @param start  The start position in the character array.
     * @param length The number of characters to use from the
     *               character array.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.ContentHandler#characters
     */
    @Override
    public void characters(final char[] ch, final int start, final int length) throws SAXException {
        getCurrentHandler().characters(ch, start, length);    //To change body of overridden methods use File | Settings | File Templates.
    }
}
