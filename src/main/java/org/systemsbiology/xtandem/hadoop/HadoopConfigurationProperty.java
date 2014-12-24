package org.systemsbiology.xtandem.hadoop;

/**
 * org.systemsbiology.xtandem.hadoop.HadoopConfigurationProperty
 * User: steven
 * Date: 5/31/11
 */
public class HadoopConfigurationProperty {
    public static final HadoopConfigurationProperty[] EMPTY_ARRAY = {};

    private final String m_Name;
    private final String m_Value;
    private final boolean m_Final;

    public HadoopConfigurationProperty(final String pName, final String pValue, final boolean pFinal) {
        m_Name = pName;
        m_Value = pValue;
        m_Final = pFinal;
    }

    public String getName() {
        return m_Name;
    }

    public String getValue() {
        return m_Value;
    }

    public boolean isFinal() {
        return m_Final;
    }
}
