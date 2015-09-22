package com.lordjoe.distributed.hydra.comet;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
 * com.lordjoe.distributed.hydra.comet.CometProperties
 * User: Steve
 * Date: 9/14/2015
 */
public class CometProperties {


    private static Properties CORRESPONDANCES = new Properties();

    static {
        CORRESPONDANCES.setProperty("peptide_mass_tolerance", "spectrum, fragment mass error");
        CORRESPONDANCES.setProperty("fragment_bin_tol", "comet.fragment_bin_tol");
        CORRESPONDANCES.setProperty("fragment_bin_offset", "comet.fragment_bin_offset");
        CORRESPONDANCES.setProperty("mass_tolerance", "comet.mass_tolerance");
        CORRESPONDANCES.setProperty("max_fragment_charge", "comet.max_fragment_charge");


    }

    public static abstract class PropertyHandler {
        public abstract void handleProperty(XTandemMain holder, String value);
    }

    private static Map<String, PropertyHandler> gHandlers = new HashMap<String, PropertyHandler>();

    private static void putHandler(String id, PropertyHandler handler) {
        if (gHandlers.containsKey(id))
            throw new IllegalStateException("Duplicate handler " + id);
        gHandlers.put(id, handler);
    }


    /**
     * when there is a one to one correspondence between comet and tandem properties
     */
    public static class CorrespondanceHandler extends PropertyHandler {
        private final String tp;

        public CorrespondanceHandler(final String tandemProperty) {
            tp = tandemProperty;
        }

        @Override
        public void handleProperty(final XTandemMain holder, final String value) {
            holder.setParameter(tp, value);
        }
    }

    public static PropertyHandler getHandler(String id) {
        String prop = CORRESPONDANCES.getProperty(id);
        if (prop != null)
            return new CorrespondanceHandler(prop);
        return gHandlers.get(id);
    }

    public static class RequireValue extends PropertyHandler {
        private final String required;

        public RequireValue(final String pRequired) {
            required = pRequired;
        }

        @Override
        public void handleProperty(final XTandemMain holder, final String value) {
            if (value.equals(required)) return;
            throw new UnsupportedOperationException("Cannot handle values " + value + "- required " + required);

        }
    }

    public static PropertyHandler IGNORE = new PropertyHandler() {
        @Override
        public void handleProperty(final XTandemMain holder, final String value) {
            return;
        }
    };

    private static PropertyHandler VariableModHandler = new PropertyHandler() {
        @Override
        public void handleProperty(final XTandemMain holder, final String value) {
            if (value.startsWith("0.0 ")) return;
            String[] props = value.split(" ");
            if (props.length < 7)
                throw new IllegalStateException("shhould look like 15.9949 M 0 3 -1 0 0 not " + value);
            String mod = props[0] + "@" + props[1].trim();
            PeptideModification pm = PeptideModification.fromString("15.9949@M", PeptideModificationRestriction.Global, false);
            XTandemMain app = (XTandemMain) holder;
            ScoringModifications scoringMods = app.getScoringMods();
            scoringMods.addModification(pm);

        }
    };
    private static PropertyHandler MassFragmentHandler = new PropertyHandler() {
        @Override
        public void handleProperty(final XTandemMain holder, final String value) {
            if (value.equals("0"))
                holder.setParameter("spectrum, fragment mass type", "average");
            else
                holder.setParameter("spectrum, fragment mass type", "monoisotopic ");
        }
    };


    private static PropertyHandler Require0 = new RequireValue("0");
    private static PropertyHandler Require0000 = new RequireValue("0.0000");


    private static PropertyHandler Require1 = new RequireValue("1");

    static {
        putHandler("database_name", new PropertyHandler() {
            @Override
            public void handleProperty(final XTandemMain holder, final String value) {
                holder.setParameter("spectrum, path", value);
            }
        });

        putHandler("num_threads", IGNORE);
        putHandler("precursor_tolerance_type", Require0);
        putHandler("isotope_error", Require0);
        putHandler("peptide_mass_units", Require0);
        putHandler("search_enzyme_number", Require0);
        putHandler("mass_type_parent", IGNORE);  // todo is this right
        putHandler("mass_type_fragment", MassFragmentHandler);
        putHandler("num_enzyme_termini", IGNORE);  // todo is this right
        putHandler("max_variable_mods_in_peptide", IGNORE);  // todo is this right


        putHandler("allowed_missed_cleavage", new PropertyHandler() {
            @Override
            public void handleProperty(final XTandemMain holder, final String value) {
                holder.setParameter("scoring, maximum missed cleavage sites", value);
            }
        });

        putHandler("use_A_ions", Require0);
        putHandler("use_X_ions", Require0);
        putHandler("use_C_ions", Require0);
        putHandler("use_Z_ions", Require0);
        putHandler("use_Y_ions", Require1);
        putHandler("use_NL_ions", Require1);
        putHandler("use_B_ions", Require1);
        putHandler("theoretical_fragment_ions", Require1);

        putHandler("decoy_search", Require0);
        putHandler("output_sqtstream", Require1);
        putHandler("output_sqtfile", Require0);
        putHandler("output_txtfile", Require0);
        putHandler("output_pepxmlfile", Require0);
        putHandler("output_percolatorfile", Require0);
        putHandler("output_outfiles", Require0);
        putHandler("print_expect_score", Require1);
        putHandler("num_output_lines", IGNORE);
        putHandler("show_fragment_ions", Require0);
        putHandler("sample_enzyme_number", Require1);
        putHandler("scan_range", IGNORE);
        putHandler("precursor_charge", IGNORE);
        putHandler("override_charge", Require0);
        putHandler("ms_level", new RequireValue("2"));
        putHandler("digest_mass_range", IGNORE);   // todo handle digest_mass_range = 600.0 5000.0
        putHandler("num_results", IGNORE);   // todo handle num_results = 100
        putHandler("skip_researching", Require1);
        putHandler("max_precursor_charge", IGNORE);   // todo handle max_precursor_charge = 6
        putHandler("decoy_prefix", new RequireValue("DECOY_"));
        putHandler("minimum_peaks", IGNORE);   // todo handle minimum_peaks = 10
        putHandler("minimum_intensity", IGNORE);   // todo handle minimum_intensity = 0
        putHandler("remove_precursor_peak", Require0);
        putHandler("remove_precursor_tolerance", IGNORE);   // todo handle remove_precursor_tolerance = 1.5
        putHandler("clear_mz_range", IGNORE);   // todo handle clear_mz_range = 0.0 0.0
        putHandler("add_Nterm_peptide", IGNORE);   // todo handle add_Nterm_peptide = 0.0
        putHandler("add_Cterm_protein", IGNORE);   // todo handle add_Cterm_protein = 0.0
        putHandler("add_Nterm_protein", IGNORE);   // todo handle add_Nterm_protein = 0.0
        putHandler("add_G_glycine", Require0000);   // todo handle add_G_glycine = 0.0000
        putHandler("add_A_alanine", Require0000);   // todo handle add_G_glycine = 0.0000
        putHandler("add_S_serine", Require0000);   // todo handle add_G_glycine = 0.0000
        putHandler("add_P_proline", Require0000);   // todo handle add_P_proline = 0.0000
        putHandler("add_V_valine", Require0000);   // todo handle add_V_valine = 0.0000
        putHandler("add_K_lysine", Require0000);   // todo handle add_V_valine = 0.0000
          putHandler("add_T_threonine", Require0000);   // todo handle add_T_threonine = 0.0000
        putHandler("add_L_leucine", Require0000);   // todo handle add_L_leucine = 0.0000
        putHandler("add_I_isoleucine", Require0000);   // todo handle add_I_isoleucine = 0.0000
        putHandler("add_N_asparagine", Require0000);   // todo handle add_N_asparagine = 0.0000
        putHandler("add_D_aspartic_acid", Require0000);   // todo handle add_D_aspartic_acid = 0.0000
        putHandler("add_Q_glutamine", Require0000);   // todo handle add_Q_glutamine = 0.0000
        putHandler("add_E_glutamic_acid", Require0000);   // todo handle add_E_glutamic_acid = 0.0000
        putHandler("add_M_methionine", Require0000);   // todo handle add_M_methionine = 0.0000
        putHandler("add_O_ornithine", Require0000);   // todo handle add_O_ornithine = 0.0000
        putHandler("add_H_histidine", Require0000);   // todo handle add_H_histidine = 0.0000
        putHandler("add_F_phenylalanine", Require0000);   // todo handle add_F_phenylalanine = 0.0000
        putHandler("add_R_arginine", Require0000);   // todo handle add_R_arginine = 0.0000
        putHandler("add_Y_tyrosine", Require0000);   // todo handle add_Y_tyrosine = 0.0000
        putHandler("add_W_tryptophan", Require0000);   // todo handle add_W_tryptophan = 0.0000
        putHandler("add_B_user_amino_acid", Require0000);   // todo handle add_B_user_amino_acid = 0.0000
         putHandler("add_C_cysteine", IGNORE);   // todo handle add_B_user_amino_acid = 0.0000
        //    " = 57.021464             # added to C - avg. 103.1429, mono. 103.00918\n" +
         putHandler("add_J_user_amino_acid", Require0000);   // todo handle add_B_user_amino_acid = 0.0000
        putHandler("add_U_user_amino_acid", Require0000);   // todo handle add_B_user_amino_acid = 0.0000
        putHandler("add_X_user_amino_acid", Require0000);   // todo handle add_B_user_amino_acid = 0.0000
        putHandler("add_Z_user_amino_acid", Require0000);   // todo handle add_B_user_amino_acid = 0.0000
        //   putHandler("add_Nterm_protein", IGNORE);   // todo handle add_Nterm_protein = 0.0

        putHandler("variable_mod01", VariableModHandler);
        putHandler("variable_mod02", VariableModHandler);
        putHandler("variable_mod03", VariableModHandler);
        putHandler("variable_mod04", VariableModHandler);
        putHandler("variable_mod05", VariableModHandler);
        putHandler("variable_mod06", VariableModHandler);
        putHandler("variable_mod07", VariableModHandler);
        putHandler("variable_mod08", VariableModHandler);
        putHandler("variable_mod09", VariableModHandler);

        putHandler("require_variable_mod", Require0);
        putHandler("nucleotide_reading_frame", Require0);
        putHandler("clip_nterm_methionine", Require0);
        putHandler("spectrum_batch_size", Require0);
        putHandler("activation_method", new RequireValue("ALL"));

        putHandler("add_Cterm_peptide", new RequireValue("0.0"));


    }

}
