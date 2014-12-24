package org.systemsbiology.hadoop;

import java.io.*;
import java.net.*;

/**
 * org.systemsbiology.hadoop.StreamOpeners
 *
 * @author Steve Lewis
 * @date Mar 8, 2011
 */
public class StreamOpeners
{
    public static StreamOpeners[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = StreamOpeners.class;

    public static final String RESOURCE_BASE = "res://";

    public static class ResourceStreamOpener implements IStreamOpener
    {
        private final Class m_Base;

        public ResourceStreamOpener(Class pBase)
        {
            m_Base = pBase;
        }

        public ResourceStreamOpener()
        {
            m_Base = getClass();
        }

        public Class getBase()
        {
            return m_Base;
        }

        /**
         * open a file from a string
         *
         * @param fileName  string representing the file
         * @param otherData any other required data
         * @return possibly null stream
         */
        @Override
        public InputStream open(String fileName, Object... otherData)
        {
            if(fileName.startsWith(RESOURCE_BASE)) {
                InputStream ret = getBase().getResourceAsStream(fileName.substring(RESOURCE_BASE.length()));
                return ret;
            }
            return null;
        }
    }

    /**
     * open a Stream from a URI
     */
    public static  class URIStreamOpener implements IStreamOpener
    {


        /**
         * open a file from a string
         *
         * @param fileName  string representing the file
         * @param otherData any other required data
         * @return possibly null stream
         */
        @Override
        public InputStream open(String fileName, Object... otherData)
        {
            try {
                URL val = new URL(fileName);
                return val.openStream();
            }
            catch (IOException e) {
                return null;
            }
        }
    }

}
