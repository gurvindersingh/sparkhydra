package org.systemsbiology.xtandem.comet;

import java.util.*;

/**
 * org.systemsbiology.xtandem.comet.CometTestData
 * User: Steve
 * Date: 3/31/2015
 */
public class CometTestData {

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




    public static final String TEST_PROTEIN = "NECFLSHKDDSPDLPK";


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
          "<note>list path parameters</note>\n" +
                   "\t<note type=\"input\" label=\"list path, default parameters\">default_input.xml</note>\n" +
                   "\t\t<note>This value is ignored when it is present in the default parameter\n" +
                   "\t\tlist path.</note>\n" +
                   "\t<note type=\"input\" label=\"list path, taxonomy information\">taxonomy_searchGUI.xml</note>\n" +
                   "\n" +
                   "<note>spectrum parameters</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, fragment monoisotopic mass error\">0.5</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, parent monoisotopic mass error plus\">20.0</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, parent monoisotopic mass error minus\">20.0</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, parent monoisotopic mass isotope error\">yes</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, fragment monoisotopic mass error units\">Daltons</note>\n" +
                   "\t<note>The value for this parameter may be 'Daltons' or 'ppm': all other values are ignored</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, parent monoisotopic mass error units\">ppm</note>\n" +
                   "\t\t<note>The value for this parameter may be 'Daltons' or 'ppm': all other values are ignored</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, fragment mass type\">monoisotopic</note>\n" +
                   "\t\t<note>values are monoisotopic|average </note>\n" +
                   "\n" +
                   "<note>comet parameters</note>\n" +
                   "\t<note type=\"input\" label=\"comet.fragment_bin_tol\">0.02</note> <!-- value for High resolution -->\n" +
                   "\t<note type=\"input\" label=\"comet.fragment_bin_offset\">0.4</note> <!-- value for High resolution -->\n" +
                   "\t<note type=\"input\" label=\"comet.mass_tolerance\">0.5</note> <!-- value for High resolution -->\n" +
                   "\n" +
                   "\n" +
                   "<note>spectrum conditioning parameters</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, dynamic range\">100.0</note>\n" +
                   "\t\t<note>The peaks read in are normalized so that the most intense peak\n" +
                   "\t\tis set to the dynamic range value. All peaks with values of less that\n" +
                   "\t\t1, using this normalization, are not used. This normalization has the\n" +
                   "\t\toverall effect of setting a threshold value for peak intensities.</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, total peaks\">50</note> \n" +
                   "\t\t<note>If this value is 0, it is ignored. If it is greater than zero (lets say 50),\n" +
                   "\t\tthen the number of peaks in the spectrum with be limited to the 50 most intense\n" +
                   "\t\tpeaks in the spectrum. X! tandem does not do any peak finding: it only\n" +
                   "\t\tlimits the peaks used by this parameter, and the dynamic range parameter.</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, maximum parent charge\">4</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, use noise suppression\">no</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, minimum parent m+h\">500.0</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, minimum fragment mz\">200.0</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, minimum peaks\">5</note> \n" +
                   "\t<note type=\"input\" label=\"spectrum, threads\">4</note>\n" +
                   "\t<note type=\"input\" label=\"spectrum, sequence batch size\">1000</note>\n" +
                   "\t\n" +
                   "<note>residue modification parameters</note>\n" +
                   "\t<note type=\"input\" label=\"residue, modification mass\"></note>\n" +
                   "\t\t<note></note>\n" +
                   " \t <note type=\"input\" label=\"residue, potential modification mass\">79.966331@T,79.966331@S,57.021464@C,15.994915@M,79.966331@Y</note>\n" +
                   "\t\t<note>phosphorylation of t,phosphorylation of s,carbamidomethyl c,oxidation of m,phosphorylation of y</note>\n" +
                   "\t<note type=\"input\" label=\"residue, potential modification motif\"></note>\n" +
                   "\t\t<note>The format of this parameter is similar to residue, modification mass,\n" +
                   "\t\twith the addition of a modified PROSITE notation sequence motif specification.\n" +
                   "\t\tFor example, a value of 80@[ST!]PX[KR] indicates a modification\n" +
                   "\t\tof either S or T when followed by P, and residue and the a K or an R.\n" +
                   "\t\tA value of 204@N!{P}[ST]{P} indicates a modification of N by 204, if it\n" +
                   "\t\tis NOT followed by a P, then either an S or a T, NOT followed by a P.\n" +
                   "\t\tPositive and negative values are allowed.\n" +
                   "\t\t</note>\n" +
                   "\n" +
                   "<note>protein parameters</note>\n" +
                   "\t<note type=\"input\" label=\"protein, taxon\">all</note>\n" +
                   "\t\t<note>This value is interpreted using the information in taxonomy.xml.</note>\n" +
                   "\t<note type=\"input\" label=\"protein, cleavage site\">[RK]|{P}</note>\n" +
                   "\t\t<note>this setting corresponds to the enzyme trypsin. The first characters\n" +
                   "\t\tin brackets represent residues N-terminal to the bond - the '|' pipe -\n" +
                   "\t\tand the second set of characters represent residues C-terminal to the\n" +
                   "\t\tbond. The characters must be in square brackets (denoting that only\n" +
                   "\t\tthese residues are allowed for a cleavage) or french brackets (denoting\n" +
                   "\t\tthat these residues cannot be in that position). Use UPPERCASE characters.\n" +
                   "\t\tTo denote cleavage at any residue, use [X]|[X] and reset the \n" +
                   "\t\tscoring, maximum missed cleavage site parameter (see below) to something like 50.\n" +
                   "\t\t</note>\n" +
                   "\t<note type=\"input\" label=\"protein, cleavage semi\">no</note>\n" +
                   "\t<note type=\"input\" label=\"protein, modified residue mass file\"></note>\n" +
                   "\t<note type=\"input\" label=\"protein, cleavage C-terminal mass change\">17.002735</note>\n" +
                   "\t<note type=\"input\" label=\"protein, cleavage N-terminal mass change\">+1.007825</note>\n" +
                   "\t<note type=\"input\" label=\"protein, quick acetyl\">yes</note>\n" +
                   "\t<note type=\"input\" label=\"protein, quick pyrolidone\">yes</note>\n" +
                   "\t<note type=\"input\" label=\"protein, stP bias\">no</note>\n" +
                   "\t<note type=\"input\" label=\"protein, N-terminal residue modification mass\">0.0</note>\n" +
                   "\t<note type=\"input\" label=\"protein, C-terminal residue modification mass\">0.0</note>\n" +
                   "\t<note type=\"input\" label=\"protein, homolog management\">no</note>\n" +
                   "\t\t<note>if yes, an upper limit is set on the number of homologues kept for a particular spectrum</note>\n" +
                   "\n" +
                   "<note>model refinement parameters</note>\n" +
                   "\t<note type=\"input\" label=\"refine\">yes</note>\n" +
                   "\t<note type=\"input\" label=\"refine, modification mass\"></note>\n" +
                   "\t<note type=\"input\" label=\"refine, sequence path\"></note>\n" +
                   "\t<note type=\"input\" label=\"refine, tic percent\">20</note>\n" +
                   "\t<note type=\"input\" label=\"refine, spectrum synthesis\">yes</note>\n" +
                   "\t<note type=\"input\" label=\"refine, maximum valid expectation value\">0.01</note>\n" +
                   "\t<note type=\"input\" label=\"refine, unanticipated cleavage\">yes</note>\n" +
                   "\t<note type=\"input\" label=\"refine, cleavage semi\">no</note>\n" +
                   "\t<note type=\"input\" label=\"refine, point mutations\">no</note>\n" +
                   "\t<note type=\"input\" label=\"refine, saps\">no</note>\n" +
                   "\t<note type=\"input\" label=\"refine, use potential modifications for full refinement\">no</note>\n" +
                   "\t<note type=\"input\" label=\"refine, potential N-terminus modifications\"></note>\n" +
                   "\t<note type=\"input\" label=\"refine, potential C-terminus modifications\"></note>\n" +
                   "\t<note type=\"input\" label=\"refine, potential modification mass\"></note>\n" +
                   "\t<note type=\"input\" label=\"refine, potential modification motif\"></note>\n" +
                   "\t<note>The format of this parameter is similar to residue, modification mass,\n" +
                   "\t\twith the addition of a modified PROSITE notation sequence motif specification.\n" +
                   "\t\tFor example, a value of 80@[ST!]PX[KR] indicates a modification\n" +
                   "\t\tof either S or T when followed by P, and residue and the a K or an R.\n" +
                   "\t\tA value of 204@N!{P}[ST]{P} indicates a modification of N by 204, if it\n" +
                   "\t\tis NOT followed by a P, then either an S or a T, NOT followed by a P.\n" +
                   "\t\tPositive and negative values are allowed.\n" +
                   "\t\t</note>\n" +
                   "\n" +
                   "<note>scoring parameters</note>\n" +
                   "\t<note type=\"input\" label=\"scoring, minimum ion count\">4</note>\n" +
                   "\t<note type=\"input\" label=\"scoring, maximum missed cleavage sites\">2</note>\n" +
                   "\t<note type=\"input\" label=\"scoring, x ions\">no</note>\n" +
                   "\t<note type=\"input\" label=\"scoring, y ions\">yes</note>\n" +
                   "\t<note type=\"input\" label=\"scoring, z ions\">no</note>\n" +
                   "\t<note type=\"input\" label=\"scoring, a ions\">no</note>\n" +
                   "\t<note type=\"input\" label=\"scoring, b ions\">yes</note>\n" +
                   "\t<note type=\"input\" label=\"scoring, c ions\">no</note>\n" +
                   "\t<note type=\"input\" label=\"scoring, cyclic permutation\">no</note>\n" +
                   "\t\t<note>if yes, cyclic peptide sequence permutation is used to pad the scoring histograms</note>\n" +
                   "\t<note type=\"input\" label=\"scoring, include reverse\">no</note>\n" +
                   "\t\t<note>if yes, then reversed sequences are searched at the same time as forward sequences</note>\n" +
                   "\n" +
                   "<note>output parameters</note>\n" +
                   "\t<note type=\"input\" label=\"output, log path\"></note>\n" +
                   "\t<note type=\"input\" label=\"output, message\"></note>\n" +
                   "\t<note type=\"input\" label=\"output, one sequence copy\">no</note>\n" +
                   "\t<note type=\"input\" label=\"output, sequence path\"></note>\n" +
                   "\t<note type=\"input\" label=\"output, path\">output.xml</note>\n" +
                   "\t<note type=\"input\" label=\"output, sort results by\">spectrum</note>\n" +
                   "\t\t<note>values = protein|spectrum (spectrum is the default)</note>\n" +
                   "\t<note type=\"input\" label=\"output, path hashing\">yes</note>\n" +
                   "\t\t<note>values = yes|no</note>\n" +
                   "\t<note type=\"input\" label=\"output, xsl path\">tandem-style.xsl</note>\n" +
                   "\t<note type=\"input\" label=\"output, parameters\">yes</note>\n" +
                   "\t\t<note>values = yes|no</note>\n" +
                   "\t<note type=\"input\" label=\"output, performance\">yes</note>\n" +
                   "\t\t<note>values = yes|no</note>\n" +
                   "\t<note type=\"input\" label=\"output, spectra\">yes</note>\n" +
                   "\t\t<note>values = yes|no</note>\n" +
                   "\t<note type=\"input\" label=\"output, histograms\">no</note>\n" +
                   "\t\t<note>values = yes|no</note>\n" +
                   "\t<note type=\"input\" label=\"output, proteins\">yes</note>\n" +
                   "\t\t<note>values = yes|no</note>\n" +
                   "\t<note type=\"input\" label=\"output, sequences\">no</note>\n" +
                   "\t\t<note>values = yes|no</note>\n" +
                   "\t<note type=\"input\" label=\"output, one sequence copy\">no</note>\n" +
                   "\t\t<note>values = yes|no, set to yes to produce only one copy of each protein sequence in the output xml</note>\n" +
                   "\t<note type=\"input\" label=\"output, results\">all</note>\n" +
                   "\t\t<note>values = all|valid|stochastic</note>\n" +
                   "\t<note type=\"input\" label=\"output, maximum valid expectation value\">100.0</note>\n" +
                   "\t\t<note>value is used in the valid|stochastic setting of output, results</note>\n" +
                   "\t<note type=\"input\" label=\"output, histogram column width\">50</note>\n" +
                   "\t\t<note>values any integer greater than 0. Setting this to '1' makes cutting and pasting histograms\n" +
                   "\t\tinto spread sheet programs easier.</note>\n" +
                   "<note type=\"description\">ADDITIONAL EXPLANATIONS</note>\n" +
                   "\t<note type=\"description\">Each one of the parameters for X! tandem is entered as a labeled note\n" +
                   "\t\t\tnode. In the current version of X!, keep those note nodes\n" +
                   "\t\t\ton a single line.\n" +
                   "\t</note>\n" +
                   "\t<note type=\"description\">The presence of the type 'input' is necessary if a note is to be considered\n" +
                   "\t\t\tan input parameter.\n" +
                   "\t</note>\n" +
                   "\t<note type=\"description\">Any of the parameters that are paths to files may require alteration for a \n" +
                   "\t\t\tparticular installation. Full path names usually cause the least trouble,\n" +
                   "\t\t\tbut there is no reason not to use relative path names, if that is the\n" +
                   "\t\t\tmost convenient.\n" +
                   "\t</note>\n" +
                   "\t<note type=\"description\">Any parameter values set in the 'list path, default parameters' file are\n" +
                   "\t\t\treset by entries in the normal input file, if they are present. Otherwise,\n" +
                   "\t\t\tthe default set is used.\n" +
                   "\t</note>\n" +
                   "\t<note type=\"description\">The 'list path, taxonomy information' file must exist.\n" +
                   "\t\t</note>\n" +
                   "\t<note type=\"description\">The directory containing the 'output, path' file must exist: it will not be created.\n" +
                   "\t\t</note>\n" +
                   "\t<note type=\"description\">The 'output, xsl path' is optional: it is only of use if a good XSLT style sheet exists.\n" +
                   "\t\t</note>\n" +
                     "</bioml>";
}
