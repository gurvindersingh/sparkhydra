package org.systemsbiology.xml;


import org.systemsbiology.sax.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.parsers.*;
import java.io.*;

/**
 * org.systemsbiology.xml.AbstractSaxParser
 *
 * @author Steve Lewis
 * @date Dec 22, 2010
 */
public abstract class AbstractSaxParser extends DefaultHandler implements ISaxHandler
{

    private String m_strFileName;

    public AbstractSaxParser()
    {
    }


    public String getStrFileName()
    {
        return m_strFileName;
    }

    public void setStrFileName(String pStrFileName)
    {
        m_strFileName = pStrFileName;
    }



    public void parseDocument(File inp)
    {
        setStrFileName(inp.getName());
        try {
            parseDocument(new FileInputStream(inp));
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void parseDocument(InputStream inp)
    {

        //get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {

            //get a new instance of parser
            SAXParser sp = spf.newSAXParser();

            //parse the file and also register this class for call backs
            sp.parse(inp, this);


        }
        catch (SAXException se) {
            throw new RuntimeException(se);
        }
        catch (ParserConfigurationException pce) {
            throw new RuntimeException(pce);
         }
        catch (IOException ie) {
            throw new RuntimeException(ie);
          }
    }

    /**
     * Divides the handling of a start element into handling the element and handling
     * Attributes
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
    public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {

    }

//Event Handlers


}





