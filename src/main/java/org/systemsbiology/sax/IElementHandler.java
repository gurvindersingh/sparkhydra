package org.systemsbiology.sax;

/**
 * org.systemsbiology.xtandem.sax.IElementHandler
 * User: steven
 * Date: Jan 3, 2011
 */
public interface IElementHandler<T> extends ISaxHandler {
    public static final IElementHandler[] EMPTY_ARRAY = {};

    /**
     * return the handler for the element above this or null if this is the top handler
     * @return
     */
    public IElementHandler getParent();

    /**
     * get the tag used to initialize the handler - seeing this tag should terminate processing
     * @return !null tag
     */
     public String getInitiatingTag();

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    public void finishProcessing();

    /**
     * return the file responsible
     */
    public String getURL();


}
