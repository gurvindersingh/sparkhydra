package org.systemsbiology.xtandem;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.MassSpecRun
 * User: steven
 * Date: Jan 3, 2011
 */
public class MassSpecRun  implements Serializable {
    public static final MassSpecRun[] EMPTY_ARRAY = {};

    private int m_ScanCount;
    private String m_StartTime;
    private String m_EndTime;
    private Map<String, MassSpecInstrument> m_Instrument = new HashMap<String, MassSpecInstrument>();
    private Map<String, RawPeptideScan> m_Scan = new HashMap<String, RawPeptideScan>();


    public int getScanCount() {
        return m_ScanCount;
    }

    public void setScanCount(final int pScanCount) {
        m_ScanCount = pScanCount;
    }

    public String getStartTime() {
        return m_StartTime;
    }

    public void setStartTime(final String pStartTime) {
        m_StartTime = pStartTime;
    }

    public String getEndTime() {
        return m_EndTime;
    }

    public void setEndTime(final String pEndTime) {
        m_EndTime = pEndTime;
    }

    public void addScan(RawPeptideScan added) {
        if(added.getPrecursorMz() == null)
            return;
        if(added.getPeaksCount() == 0)
            return;
        added.validate();
        m_Scan.put(added.getId(), added);
    }




    public RawPeptideScan[] getScans() {
        RawPeptideScan[] scans = m_Scan.values().toArray(new RawPeptideScan[0]);
        Arrays.sort(scans);
        return scans;
    }

    public RawPeptideScan getScan(String key) {
        return m_Scan.get(key);
    }


    public void addInstrument(MassSpecInstrument added) {
        m_Instrument.put(added.getId(), added);
    }


    public void removeInstrument(MassSpecInstrument removed) {
        m_Instrument.remove(removed.getId());
    }

    public void removeInstrument(String removed) {
        m_Instrument.remove(removed);
    }

    public MassSpecInstrument[] getInstruments() {
        return m_Instrument.values().toArray(new MassSpecInstrument[0]);
    }

    public MassSpecInstrument getInstrument(String key) {
        return m_Instrument.get(key);
    }

}
