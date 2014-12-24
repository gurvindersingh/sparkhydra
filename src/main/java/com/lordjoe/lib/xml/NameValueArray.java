package com.lordjoe.lib.xml;

import com.lordjoe.utilities.*;
import java.io.Serializable;

/**
 * Most useful for reading in XML Data consisting of name- value pairs
 * com.lordjoe.lib.xml.NameValueArray
 */
public class NameValueArray implements Serializable,ITagHandler 
{
	private NameValue[] m_Items = {};
	private transient String m_LastTag;
	public NameValueArray() {
	}
	
	public NameValue[] getItems() {
        NameValue[] ret = new NameValue[m_Items.length];
          System.arraycopy(m_Items, 0, ret, 0, ret.length);
         return ret;
 	}
	
	public void addItem(NameValue added) {
	    m_Items = (NameValue[])Util.addToArray(m_Items, added);
	}
	
    /** 
    * This returns the object which will handle the tag - the handler
    * may return itself or create a sub-object to manage the tag
    * @param TagName non-null name of the tag
    * @param attributes non-null array of name-value pairs
    * @return possibly null handler
    */
    public Object handleTag(String TagName,NameValue[] attributes)
    {
        if(TagName.equals("NameValue")) {
            NameValue added = new NameValue();
            added.m_Name = XMLUtil.findRequiredNameValue("name",attributes).m_Value.toString();
            added.m_Value = XMLUtil.findRequiredNameValue("value",attributes).m_Value.toString();
            
            addItem(added);
        }
        return(null);
    }
    
//    public static void main(String[] args) {
//        String TagsToClasses = "conf/TagsToClasses.conf";
//        FileUtilities.assertFileExists(TagsToClasses);
//	    ClassMapper TheMapper = ClassMapper.buildFromFile(TagsToClasses);
//        TheMapper.registerTagClasses();
//        NameValueArray test = (NameValueArray)XMLSerializer.parseFile("TestValues.xml");
//        test = null;
//    }
	
}
