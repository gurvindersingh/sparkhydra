package org.systemsbiology.xtandem.bioml.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.sax.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.bioml.sax.AttributeHandler
 * User: steven
 * Date: 8/22/11
 */
public class AttributeHandler extends AbstractXTandemElementSaxHandler<String> {
    public static final AttributeHandler[] EMPTY_ARRAY = {};

    private String m_Type;
    public AttributeHandler( final IElementHandler pParent) {
        super("GAML:attribute", pParent);
    }


    public String getType() {
        return m_Type;
    }

    public void setType(final String pType) {
        m_Type = pType;
    }

    @Override
    public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        setType(attributes.getValue("type"));
        super.handleAttributes(uri, localName, qName, attributes);
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
       setElementObject(getIncludedText());
    }
}
