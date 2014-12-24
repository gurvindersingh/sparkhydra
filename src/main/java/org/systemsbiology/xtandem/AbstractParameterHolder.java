package org.systemsbiology.xtandem;

import org.systemsbiology.hadoop.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.AbstractParameterHolder
 *    This class holds a set of name value pairs - held as
 *     strings but with methods to treat values as ints, doubles or enums as
 *     well as Strings - it is used to handle many of the parameters used by
 *     X!Tandem but in other places as well
 * @author Steve Lewis
  * Base class for a ParameterHolder -
 */
public abstract class AbstractParameterHolder implements ISetableParameterHolder
{
    public static AbstractParameterHolder[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = AbstractParameterHolder.class;
    private final Map<String, String> m_Parameters = new HashMap<String, String>();
    private final Set<String> m_UsedKeys = new HashSet<String>();


    protected Map<String, String> getParametersMap()
    {
        return m_Parameters;
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
     * set a parameter value
     * @param key  !null key
     * @param value  !null value
     */
    public void setParameter(String key,String value)
    {
        m_Parameters.put(key,value);
    }

    /**
       * return a parameter configured in  default parameters
       *
       * @param key !null key
       * @return possibly null parameter
       */
      public String getParameter(String key)
      {
          m_UsedKeys.add(key);
          String s = m_Parameters.get(key);
          if(s != null)
              s = s.trim();
          return s;
      }


    public String[] getUnusedKeys() {
        Set<String> unusedKeys = new HashSet<String>(m_Parameters.keySet());
        unusedKeys.removeAll(m_UsedKeys);
        String[] ret = unusedKeys.toArray(new String[unusedKeys.size()]);
        Arrays.sort(ret);
        return ret;
    }

    /**
     * get all keys of the foro key , key 1, key 2 ...
     *
     * @param key !null key
     * @return non-null array
     */
    @Override
    public String[] getIndexedParameters(String key)
    {
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
    public String getParameter(String key, String defaultVal)
    {
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
    public String[] getParameterKeys()
    {
        final String[] strings = m_Parameters.keySet().toArray(new String[m_Parameters.size()]);
        Arrays.sort(strings);
        return strings;
    }

    /**
     * access a parameter from a parameter map
     *
     * @param key !null key
     * @return possibly null parameter
     */
    @Override
    public Integer getIntParameter(String key)
    {
        String val = getParameter(key);
        if (val == null  || "".equals(val))
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
    public Boolean getBooleanParameter(String key)
    {
        String val = getParameter(key);
        if (val == null  || "".equals(val))
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
    public Float getFloatParameter(String key)
    {
        String val = getParameter(key);
        if (val == null  || "".equals(val))
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
    public Double getDoubleParameter(String key)
    {
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
    public <T extends Enum<T>> T getEnumParameter(String key, Class<T> cls)
    {
        String val = getParameter(key);
        if (val == null  || "".equals(val) )
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
    public Integer getIntParameter(String key, int defaultValue)
    {
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
    public Boolean getBooleanParameter(String key, boolean defaultValue)
    {
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
    public Float getFloatParameter(String key, float defaultValue)
    {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return defaultValue;
        try {
            return new Float(val);
        }
        catch (NumberFormatException e) {
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
    public Double getDoubleParameter(String key, double defaultValue)
    {
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
    public <T extends Enum<T>> T getEnumParameter(String key, Class<T> cls, T defaultValue)
    {
        String val = getParameter(key);
        if (val == null || "".equals(val))
            return defaultValue;
        return Enum.valueOf(cls, val);
    }


}
