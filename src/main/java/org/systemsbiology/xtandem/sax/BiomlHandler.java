package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.sax.SaxMzXMLHandler
 *
 * @author Steve Lewis
 * @date Dec 23, 2010
 */
public class BiomlHandler<T> extends AbstractXTandemElementSaxHandler<AbstractXTandemElementSaxHandler<T>> implements ITopLevelSaxHandler {
    public static BiomlHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = BiomlHandler.class;

    private final AbstractXTandemElementSaxHandler<T> m_InternalHandler;
    private final String m_Url;

    public BiomlHandler(DelegatingSaxHandler hdlr, AbstractXTandemElementSaxHandler<T> internal, String file) {
        super("bioml", hdlr);
        m_InternalHandler = internal;
        m_InternalHandler.setParent(this);
        m_Url = file;
    }

    public BiomlHandler(DelegatingSaxHandler hdlr, String file) {
        super("bioml", hdlr);
        m_InternalHandler = null;
        m_Url = file;
    }

    public String getUrl() {
        return m_Url;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if (m_InternalHandler != null && m_InternalHandler.getInitiatingTag().equals(qName)) {
            getHandler().pushCurrentHandler(m_InternalHandler);
            m_InternalHandler.handleAttributes(uri, localName, qName, attributes);
            return;
        }
        super.startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void endElement(String elx, String localName, String el) throws SAXException {
        if (m_InternalHandler != null) {
            String initiatingTag = m_InternalHandler.getInitiatingTag();
            if (initiatingTag.equals(el)) {
                getHandler().popCurrentHandler();

                return;
            }
        }
        super.endElement(elx, localName, el);

    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        setElementObject(m_InternalHandler);
    }


    public T getFileObject() {
        if (m_InternalHandler == null)
            return null;
        return m_InternalHandler.getElementObject();
    }
}
