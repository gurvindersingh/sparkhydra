
package com.lordjoe.lib.xml;

import com.lordjoe.utilities.*;
import org.w3c.dom.*;
import org.w3c.dom.CharacterData;
import org.xml.sax.*;
import org.xml.sax.helpers.*;

import javax.xml.parsers.*;
import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

/**
 * Handy routines for dealing with XML.
 * Used by TestXMLTransportJFrame
 * 
 * @author Steve Lewis
 */
@SuppressWarnings(value = "deprecated")
public abstract class XMLUtil {

    public static final ITagHandler IGNORE_TAG_HANDLER = new IgnoreXMLTag();

    private static SAXParserFactory getSaxParserFactory()
    {
        try {
          return SAXParserFactory.newInstance();
        }
        catch (Exception e) {
          return   (SAXParserFactory)new   com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl();
         }
     }
//
//    public static final SAXParserFactory SAX_FACTORY = getSaxParserFactory();

    /**
    * string to make a tag as having been handled
    */
    public static final String TAG_HANDLED = "<<handled>>";
    
    /** Default parser name. */
    public static final String 
        DEFAULT_PARSER_NAME = "org.apache.xerces.parsers.SAXParser";
    /** Default parser property. */
    public static final String 
        DEFAULT_PARSER_PROPERTY = "org.xml.sax.parser";
    
    
    /**
    * Handle the details of creating an HTMLParser
    * @return non-null Parser;
    */
    @SuppressWarnings(value = "deprecated")
    public static Parser getHTMLParser()
    {
        throw new UnsupportedOperationException("Fix This");
    }
     /**
    * Handle the details of creating an XMLParser
    * @return non-null Parser;
    */
    public static DocumentBuilder getDocumentBuilder()
    {
        DocumentBuilderFactory TheFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder ret;
        try {
            ret = TheFactory.newDocumentBuilder();
        }
        catch(ParserConfigurationException ex) {
           throw new RuntimeException(ex);
        }
        return(ret);
    }


    @SuppressWarnings("UnusedDeclaration")
    public static float extractFloatValue(String attr, String pStr) {
        String s = extractStringValue(attr, pStr);
        if(s.contains(","))
            s = s.replace(",","");
        return Float.parseFloat(s);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static double extractDoubleValue(String attr, String pStr) {
        String s = extractStringValue(attr, pStr);
        if(s.contains(","))
            s = s.replace(",","");
        return Double.parseDouble(s);
    }


    @SuppressWarnings("UnusedDeclaration")
    public static int extractIntegerValue(String attr, String pStr) {
        String s = extractStringValue(attr, pStr);
        if(s.contains(","))
            s = s.replace(",","");
        return Integer.parseInt(s);
    }


    @SuppressWarnings("UnusedDeclaration")
    public static boolean extracBooleanValue(String attr, String pStr) {
        String s = extractStringValue(attr, pStr);
        return "TRUE".equalsIgnoreCase(s);
    }


    public static String extractStringValue(String attr, String pStr) {
        String testStr = attr + "=\"";
        int index1 = pStr.indexOf(testStr);
        if (index1 == -1)
            throw new IllegalArgumentException("bad string " + pStr);
        pStr = pStr.substring(index1 + testStr.length());
        int index2 = pStr.indexOf("\"");
        if (index2 == -1)
            throw new IllegalArgumentException("bad string " + pStr);
        pStr = pStr.substring(0, index2);
        return  pStr;
    }

   
    /**
    * Handle the details of creating an XMLParser
    * @return non-null Parser;
    */
    @SuppressWarnings(value = "deprecated")
    public static Parser getXMLParser()
    {
        Parser TheParser = null;
        String DefaultParser = System.getProperty(DEFAULT_PARSER_PROPERTY);
        if(DefaultParser == null) {
            System.setProperty(DEFAULT_PARSER_PROPERTY,DEFAULT_PARSER_NAME);
            DefaultParser = System.getProperty(DEFAULT_PARSER_PROPERTY);
            assert(DefaultParser != null);
        }
            
        try {
            TheParser = ParserFactory.makeParser();
        }
        catch (Exception ex) {
           throw new RuntimeException(ex);
        }
        if (TheParser == null) {
            System.err.print(" No default parser.");
            }
      /*  else {
            System.err.println();
            System.err.print("                   Default parser: "+TheParser.getClass().getUrl());
            }
            */
        return(TheParser);
    }

    /**
     * Handle the details of creating an XMLParser
     * @return non-null Parser;
     */
     public static SAXParser getSAXParser()
     {
         SAXParser TheParser = null;

         try {
             SAXParserFactory saxParserFactory = getSaxParserFactory();
             saxParserFactory.setNamespaceAware(false);
             saxParserFactory.setValidating(false);
             TheParser = saxParserFactory.newSAXParser();
         }
         catch (Exception ex) {
            throw new RuntimeException(ex);
         }
         if (TheParser == null) {
             System.err.print(" No default parser.");
             }
       /*  else {
             System.err.println();
             System.err.print("                   Default parser: "+TheParser.getClass().getUrl());
             }
             */
         return(TheParser);
     }

    
    @SuppressWarnings("UnusedDeclaration")
    public static Document buildDOM(String FileName) throws SAXException
    {
        try {
            DocumentBuilder TheBuilder = getDocumentBuilder();
            return(TheBuilder.parse(FileName));
        }
        catch(IOException ex) {
            throw new IllegalArgumentException("Huh - should not happen");
        }
    }
    
	/**
	* find a NameValue fron a collection given the Name
	* @param Name - desired name non-null case insensitive
	* @return possibly-null NameValue where ret.m_Name equals Name
	*/
    public static NameValue findNameValue(String Name,NameValue[] attributes)
    {
    	for (int i = 0; i < attributes.length; i++) {
		    if (attributes[i].m_Name.equalsIgnoreCase(Name)) {
			    return(attributes[i]);
		    } 
		}
		return(null);
	}
	
	/**
	* find a NameValue fron a collection given the Name
	* @param Name - desired name non-null case insensitive
	* @return non-null NameValue where ret.m_Name equals Name
	*/
    public static NameValue findRequiredNameValue(String Name,NameValue[] attributes)
    {
    	NameValue ret = findNameValue(Name,attributes);
    	if(ret == null)
    	    throw new RequiredAttributeNotFoundException(Name,attributes);
		return(ret);
	}

    public static void markAsHandled(NameValue nv)
    {
       nv.m_Name = XMLUtil.TAG_HANDLED; // mark handled
    }

    /**
      * return the value of a required arrtubute as a string marking the attribute as
      * hanfled
      * @param Name non-null name of the required attribute
      * @param attributes non-null array of attributes
      * @return non-null String
      */
     public static String handleRequiredNameValueString(String Name,NameValue[] attributes)
     {
         NameValue nv = findRequiredNameValue(Name,attributes);
         markAsHandled(nv);
          String idName = (String) nv.getValue();
          return idName;
     }
    /**
      * return the value of an optional attrubute as a string marking the attribute as
      * handled
      * @param Name non-null name of the required attribute
      * @param attributes non-null array of attributes
      * @return possibly null String
      */
     public static String handleOptionalNameValueString(String Name,NameValue[] attributes)
     {
         NameValue nv = findNameValue(Name,attributes);
         if(nv == null)
            return null;
         markAsHandled(nv);
          String idName = (String) nv.getValue();
          return idName;
     }

    /**
        * return the value of a required arrtubute as an int marking the attribute as
        * hanfled
        * @param Name non-null name of the required attribute
        * @param attributes non-null array of attributes
        * @return resultant int
        */
    @SuppressWarnings("UnusedDeclaration")
       public static boolean handleRequiredNameValueBoolean(String Name,NameValue[] attributes)
       {
           String s = handleRequiredNameValueString( Name,attributes);
           if("true".equalsIgnoreCase(s))
               return true;
           if("false".equalsIgnoreCase(s))
               return false;
           throw new IllegalStateException(s + " must be true or false");
       }
    /**
            * return the value of a required arrtubute as an int marking the attribute as
            * hanfled
            * @param Name non-null name of the required attribute
            * @param attributes non-null array of attributes
            * @return resultant int
            */
           public static long handleRequiredNameValueLong(String Name,NameValue[] attributes)
           {
               String s = handleRequiredNameValueString( Name,attributes);
               return Long.parseLong(s);
           }
    /**
            * return the value of a required arrtubute as an int marking the attribute as
            * hanfled
            * @param Name non-null name of the required attribute
            * @param attributes non-null array of attributes
            * @return resultant int
            */
    @SuppressWarnings("UnusedDeclaration")
           public static double handleRequiredNameValueDouble(String Name,NameValue[] attributes)
           {
               String s = handleRequiredNameValueString( Name,attributes);
               return Double.parseDouble(s);
           }
     /**
           * return the value of a required arrtubute as an int marking the attribute as
           * hanfled
           * @param Name non-null name of the required attribute
           * @param attributes non-null array of attributes
           * @return resultant int
           */
          public static int handleRequiredNameValueInt(String Name,NameValue[] attributes)
          {
              String s = handleRequiredNameValueString( Name,attributes);
              return Integer.parseInt(s);
          }
      /**
         * return the value of a required arrtubute as an int marking the attribute as
         * hanfled
         * @param Name non-null name of the required attribute
         * @param attributes non-null array of attributes
         * @return resultant int
         */
      @SuppressWarnings("UnusedDeclaration")
         public static int[] handleRequiredNameValueIntArray(String Name,NameValue[] attributes)
        {
            String s = handleRequiredNameValueString( Name,attributes);
            String[] tokens = s.split(",");
            int[] ret = new int[tokens.length];
            for (int i = 0; i < tokens.length; i++)
            {
                ret[i] = Integer.parseInt(tokens[i]);        
            }
            return ret;
        }
//    /**
//     * return the value of a required arrtubute as an int marking the attribute as
//     * hanfled
//     * @param Name non-null name of the required attribute
//     * @param attributes non-null array of attributes
//     * @return resultant int
//     */
//    public static EnumeratedType handleRequiredEnumByName(String Name,NameValue[] attributes,EnumeratedType exemplar)
//    {
//        String s = handleRequiredNameValueString( Name,attributes);
//        EnumeratedType enumeratedTypeByName = exemplar.getEnumeratedTypeByName(s);
//        if(enumeratedTypeByName != null)
//           return enumeratedTypeByName;
//        else
//           throw new IllegalArgumentException("Unknown name of " +
//              exemplar.getClass().getName() + " \'" +  s + "\'"
//           );
//    }
    /**
     * return the value of a required arrtubute as an int marking the attribute as
     * hanfled
     * @param Name non-null name of the required attribute
     * @param attributes non-null array of attributes
     * @return resultant int
     */
    public static Enum  handleRequiredEnumByName(String Name,NameValue[] attributes,Class exemplar)
    {
        if(!exemplar.isEnum())
            throw new IllegalArgumentException("Class must be an enum"); 
         String s = handleRequiredNameValueString( Name,attributes);
         return Enum.valueOf(exemplar,s);
    }
//       /**
//       * return the value of a required arrtubute as an int marking the attribute as
//       * hanfled
//       * @param Name non-null name of the required attribute
//       * @param attributes non-null array of attributes
//       * @return resultant int
//       */
//      public static EnumeratedType handleRequiredEnumByValue(String Name,NameValue[] attributes,EnumeratedType exemplar)
//      {
//          String s = handleRequiredNameValueString( Name,attributes);
//          EnumeratedType enumeratedTypeByName = exemplar.getEnumeratedTypeByValue(s);
//          if(enumeratedTypeByName != null) {
//             return enumeratedTypeByName;
//          }
//           else  {
//             enumeratedTypeByName = exemplar.getEnumeratedTypeByValue(s);
//             throw new IllegalArgumentException("Unknown value of " +
//                exemplar.getClass().getName() + " \'" +  s + "\'"
//             );
//          }
//      }
    /**
      * return the value of a required arrtubute as a string marking the attribute as
      * hanfled
     * @param ignored non-null list of tags to ignore
     * @param name non-null name of the required attribute
      * @param attributes non-null array of attributes
      * @return non-null String
      */
     public static boolean ignoreTagList(String[] ignored,String name,NameValue[] attributes)
     {
         for (int i = 0; i < ignored.length; i++)
         {
             String s = ignored[i];
             if(s.equalsIgnoreCase(name)) {
                 handleAllTags(attributes);
                 return true;
             }
         }
         return false;
     }

    /**
     * mark ignored attributes as handled
     * @param ignored
     * @param attributes
     */
    public static void ignoreAttributeList(String[] ignored,NameValue[] attributes)
     {
         for (int i = 0; i < ignored.length; i++)
         {
             String s = ignored[i];
             handleAllAttributes(s,attributes);
           }
       }

    /**
     * mark attributes with name tagName as handled
      * @param tagName
     * @param attributes
     */
    public static void handleAllAttributes(String tagName,NameValue[] attributes)
    {
        for (int i = 0; i < attributes.length; i++)
        {
            NameValue attribute = attributes[i];
            if(attribute.getName().equalsIgnoreCase(tagName))
                markAsHandled(attribute);
        }
    }

    public static void handleAllTags(NameValue[] attributes)
    {
        for (int i = 0; i < attributes.length; i++)
        {
            NameValue attribute = attributes[i];
            markAsHandled(attribute);
        }
    }


    /**
    *    length before we end a line
    */
    public static final int MAX_LINE_LENGTH = 70;
    /**
     * Pretty print our XML to System.out.
     * Our XML is generated as a single line of text.
     * These routines insert newlines and indents into that single line.
     * 
     * @param xml Text of the XML page to format.
     * @return Formated XML String.
     */
    public static void pp(String xml) {
        System.out.println(formatXML(xml));
    }

     /**
     * Format XML for display.
     * Our XML is generated as a single line of text.
     * These routines insert newlines and indents into that single line.
     * 
     * @param xml Text of the XML page to format.
     * @return Formated XML String.
     */
    public static String generateXML(Document base) {
        Element Start = base.getDocumentElement(); 
        StringBuilder sb = new StringBuilder(1024);
        generateXML(Start,sb,0);
        return(sb.toString());
        
    }
    /** Format XML for display.
     * Our XML is generated as a single line of text.
     * These routines insert newlines and indents into that single line.
     * 
     * @param xml Text of the XML page to format.
     * @return Formated XML String.
     */
    protected static void generateXML(Element base,StringBuilder sb,int indent)
    {
        int Start = sb.length();
        doIndent(sb,indent);
        String name = base.getNodeName();
        sb.append("<" + name + " ");
        handleNodeAttributes(base,sb,indent,sb.length() - Start);
        handleElements(base,sb,indent + 1);
        doIndent(sb,indent);
        sb.append("</" + name + ">\n");
    }
    
    protected static void handleNodeAttributes(Element base,StringBuilder sb,int indent,int LineChars)
    {
        NamedNodeMap items = base.getAttributes();
        int Start = sb.length();
        for(int i = 0; i < items.getLength(); i++) {
            Attr TheAttrubute = (Attr)items.item(i);
            LineChars = handleNodeAttribute(TheAttrubute,sb,indent,LineChars);
        }  
        sb.append(">\n");
    }
    
    protected static int handleNodeAttribute(Attr base,StringBuilder sb,int indent,int LineChars)
    {
        int Start = sb.length();
        String name = base.getNodeName();
        sb.append(name);
        sb.append("=\"");
        String value = base.getNodeValue();
        sb.append(makeXMLString(value));
        sb.append("\" ");
        int CurrentLength = LineChars + sb.length() - Start;
        if(CurrentLength > MAX_LINE_LENGTH) {
            sb.append("\n");
            CurrentLength = doIndent(sb,indent + 1);
            return(CurrentLength);
        }
        return(CurrentLength);
            
    }

	/**
	* convert a string mangled for XML into a legitimate string
	* @param in non-null string to convert
	* @return non-null concerted string
	*/
	public static String fromURLString(String in)
	{
		StringBuilder ret = new StringBuilder(in.length() + 20);
		for(int i = 0; i < in.length(); i++) {
		    char c = in.charAt(i);
		    switch(c) {
		        case '%' :
		            i = decodeHexChar(ret,in,i);
		            break;
			    default:
				    ret.append(c);
			}
		}
		return(ret.toString());
	}

    public static String makeURLString(String in)
	{
		StringBuilder ret = new StringBuilder(in.length() + 20);
		for(int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if( c > ' ' && c < 127 )
			{
			    switch(c) {
                    case '&' :
                    case '=' :
                    case '\"' :
                    case '\'' :
                    case '\\' :
					    ret.append("%" + Integer.toHexString((int)c));
					    break;
				    default:
					    ret.append(c);
			    }
			}
			else {
			    ret.append("%" + Integer.toHexString((int)c));
			}

		}
		return(ret.toString());
	}

	/**
	* convert a string mangled for XML into a legitimate string
	* @param in non-null string to convert
	* @return non-null concerted string
	*/
	public static String fromXMLString(String in) 
	{
		StringBuilder ret = new StringBuilder(in.length() + 20);
		for(int i = 0; i < in.length(); i++) {
		    char c = in.charAt(i);
		    switch(c) {
		        case '%' :
		            i = decodeHexChar(ret,in,i);
		            break;
		        case '&' :
		            i = decodeAmpChar(ret,in,i);
		            break;
			    default:
				    ret.append(c);
			}
		}
		return(ret.toString());
	}
	
	public static int decodeHexChar(StringBuilder sb,String in,int index)
	{
	    int end = in.indexOf(';',index);
        char added = 0;
	    if(end == -1 || end >  index + 3 ) {
            end = index + 2;
    //        String examine = in.substring(org.systemsbiology.couldera.training.index,org.systemsbiology.couldera.training.index + 10);
   //         System.out.println(examine);
            String test = in.substring(index + 1,end + 1);
              added = (char)Integer.valueOf(test,16).intValue();
        }
        else {
            String test = in.substring(index + 1,end);
             added = (char)Integer.valueOf(test,16).intValue();
        }
	    sb.append(added);
	    return(end + 1);
	}
	
	public static int decodeAmpChar(StringBuilder sb,String in,int index)
	{
	    int end = in.indexOf(';',index);
	    
	    String test = in.substring(index + 1,end);
	    if(test.equals("amp"))
	        sb.append('&');
	    else if(test.equals("quot"))
	        sb.append('\"');
	    else if(test.equals("gt"))
	        sb.append('>');
	    else if(test.equals("lt"))
	        sb.append('<');
	    else if(test.equals("apos"))
	        sb.append('\'');
	    else
	        throw new IllegalArgumentException("misunderstood XML Expression '" + test + "'");
	    return(end + 1);
	}
	
	public static String makeXMLString(String in) 
	{
        if(in == null)
            return "null";
        StringBuilder ret = new StringBuilder(in.length() + 20);
		for(int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if( c >= ' ' && c < 255 ) 
			{
			    switch(c) {
				    case '&' :
					    ret.append("&amp;");
					    break;
				    case '<' :
					    ret.append("&lt;");
					    break;
				    case '>' :
					    ret.append("&gt;");
					    break;
				    case '\'' :
					    ret.append("&apos;");
					    break;
				    case '\"' :
					    ret.append("&quot;");
					    break;
				    default:
					    ret.append(c);
			    }
			}
			else {
                ret.append("&#x" + Integer.toHexString((int)c) + ";");
			}
					
		}
		return(ret.toString());
	}
	
	
	
	
    
    
    protected static void handleElements(Element base,StringBuilder sb,int indent)
    {
        NodeList items = base.getChildNodes();
        for(int i = 0; i < items.getLength(); i++) {
            Node test = items.item(i);
            if(test.getNodeType() == Node.ELEMENT_NODE) {
                generateXML((Element)test,sb,indent);
            }
            if(test.getNodeType() == Node.TEXT_NODE ) {
                sb.append(test.getNodeValue());
            }
            if(test.getNodeType() == Node.CDATA_SECTION_NODE ) {
                sb.append(test.getNodeValue());
            }
        }
    }
    
    /**
        handle a document of the form 
        @param doc non-null document
            PropertyValue Name="Email"  Value="bb"  times n
        @return non-null hashtable where key is name string and 
        value is value string
    */
    public static Hashtable getPropertyValues(Document doc)
    {
        Element[] Items = XMLUtil.getElementsWithName(doc,"PropertyValue");
        Hashtable ret = new Hashtable();
        for(int i = 0; i < Items.length; i++)
            addPropertyValue(Items[i],ret);
        return(ret);
    }
     /**
        handle a document of the form 
        @param doc non-null document
            PropertyValue Name="Email"  Value="bb"  times n
        @return non-null hashtable where key is name string and 
        value is value string
    */
    public static void addPropertyValue(Element doc,Hashtable table)
    {
        String Name = doc.getAttribute("Name");
        String Value = doc.getAttribute("Value");
        if(Name == null || Name.length() == 0 ||
            Value == null || Value.length() == 0)
            return;
        table.put(Name,Value);
    }
   
    protected static int doIndent(StringBuilder sb,int indent)
    {
        for(int i = 0; i < indent; i++)
            sb.append("\t");
        return(indent); // number chars added
    }

   /**
     * Format XML for display.
     * Our XML is generated as a single line of text.
     * These routines insert newlines and indents into that single line.
     * 
     * @param xml Text of the XML page to format.
     * @return Formated XML String.
     */
    public static String formatXML(String xml) {
        return formatXML(xml, new Integer(0));
	}
	
   /**
     * Format XML for display.
     * Our XML is generated as a single line of text.
     * These routines insert newlines and indents into that single line.
     * 
     * @param xml Text of the XML page to format.
     * @param initalIndentLevel Indent level to start with 
     * @return Formated XML String.
     */
    public static String formatXML(String xml, Integer initialIndentLevel) {
        int indent = initialIndentLevel.intValue();
        String outString = "";
        for(int i=0; i < xml.length(); ++i) {
            char c = xml.charAt(i);
            if(c == '<') {
                //outString += '\n';
                if(xml.charAt(i+1) == '/') {
                    indent -= 1;
                }
                for(int j=0; j < indent; ++j) {
                    outString += '\t';
                }
                outString += c;
                if(xml.charAt(i+1) != '/') {
                    indent += 1;
                }
            } else if(c == '\n') {
                outString += c;
                for(int j=0; j < indent; ++j) {
                    outString += '\t';
                }
            } else if(c == '>') {
                outString += c;
                outString += '\n';
            } else {
                outString += c;
            }
        }
        return outString;
    }
    
    public static String exceptionToXML(Exception ex)
    {
        StringBuilder ret = new StringBuilder(2048);
        ret.append("<Error>\n");
        ret.append("    <Exception ");
        ret.append("  Class=\"");
        ret.append(ex.getClass().getName());
        ret.append("\" ");
        ret.append(">\n");
  //      ret.append(ex.toString());
      //  ret.append(Developer.getStackTrace(ex));
        ret.append("    </Exception>\n");
        ret.append("</Error>\n");
        
        return(ret.toString());
    }
//    /**
//    * This returns the class specified either as a
//    * class tag with a full class name or a
//    * tag tag with a tag to look up
//    * @param attributes non-null array of name-value pairs
//    * @param map non-null ClassMapper mapping tag to ClassName
//    * @return non-null instance of the class
//    */
//    public static Object buildClassFromTags(NameValue[] attributes)
//    {
//        return(buildClassFromTags(attributes,null)); // no tags are supplies so do not use
//    }
//    /**
//    * This returns the class specified either as a
//    * class tag with a full class name or a
//    * tag tag with a tag to look up
//    * @param attributes non-null array of name-value pairs
//    * @param map non-null ClassMapper mapping tag to ClassName
//    * @return non-null instance of the class
//    */
//    public static Object buildClassFromTags(NameValue[] attributes,ClassMapper map)
//    {
//        String className = null;
//	    NameValue classAttribute = XMLUtil.findNameValue("class",attributes);
//	        if(classAttribute != null) {
//    	        className = classAttribute.m_Value.toString();
//                markAsHandled(classAttribute);
//    	    }
//    	    if(className == null && map != null) {
//    	        NameValue tagAttribute = XMLUtil.findNameValue("tag",attributes);
//    	        if(tagAttribute == null)
//    	            tagAttribute = XMLUtil.findNameValue("strategy",attributes);
//	            if(tagAttribute != null) {
//	                String TagString = tagAttribute.m_Value.toString();
//	                Object test = map.getValue(TagString);
//	                if(test != null) {
//    	                className = test.toString();
//                        markAsHandled(tagAttribute);
//    	            }
//    	            else {
//    	                throw new IllegalStateException("Unknown class specified in tags -'" + TagString + "'" );
//    	            }
//    	        }
//    	    }
//    	    if(className == null)
//		        throw new IllegalStateException("No class specified in tags");
//
//	    // check for the class so as to throw meaningful error
//	    try {
//		    return(ClassUtilities.createInstance(className));
//	    } catch ( RuntimeException le) {
//		    throw new IllegalStateException("Class: "+ className +" cannot be loaded!");
//	    }
//	}

    /** Pretty print files specified on command line.  This is both a utillity and our test routine */
    public static void main(String []args) {
        if(args.length == 0) {
            System.out.println("Usage:\n\tXMLUtil <files>");
        } else {
            // Loop through files
            for(int i=0; i < args.length; ++i) {
                try {
                    File f = new File(args[i]);
                    FileInputStream fIn = new FileInputStream(f);
                    int pos = 0;
                    String xml = "";
                    // Loop through file data
                    while(fIn.available() > 0) {
                        byte []buf = new byte[fIn.available()];
                        pos += fIn.read(buf, pos, fIn.available());
                        xml += new String(buf);
                    }
                    // Display formatted XML
                    System.out.println(args[i] + ":");
                    System.out.println(formatXML(xml));
                } catch (IOException e) {
                    System.err.println("An I/O error has occured accessing " + args[i] + "\n" + e.getMessage());
                }
            }
        }
    }
    
    /**
        Insert the contents of a colletion into the XML describing an empty 
        instance
        Hack to COM objects
        @param XMLTarget - non-null original XML String
        @param XMLInsert - non-null XML to insert
        @param CollectionName - non-null name of the collection
     */   
    public static String insertXMLCollection(String XMLTarget,String XMLInsert,String CollectionName)
    {
        int index = XMLTarget.indexOf("<CDGJCollection");
        while(index > -1) {
            int endIndex = XMLTarget.indexOf(">",index);
            String test = XMLTarget.substring(index,endIndex);
            if(test.indexOf(CollectionName) > -1) {
                String S1 = XMLTarget.substring(0,endIndex + 1);
                String S2 = XMLTarget.substring(endIndex + 1);
                return(S1 + XMLInsert + S2);
             }
            index = XMLTarget.indexOf("<CDGJCollection",endIndex);   
                
        }
        throw new RuntimeException("Failure to find Collection");
      //  return(null);
    }
    /**
        Trim one layer of Tags in XML
        @param XMLIn - non-null original XML String
        @return new XML
     */   
    public static String trimTag(String XMLIn)
    {
        int StartIndex = XMLIn.indexOf("<");
        assert(StartIndex >= 0);
        StartIndex = XMLIn.indexOf(">",StartIndex);
        assert(StartIndex >= 0);
        int EndIndex = XMLIn.lastIndexOf(">");
        assert(EndIndex >= 0);
        EndIndex = XMLIn.lastIndexOf("<");
        assert(EndIndex >= 0);
        
        return(XMLIn.substring(StartIndex + 1,EndIndex));
    }
    
    /**
        Write all links if it has a CatalogID and recurse to contained ids
     */
    public static Element[] getElementsWithName(Document doc,String name)
    {
        NodeList nodes = doc.getElementsByTagName(name);
        int n = nodes.getLength();
        List holder = new ArrayList(n);
        for(int i = 0; i < n; i++) {
              Node item = nodes.item(i);
              if(item.getNodeType() == Node.ELEMENT_NODE)
                holder.add(item);
        }
        Element[] ret = new Element[holder.size()];
        holder.toArray(ret);
        return(ret);
    }
    /**
        Write all links if it has a CatalogID and recurse to contained ids
     */
    public static Element[] getElementsWithName(Element doc,String name)
    {
        NodeList nodes = doc.getElementsByTagName(name);
        int n = nodes.getLength();
        List holder = new ArrayList(n);
        for(int i = 0; i < n; i++) {
              Node item = nodes.item(i);
              if(item.getNodeType() == Node.ELEMENT_NODE)
                holder.add(item);
        }
        Element[] ret = new Element[holder.size()];
        holder.toArray(ret);
        return(ret);
    }
    
    /**
        Write all links if it has a CatalogID and recurse to contained ids
     */
    public static Element[] getChildrenWithName(Element doc,String name)
    {
        NodeList nodes = doc.getChildNodes();
        int n = nodes.getLength();
        List holder = new ArrayList(n);
        for(int i = 0; i < n; i++) {
              Node item = nodes.item(i);
              if(item.getNodeType() == Node.ELEMENT_NODE) {
                if(name.equalsIgnoreCase(item.getNodeName()))
                    holder.add(item);
              }
        }
        Element[] ret = new Element[holder.size()];
        holder.toArray(ret);
        return(ret);
    }
    /**
        retrieve all Strings from this node
     */
    public static String[] getElementText(Element doc)
    {
        List holder = new ArrayList();
        accumulateElementText(doc,holder);
        String[] ret = Util.collectionToStringArray(holder);
        return(ret);
   }
    
    /**
    * retrieve one Strings from this node
    */
    public static String getOneElementText(Element doc)
    {
        List holder = new ArrayList();
        accumulateElementText(doc,holder);
        if(holder.size() > 0)
            return((String)holder.get(0));
        else
            return(null);
   }
    
    /**
        retrieve all Strings from this node
     */
    public static void accumulateElementText(Element doc,List holder)
    {
        NodeList nodes = doc.getChildNodes();
        int n = nodes.getLength();
        for(int i = 0; i < n; i++) {
              addElementText(nodes.item(i),holder);
        }
    }
    
    protected static void addElementText(Node test,List holder)
    {
        int Type = test.getNodeType();
        switch(Type) {
          case Node.CDATA_SECTION_NODE :
          case Node.TEXT_NODE :
            String text = test.getNodeValue();
            if(isAlphaString(text))
                holder.add(text);
            return;
          case Node.ELEMENT_NODE :
            accumulateElementText((Element)test,holder);
          default:
            Type = 0;
        }
    }
    
    public static boolean isAlphaString(String s)
    {
        if(s == null) return(false);
        for(int i = 0; i < s.length(); i++) {
            if(Character.isLetterOrDigit(s.charAt(i)))
                return(true);
        }
        return(false);
    }
                
      public static String getProductImage(Element e)
     {
        return(getImageContaining(e,"images/product/"));
     }

    protected static String getImageContaining(Element e,String text)
     {
        String[] images = getElementImages(e);
        String TextUpper = text.toUpperCase();
        for(int i = 0; i < images.length; i++) {
            if(images[i].toUpperCase().indexOf(TextUpper) >= 0)
                return(images[i]);
        }
        return(null); // not found
     }
    
    /**
        Write all links if it has a CatalogID and recurse to contained ids
     */
    public static String[] getElementImages(Element doc)
    {
        NodeList nodes = doc.getElementsByTagName("IMG");
        int n = nodes.getLength();
        List holder = new ArrayList(n);
        for(int i = 0; i < n; i++) {
              addImageSource(nodes.item(i),holder);
        }
        String[] ret = Util.collectionToStringArray(holder);
        return(ret);
    }
    
    protected static void addImageSource(Node test,List holder)
    {
        if(test.getNodeType() != Node.ELEMENT_NODE)
            return;
        Element RealTest = (Element)test;
        Node SourceNode = RealTest.getAttributeNode("SRC");
        if(SourceNode == null)
            return;
        String text = SourceNode.getNodeValue();
        if(text == null || text.length() == 0)
          return;
        holder.add(text);
    }
     
    public static InputStream openURLStream(String in) throws IOException
    {
            URL inUrl = new URL(in);
            InputStream ret = inUrl.openStream();
            return(ret);
    }
    public static void report( SAXException except )
    {
	// Check if there is an underlying exception (e.g. I/O exception)
	// and report it.
	if ( except.getException() != null )
	    System.out.println( except.getException().toString() );
	else
	    System.out.println( except.toString() );

	if ( except instanceof SAXParseException ) {
	    SAXParseException parse = (SAXParseException) except;
	    if ( parse.getPublicId() != null )
		System.out.println( "In document PUBLIC " + parse.getPublicId() + " " + parse.getSystemId() );
	    else if ( parse.getSystemId() != null )
		System.out.println( "In document SYSTEM " + parse.getSystemId() );
	    System.out.println( "At line " + parse.getLineNumber() + " column " + parse.getColumnNumber() );
	}
    }
    
    public static String extractTag(String in,String tag)
    {
        String StartTag = "<" + tag + ">";
        String EndTag = "</" + tag + ">";
        int StartIndex = in.indexOf(StartTag);
        if(StartIndex == -1)
            return("");
        int EndIndex = in.indexOf(EndTag,StartIndex);
        if(EndIndex == -1)
            return("");
        return(in.substring(StartIndex,EndIndex + EndTag.length()));
    }


    public static String extractAttribute(String in,String tag)
    {
        String StartTag = tag + "=\"";
        String EndTag = "\"";
        int StartIndex = in.indexOf(StartTag);
        if(StartIndex == -1)
            return("");
        StartIndex += StartTag.length();
        int EndIndex = in.indexOf(EndTag,StartIndex );
        if(EndIndex == -1)
            return("");
        return(in.substring(StartIndex,EndIndex));
    }

    public static String extractTableTag(String in,String tag)
    {
        String StartTag = "<" + tag + ">";
        String ReStartTag = "<table";
        String EndTag = "</table>";
        int StartIndex = in.indexOf(StartTag);
        if(StartIndex == -1)
            return("");
        int Current = StartIndex + StartTag.length();
        int EndIndex = in.indexOf(EndTag,Current);
        if(EndIndex == -1)
            return("");
        int RestartIndex = in.indexOf(ReStartTag,Current);
        if(RestartIndex != -1 && EndIndex != -1 &&  RestartIndex < EndIndex) {
            int count = 1;
            Current = RestartIndex + ReStartTag.length();
            while(count > 0 || ( EndIndex != -1 && 
                (RestartIndex != -1 && RestartIndex < EndIndex))) {
                EndIndex = in.indexOf(EndTag,Current);
                RestartIndex = in.indexOf(ReStartTag,Current);
                if(RestartIndex != -1 && RestartIndex < EndIndex) {
                    count++;
                    Current = RestartIndex + ReStartTag.length();
                }
                else {
                    count--;
                    Current = EndIndex + EndTag.length();
                    EndIndex = in.indexOf(EndTag,Current);
                    RestartIndex = in.indexOf(ReStartTag,Current);
              }
            }
        }
        if(EndIndex == -1)
            return("");
        return(in.substring(StartIndex,EndIndex + EndTag.length()));
    }
    
    public static String readURLContents(String UrlText)
    {
        try {
            InputStream in =openURLStream(UrlText);
            String page = readStream(in);
            return(page);
        }
        catch(IOException ex) {
            throw new IllegalArgumentException(ex.toString()); // should not happen
        }
    }
    
    /**
       Write text to a file called Filename
       @param Filename non-null name for the file
       @paream text - text as contents - a cr will be added on the end
     */
    public static void writeStringFile(String FileName,String text)
    {
        try {
            FileOutputStream fs = new FileOutputStream(FileName);
            PrintStream out = new PrintStream(fs);
            out.println(text);
            out.close();
        }
        catch(IOException ex) {
            throw new IllegalArgumentException(ex.toString()); // should not happen
        }
    }
    
    /**
       Write text to a file called Filename
       @param Filename non-null name for the file
       @paream text - text as contents - a cr will be added on the end
     */
    public static String readStringFile(String FileName)
    {
        try {
            FileInputStream fs = new FileInputStream(FileName);
            return(readStream(fs));
        }
        catch(IOException ex) {
            throw new IllegalArgumentException(ex.toString()); // should not happen
        }
    }
    public static String adjustURLString(String in)
    {
        StringBuilder sb = new StringBuilder(in.length());
        int index = 0;
        while(index < in.length()) {
            char c = in.charAt(index++);
            if(c == '%') { // handle %2E code
                c = (char)Integer.parseInt(in.substring(index,index + 2),16); // hex
                index += 2;
                sb.append(c);
            }
            else {
                sb.append(c);
            }
        }
        return(sb.toString());
    }
    /**
       test if  a tag can be parsed
       @param XMLText non-null text as XML or HTML 
        @return true if we can parse this tag
     */
    @SuppressWarnings(value = "depricated")
    public static boolean testTagParse(String XMLText)
    {
	    DocumentBuilder   parser = getDocumentBuilder();
         InputStream in = new StringBufferInputStream(XMLText);
        return(getStreamDOM( in,parser) != null);
    }
    
    /**
       When the XML parser cannot handle an html document this can 
       pull out tags from a poorly formed document as long as tag ends with /tag
       @param XMLText non-null text as XML or HTML 
       @paream TagName - non-null tage to search for
       @return non-null xml string with dummy enclosing head tag and all other tags
     */
    public static String buildTagPage(String XMLText,String TagName)
    {
        String[] Tags = captureTagsWithName( XMLText, TagName);
        StringBuilder sb = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        // dummy tag
        sb.append("<head>\n");
        // add all tags
        for(int i = 0; i < Tags.length; i++) {
            String RealTag = toXMLString(Tags[i]);
            sb2.append(RealTag);
            sb2.append("\n");
           if(testTagParse(RealTag)) {
                sb.append(toXMLString(Tags[i]));
                sb.append("\n");
            }
        }
        // dummy tag
        sb.append("</head>\n");
	    writeStringFile("FullTagPage.xml",sb2.toString());
        return(sb.toString());
    }
    
    /**
       Whne the XML parser cannot handle an html document this can 
       pull out tags from a poorly formed document as long as tag ends with /tag
       @param XMLText non-null text as XML or HTML 
       @paream TagName - non-null tage to search for
       @return non-null arrag of strings holding the tags
     */
    public static String[] captureTagsWithName(String XMLText,String TagName)
    {
        String UpperSearchString = XMLText.toUpperCase();
        String StartTag = "<" + TagName.toUpperCase();
        String EndTag = "</" + TagName.toUpperCase() + ">";
        int EndLength = EndTag.length();
        
        List holder = new ArrayList();
        int index = UpperSearchString.indexOf(StartTag);
        while(index >= 0) {
            int EndIndex = UpperSearchString.indexOf(EndTag,index);
            if(EndIndex == -1)
                break; // this is not good but we allow it for now
            // note we pull tag from mixed case string
            String FullTag = XMLText.substring(index,EndIndex + EndLength);
            holder.add(FullTag);
            // next tag
            index = UpperSearchString.indexOf(StartTag,EndIndex);
        }
        
        String[] ret = Util.collectionToStringArray(holder);
        return(ret);
    }
    
    public static String readStream(InputStream in)
    {
        BufferedInputStream TheStream = null;
        StringBuilder s = new StringBuilder(2048);
        try {
            TheStream = new BufferedInputStream(in,2048);
            int c = TheStream.read();
            while(c != - 1) {
                s.append((char) c);
                c = TheStream.read();
                // ought to look at non-printing chars
            }
            TheStream.close();
        }
        catch(IOException e) {
            return(null);
        }
        return(s.toString());
    }
    /**
    * convert a string to a DOM
    * @param id 
    */
    public static Document getStringDOM(String id) 
    {
        return(getStringDOM(id,getDocumentBuilder()));
    }
    /**
    * convert a string to a DOM
    * @param id 
    */
    @SuppressWarnings(value = "depricated")
    public static Document getStringDOM(String id,DocumentBuilder parser)
    {
        InputStream in = null;
        in = new StringBufferInputStream(id);
        return(getStreamDOM(in,parser)); 
    }
    /**
    * convert a string to a DOM
    * @param id 
    */
    @SuppressWarnings("UnusedDeclaration")
    public static Document getStringDOM(String id,Parser parser)
    {
        InputStream in = null;
        in = new StringBufferInputStream(id);
        return(getStreamDOM(in,parser)); 
    }
  
    /**
        remember all catalogId urls contained in the url given by id
    */
    @SuppressWarnings(value = "deprecated")
    public static Document getFileDOM(String id,Parser parser)
    {
        InputStream in = null;
        int index = 0;
            
       try {
            in = new FileInputStream(id);
            return(getStreamDOM(in,parser)); 
        }
        catch(IOException ex) {
        }
        return(null);
    }
    /**
        remember all catalogId urls contained in the url given by id
    */
    @SuppressWarnings(value = "deprecated")
    public static Document getURLDOM(String id,Parser parser)
    {
        InputStream in = null;
        int index = 0;
            
       try {
            in = openURLStream(id);
            return(getStreamDOM(in,parser));
       }
        catch(IOException ex) {
        }
        return(null);
    }
    /**
        @param text is a non-null string with a substring which is a number
        @parem index is the start of the substring
        @return non-null Integer representing the value of the substring
    */
    public static Integer extractIndex(String text,int index)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = index; i < text.length(); i++) {
            char c = text.charAt(i);
            if(!Character.isDigit(c))
                break;
            sb.append(c);
        }
        if(sb.length() == 0) {
            String test = text.substring(index);
            test = null;
            throw new IllegalArgumentException("Must past the index of a number substring");
        }
        String NumberString = sb.toString();
        int value = Integer.parseInt(NumberString);
        return(new Integer(value));
    }
    
    @SuppressWarnings(value = "deprecated")
    public static Document getStreamDOM(InputStream in,Parser parser)
    {
	    InputSource  input;
	    Document doc;
	    if(parser == null)
	        parser = getXMLParser();

	    try {
	        // Create an input source for the document we want to parse.
	        // First line provides the actual input stream, the second
	        // line specifies the system identifier for error reporting.
	        input = new InputSource( in );
    	        	    
	        // Parse the document, report any parsing exceptions
	        // that might be thrown.
	        try {
		        parser.parse( input );
	        } 
	        catch ( SAXException except ) {
		        report( except );
		        return(null);
	        }
    	    
	        // The DOM parser will have an ErrorReport associated with it.
	      /*  if ( parser.getErrorReport().getValue() > 0 ) {
		        ErrorReport error;
        		
		        error = parser.getErrorReport();
		        System.out.println( "Errors parsing document: " + error.getValue() + " errors:" );
		        for ( int i = 0 ; i < error.getValue() ; ++i ) {
		            report( error.getException( i ) );
		        }
	        }*/
    	    
	        // Retrieve the document directly from the parser.
	        throw new UnsupportedOperationException("Fix This"); // todo

	    } catch ( IOException except ) {
	        // I/O exception can occur both in parser and printer.
	        System.out.println( except.getMessage() );
	    }
	    return(null);
    }
    
    
    public static Document getStreamDOM(InputStream in,DocumentBuilder parser) 
    {
	    InputSource  input;
	    Document doc;

	    try {
	        // Create an input source for the document we want to parse.
	        // First line provides the actual input stream, the second
	        // line specifies the system identifier for error reporting.
	        input = new InputSource( in );
    	        	    
	        // Parse the document, report any parsing exceptions
	        // that might be thrown.
	        try {
		        doc = parser.parse( input );
	        } 
	        catch ( SAXException except ) {
		        report( except );
		        return(null);
	        }
    	    
	        return(doc);
	    } catch ( IOException except ) {
	        // I/O exception can occur both in parser and printer.
	        System.out.println( except.getMessage() );
	    }
	    return(null);
    }

     /**
        Do character conversions to convert a string to an XML parsable
        string
        @param in non-null input string
        @return non-null return string
       */
    public static String toXMLString(String in)
    {
        
	    StringBuilder sb = new StringBuilder(in.length() + 100);
	//    boolean inQuote = false;
	    for(int i = 0; i < in.length(); i++)
	    {
	        char c = in.charAt(i);

	            switch(c) {
                    case '\"' :
                        sb.append("&quot;");
                        break;
                    case '&' :
	                    sb.append("&amp;");
	                    break;
	                case '\'' :
	                    sb.append("&apos;");
                        break;
	                case '<' :
	                    sb.append("&lt;");
	                    break;
	                case '>' :
	                    sb.append("&gt;");
	                    break;
	                default:
	                    sb.append((char)c);
	            }
        }

	    return(sb.toString());
	}

	/**
	* return an attruibte value from a Node
	* @param QueryNode non-null node
	* @param AttrName non-null attr name
	* return possibly null value - null of the attribute does not exist
	*/
	public static String getNodeAttribute(Node QueryNode,String AttrName)
	{
	    NamedNodeMap attrs = QueryNode.getAttributes();
	    if(attrs == null)
	        return(null);   
	    Node Value = attrs.getNamedItem(AttrName);
	    if(Value == null)
	        return(null);   
	    return(Value.getNodeValue());
	}
	
	/**
	* return truew if the node type can be a text node
	* @param type node type
	* @return true for text type
	*/
	public static boolean isTextType(int type)
	{
	    switch(type) {
	        case Node.TEXT_NODE :
	        case Node.CDATA_SECTION_NODE :
	            return(true);
	        default:
	            return(false);
	    }
	}

    /**
    * Given a node which May contain text return all contained text 
    * @param in non-null input node
    * @return non-null text 
    */
    public static String getNodeText(Node in)
    {
        StringBuilder text = new StringBuilder();
        accumulateNodeText(in,text);
        return(text.toString());
    }
    
    protected static void accumulateNodeText(Node in,StringBuilder text)
    {
        NodeList items = in.getChildNodes();
        int count = items.getLength();
        for(int i = 0; i < count; i++) {
            Node Test = items.item(i);
            int type = Test.getNodeType();
            if(XMLUtil.isTextType(type)) {
                text.append(((CharacterData)Test).getData());
            }
            else {
                if(Test instanceof Element) {
                    accumulateNodeText(Test,text);
                }
            }
        }
        
    }
    
    /**
    * build an html param string escaping spaces as %20 and other
    * piunct characters
    * @param in non-null string 
    * @return non-null escaped string
    */
    public static String escapeParamString(String in) 
    {
        StringBuilder sb = new StringBuilder(in.length() + 20);
        for(int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            if(Character.isJavaIdentifierPart(c)) 
                sb.append(c);
            else
                sb.append(buildEscapeString(c));
        }
        
        return(sb.toString());
    }
    
    protected static String buildEscapeString(char c) 
    {
        int n = (int)c;
        if(c > 15) 
            return("%" + Integer.toHexString(n));
        else
            return("%0" + Integer.toHexString(n));
    }
    
    /**
    * read an html param string escaping spaces as %20 and other
    * piunct characters
    * @param in non-null string 
    * @return non-null escaped string
    * @throws IllegalArgumentException if the string contains impropertly 
    * escaped text
    */
    public static String decodeParamString(String in) 
    {
        StringBuilder sb = new StringBuilder(in + 20);
        for(int i = 0; i < in.length(); i++) {
            char c = in.charAt(i);
            switch(c) {
            case '+' :
                sb.append(' ');
                break;
            case '%' :
                if(i > in.length() - 3)
                    throw new IllegalArgumentException("Bad param string '" + in + "'");
                i++;
                char c1 = in.charAt(i++);   
                char c2 = in.charAt(i); 
                StringBuilder decode = new StringBuilder(2);
                decode.append(c1);
                decode.append(c2);
                int n = Integer.parseInt(decode.toString(), 16);
                sb.append((char)n);
                break;
            default:
                sb.append(c);
            }
        }
        return(sb.toString());
    }
    
    /* 
    * decode <?xml version="1.0"?><Date pattern='??" >text</Date> to
    * Date
    * @param xml non-null string representing xml date
    * @return non-null Date
    * @throws IllegalArgumentException in invalid input
    * @see formatXMLDate
    */
    public static Date decodeXMLDate(String xml)
    {
        Document doc = null;
        doc = getStringDOM(xml);
        if(doc == null) {
            throw new IllegalArgumentException("Cannot parse '" +
                xml + "' as xml");
        }
        Element DateElt = doc.getDocumentElement();
        if(!DateElt.getTagName().equals("Date"))
            throw new IllegalArgumentException("Xml text '" +
                xml + "' does not represent a date.");
        String text = getElementTextValue(DateElt);
        if(DateElt.hasAttribute("pattern"))
            return(decodeDate(text,DateElt.getAttribute("pattern")));
        else
            return(decodeDate(text,null));
    }
    
    /**
    * retrieve text from text from element
    * @param in non-null Element
    * @return possibly null text string
    */
    public static String getElementTextValue(Element in)
    {
        Node[] testItems = getSubnodesOfType(in,Node.TEXT_NODE);
        if(testItems.length > 0)
            return(testItems[0].getNodeValue());
        testItems = getSubnodesOfType(in,Node.CDATA_SECTION_NODE);
        if(testItems.length > 0)
            return(testItems[0].getNodeValue());
        return(null);
    }
    
    
    /**
   * @param in non-null Node
   * @param type Some note type constant i.e. Node.CDATA_SECTION_NODE
   * @return non-null array or children of that type
    */
    public static Node[] getSubnodesOfType(Node in,int type)
    {
        NodeList subnodes = in.getChildNodes();
        if(subnodes == null)
            return(new Node[0]);
        int count = subnodes.getLength();
        List holder = new ArrayList();
        for(int i = 0; i < count; i++) {
            Node test = subnodes.item(i);
            if(test.getNodeType() == type)
                holder.add(test);
        }
        Node[] ret = new Node[holder.size()];
        holder.toArray(ret);
        return(ret);
    }
    
    
    public static Date decodeDate(String text,String pattern)
    {
        if(pattern == null)
            return(new Date(Long.parseLong(text)));
        SimpleDateFormat fmt = new SimpleDateFormat(pattern);
        try {
            return(fmt.parse(text));
        }
        catch(ParseException ex) {
            throw new IllegalArgumentException("Cannot parse '" +
                text + "' with pattern '" + pattern + "'");
        }
    }
    
    public static String formatXMLDate(Date date)
    {
        StringBuilder sb = new StringBuilder();
        addXMLHeader(sb);
        sb.append("<Date>");
        sb.append(Long.toString(date.getTime()));
        sb.append("</Date>");
        return(sb.toString());
    }

    public static String formatXMLDate(Date date,String pattern)
    {
        StringBuilder sb = new StringBuilder();
        addXMLHeader(sb);
        sb.append("<Date pattern=\"");
        sb.append(pattern);
        sb.append("\" >");
        SimpleDateFormat fmt = new SimpleDateFormat(pattern);
        sb.append(fmt.format(date));
        sb.append("</Date>");
        return(sb.toString());
    }

    public static void addXMLHeader(StringBuilder sb)
    {
        sb.append("<?xml version=\"1.0\"?>\n");
    }
    
    public static String buildArgumentText(String in)
    {
        StringBuilder sb = new StringBuilder((int)(in.length() * 1.1));
        int n = in.length();
        for(int i = 0; i < n; i++) {
            char c = in.charAt(i);
            if(Character.isJavaIdentifierPart(c)) {
                sb.append(c);
            }
            else {
                switch(c) {
                    case '-' :
                    case '_' :
                    case '|' :
                    case '(' :
                    case ')' :
                    case '~' :
                        sb.append(c);
                        break;
                    default:
                        sb.append(escapeChar(c));
                }
            }
        }
        
        return(sb.toString());
    }
    
    public static String escapeChar(char in) {
        String HexVal = Integer.toHexString((int)in);
        if(HexVal.length() == 1)
            HexVal = "0" + HexVal;
        return("%" + HexVal);
    }

    /**
     * build s simple xml tag with indent and cr
     * @param tagName  non-null name
     * @param item  possibly null itme - if null no tag is generated
     * @param indent non-negative indent level
     * @return  non-null string to add for tag
     */
    public static String buildSimpleTag(String tagName,Object item,int indent)
    {
        if(item == null)
            return "";
        StringBuilder sb = new StringBuilder();
        sb.append(Util.indentString(indent));
        sb.append("<");
        sb.append(tagName);
        sb.append(">");

        sb.append(item.toString());

        sb.append("</");
        sb.append(tagName);
        sb.append(">\n");


        return(sb.toString());
    }

    public static class IgnoreXMLTag implements ITagHandler
    {
        private IgnoreXMLTag() {}
        public Object handleTag(String tagName,NameValue[] attributes)
         {
             XMLUtil.handleAllTags(attributes);
             return this;
         }
    }

}
