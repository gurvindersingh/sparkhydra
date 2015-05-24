package com.lordjoe.distributed.hydra.scoring;

import org.systemsbiology.xtandem.fdr.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.scoring.PepXMLComparer
 * User: Steve
 * Date: 1/24/2015
 */
public class PepXMLComparer {

    public static ProteinPepxmlParser readOnePepXML(String file) {
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

        if (holder.size() <= 1)
            return;

        ProteinPepxmlParser p1 = holder.get(0);
        ProteinPepxmlParser p2 = holder.get(1);
        Map<String, List<ProteinPepxmlParser.SpectrumHit>> spectrumHits1 = p1.getSpectrumHits();
        Map<String, List<ProteinPepxmlParser.SpectrumHit>> spectrumHits2 = p2.getSpectrumHits();
        List<String> keys = new ArrayList<String>(spectrumHits1.keySet());
        Collections.sort(keys);
        for (String s : keys) {
            List<ProteinPepxmlParser.SpectrumHit> spectrumHits = spectrumHits1.get(s);
            if (spectrumHits != null)
                for (ProteinPepxmlParser.SpectrumHit spectrumHit : spectrumHits) {
                    System.out.println(spectrumHit);

                }
            System.out.println();
            spectrumHits = spectrumHits2.get(s);
            if (spectrumHits != null)
                for (ProteinPepxmlParser.SpectrumHit spectrumHit : spectrumHits) {
                    System.out.println(spectrumHit );

                }
            System.out.println();
            System.out.println();
        }

    }

}
