package com.lordjoe.lib.xml;
/**
* interface saying the object can handle an XML 
* tag 
*/
public interface ITagHandler
{
    /** 
    * This returns the object which will handle the tag - the handler
    * may return itself or create a sub-object to manage the tag
    * @param TagName non-null name of the tag
    * @param attributes non-null array of name-value pairs
    * @return possibly null handler
    */
    public Object handleTag(String TagName,NameValue[] attributes);
}
