package org.systemsbiology.xtandem.comet;

//import org.proteios.io.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.testing.*;

import java.io.*;

/**
 * org.systemsbiology.xtandem.comet.CometUtilities
 * User: Steve
 * Date: 4/5/13
 */
public class CometUtilities {
    public static final CometUtilities[] EMPTY_ARRAY = {};

    public static RawPeptideScan[] readMzXML(String f)   {
        File file = new File(f);
        return readMzXML(file);

    }

    private static  RawPeptideScan[]  readMzXML(final File pFile) {
              return MZXMLReader.processFile(pFile);

    }

    public static void writeSpectra(final String pArg, final RawPeptideScan[] pSpectra) {
        try {
            PrintWriter pw = new PrintWriter(pArg + ".mgf");
            for (int j = 0; j < pSpectra.length; j++) {
                RawPeptideScan rs = pSpectra[j];
                rs.appendAsMGF(pw);
            }
            pw.close();
        }
        catch (FileNotFoundException e) {
            throw new RuntimeException(e);

        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            RawPeptideScan[] spectra = readMzXML(arg);
            writeSpectra(arg, spectra);

        }
     }




}
