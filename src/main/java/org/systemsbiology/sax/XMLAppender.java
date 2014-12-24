package org.systemsbiology.sax;


import com.lordjoe.lib.xml.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.XMLAppender
 * User: steven
 * Date: 4/20/11
 */
public class XMLAppender implements IXMLAppender {
    public static final XMLAppender[] EMPTY_ARRAY = {};

    private final Appendable m_Accumlator;
    private boolean m_InTagOpening;
    private final Stack<String> m_Tags = new Stack<String>();

    public XMLAppender(final Appendable pAccumlator) {
        m_Accumlator = pAccumlator;
    }

    protected boolean isInTagOpening() {
        return m_InTagOpening;
    }

    protected void setInTagOpening(final boolean pInTagOpening) {
        m_InTagOpening = pInTagOpening;
    }


    @Override
    public void appendText(final String in) {
        try {
            if (isInTagOpening())
                throw new IllegalStateException("Cannot add text when  in tag opening");
            String s = XMLUtil.makeXMLString(in);
            m_Accumlator.append(s);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    @Override
    public void appendAttribute(String name, int value) {
        appendAttribute(name, Integer.toString(value));
    }


    @Override
    public void appendAttribute(String name, long value) {
        appendAttribute(name, Long.toString(value));
    }


    @Override
    public void appendAttribute(String name, short value) {
        appendAttribute(name, Short.toString(value));
    }


    @Override
    public void appendAttribute(String name, char value) {
        appendAttribute(name, Character.toString(value));
    }


    @Override
    public void appendAttribute(String name, byte value) {
        appendAttribute(name, Byte.toString(value));
    }


    @Override
    public void appendAttribute(String name, float value) {
        if (Float.isInfinite(value))
            value = 0;
        if (Float.isNaN(value))
            value = 0;
        appendAttribute(name, Float.toString(value));
    }

    @Override
    public void appendAttribute(String name, double value) {
        if (Double.isInfinite(value))
            value = 0;
        if (Double.isNaN(value))
            value = 0;

        appendAttribute(name, Double.toString(value));
    }


    @Override
    public void appendAttribute(String name, Object value) {
        try {
            if (!isInTagOpening())
                throw new IllegalStateException("Cannot add attribute when not in tag opening");
            String in = "null";
            if (value != null)
                in = value.toString();
            //noinspection StringConcatenationInsideStringBufferAppend
            m_Accumlator.append(" " + name + "=\"" +
                    XMLUtil.makeXMLString(in) + "\"");
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public void openEmptyTag(final String name) {
        openTag(name);
        endTag();
    }

    @Override
    public void openTag(String name) {
        try {
            name = name.trim();
            if (!m_Tags.isEmpty())
                m_Accumlator.append("    ");
            m_Tags.push(name);
            //noinspection StringConcatenationInsideStringBufferAppend
            m_Accumlator.append("<" + name + " ");
            setInTagOpening(true);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public void closeTag(String name) {
        try {
            name = name.trim();
            String desired = m_Tags.pop();
            if (!desired.equals(name))
                throw new IllegalStateException("bad tag end " + name + " should be " + desired);
            if (isInTagOpening()) {
                m_Accumlator.append(" />");
            } else {
                //noinspection StringConcatenationInsideStringBufferAppend
                m_Accumlator.append("</" + name + ">");
            }
            setInTagOpening(false);
            cr();
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public void cr() {
        try {
            m_Accumlator.append("\n");
            for (int i = 0; i < m_Tags.size() - 1; i++) {
                m_Accumlator.append("    ");

            }
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    @Override
    public void endTag() {
        try {
            m_Accumlator.append(" >");
            setInTagOpening(false);
        } catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

}
