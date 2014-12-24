package org.systemsbiology.xtandem.mzid;

import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.validation.*;
import java.io.*;

/**
 * org.systemsbiology.xtandem.mzid.MzIdentMLUtilities
 * User: steven
 * Date: 2/19/13
 */
public class MzIdentMLUtilities {
    public static final MzIdentMLUtilities[] EMPTY_ARRAY = {};

    public static final String VERSION_1_1_XSD = "http://www.psidev.info/files/mzIdentML1.1.0.xsd";
    public static final String VERSION_1_1_XSD_RSRC = "mzIdentML1.1.0.xsd";

    public static void validateMZIdentMLFile(String arg) {
        File f = new File(arg);
        if (!f.exists())
            throw new IllegalArgumentException("file does not exist");
        if (f.isDirectory())
            throw new IllegalArgumentException("file is a directory");
        if (!f.canRead())
            throw new IllegalArgumentException("file cannot be read");
        try {
            validateMZIdentMLFile(f);
        }
        catch (Exception e) {
            throw new RuntimeException(e);

        }

    }


    public static void validateMZIdentMLFile(File f) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setValidating(false);
        factory.setNamespaceAware(true);

        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      // load a WXS schema, represented by a Schema instance
        Source schemaFile = new StreamSource(VERSION_1_1_XSD);
        //     Source schemaFile = new StreamSource(xsdStream);
        Schema schema = sf.newSchema(schemaFile);

        factory.setSchema(schema);

        SAXParser parser = factory.newSAXParser();

        XMLReader reader = parser.getXMLReader();
        reader.setErrorHandler(new SimpleErrorHandler());
        InputSource inscr = new InputSource(new FileInputStream(f));
        reader.parse(inscr);
        System.out.println("validated " + f);
    }

    public static void validateMZIdentMLFileX(File f) throws Exception {
        // create a SchemaFactory capable of understanding WXS schemas
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        Class cls = MzIdentMLUtilities.class;
        InputStream xsdStream = cls.getResourceAsStream(VERSION_1_1_XSD_RSRC);
        // load a WXS schema, represented by a Schema instance
        Source schemaFile = new StreamSource(VERSION_1_1_XSD);
        //     Source schemaFile = new StreamSource(xsdStream);
        Schema schema = factory.newSchema(schemaFile);


        // validate the DOM tree
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        df.setValidating(true);
        df.setNamespaceAware(true);
        df.setSchema(schema);

        DocumentBuilder parser = df.newDocumentBuilder();
        parser.setErrorHandler(new SimpleErrorHandler());
        Document document = parser.parse(f);
        System.out.println(" Finished parsing " + f);

        System.out.println("read schema");

//        // create a Validator instance, which can be used to validate an instance document
//        Validator validator = schema.newValidator();
//
//        validator.validate(new DOMSource(document));

        System.out.println("validated " + f);

    }

    public static void showMessage(final SAXParseException e) {
         int line = e.getLineNumber();
         System.out.println("error on line " + line + " " + e.getMessage());
     }


    public static class SimpleErrorHandler implements ErrorHandler {
        public void warning(SAXParseException e) throws SAXException {
            showMessage(e);
        }


        public void error(SAXParseException e) throws SAXException {
            showMessage(e);
         }

        public void fatalError(SAXParseException e) throws SAXException {
            showMessage(e);
         }
    }


    public static void main(String[] args) throws Exception {
       // System.setProperty("javax.xml.parsers.SAXParserFactory","gnu.xml.aelfred2.JAXPFactory");
     //   System.setProperty("javax.xml.parsers.SAXParserFactory","org.apache.xerces.jaxp.SAXParserFactoryImpl");

        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            try {
                validateMZIdentMLFile(arg);
            }
            catch (Exception e) {
                System.out.println("Cannot validate file " + arg + "because " + e.getMessage());

            }
        }
    }
    // parse an XML document into a DOM tree

}
