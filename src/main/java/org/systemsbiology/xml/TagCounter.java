package org.systemsbiology.xml;


import org.xml.sax.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xml.TagCounter
 *
 * @author Steve Lewis
 * @date Feb 1, 2011
 */
public class TagCounter  extends AbstractSaxParser
{
    public static TagCounter[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = TagCounter.class;
    public static final Integer ONE = 1;

    private final Map<String, Integer> m_TagCount =
            new HashMap<String, Integer>();

    public int getTagCount(String tag)
    {
        Integer count = m_TagCount.get(tag);
        if (count == null)
            return 0;
        else
            return count;
    }

    public void addTagCount(String tag)
    {
        Integer count = m_TagCount.get(tag);
        if (count == null)
            m_TagCount.put(tag, ONE);
        else
            m_TagCount.put(tag, count + 1);
    }


    public String[] getTags()
    {
        Set<String> strings = m_TagCount.keySet();
        String[] ret = strings.toArray(new String[strings.size()]);
        Arrays.sort(ret);
        return ret;
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
            throws SAXException
    {
        addTagCount(qName);

    }

    public static void main(String[] args)
    {
          TagCounter handler = new TagCounter();
          handler.parseDocument(new File(args[0]));
          String[] tags = handler.getTags();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < tags.length; i++) {
            String tag = tags[i];
            XMLUtilities.outputLine(tag + " " + handler.getTagCount(tag));
          }
    }
}
