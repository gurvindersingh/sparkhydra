package org.systemsbiology.hadoop;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.AttributeParameterHolder
 * an inplementation if  IParameterHolder  wherte the input is a line of the form
 * name="value" as you would find in the attributes of an xml tag
 *
 * @author Steve Lewis
 *         Base class for a ParameterHolder -
 */
public class AttributeParameterHolder implements IParameterHolder {
    public static AttributeParameterHolder[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = AttributeParameterHolder.class;

    private final String m_Line;

    public AttributeParameterHolder(final String pLine) {
        m_Line = pLine;
    }

    /**
     * open a file from a string
     *
     * @param fileName  string representing the file
     * @param otherData any other required data
     * @return possibly null stream
     */
    @Override
    public InputStream open(final String fileName, final Object... otherData) {
        return null;
    }

    /**
     * return a parameter configured in  default parameters
     *
     * @param key !null key
     * @return possibly null parameter
     */
    public String getParameter(String key) {
        String txt = key + "=\"";
        int index = m_Line.indexOf(txt);
        if (index == -1)
            return null;
        index += txt.length();
        int index2 = m_Line.indexOf("\"", index);
        if (index2 == -1)
            return null;
        return m_Line.substring(index, index2);
    }


    /**
     * get all keys of the foro key , key 1, key 2 ...
     *
     * @param key !null key
     * @return non-null array
     */
    @Override
    public String[] getIndexedParameters(String key) {
        List<String> holder = new ArrayList<String>();
        String value = getParameter(key);
        int index = 1;
        while (value != null) {
            holder.add(value);
            value = getParameter(key + " " + index++);
        }

        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * return a parameter configured in  default parameters
     *
     * @param key !null key
     * @return possibly null parameter
     */
    public String getParameter(String key, String defaultVal) {
        final String s = getParameter(key);
        if (s == null)
            return defaultVal;
        return s;
    }

    /**
     * return all keys as an array
     *
     * @return !null key array
     */
    @Override
    public String[] getParameterKeys() {
        throw new UnsupportedOperationException("We do not support this");
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Integer getIntParameter(String key) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return null;
        return new Integer(val);
    }
    /**
      * access a parameter from a parameter map
      *
      * @param key !null key
      * @return possibly null parameter
      */
     @Override
     public Long getLongParameter(String key) {
         String val = getParameter(key);
         if (val == null || "".equals(val))
             return null;
         return new Long(val);
     }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Boolean getBooleanParameter(String key) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return null;
        if ("yes".equalsIgnoreCase(val))
            return Boolean.TRUE;
        if ("no".equalsIgnoreCase(val))
            return Boolean.FALSE;
        if ("true".equalsIgnoreCase(val))
            return Boolean.TRUE;
        if ("false".equalsIgnoreCase(val))
            return Boolean.FALSE;
        throw new IllegalArgumentException("value must be yes or no or true or false - not " + val);
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Float getFloatParameter(String key) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return null;
        return new Float(val);
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Double getDoubleParameter(String key) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return null;
        return new Double(val);
    }

    /**
     * return an enum from the value of the parameter
     *
     * @param key !nulkl key
     * @param cls !null expected class
     * @param <T> type of enum
     * @return possibly null value - null says parameter does not exist
     */
    @Override
    public <T extends Enum<T>> T getEnumParameter(String key, Class<T> cls) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return null;
        return Enum.valueOf(cls, val);
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Integer getIntParameter(String key, int defaultValue) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return defaultValue;
        return new Integer(val);
    }

    /**
        * access a parameter from a parameter map
        *
        * @param key !null key
        * @return possibly null parameter
        */
       @Override
       public Long getLongParameter(String key, long defaultValue) {
           String val = getParameter(key);
           if (val == null || "".equals(val))
               return defaultValue;
           return new Long(val);
       }


    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Boolean getBooleanParameter(String key, boolean defaultValue) {
        String val = getParameter(key);
        if (val == null)
            return defaultValue;
        if ("yes".equalsIgnoreCase(val))
            return Boolean.TRUE;
        if ("no".equalsIgnoreCase(val))
            return Boolean.FALSE;
        if ("true".equalsIgnoreCase(val))
            return Boolean.TRUE;
        if ("false".equalsIgnoreCase(val))
            return Boolean.FALSE;
        throw new IllegalArgumentException("value must be yes or no or true or false - not " + val);
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Float getFloatParameter(String key, float defaultValue) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return defaultValue;
        try {
            return new Float(val);
        } catch (NumberFormatException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Double getDoubleParameter(String key, double defaultValue) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return defaultValue;
        return new Double(val);
    }

    /**
     * return an enum from the value of the parameter
     *
     * @param key !nulkl key
     * @param cls !null expected class
     * @param <T> type of enum
     * @return possibly null value - null says parameter does not exist
     */
    @Override
    public <T extends Enum<T>> T getEnumParameter(String key, Class<T> cls, T defaultValue) {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return defaultValue;
        return Enum.valueOf(cls, val);
    }


}
