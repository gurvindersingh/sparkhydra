package org.systemsbiology.sax;

import org.systemsbiology.xml.*;
import org.xml.sax.*;

import java.io.*;

/**
 * org.systemsbiology.xtandem.sax.TagLimitingParser
 * User: steven
 * Date: 5/31/11
 */
public class TagLimitingParser extends AppendableAccumulatingSaxParser {
    public static final TagLimitingParser[] EMPTY_ARRAY = {};

    public static PrintWriter buildWriter(String file) {
        try {
            return new PrintWriter(new FileWriter(file));
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    private final String m_LimitTag;
    private final int m_Limit;
    private int m_Count;
    private boolean m_IgnoreAll;

    public TagLimitingParser(final String initTag, final IElementHandler pParent, String file, String limitTag, int number) {
        super(initTag, pParent, buildWriter(file));
        m_LimitTag = limitTag;
        m_Limit = number;
    }

    public String getLimitTag() {
        return m_LimitTag;
    }

    public int getLimit() {
        return m_Limit;
    }

    public int getCount() {
        return m_Count;
    }

    public boolean isIgnoreAll() {
        return m_IgnoreAll;
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        if (isIgnoreAll()) {
            String initiatingTag = getInitiatingTag();
            Appendable sb = getSb();
            if("mzXML".equals(initiatingTag)) {
                  try {
                      sb.append("</msRun>\n</mzXML>\n");
                  }
                  catch (IOException e) {
                      throw new RuntimeException(e);

                  }
              }
            if("indexedmzML".equals(initiatingTag)) {
                  try {
                      sb.append("</spectrumList>\n</run>\n</mzML>\n</indexedmzML>\n");
                  }
                  catch (IOException e) {
                      throw new RuntimeException(e);

                  }
              }
            if("mzML".equals(initiatingTag)) {
                  try {
                      sb.append("</spectrumList>\n</run>\n</mzML>\n");
                  }
                  catch (IOException e) {
                      throw new RuntimeException(e);

                  }
              }
          }
        super.finishProcessing();
    }

    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        if (isIgnoreAll()) {
            String initiatingTag = getInitiatingTag();
            if (initiatingTag.equals(el))
                finishProcessing();
            return;
        }
        super.endElement(elx, localName, el);
        if (m_LimitTag.equals(el)) {
            if (m_Count++ >= m_Limit)
                m_IgnoreAll = true;
        }
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if (isIgnoreAll()) {
            return;
        }
        super.startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void characters(final char[] s, final int start, final int length) throws SAXException {
        if (isIgnoreAll()) {
            return;
        }
        super.characters(s, start, length);    //To change body of overridden methods use File | Settings | File Templates.
    }



}
