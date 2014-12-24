package org.systemsbiology.xtandem.hadoop;

/**
* org.systemsbiology.xtandem.hadoop.SearchDatabaseCounts
*
* @author Steve Lewis
* @date 08/11/13
*/
public class SearchDatabaseCounts {
    private final int m_MZBin;
    private final int m_Entries;
    private final int m_Modified;
    private final int m_Unmodified;

    public SearchDatabaseCounts(int MZBin, int entries, int modified, int unmodified) {
        m_MZBin = MZBin;
        m_Entries = entries;
        m_Modified = modified;
        m_Unmodified = unmodified;
    }


    public SearchDatabaseCounts(String asString) {
        String[] items = asString.split("\t");
        int index = 0;
        m_MZBin = Integer.parseInt(items[index++]);
        m_Entries = Integer.parseInt(items[index++]);
        m_Modified = Integer.parseInt(items[index++]);
        m_Unmodified = Integer.parseInt(items[index++]);
    }

    public int getMZBin() {
        return m_MZBin;
    }

    public int getEntries() {
        return m_Entries;
    }

    public int getModified() {
        return m_Modified;
    }

    public int getUnmodified() {
        return m_Unmodified;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toString(getMZBin()));
        sb.append("\t");
        sb.append(Integer.toString(getEntries()));
        sb.append("\t");
        sb.append(Integer.toString(getModified()));
        sb.append("\t");
        sb.append(Integer.toString(getUnmodified()));


        return sb.toString();
    }
}
