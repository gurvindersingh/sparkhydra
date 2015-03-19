package org.systemsbiology.xtandem.bioml.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.sax.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.bioml.sax.AbstractXTandemElementSaxHandler
 *
 * @author Steve Lewis
 * @date 8/20/13
 */
public abstract class AbstractXTandemElementSaxHandler<T> extends AbstractElementSaxHandler<T> {

    private  Map<String, String> m_Notes = new HashMap<String, String>();
    public AbstractXTandemElementSaxHandler(String initTag, IElementHandler pParent) {
        super(initTag, pParent);
    }

    public AbstractXTandemElementSaxHandler(String initTag, DelegatingSaxHandler pParent) {
        super(initTag, pParent);
    }

    /**
      * todo make a more specialized class for this method
      * @return
      */
     public IMainData getMainData()
     {
         IElementHandler parent = getParent();
         if(parent != null && parent instanceof AbstractElementSaxHandler)
             return ((AbstractXTandemElementSaxHandler)parent).getMainData();
         return null; // not present
     }

     public static final String[] FORGIVEN_DUPLICATES_FROM_DEFAULT_ISB = {
             "refine, point mutations",
             "scoring, cyclic permutation",
             "scoring, include reverse",
     };
     public void addNote(String key, String value)
     {
         if(key == null)
             return;
         boolean  forgiven = false;
         if(m_Notes.containsKey(key) ) {
             for (int i = 0; i < FORGIVEN_DUPLICATES_FROM_DEFAULT_ISB.length; i++) {
                 String test = FORGIVEN_DUPLICATES_FROM_DEFAULT_ISB[i];
                 if(test.equals(key)) {
                      forgiven = true;
                     break;
                 }


             }
             String duplicateValue = m_Notes.get(key);
             if(!forgiven && duplicateValue.length() > 0 && !duplicateValue.equals(m_Notes.get(key)))
                 throw new IllegalStateException("duplicate key in Notes file key=" + key +  " old value is " + m_Notes.get(key));
         }

         m_Notes.put(key, value);
         // use for generating tests
        // XTandemUtilities.outputLine("FOO.put(\"" + key + "\",\"" + value + "\");");
     }

     public String getNote(String key )
     {
        return m_Notes.get(key );
      }


     public Map<String, String> getNotes()
     {
         return m_Notes;
     }


     @Override
     public void endElement(String elx, String localName, String el) throws SAXException
     {
         if ("note".equals(el)) {
             NoteSaxHandler handler = (NoteSaxHandler) getHandler().popCurrentHandler();
             final KeyValuePair valuePair = handler.getElementObject();
             if (valuePair != null && valuePair.getKey() != null && valuePair.getValue() != null)
                 addNote(valuePair.getKey(), valuePair.getValue());
             return;
         }
         super.endElement(elx, localName, el);
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
         if ("note".equals(qName)) {
             NoteSaxHandler handler = new NoteSaxHandler(this);
             handler.handleAttributes(uri, localName, qName, attributes);
             getHandler().pushCurrentHandler(handler);
             return;
         }
         super.startElement(uri, localName, qName, attributes);

     }


 }

