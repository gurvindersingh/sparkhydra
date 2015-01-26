package com.lordjoe.distributed.hydra.scoring;

import org.systemsbiology.xtandem.peptide.*;

/**
 * com.lordjoe.distributed.hydra.scoring.ScanScoreTimingTest
 * Scoring a large sample requires about 20G   scorings
 * User: Steve
 * Date: 1/20/2015
 */
public class ScanScoreTimingTest {
    IPolypeptide[] pps = {
            Polypeptide.fromString("E[-18.01]VSS"), //131104_Berit_BSA2.5408.5408.3
            Polypeptide.fromString("GNGR"), //131104_Berit_BSA2.5408.5408.3
            Polypeptide.fromString("IGLT"), //131104_Berit_BSA2.5408.5408.3
            Polypeptide.fromString("IGLT"), //131104_Berit_BSA2.5586.5586.3
            Polypeptide.fromString("IIAS"), //131104_Berit_BSA2.5408.5408.3
            Polypeptide.fromString("IIAS"), //131104_Berit_BSA2.5586.5586.3
            Polypeptide.fromString("SALL"), //131104_Berit_BSA2.5408.5408.3
            Polypeptide.fromString("SALL"), //131104_Berit_BSA2.8868.8868.3
            Polypeptide.fromString("SPSL"), //131104_Berit_BSA2.5408.5408.3
            Polypeptide.fromString("VGPM"), //131104_Berit_BSA2.5408.5408.3
            Polypeptide.fromString("VGPM"), //131104_Berit_BSA2.5586.5586.3
            Polypeptide.fromString("GGGGR"), //131104_Berit_BSA2.5408.5408.3
            Polypeptide.fromString("GGGGR"), //131104_Berit_BSA2.5586.5586.3
            Polypeptide.fromString("GAAGK"), //131104_Berit_BSA2.6491.6491.3
     };



}
