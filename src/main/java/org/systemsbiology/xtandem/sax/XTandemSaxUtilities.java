package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.xml.sax.*;

import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.XTandemSaxUtilities
 * User: steven
 * Date: Jan 3, 2011
 */
public class XTandemSaxUtilities {
    public static final XTandemSaxUtilities[] EMPTY_ARRAY = {};


    public static void limitTagCount(String filein,String fileOut,final String initTag,String tag,int maxCount)
    {
        TagLimitingParser th = new TagLimitingParser( initTag,null,fileOut,tag,maxCount );
         XMLUtilities.parseFile(new File(filein), th);
    }
    /**
      * code to return an attribute as an integer the attribute must exist
      * @param name  !null name
      * @param attr !null attribute list
      * @return value
      */
     public static String getRequiredAttribute(String name, Attributes attr)
     {
         String value = attr.getValue(name);
         if(value != null)
             return value;
         value = attr.getValue(name.toLowerCase());
           if(value != null)
               return value;
         value = attr.getValue(name.toUpperCase());
           if(value != null)
               return value;
         throw new IllegalArgumentException("Cannot find attribute " + name);
      }

    /**
     * code to return an attribute as an integer the attribute must exist
     * @param name  !null name
     * @param attr !null attribute list
     * @return value
     */
    public static int getRequiredIntegerAttribute(String name, Attributes attr)
    {
        String value = getRequiredAttribute(name,attr);
        return Integer.parseInt(value); 
    }

    /**
     * code to return an attribute as a double the attribute must exist
     * @param name  !null name
     * @param attr !null attribute list
     * @return value
     */
    public static double getRequiredDoubleAttribute(String name, Attributes attr)
    {
        String value = getRequiredAttribute(name,attr);
        try {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException e) {
            return -1 ;

        }

    }

    /**
     * code to return an attribute as a float the attribute must exist
     * @param name  !null name
     * @param attr !null attribute list
     * @return value
     */
    public static float getRequiredFloatAttribute(String name, Attributes attr)
    {
        String value =  getRequiredAttribute(name,attr);
        return Float.parseFloat(value); 
        
    }

    /**
     * code to return an attribute as a boolean the attribute must exist
     * @param name  !null name
     * @param attr !null attribute list
     * @return value
     */
    public static boolean getRequiredBooleanAttribute(String name, Attributes attr)
    {
        String value =  getRequiredAttribute(name,attr);
        return Boolean.parseBoolean(value);

    }

    /**
     * code to return an attribute as an enum the attribute must exist
     * @param name  !null name
     * @param attr !null attribute list
     * @param mapping !null map from Strings to enums
     * @return value
     */
    public static  <T extends Enum> T   getRequiredEnumAttribute(String name, Attributes attr, Map<String,T> mapping)
    {
        String value =  getRequiredAttribute(name,attr);
        return mapping.get(value);

    }

    /**
     * code to return an attribute as an enum the attribute must exist
     * @param name  !null name
     * @param attr !null attribute list
     * @param mapping !null map from Strings to enums
     * @return value
     */
    public static  <T extends Enum> T getRequiredEnumAttribute(String name, Attributes attr,Class<T> cls)
    {
        String value;
        try {
            value =  getRequiredAttribute(name,attr);;
            return (T)Enum.valueOf(cls,value );
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static Integer getIntegerAttribute(String name, Attributes attr,int defaultVal)
    {
        String value = attr.getValue(name);
        if(value == null)
            return defaultVal;
        return Integer.parseInt(value); 
    }
    
    public static Double getDoubleAttribute(String name, Attributes attr,double defaultVal)
    {
        String value = attr.getValue(name);
        if(value == null)
            return defaultVal;
        return Double.parseDouble(value); 
        
    }
    public static Float getFloatAttribute(String name, Attributes attr,float defaultVal)
    {
        String value = attr.getValue(name);
        if(value == null)
            return defaultVal;
        return Float.parseFloat(value); 
        
    }
    public static Boolean getBooleanAttribute(String name, Attributes attr,boolean defaultVal)
    {
        String value = attr.getValue(name);
        if(value == null)
            return defaultVal;
        return Boolean.parseBoolean(value); 
        
    }

    /**
     * code to return an attribute as an enum the attribute must exist
     * @param name  !null name
     * @param attr !null attribute list
     * @param mapping !null map from Strings to enums
     * @return value
     */
    public static   <T extends Enum> T  getEnumAttribute(String name, Attributes attr, Map<String,T> mapping)
    {
        String value = attr.getValue(name);
        if(value == null)
            return null;
        return mapping.get(value);

    }

    /**
     * code to return an attribute as an enum the attribute must exist
     * @param name  !null name
     * @param attr !null attribute list
     * @param mapping !null map from Strings to enums
     * @return value
     */
    public static   <T extends Enum> T  getEnumAttribute(String name, Attributes attr,Class<T> cls,T defaultValue)
    {
        String value = attr.getValue(name);
        if(value == null)
            return defaultValue;
        try {
            final T t = (T) Enum.valueOf(cls, value);
            return t;
        }
        catch (IllegalArgumentException e) {
            // try2 - case does not really matter
            try {
                Method m = cls.getMethod("values");
                Object[] items = (Object[])m.invoke(null);
                for (int i = 0; i < items.length; i++) {
                    Object item = items[i];
                    final String itemName = item.toString();
                    if(value.equalsIgnoreCase(itemName))
                        return (T)item;
                }
                return null;
            }
            catch (NoSuchMethodException e1) {
                throw new RuntimeException(e1);
            }
            catch (IllegalAccessException e1) {
                throw new RuntimeException(e1);
            }
            catch (InvocationTargetException e1) {
                throw new RuntimeException(e1);
            }
        }

    }


}
