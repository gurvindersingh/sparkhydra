package org.systemsbiology.xtandem.scoring;

import com.lordjoe.distributed.hydra.*;
import org.junit.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.PaptideScoringTest
 * User: Steve
 * Date: 1/3/2015
 */
public class PeptideScoringTest {
    public static final String TEST_SPECTRUM =
            "<MeasuredSpectrum  PrecursorCharge=\"2\" PrecursorMass=\"1071.333\" >\n" +
                    "    <peaks  >QxYLlj3CYzNDHxB8PdDaA0MnDgk+mhiLQ0Td7z3EIUdDRtzkPf9g40NZFVk9u205Q10VyT4PbANDXxDiPiowi0NhC3I+QhZoQ2vj8T3uBCxDbxhAPpG7O0N6zc8+Blx9Q32jcT3aOaRDgGkrPgD+f0OBaIU99EGBQ4yGnj6R7MpDjoENPgxaB0OViA8+lBMiQ5YH2z3rF+dDmWHHPawtxUOaYX897UkuQ5thnj2nEEVDrH1JPflwlkOxiRg9eFIJQ7Jixz1g3pJDs4NVPdHRdkO3XKA9btPbQ7hbwz4ZsB1Dz4Q/PptU7EP+tow+nHrT</peaks>\n" +
                    "</MeasuredSpectrum>\n";

    public static final String[] TEST_PEPTIDES = {
            "DTGLIVLIAR",
            "TNQVVIGLVK",
            "VLGIVVQNTK",
            "LTNLLQQIK",
            "TVVTPVILTK",
            "TALLLAVELK",
            "EIVQIILSR",
            "VAITAVILDR",
            "LEVALLLATK",
            "SLIIQVIER",
            "NVTLILVGNK",
            "ILFKPLLAR",
            "LLVWLADLK",
            "TLLLLSSVPK",
            "VIVQLVTNGK",
            "SLPTLASILR",
            "IQQLAISGLK",
            "VAIVVSILEK",
            "LGSIALQQIK",
            "ELISVVIAVK",
            "LTQGLIQLGK",
            "GLQILGQTLK",
            "ILLEVLSQR",
            "GNTVLQVIVK",
            "GLNTTVIKPK",
            "QLSILEVLR",
            "IQQLLNTLK",
            "LISALTPLSR",
            "LELGIVSLVK",
            "LQIGLTGALGK",
            "VLSVIGLELK",
            "VLIVSLAISR",
            "SIALSVILVR",
            "SSILSPLLLK",
            "ILSTPVVTLK"
    };

    public static final String TANDEM_XML =
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                    "\n" +
                    "<bioml>\n" +
                    " \n" +
                    " <note> FILE LOCATIONS. Replace them with your input (.mzXML) file and output file -- these are REQUIRED. Optionally a log file and a sequence output file of all protein sequences identified in the first-pass can be specified. Use of FULL path (not relative) paths is recommended. </note>\n" +
                    "\n" +
                    " <!--   <note type=\"input\" label=\"spectrum, path\">LargeSample/OR20080317_S_SILAC-LH_1-1_01.mzXML</note>   -->\n" +
                    "   <!-- \n" +
                    "\t<note type=\"input\" label=\"spectrum, path\">or20080317_s_silac-lh_1-1_01short.mzxml</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, path\">SmallSample/one_spectrum.mzxml</note> \n" +
                    "\t<note type=\"input\" label=\"output, path\">yeast_orfs_all_REV01_short.xml</note> \n" +
                    " \t<note type=\"input\" label=\"protein, taxon\">yeast_orfs_pruned.fasta</note>\n" +
                    "\t-->\n" +
                    "\t<note type=\"input\" label=\"protein, taxon\">yeast_orfs_all_REV.20060126.short.fasta</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, path\">spectra</note>\n" +
                    "\t<note type=\"input\" label=\"output, path\">full_tandem_output_path</note>\n" +
                    "\t<note type=\"input\" label=\"output, sequence path\">full_tandem_output_sequence_path</note>\n" +
                    "\n" +
                    "\t<note type=\"input\" label=\"list path, taxonomy information\">taxonomy.xml</note>\n" +
                    "\n" +
                    "\n" +
                    "\t\n" +
                    " \t<note type=\"input\" label=\"output, histogram column width\">30</note>\n" +
                    "\t<note type=\"input\" label=\"output, histograms\">yes</note>\n" +
                    "\t<note type=\"input\" label=\"output, log path\"></note>\n" +
                    "\t<note type=\"input\" label=\"output, maximum valid expectation value\">0.1</note>\n" +
                    "\t<note type=\"input\" label=\"output, message\">1234567890</note>\n" +
                    "\t<note type=\"input\" label=\"output, one sequence copy\">no</note>\n" +
                    "\t<note type=\"input\" label=\"output, parameters\">yes</note>\n" +
                    " \t<note type=\"input\" label=\"output, path hashing\">yes</note>\n" +
                    "\t<note type=\"input\" label=\"output, performance\">yes</note>\n" +
                    "\t<note type=\"input\" label=\"output, proteins\">yes</note>\n" +
                    "\t<note type=\"input\" label=\"output, results\">all</note>\n" +
                    "\t<note type=\"input\" label=\"output, sequences\">no</note>\n" +
                    "\t<note type=\"input\" label=\"output, sort results by\">spectrum</note>\n" +
                    "\t<note type=\"input\" label=\"output, spectra\">yes</note>\n" +
                    "\t<note type=\"input\" label=\"output, xsl path\">tandem-style.xsl</note>\n" +
                    "\t<note type=\"input\" label=\"protein, C-terminal residue modification mass\"></note>\n" +
                    "\t<note type=\"input\" label=\"protein, N-terminal residue modification mass\"></note>\n" +
                    "\t<note type=\"input\" label=\"protein, cleavage semi\">no</note>\n" +
                    "    <!--<note type=\"input\" label=\"protein, cleavage site\">[RK]|{P}</note>     -->\n" +
                    "    <note type=\"input\" label=\"protein, cleavage site\">[K]</note>\n" +
                    "\t<note type=\"input\" label=\"protein, homolog management\">no</note>\n" +
                    "\t<note type=\"input\" label=\"protein, modified residue mass file\"></note>\n" +
                    "\t<note type=\"input\" label=\"refine\">no</note>\n" +
                    "\t<note type=\"input\" label=\"refine, maximum valid expectation value\">0.1</note>\n" +
                    "\t<note type=\"input\" label=\"refine, potential modification mass\">15.994915@M,8.014199@K,10.008269@R,79.9663@H,79.9663@R,79.9663@T,79.9663@Y,79.9663@K</note>\n" +
                    "\t<note type=\"input\" label=\"refine, sequence path\"></note>\n" +
                    "\t<note type=\"input\" label=\"refine, spectrum synthesis\">yes</note>\n" +
                    "\t<note type=\"input\" label=\"residue, modification mass\">57.021464@C</note>\n" +
                    "\t<note type=\"input\" label=\"residue, potential modification mass\">79.9663@H,79.9663@R,79.9663@T,79.9663@Y,79.9663@K</note>\n" +
                    "\t<note type=\"input\" label=\"residue, potential modification motif\"></note>\n" +
                    "\t<note type=\"input\" label=\"scoring, a ions\">no</note>\n" +
                    "\t<note type=\"input\" label=\"scoring, algorithm\">k-score</note>\n" +
                    "\t<note type=\"input\" label=\"scoring, b ions\">yes</note>\n" +
                    "\t<note type=\"input\" label=\"scoring, c ions\">no</note>\n" +
                    "\t<note type=\"input\" label=\"scoring, cyclic permutation\">no</note>\n" +
                    "\t<note type=\"input\" label=\"scoring, include reverse\">no</note>\n" +
                    "\t<note type=\"input\" label=\"scoring, maximum missed cleavage sites\">2</note>\n" +
                    "\t<note type=\"input\" label=\"scoring, minimum ion count\">1</note>\n" +
                    "\t<note type=\"input\" label=\"scoring, x ions\">no</note>\n" +
                    "\t<note type=\"input\" label=\"scoring, y ions\">yes</note>\n" +
                    "\t<note type=\"input\" label=\"scoring, z ions\">no</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, dynamic range\">10000.0</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, fragment mass type\">monoisotopic</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, fragment monoisotopic mass error\">0.4</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, fragment monoisotopic mass error units\">Daltons</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, maximum parent charge\">5</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, minimum fragment mz\">125.0</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, minimum parent m+h\">600.0</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, minimum peaks\">10</note>\n" +
                    "<note> PRECURSOR MASS TOLERANCES. In the example below, a -2.0 Da to 4.0 Da (monoisotopic mass) window is searched for peptide candidates. Since this is monoisotopic mass, so for non-accurate-mass instruments, for which the precursor is often taken nearer to the isotopically averaged mass, an asymmetric tolerance (-2.0 Da to 4.0 Da) is preferable. This somewhat imitates a (-3.0 Da to 3.0 Da) window for averaged mass (but not exactly)</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, parent monoisotopic mass error minus\">2.0</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, parent monoisotopic mass error plus\">4.1</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, parent monoisotopic mass error units\">Daltons</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, parent monoisotopic mass isotope error\">no</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, sequence batch size\">1000</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, threads\">1</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, total peaks\">400</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, use conditioning\">no</note>\n" +
                    "\t<note type=\"input\" label=\"spectrum, use noise suppression\">yes</note>\n" +
                    "</bioml>\n" +
                    "\n";

    /**
     * code to work on scoring from a lits of IPolypepdides or whatever
     * @throws Exception
     */
    @Test
    public void testScoring() throws Exception {
         IMeasuredSpectrum scan = XTandemHadoopUtilities.parseXMLString(TEST_SPECTRUM, new TopLevelMeasuredSpectrumHandler( ));

        InputStream is = new StringBufferInputStream(TANDEM_XML);
        XTandemMain application = new SparkXTandemMain(is, "TANDEM_XML");

        Assert.assertNotNull(scan);
        Assert.assertTrue(scan.getPrecursorCharge() > 0 );
        Assert.assertTrue(scan.getPrecursorMass() > 0 );

         SequenceUtilities sequenceUtilities = new SequenceUtilities(application);


        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (int i = 0; i < TEST_PEPTIDES.length; i++) {
            String testPeptide = TEST_PEPTIDES[i];
            IPolypeptide pp = Polypeptide.fromString(testPeptide);
            holder.add(pp);
        }

        List<ITheoreticalSpectrumSet> theoretical = new ArrayList<ITheoreticalSpectrumSet>();
        for (IPolypeptide pp : holder) {
            theoretical.add(generateTheoreticalSet(pp,scan.getPrecursorCharge(),sequenceUtilities));
        }

        Scorer scoreRunner = application.getScoreRunner();
        ITandemScoringAlgorithm algorithm = application.getScorer();
        IScoredScan score = scoreScan(scan,theoretical,scoreRunner);
        Assert.assertNotNull(score);
        Assert.assertNotNull(score.getBestMatch());


    }

    public static IScoredScan scoreScan(final IMeasuredSpectrum scan, final List<ITheoreticalSpectrumSet> pTheoretical,
                                        Scorer scorer) {
        ITandemScoringAlgorithm  algorithm = (ITandemScoringAlgorithm)scorer.getAlgorithm();
        OriginatingScoredScan scoring = new OriginatingScoredScan( scan);
        scoring.setAlgorithm(algorithm.getName());
         IonUseCounter counter = new IonUseCounter();
        ITheoreticalSpectrumSet[] tss = pTheoretical.toArray(new ITheoreticalSpectrumSet[pTheoretical.size()]);
        int numberDotProducts = algorithm.scoreScan(scorer, counter, tss, scoring);
         return scoring;
     }

    public static ITheoreticalSpectrumSet generateTheoreticalSet(final IPolypeptide peptide,int charge,SequenceUtilities sequenceUtilities) {
        ITheoreticalSpectrumSet ret = new TheoreticalSpectrumSet(charge,peptide.getMatchingMass(),peptide);
        for (int i = 0; i < charge - 1; i++) {
            PeptideSpectrum ps = new PeptideSpectrum(ret, i + 1, IonType.B_ION_TYPES,
                   sequenceUtilities);
            ITheoreticalSpectrum conditioned = ScoringUtilities.applyConditioner(ps,
                    new XTandemTheoreticalScoreConditioner());

        }
              return ret;
    }
}
