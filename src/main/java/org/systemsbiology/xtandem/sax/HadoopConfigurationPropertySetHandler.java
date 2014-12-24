package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.HadoopConfigurationPropertySetHandler
 *  parses a hadoop configuration file
 * @author Steve Lewis
 * @date Dec 23, 2010
 */
public class HadoopConfigurationPropertySetHandler extends AbstractXTandemElementSaxHandler<HadoopConfigurationPropertySet> implements ITopLevelSaxHandler
{
    public static HadoopConfigurationPropertySetHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = HadoopConfigurationPropertySetHandler.class;


    private   String m_Name;
    private   String m_Value;
    private   boolean m_Final;
    private final List<HadoopConfigurationProperty>  m_Properties =
            new ArrayList<HadoopConfigurationProperty>();

    public HadoopConfigurationPropertySetHandler( ) {
        super("configuration", (IElementHandler)null);
    }



    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {

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

        if ("property".equals(qName)) {
            return;
        }
        else if ("name".equals(qName)) {
            clearIncludedText();
            return;
        }
        else if ("value".equals(qName)) {
            clearIncludedText();
            return;  // ignore
        }
        else if ("final".equals(qName)) {
            clearIncludedText();
            return;  // ignore
        }

        super.startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        if ("property".equals(el)) {
            HadoopConfigurationProperty handler = new HadoopConfigurationProperty(m_Name,m_Value,m_Final);
            m_Properties.add(handler);
            m_Name = null;
            m_Value = null;
            m_Final = false;
            clearIncludedText();
            return;
        }
         else if ("name".equals(el)) {
             m_Name = getIncludedText().trim();
            clearIncludedText();
             return;
         }
        else if ("value".equals(el)) {
             m_Value = getIncludedText().trim();
            clearIncludedText();
             return;
         }
          else if ("final".equals(el)) {
            String trim = getIncludedText().trim();
            m_Final = "true".equalsIgnoreCase(trim);
            clearIncludedText();
             return;
         }
         super.endElement(elx, localName, el);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public static class BadEndTagException extends RuntimeException {
        /**
         * Constructs a new runtime exception with the specified detail message.
         * The cause is not initialized, and may subsequently be initialized by a
         * call to {@link #initCause}.
         *
         * @param message the detail message. The detail message is saved for
         *                later retrieval by the {@link #getMessage()} method.
         */
        public BadEndTagException(final String message) {
            super(message);
        }
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        // finish building the object
        HadoopConfigurationPropertySet item = new HadoopConfigurationPropertySet(m_Properties);


        setElementObject(item);

    }
}
