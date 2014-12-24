package org.systemsbiology.xml;

import org.systemsbiology.sax.*;
import org.xml.sax.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xml.AppendableAccumulatingSaxParser
 * Parses by creating a string
 * User: steven
 * Date: 4/27/11
 */
public class AppendableAccumulatingSaxParser extends AbstractElementSaxHandler<String> implements ITopLevelSaxHandler {
    public static final AppendableAccumulatingSaxParser[] EMPTY_ARRAY = {};



    private Appendable m_Sb ;
    private Stack<Boolean> m_InElement = new Stack<Boolean>();
    private int m_Indent;
    private boolean m_ExitWhenDone;

    public AppendableAccumulatingSaxParser(final String initTag, final IElementHandler pParent,Appendable sb) {
        super(initTag, pParent);
        m_Sb = sb;
        m_InElement.push(false);
    }

    public AppendableAccumulatingSaxParser(final String initTag, final DelegatingSaxHandler pParent,Appendable sb) {
        super(initTag, pParent);
        m_Sb = sb;
    }



    public Appendable getSb() {
        return m_Sb;
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

        try {
            if (!isInElement())
                m_Sb.append(" >");
            String  values = new String(s);

            m_Sb.append(values, start, start + length);
            setInElement(true);  // no need for close tag
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        if(m_Sb instanceof PrintWriter)
             ((PrintWriter) m_Sb).close();

    }

    @Override
    public void endElement(String elx, String localName, String el) throws SAXException {
        try {
            if (isInElement()) {
                m_Sb.append("</" + el + ">\n");

            }
            else {
                m_Sb.append(" />\n");
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

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

        try {
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
        catch (IOException e) {
            throw new RuntimeException(e);

        }

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
