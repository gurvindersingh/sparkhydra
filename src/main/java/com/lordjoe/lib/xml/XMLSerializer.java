
package com.lordjoe.lib.xml;

import com.lordjoe.utilities.*;
import org.xml.sax.*;

import javax.xml.parsers.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * Class to introspect objects and determine their DOM/XML transport.
 * This class is intended to convert Java object to and from an XML stream
 * for the purpose of transporting them between the client and server.  The
 * conventions used for naming these objects are specific to the Spec2000 system.
 * 
 * @author Steve Lewis
 * @since 3 January 2000
 */

@SuppressWarnings({"Deprecated", "deprecation"})
public class XMLSerializer extends HandlerBase
{
//    private static ILogger gLogger = null; // StreamLogger.getConsole();
//    public static ILogger getLogger()
//    {
//        return(gLogger);
//    }
//
//    @SuppressWarnings("UnusedDeclaration")
//    public static void setLogger(ILogger in)
//    {
//        gLogger = in;
//    }
    @SuppressWarnings("UnusedDeclaration")
    public static final Class[] NULL_CLASSES = new Class[0];
    @SuppressWarnings("UnusedDeclaration")
    public static final Class[] INT_CLASSEARRAY =  { Integer.TYPE };
    @SuppressWarnings("UnusedDeclaration")
    public static final Object[] NULL_OBJECTS = new Object[0];
    /**
     * The string representing a "get" mode.
     */
    public static final String GET_STRING = "get";
    /**
     * The string representing a "is" mode.
     */
    public static final String IS_STRING;

    /**
     * The String representing a "set" mode.
     */
    public static final String SET_STRING = "set";

    //private static boolean gDebugMode;
    private static Hashtable gTagToClass;

    protected static void registerDefaultTags()
    {

    }

    // Should always work SLewis
    static {
        registerDefaultTags();
        IS_STRING = "is";
    }

    private Object m_ActiveObject;

    private Object m_DefaultHandler;

    private List<EntityItem> m_EntityStack;
    private List<String> m_TagStack;



    public XMLSerializer()
    {
        m_EntityStack = new ArrayList<EntityItem>();
        m_TagStack = new ArrayList<String>();
      //  TransportMapping.initTables();
    }

    /*
    public static void setDebugMode(boolean doit)
    {
        gDebugMode = doit;
    }
    */

    public static boolean getDebugMode()
    {
//        ILogger logger = getLogger();
//        //noinspection SimplifiableIfStatement
//        if(logger == null)
//            return false;
//        return logger.isDebugEnabled();
        //return(gDebugMode);
        return false;
    }

    public static synchronized void registerTag(String TagName,Class AssociatedClass)
    {
        if(gTagToClass == null)
            gTagToClass = new Hashtable();
        //noinspection unchecked
        gTagToClass.put(TagName,AssociatedClass);
        // also support caseless lookups
        //noinspection unchecked
        gTagToClass.put(TagName.toUpperCase(),AssociatedClass);
    }

     public Object getDefaultHandler()
    {
        return(m_DefaultHandler);
    }

    public void setDefaultHandler(Object o)
    {
        m_DefaultHandler = o;
    }

    public Object getActiveObject()
    {
        return(m_ActiveObject);
    }

    protected void setActiveObject(Object o)
    {
        m_ActiveObject = o;
    }

    public static Object parseXMLString(String xmlString)
    {
        return(parseXMLString(xmlString,null));
    }

    @SuppressWarnings(value = "deprecated")
    public static Object parseXMLString(String xmlString,ITagHandler Handler)
    {
        XMLSerializer TheObj = new XMLSerializer();
        InputStream ins = new StringBufferInputStream(xmlString);
        return internalParseStream(Handler, TheObj, ins);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static Object parseXMLStream(InputStream stream)
    {
        return(parseXMLStream(stream,null));
    }

    public static Object parseXMLStream(InputStream stream,ITagHandler Handler)
    {
        XMLSerializer TheObj = new XMLSerializer();
        return internalParseStream(Handler, TheObj, stream);
    }

    protected static Object internalParseStream(ITagHandler Handler, XMLSerializer theObj, InputStream ins)
    {
        if(Handler != null) {
        	if (Handler.getClass().getName().contains("Specimen")) {
        		Util.breakHere();
        	}
            theObj.setDefaultHandler(Handler);
            theObj.pushEntity(new EntityItem(Handler,"top"));
        }
        try {
            return(theObj.parseStream2(ins));
        }

        catch(SAXParseException ex) {
            XMLSerializerException xmlEx = new XMLSerializerException(ex,theObj.m_TagStack,theObj.m_EntityStack);
          //  gLogger.error( xmlEx.getMessage(), ex );
            throw xmlEx;
        }
        catch(SAXException ex) {
            Exception ex1 = ex.getException();
            if(ex1 == null)
                //noinspection UnusedDeclaration
            {
                //noinspection UnusedAssignment
                ex1 = ex;
            }
        // Unwrap a SAX exception, we need more detail - MWR
            //throw new XMLSerializerException(ex,TheObj.m_TagStack,TheObj.m_EntityStack);
            Exception cause = ex.getException();
            if (cause == null)
                cause = ex;
            throw new XMLSerializerException(cause,theObj.m_TagStack,theObj.m_EntityStack);
        }
        catch(IOException ex) {
            throw new XMLSerializerException(ex,theObj.m_TagStack,theObj.m_EntityStack);
        }
        catch(Exception ex) {
            throw new XMLSerializerException(ex,theObj.m_TagStack,theObj.m_EntityStack);
        }
    }


    public static Object parseFile(File TheFile)
    {
        String TestXML = FileUtilities.readInFile(TheFile);
        if(TestXML == null)
            return(null);
        return(parseXMLString(TestXML));
    }

    public static Object parseFile(String FileName)
    {
        String TestXML = FileUtilities.readInFile(FileName);
        if(TestXML == null)
            return(null);
        return(parseXMLString(TestXML));
    }

    public static Object parseFile(String FileName,ITagHandler Handler)
    {
        String TestXML = FileUtilities.readInFile(FileName);
        if(TestXML == null)
            return(null);
        try {
            return(parseXMLString(TestXML,Handler));
        }
        catch(RuntimeException ex)  {
           RuntimeException ex2 =  Util.getRuntimeCause(ex);

            ex2.printStackTrace();
            throw ex2;
        }
    }

    public static Object parseFile(File TheFile, ITagHandler Handler)
    {
        String TestXML = FileUtilities.readInFile(TheFile);
        if(TestXML == null)
            return(null);
        return(parseXMLString(TestXML,Handler));
    }


    @SuppressWarnings("UnusedDeclaration")
    public static Object parseResouceObject(Class ResourceClass,String ResourceName)
    {
        String TestXML = FileUtilities.readInResource(ResourceClass, ResourceName) ;
        return(parseXMLString(TestXML));
    }

    @SuppressWarnings("UnusedDeclaration")
    public static Object parseResouceObject(Class ResourceClass,String ResourceName,ITagHandler Handler)
    {
        //noinspection RedundantStringToString
        String TestXML = FileUtilities.readInResource(ResourceClass,ResourceName).toString();
        //noinspection ConstantConditions
        if(TestXML == null)
            return(null);
        return(parseXMLString(TestXML,Handler));
    }

    /**
     * old code
     * @param in
     * @return
     * @throws IOException
     * @throws SAXException
     */
      @SuppressWarnings("UnusedDeclaration")
    public Object parseStream(InputStream in) throws IOException,SAXException
    {
        Parser TheParser = XMLUtil.getXMLParser();
        TheParser.setDocumentHandler(this);
        InputSource RealIn = new InputSource(in);
        TheParser.parse(RealIn);
        return(getActiveObject());
    }

    /**
     * newer code
     * @param in
     * @return
     * @throws IOException
     * @throws SAXException
     */
    public Object parseStream2(InputStream in) throws IOException,SAXException
    {
        SAXParser TheParser = XMLUtil.getSAXParser();
        InputSource RealIn = new InputSource(in);
        TheParser.parse(RealIn,this);
        return(getActiveObject());
    }


  /**
    * Receive notification of the start of an element.
    *
    * <p>By default, do nothing.  Application writers may override this
    * method in a subclass to take specific actions at the start of
    * each element (such as allocating a new tree node or writing
    * output to a file).</p>
    *
    * @param name The element type name.
    * @param attributes The specified or defaulted attributes.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly
    *            wrapping another exception.
    * @see org.xml.sax.DocumentHandler#startElement
    */
  @SuppressWarnings(value = "deprecated")
  public void startElement (String name, AttributeList attributes)
    throws SAXException
  {
      try {
       pushTag(name);
     //  if(getDebugMode())
    //        getLogger().debug("Handling Tag " + name);

       Object handler = getDefaultHandler();
       Object top = getTopObject();
       NameValue[] data = attributesToNameValues(attributes);
       if(top != null) {
            if(top instanceof ITagHandler) {
                 handler = ((ITagHandler)top).handleTag(name,data);
                 if(handler == null) {
                    pushEntity(new EntityItem(top,name));
                    return;
                 }
            }
       }
       if(handler == null) {
            Class ElementClass = nameToCreatedClass(name);
            if(ElementClass == null)
                ElementClass = buildClassFromAttributes(data);

            if(ElementClass != null) {
                handler = ElementClass.newInstance();
                throw new IllegalStateException("problem"); // ToDo change
               // ClassAnalyzer.setAttributes(handler,data);
            }
            else {
                handler = new DeferredHandler();
            }
       }
       else {
            setDefaultHandler(null);
            if(handler != XMLUtil.IGNORE_TAG_HANDLER)    throw new IllegalStateException("problem"); // ToDo change
           //     ClassAnalyzer.setAttributes(handler,data);
       }
  //     if(getDebugMode())
  //          getLogger().debug(" Handled by " + handler.getClass().getName());

       pushEntity(new EntityItem(handler,name));
      }
      catch (InstantiationException ex) {
        throw new SAXException(ex.toString());
      }
      catch (IllegalAccessException ex) {
        throw new SAXException(ex.toString(),ex);
      }
      catch (Exception ex) {
          ex.printStackTrace();
          //noinspection ConstantConditions
          if(!(ex instanceof SAXException)) {
            throw new SAXException(ex.toString(),ex);
        }
        else {
            //noinspection UnnecessaryLocalVariable
            SAXException saxex = (SAXException)ex;
            throw saxex;
        }
      }
  }

  /**
  * Map a tag name into the class to create for that tag
  * @param name non-null name of a possible tage
  * @return - Associated class to create or null of no association
  */
  public static Class buildClassFromAttributes(NameValue[] attributes)
  {
        NameValue nv = XMLUtil.findNameValue("class", attributes);
        if(nv == null)
            return(null);
        String ClassName = nv.m_Value.toString();
        nv.m_Name = XMLUtil.TAG_HANDLED;

        try {
            Class TheClass = Class.forName(ClassName);
             return(TheClass);
        }
        catch(ClassNotFoundException ex) {
            return(null);
        }
  }

  /**
  * Map a tag name into the class to create for that tag
  * @param name non-null name of a possible tage
  * @return - Associated class to create or null of no association
  */
  public static Class nameToCreatedClass(String name)
  {
     if(gTagToClass != null) {
        Class ret = (Class)gTagToClass.get(name);
        if(ret != null)
            return(ret);
        ret = (Class)gTagToClass.get(name.toUpperCase());
        if(ret != null)
            return(ret);
     }
     return(null);
  }



  /**
    * Receive notification of the end of an element.
    *
    * <p>By default, do nothing.  Application writers may override this
    * method in a subclass to take specific actions at the end of
    * each element (such as finalising a tree node or writing
    * output to a file).</p>
    *
    * @param name The element type name.
    * @param attributes The specified or defaulted attributes.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly
    *            wrapping another exception.
    * @see org.xml.sax.DocumentHandler#endElement
    */
  public void endElement (String name)
    throws SAXException
  {
      popTag(name);
      if(!name.equals(getTopName()))
        throw new SAXException("Unmatched Tag " + name);
      if(getAccumulator().length() > 0) {
            String data = getAccumulator().toString().trim();
            getAccumulator().setLength(0); // clear
            if(data.length() > 0) {
                handleCData(name,data);
            }
      }
     popEntity();
  }

  protected void handleCData(String name,String data)
  {
      throw new UnsupportedOperationException("Fix This"); // ToDo
//        Object Item = getTopObject();
//        if(Item.getClass() == DeferredHandler.class) {
//            Item = getSecondObject();
//            if(Item instanceof ITagHandler) {
//                NameValue[] items = buildCDataAttributes(name,data);
//                ((ITagHandler)Item).handleTag("CDATA",items);
//                 if(!items[0].m_Name.equals(XMLUtil.TAG_HANDLED)) {
//                    ClassAnalyzer.setProperty(Item, name, data);
//                    items[0].m_Name = XMLUtil.TAG_HANDLED;
//                 }
//          }
//            else {
//                ClassAnalyzer.setProperty(Item, name, data);
//            }
//        }
//        else {
//            if(Item instanceof ITagHandler) {
//                NameValue[] items = buildCDataAttributes("CDATA",data);
//                ((ITagHandler)Item).handleTag("CDATA",items);
//                if(!items[0].m_Name.equals(XMLUtil.TAG_HANDLED)) {
//                    ClassAnalyzer.setProperty(Item, name, data);
//                    items[0].m_Name = XMLUtil.TAG_HANDLED;
//                }
//            }
//            else {
//                ClassAnalyzer.setProperty(Item, name, data);
//            }
//            /*  else {
//                throw new IllegalStateException("Unhandled CDATA " + data);
//            }
//            */
//        }
    }


    protected NameValue[] buildCDataAttributes(String name,String data)
    {
        NameValue[] ret = new NameValue[1];
        ret[0] = new NameValue();
        ret[0].m_Name = name;
        ret[0].m_Value = data;
        return(ret);
    }

    public StringBuilder getAccumulator()
    {
        if(m_EntityStack.size() > 0)
           return((m_EntityStack.get(m_EntityStack.size() - 1)).m_Text);
        return(null);
    }

    public String getTopName()
    {
        if(m_EntityStack.size() > 0)
            return((m_EntityStack.get(m_EntityStack.size() - 1)).m_Name);
        return(null);
   }

    public Object getTopObject()
    {
        if(m_EntityStack.size() > 0)
             return((m_EntityStack.get(m_EntityStack.size() - 1)).m_Object);
        return(null);
    }


    public Object getSecondObject()
    {
        if(m_EntityStack.size() > 1)
             return((m_EntityStack.get(m_EntityStack.size() - 2)).m_Object);
        return(null);
    }

    public void pushTag(String name)
    {
        m_TagStack.add(name);
    }

    public void popTag(String name)
    {
        if(!m_TagStack.get(m_TagStack.size() - 1).equals(name))
            throw new IllegalStateException("Bad Tag Stack");
        m_TagStack.remove(m_TagStack.size() - 1);
    }


    public void popEntity()
    {
        m_EntityStack.remove(m_EntityStack.size() - 1);
    }

    public void pushEntity(EntityItem added)
    {
        if(m_EntityStack.size() == 0)
            setActiveObject(added.m_Object);
        m_EntityStack.add(added);
    }


  /**
    * Receive notification of character data inside an element.
    *
    * <p>By default, do nothing.  Application writers may override this
    * method to take specific actions for each chunk of character data
    * (such as adding the data to a node or buffer, or printing it to
    * a file).</p>
    *
    * @param ch The characters.
    * @param start The start position in the character array.
    * @param length The number of characters to use from the
    *               character array.
    * @exception org.xml.sax.SAXException Any SAX exception, possibly
    *            wrapping another exception.
    * @see org.xml.sax.DocumentHandler#characters
    */
    public void characters (char ch[], int start, int length)
        throws SAXException
    {
        StringBuilder sb = getAccumulator();
        for(int i = start; i < start + length; i++) {
            if(!Character.isWhitespace(ch[i]) || sb.length() > 0)
                sb.append(ch[i]);
        }
    }

    /**
     * Convert an Element to a Java Object.
     * Use our rules to determine the Object's properties and fill them in.
     * If the object is a collection iterate through collection and add to current object.
     * If the object is "one of ours" recurse into sub object, fill out and add.
     * 
     * @param elem The DOM Element to convert.
     * @param rootObj If non-null, the object to add this element to.
     * @return The Object serialized in the Element or rootObj if it is non-null.
     * @exception java.lang.InstantiationException
     * @exception java.lang.IllegalAccessException
     * @exception java.lang.ClassNotFoundException
     */
  /*  protected void updateObject(Element elem, Object obj,Object rootObj) throws XMLException
    {
        handleElementAttributes(elem,obj,rootObj);
        updateElementChidren(elem,obj);
    }
    
    
    protected void updateElementChidren(Element elem, Object rootObj) throws XMLException
    {
         // Iterate through sub elements and add to current object
        NodeList children = elem.getChildNodes();
        for(int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if(child.getNodeType() == Node.ELEMENT_NODE) {
  			  Object TheObject = null;
              updateObject((Element)child, TheObject,rootObj);
            }
        }
    }
    */

    /**
     * Given a name such as FIRST_NAME convert it to FirstName
     * 
     * @param tagName The name who's set method we are looking for.
     * @return Name of the set method for this tag.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected String adjustName(String tagName) {
        if(!tagName.equals(tagName.toUpperCase()))
            return(tagName);
        StringBuilder sb = new StringBuilder(tagName.length());
        sb.append(tagName.charAt(0));
        for(int i = 1; i < tagName.length(); i++) {
            char c = tagName.charAt(i);
            if(c == '_') {
                i++;
                if(i < tagName.length())
                    sb.append(Character.toUpperCase(tagName.charAt(i)));
            }
            else {
               sb.append(Character.toLowerCase(c));
            }
        }
        return(sb.toString());
    }

    @SuppressWarnings(value = "deprecated")
    protected NameValue[] attributesToNameValues(AttributeList attributes)
    {
        if(attributes.getLength() == 0)
            return(NameValue.NULL_ARRAY);
        NameValue[] ret = new NameValue[attributes.getLength()];
        for(int i = 0; i < attributes.getLength(); i++) {
            ret[i] = new NameValue();
            ret[i].m_Name = attributes.getName(i);
            String val  = attributes.getValue(i);
            String Type  = attributes.getType(i);
            if(Type.equals("CDATA")) {
                ret[i].m_Value = val;
            }
            else {
             //   getLogger().debug("Need to handle Type " + Type + " for Value " + ret[i].m_Name);
                ret[i].m_Value = val;
            }
        }
        return(ret);
    }

     @SuppressWarnings("UnusedDeclaration")
    protected static Object convertStringToDesiredType(Class dataType,String sValue)
    {
        //noinspection UnusedAssignment
        Object value = null;
/*		if(OIDBAccessor.class.isAssignableFrom(dataType)) {
		    try {
		        OIDBAccessor TheAccessor = (OIDBAccessor)dataType.newInstance();
		        return(TheAccessor.fromDataBaseString(sValue));
		    }
		    catch(InstantiationException ex) {
		        Assertion.fatalException(ex);
		    }
		    catch(IllegalAccessException ex) {
		        Assertion.fatalException(ex);
		    }
    	}
    	*/
        // TODO: use Class.getConstructor(parameter types) here instead of if else structure
/*		if(dataType == com.lordjoe.dataobject.OIPropertyChangeHandler.class) {
		    try {
		        Class RealType = Class.forName(sValue);
		        return(RealType.newInstance());
		    }
		    catch(ClassNotFoundException ex) {
		        Assertion.fatalException(ex);
		    }
		    catch(InstantiationException ex) {
		        Assertion.fatalException(ex);
		    }
		    catch(IllegalAccessException ex) {
		        Assertion.fatalException(ex);
		    }
		    
		} */
        if(dataType == String.class) {
            value = sValue;
        } else if(dataType == Integer.class) {
            value = new Integer(sValue);
        } else if(dataType == Double.class) {
            value = new Double(sValue);
        } else if(dataType == Float.class) {
            value = new Float(sValue);
        } else if(dataType == Boolean.class) {
            value = Boolean.valueOf(sValue);
       } else if(dataType == Boolean.TYPE) {
            value = Boolean.valueOf(sValue);
       } else if(dataType == Long.TYPE) {
            value = new Long(sValue);
        } else if(dataType == Class.class) {
            try {
                value = Class.forName(sValue);
            }
            catch(Exception ex) {
                throw new IllegalArgumentException("Bad Class Name " + sValue);
            }

        } else if(dataType == java.util.Date.class) {
            value = new java.util.Date(Long.parseLong(sValue));
        } else if(dataType == java.sql.Date.class) {
            value = new java.sql.Date(Long.parseLong(sValue));
        } else if(dataType == java.lang.Object.class) {
            value = sValue;
        } else {
            throw new IllegalArgumentException("Data type not implemented:" + dataType.getName());
        }
        return(value);
    }



    @SuppressWarnings("UnusedDeclaration")
    protected void invokeTransportSet(Object obj,Method setMeth,Object[] setValues) throws XMLException
    {
        try {
            setMeth.invoke(obj, setValues);
        }
        catch(InvocationTargetException ex) {
            System.out.println("Set Method Threw Exception - Class " + obj.getClass().getName() +
                " Method " + setMeth.getName() +
                "  Data " + setValues[0]);
             Throwable RealException = ex.getTargetException();
             RealException.printStackTrace();
            throw new XMLException("Method Invokation threw Exception " + setMeth.getName(),ex);
        }
        catch(IllegalAccessException ex) {
            throw new XMLException("Method Invokation threw IllegalAccessException " + setMeth.getName(),ex);
        }

    }


    /**
     * Return the simple Element name for a given method.
     * The method should begin with "get" or "set".
     * 
     * @param meth Method to derive the element/property name from.
     * @return The name of the Element.
     * This is also the name of the Java property.
     */
    @SuppressWarnings("UnusedDeclaration")

    protected String methodTagName(Method meth,boolean mode) throws XMLException
    {
        String startString;
        if(mode) {
            startString = GET_STRING;
        }
        else {
            startString = SET_STRING;
        }
        String MethodName = meth.getName();
        String ret = MethodName.substring(startString.length());
        return(ret);
    }




    /**
     * Find all interfaces implemented by this Class that match our interface filter.
     * 
     * @param objClass Class of Object to check for interfaces.
     * @param m_InterfaceFilter The interfaces filter to check for.
     * Interface names must start with this String.
     * @return Vector of interfaces that match our interface filter.
     */

    protected Vector<Class> findMatchingInterfaces(Class objClass, String interfaceFilter) {
        // find the matching interfaces
        Class []allInterfaces = objClass.getInterfaces();
        Vector<Class> interfaces = new Vector<Class>();
        //noinspection ForLoopReplaceableByForEach
        for(int i = 0; i < allInterfaces.length; ++i) {
            //DBG.msg(this, "Interface: " + allInterfaces[i].getUrl());
            if(null != interfaceFilter) {
                if(allInterfaces[i].getName().startsWith(interfaceFilter)) {
                    //noinspection unchecked
                    interfaces.addElement(allInterfaces[i]);
                }
            } else {
                interfaces.addElement(allInterfaces[i]);
            }
        }

        return interfaces;
    }

    /**
     * Find all interfaces implemented by this Object that match our interface filter.
     * 
     * @param obj Object to check for interfaces.
     * @param interfaceFilter The interfaces filter to check for.
     * Interface names must start with this String.
     * @return Vector of interfaces that match our interface filter.
     */

    @SuppressWarnings("UnusedDeclaration")
    protected Vector<Class> findMatchingInterfaces(Object obj, String interfaceFilter) {
        return findMatchingInterfaces(obj.getClass(), interfaceFilter);
    }




    /**
    * Holds the state of the Current Entity being processed
    */
    public static class EntityItem implements ITagHandler
    {
        public Object m_Object; // controling Objecrt
        public String m_Name; // name of the entity
        public StringBuilder m_Text; // AccumulatedText

        protected EntityItem(Object o,String Name) {
            m_Object = o;
            m_Name = Name;
           m_Text = new StringBuilder();
        }

        /**
        * This returns the object which will handle the element - the handler
        * may return itself or create a sub-object to manage the element
        * @param elementName non-null name of the element
        * @param attributes non-null array of name-value pairs
        * @return possibly null handler
        */
        public Object handleTag(String elementName, NameValue[] attributes) {
            if (elementName.equals("CDATA")) {
                NameValue nv = XMLUtil.findRequiredNameValue("CDATA",attributes);
                m_Text = m_Text.append(nv.m_Value.toString());
                return (null);
            }
            return (null);
        }

    }

    /**
    * place holder when you are not sure what to do
    */
    protected static class DeferredHandler
    {
        public DeferredHandler() {
        }
    }

    public static void main(String[] args)
    {
//	    registerTag("ICCC",com.onvia.lib.xml.ICCC.class);
//	    registerTag("Schema",com.onvia.metadata.OCMetaData.class);
        try {
            //noinspection UnusedDeclaration
            Object test = parseFile(args[0]);
            System.out.println("Done"); // test
         }
        catch(Exception ex) {
            ex.printStackTrace();
        }

    }

}
