package org.systemsbiology.xtandem.hadoop;

import java.util.*;

/**
 * org.systemsbiology.xtandem.hadoop.HadoopConfigurationPropertySet
 * User: steven
 * Date: 5/31/11
 */
public class HadoopConfigurationPropertySet {
    public static final HadoopConfigurationPropertySet[] EMPTY_ARRAY = {};

    private final Map<String, HadoopConfigurationProperty> m_HadoopProperty = new HashMap<String, HadoopConfigurationProperty>();

    public HadoopConfigurationPropertySet() {
    }

    public HadoopConfigurationPropertySet(Collection<HadoopConfigurationProperty> values) {
        this();
        addProperties(values);
    }

    public void addProperties(HadoopConfigurationPropertySet values) {
           addProperties(values.m_HadoopProperty.values());
      }

    public void addProperties(Collection<HadoopConfigurationProperty> values) {
            for(HadoopConfigurationProperty prop : values)  {
                addHadoopProperty(prop.getName(),prop);
            }
      }

    public void addHadoopProperty(String key, HadoopConfigurationProperty added) {
        m_HadoopProperty.put(key, added);
    }


    public void removeHadoopProperty(String removed) {
        m_HadoopProperty.remove(removed);
    }

    public HadoopConfigurationProperty[] getHadoopProperties() {
        return m_HadoopProperty.values().toArray(new HadoopConfigurationProperty[0]);
    }

    public HadoopConfigurationProperty getHadoopProperty(String key) {
        return m_HadoopProperty.get(key);
    }

}
