package org.systemsbiology.xtandem;

import org.junit.*;
import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.sax.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.MZXMLReaderTest
 *
 * @author Steve Lewis
 * @date Dec 28, 2010
 */
public class MZXMLReaderTest
{
    public static MZXMLReaderTest[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MZXMLReaderTest.class;

    public static final String RESOURCE_XML = "10spectra_32.mzXML";
    public static final String RESOURCE_XML2 = "SmallNested.mzXML";

//    @Test
//     public void testRead10spectra_32()
//     {
//         final Class<? extends MZXMLReaderTest> aClass = getClass();
//         InputStream is = aClass.getResourceAsStream( RESOURCE_XML);
//         Assert.assertNotNull(is);
//         List<Spectrum> _vS = new ArrayList<Spectrum>();
//         SpectrumCondition _sC = new SpectrumCondition();
//         MScore _m = new MScore(IParameterHolder.NULL_PARAMETER_HOLDER);
//         SaxMzXMLHandler handler = new SaxMzXMLHandler(_vS, _sC, _m);
//         String fileName = (aClass.getName() + "." + RESOURCE_XML).replace(".","/");
//         handler.setStrFileName(fileName);
//         handler.parseDocument(is);
//
//         final int numScans = handler.getScanNum();
//         Assert.assertTrue(numScans == 10);
//
//       }
    @Test
     public void testReadSmallNested()
     {
         final Class<? extends MZXMLReaderTest> aClass = getClass();
         InputStream is = aClass.getResourceAsStream( RESOURCE_XML2);
           Assert.assertNotNull(is);
         DelegatingSaxHandler handler = new DelegatingSaxHandler();
          MzXMLHandler mxhandler = new MzXMLHandler(handler );
         handler.pushCurrentHandler(mxhandler);
          String fileName = (aClass.getName() + "." + RESOURCE_XML2).replace(".","/");
         handler.setStrFileName(fileName);
         handler.parseDocument(is);

         MassSpecRun[] runs = mxhandler.getElementObject();
           Assert.assertEquals(1,runs.length);
         MassSpecRun run = runs[0];
         RawPeptideScan[] scans = run.getScans();
         Arrays.sort(scans);
         Assert.assertEquals(935,scans.length);
         RawPeptideScan detailed = null;
         for (int i = 0; i < scans.length; i++) {
             RawPeptideScan scan = scans[i];
             Assert.assertEquals(2,scan.getMsLevel() );
         }

     }

}
