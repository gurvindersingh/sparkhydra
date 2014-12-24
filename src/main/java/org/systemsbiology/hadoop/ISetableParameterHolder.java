package org.systemsbiology.hadoop;



/**
 * org.systemsbiology.hadoop.ISetableParameterHolder
 *
 * @author Steve Lewis
 * @date 8/19/13
 */
public interface ISetableParameterHolder extends IParameterHolder {

    /**
     * set a parameter value
     * @param key  !null key
     * @param value  !null value
     */
    public void setParameter(String key,String value);




    public String[] getUnusedKeys();
}
