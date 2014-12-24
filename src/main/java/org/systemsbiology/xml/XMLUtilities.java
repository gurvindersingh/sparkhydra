package org.systemsbiology.xml;

import com.lordjoe.lib.xml.*;
import com.lordjoe.utilities.*;
import org.systemsbiology.sax.*;
import org.xml.sax.*;

import java.io.*;

/**
 * org.systemsbiology.xml.XMLUtilities
 *
 * @author Steve Lewis
 * @date Feb 1, 2011
 */
public class XMLUtilities
{
    public static XMLUtilities[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = XMLUtilities.class;

     /**
     * copy code that works well for text files organized into lines
     * @param inp - !null exisating readable file
     * @param out - writable file
     */
    public static void copyFileLines(File inp,File out) {
        LineNumberReader reader = null;
         PrintWriter outWriter = null;
        try {
            reader = new LineNumberReader(new InputStreamReader(new FileInputStream(inp)));
            outWriter = new PrintWriter(new FileWriter(out));
            String line = reader.readLine();
            while(line != null) {
                outWriter.println(line);
                line = reader.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                if(reader != null)
                    reader.close();
                if(outWriter != null)
                    outWriter.close();
            }
            catch (IOException e) {
              }
        }

    }

    /**
      * * Convert String to InputStream using ByteArrayInputStream
      * class. This class constructor takes the string byte array
      * which can be done by calling the getBytes() method.
      *
      * @param text !null string
      * @return !null inputstream
      */
     public static InputStream stringToInputStream(String text) {

         try {

             InputStream is = new ByteArrayInputStream(text.getBytes("UTF-8"));
             return is;

         }
         catch (UnsupportedEncodingException e) {

             throw new RuntimeException(e);
         }
     }

     /**
      * * Convert String a reader
      *
      * @param text !null string
      * @return !null Reader
      */
     public static Reader stringToReader(String text) {

         return new InputStreamReader(stringToInputStream(text));
     }

     public static String extractTag(String tagName, String xml) {
         String startStr = tagName + "=\"";
         int index = xml.indexOf(startStr);
         if (index == -1) {
             String xmlLc = xml.toLowerCase();
             if (xmlLc.equals(xml))
                 throw new IllegalArgumentException("cannot find tag " + tagName);
             String tagLc = tagName.toLowerCase();
               return  extractTag(tagLc,xmlLc); // rey without case
         }
         index += startStr.length();
         int endIndex = xml.indexOf("\"", index);
         if (endIndex == -1)
             throw new IllegalArgumentException("cannot terminate tag " + tagName);
         // new will uncouple old and long string
         return new String(xml.substring(index, endIndex));
     }

     /**
      * grab a tag that might not exist
      * @param tagName
      * @param xml
      * @return
      */
     public static String maybeExtractTag(String tagName, String xml) {
         String startStr = tagName + "=\"";
         int index = xml.indexOf(startStr);
         if (index == -1) {
             return null;
         }
         index += startStr.length();
         int endIndex = xml.indexOf("\"", index);
         if (endIndex == -1)
             return null;
         // new will uncouple old and long string
         return new String(xml.substring(index, endIndex));
     }

    public static void outputLine() {
        System.out.println();
    }

    public static void outputLine(String text) {
        System.out.println(text);
    }

    public static void outputText(String text) {
        System.out.print(text);
    }

    public static void errorLine(String text) {
        System.err.println(text);
    }

    public static void errorLine() {
        System.err.println();
    }

    public static void errorText(String text) {
        System.err.print(text);
    }

    public static String freeMemoryString() {
        StringBuilder sb = new StringBuilder();
        Runtime rt = Runtime.getRuntime();
        double mem = rt.freeMemory() / 1000000;
        double totmem = rt.totalMemory() / 1000000;

        sb.append(String.format("%5.1f", mem));
        sb.append(String.format(" %4.2f", mem / totmem));
        return sb.toString();
    }

    /**
     * convert a name like  C:\Inetpub\wwwroot\ISB\data\parameters\isb_default_input_kscore.xml
     * to  isb_default_input_kscore.xml
     *
     * @param fileName !null file name
     * @return !null name
     */
    public static String asLocalFile(String fileName) {
        fileName = fileName.replace("\\", "/");
        File f = new File(fileName);
        return f.getName();
    }


    public static boolean isAbsolute(final String pName) {
        return new File(pName).isAbsolute();
    }


    public static void showProgress(int value) {
        if (value % 1000 == 0)
            outputText(".");
        if (value % 50000 == 0)
            outputLine();

    }


    public static void printProgress(int value, int resolution) {
        if (value % resolution == 0) {
            outputText(Integer.toString(value));
            outputText(",");
        }
        if (value % (5 * resolution) == 0)
            outputLine();

    }

    /**
     * parse an xml file using a specific handler
     *
     * @param is !null stream
     * @return !null key value set
     */
    public static <T> T parseFile(InputStream is, AbstractElementSaxHandler<T> handler1, String url) {
        DelegatingSaxHandler handler = new DelegatingSaxHandler();
        if (handler1 instanceof ITopLevelSaxHandler) {
            handler1.setHandler(handler);
            handler.pushCurrentHandler(handler1);
            handler.parseDocument(is);
            T ret = handler1.getElementObject();
            return ret;

        }
       throw new UnsupportedOperationException("Not a Top Level Handler");
    }

    /**
     * parse an xml file using a specific handler
     *
     * @param is !null existing readible file
     * @return !null key value set
     */
    public static <T> T parseFile(File file, AbstractElementSaxHandler<T> handler1) {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return parseFile(is, handler1, file.getName());
    }

    public static Object parseXMLString(String xmlString)
      {
          return(parseXMLString(xmlString,null));
      }

      @SuppressWarnings(value = "deprecated")
      public static   <T> T parseXMLString(String xmlString,AbstractElementSaxHandler<T> handler1)
      {
          XMLSerializer TheObj = new XMLSerializer();
          InputStream ins = new StringBufferInputStream(xmlString);
          return parseFile( ins,handler1, null);
      }




}
