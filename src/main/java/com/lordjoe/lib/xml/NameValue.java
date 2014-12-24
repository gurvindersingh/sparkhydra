package com.lordjoe.lib.xml;

import com.lordjoe.utilities.*;
import java.io.Serializable;
import java.util.*;

/**
 * Data class for name value pairs
 * system
 */
public class NameValue  implements Serializable {
    public static final NameValue[] EMPTY_ARRAY = {};
    public static final Class THIS_CLASS = NameValue.class;
   public static final Comparator VALUE_COMPARATOR = new ValueComparator();;

    public static class ValueComparator implements Comparator,Serializable
    {
        public int compare(Object o1, Object o2)
        {
            java.lang.Comparable n1 = (java.lang.Comparable)(((NameValue)o1).getValue());
            java.lang.Comparable n2 = (java.lang.Comparable)(((NameValue)o2).getValue());
            if(n1 == null) {
                return(n2 == null ? 0 : 1) ;
            }
            return(n1.compareTo(n2));
         }
    }

    public static Map buildMap(NameValue[] items)
    {
        Map ret = new HashMap();
        for(int i = 0; i < items.length; i++)
            ret.put(items[i].m_Name,items[i].m_Value);
        return(ret);
    }
    
    public static NameValue[] fromMap(Map Values)
    {
        NameValue[] items = new NameValue[Values.size()];
        String[] keys = Util.getKeyStrings(Values);
        for(int i = 0; i < keys.length; i++) {
            items[i] = new NameValue(keys[i],Values.get(keys[i]));
        }
        return(items);
    }
    
    public NameValue() {}
    public NameValue(String name,Object value)
    {
        m_Name = name;
        m_Value = value;
    }
    
    
	public static final NameValue[] NULL_ARRAY = {};
	public String					m_Name;
	public Object					m_Value;
	
	public String getName() { return(m_Name); }
	public Object getValue() { return(m_Value); }
	public void setName(String in) { m_Name = in; }
	public void setValue(Object in) { m_Value = in; }
    public int compareTo(Object in) {
        int ret = getName().compareTo(((NameValue) in).getName());
        if(ret != 0)
            return ret;
        return ret;
    }

}
