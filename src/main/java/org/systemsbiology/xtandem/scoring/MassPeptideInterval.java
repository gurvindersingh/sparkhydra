package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.peptide.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.MassPeptideInterval
 * represents a mass and a number of peptides
 * User: Steve
 * Date: 10/6/11
 */
public class MassPeptideInterval implements Serializable, Comparable<MassPeptideInterval> {
    private final int m_Mass;
    private final int m_Start;
    private final int m_End;

    public MassPeptideInterval(final int pMass, final int pStart, final int pEnd) {
        m_Mass = pMass;
        m_Start = pStart;
        m_End = pEnd;
    }

    public MassPeptideInterval(String s) {
        String[] items = s.split(":");
        m_Mass = Integer.parseInt(items[0]);
        if (items.length > 1)  // there is a colon
            m_Start = Integer.parseInt(items[1]);
        else
            m_Start = 0;
        if (items.length > 2)
            m_End = Integer.parseInt(items[2]);
        else
            m_End = Integer.MAX_VALUE;

    }


    /**
     * @return true if there are no limits to the interval
     */
    public boolean isUnlimited() {
        return m_End == Integer.MAX_VALUE && m_Start == 0;
    }

    /**
     * return all the peptides in start .. end - 1
     *
     * @param pps !null list of polypeptides
     * @return
     */
    public IPolypeptide[] filterPeptideList(IPolypeptide[] pps) {
        // np filtering
        if (pps.length == 0 || (m_End >= pps.length && m_Start == 0))
            return pps;
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (int i = getStart(); i < Math.min(getEnd(), pps.length); i++) {
            holder.add(pps[i]);
        }
        IPolypeptide[] ret = new IPolypeptide[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    public int getMass() {
        return m_Mass;
    }

    public int getStart() {
        return m_Start;
    }

    public int getEnd() {
        return m_End;
    }


    @Override
    public String toString() {
        if (isUnlimited()) {
            return String.format("%08d", m_Mass);
        }
        else {
            return
                    String.format("%08d", m_Mass) +
                            ":" + m_Start +
                            ":" + m_End;

        }

    }


    @Override
    public int compareTo(final MassPeptideInterval o) {
        int ret = Integer.compare(getMass(), o.getMass());
        if (ret != 0)
            return ret;
        ret = Integer.compare(getStart(), o.getStart());
        if (ret != 0)
            return ret;
        ret = Integer.compare(getEnd(), o.getEnd());
        return ret;
    }
}
