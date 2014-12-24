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
public class NoteSaxHandler extends AbstractXTandemElementSaxHandler<KeyValuePair> {
    public static NoteSaxHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = NoteSaxHandler.class;

    private String m_Key;
      private String m_Type;

    public NoteSaxHandler(IElementHandler parent) {
        super("note", parent);
    }


    public String getKey()
    {
        return m_Key;
    }

    public void setKey(String pKey)
    {
        m_Key = pKey;
    }


    public String getType()
    {
        return m_Type;
    }

    public void setType(String pType)
    {
        m_Type = pType;
    }



    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {

        setKey(attr.getValue("label")); 
        setType(attr.getValue("type"));
         return;
    }

    @Override
    public void endElement(String elx, String localName, String el) throws SAXException
    {
          // added slewis
        if (getInitiatingTag().equals(el)) {
            finishProcessing();
            getParent().endElement(elx, localName, el);
            return;
        }
        throw new UnsupportedOperationException("Cannot handle end tag " + elx);
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        if("input".equals(getType())) {
            setElementObject(new KeyValuePair(getKey(),getIncludedText()));
        }


    }
}
