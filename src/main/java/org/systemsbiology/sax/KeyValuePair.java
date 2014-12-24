package org.systemsbiology.sax;

/**
 * org.systemsbiology.xtandem.sax.KeyValuePair
 *
 * @author Steve Lewis
 * @date Jan 4, 2011
 */
public class KeyValuePair
{
    public static KeyValuePair[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = KeyValuePair.class;

    private final String m_Key;
    private final String m_Value;

    public KeyValuePair(String pKey, String pValue)
    {
        m_Key = pKey;
        m_Value = pValue;
    }

    public String getKey()
    {
        return m_Key;
    }

    public String getValue()
    {
        return m_Value;
    }
}
