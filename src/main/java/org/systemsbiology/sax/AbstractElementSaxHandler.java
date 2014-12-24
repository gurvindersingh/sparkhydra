package org.systemsbiology.sax;


import org.systemsbiology.xml.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.AbstractElementSaxHandler
 * User: steven
 * Date: Jan 3, 2011
 */
public abstract class AbstractElementSaxHandler<T> extends AbstractSaxParser implements
        IElementHandler<T>
{
    public static final AbstractElementSaxHandler[] EMPTY_ARRAY = {};

    public   DelegatingSaxHandler m_Handler;
    public   IElementHandler m_Parent;

    private final String m_InitiatingTag;
    private final StringBuilder m_IncludedText = new StringBuilder();
    private T m_ElementObject;  // Object represented by this element


    protected AbstractElementSaxHandler(String initTag, IElementHandler pParent)
    {
        m_InitiatingTag = initTag;
        m_Parent = pParent;
        if (pParent != null) {
            if (pParent instanceof AbstractElementSaxHandler) {
                m_Handler = ((AbstractElementSaxHandler) pParent).getHandler();

            }
            else {
                m_Handler = null;
            }
        }
        else {
            m_Handler = null;
        }
    }

    protected AbstractElementSaxHandler(String initTag, DelegatingSaxHandler pParent)
    {
        m_InitiatingTag = initTag;
        m_Parent = null;
        m_Handler = pParent;
    }



    public void setParent(final IElementHandler pParent) {
        if(m_Parent == pParent)
            return;
        if(pParent == null) {
            m_Parent = pParent;
            return; // ok to clear
        }
        if(m_Parent != null)
            throw new IllegalStateException("m_ParentStream can only be set once");
        m_Parent = pParent;
    }


    public String getInitiatingTag()
    {
        return m_InitiatingTag;
    }

    public DelegatingSaxHandler getHandler()
    {
        return m_Handler;
    }

    public void setHandler(DelegatingSaxHandler pHandler)
    {
        if(m_Handler == pHandler)
            return;
       if(m_Handler != null)
           throw new IllegalStateException("handler can only be set once");
        m_Handler = pHandler;
    }

    public T getElementObject()
    {
        return m_ElementObject;
    }

    public void setElementObject(final T pElementObject)
    {
        m_ElementObject = pElementObject;
    }

    public IElementHandler getParent()
    {
        return m_Parent;
    }

    /**
     * return the file responsible
     */
    @Override
    public String getURL()
    {
        if (getParent() != null)
            return getParent().getURL();
        return null;
    }

    @Override
    public void characters(char[] s, int start, int length) throws SAXException
    {
        for (int i = 0; i < length; i++) {
            m_IncludedText.append(s[start + i]);
        }
    }

    public final String getIncludedText()
    {
        return m_IncludedText.toString();
    }

    public void clearIncludedText()
    {
        m_IncludedText.setLength(0);
    }


    @Override
    public void endElement(String elx, String localName, String el) throws SAXException
    {
        if (m_Handler != null) {
             String initiatingTag =  getInitiatingTag();
             if (initiatingTag.equals(el)) {
                 finishProcessing();
                 IElementHandler parent = getParent();
                 if(parent != null)
                     parent.endElement(elx,   localName,   el);
                  return;
             }
         }
           throw new UnsupportedOperationException("Cannot handle end tag " + el);
    }


    /**
      * Ignore this tag and all sub tags
      * @param uri
      * @param localName
      * @param qName
      * @param attributes
      */
     protected void setIgnoreTagContents(final String uri, final String localName, final String qName, final Attributes attributes)
     {
         AbstractElementSaxHandler handler = new DiscardingSaxParser(qName, this);
         getHandler().pushCurrentHandler(handler);
         try {
             handler.handleAttributes(uri, localName, qName, attributes);
         }
         catch (SAXException e) {
             throw new RuntimeException(e);

         }

     }

}
