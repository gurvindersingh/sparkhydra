package org.systemsbiology.sax;

import java.io.*;

/**
 * org.systemsbiology.xtandem.sax.IXMLAppender
 * User: steven
 * Date: 4/20/11
 */
public interface IXMLAppender extends Serializable {
    public static final IXMLAppender[] EMPTY_ARRAY = {};

    public void appendAttribute(String name, Object value);

    public void appendAttribute(String name, int value);

    public void appendAttribute(String name, long value);

    public void appendAttribute(String name, short value);

    public void appendAttribute(String name, char value);

    public void appendAttribute(String name, byte value);

    public void appendAttribute(String name, float value);

    public void appendAttribute(String name, double value);


    public void openEmptyTag(String name);

    public void openTag(String name);

    public void appendText(String name);

    public void closeTag(String name);

    public void cr();

    public void endTag();
}
