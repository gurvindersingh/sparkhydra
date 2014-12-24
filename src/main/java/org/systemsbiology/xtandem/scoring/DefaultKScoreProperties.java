package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.DefaultKScoreProperties
 *  class to hardcode isb kscore defaults
 * @author Steve Lewis
 * @date 31/05/13
 */
public class DefaultKScoreProperties {
    public static final String IMPLEMENTED_DEFAULT_FILE = "isb_default_input_kscore.xml";
    
    public static final String DEFAULT_PROPERTIES_STRING =
            "spectrum, parent monoisotopic mass error minus=2.0\n" +
                    "spectrum, parent monoisotopic mass error plus=4.0\n" +
                    "spectrum, parent monoisotopic mass isotope error=no\n" +
                    "spectrum, fragment monoisotopic mass error units=Daltons\n" +
                    "spectrum, parent monoisotopic mass error units=Daltons\n" +
                    "spectrum, fragment monoisotopic mass error=0.4\n" +
                    "spectrum, fragment mass type=monoisotopic\n" +
                    "spectrum, dynamic range=10000.0\n" +
                    "spectrum, total peaks=400 \n" +
                    "spectrum, maximum parent charge=5\n" +
                    "spectrum, use noise suppression=yes\n" +
                    "spectrum, minimum parent m+h=600.0\n" +
                    "spectrum, minimum fragment mz=125.0\n" +
                    "spectrum, minimum peaks=10 \n" +
                    "spectrum, threads=1\n" +
                    "spectrum, sequence batch size=1000\n" +
                    "protein, cleavage site=[RK]|{P}\n" +
                    "protein, homolog management=no\n" +
                    "refine=no\n" +
                    "refine, tic percent=10\n" +
                    "refine, spectrum synthesis=yes\n" +
                    "refine, maximum valid expectation value=0.1\n" +
                    "refine, unanticipated cleavage=no\n" +
                    "refine, point mutations=no\n" +
                    "refine, use potential modifications for full refinement=no\n" +
                    "refine, point mutations=no\n" +
                    "scoring, minimum ion count=1\n" +
                    "scoring, maximum missed cleavage sites=2\n" +
                    "scoring, x ions=no\n" +
                    "scoring, y ions=yes\n" +
                    "scoring, z ions=no\n" +
                    "scoring, a ions=no\n" +
                    "scoring, b ions=yes\n" +
                    "scoring, c ions=no\n" +
                    "scoring, cyclic permutation=no\n" +
                    "scoring, include reverse=no\n" +
                    "output, message=1234567890\n" +
                    "output, sort results by=spectrum\n" +
                    "output, path hashing=no\n" +
                    "output, xsl path=tandem-style.xsl\n" +
                    "output, parameters=yes\n" +
                    "output, performance=yes\n" +
                    "output, spectra=yes\n" +
                    "output, histograms=no\n" +
                    "output, proteins=yes\n" +
                    "output, sequences=no\n" +
                    "output, one sequence copy=no\n" +
                    "output, results=all\n" +
                    "output, maximum valid expectation value=0.1\n" +
                    "output, histogram column width=30\n"  
                   ;

    private static Properties g_DefaultProperties;

    protected synchronized static Properties getDefaultProperties() {
        try {
            if(g_DefaultProperties == null)    {
                g_DefaultProperties = new Properties();
                g_DefaultProperties.load(new StringReader(DEFAULT_PROPERTIES_STRING));
            }
            return g_DefaultProperties;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * add any default properties not currently defined
     * @param holder
     */
    public static void addDefaultProperties(Map<String, String> holder)
    {
        Properties defaultProperties = getDefaultProperties();
        for(String key : defaultProperties.stringPropertyNames())  {
            if(!holder.containsKey(key) )   {
                holder.put(key,defaultProperties.getProperty(key));
            }
         }

    }
}
