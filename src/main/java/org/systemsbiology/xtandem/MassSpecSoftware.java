package org.systemsbiology.xtandem;

import java.io.*;

/**
 * org.systemsbiology.xtandem.MassSpecSoftware
 * User: steven
 * Date: Jan 3, 2011
 */
public class MassSpecSoftware   implements Serializable {
    public static final MassSpecSoftware[] EMPTY_ARRAY = {};

    private final String m_Type;
    private final String m_Name;
    private final String m_Version;

    public MassSpecSoftware(final String pType, final String pName, final String pVersion) {
        m_Type = pType;
        m_Name = pName;
        m_Version = pVersion;
    }

    public String getType() {
        return m_Type;
    }

    public String getName() {
        return m_Name;
    }

    public String getVersion() {
        return m_Version;
    }
}
