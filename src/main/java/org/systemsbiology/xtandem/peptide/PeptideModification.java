package org.systemsbiology.xtandem.peptide;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.peptide.PeptideModification
 * information about a potential peptide modification
 * User: steven
 * Date: 6/30/11
 */
public class PeptideModification implements Comparable<PeptideModification>,Serializable {
    public static final PeptideModification[] EMPTY_ARRAY = {};

    private static boolean gHardCodeModifications = false; // onlt true for tandem true;

    public static final Random RND = new Random();


    public static final int MAX_RANDOM_MASS_CHANGE = 20;

    /**
     * used by test code
     * @return
     */
    public static PeptideModification randomModification()
    {
        double randomMassChange = RND.nextDouble() * MAX_RANDOM_MASS_CHANGE;
        return new PeptideModification(FastaAminoAcid.randomAminoAcid(),randomMassChange);
    }


    public static PeptideModification[] selectFixedModifications(PeptideModification[] mods)
    {
         List<PeptideModification> holder = new ArrayList<PeptideModification>();
        for (int i = 0; i < mods.length; i++) {
            PeptideModification mod = mods[i];
            if(mod.isFixed())
                holder.add(mod);
        }
         PeptideModification[] ret = new PeptideModification[holder.size()];
         holder.toArray(ret);
         return ret;
    }


    public static PeptideModification[] selectPotentialModifications(PeptideModification[] mods)
    {
         List<PeptideModification> holder = new ArrayList<PeptideModification>();
        for (int i = 0; i < mods.length; i++) {
            PeptideModification mod = mods[i];
            if(!mod.isFixed())
                holder.add(mod);
        }
         PeptideModification[] ret = new PeptideModification[holder.size()];
         holder.toArray(ret);
         return ret;
    }

     /**
     * true if hadr coded modifications are used
     * @return
     */
    public static boolean isHardCodeModifications() {
        return gHardCodeModifications;
    }

    /**
     * normally a few peptide modifications are hard coded
     * @param hardCodeModifications
     */
    public static void setHardCodeModifications(boolean hardCodeModifications) {
        if(gHardCodeModifications == hardCodeModifications)
            return;
        gHardCodeModifications = hardCodeModifications;
        resetHardCodedModifications();
    }


    private static final Map<String, PeptideModification> gAsString = new HashMap<String, PeptideModification>();

    public static PeptideModification[] fromListString(String mods, PeptideModificationRestriction restrict, boolean fixed) {
        List<PeptideModification> holder = new ArrayList<PeptideModification>();
        String[] items = mods.split(",");
        for (int i = 0; i < items.length; i++) {
            holder.add(fromString(items[i], restrict, fixed));
        }
        PeptideModification[] ret = new PeptideModification[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    public static PeptideModification fromString(String s, PeptideModificationRestriction restrict, boolean fixed) {
        PeptideModification ret = gAsString.get(s);
        if (ret == null) {
            ret = new PeptideModification(s, restrict, fixed);
            gAsString.put(s, ret);
        }
        return ret;
    }

    public static PeptideModification[] fromModificationString(String sequence, String mods) {
        PeptideModification[] ret = new PeptideModification[sequence.length()];
        String[] items = mods.split(",");
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            String[] elements = item.split(":");
            int index = Integer.parseInt(elements[0]);
            PeptideModification pm = PeptideModification.fromString(elements[1], PeptideModificationRestriction.Global, false);
            ret[index] = pm;
        }

        return ret;
    }


    /**
     * XTandem codes these potential modifications of the N Terminal
     */
    private static final Map<FastaAminoAcid, PeptideModification[]> gNTermalModifications =
            new HashMap<FastaAminoAcid, PeptideModification[]>();
    /**
     * XTandem cods these potential modifications of the N Terminal
     */
    private static final Map<FastaAminoAcid, PeptideModification[]> gCTermalModifications =
            new HashMap<FastaAminoAcid, PeptideModification[]>();

    public static PeptideModification[] getNTerminalModifications(FastaAminoAcid aa) {
        guaranteeHardCodedModifications();
        PeptideModification[] pm = gNTermalModifications.get(aa);
        if (pm == null)  {
             pm = gNTermalModifications.get(FastaAminoAcid.X); // any
            if (pm == null)
                return PeptideModification.EMPTY_ARRAY;
        }
        return pm;
    }

    public static PeptideModification[] getCTerminalModifications(FastaAminoAcid aa) {
        guaranteeHardCodedModifications();
        PeptideModification[] pm = gCTermalModifications.get(aa);
        if (pm == null)  {
             pm = gCTermalModifications.get(FastaAminoAcid.X); // any
            if (pm == null)
                return PeptideModification.EMPTY_ARRAY;
        }
        return pm;
    }

    public static PeptideModification[] getTerminalModifications( ) {
         guaranteeHardCodedModifications();
        List<PeptideModification> holder = new ArrayList<PeptideModification>();
        for(PeptideModification[] pms : gNTermalModifications.values())
             holder.addAll(Arrays.asList(pms));
        for(PeptideModification[] pms : gCTermalModifications.values())
             holder.addAll(Arrays.asList(pms));
         PeptideModification[] ret = new PeptideModification[holder.size()];
        holder.toArray(ret);
        return ret;
       }



    public static final double CYSTEIN_MODIFICATION_MASS =  57.02146;  // alkylation of cysteine

     private static PeptideModification gCYSTEIN_MODIFICATION;

    public static PeptideModification getCysteinModification()
    {
        if(gCYSTEIN_MODIFICATION == null)
            gCYSTEIN_MODIFICATION = new PeptideModification(FastaAminoAcid.C, CYSTEIN_MODIFICATION_MASS,
                         PeptideModificationRestriction.Global, true);
        return gCYSTEIN_MODIFICATION;
    }

    public static void guaranteeHardCodedModifications() {
        if(!isHardCodeModifications())
            return;
        // new PeptideModification(FastaAminoAcid.C, CYSTEIN_MODIFICATION_MASS,
        //        PeptideModificationRestriction.Global, true);
        if (gNTermalModifications.isEmpty()) {
            double nh3 = MassCalculator.getDefaultCalculator().calcMass("NH3");
            double h2O = MassCalculator.getDefaultCalculator().calcMass("H2O");
            new PeptideModification(FastaAminoAcid.Q, -nh3,
                    PeptideModificationRestriction.NTerminal, false);
            new PeptideModification(FastaAminoAcid.C, -nh3,
                    PeptideModificationRestriction.NTerminal, false);
            new PeptideModification(FastaAminoAcid.E, -h2O,
                    PeptideModificationRestriction.NTerminal, false);
        }
    }


    public static void resetHardCodedModifications() {
        gNTermalModifications.clear();
        gCTermalModifications.clear();
         // new PeptideModification(FastaAminoAcid.C, CYSTEIN_MODIFICATION_MASS,
        //        PeptideModificationRestriction.Global, true);
        if(isHardCodeModifications())
            guaranteeHardCodedModifications();
    }


    /**
     * turm a modification string into modifications
     * 15.994915@M,8.014199@K,10.008269@R
     *
     * @param value !null string
     * @return !null array
     */
    public static PeptideModification[] parsePeptideModifications(String value) {
        return parsePeptideModifications(value, PeptideModificationRestriction.Global, false);
    }

    /**
     * turm a modification string into modifications
     * 15.994915@M,8.014199@K,10.008269@R
     *
     * @param value !null string
     * @return !null array
     */
    public static PeptideModification[] parsePeptideModifications(String value, PeptideModificationRestriction restriction, boolean fixed) {
        value = value.trim();
        if (value.length() == 0)
            return EMPTY_ARRAY;
        String[] items = value.split(",");
        PeptideModification[] ret = new PeptideModification[items.length];
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            ret[i] = new PeptideModification(items[i], restriction, fixed);
        }
        Arrays.sort(ret);
        return ret;
    }


    private final FastaAminoAcid m_AminoAcid;
    private final double m_MassChange;
    private final double m_PeptideMass;
    private final boolean m_Fixed;
    private final PeptideModificationRestriction m_Restriction;

    private PeptideModification(String value) {
        this(value, PeptideModificationRestriction.Global, false);
    }

    private PeptideModification(String value, PeptideModificationRestriction restriction, boolean fixed) {
        if (value.endsWith(PeptideModificationRestriction.NTERMINAL_STRING)) {
            restriction = PeptideModificationRestriction.NTerminal;
            value = value.substring(0, value.length() - 1);
        }
        if (value.endsWith(PeptideModificationRestriction.CTERMINAL_STRING)) {
            restriction = PeptideModificationRestriction.CTerminal;
            value = value.substring(0, value.length() - 1);
        }
        String[] items = value.split("@");
        if (items.length > 1) {
            try {
                m_AminoAcid = FastaAminoAcid.valueOf(items[1]);
            }
            catch (IllegalArgumentException e) {
                throw new RuntimeException(e);

            }
        }
        else {
            if (restriction != PeptideModificationRestriction.NTerminal &&
                    restriction != PeptideModificationRestriction.CTerminal)
                throw new IllegalStateException("Any Animo Acid can only apply to terminal modifications ");
            m_AminoAcid = null;
        }
        m_MassChange = Double.parseDouble(items[0]);
        m_Restriction = restriction;
        m_Fixed = fixed;
        m_PeptideMass = computePeptideMass();
    }

    private PeptideModification(final FastaAminoAcid pAminoAcid, final double pMassChange) {
        this(pAminoAcid, pMassChange, PeptideModificationRestriction.Global, false);
    }

    private PeptideModification(final FastaAminoAcid pAminoAcid, final double pMassChange,
                                PeptideModificationRestriction restriction, boolean fixed) {
        m_AminoAcid = pAminoAcid;
        m_MassChange = pMassChange;
        m_Restriction = restriction;
        switch (m_Restriction) {
            case CTerminal:
                if (pAminoAcid == null)
                    XTandemUtilities.insertIntoArrayMap(gCTermalModifications, FastaAminoAcid.X, this);   // any
                else
                    XTandemUtilities.insertIntoArrayMap(gCTermalModifications, pAminoAcid, this);
                break;
            case NTerminal:
                  if (pAminoAcid == null)
                     XTandemUtilities.insertIntoArrayMap(gNTermalModifications, FastaAminoAcid.X, this);   // any
                 else
                     XTandemUtilities.insertIntoArrayMap(gNTermalModifications, pAminoAcid, this);
                break;
            default:
                break;
        }
        m_Fixed = fixed;
        m_PeptideMass = computePeptideMass();
    }


    public boolean isApplicable(IPolypeptide pep)
    {
        FastaAminoAcid aminoAcid = getAminoAcid();
        switch(getRestriction())  {
            case CTerminal:
                if(aminoAcid != null)
                    return pep.getCTerminal() == aminoAcid;
                 else
                    return true; // always
            case NTerminal:
                if(aminoAcid != null)
                    return pep.getNTerminal() == aminoAcid;
                 else
                    return true; // always
            default:
                return pep.hasUnmodifiedAminoAcid(aminoAcid);
        }
    }
    /**
     * if true this is always applied
     *
     * @return
     */
    public boolean isFixed() {
        return m_Fixed;
    }

    public String applyTo() {
        FastaAminoAcid aminoAcid = getAminoAcid();
        if(aminoAcid == null)
            return "";
        return aminoAcid.toString();
    }


    /**
     * null says any
     *
     * @return
     */
    public FastaAminoAcid getAminoAcid() {
        return m_AminoAcid;
    }

    public double getMassChange() {
        return m_MassChange;
    }

    public double getPepideMass() {
         return m_PeptideMass;
    }

    private double computePeptideMass() {
        FastaAminoAcid aminoAcid = getAminoAcid();

        String abbreviation;
        if(aminoAcid != null)  {
            abbreviation= aminoAcid.toString();
        }
        else {
            switch(m_Restriction)  {
                case CTerminal:
                    abbreviation = PeptideModificationRestriction.CTERMINAL_STRING;
                    break;
                case NTerminal:
                     abbreviation = PeptideModificationRestriction.NTERMINAL_STRING;
                     break;
                 default:
                     throw new UnsupportedOperationException("Any "); // ToDo
            }
        }
        char aa = abbreviation.charAt(0);
        MassCalculator defaultCalculator = MassCalculator.getDefaultCalculator();
        double aminoAcidMass = defaultCalculator.getAminoAcidMass(aa);
        double massChange = getMassChange();
        if(aa == 'C' && Math.abs(57 - massChange) < 1)  {
            aminoAcidMass -= CYSTEIN_MODIFICATION_MASS;
        }

        double ret = massChange + aminoAcidMass;
        return ret;
    }

    public PeptideModificationRestriction getRestriction() {
        return m_Restriction;
    }

    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The <code>toString</code> method for class <code>Object</code>
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `<code>@</code>', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        FastaAminoAcid aminoAcid = getAminoAcid();
        String value = "";
        if (aminoAcid != null)
            value = aminoAcid.toString();
        return Double.toString(getMassStepChange()) + "@" + value + getRestriction().getRestrictionString();
    }

    public double getMassStepChange() {
        int value = (int) (1000 * (getMassChange() + 0.0005));
        return value / 1000.0;
    }

    @Override
    public int compareTo(final PeptideModification o) {
        if (this == o)
            return 0;
        FastaAminoAcid aminoAcid = o.getAminoAcid();
        if (getAminoAcid() != null) {
            if(aminoAcid == null)
                return -1;
            int ret = getAminoAcid().compareTo(aminoAcid);
            if (ret != 0)
                return ret;

        }
        else {
            if (aminoAcid != null)
                return 1;
        }
        PeptideModificationRestriction r1 = getRestriction();
        PeptideModificationRestriction r2 = o.getRestriction();
        if(r1 != r2)
            return r1.compareTo(r2) ;
          int ms1 = getRoundedMassChange();
        int ms2 = o.getRoundedMassChange();
        if (ms1 == ms2)
            return 0;
        return ms1 < ms2 ? -1 : 1;
    }

    protected int getRoundedMassChange()
    {
        return (int)( 500 * (getMassChange() + 0.5));
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final PeptideModification that = (PeptideModification) o;
         return compareTo(that)== 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = m_AminoAcid != null ? m_AminoAcid.hashCode() : 0;
        if(getRestriction() != null)
             result |= getRestriction().hashCode();
        temp = (long)getRoundedMassChange();
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public void serializeAsString(IXMLAppender ap) {
        ap.openTag("modification");
        ap.appendAttribute("aminoacid", getAminoAcid());
        ap.appendAttribute("massChange", String.format("%10.4f", getMassChange()).trim());
        ap.appendAttribute("restriction", getRestriction());
        ap.closeTag("modification");
    }


}
