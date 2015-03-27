package org.systemsbiology.xtandem.taxonomy;

import org.systemsbiology.xml.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.taxonomy.FastaParser
 * User: Steve
 * Date: Apr 11, 2011
 */
public class FastaParser implements IFastaHandler{
    public static final FastaParser[] EMPTY_ARRAY = {};

    private final List<IFastaHandler> m_Handlers = new ArrayList<IFastaHandler>();

    public FastaParser() {
    }

    public void addHandler(IFastaHandler added)
    {
        m_Handlers.add(added);
    }

    public void parseFastaFile(InputStream is,String url)
    {
        LineNumberReader inp = new LineNumberReader(new InputStreamReader(is));
        int numberProtein = 0;
        StringBuilder sb = new StringBuilder();
          String annotation = null;
            try {
              String line = inp.readLine();
              while (line != null) {
                  if (line.startsWith(">")) {
                      if(annotation != null && sb.length() > 0) {
                          numberProtein = showProteinProgress(numberProtein);
                            handleProtein(annotation,sb.toString(),url);
                          sb.setLength(0);
                      }
                      annotation = line.substring(1); // annotation is the rest o fthe line
                     }

                  else {
                      sb.append(line.trim()); // add to sequence
                  }
                  line = inp.readLine();

                }
                // don finish last protein if any
                if(annotation != null && sb.length() > 0) {
                     handleProtein(annotation,sb.toString());
                   numberProtein = showProteinProgress(numberProtein);
                    sb.setLength(0);
                 }
              }
          catch (IOException ex) {
              throw new RuntimeException(ex);
          }

          finally {
              try {
                  is.close();
              }
              catch (IOException e) {
                  throw new RuntimeException(e);
              }
          }

    }

    public static final int SHOWN_NUMBER = 200;

    protected static int showProteinProgress(int pNumberProtein) {
        if(++pNumberProtein % SHOWN_NUMBER == 0)  {
            XMLUtilities.outputText(".");
            if(pNumberProtein % (80 * SHOWN_NUMBER) == 0)
                XMLUtilities.outputLine();
        }
        return pNumberProtein;
    }


    @Override
    public void handleProtein(final String annotation,   String sequence ) {
        handleProtein( annotation,  sequence,"");
    }

    @Override
    public void handleProtein(final String annotation,   String sequence,String url) {
        sequence = sequence.trim();
        if(sequence.length() == 0)
            return;
         for(IFastaHandler handler : m_Handlers) {
              handler.handleProtein(annotation,  sequence,url);
         }
    }
}
