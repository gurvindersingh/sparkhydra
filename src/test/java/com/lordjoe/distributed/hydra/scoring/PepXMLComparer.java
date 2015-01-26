package com.lordjoe.distributed.hydra.scoring;

import org.systemsbiology.xtandem.fdr.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.scoring.PepXMLComparer
 * User: Steve
 * Date: 1/24/2015
 */
public class PepXMLComparer {

    public static ProteinPepxmlParser readOnePepXML(String file)
    {
        boolean onlyUniquePeptides = false;
            ProteinPepxmlParser fdrParser = new ProteinPepxmlParser(file);
          fdrParser.readFileAndGenerate(onlyUniquePeptides);
         return fdrParser;
    }

    public static void main(String[] args) {
        List<ProteinPepxmlParser> holder = new ArrayList<ProteinPepxmlParser>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            holder.add(readOnePepXML(arg));
        }

        if(holder.size() <= 1)
            return;

        ProteinPepxmlParser p1 = holder.get(0);
        ProteinPepxmlParser p2 = holder.get(1);
        Map<String, ProteinPepxmlParser.SpectrumHit> spectrumHits = p1.getSpectrumHits();
        Map<String, ProteinPepxmlParser.SpectrumHit> spectrumHits2 = p2.getSpectrumHits();
        for (String s : spectrumHits2.keySet()) {
            ProteinPepxmlParser.SpectrumHit spectrumHit = spectrumHits.get(s);
            ProteinPepxmlParser.SpectrumHit spectrumHit2 = spectrumHits2.get(s);
            System.out.println(spectrumHit + "\n" + spectrumHit2 + "\n\n");
         }

    }

}
