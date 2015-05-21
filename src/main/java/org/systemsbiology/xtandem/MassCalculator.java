package org.systemsbiology.xtandem;

import org.systemsbiology.xtandem.peptide.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.MassCalculator
 *
 * @author Steve Lewis
 *         Mass calculator class
 *         Calculates molecular masses based on atomic masses.
 *         Atomic masses come from http://www.unimod.org/unimod_help.html.
 */

public class MassCalculator implements Serializable {


    private static MassType gDefaultMassType = MassType.monoisotopic;


    private static final MassCalculator[] INSTANCES = new MassCalculator[MassType.values().length];
    public static final PeptideModification CYSTEIN_MONO = PeptideModification.fromString("57.021464@C", PeptideModificationRestriction.Global, true);  // these are fixed modifications
    public static final PeptideModification CYSTEIN_AVERAGE = PeptideModification.fromString("57.0513@C", PeptideModificationRestriction.Global, true); // these are fixed modifications

    public static MassType getDefaultMassType() {
        return gDefaultMassType;
    }

    public static void setDefaultMassType(final MassType pDefaultMassType) {
        if (pDefaultMassType == gDefaultMassType)
            return;
        gDefaultMassType = pDefaultMassType;
        PeptideModification.resetHardCodedModifications();
    }


    public static MassCalculator getCalculator(MassType _t) {
        switch (_t) {
            case monoisotopic:
                if (INSTANCES[0] == null)
                    INSTANCES[0] = new MassCalculator(MassType.monoisotopic);
                return INSTANCES[0];
            case average:
                if (INSTANCES[1] == null)
                    INSTANCES[1] = new MassCalculator(MassType.average);
                return INSTANCES[1];

        }
        throw new UnsupportedOperationException("Never get Here");
    }

    public static MassCalculator getDefaultCalculator() {
        return getCalculator(getDefaultMassType());
    }


    private final MassType m_massType;
    private final Map<String, MassPair> m_masses =
            new HashMap<String, MassPair>();
    private double m_pdAaMass[] = new double[127];
    private List<PeptideModification> m_PermanentModifications = new ArrayList<PeptideModification>();


    public static class MassPair implements Serializable {
        public final double monoisotopic;
        public final double average;

        public MassPair(double mono, double avg) {
            monoisotopic = mono;
            average = avg;
        }


    }

     // monoisotipic , average
    private MassCalculator(MassType _t) {
        m_massType = _t;
        addMass("H", 1.007825035, 1.00794);
        addMass("O", 15.99491463, 15.9994);
        addMass("N", 14.003074, 14.0067);
        addMass("C", 12.0, 12.0107);
        addMass("S", 31.9720707, 32.065);
        addMass("P", 30.973762, 30.973761);
        setAminoAcidMasses(_t);
        checkAminoAcidMasses();
        switch (_t) {
            case average:
                addPermanentModifications(CYSTEIN_AVERAGE);
                break;
            case monoisotopic:
                addPermanentModifications(CYSTEIN_MONO);
                break;
        }
    }


    /**
     * add a mass modification which is always applied
     *
     * @param added
     */
    protected void addPermanentModifications(PeptideModification added) {
        if (m_PermanentModifications.contains(added))
            return;
        m_PermanentModifications.add(added);
        // this might be a BIG Bud SLewis
        /*
        char c = added.getAminoAcid().toString().charAt(0);
        int index1 = Character.toUpperCase(c);
        int index2 = Character.toLowerCase(c);
        double newMass = m_pdAaMass[index1] + added.getMassChange();
        m_pdAaMass[index1] = m_pdAaMass[index2] = newMass;
        */
    }


    private void setAminoAcidMasses(MassType type) {

        double[] pdValue = m_pdAaMass;

        if (type == MassType.monoisotopic) {
            pdValue['a'] = pdValue['A'] = calcMass("C3H5ON");

            pdValue['b'] = pdValue['B'] = calcMass("C4H6O2N2");    // Same as N

            pdValue['c'] = pdValue['C'] = calcMass("C3H5ONS");

            pdValue['d'] = pdValue['D'] = calcMass("C4H5O3N");

            pdValue['e'] = pdValue['E'] = calcMass("C5H7O3N");

            pdValue['f'] = pdValue['F'] = calcMass("C9H9ON");

            pdValue['g'] = pdValue['G'] = calcMass("C2H3ON");

            pdValue['h'] = pdValue['H'] = calcMass("C6H7ON3");

            pdValue['i'] = pdValue['I'] = calcMass("C6H11ON");

            pdValue['j'] = pdValue['J'] = 0.0;

            pdValue['k'] = pdValue['K'] = calcMass("C6H12ON2");  //  HO2CCH(NH2)(CH2)4NH2

            pdValue['l'] = pdValue['L'] = calcMass("C6H11ON");

            pdValue['m'] = pdValue['M'] = calcMass("C5H9ONS");

            pdValue['n'] = pdValue['N'] = calcMass("C4H6O2N2");

            pdValue['o'] = pdValue['O'] = calcMass("C4H6O2N2");    // Same as N

            pdValue['p'] = pdValue['P'] = calcMass("C5H7ON");

            pdValue['q'] = pdValue['Q'] = calcMass("C5H8O2N2");

            pdValue['r'] = pdValue['R'] = calcMass("C6H12ON4");

            pdValue['s'] = pdValue['S'] = calcMass("C3H5O2N");

            pdValue['t'] = pdValue['T'] = calcMass("C4H7O2N");

            pdValue['u'] = pdValue['U'] = 150.953640;    // Why?

            pdValue['v'] = pdValue['V'] = calcMass("C5H9ON");

            pdValue['w'] = pdValue['W'] = calcMass("C11H10ON2");

            pdValue['x'] = pdValue['X'] = 111.060000;    // Why?

            pdValue['y'] = pdValue['Y'] = calcMass("C9H9O2N");

            pdValue['z'] = pdValue['Z'] = calcMass("C5H8O2N2");    // Same as Q

        }
        else {
            /*
            * unfortunately, the average masses for amino acids do not
            * seem to be straight sums of the average masses for the atoms
            * they contain.
            *
            * instead of using the mass calculator, these numbers are taken
            * as finalants from the web page referenced above.
            */
            pdValue['a'] = pdValue['A'] = 71.0788;

            pdValue['b'] = pdValue['B'] = 114.1038;    // Same as N

            pdValue['c'] = pdValue['C'] = 103.1388;

            pdValue['d'] = pdValue['D'] = 115.0886;

            pdValue['e'] = pdValue['E'] = 129.1155;

            pdValue['f'] = pdValue['F'] = 147.1766;

            pdValue['g'] = pdValue['G'] = 57.0519;

            pdValue['h'] = pdValue['H'] = 137.1411;

            pdValue['i'] = pdValue['I'] = 113.1594;

            pdValue['j'] = pdValue['J'] = 0.0;

            pdValue['k'] = pdValue['K'] = 128.1741;

            pdValue['l'] = pdValue['L'] = 113.1594;

            pdValue['m'] = pdValue['M'] = 131.1926;

            pdValue['n'] = pdValue['N'] = 114.1038;

            pdValue['o'] = pdValue['O'] = 114.1038;    // Same as N

            pdValue['p'] = pdValue['P'] = 97.1167;

            pdValue['q'] = pdValue['Q'] = 128.1307;

            pdValue['r'] = pdValue['R'] = 156.1875;

            pdValue['s'] = pdValue['S'] = 87.0782;

            pdValue['t'] = pdValue['T'] = 101.1051;

            pdValue['u'] = pdValue['U'] = 0.0;    // Why?

            pdValue['v'] = pdValue['V'] = 99.1326;

            pdValue['w'] = pdValue['W'] = 186.2132;

            pdValue['x'] = pdValue['X'] = 113.1594;    // Why?

            pdValue['y'] = pdValue['Y'] = 163.1760;

            pdValue['z'] = pdValue['Z'] = 128.1307;    // Same as Q
        }

    }

    private void checkAminoAcidMasses() {

        for (int i = 0; i < m_pdAaMass.length; i++) {
            checkAminoAcidMasses(m_pdAaMass[i], i);

        }
    }

    public static final double TOO_BIG_DIFFERENCE = 0.2;

    private void checkAminoAcidMasses(double using, int test) {

        switch ((char) test) {
            case 'A':
            case 'a':
                if (Math.abs(using - 71.0788) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'B':
            case 'b':
                if (Math.abs(using - 114.1038) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'C':
            case 'c':
                if (Math.abs(using - 103.1388) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'D':
            case 'd':
                if (Math.abs(using - 115.0886) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'E':
            case 'e':
                if (Math.abs(using - 129.1155) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'F':
            case 'f':
                if (Math.abs(using - 147.1766) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'G':
            case 'g':
                if (Math.abs(using - 57.0519) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'H':
            case 'h':
                if (Math.abs(using - 137.1411) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'I':
            case 'i':
                if (Math.abs(using - 113.1594) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'K':
            case 'k':
                if (Math.abs(using - 128.1741) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'L':
            case 'l':
                if (Math.abs(using - 113.1594) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'M':
            case 'm':
                if (Math.abs(using - 131.1926) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'N':
            case 'n':
                if (Math.abs(using - 114.1038) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'O':
            case 'o':
                if (Math.abs(using - 114.1038) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'P':
            case 'p':
                if (Math.abs(using - 97.1167) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'Q':
            case 'q':
                if (Math.abs(using - 128.1307) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'R':
            case 'r':
                if (Math.abs(using - 156.1875) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'S':
            case 's':
                if (Math.abs(using - 87.0782) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'T':
            case 't':
                if (Math.abs(using - 101.1051) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'V':
            case 'v':
                if (Math.abs(using - 99.1326) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'W':
            case 'w':
                if (Math.abs(using - 186.2132) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'Y':
            case 'y':
                if (Math.abs(using - 163.1760) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
                break;
            case 'Z':
            case 'z':
                if (Math.abs(using - 128.1307) > TOO_BIG_DIFFERENCE)
                    throw new IllegalStateException("Bad mass " + (char) test);
            default:
                break;
        }
    }


    /**
     * return the mass of a polypeptide
     *
     * @param pp
     * @return
     */
    public double getSequenceMass(IPolypeptide pp) {
        String sequence = pp.getSequence();
        return getSequenceMass(sequence);
    }

    /**
     * return the mass of a polypeptide
     *
     * @param pp
     * @return
     */
    public double getSequenceMass(String pp) {
        if (pp.length() == 0)
            return 0;
        String seq = pp.toUpperCase();
        double ret = 0;
        int length = seq.length();
        for (int i = 0; i < length; i++) {
            char c = seq.charAt(i);
            double aminoAcidMass = getAminoAcidMass(c);
            //      XTandemUtilities.mayBeShowAddedMassX(  ret,  aminoAcidMass," " + c);
            ret += aminoAcidMass;
            // handle hard coded C and N terminal modifications
            if (i == 0) {
                FastaAminoAcid NTerminal = FastaAminoAcid.fromChar(c);
                PeptideModification[] nmods = PeptideModification.getNTerminalModifications(NTerminal);
                for (int j = 0; j < nmods.length; j++) {
                    PeptideModification nmod = nmods[j];
                    if (nmod.isFixed()) {
                        double massChange = nmod.getMassChange();
                        //                 XTandemUtilities.mayBeShowAddedMassX(  ret,  massChange," " + nmod);
                        ret += massChange;
                    }
                }

            }
            if (i == length - 1) {
                FastaAminoAcid CTerminal = FastaAminoAcid.fromChar(c);
                PeptideModification[] nmods = PeptideModification.getCTerminalModifications(CTerminal);
                for (int j = 0; j < nmods.length; j++) {
                    PeptideModification nmod = nmods[j];
                    if (nmod.isFixed()) {
                        double massChange = nmod.getMassChange();
                        //         XTandemUtilities.mayBeShowAddedMassX(  ret,  massChange," " + nmod);
                        ret += massChange;
                    }
                }

            }
        }

        // XTandem hard codes some terminal modifications

        return ret;
    }


    /**
     * return the mass of an amino acid
     *
     * @param aa
     * @return
     */
    public double getAminoAcidMass(char aa) {
        double mas = m_pdAaMass[aa & 0x7F];
        // we already handle the fixed cystein change

        switch (aa) {
            case 'C':
            case 'c':
                mas += PeptideModification.CYSTEIN_MODIFICATION_MASS; // hard code cystein shift
                break;
            default:
                break;

        }
        return mas;
    }

    /**
     * take a string like "C9H9ON" "C2H3ON" "C6H7ON3" and find the mass
     *
     * @return the mass
     * @param_m
     */
    public double calcMass(String s) {
        if (s.length() == 0)
            return 0;
        double totalMass = 0.0;

        String atom = null;
        int number = 0;
        int i = 0;
        while (i < s.length()) {
            if (atom == null) {
                atom = s.substring(i, i + 1);
                number = 0;
                i++;
                continue;

            }
            final char c = s.charAt(i);
            if (Character.isDigit(c)) {
                number *= 10;
                number += c - '0';  // parse as an int
                i++;
            }
            else { // must be am atom
                final double mass = getMass(atom);
                if (number == 0)
                    number = 1;
                totalMass += number * mass;
                number = 0;
                atom = s.substring(i, i + 1);
                i++;

            }
        }
        double mass = getMass(atom);
        if (number == 0)
            number = 1;
        totalMass += number * mass;
        return totalMass;
    }

    //    throw new UnsupportedOperationException("Fix This"); // ToDo
//        String pchAtom = _m;
//        String pchCount = pchAtom;
//
//        string atom;
//        int count;
//
//        while (*pchCount != '\0')
//        {
//            // Advance past atom name
//            pchCount++;
//            while (isalpha( * pchCount)&&!isupper( * pchCount))
//            pchCount++;
//
//            // Get count, 1 if not present
//            count = 1;
//            if (isdigit( * pchCount))
//            count = atoi(pchCount);
//
//            // Add atomic mass * count
//            atom.assign(pchAtom, pchCount - pchAtom);
//            totalMass += getMass(atom.data()) * count;
//
//            // Advance past count, if there is one
//            while (*pchCount != '\0' && !isalpha( * pchCount))
//            pchCount++;

    //           pchAtom = pchCount;
//        }
//
//        return totalMass;
//    }

    /**
     * @param _m
     * @return
     */
    public double getMass(String _m) {
        final MassPair pair = m_masses.get(_m);
        if (pair == null)
            return 0.0;

        if (m_massType == MassType.monoisotopic)
            return pair.monoisotopic;

        return pair.average;
    }

    public void addMass(String _m, double _mono, double _ave) {
        m_masses.put(_m, new MassPair(_mono, _ave));
    }

    /**
     * return the calculator's masstype - monoisotopic or average
     *
     * @return as above
     */
    public MassType getMassType() {
        return m_massType;
    }
}
