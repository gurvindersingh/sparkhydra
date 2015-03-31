package org.systemsbiology.xtandem.comet;

import org.junit.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.comet.CometParameterTests
 * User: steven
 * Date: 4/2/13
 */
public class CometParameterTests {
    public static final CometParameterTests[] EMPTY_ARRAY = {};

    public static final String TEST_PARAMS_FILE =

            "# Comet MS/MS search engine parameters file.\n" +
                    "# Everything following the '#' symbol is treated as a comment.\n" +
                    "\n" +
                    "database_name =  Homo_sapiens_non-redundant.GRCh37.68.pep.all_FPKM_SNV-cRAP_targetdecoy.fasta\n" +
                    "\n" +
                    "decoy_search = 0                       # 0=no (default), 1=concatenated search, 2=separate search\n" +
                    "\n" +
                    "num_threads = 8                        # 0=poll CPU to set num threads; else specify num threads directly (max 32)\n" +
                    "\n" +
                    "#\n" +
                    "# masses\n" +
                    "#\n" +
                    "peptide_mass_tolerance = 20\n" +
                    "peptide_mass_units = 2                 # 0=amu, 1=mmu, 2=ppm\n" +
                    "mass_type_parent = 1                   # 0=average masses, 1=monoisotopic masses\n" +
                    "mass_type_fragment = 1                 # 0=average masses, 1=monoisotopic masses\n" +
                    "precursor_tolerance_type = 0           # 0=MH+ (default), 1=precursor m/z\n" +
                    "isotope_error = 0                      # 0=off, 1=on -1/0/1/2/3 (standard C13 error), 2= -8/-4/0/4/8 (for +4/+8 labeling)\n" +
                    "\n" +
                    "#\n" +
                    "# search enzyme\n" +
                    "#\n" +
                    "search_enzyme_number = 1               # choose from list at end of this params file\n" +
                    "num_enzyme_termini = 1                 # valid values are 1 (semi-digested), 2 (fully digested, default), 8 N-term, 9 C-term\n" +
                    "allowed_missed_cleavage = 2            # maximum value is 5; for enzyme search\n" +
                    "\n" +
                    "#\n" +
                    "# Up to 6 variable modifications are supported\n" +
                    "# format:  <mass> <residues> <0=variable/1=binary> <max mods per a peptide>\n" +
                    "#     e.g. 79.966331 STY 0 3\n" +
                    "#\n" +
                    "variable_mod1 = 15.994915 M 0 3\n" +
                    "variable_mod2 = 0.984016 N 0 3\n" +
                    "variable_mod3 = 0.0 X 0 3\n" +
                    "variable_mod4 = 0.0 X 0 3\n" +
                    "variable_mod5 = 0.0 X 0 3\n" +
                    "variable_mod6 = 0.0 X 0 3\n" +
                    "max_variable_mods_in_peptide = 5\n" +
                    "\n" +
                    "#\n" +
                    "# fragment ions\n" +
                    "#\n" +
                    "# ion trap ms/ms:  0.36 tolerance, 0.11 offset (mono masses)\n" +
                    "# high res ms/ms:  0.01 tolerance, 0.00 offset (mono masses)\n" +
                    "#\n" +
                    "fragment_bin_tol = 0. 03               # binning to use on fragment ions\n" +
                    "fragment_bin_offset = 0.00             # offset position to start the binning\n" +
                    "theoretical_fragment_ions = 0          # 0=default peak shape, 1=M peak only\n" +
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
                    "output_sqtstream = 0                   # 0=no, 1=yes  write sqt to standard output\n" +
                    "output_sqtfile = 0                     # 0=no, 1=yes  write sqt file\n" +
                    "output_pepxmlfile = 1                  # 0=no, 1=yes  write pep.xml file\n" +
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
                    "precursor_charge = 0 0                 # precursor charge range to analyze; does not override mzXML charge; 0 as 1st entry ignores parameter\n" +
                    "ms_level = 2                           # MS level to analyze, valid are levels 2 (default) or 3\n" +
                    "activation_method = ALL                # activation method; used if activation method set; allowed ALL, CID, ECD, ETD, PQD, HCD, IRMPD\n" +
                    "\n" +
                    "#\n" +
                    "# misc parameters\n" +
                    "#\n" +
                    "digest_mass_range = 600.0 5000.0       # MH+ peptide mass range to analyze\n" +
                    "num_results = 50                       # number of search hits to store internally\n" +
                    "skip_researching = 1                   # for '.out' file output only, 0=search everything again (default), 1=don't search if .out exists\n" +
                    "max_fragment_charge = 3                # set maximum fragment charge state to analyze (allowed max 5)\n" +
                    "max_precursor_charge = 6               # set maximum precursor charge state to analyze (allowed max 9)\n" +
                    "nucleotide_reading_frame = 0           # 0=proteinDB, 1-6, 7=forward three, 8=reverse three, 9=all six\n" +
                    "clip_nterm_methionine = 0              # 0=leave sequences as-is; 1=also consider sequence w/o N-term methionine\n" +
                    "\n" +
                    "#\n" +
                    "# spectral processing\n" +
                    "#\n" +
                    "minimum_peaks = 5                      # minimum num. of peaks in spectrum to search (default 5)\n" +
                    "minimum_intensity = 0                  # minimum intensity value to read in\n" +
                    "remove_precursor_peak = 0              # 0=no, 1=yes, 2=all charge reduced precursor peaks (for ETD)\n" +
                    "remove_precursor_tolerance = 1.5       # +- Da tolerance for precursor removal\n" +
                    "\n" +
                    "#\n" +
                    "# additional modifications\n" +
                    "#\n" +
                    "\n" +
                    "variable_C_terminus = 0.0\n" +
                    "variable_N_terminus = 0.0\n" +
                    "variable_C_terminus_distance = -1      # -1=all peptides, 0=protein terminus, 1-N = maximum offset from C-terminus\n" +
                    "variable_N_terminus_distance = -1      # -1=all peptides, 0=protein terminus, 1-N = maximum offset from N-terminus\n" +
                    "\n" +
                    "add_Cterm_peptide = 0.0\n" +
                    "add_Nterm_peptide = 229.162932\n" +
                    "add_Cterm_protein = 0.0\n" +
                    "add_Nterm_protein = 0.0\n" +
                    "\n" +
                    "add_G_glycine = 0.0000                 # added to G - avg.  57.0513, mono.  57.02146\n" +
                    "add_A_alanine = 0.0000                 # added to A - avg.  71.0779, mono.  71.03711\n" +
                    "add_S_serine = 0.0000                  # added to S - avg.  87.0773, mono.  87.02303\n" +
                    "add_P_proline = 0.0000                 # added to P - avg.  97.1152, mono.  97.05276\n" +
                    "add_V_valine = 0.0000                  # added to V - avg.  99.1311, mono.  99.06841\n" +
                    "add_T_threonine = 0.0000               # added to T - avg. 101.1038, mono. 101.04768\n" +
                    "add_C_cysteine = 57.021464            # added to C - avg. 103.1429, mono. 103.00918\n" +
                    "add_L_leucine = 0.0000                 # added to L - avg. 113.1576, mono. 113.08406\n" +
                    "add_I_isoleucine = 0.0000              # added to I - avg. 113.1576, mono. 113.08406\n" +
                    "add_N_asparagine = 0.0000              # added to N - avg. 114.1026, mono. 114.04293\n" +
                    "add_D_aspartic_acid = 0.0000           # added to D - avg. 115.0874, mono. 115.02694\n" +
                    "add_Q_glutamine = 0.0000               # added to Q - avg. 128.1292, mono. 128.05858\n" +
                    "add_K_lysine = 229.162932             # added to K - avg. 128.1723, mono. 128.09496\n" +
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

    public static final String[] TEST_PARAM_NAMES = {
            "database_name", //  Homo_sapiens_non-redundant.GRCh37.68.pep.all_FPKM_SNV-cRAP_targetdecoy.fasta\n" +
            "decoy_search", // 0                       # 0=no (default), 1=concatenated search, 2=separate search\n" +
            "num_threads", // 8                        # 0=poll CPU to set num threads; else specify num threads directly (max 32)\n" +
            "peptide_mass_tolerance", // 20\n" +
            "peptide_mass_units", // 2                 # 0=amu, 1=mmu, 2=ppm\n" +
            "mass_type_parent", // 1                   # 0=average masses, 1=monoisotopic masses\n" +
            "mass_type_fragment", // 1                 # 0=average masses, 1=monoisotopic masses\n" +
            "precursor_tolerance_type", // 0           # 0=MH+ (default), 1=precursor m/z\n" +
            "isotope_error", // 0                      # 0=off, 1=on -1/0/1/2/3 (standard C13 error), 2= -8/-4/0/4/8 (for +4/+8 labeling)\n" +
            "search_enzyme_number", // 1               # choose from list at end of this params file\n" +
            "num_enzyme_termini", // 1                 # valid values are 1 (semi-digested), 2 (fully digested, default), 8 N-term, 9 C-term\n" +
            "allowed_missed_cleavage", // 2            # maximum value is 5; for enzyme search\n" +
            "variable_mod1", // 15.994915 M 0 3\n" +
            "variable_mod2", // 0.984016 N 0 3\n" +
            "variable_mod3", // 0.0 X 0 3\n" +
            "variable_mod4", // 0.0 X 0 3\n" +
            "variable_mod5", // 0.0 X 0 3\n" +
            "variable_mod6", // 0.0 X 0 3\n" +
            "max_variable_mods_in_peptide", // 5\n" +
            "fragment_bin_tol", // 0. 03               # binning to use on fragment ions\n" +
            "fragment_bin_offset", // 0.00             # offset position to start the binning\n" +
            "theoretical_fragment_ions", // 0          # 0=default peak shape, 1=M peak only\n" +
            "use_A_ions", // 0\n" +
            "use_B_ions", // 1\n" +
            "use_C_ions", // 0\n" +
            "use_X_ions", // 0\n" +
            "use_Y_ions", // 1\n" +
            "use_Z_ions", // 0\n" +
            "use_NL_ions", // 1                        # 0=no, 1=yes to consider NH3/H2O neutral loss peaks\n" +
            "output_sqtstream", // 0                   # 0=no, 1=yes  write sqt to standard output\n" +
            "output_sqtfile", // 0                     # 0=no, 1=yes  write sqt file\n" +
            "output_pepxmlfile", // 1                  # 0=no, 1=yes  write pep.xml file\n" +
            "output_outfiles", // 0                    # 0=no, 1=yes  write .out files\n" +
            "print_expect_score", // 1                 # 0=no, 1=yes to replace Sp with expect in out & sqt\n" +
            "num_output_lines", // 5                   # num peptide results to show\n" +
            "show_fragment_ions", // 0                 # 0=no, 1=yes for out files only\n" +
            "sample_enzyme_number", // 1               # Sample enzyme which is possibly different than the one applied to the search.\n" +
            "scan_range", // 0 0                       # start and scan scan range to search; 0 as 1st entry ignores parameter\n" +
            "precursor_charge", // 0 0                 # precursor charge range to analyze; does not override mzXML charge; 0 as 1st entry ignores parameter\n" +
            "ms_level", // 2                           # MS level to analyze, valid are levels 2 (default) or 3\n" +
            "activation_method", // ALL                # activation method; used if activation method set; allowed ALL, CID, ECD, ETD, PQD, HCD, IRMPD\n" +
            "digest_mass_range", // 600.0 5000.0       # MH+ peptide mass range to analyze\n" +
            "num_results", // 50                       # number of search hits to store internally\n" +
            "skip_researching", // 1                   # for '.out' file output only, 0=search everything again (default), 1=don't search if .out exists\n" +
            "max_fragment_charge", // 3                # set maximum fragment charge state to analyze (allowed max 5)\n" +
            "max_precursor_charge", // 6               # set maximum precursor charge state to analyze (allowed max 9)\n" +
            "nucleotide_reading_frame", // 0           # 0=proteinDB, 1-6, 7=forward three, 8=reverse three, 9=all six\n" +
            "clip_nterm_methionine", // 0              # 0=leave sequences as-is; 1=also consider sequence w/o N-term methionine\n" +
            "minimum_peaks", // 5                      # minimum num. of peaks in spectrum to search (default 5)\n" +
            "minimum_intensity", // 0                  # minimum intensity value to read in\n" +
            "remove_precursor_peak", // 0              # 0=no, 1=yes, 2=all charge reduced precursor peaks (for ETD)\n" +
            "remove_precursor_tolerance", // 1.5       # +- Da tolerance for precursor removal\n" +
            "variable_C_terminus", // 0.0\n" +
            "variable_N_terminus", // 0.0\n" +
            "variable_C_terminus_distance", // -1      # -1=all peptides, 0=protein terminus, 1-N", // maximum offset from C-terminus\n" +
            "variable_N_terminus_distance", // -1      # -1=all peptides, 0=protein terminus, 1-N", // maximum offset from N-terminus\n" +
            "add_Cterm_peptide", // 0.0\n" +
            "add_Nterm_peptide", // 229.162932\n" +
            "add_Cterm_protein", // 0.0\n" +
            "add_Nterm_protein", // 0.0\n" +
            "add_G_glycine", // 0.0000                 # added to G - avg.  57.0513, mono.  57.02146\n" +
            "add_A_alanine", // 0.0000                 # added to A - avg.  71.0779, mono.  71.03711\n" +
            "add_S_serine", // 0.0000                  # added to S - avg.  87.0773, mono.  87.02303\n" +
            "add_P_proline", // 0.0000                 # added to P - avg.  97.1152, mono.  97.05276\n" +
            "add_V_valine", // 0.0000                  # added to V - avg.  99.1311, mono.  99.06841\n" +
            "add_T_threonine", // 0.0000               # added to T - avg. 101.1038, mono. 101.04768\n" +
            "add_C_cysteine", // 57.021464            # added to C - avg. 103.1429, mono. 103.00918\n" +
            "add_L_leucine", // 0.0000                 # added to L - avg. 113.1576, mono. 113.08406\n" +
            "add_I_isoleucine", // 0.0000              # added to I - avg. 113.1576, mono. 113.08406\n" +
            "add_N_asparagine", // 0.0000              # added to N - avg. 114.1026, mono. 114.04293\n" +
            "add_D_aspartic_acid", // 0.0000           # added to D - avg. 115.0874, mono. 115.02694\n" +
            "add_Q_glutamine", // 0.0000               # added to Q - avg. 128.1292, mono. 128.05858\n" +
            "add_K_lysine", // 229.162932             # added to K - avg. 128.1723, mono. 128.09496\n" +
            "add_E_glutamic_acid", // 0.0000           # added to E - avg. 129.1140, mono. 129.04259\n" +
            "add_M_methionine", // 0.0000              # added to M - avg. 131.1961, mono. 131.04048\n" +
            "add_O_ornithine", // 0.0000               # added to O - avg. 132.1610, mono  132.08988\n" +
            "add_H_histidine", // 0.0000               # added to H - avg. 137.1393, mono. 137.05891\n" +
            "add_F_phenylalanine", // 0.0000           # added to F - avg. 147.1739, mono. 147.06841\n" +
            "add_R_arginine", // 0.0000                # added to R - avg. 156.1857, mono. 156.10111\n" +
            "add_Y_tyrosine", // 0.0000                # added to Y - avg. 163.0633, mono. 163.06333\n" +
            "add_W_tryptophan", //", // 0.0000              # added to W - avg. 186.0793, mono. 186.07931\n" +
            "add_B_user_amino_acid", //", // 0.0000         # added to B - avg.   0.0000, mono.   0.00000\n" +
            "add_J_user_amino_acid", //", // 0.0000         # added to J - avg.   0.0000, mono.   0.00000\n" +
            "add_U_user_amino_acid", //", // 0.0000         # added to U - avg.   0.0000, mono.   0.00000\n" +
            "add_X_user_amino_acid", // 0.0000         # added to X - avg.   0.0000, mono.   0.00000\n" +
            "add_Z_user_amino_acid", //

    };

    public static final Set<String> PARAM_KEY_SET = new HashSet<String>(Arrays.asList(TEST_PARAM_NAMES));

    // These parameters have a value of 0
    public static final String[] ZERO_TEST_PARAM_NAMES = {
            "decoy_search", // 0                       # 0=no (default), 1=concatenated search, 2=separate search\n" +
            "precursor_tolerance_type", // 0           # 0=MH+ (default), 1=precursor m/z\n" +
            "isotope_error", // 0                      # 0=off, 1=on -1/0/1/2/3 (standard C13 error), 2= -8/-4/0/4/8 (for +4/+8 labeling)\n" +
            "fragment_bin_offset", // 0.00             # offset position to start the binning\n" +
            "theoretical_fragment_ions", // 0          # 0=default peak shape, 1=M peak only\n" +
            "use_A_ions", // 0\n" +
            "use_C_ions", // 0\n" +
            "use_X_ions", // 0\n" +
            "use_Z_ions", // 0\n" +
            "output_sqtstream", // 0                   # 0=no, 1=yes  write sqt to standard output\n" +
            "output_sqtfile", // 0                     # 0=no, 1=yes  write sqt file\n" +
            "output_outfiles", // 0                    # 0=no, 1=yes  write .out files\n" +
            "show_fragment_ions", // 0                 # 0=no, 1=yes for out files only\n" +
            "nucleotide_reading_frame", // 0           # 0=proteinDB, 1-6, 7=forward three, 8=reverse three, 9=all six\n" +
            "clip_nterm_methionine", // 0              # 0=leave sequences as-is; 1=also consider sequence w/o N-term methionine\n" +
            "minimum_intensity", // 0                  # minimum intensity value to read in\n" +
            "remove_precursor_peak", // 0              # 0=no, 1=yes, 2=all charge reduced precursor peaks (for ETD)\n" +
            "variable_C_terminus", // 0.0\n" +
            "variable_N_terminus", // 0.0\n" +
            "add_Cterm_peptide", // 0.0\n" +
            "add_Cterm_protein", // 0.0\n" +
            "add_Nterm_protein", // 0.0\n" +
            "add_G_glycine", // 0.0000                 # added to G - avg.  57.0513, mono.  57.02146\n" +
            "add_A_alanine", // 0.0000                 # added to A - avg.  71.0779, mono.  71.03711\n" +
            "add_S_serine", // 0.0000                  # added to S - avg.  87.0773, mono.  87.02303\n" +
            "add_P_proline", // 0.0000                 # added to P - avg.  97.1152, mono.  97.05276\n" +
            "add_V_valine", // 0.0000                  # added to V - avg.  99.1311, mono.  99.06841\n" +
            "add_T_threonine", // 0.0000               # added to T - avg. 101.1038, mono. 101.04768\n" +
            "add_L_leucine", // 0.0000                 # added to L - avg. 113.1576, mono. 113.08406\n" +
            "add_I_isoleucine", // 0.0000              # added to I - avg. 113.1576, mono. 113.08406\n" +
            "add_N_asparagine", // 0.0000              # added to N - avg. 114.1026, mono. 114.04293\n" +
            "add_D_aspartic_acid", // 0.0000           # added to D - avg. 115.0874, mono. 115.02694\n" +
            "add_Q_glutamine", // 0.0000               # added to Q - avg. 128.1292, mono. 128.05858\n" +
            "add_E_glutamic_acid", // 0.0000           # added to E - avg. 129.1140, mono. 129.04259\n" +
            "add_M_methionine", // 0.0000              # added to M - avg. 131.1961, mono. 131.04048\n" +
            "add_O_ornithine", // 0.0000               # added to O - avg. 132.1610, mono  132.08988\n" +
            "add_H_histidine", // 0.0000               # added to H - avg. 137.1393, mono. 137.05891\n" +
            "add_F_phenylalanine", // 0.0000           # added to F - avg. 147.1739, mono. 147.06841\n" +
            "add_R_arginine", // 0.0000                # added to R - avg. 156.1857, mono. 156.10111\n" +
            "add_Y_tyrosine", // 0.0000                # added to Y - avg. 163.0633, mono. 163.06333\n" +
            "add_W_tryptophan", //", // 0.0000              # added to W - avg. 186.0793, mono. 186.07931\n" +
            "add_B_user_amino_acid", //", // 0.0000         # added to B - avg.   0.0000, mono.   0.00000\n" +
            "add_J_user_amino_acid", //", // 0.0000         # added to J - avg.   0.0000, mono.   0.00000\n" +
            "add_U_user_amino_acid", //", // 0.0000         # added to U - avg.   0.0000, mono.   0.00000\n" +
            "add_X_user_amino_acid", // 0.0000         # added to X - avg.   0.0000, mono.   0.00000\n" +
            "add_Z_user_amino_acid", //

    };


    public static String[] getTestParameters() {
        return TEST_PARAMS_FILE.split("\n");
    }

    /**
     *
     */
    @Test
    public void testCometParamsReport() {
            throw new UnsupportedOperationException("Fix This"); // ToDo
//        String[] lines = getTestParameters();
//        ISetableParameterHolder hdr = new TestMain();
//        CometParameterUtilities.parseParameters(hdr, lines);
//        String[] parameterKeys = hdr.getParameterKeys();
//        Assert.assertEquals(PARAM_KEY_SET.size(), parameterKeys.length);
//        for (int i = 0; i < parameterKeys.length; i++) {
//            String parameterKey = parameterKeys[i];
//            if (!PARAM_KEY_SET.contains(parameterKey))
//                Assert.fail();
//
//        }
//        // these should have value 0
//        for (int i = 0; i < ZERO_TEST_PARAM_NAMES.length; i++) {
//            String parameterKey = ZERO_TEST_PARAM_NAMES[i];
//            String value = hdr.getParameter(parameterKey);
//            Double doubleParameter = null;
//            try {
//                doubleParameter = hdr.getDoubleParameter(parameterKey);
//            }
//            catch (Exception e) {
//                throw new RuntimeException(e);
//
//            }
//            Assert.assertEquals(parameterKey, 0.0, doubleParameter, 0.000001);
//
//        }
//        // now test specific parameters
//        Assert.assertEquals("database_name","Homo_sapiens_non-redundant.GRCh37.68.pep.all_FPKM_SNV-cRAP_targetdecoy.fasta",hdr.getParameter("database_name"));
//        Assert.assertEquals("activation_method","ALL",hdr.getParameter( "activation_method"));
//        Assert.assertEquals("variable_mod1","15.994915 M 0 3",hdr.getParameter("variable_mod1"));
//
//        Assert.assertEquals("peptide_mass_tolerance",20,(int)hdr.getIntParameter("peptide_mass_tolerance") );
//        Assert.assertEquals("num_threads",8,(int)hdr.getIntParameter("num_threads") );
//        Assert.assertEquals("peptide_mass_units",2,(int)hdr.getIntParameter("peptide_mass_units") );

    }

}
