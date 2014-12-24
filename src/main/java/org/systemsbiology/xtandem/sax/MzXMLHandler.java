package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.MzXMLHandler
 *
 * @author Steve Lewis
 * @date Dec 23, 2010
 */
public class MzXMLHandler extends AbstractXTandemElementSaxHandler<MassSpecRun[]> implements ITopLevelSaxHandler  {
    public static MzXMLHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MzXMLHandler.class;

    private final List<MassSpecRun>  m_Runs = new ArrayList<MassSpecRun>();


    public MzXMLHandler( ) {
          super("mzXML",(IElementHandler)null);
      }
    public MzXMLHandler(DelegatingSaxHandler handler ) {
          super("mzXML",handler);
      }

    public void addRun(MassSpecRun added)  {
        m_Runs.add(added);
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
    public void startElement(final String uri, final String localName, final String el, final Attributes attributes) throws SAXException {
  
        if ("msRun".equals(el)) {
            MassSpecRunHandler handler = new MassSpecRunHandler(this);
            final DelegatingSaxHandler saxHandler = getHandler();
            saxHandler.pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName,  el, attributes);
             return;
         }
        // Ignore all of these
        if ("index".equals(el)) {
                return;
         }
        if ("offset".equals(el)) {
                return;
         }
        if ("indexOffset".equals(el)) {
                return;
         }
        if ("sha1".equals(el)) {
                return;
         }
 
        super.startElement(uri, localName, el, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        if ("msRun".equals(el)) {
            final DelegatingSaxHandler saxHandler = getHandler();
            final ISaxHandler saxHandler1 = saxHandler.popCurrentHandler();
            MassSpecRunHandler handler = (MassSpecRunHandler) saxHandler1;
            addRun(handler.getElementObject());
            return;
        }
        // Ignore all of these
        if ("index".equals(el)) {
                return;
         }
        if ("offset".equals(el)) {
                return;
         }
        if ("indexOffset".equals(el)) {
                return;
         }
        if ("sha1".equals(el)) {
                return;
         }
   
        super.endElement(elx, localName, el);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {

        setElementObject(m_Runs.toArray(new MassSpecRun[0]));

    }
}
