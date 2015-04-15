package com.lordjoe.distributed.test;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.test.CometSpectraUse
 * User: Steve
 * Date: 4/14/2015
 */
public class CometSpectraUse {


    private final Map<String, CometSpectralScoring> idToUse = new HashMap<String, CometSpectralScoring>();

    public CometSpectraUse(File uses) {
        try {
            if (uses == null || !uses.exists()) {
                return;
            }
            LineNumberReader rdr = new LineNumberReader((new FileReader(uses)));
            String line = rdr.readLine();
            while (line != null) {
                handleLine(line);
                line = rdr.readLine();
            }
            rdr.close();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public void handleLine(String line) {
        CometSpectralScoring use = new CometSpectralScoring(line);
        idToUse.put(use.getIdString(), use);
    }

    public  CometSpectralScoring getScoring(IMeasuredSpectrum spec,IPolypeptide pp)
    {
        String id = spec.getId();
        Integer idx = new Integer(id);
        String idb = "" + idx   + ":" + pp.getSequence();
        return idToUse.get(idb);
    }


    public static void main(String[] args) throws Exception {
        File uses = new File(args[0]);
        CometSpectraUse cs = new CometSpectraUse(uses);

    }

}
