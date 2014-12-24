package org.systemsbiology.xml;

import org.systemsbiology.sax.*;
import org.xml.sax.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xml.StringAccumulatingSaxParser
 * Parses by creating a string
 * User: steven
 * Date: 4/27/11
 */
public class StringAccumulatingSaxParser extends AbstractElementSaxHandler<String> implements ITopLevelSaxHandler {
    public static final StringAccumulatingSaxParser[] EMPTY_ARRAY = {};


    public static String accumulateStringFromTag(String str, String tag) {
        byte[] bytes = str.getBytes();
        InputStream inp = new ByteArrayInputStream(bytes);
        return accumulateStringFromTag(inp, tag);
    }

    public static String accumulateStringFromTag(InputStream inp, String tag) {
        StringAccumulatingSaxParser handler = new StringAccumulatingSaxParser(tag, (IElementHandler) null);
        handler.setExitWhenDone(true);
        //   StringAccumulatingSaxParser handler = new StringAccumulatingSaxParser("mzXML",new DelegatingSaxHandler());
        String ret = null;
        try {
            ret = XMLUtilities.parseFile(inp, handler, "");
        }
        catch (ParseFinishedException e) {  // we expect to exit like this
            ret = handler.getElementObject();

        }
        finally {
            try {
                inp.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return ret;
    }

    private StringBuilder m_Sb = new StringBuilder();
    private Stack<Boolean> m_InElement = new Stack<Boolean>();
    private int m_Indent;
    private boolean m_ExitWhenDone;

    public StringAccumulatingSaxParser(final String initTag, final IElementHandler pParent) {
        super(initTag, pParent);
        m_InElement.push(false);
    }

    public StringAccumulatingSaxParser(final String initTag, final DelegatingSaxHandler pParent) {
        super(initTag, pParent);
    }

    public boolean isExitWhenDone() {
        return m_ExitWhenDone;
    }

    public void setExitWhenDone(final boolean pExitWhenDone) {
        m_ExitWhenDone = pExitWhenDone;
    }

    public boolean isInElement() {
        return m_InElement.peek();
    }

    public void setInElement(boolean value) {
        if (value == (isInElement()))
            return;
        m_InElement.pop();
        m_InElement.push(value);

    }


    @Override
    public void characters(char[] s, int start, int length) throws SAXException {
        if (!isInElement())
            m_Sb.append(" >");
        m_Sb.append(s, start, length);
        setInElement(true);  // no need for close tag
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        setElementObject(m_Sb.toString());

    }

    @Override
    public void endElement(String elx, String localName, String el) throws SAXException {
        if (isInElement()) {
            m_Sb.append("</" + el + ">\n");

        }
        else {
            m_Sb.append(" />\n");
        }
        m_InElement.pop();
        m_Indent--;
        // added slewis
        String initiatingTag = getInitiatingTag();
        if (initiatingTag.equals(el)) {
            finishProcessing();

            final IElementHandler parent = getParent();
            if (parent != null)
                parent.endElement(elx, localName, el);
            if (isExitWhenDone())
                throw new ParseFinishedException(); // not a problem but stops the parse
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
            throws SAXException {
        setInElement(true);  // m_ParentStream has content

        m_Sb.append("<");
        m_Sb.append(qName);
        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getLocalName(i);
            String value = attributes.getValue(i);
            m_Sb.append(" " + name + "=\"" + value + "\" ");
        }
        m_InElement.push(false);   // no need for close tag
        m_Indent++;

    }

    public static class ParseFinishedException extends RuntimeException {
        /**
         * Constructs a new runtime exception with <code>null</code> as its
         * detail message.  The cause is not initialized, and may subsequently be
         * initialized by a call to {@link #initCause}.
         */
        public ParseFinishedException() {
        }
    }

}
