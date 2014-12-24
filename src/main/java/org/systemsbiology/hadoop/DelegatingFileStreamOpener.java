package org.systemsbiology.hadoop;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.hadoop.DelegatingFileStreamOpener
 * in this version we have a list of possible openers and ]
 * take the first one that works
 *
 * @author Steve Lewis
 * @date Mar 8, 2011
 */
public class DelegatingFileStreamOpener implements IStreamOpener {
    public static DelegatingFileStreamOpener[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = DelegatingFileStreamOpener.class;

    private final List<IStreamOpener> m_Openers = new ArrayList<IStreamOpener>();

    public DelegatingFileStreamOpener(IStreamOpener... openers) {
        for (int i = 0; i < openers.length; i++) {
            IStreamOpener opener = openers[i];
            addOpener(opener);
        }
    }

    /**
     * add at the beginning to override
     *
     * @param added
     */
    public void addOpener(IStreamOpener added) {
        if (m_Openers.size() == 0)
            m_Openers.add(added);
        else
            m_Openers.add(0, added);
    }

    /**
     * add at the end
     *
     * @param added
     */
    public void addLastOpener(IStreamOpener added) {
        m_Openers.add(added);
    }


    public void removeOpener(IStreamOpener removed) {
        m_Openers.remove(removed);
    }

    public IStreamOpener[] getOpeners() {
        return m_Openers.toArray(new IStreamOpener[0]);
    }

    /**
     * open a file from a string
     *
     * @param fileName  string representing the file
     * @param otherData any other required data
     * @return possibly null stream
     */
    @Override
    public InputStream open(String fileName, Object... otherData) {
        // try all openers in order take the first success
        for (IStreamOpener opener : m_Openers) {
            final InputStream ret = opener.open(fileName, otherData);
            if (ret != null)
                return ret;
        }
        return null;
    }
}
