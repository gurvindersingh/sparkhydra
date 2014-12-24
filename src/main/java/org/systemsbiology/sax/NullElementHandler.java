package org.systemsbiology.sax;

import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.sax.NullElementHandler
 * simply ignores everything in the element adding similar tage for sub-items
 * User: steven
 * Date: 3/7/12
 */
public class NullElementHandler extends AbstractElementSaxHandler {
    public static final NullElementHandler[] EMPTY_ARRAY = {};

    public NullElementHandler(final String initTag, final IElementHandler pParent) {
        super(initTag, pParent);
    }


    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        NullElementHandler handler = new NullElementHandler(qName,this);
         getHandler().pushCurrentHandler(handler);
        //noinspection UnnecessaryReturnStatement
        return;

     }

    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        // added slewis
        String initiatingTag = getInitiatingTag();
        if (initiatingTag.equals(el)) {
            finishProcessing();

            getHandler().popCurrentHandler( );
              return;
        }
        //noinspection UnnecessaryReturnStatement
        return;
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {

    }
}
