package com.lordjoe.distributed.hydra.comet;

import org.junit.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometTestData
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
                    "\t<note type=\"input\" label=\"comet.mass_tolerance\">30</note> <!-- value for High resolution -->\n" +
                    "\t<note type=\"input\" label=\"comet.max_fragment_charge\">3</note>\n" +
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
                    "\t<note type=\"input\" label=\"protein, cleavage C-terminal mass change\">19.017841166880000</note>\n" +
                    "\t<note type=\"input\" label=\"protein, cleavage N-terminal mass change\">1.007276466</note>\n" +
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

    public static final String PARAMS_FROM_COMET =
            "5753\t1\tB\t0\n" +
                    "12205\t1\tB\t1\n" +
                    "20206\t1\tB\t2\n" +
                    "27560\t1\tB\t3\n" +
                    "33214\t1\tB\t4\n" +
                    "37565\t1\tB\t5\n" +
                    "44418\t1\tB\t6\n" +
                    "50823\t1\tB\t7\n" +
                    "56575\t1\tB\t8\n" +
                    "62326\t1\tB\t9\n" +
                    "66677\t1\tB\t10\n" +
                    "71530\t1\tB\t11\n" +
                    "77281\t1\tB\t12\n" +
                    "82936\t1\tB\t13\n" +
                    "87788\t1\tB\t14\n" +
                    "7356\t1\tY\t0\n" +
                    "12208\t1\tY\t1\n" +
                    "17863\t1\tY\t2\n" +
                    "23614\t1\tY\t3\n" +
                    "28467\t1\tY\t4\n" +
                    "32818\t1\tY\t5\n" +
                    "38570\t1\tY\t6\n" +
                    "44321\t1\tY\t7\n" +
                    "50726\t1\tY\t8\n" +
                    "57579\t1\tY\t9\n" +
                    "61930\t1\tY\t10\n" +
                    "67584\t1\tY\t11\n" +
                    "74938\t1\tY\t12\n" +
                    "82939\t1\tY\t13\n" +
                    "89391\t1\tY\t14\n" +
                    "2902\t2\tB\t0\n" +
                    "6128\t2\tB\t1\n" +
                    "10128\t2\tB\t2\n" +
                    "13805\t2\tB\t3\n" +
                    "16632\t2\tB\t4\n" +
                    "18808\t2\tB\t5\n" +
                    "22234\t2\tB\t6\n" +
                    "25437\t2\tB\t7\n" +
                    "28313\t2\tB\t8\n" +
                    "31188\t2\tB\t9\n" +
                    "33364\t2\tB\t10\n" +
                    "35790\t2\tB\t11\n" +
                    "38666\t2\tB\t12\n" +
                    "41493\t2\tB\t13\n" +
                    "43919\t2\tB\t14\n" +
                    "3703\t2\tY\t0\n" +
                    "6129\t2\tY\t1\n" +
                    "8957\t2\tY\t2\n" +
                    "11832\t2\tY\t3\n" +
                    "14259\t2\tY\t4\n" +
                    "16434\t2\tY\t5\n" +
                    "19310\t2\tY\t6\n" +
                    "22186\t2\tY\t7\n" +
                    "25388\t2\tY\t8\n" +
                    "28815\t2\tY\t9\n" +
                    "30990\t2\tY\t10\n" +
                    "33817\t2\tY\t11\n" +
                    "37494\t2\tY\t12\n" +
                    "41495\t2\tY\t13\n" +
                    "44721\t2\tY\t14";

    public static final String CORRELATION_DATA_FROM_COMET_EG0 =
            "7653\t49.3343\n" +
                    "7654\t32.2833\n" +
                    "7853\t10.1614\n" +
                    "7854\t2.5678\n" +
                    "8354\t6.75998\n" +
                    "8554\t10.8881\n" +
                    "8754\t3.53096\n" +
                    "8756\t5.29041\n" +
                    "8954\t3.4007\n" +
                    "8955\t5.35757\n" +
                    "9052\t3.05898\n" +
                    "9053\t12.61\n" +
                    "9157\t3.90263\n" +
                    "9158\t4.46378\n" +
                    "9252\t2.83906\n" +
                    "9253\t10.6991\n" +
                    "9254\t3.43916\n" +
                    "9255\t4.66902\n" +
                    "9455\t5.6085\n" +
                    "9753\t2.40406\n" +
                    "9754\t10.6194\n" +
                    "9855\t5.76275\n" +
                    "9856\t4.20582\n" +
                    "9904\t7.89952\n" +
                    "9905\t8.35497\n" +
                    "9953\t9.35735\n" +
                    "9954\t16.2633\n" +
                    "10003\t2.1451\n" +
                    "10004\t4.51912\n" +
                    "10158\t3.77071\n" +
                    "10159\t2.88381\n" +
                    "10403\t3.6121\n" +
                    "10404\t5.49864\n" +
                    "10453\t4.40342\n" +
                    "10456\t5.75574\n" +
                    "10457\t5.95788\n" +
                    "10557\t4.723\n" +
                    "10558\t3.75636\n" +
                    "10605\t11.7012\n" +
                    "10606\t6.17672\n" +
                    "10654\t16.8101\n" +
                    "10655\t16.1188\n" +
                    "10804\t4.53048\n" +
                    "10805\t25.2006\n" +
                    "10806\t3.61747\n" +
                    "10855\t5.64979\n" +
                    "11254\t3.58952\n" +
                    "11255\t13.9481\n" +
                    "11256\t2.69622\n" +
                    "11304\t16.2472\n" +
                    "11305\t9.21725\n" +
                    "11307\t10.7572\n" +
                    "11308\t20.6071\n" +
                    "11353\t5.07557\n" +
                    "11354\t4.36129\n" +
                    "11355\t3.89627\n" +
                    "11356\t2.73506\n" +
                    "11357\t4.27422\n" +
                    "11358\t4.44495\n" +
                    "11505\t4.55056\n" +
                    "11506\t10.1171\n" +
                    "11658\t5.88933\n" +
                    "11659\t5.04938\n" +
                    "12004\t2.93144\n" +
                    "12005\t10.3157\n" +
                    "12006\t2.28436\n" +
                    "12054\t5.37699\n" +
                    "12055\t3.60845\n" +
                    "12204\t8.0428\n" +
                    "12205\t15.0669\n" +
                    "12207\t4.01328\n" +
                    "12208\t47.4723\n" +
                    "12209\t44.8608\n" +
                    "12210\t1.57758\n" +
                    "12255\t1.32203\n" +
                    "12258\t12.2906\n" +
                    "12259\t12.9381\n" +
                    "12308\t3.87308\n" +
                    "12309\t4.13748\n" +
                    "12407\t2.99668\n" +
                    "12408\t3.81441\n" +
                    "12457\t2.85468\n" +
                    "12458\t10.0368\n" +
                    "12459\t4.39089\n" +
                    "12905\t5.56575\n" +
                    "12906\t5.25948\n" +
                    "13154\t4.67977\n" +
                    "13155\t3.61101\n" +
                    "13307\t2.39561\n" +
                    "13308\t12.1791\n" +
                    "13309\t6.8828\n" +
                    "13603\t8.99531\n" +
                    "13604\t9.8454\n" +
                    "13803\t4.13596\n" +
                    "13804\t3.62396\n" +
                    "13953\t4.3063\n" +
                    "13954\t2.84907\n" +
                    "14005\t15.6771\n" +
                    "14006\t18.2808\n" +
                    "14007\t2.19392\n" +
                    "14055\t2.97154\n" +
                    "14056\t4.19966\n" +
                    "14258\t6.2109\n" +
                    "14259\t5.6679\n" +
                    "14503\t3.27272\n" +
                    "14504\t6.09471\n" +
                    "14505\t3.9757\n" +
                    "15003\t6.36688\n" +
                    "15004\t5.33187\n" +
                    "15005\t3.38473\n" +
                    "15006\t9.598\n" +
                    "15007\t3.62068\n" +
                    "15108\t5.04566\n" +
                    "15405\t13.2433\n" +
                    "15406\t13.5095\n" +
                    "15455\t4.3092\n" +
                    "15456\t4.35887\n" +
                    "15655\t15.4808\n" +
                    "15656\t23.0131\n" +
                    "15657\t8.05834\n" +
                    "15953\t6.59819\n" +
                    "15954\t9.78016\n" +
                    "16203\t9.90329\n" +
                    "16204\t9.38928\n" +
                    "16308\t14.9789\n" +
                    "16309\t20.543\n" +
                    "16406\t11.2129\n" +
                    "16407\t12.3127\n" +
                    "16658\t8.7423\n" +
                    "16659\t10.7474\n" +
                    "16759\t10.4053\n" +
                    "16760\t6.7906\n" +
                    "16908\t8.83941\n" +
                    "16909\t21.6339\n" +
                    "16910\t12.1617\n" +
                    "17103\t11.8177\n" +
                    "17104\t13.6938\n" +
                    "17105\t7.3551\n" +
                    "17254\t12.1329\n" +
                    "17255\t10.4158\n" +
                    "17659\t22.2797\n" +
                    "17660\t24.9958\n" +
                    "17661\t8.51126\n" +
                    "17805\t3.52708\n" +
                    "17806\t10.9621\n" +
                    "17807\t9.87758\n" +
                    "17861\t7.95241\n" +
                    "17862\t44.1107\n" +
                    "17863\t46.8671\n" +
                    "17864\t11.1828\n" +
                    "17912\t16.967\n" +
                    "17913\t18.5861\n" +
                    "17914\t4.36342\n" +
                    "18025\t9.32212\n" +
                    "18026\t8.55597\n" +
                    "18058\t5.87121\n" +
                    "18059\t13.9354\n" +
                    "18060\t12.1715\n" +
                    "18457\t8.29275\n" +
                    "18458\t21.5806\n" +
                    "18459\t17.5351\n" +
                    "18503\t18.4305\n" +
                    "18504\t20.6259\n" +
                    "18505\t5.8067\n" +
                    "19007\t8.31458\n" +
                    "19008\t11.3492\n" +
                    "19009\t6.59104\n" +
                    "19159\t5.95183\n" +
                    "19160\t10.8398\n" +
                    "19161\t8.70971\n" +
                    "19259\t6.70659\n" +
                    "19260\t14.2428\n" +
                    "19261\t13.2252\n" +
                    "19262\t6.97699\n" +
                    "19354\t15.6548\n" +
                    "19355\t20.3418\n" +
                    "19356\t10.1801\n" +
                    "19856\t6.75466\n" +
                    "19857\t10.4866\n" +
                    "19858\t7.74432\n" +
                    "20205\t17.2078\n" +
                    "20206\t30.9501\n" +
                    "20207\t29.1211\n" +
                    "20208\t6.38913\n" +
                    "20256\t10.0658\n" +
                    "20257\t9.55605\n" +
                    "20956\t11.201\n" +
                    "20957\t20.595\n" +
                    "20958\t11.9093\n" +
                    "21058\t7.56045\n" +
                    "21059\t18.7534\n" +
                    "21060\t21.8307\n" +
                    "21061\t5.29876\n" +
                    "22083\t10.4451\n" +
                    "22084\t14.7999\n" +
                    "22085\t13.1054\n" +
                    "22207\t6.39287\n" +
                    "22208\t10.7548\n" +
                    "22209\t10.862\n" +
                    "22356\t13.3769\n" +
                    "22357\t14.673\n" +
                    "22358\t9.51703\n" +
                    "23008\t9.37056\n" +
                    "23009\t13.2936\n" +
                    "23010\t12.1387\n" +
                    "23313\t11.6421\n" +
                    "23314\t14.1037\n" +
                    "23315\t11.2574\n" +
                    "23410\t12.2344\n" +
                    "23411\t17.7203\n" +
                    "23412\t12.5378\n" +
                    "23613\t12.0992\n" +
                    "23614\t15.8114\n" +
                    "23615\t13.2428\n" +
                    "24261\t9.6417\n" +
                    "24262\t18.4256\n" +
                    "24263\t18.7067\n" +
                    "24264\t10.3331\n" +
                    "24809\t10.6655\n" +
                    "24810\t23.6891\n" +
                    "24811\t26.0264\n" +
                    "24812\t21.9961\n" +
                    "24813\t7.05984\n" +
                    "24910\t18.146\n" +
                    "24911\t31.067\n" +
                    "24912\t28.2336\n" +
                    "24913\t20.6346\n" +
                    "24914\t5.63527\n" +
                    "24961\t8.76701\n" +
                    "24962\t11.2067\n" +
                    "24963\t8.71311\n" +
                    "24987\t11.5794\n" +
                    "24988\t13.6296\n" +
                    "24989\t9.38402\n" +
                    "25387\t13.9132\n" +
                    "25388\t18.7246\n" +
                    "25389\t16.3996\n" +
                    "25390\t6.30071\n" +
                    "25412\t11.1455\n" +
                    "25413\t15.0751\n" +
                    "25414\t12.5408\n" +
                    "25757\t8.20362\n" +
                    "25758\t13.864\n" +
                    "25759\t13.4492\n" +
                    "25760\t10.8854\n" +
                    "26411\t14.0687\n" +
                    "26412\t15.2482\n" +
                    "26413\t10.5584\n" +
                    "26859\t6.55664\n" +
                    "26860\t14.5467\n" +
                    "26861\t14.092\n" +
                    "26884\t11.5753\n" +
                    "26885\t13.0175\n" +
                    "26886\t12.0609\n" +
                    "27345\t11.1883\n" +
                    "27346\t15.2298\n" +
                    "27347\t13.3496\n" +
                    "28464\t6.24428\n" +
                    "28465\t28.164\n" +
                    "28466\t38.0785\n" +
                    "28467\t42.7259\n" +
                    "28468\t25.2437\n" +
                    "28469\t9.97065\n" +
                    "28514\t2.75027\n" +
                    "28515\t12.1804\n" +
                    "28516\t25.2655\n" +
                    "28517\t25.104\n" +
                    "28518\t18.4858\n" +
                    "28611\t13.2465\n" +
                    "28612\t15.9314\n" +
                    "28613\t16.0686\n" +
                    "28614\t9.91405\n" +
                    "28812\t4.12041\n" +
                    "28813\t22.2968\n" +
                    "28814\t31.6219\n" +
                    "28815\t37.7037\n" +
                    "28816\t23.992\n" +
                    "28817\t10.5168\n" +
                    "28838\t15.2081\n" +
                    "28839\t23.9873\n" +
                    "28840\t30.7531\n" +
                    "28841\t21.8181\n" +
                    "29161\t20.8105\n" +
                    "29162\t27.9441\n" +
                    "29163\t28.5904\n" +
                    "29164\t14.3236\n" +
                    "29165\t5.5653\n" +
                    "29211\t4.78201\n" +
                    "29212\t14.1151\n" +
                    "29213\t15.0868\n" +
                    "29214\t12.9277\n" +
                    "29261\t8.35396\n" +
                    "29262\t13.3712\n" +
                    "29263\t12.9347\n" +
                    "29264\t10.1968\n" +
                    "29996\t10.4583\n" +
                    "29997\t15.6046\n" +
                    "29998\t14.0942\n" +
                    "29999\t10.2814\n" +
                    "30113\t12.8492\n" +
                    "30114\t14.2495\n" +
                    "30115\t12.9905\n" +
                    "30137\t7.70603\n" +
                    "30138\t12.388\n" +
                    "30139\t16.8338\n" +
                    "30140\t14.8809\n" +
                    "30141\t10.3703\n" +
                    "30538\t8.77779\n" +
                    "30539\t13.3174\n" +
                    "30540\t15.5756\n" +
                    "30541\t11.2439\n" +
                    "30665\t7.39289\n" +
                    "30666\t13.2579\n" +
                    "30667\t17.7614\n" +
                    "30668\t24.4368\n" +
                    "30669\t13.7147\n" +
                    "30670\t8.41425\n" +
                    "30862\t7.34079\n" +
                    "30863\t12.5315\n" +
                    "30864\t16.535\n" +
                    "30865\t18.0104\n" +
                    "30866\t12.1138\n" +
                    "30867\t6.58309\n" +
                    "30987\t0.117294\n" +
                    "30988\t7.6982\n" +
                    "30989\t33.7222\n" +
                    "30990\t43.0368\n" +
                    "30991\t41.7307\n" +
                    "30992\t31.3498\n" +
                    "30993\t3.7145\n" +
                    "31012\t1.20953\n" +
                    "31013\t5.93611\n" +
                    "31014\t26.1408\n" +
                    "31015\t34.4843\n" +
                    "31016\t35.2755\n" +
                    "31017\t27.0319\n" +
                    "31018\t3.19162\n" +
                    "31163\t12.7393\n" +
                    "31164\t14.4748\n" +
                    "31165\t13.6496\n" +
                    "31166\t10.6629\n" +
                    "31361\t8.05411\n" +
                    "31362\t16.6656\n" +
                    "31363\t22.6675\n" +
                    "31364\t26.2703\n" +
                    "31365\t17.498\n" +
                    "31366\t9.7606\n" +
                    "31367\t4.55423\n" +
                    "32161\t7.59437\n" +
                    "32162\t19.1119\n" +
                    "32163\t21.3849\n" +
                    "32164\t17.9111\n" +
                    "32165\t12.8884\n" +
                    "32166\t5.4914\n" +
                    "32212\t3.57956\n" +
                    "32213\t7.3008\n" +
                    "32214\t9.37027\n" +
                    "32215\t7.26105\n" +
                    "32216\t3.82722\n" +
                    "32262\t4.79592\n" +
                    "32263\t15.7791\n" +
                    "32264\t20.1478\n" +
                    "32265\t19.2273\n" +
                    "32266\t12.211\n" +
                    "32462\t9.26477\n" +
                    "32463\t11.643\n" +
                    "32464\t12.1435\n" +
                    "32465\t10.6902\n" +
                    "32466\t7.72181\n" +
                    "32587\t6.83658\n" +
                    "32588\t19.2516\n" +
                    "32589\t20.4182\n" +
                    "32590\t15.7438\n" +
                    "32591\t8.94715\n" +
                    "32592\t3.6351\n" +
                    "32612\t6.75768\n" +
                    "32613\t10.9715\n" +
                    "32614\t13.0799\n" +
                    "32615\t9.07203\n" +
                    "32616\t4.19995\n" +
                    "32815\t5.60248\n" +
                    "32816\t18.2516\n" +
                    "32817\t31.3259\n" +
                    "32818\t43.8515\n" +
                    "32819\t34.838\n" +
                    "32820\t22.925\n" +
                    "32821\t8.98064\n" +
                    "32822\t1.13839\n" +
                    "32866\t7.85014\n" +
                    "32867\t15.6306\n" +
                    "32868\t21.913\n" +
                    "32869\t22.1582\n" +
                    "32870\t16.04\n" +
                    "32911\t7.19334\n" +
                    "32912\t16.7538\n" +
                    "32913\t23.5804\n" +
                    "32914\t26.5813\n" +
                    "32915\t14.6319\n" +
                    "32916\t5.67929\n" +
                    "33512\t7.30782\n" +
                    "33513\t12.1415\n" +
                    "33514\t15.5602\n" +
                    "33515\t12.9943\n" +
                    "33516\t8.40582\n" +
                    "33563\t8.1216\n" +
                    "33564\t10.9726\n" +
                    "33565\t11.8767\n" +
                    "33566\t10.6435\n" +
                    "33815\t11.5812\n" +
                    "33816\t19.2645\n" +
                    "33817\t25.3931\n" +
                    "33818\t26.0911\n" +
                    "33819\t20.2536\n" +
                    "33820\t12.5646\n" +
                    "33839\t3.2439\n" +
                    "33840\t13.5112\n" +
                    "33841\t22.772\n" +
                    "33842\t30.2199\n" +
                    "33843\t31.0067\n" +
                    "33844\t15.2871\n" +
                    "33845\t5.23562\n" +
                    "35267\t9.93826\n" +
                    "35268\t13.3054\n" +
                    "35269\t14.66\n" +
                    "35270\t10.4746\n" +
                    "35363\t10.5664\n" +
                    "35364\t14.5398\n" +
                    "35365\t16.1192\n" +
                    "35366\t14.9417\n" +
                    "35367\t7.14574\n" +
                    "36063\t8.07499\n" +
                    "36064\t12.3379\n" +
                    "36065\t14.1686\n" +
                    "36066\t8.82438\n" +
                    "36088\t10.1254\n" +
                    "36089\t11.6446\n" +
                    "36090\t10.8338\n" +
                    "36091\t7.9403\n" +
                    "36913\t6.84019\n" +
                    "36914\t11.2017\n" +
                    "36915\t13.9629\n" +
                    "36916\t14.3162\n" +
                    "36917\t12.226\n" +
                    "36918\t8.37053\n" +
                    "37491\t5.16015\n" +
                    "37492\t16.8732\n" +
                    "37493\t26.1161\n" +
                    "37494\t32.1309\n" +
                    "37495\t27.9915\n" +
                    "37496\t18.7513\n" +
                    "37497\t6.33882\n" +
                    "37517\t17.2973\n" +
                    "37518\t25.8551\n" +
                    "37519\t30.5763\n" +
                    "37520\t25.4165\n" +
                    "37521\t16.4691\n" +
                    "37522\t3.78958\n" +
                    "37543\t3.26254\n" +
                    "37544\t5.13636\n" +
                    "37545\t4.47934\n" +
                    "37546\t1.3671\n" +
                    "37568\t3.74936\n" +
                    "37569\t6.8018\n" +
                    "37570\t7.5898\n" +
                    "37571\t6.03433\n" +
                    "37572\t2.80947\n" +
                    "37764\t7.07385\n" +
                    "37765\t10.9091\n" +
                    "37766\t12.7894\n" +
                    "37767\t12.2747\n" +
                    "37768\t9.70433\n" +
                    "37769\t5.96547\n" +
                    "37813\t8.84205\n" +
                    "37814\t12.451\n" +
                    "37815\t14.1568\n" +
                    "37816\t13.3788\n" +
                    "37817\t10.2073\n" +
                    "37818\t5.40328\n" +
                    "38132\t6.67458\n" +
                    "38133\t10.7084\n" +
                    "38134\t13.4162\n" +
                    "38135\t14.0358\n" +
                    "38136\t12.3969\n" +
                    "38137\t8.93762\n" +
                    "38566\t9.41151\n" +
                    "38567\t21.3571\n" +
                    "38568\t30.9755\n" +
                    "38569\t38.3318\n" +
                    "38570\t37.3695\n" +
                    "38571\t28.8927\n" +
                    "38572\t18.255\n" +
                    "38573\t4.98799\n" +
                    "38616\t6.56061\n" +
                    "38617\t13.8192\n" +
                    "38618\t19.6539\n" +
                    "38619\t20.8773\n" +
                    "38620\t15.8823\n" +
                    "38621\t9.14615\n" +
                    "38622\t0.387901\n" +
                    "38665\t2.5525\n" +
                    "38666\t7.28666\n" +
                    "38667\t15.7307\n" +
                    "38668\t19.278\n" +
                    "38669\t18.3025\n" +
                    "38670\t13.7235\n" +
                    "38671\t6.09367\n" +
                    "38672\t1.51931\n" +
                    "38915\t7.79577\n" +
                    "38916\t10.6631\n" +
                    "38917\t11.9496\n" +
                    "38918\t11.2052\n" +
                    "38919\t8.58028\n" +
                    "39166\t6.68614\n" +
                    "39167\t10.8752\n" +
                    "39168\t13.4195\n" +
                    "39169\t13.5419\n" +
                    "39170\t11.2998\n" +
                    "39171\t7.57062\n" +
                    "39763\t6.48889\n" +
                    "39764\t12.2315\n" +
                    "39765\t17.6449\n" +
                    "39766\t21.0304\n" +
                    "39767\t18.4208\n" +
                    "39768\t13.3262\n" +
                    "39769\t5.88691\n" +
                    "41016\t8.20905\n" +
                    "41017\t10.32\n" +
                    "41018\t10.2706\n" +
                    "41019\t8.04023\n" +
                    "41020\t4.2569\n" +
                    "41043\t6.9457\n" +
                    "41044\t8.99097\n" +
                    "41045\t9.04165\n" +
                    "41046\t7.14787\n" +
                    "41047\t3.90433\n" +
                    "41091\t3.18504\n" +
                    "41092\t7.30288\n" +
                    "41093\t10.6008\n" +
                    "41094\t11.8973\n" +
                    "41095\t10.7665\n" +
                    "41096\t7.57427\n" +
                    "41491\t3.06012\n" +
                    "41492\t14.1639\n" +
                    "41494\t21.805\n" +
                    "41495\t26.8257\n" +
                    "41496\t23.0964\n" +
                    "41497\t15.416\n" +
                    "41498\t5.6367\n" +
                    "41516\t1.29976\n" +
                    "41517\t9.96031\n" +
                    "41519\t16.063\n" +
                    "41520\t19.9886\n" +
                    "41521\t17.1278\n" +
                    "41522\t10.9393\n" +
                    "41523\t2.7826\n" +
                    "41543\t5.14096\n" +
                    "41544\t10.5506\n" +
                    "41545\t13.7584\n" +
                    "41546\t10.9986\n" +
                    "41547\t5.83704\n" +
                    "41568\t0.152111\n" +
                    "41569\t3.18825\n" +
                    "41570\t5.29032\n" +
                    "41571\t5.35692\n" +
                    "41572\t3.34372\n" +
                    "41669\t7.91095\n" +
                    "41670\t11.5935\n" +
                    "41671\t13.2105\n" +
                    "41672\t12.4784\n" +
                    "41673\t9.80927\n" +
                    "41675\t6.15645\n" +
                    "42166\t4.73407\n" +
                    "42167\t9.22006\n" +
                    "42169\t14.8376\n" +
                    "42170\t18.0212\n" +
                    "42171\t15.7075\n" +
                    "42172\t10.9759\n" +
                    "42173\t5.94351\n" +
                    "42966\t8.91548\n" +
                    "42967\t11.5828\n" +
                    "42969\t12.7034\n" +
                    "42970\t11.8181\n" +
                    "42971\t8.99297\n" +
                    "43069\t7.07715\n" +
                    "43070\t10.4059\n" +
                    "43071\t11.7604\n" +
                    "43073\t10.8909\n" +
                    "43074\t8.19454\n" +
                    "43215\t7.12543\n" +
                    "43216\t10.7677\n" +
                    "43217\t12.7886\n" +
                    "43218\t12.5005\n" +
                    "43219\t9.96907\n" +
                    "43221\t6.18099\n" +
                    "43366\t6.60843\n" +
                    "43367\t9.71622\n" +
                    "43368\t10.7211\n" +
                    "43369\t9.33206\n" +
                    "43370\t6.07931\n" +
                    "43416\t5.43539\n" +
                    "43418\t9.7579\n" +
                    "43419\t15.8968\n" +
                    "43420\t17.5526\n" +
                    "43421\t14.2742\n" +
                    "43423\t9.41383\n" +
                    "43424\t4.91346\n" +
                    "43517\t8.58328\n" +
                    "43519\t11.717\n" +
                    "43520\t12.8871\n" +
                    "43521\t11.7739\n" +
                    "43522\t8.81306\n" +
                    "43920\t8.43109\n" +
                    "43921\t12.3333\n" +
                    "43922\t14.1241\n" +
                    "43923\t13.2563\n" +
                    "43925\t10.1035\n" +
                    "43926\t5.90816\n" +
                    "44019\t9.46334\n" +
                    "44020\t11.5491\n" +
                    "44021\t11.8164\n" +
                    "44022\t9.99584\n" +
                    "44024\t6.50859\n" +
                    "44114\t4.9883\n" +
                    "44115\t13.2598\n" +
                    "44117\t19.7649\n" +
                    "44118\t24.5351\n" +
                    "44119\t22.9716\n" +
                    "44120\t17.064\n" +
                    "44122\t9.37874\n" +
                    "44123\t1.78369\n" +
                    "44165\t2.53391\n" +
                    "44166\t6.66144\n" +
                    "44168\t9.60657\n" +
                    "44169\t10.2121\n" +
                    "44170\t8.27963\n" +
                    "44171\t4.48633\n" +
                    "44267\t0.825155\n" +
                    "44269\t5.24131\n" +
                    "44270\t11.4643\n" +
                    "44271\t11.4112\n" +
                    "44272\t7.08695\n" +
                    "44274\t1.97552\n" +
                    "44294\t1.63492\n" +
                    "44295\t3.71093\n" +
                    "44296\t3.65935\n" +
                    "44297\t1.38832\n" +
                    "44316\t0.338752\n" +
                    "44317\t12.1913\n" +
                    "44319\t25.7281\n" +
                    "44320\t36.2637\n" +
                    "44321\t39.6803\n" +
                    "44322\t31.4197\n" +
                    "44324\t19.9745\n" +
                    "44325\t5.47298\n" +
                    "44367\t2.72235\n" +
                    "44368\t12.2486\n" +
                    "44370\t19.5573\n" +
                    "44371\t22.0814\n" +
                    "44372\t16.9335\n" +
                    "44373\t9.62226\n" +
                    "44375\t0.408239\n" +
                    "44416\t3.0339\n" +
                    "44418\t8.891\n" +
                    "44419\t14.3953\n" +
                    "44420\t18.2761\n" +
                    "44421\t16.6252\n" +
                    "44423\t11.6702\n" +
                    "44424\t5.15967\n" +
                    "44517\t8.11916\n" +
                    "44518\t11.9559\n" +
                    "44520\t17.8213\n" +
                    "44521\t17.0752\n" +
                    "44522\t11.0482\n" +
                    "44523\t6.7605\n" +
                    "44966\t0.622545\n" +
                    "44967\t9.86341\n" +
                    "44969\t16.0553\n" +
                    "44970\t20.3982\n" +
                    "44971\t18.3323\n" +
                    "44973\t12.6847\n" +
                    "44974\t5.32883\n" +
                    "44975\t0.574179\n" +
                    "44991\t0.735199\n" +
                    "44992\t7.70474\n" +
                    "44994\t14.2144\n" +
                    "44995\t19.4846\n" +
                    "44996\t20.4257\n" +
                    "44998\t15.3546\n" +
                    "44999\t8.60155\n" +
                    "45018\t2.58296\n" +
                    "45019\t5.3303\n" +
                    "45020\t6.57632\n" +
                    "45022\t5.71801\n" +
                    "45023\t2.90091\n" +
                    "45416\t3.68317\n" +
                    "45418\t12.5213\n" +
                    "45419\t19.604\n" +
                    "45420\t24.8965\n" +
                    "45422\t23.5806\n" +
                    "45423\t17.5756\n" +
                    "45424\t9.84344\n" +
                    "45425\t2.99654\n" +
                    "45467\t4.01019\n" +
                    "45468\t7.97983\n" +
                    "45470\t13.6571\n" +
                    "45471\t15.64\n" +
                    "45472\t9.87432\n" +
                    "45473\t6.54209\n" +
                    "45475\t2.40541\n" +
                    "48241\t3.77335\n" +
                    "48242\t8.80224\n" +
                    "48244\t16.3497\n" +
                    "48245\t20.2651\n" +
                    "48247\t19.0325\n" +
                    "48248\t14.0419\n" +
                    "48249\t6.90724\n" +
                    "48268\t6.72036\n" +
                    "48269\t10.239\n" +
                    "48271\t11.32\n" +
                    "48272\t9.7794\n" +
                    "48274\t6.29187\n" +
                    "48868\t7.39228\n" +
                    "48870\t13.4758\n" +
                    "48871\t18.4091\n" +
                    "48872\t19.0789\n" +
                    "48874\t14.9974\n" +
                    "48875\t7.69221\n" +
                    "48917\t3.17692\n" +
                    "48919\t11.6478\n" +
                    "48920\t17.2325\n" +
                    "48922\t19.9867\n" +
                    "48923\t16.7048\n" +
                    "48925\t11.4943\n" +
                    "48926\t4.59241\n" +
                    "49767\t3.06374\n" +
                    "49769\t14.3368\n" +
                    "49770\t23.8741\n" +
                    "49772\t31.3512\n" +
                    "49773\t30.5957\n" +
                    "49775\t22.8097\n" +
                    "49776\t13.1211\n" +
                    "49778\t3.24415\n" +
                    "49818\t0.86248\n" +
                    "49819\t10.6708\n" +
                    "49821\t17.4412\n" +
                    "49822\t21.9783\n" +
                    "49824\t19.8518\n" +
                    "49825\t13.873\n" +
                    "49826\t6.26368\n" +
                    "49870\t5.31741\n" +
                    "49872\t9.00712\n" +
                    "49873\t10.6781\n" +
                    "49875\t9.90762\n" +
                    "49876\t6.95878\n" +
                    "50167\t5.39783\n" +
                    "50169\t9.81165\n" +
                    "50170\t12.9815\n" +
                    "50172\t13.8444\n" +
                    "50173\t12.1054\n" +
                    "50175\t8.33562\n" +
                    "50219\t5.88732\n" +
                    "50220\t10.094\n" +
                    "50222\t12.5461\n" +
                    "50223\t12.5483\n" +
                    "50225\t10.2631\n" +
                    "50226\t6.47616\n" +
                    "50720\t6.32888\n" +
                    "50722\t20.5148\n" +
                    "50724\t32.572\n" +
                    "50725\t41.7948\n" +
                    "50727\t39.9157\n" +
                    "50728\t29.6456\n" +
                    "50730\t16.6255\n" +
                    "50731\t4.55545\n" +
                    "50771\t5.69243\n" +
                    "50773\t17.069\n" +
                    "50774\t25.2749\n" +
                    "50776\t29.8006\n" +
                    "50777\t24.8668\n" +
                    "50779\t16.5316\n" +
                    "50780\t4.78121\n" +
                    "51069\t6.71173\n" +
                    "51070\t14.073\n" +
                    "51072\t18.7196\n" +
                    "51073\t19.7227\n" +
                    "51075\t15.4133\n" +
                    "51076\t9.47508\n" +
                    "51078\t4.66959\n" +
                    "51095\t7.76546\n" +
                    "51096\t11.1769\n" +
                    "51098\t12.3645\n" +
                    "51100\t11.0378\n" +
                    "51101\t7.81487\n" +
                    "52369\t7.81879\n" +
                    "52370\t15.0331\n" +
                    "52372\t19.879\n" +
                    "52373\t20.6953\n" +
                    "52375\t16.2737\n" +
                    "52377\t9.49418\n" +
                    "53669\t9.2309\n" +
                    "53670\t11.7219\n" +
                    "53672\t12.5047\n" +
                    "53674\t11.411\n" +
                    "53675\t8.73647\n" +
                    "54519\t10.3131\n" +
                    "54521\t16.4502\n" +
                    "54523\t21.0991\n" +
                    "54525\t29.4348\n" +
                    "54526\t20.7904\n" +
                    "54528\t15.8696\n" +
                    "54530\t9.46773\n" +
                    "55422\t13.9799\n" +
                    "55423\t25.391\n" +
                    "55425\t34.3175\n" +
                    "55427\t35.4124\n" +
                    "55429\t27.3742\n" +
                    "55430\t17.092\n" +
                    "55432\t5.16378\n" +
                    "55473\t9.85614\n" +
                    "55474\t15.2769\n" +
                    "55476\t18.0356\n" +
                    "55478\t17.1175\n" +
                    "55480\t12.7201\n" +
                    "55481\t6.24151\n" +
                    "56222\t12.4884\n" +
                    "56223\t16.1816\n" +
                    "56225\t17.5347\n" +
                    "56227\t16.0285\n" +
                    "56229\t11.9772\n" +
                    "56718\t8.07623\n" +
                    "56720\t18.0096\n" +
                    "56722\t26.1133\n" +
                    "56724\t32.0826\n" +
                    "56726\t30.4745\n" +
                    "56727\t23.308\n" +
                    "56729\t13.8227\n" +
                    "56731\t6.05965\n" +
                    "57119\t11.1558\n" +
                    "57121\t22.3034\n" +
                    "57123\t30.2362\n" +
                    "57125\t34.3814\n" +
                    "57126\t30.1223\n" +
                    "57128\t22.3362\n" +
                    "57130\t10.0981\n" +
                    "57572\t3.47367\n" +
                    "57574\t18.4236\n" +
                    "57575\t29.3979\n" +
                    "57577\t37.6032\n" +
                    "57579\t35.7345\n" +
                    "57581\t26.9146\n" +
                    "57583\t16.037\n" +
                    "57585\t4.901\n" +
                    "57624\t8.28251\n" +
                    "57626\t14.2676\n" +
                    "57628\t17.2829\n" +
                    "57630\t16.2633\n" +
                    "57632\t11.5784\n" +
                    "57634\t4.94727\n" +
                    "60622\t13.1516\n" +
                    "60624\t17.7249\n" +
                    "60626\t19.1872\n" +
                    "60628\t17.0633\n" +
                    "60630\t12.0397\n" +
                    "61022\t12.1501\n" +
                    "61024\t15.6962\n" +
                    "61026\t17.1211\n" +
                    "61028\t15.9369\n" +
                    "61030\t12.2339\n" +
                    "61126\t11.0335\n" +
                    "61128\t15.8781\n" +
                    "61130\t18.6114\n" +
                    "61132\t18.5539\n" +
                    "61134\t15.7481\n" +
                    "61136\t10.8709\n" +
                    "61472\t12.0006\n" +
                    "61474\t16.6282\n" +
                    "61476\t18.4499\n" +
                    "61478\t16.9319\n" +
                    "61480\t12.5636\n" +
                    "61924\t11.3476\n" +
                    "61926\t25.4569\n" +
                    "61928\t36.1577\n" +
                    "61930\t41.3306\n" +
                    "61932\t34.6378\n" +
                    "61935\t23.829\n" +
                    "61937\t8.70329\n" +
                    "61974\t2.66855\n" +
                    "61976\t15.8798\n" +
                    "61978\t27.12\n" +
                    "61980\t35.5062\n" +
                    "61982\t33.7229\n" +
                    "61984\t24.1526\n" +
                    "61986\t12.1323\n" +
                    "62025\t6.60523\n" +
                    "62027\t12.1763\n" +
                    "62030\t15.2405\n" +
                    "62032\t14.835\n" +
                    "62034\t11.1038\n" +
                    "62036\t5.23982\n" +
                    "62774\t13.6724\n" +
                    "62776\t27.2057\n" +
                    "62778\t37.3465\n" +
                    "62780\t40.4862\n" +
                    "62782\t33.0524\n" +
                    "62785\t22.9335\n" +
                    "62787\t7.87996\n" +
                    "62825\t6.18988\n" +
                    "62827\t14.9191\n" +
                    "62829\t20.4807\n" +
                    "62831\t21.1471\n" +
                    "62833\t16.9324\n" +
                    "62835\t9.52644\n" +
                    "64374\t13.2556\n" +
                    "64376\t18.1321\n" +
                    "64378\t19.1728\n" +
                    "64380\t16.1724\n" +
                    "64382\t10.1537\n" +
                    "64422\t9.21877\n" +
                    "64424\t16.3019\n" +
                    "64426\t20.7243\n" +
                    "64428\t21.1171\n" +
                    "64431\t17.2761\n" +
                    "64433\t10.3968\n" +
                    "65121\t11.4263\n" +
                    "65123\t26.7514\n" +
                    "65125\t37.7389\n" +
                    "65127\t43.7013\n" +
                    "65129\t37.2608\n" +
                    "65132\t26.415\n" +
                    "65134\t11.3931\n" +
                    "65172\t7.63387\n" +
                    "65174\t14.1632\n" +
                    "65176\t17.2471\n" +
                    "65179\t16.032\n" +
                    "65181\t11.262\n" +
                    "65183\t4.83256\n" +
                    "65223\t12.9612\n" +
                    "65226\t17.8244\n" +
                    "65228\t19.0126\n" +
                    "65230\t16.0808\n" +
                    "65232\t9.8418\n" +
                    "67580\t13.9297\n" +
                    "67583\t18.7701\n" +
                    "67585\t20.5445\n" +
                    "67587\t18.7378\n" +
                    "67590\t13.9007\n" +
                    "68078\t16.9672\n" +
                    "68080\t21.1597\n" +
                    "68083\t21.8983\n" +
                    "68085\t19.0987\n" +
                    "68087\t13.5365\n" +
                    "69376\t11.5028\n" +
                    "69378\t16.7047\n" +
                    "69381\t18.4698\n" +
                    "69383\t16.0144\n" +
                    "69386\t9.78979\n" +
                    "69427\t14.2984\n" +
                    "69430\t18.5474\n" +
                    "69432\t19.7375\n" +
                    "69435\t17.2372\n" +
                    "69437\t11.5617\n" +
                    "69923\t7.73953\n" +
                    "69926\t17.016\n" +
                    "69928\t29.3614\n" +
                    "69931\t33.1891\n" +
                    "69933\t27.858\n" +
                    "69936\t16.7715\n" +
                    "69938\t8.50217\n" +
                    "70773\t7.85853\n" +
                    "70775\t24.8446\n" +
                    "70778\t35.8907\n" +
                    "70780\t41.2052\n" +
                    "70783\t34.2362\n" +
                    "70785\t23.4115\n" +
                    "70788\t9.7664\n" +
                    "70790\t2.1513\n" +
                    "70824\t6.77829\n" +
                    "70827\t19.8995\n" +
                    "70830\t29.4677\n" +
                    "70832\t34.1115\n" +
                    "70835\t28.5624\n" +
                    "70837\t18.8624\n" +
                    "70840\t5.93172\n" +
                    "70875\t9.53732\n" +
                    "70878\t16.0138\n" +
                    "70880\t19.275\n" +
                    "70883\t18.3769\n" +
                    "70885\t13.75\n" +
                    "70888\t7.13268\n" +
                    "72070\t6.05628\n" +
                    "72072\t17.7805\n" +
                    "72075\t27.5069\n" +
                    "72078\t35.0284\n" +
                    "72080\t33.7148\n" +
                    "72083\t25.1457\n" +
                    "72085\t14.2439\n" +
                    "72088\t5.79962\n" +
                    "72124\t8.18573\n" +
                    "72127\t13.825\n" +
                    "72130\t15.6341\n" +
                    "72132\t13.2327\n" +
                    "72135\t7.60198\n" +
                    "77726\t12.1009\n" +
                    "77729\t17.1866\n" +
                    "77732\t19.3027\n" +
                    "77735\t17.9959\n" +
                    "77738\t13.8439\n" +
                    "77741\t8.15855\n" +
                    "77776\t7.76871\n" +
                    "77779\t14.9261\n" +
                    "77782\t19.8498\n" +
                    "77785\t20.8836\n" +
                    "77788\t17.7267\n" +
                    "77790\t11.4475\n";


    static List<SpectrumBinnedScore> buildCometBinnedScores() {
        List<SpectrumBinnedScore> holder = new ArrayList<SpectrumBinnedScore>();
        String[] lines = CORRELATION_DATA_FROM_COMET_EG0.trim().split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            holder.add(new SpectrumBinnedScore(line));
        }

        return holder;
    }

    public static final String USED_EG0_CROSSCORRELATION_DATA =
            "1\tB\t12205\t19.1886\n" +
                    "1\tB\t20206\t34.6509\n" +
                    "1\tB\t33214\t-0.337312\n" +
                    "1\tB\t37565\t-11.4424\n" +
                    "1\tB\t44418\t10.3739\n" +
                    "1\tB\t50823\t-3.94382\n" +
                    "1\tB\t62326\t-0.665863\n" +
                    "1\tY\t12208\t50.4785\n" +
                    "1\tY\t17863\t46.6895\n" +
                    "1\tY\t23614\t15.8114\n" +
                    "1\tY\t28467\t42.7259\n" +
                    "1\tY\t32818\t43.8515\n" +
                    "1\tY\t38570\t37.0649\n" +
                    "1\tY\t44321\t42.5699\n" +
                    "1\tY\t50726\t-4.44906\n" +
                    "1\tY\t57579\t34.2652\n" +
                    "1\tY\t61930\t40.2915\n" +
                    "1\tY\t67584\t-1.86702\n" +
                    "2\tB\t10128\t-0.135807\n" +
                    "2\tB\t13805\t-0.158366\n" +
                    "2\tB\t16632\t-0.397749\n" +
                    "2\tB\t22234\t-0.583535\n" +
                    "2\tB\t25437\t-2.13863\n" +
                    "2\tB\t31188\t-1.09631\n" +
                    "2\tB\t38666\t7.28666\n" +
                    "2\tB\t41493\t-8.82698\n" +
                    "2\tB\t43919\t-1.4257\n" +
                    "2\tY\t8957\t-0.17874\n" +
                    "2\tY\t14259\t5.6679\n" +
                    "2\tY\t16434\t-0.480113\n" +
                    "2\tY\t19310\t-1.83758\n" +
                    "2\tY\t22186\t-0.583535\n" +
                    "2\tY\t25388\t18.7246\n" +
                    "2\tY\t28815\t37.7037\n" +
                    "2\tY\t30990\t43.0368\n" +
                    "2\tY\t33817\t25.3931\n" +
                    "2\tY\t37494\t32.1309\n" +
                    "2\tY\t41495\t26.8257";

    public static List<XCorrUsedData> buildXCorrUsedData() {
        List<XCorrUsedData> holder = new ArrayList<XCorrUsedData>();
        String[] lines = USED_EG0_CROSSCORRELATION_DATA.trim().split("\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            holder.add(new XCorrUsedData(line));
        }
        return holder;
    }

    public static void testUsedXCorrData(final List<XCorrUsedData> pUsed) {
        List<XCorrUsedData> compare = CometTestData.buildXCorrUsedData();
        //     Assert.assertEquals(compare.size(), pUsed.size());
        for (int i = 0; i < compare.size(); i++) {
            XCorrUsedData expexted = compare.get(i);
            XCorrUsedData seen = pUsed.get(i);
            if (!expexted.equivalent(seen))
                Assert.assertTrue(expexted.equivalent(seen));

        }

    }


}
