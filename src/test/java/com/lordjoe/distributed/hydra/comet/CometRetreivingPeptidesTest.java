package com.lordjoe.distributed.hydra.comet;

import com.lordjoe.distributed.protein.ProteinParser;
import org.junit.Assert;
import org.junit.Test;
import org.systemsbiology.xtandem.peptide.IPeptideDigester;
import org.systemsbiology.xtandem.peptide.IPolypeptide;
import org.systemsbiology.xtandem.peptide.IProtein;
import org.systemsbiology.xtandem.peptide.PeptideBondDigester;

import java.util.List;

/**
 * com.lordjoe.distributed.hydra.comet.CometRereivingPeptidesTest
 *
 * @author Steve Lewis
 * @date 5/24/2015
 */
public class CometRetreivingPeptidesTest {
    @Test
    public void testReadProteins() throws Exception {
        List<IProtein> proteins = ProteinParser.getProteinsFromResource("/2Protein.fasta");
        Assert.assertEquals(2, proteins.size());

        List<IProtein> proteins2 = ProteinParser.getProteinsFromResource("/select_20.fasta");
        Assert.assertEquals(28, proteins2.size());

        List<IProtein> peptides2 = ProteinParser.getProteinsFromResource("/SmallSampleProteins.fasta");
        Assert.assertEquals(286, peptides2.size());

    }

    @Test
    public void testReadPeptides() throws Exception {
        IPeptideDigester digester =  PeptideBondDigester.getDefaultDigester() ;
        digester.setNumberMissedCleavages(2);
        List<IPolypeptide> peptides = ProteinParser.getPeptidesFromResource("/2Protein.fasta", digester);
        Assert.assertEquals(283, peptides.size());

        List<IPolypeptide> peptides2 = ProteinParser.getPeptidesFromResource("/SmallSampleProteins.fasta", digester);
        Assert.assertEquals(77017, peptides2.size());

        List<IPolypeptide> proteins2 = ProteinParser.getPeptidesFromResource("/select_20.fasta", digester,
                CometTestingUtilities.MSTV_ONLY);
        Assert.assertEquals(194836, proteins2.size());


    }




}
