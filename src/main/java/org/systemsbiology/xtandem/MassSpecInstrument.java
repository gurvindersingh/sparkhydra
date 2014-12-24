package org.systemsbiology.xtandem;

import java.io.*;

/**
 * org.systemsbiology.xtandem.MassSpecInstrument
 * User: steven
 * Date: Jan 3, 2011
 */
public class MassSpecInstrument  implements Serializable {
    public static final MassSpecInstrument[] EMPTY_ARRAY = {};

    private final String m_Id;
    private String m_Manufacturer;
    private String m_Model;
    private String m_MassAnalyzer;
    private String m_Ionisation;
    private String m_Detector;
    private MassSpecSoftware m_Software;
     

    public MassSpecInstrument(final String pId) {
        m_Id = pId;
    }

    public String getId() {
        return m_Id;
    }

    public String getManufacturer() {
        return m_Manufacturer;
    }

    public void setManufacturer(final String pManufacturer) {
        m_Manufacturer = pManufacturer;
    }

    public String getModel() {
        return m_Model;
    }

    public void setModel(final String pModel) {
        m_Model = pModel;
    }

    public String getMassAnalyzer() {
        return m_MassAnalyzer;
    }

    public void setMassAnalyzer(final String pMassAnalyzer) {
        m_MassAnalyzer = pMassAnalyzer;
    }

    public String getIonisation() {
        return m_Ionisation;
    }

    public void setIonisation(final String pIonisation) {
        m_Ionisation = pIonisation;
    }

    public String getDetector() {
        return m_Detector;
    }

    public void setDetector(final String pDetector) {
        m_Detector = pDetector;
    }

    public MassSpecSoftware getSoftware() {
        return m_Software;
    }

    public void setSoftware(final MassSpecSoftware pSoftware) {
        m_Software = pSoftware;
    }
}
