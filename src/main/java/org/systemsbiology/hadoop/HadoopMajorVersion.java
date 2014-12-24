package org.systemsbiology.hadoop;

import org.apache.hadoop.*;
import org.apache.hadoop.util.*;

/**
 * org.systemsbiology.hadoop.HadoopMajorVersion
 *   enumeration of major Hadoop versions
 * @author Steve Lewis
 * @date 19/05/13
 */
public enum HadoopMajorVersion {
    Version0("0.2"), Version1("1."), Version2("2.");

    /**
     * the major version of Hadoop we are using
     */
    public static  HadoopMajorVersion CURRENT_VERSION =  getHadoopVersion();

    private final String m_StartText;

    private HadoopMajorVersion(String startText) {
        m_StartText = startText;
    }

    public String getStartText() {
        return m_StartText;
    }

    /**
     * figure out the major hadoop version by looking at     HadoopVersionAnnotation in
     * package
     * @return !null majoe version
     * @throws  IllegalStateException if presented with a version it does not understand
     */
    private static HadoopMajorVersion getHadoopVersion() {
        // force the class loader to load a class in the package so we can read the package
           String version = VersionInfo.getVersion();
        final String[] split = version.split("\\.");
        int majorVersion = Integer.parseInt(split[0]);
        switch(majorVersion)  {
            case 0:
                return Version0;
            case 1:
                return Version1;
            case 2:
                return Version2 ;
            default:
                throw new IllegalStateException("Unknown Hadoop version " + version);

        }
     }
}
