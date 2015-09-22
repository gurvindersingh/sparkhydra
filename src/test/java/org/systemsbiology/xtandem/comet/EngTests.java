package org.systemsbiology.xtandem.comet;

import com.lordjoe.distributed.hydra.comet.*;
import com.lordjoe.distributed.protein.*;
import org.junit.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.comet.EngTests
 * User: Steve
 * Date: 9/11/2015
 */
public class EngTests {

    public static final String PLAIN_COMET_PROPERTIES = "# comet_version 2015.02 rev. 0\n" +
            "# Comet MS/MS search engine parameters file.\n" +
            "# Everything following the '#' symbol is treated as a comment.\n" +
            "\n" +
            "database_name = tmp.db\n" +
            "decoy_search = 0                       # 0=no (default), 1=concatenated search, 2=separate search\n" +
            "\n" +
            "num_threads = 1                        # 0=poll CPU to set num threads; else specify num threads directly (max 64)\n" +
            "\n" +
            "#\n" +
            "# masses\n" +
            "#\n" +
            "peptide_mass_tolerance = 3.00\n" +
            "peptide_mass_units = 0                 # 0=amu, 1=mmu, 2=ppm\n" +
            "mass_type_parent = 1                   # 0=average masses, 1=monoisotopic masses\n" +
            "mass_type_fragment = 1                 # 0=average masses, 1=monoisotopic masses\n" +
            "precursor_tolerance_type = 0           # 0=MH+ (default), 1=precursor m/z; only valid for amu/mmu tolerances\n" +
            "isotope_error = 0                      # 0=off, 1=on -1/0/1/2/3 (standard C13 error), 2= -8/-4/0/4/8 (for +4/+8 labeling)\n" +
            "\n" +
            "#\n" +
            "# search enzyme\n" +
            "#\n" +
            "search_enzyme_number = 0               # choose from list at end of this params file\n" +
            "num_enzyme_termini = 2                 # valid values are 1 (semi-digested), 2 (fully digested, default), 8 N-term, 9 C-term\n" +
            "allowed_missed_cleavage = 2            # maximum value is 5; for enzyme search\n" +
            "\n" +
            "#\n" +
            "# Up to 9 variable modifications are supported\n" +
            "# format:  <mass> <residues> <0=variable/1=binary> <max_mods_per_peptide> <term_distance> <n/c-term> <required>\n" +
            "#     e.g. 79.966331 STY 0 3 -1 0 0\n" +
            "#\n" +
            "variable_mod01 = 15.9949 M 0 3 -1 0 0\n" +
            "variable_mod02 = 0.0 X 0 3 -1 0 0\n" +
            "variable_mod03 = 0.0 X 0 3 -1 0 0\n" +
            "variable_mod04 = 0.0 X 0 3 -1 0 0\n" +
            "variable_mod05 = 0.0 X 0 3 -1 0 0\n" +
            "variable_mod06 = 0.0 X 0 3 -1 0 0\n" +
            "variable_mod07 = 0.0 X 0 3 -1 0 0\n" +
            "variable_mod08 = 0.0 X 0 3 -1 0 0\n" +
            "variable_mod09 = 0.0 X 0 3 -1 0 0\n" +
            "max_variable_mods_in_peptide = 5\n" +
            "require_variable_mod = 0\n" +
            "\n" +
            "#\n" +
            "# fragment ions\n" +
            "#\n" +
            "# ion trap ms/ms:  1.0005 tolerance, 0.4 offset (mono masses), theoretical_fragment_ions = 1\n" +
            "# high res ms/ms:    0.02 tolerance, 0.0 offset (mono masses), theoretical_fragment_ions = 0\n" +
            "#\n" +
            "fragment_bin_tol = 1.0005              # binning to use on fragment ions\n" +
            "fragment_bin_offset = 0.4              # offset position to start the binning (0.0 to 1.0)\n" +
            "theoretical_fragment_ions = 1          # 0=use flanking peaks, 1=M peak only\n" +
            "use_A_ions = 0\n" +
            "use_B_ions = 1\n" +
            "use_C_ions = 0\n" +
            "use_X_ions = 0\n" +
            "use_Y_ions = 1\n" +
            "use_Z_ions = 0\n" +
            "use_NL_ions = 1                        # 0=no, 1=yes to consider NH3/H2O neutral loss peaks\n" +
            "\n" +
            "#\n" +
            "# output\n" +
            "#\n" +
            "output_sqtstream = 1                   # 0=no, 1=yes  write sqt to standard output\n" +
            "output_sqtfile = 0                     # 0=no, 1=yes  write sqt file\n" +
            "output_txtfile = 0                     # 0=no, 1=yes  write tab-delimited txt file\n" +
            "output_pepxmlfile = 0                  # 0=no, 1=yes  write pep.xml file\n" +
            "output_percolatorfile = 0              # 0=no, 1=yes  write Percolator tab-delimited input file\n" +
            "output_outfiles = 0                    # 0=no, 1=yes  write .out files\n" +
            "print_expect_score = 1                 # 0=no, 1=yes to replace Sp with expect in out & sqt\n" +
            "num_output_lines = 5                   # num peptide results to show\n" +
            "show_fragment_ions = 0                 # 0=no, 1=yes for out files only\n" +
            "\n" +
            "sample_enzyme_number = 1               # Sample enzyme which is possibly different than the one applied to the search.\n" +
            "                                       # Used to calculate NTT & NMC in pepXML output (default=1 for trypsin).\n" +
            "\n" +
            "#\n" +
            "# mzXML parameters\n" +
            "#\n" +
            "scan_range = 0 0                       # start and scan scan range to search; 0 as 1st entry ignores parameter\n" +
            "precursor_charge = 0 0                 # precursor charge range to analyze; does not override any existing charge; 0 as 1st entry ignores parameter\n" +
            "override_charge = 0                    # 0=no, 1=yes to override existing precursor charge states with precursor_charge parameter\n" +
            "ms_level = 2                           # MS level to analyze, valid are levels 2 (default) or 3\n" +
            "activation_method = ALL                # activation method; used if activation method set; allowed ALL, CID, ECD, ETD, PQD, HCD, IRMPD\n" +
            "\n" +
            "#\n" +
            "# misc parameters\n" +
            "#\n" +
            "digest_mass_range = 600.0 5000.0       # MH+ peptide mass range to analyze\n" +
            "num_results = 100                      # number of search hits to store internally\n" +
            "skip_researching = 1                   # for '.out' file output only, 0=search everything again (default), 1=don't search if .out exists\n" +
            "max_fragment_charge = 3                # set maximum fragment charge state to analyze (allowed max 5)\n" +
            "max_precursor_charge = 6               # set maximum precursor charge state to analyze (allowed max 9)\n" +
            "nucleotide_reading_frame = 0           # 0=proteinDB, 1-6, 7=forward three, 8=reverse three, 9=all six\n" +
            "clip_nterm_methionine = 0              # 0=leave sequences as-is; 1=also consider sequence w/o N-term methionine\n" +
            "spectrum_batch_size = 0                # max. # of spectra to search at a time; 0 to search the entire scan range in one loop\n" +
            "decoy_prefix = DECOY_                  # decoy entries are denoted by this string which is pre-pended to each protein accession\n" +
            "output_suffix =                        # add a suffix to output base names i.e. suffix \"-C\" generates base-C.pep.xml from base.mzXML input\n" +
            "mass_offsets =                         # one or more mass offsets to search (values substracted from deconvoluted precursor mass)\n" +
            "\n" +
            "#\n" +
            "# spectral processing\n" +
            "#\n" +
            "minimum_peaks = 10                     # required minimum number of peaks in spectrum to search (default 10)\n" +
            "minimum_intensity = 0                  # minimum intensity value to read in\n" +
            "remove_precursor_peak = 0              # 0=no, 1=yes, 2=all charge reduced precursor peaks (for ETD)\n" +
            "remove_precursor_tolerance = 1.5       # +- Da tolerance for precursor removal\n" +
            "clear_mz_range = 0.0 0.0               # for iTRAQ/TMT type data; will clear out all peaks in the specified m/z range\n" +
            "\n" +
            "#\n" +
            "# additional modifications\n" +
            "#\n" +
            "\n" +
            "add_Cterm_peptide = 0.0\n" +
            "add_Nterm_peptide = 0.0\n" +
            "add_Cterm_protein = 0.0\n" +
            "add_Nterm_protein = 0.0\n" +
            "\n" +
            "add_G_glycine = 0.0000                 # added to G - avg.  57.0513, mono.  57.02146\n" +
            "add_A_alanine = 0.0000                 # added to A - avg.  71.0779, mono.  71.03711\n" +
            "add_S_serine = 0.0000                  # added to S - avg.  87.0773, mono.  87.03203\n" +
            "add_P_proline = 0.0000                 # added to P - avg.  97.1152, mono.  97.05276\n" +
            "add_V_valine = 0.0000                  # added to V - avg.  99.1311, mono.  99.06841\n" +
            "add_T_threonine = 0.0000               # added to T - avg. 101.1038, mono. 101.04768\n" +
            "add_C_cysteine = 57.021464             # added to C - avg. 103.1429, mono. 103.00918\n" +
            "add_L_leucine = 0.0000                 # added to L - avg. 113.1576, mono. 113.08406\n" +
            "add_I_isoleucine = 0.0000              # added to I - avg. 113.1576, mono. 113.08406\n" +
            "add_N_asparagine = 0.0000              # added to N - avg. 114.1026, mono. 114.04293\n" +
            "add_D_aspartic_acid = 0.0000           # added to D - avg. 115.0874, mono. 115.02694\n" +
            "add_Q_glutamine = 0.0000               # added to Q - avg. 128.1292, mono. 128.05858\n" +
            "add_K_lysine = 0.0000                  # added to K - avg. 128.1723, mono. 128.09496\n" +
            "add_E_glutamic_acid = 0.0000           # added to E - avg. 129.1140, mono. 129.04259\n" +
            "add_M_methionine = 0.0000              # added to M - avg. 131.1961, mono. 131.04048\n" +
            "add_O_ornithine = 0.0000               # added to O - avg. 132.1610, mono  132.08988\n" +
            "add_H_histidine = 0.0000               # added to H - avg. 137.1393, mono. 137.05891\n" +
            "add_F_phenylalanine = 0.0000           # added to F - avg. 147.1739, mono. 147.06841\n" +
            "add_R_arginine = 0.0000                # added to R - avg. 156.1857, mono. 156.10111\n" +
            "add_Y_tyrosine = 0.0000                # added to Y - avg. 163.0633, mono. 163.06333\n" +
            "add_W_tryptophan = 0.0000              # added to W - avg. 186.0793, mono. 186.07931\n" +
            "add_B_user_amino_acid = 0.0000         # added to B - avg.   0.0000, mono.   0.00000\n" +
            "add_J_user_amino_acid = 0.0000         # added to J - avg.   0.0000, mono.   0.00000\n" +
            "add_U_user_amino_acid = 0.0000         # added to U - avg.   0.0000, mono.   0.00000\n" +
            "add_X_user_amino_acid = 0.0000         # added to X - avg.   0.0000, mono.   0.00000\n" +
            "add_Z_user_amino_acid = 0.0000         # added to Z - avg.   0.0000, mono.   0.00000\n" +
            "\n" +
            "#\n" +
            "# COMET_ENZYME_INFO _must_ be at the end of this parameters file\n" +
            "#\n" +
            "[COMET_ENZYME_INFO]\n" +
            "0.  No_enzyme              0      -           -\n" +
            "1.  Trypsin                1      KR          P\n" +
            "2.  Trypsin/P              1      KR          -\n" +
            "3.  Lys_C                  1      K           P\n" +
            "4.  Lys_N                  0      K           -\n" +
            "5.  Arg_C                  1      R           P\n" +
            "6.  Asp_N                  0      D           -\n" +
            "7.  CNBr                   1      M           -\n" +
            "8.  Glu_C                  1      DE          P\n" +
            "9.  PepsinA                1      FL          P\n" +
            "10. Chymotrypsin           1      FWYL        P\n" +
            "\n";

    public static final String PLAIN_SCAN = "scan num=\"1938\"\n" +
            "          scanType=\"Full\"\n" +
            "          centroided=\"1\"\n" +
            "          msLevel=\"2\"\n" +
            "          peaksCount=\"371\"\n" +
            "          retentionTime=\"PT142135S\"\n" +
            "          lowMz=\"387.110229\"\n" +
            "          highMz=\"1994.289795\"\n" +
            "          basePeakMz=\"1356.542603\"\n" +
            "          basePeakIntensity=\"676.220642\"\n" +
            "          totIonCurrent=\"10484.953182999992\">\n" +
            "      <precursorMz precursorIntensity=\"0\">1377.04</precursorMz>\n" +
            "      <peaks compressionType=\"none\"\n" +
            "             compressedLen=\"0\"\n" +
            "             precision=\"64\"\n" +
            "             byteOrder=\"network\"\n" +
            "             contentType=\"m/z-int\">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</peaks>\n" +
            "      <nameValue name=\"ms2 file charge state\" value=\"2 1377.04\"/>\n" +
            "    </scan>";

    public static final IProtein PLAIN_PROTEIN = Protein.buildProtein("tmp001", "tmp", "EPGCGCCMTCALAEGQSCGVYTER", "fpp");

    @Test
    public void testPlain() throws Exception {
        IProtein proteinStr = PLAIN_PROTEIN;
        String scanStr = PLAIN_SCAN;
        String paramsStr = PLAIN_COMET_PROPERTIES;

        XTandemMain main = CometTestingUtilities.getDefaultApplication();

        InputStream is = EngTests.class.getResourceAsStream("/plain/comet.params.2015020");

        Assert.assertNotNull(is);
        CometUtilities.fromCometParameters(main, is);
        // add Comet Parameters
        //CometUtilities.fromCometParameters(main, Util.asLineReader(paramsStr));
        List<RawPeptideScan> allScanFromMZXMLResource = CometTestingUtilities.getAllScanFromMZXMLResource("/plain/input.mzXML");
        Assert.assertEquals(1,allScanFromMZXMLResource.size());
        RawPeptideScan scan =  allScanFromMZXMLResource.get(0);
        Assert.assertNotNull(scan);

        List<IProtein> proteins = ProteinParser.getProteinsFromResource("/plain/tmp.db");
        Assert.assertEquals(1,proteins.size());

          IProtein protein = proteinStr;


    }

}
