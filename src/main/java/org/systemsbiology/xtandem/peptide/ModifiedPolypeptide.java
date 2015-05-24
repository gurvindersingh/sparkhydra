package org.systemsbiology.xtandem.peptide;

import com.lordjoe.utilities.*;
import org.systemsbiology.xtandem.*;

import java.util.*;

/**
 * Backs up a simpler peptide
 * org.systemsbiology.xtandem.peptide.ModifiedPolypeptide
 * User: steven
 * Date: 6/30/11
 */
public class ModifiedPolypeptide extends Polypeptide implements IModifiedPeptide {

    public static final String MAX_MODIFICASTIONS_PARAMETER_NAME = "org.systemsbiology.jxtandem.ModifiedPolypeptide.MaxPeptideModifications";
    public static final int DEFAULT_MAX_MODIFICATIONS = 3;
    private static int gMaxPeptideModifications = DEFAULT_MAX_MODIFICATIONS;

    public static final Random RND = new Random();

    public static IModifiedPeptide randomModifiedPeptide() {
        IPolypeptide pp = Polypeptide.randomPeptide();
        final String sequence = pp.getSequence();
        PeptideModification[] mods = new PeptideModification[sequence.length()];
        int nods = 1 + RND.nextInt(2);
        for (int i = 0; i < nods; i++) {
            int index = RND.nextInt(mods.length);
            mods[index] = PeptideModification.randomModification();

        }
        ModifiedPolypeptide ret = new ModifiedPolypeptide(sequence, 0, mods);
        ret.setMissedCleavages(PeptideBondDigester.getDefaultDigester().probableNumberMissedCleavages(ret));
        return ret;
    }

    /**
     * this is useful hwen reading deecoys from XML
     *
     * @param sequence !null sequence - this is already reversed and may be modified
     * @return
     */
    public static IPolypeptide asAlreadyDecoy(String sequence) {
        IPolypeptide pp = fromModifiedString(sequence);
        if (pp instanceof IModifiedPeptide) {
            ModifiedDecoyPolyPeptide ret = new ModifiedDecoyPolyPeptide(pp.getSequence(), 0, ((IModifiedPeptide) pp).getModifications());

            return ret;

        }
        else
            return pp;
    }


    /**
     * used  to create decoys
     */
    private static class ModifiedDecoyPolyPeptide extends ModifiedPolypeptide implements IDecoyPeptide {
        private ModifiedDecoyPolyPeptide(String pSequence, int missedCleavages, PeptideModification[] mods) {
            super(pSequence, missedCleavages, mods);
        }

        private ModifiedDecoyPolyPeptide(ModifiedPolypeptide pp) {
            this(getReversedSequence(pp), pp.getMissedCleavages(), Util.invert(pp.getModifications()));
            setContainedInProteins(asDecoyPositions(this, pp.getProteinPositions()));

        }


        @Override
        public IPolypeptide asDecoy() {
            return this;
        }

        @Override
        public boolean isDecoy() {
            return true;
        }


        @Override
        public String toString() {
            return "DECOY_" + super.toString();
        }

        @Override
        public IPolypeptide asNonDecoy() {
            return new ModifiedPolypeptide(getReversedSequence(this), this.getMissedCleavages(), Util.invert(this.getModifications()));
        }


    }


    public static int getMaxPeptideModifications() {
        return gMaxPeptideModifications;
    }

    public static void setMaxPeptideModifications(int maxPeptideModifications) {
        gMaxPeptideModifications = maxPeptideModifications;
    }

    /**
     * take a string like AK[]GHTE
     *
     * @param s !null string
     * @return !null peptide
     */
    public static ModifiedPolypeptide fromModifiedString(String s) {
        return fromModifiedString(s, 0);
    }

    /**
     * take a string like AK[81.456]GHTE
     *
     * @param s !null string
     * @return !null peptide
     */
    public static ModifiedPolypeptide fromModifiedString(String s, int missed_cleavages) {
        String unmods = buildUnmodifiedSequence(s);
        PeptideModification[] mods = new PeptideModification[unmods.length()];
        int charNumber = -1;
        // handle n terminal mod like n[40]AGHT...
        if (s.startsWith("n[")) { // n terminal mod
            int index = s.indexOf("]", 0);
            String modText = s.substring(2, index);
            s = s.substring(index + 1);
            String aa = s.substring(0, 1);
            PeptideModification mod = PeptideModification.fromString(modText + "@" + aa, PeptideModificationRestriction.NTerminal, false);
            mods[0] = mod;
        }
        String lastChar = "";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '[') {
                int index = s.indexOf("]", i);
                String modText = s.substring(i + 1, index);
                double offest = Double.parseDouble(modText);
                PeptideModification mod = PeptideModification.fromString(modText + "@" + lastChar, PeptideModificationRestriction.Global, false);
                if (charNumber >= mods.length)
                    XTandemUtilities.breakHere();
                mods[charNumber] = mod;
                i = index;
            }
            else {
                lastChar = Character.toString(c);
                charNumber++;
            }

        }
        ModifiedPolypeptide ret = new ModifiedPolypeptide(unmods, missed_cleavages, mods);
        ret.setMissedCleavages(PeptideBondDigester.getDefaultDigester().probableNumberMissedCleavages(ret));

        return ret;
    }

    /**
     * drop modifications
     *
     * @param s
     * @return
     */
    public static String buildUnmodifiedSequence(String s) {
        if (s.startsWith("n")) {    // drop n terminal modifications noke n[40]
            int end = s.indexOf("]");
            if (end == -1)
                throw new IllegalArgumentException("Bad n terminal modification");
            s = s.substring(end + 1);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '[':
                case ']':
                case '-':
                case '.':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * add any modifications at the C and N terminals
     *
     * @param peptide
     * @return
     */
    public static PeptideModification[] buildTerminalModifications(IPolypeptide peptide) {
        String sequence = peptide.getSequence().toUpperCase();
        FastaAminoAcid NTerminal = FastaAminoAcid.valueOf(sequence.substring(0, 1));
        FastaAminoAcid CTerminal = FastaAminoAcid.valueOf(
                sequence.substring(sequence.length() - 1, sequence.length()));
        PeptideModification[] nmods = PeptideModification.getNTerminalModifications(NTerminal);
        PeptideModification[] cmods = PeptideModification.getCTerminalModifications(CTerminal);
        if (nmods.length == 0 && cmods.length == 0)
            return PeptideModification.EMPTY_ARRAY;
        List<PeptideModification> holder = new ArrayList<PeptideModification>();
        holder.addAll(Arrays.asList(nmods));
        holder.addAll(Arrays.asList(cmods));
        PeptideModification[] ret = new PeptideModification[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    public static IModifiedPeptide[] buildModifications(IPolypeptide peptide,
                                                        PeptideModification[] allmods) {

        PeptideModification[] tmods = ModifiedPolypeptide.buildTerminalModifications(peptide);
        if (allmods.length == 0 && tmods.length == 0)
            return IModifiedPeptide.EMPTY_ARRAY;

        Set<IPolypeptide> holder = new HashSet<IPolypeptide>();
        String sequence = peptide.getSequence();

        PeptideModification[] fixedmods = PeptideModification.selectFixedModifications(allmods);
        for (int i = 0; i < fixedmods.length; i++) {
            applyModification(peptide, fixedmods[i], holder, sequence);
        }
        for (int i = 0; i < tmods.length; i++) {
            applyTerminalModification(peptide, tmods[i], holder);
        }

        PeptideModification[] potantialModArray = PeptideModification.selectPotentialModifications(allmods);
        // sort out what we can use
        potantialModArray = findApplicableMods(peptide, potantialModArray);
        applyPotentialModifications(peptide, potantialModArray, holder, sequence);

//        Collection<PeptideModification> potentialMods = new HashSet<PeptideModification>(Arrays.asList(potantialModArray));
//        if (!potentialMods.isEmpty() )
//            potentialMods = findApplicableMods(peptide,potentialMods);
//            applyPotentialModifications(peptide, potentialMods, holder, sequence);
//

        IModifiedPeptide[] ret = new IModifiedPeptide[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    private static int totalAppliedModifications = 0;

    private static void applyPotentialModifications(IPolypeptide peptide, PeptideModification[] potantialModArray, Set<IPolypeptide> withPotentialMods, String sequence) {
        Set<IPolypeptide> newMods = new HashSet<IPolypeptide>();
        Set<IPolypeptide> currentMods = new HashSet<IPolypeptide>();
        currentMods.add(peptide);
        for (int modNum = 0; modNum < getMaxPeptideModifications(); modNum++) {
            for (int i = 0; i < potantialModArray.length; i++) {
                PeptideModification potentialMod = potantialModArray[i];
                totalAppliedModifications++;
                for (IPolypeptide pm : currentMods) {
                    applyModification(pm, potentialMod, newMods, sequence);
                }

            }
            withPotentialMods.addAll(newMods);
            currentMods.clear();
            currentMods.addAll(newMods);
            newMods.clear();
        }

    }

    private static PeptideModification[] findApplicableMods(IPolypeptide peptide, PeptideModification[] potentialMods) {
        Set<PeptideModification> holder = new HashSet<PeptideModification>();
        for (PeptideModification pm : potentialMods) {
            if (pm.isApplicable(peptide))
                holder.add(pm);
        }
        return holder.toArray(PeptideModification.EMPTY_ARRAY);
    }


    private static Collection<PeptideModification> findApplicableMods(IPolypeptide peptide, Collection<PeptideModification> potentialMods) {
        Set<PeptideModification> holder = new HashSet<PeptideModification>();
        for (PeptideModification pm : potentialMods) {
            if (pm.isApplicable(peptide))
                holder.add(pm);
        }
        return holder;
    }

//    /**
//     * this starts applying potential mods to a peptide
//     * @param peptide
//     * @param potentialMods
//     * @param holder
//     * @param sequence
//     */
//    protected static void applyPotentialModifications(IPolypeptide peptide,  Collection<PeptideModification> potentialMods, Set<IModifiedPeptide> holder, String sequence) {
//        Set<IModifiedPeptide> withPotentialMods = new HashSet<IModifiedPeptide>();
//        applyPotentialModifications(peptide, sequence, potentialMods, withPotentialMods);
//        holder.addAll(withPotentialMods);
//
//    }
//
//    /**
//     * @param index             index into potentialmods
//     * @param peptide           start peptide
//     * @param potentialMods     array of mods to apply
//     * @param withPotentialMods list of modified peptides
//     * @param sequence          original sequence
//     */
//    protected static void applyPotentialModifications(IPolypeptide peptide, String sequence, Collection<PeptideModification> potentialMods, Set<IModifiedPeptide> withPotentialMods) {
//        if(peptide.getNumberModifications() >= getMaxPeptideModifications())
//            return;
//        List<IModifiedPeptide> newMods = new ArrayList<IModifiedPeptide>();
//        int nMOds = potentialMods.size();
//        boolean modApplied = false;
//        int modsApplied = 0;
//        for (PeptideModification potentialMod : potentialMods) {
//            if(!potentialMod.isApplicable(peptide))
//                continue;
//             if(applyModification(peptide, potentialMod, newMods, sequence)) {
//                 withPotentialMods.addAll(newMods);
//                 Set<PeptideModification> others = new HashSet<PeptideModification>(potentialMods);
//                 others.remove(potentialMod);
//                 if(!others.isEmpty()) {
//                     for(IModifiedPeptide mp : newMods )   {
//                         if(mp.getNumberModifications() < getMaxPeptideModifications())
//                           applyPotentialModifications(mp, sequence, others, withPotentialMods);
//                     }
//                  }
//             }
//            modsApplied++;
//        }
//     }
//

    /**
     * this version will allow multiple modifications
     *
     * @param peptide input peptide
     * @param mod     !null array of modifications
     * @return
     */
    public static IModifiedPeptide[] buildAllModifications(IPolypeptide peptide,
                                                           PeptideModification[] mod) {

        PeptideModification[] tmods = ModifiedPolypeptide.buildTerminalModifications(peptide);
        if (mod.length == 0 && tmods.length == 0)
            return IModifiedPeptide.EMPTY_ARRAY;
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        String sequence = peptide.getSequence();

        for (int i = 0; i < mod.length; i++) {
            applyModification(peptide, mod[i], holder, sequence);
        }
        for (int i = 0; i < tmods.length; i++) {
            applyTerminalModification(peptide, tmods[i], holder);
        }

        // add more modifications
        for (int i = 0; i < mod.length; i++) {
            // modify modifies spectra
            IPolypeptide[] items = holder.toArray(IModifiedPeptide.EMPTY_ARRAY);
            for (int j = 0; j < items.length; j++) {
                IPolypeptide item = items[j];
                final String sequence1 = item.getSequence();
                applyModification(item, mod[i], holder, sequence1);
            }
        }
        for (int i = 0; i < tmods.length; i++) {
            // modify modifies spectra
            IPolypeptide[] items = holder.toArray(IModifiedPeptide.EMPTY_ARRAY);
            for (int j = 0; j < items.length; j++) {
                IPolypeptide item = items[j];
                applyModification(item, tmods[i], holder, item.getSequence());
            }
        }
        IModifiedPeptide[] ret = new IModifiedPeptide[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    protected static void applyTerminalModification(final IPolypeptide peptide,
                                                    final PeptideModification pPeptideModification,
                                                    final Collection<IPolypeptide> pHolder) {
        PeptideModification pm = pPeptideModification;
        String test = pm.applyTo();
        int start = 0;
        IModifiedPeptide e = null;
        switch (pm.getRestriction()) {
            case CTerminal:
                e = buildModification(peptide, pm, peptide.getSequence().length() - 1);
                break;
            case NTerminal:
                e = buildModification(peptide, pm, 0);
                break;
            default:
                throw new IllegalStateException("should only call for terminal modifications");
        }
        double mass = e.getMass(); // look here
        pHolder.add(e);
    }

    protected static boolean applyModification(final IPolypeptide peptide,
                                               final PeptideModification pPeptideModification,
                                               final Collection<IPolypeptide> pHolder,
                                               final String pSequence) {
        PeptideModification pm = pPeptideModification;
        PeptideModificationRestriction restriction = pm.getRestriction();
        int index;
        FastaAminoAcid aminoAcid = pm.getAminoAcid();
        switch (restriction) {
            case NTerminal:
                  index = 0;
                  if(aminoAcid == null) {
                      IModifiedPeptide e = buildModification(peptide, pm, index);
                       if (!pHolder.contains(e))
                           pHolder.add(e);
                      return true;
                  }
                else {
                      throw new UnsupportedOperationException("Fix This"); // ToDo
                  }
            case CTerminal:
                  index = pSequence.length() - 1;
                  if(aminoAcid == null) {
                      IModifiedPeptide e = buildModification(peptide, pm, index);
                       if (!pHolder.contains(e))
                           pHolder.add(e);
                      return true;
                  }
                else {
                      throw new UnsupportedOperationException("Fix This"); // ToDo
                  }
            case Global:
             default:
                String test = pm.applyTo();
                int start = 0;
                 index = pSequence.indexOf(test, start);
                if (index == -1)
                    return false; // no mods applied
                while (index > -1) {
                    IModifiedPeptide e = buildModification(peptide, pm, index);
                    if (!pHolder.contains(e))
                        pHolder.add(e);
                    if (index > pSequence.length() - 1)
                        break;
                    index = pSequence.indexOf(test, index + 1);
                }
                return true; // mods applied
        }
    }

    public static IModifiedPeptide buildModification(final IPolypeptide peptide,
                                                     final PeptideModification pm, final int index) {

        String sequence = peptide.getSequence();
        PeptideModification[] mods = null;
        if (peptide instanceof IModifiedPeptide) {
            IModifiedPeptide eptide = (IModifiedPeptide) peptide;
            mods = eptide.getModifications();
        }
        else {
            mods = new PeptideModification[sequence.length()];
        }
        if (mods[index] == pm)
            return (IModifiedPeptide) peptide;
        mods[index] = pm;
        ModifiedPolypeptide modifiedPolypeptide = new ModifiedPolypeptide(sequence, peptide.getMissedCleavages(), mods);
        modifiedPolypeptide.setContainedInProteins(peptide.getProteinPositions());

        // this way hadoop will not kill the job for lack of progress
       // ProgressManager.showProgress();
        return modifiedPolypeptide;

    }


    /**
     * routine to determine whether to return a PolyPeptide or a Modified Moplpeptide
     *
     * @param pSequence
     * @param missedCleavages
     * @param mods
     * @return
     */
    protected static IPolypeptide buildPolypeptide(final String pSequence,
                                                   final int missedCleavages,
                                                   PeptideModification[] mods) {
        if (pSequence.contains("."))
            XTandemUtilities.breakHere();

        for (int i = 0; i < mods.length; i++) {
            PeptideModification mod = mods[i];
            if (mod != null)
                return new ModifiedPolypeptide(pSequence, missedCleavages, mods);
        }
        return new Polypeptide(pSequence, missedCleavages);
    }

    // array of modifications of the SAME length as the sequence - most are usually null
    private final PeptideModification[] m_SequenceModifications;
    private final int m_NumberModifications;
    private String m_ModifiedString;
    private String m_ModificationString;

    public ModifiedPolypeptide(final String pSequence, final int missedCleavages,
                               PeptideModification[] mods) {
        super(pSequence, missedCleavages);
        if (mods.length != pSequence.length())
            throw new IllegalArgumentException("bad modifications array");
        PeptideModification[] modifications = new PeptideModification[mods.length];
        System.arraycopy(mods, 0, modifications, 0, mods.length);
        m_SequenceModifications = modifications;
        // count non-null modifications
        int nMods = 0;
        for (int i = 0; i < modifications.length; i++) {
            PeptideModification modification = modifications[i];
            if (modification != null)
                nMods++;
        }
        m_NumberModifications = nMods;
 /*
    If you are being careful stop peptides with too many mods
  */
        //       if (nMods > getMaxPeptideModifications())
        //          throw new IllegalStateException("too many modifications in a peptide");
    }

    /**
     * get the number of modified peptides
     *
     * @return
     */
    public int getNumberModifications() {
        return m_NumberModifications;
    }

    /**
     * deibbrately hide the manner a peptide is cleaved to
     * support the possibility of the sequence pointing to the protein as
     * Java substring does
     *
     * @param bond non-negative bond
     * @return !null array of polypeptides
     * @throws IndexOutOfBoundsException on bad bond
     */
    @Override
    public IPolypeptide[] cleave(final int bond) throws IndexOutOfBoundsException {

        IPolypeptide[] ret = new Polypeptide[2];
        String sequence = getSequence();
        int assumeNoMissedCleavages = 0;

        PeptideModification[] mods = getModificationsInInterval(0, bond + 1);
        ret[0] = buildPolypeptide(sequence.substring(0, bond + 1), assumeNoMissedCleavages, mods);
        mods = getModificationsInInterval(bond + 1);
        ret[1] = buildPolypeptide(sequence.substring(bond + 1), assumeNoMissedCleavages, mods);

        IProteinPosition[] proteinPositions = this.getProteinPositions();

        if (proteinPositions == null)
            XTandemUtilities.breakHere();

        // add proteins and before and after AAN
        ((Polypeptide) ret[0]).setContainedInProteins(ProteinPosition.buildPeptidePositions(ret[0], 0, proteinPositions));
        ((Polypeptide) ret[1]).setContainedInProteins(ProteinPosition.buildPeptidePositions(ret[1], bond + 1, proteinPositions));
        return ret;
    }

    /**
     * return true if we have the amino acid and it is not modified
     *
     * @param aa !null amino acid
     * @return as above
     */
    @Override
    public boolean hasUnmodifiedAminoAcid(FastaAminoAcid aacid) {
        if (!hasAminoAcid(aacid))
            return false;
        String aa = aacid.toString();
        String s = getSequence();
        int count = 0;
        int start = 0;
        int index = -1;
        while ((index = s.indexOf(aa, start)) > -1) {
            if (m_SequenceModifications[index] == null)
                return true;
            count++;
            if (index >= s.length() - 1)
                break;
            start = index + 1;
        }
        return false;
    }


    @Override
    public IPolypeptide asDecoy() {
        return new ModifiedDecoyPolyPeptide(this);
    }


    protected PeptideModification[] getModificationsInInterval(int start) {
        return getModificationsInInterval(start, getSequence().length());
    }

    protected PeptideModification[] getModificationsInInterval(int start, int end) {
        int length = end - start;
        PeptideModification[] ret = new PeptideModification[length];
        System.arraycopy(m_SequenceModifications, start, ret, 0, length);
        return ret;
    }

    /**
     * deibbrately hide the manner a peptide is cleaved to
     * support the possibility of the sequence pointing to the protein as
     * Java substring does - this is usually used to convert a polupeptide id to poplpeptide
     *
     * @param start  start value
     * @param length sequence length
     * @return !null polypeptide
     * @throws IndexOutOfBoundsException
     */
    @Override
    public IPolypeptide fragment(final int start, final int length) throws IndexOutOfBoundsException {
        PeptideModification[] mods = getModificationsInInterval(start, start + length);
        return buildPolypeptide(getSequence().substring(start, start + length), 0, mods);
    }

    /**
     * build a polypeptide by putting the two peptides together
     *
     * @param added !null added sequence
     * @return !null merged peptide
     */
    @Override
    public IPolypeptide concat(final IPolypeptide added) {
        throw new UnsupportedOperationException("This may never be used");
    }

    @Override
    public String toString() {
        return getModifiedSequence();
    }


    @Override
    public IPolypeptide getUnModified() {
        return new Polypeptide(getSequence());
    }

    /**
     * deibbrately hide the manner a peptide is cleaved to
     * support the possibility of the sequence pointing to the protein as
     * Java substring does
     *
     * @param bond non-negative bond
     * @return !null array of polypeptides
     * @throws IndexOutOfBoundsException on bad bond
     */
    @Override
    public IPolypeptide subsequence(final int start, final int end) throws IndexOutOfBoundsException {
        PeptideModification[] mods = getModificationsInInterval(start, end);
        return buildPolypeptide(getSequence().substring(start, end), 0, mods);
    }

    @Override
    public double getMass() {
        double massModification = getMassModification();
        double mass = super.getMass();
        return massModification + mass;
    }

    public double getMassModification() {
        double modifiedMass = 0;
        for (int i = 0; i < m_SequenceModifications.length; i++) {
            PeptideModification mod = m_SequenceModifications[i];
            if (mod != null)
                modifiedMass += mod.getMassChange();
        }
        return modifiedMass;
    }


    public String getModifiedSequence() {
        if (m_ModifiedString == null) {
            StringBuilder sb = new StringBuilder();
            String sequence = getSequence();
            for (int i = 0; i < sequence.length(); i++) {
                char c = sequence.charAt(i);
                sb.append(c);
                PeptideModification mod = m_SequenceModifications[i];
                if (mod != null)
                    sb.append("[" + mod.getMassStepChange() + "]");


            }
            m_ModifiedString = sb.toString();

        }
        return m_ModifiedString;
    }

    public String getTotalModifiedSequence() {
        StringBuilder sb = new StringBuilder();
        String sequence = getSequence();
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            sb.append(c);
            PeptideModification mod = m_SequenceModifications[i];
            if (mod != null)
                sb.append("[" + String.format("%10.3f", mod.getPepideMass()).trim() + "]");


        }
        return sb.toString();
    }


    /**
     * true if there is at least one modification
     *
     * @return
     */
    @Override
    public boolean isModified() {
        return true;
    }

    @Override
    public String getModificationString() {
        if (m_ModificationString == null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < m_SequenceModifications.length; i++) {
                PeptideModification mod = m_SequenceModifications[i];
                if (mod != null) {
                    if (sb.length() > 0)
                        sb.append(",");
                    sb.append(Integer.toString(i) + ":" + mod.toString());
                }
            }
            m_ModificationString = sb.toString();

        }
        return m_ModificationString;
    }

    /**
     * return applied modifications
     *
     * @return
     */
    @Override
    public PeptideModification[] getModifications() {
        PeptideModification[] ret = new PeptideModification[m_SequenceModifications.length];
        System.arraycopy(m_SequenceModifications, 0, ret, 0, m_SequenceModifications.length);
        return ret;
    }

    @Override
    public int compareTo(IPolypeptide o) {
        final int i = super.compareTo(o);
        if (i != 0)
            return i;
        if (o instanceof IModifiedPeptide) {
            IModifiedPeptide o1 = (IModifiedPeptide) o;
            return getModifiedSequence().compareTo(((IModifiedPeptide) o).getModificationString());
        }
        return -1;

    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final String me = toString();
        final String them = o.toString();
        final boolean equals = me.equals(them);
        if(!equals)
            return false; // isolate interesting cases

        return equals;
    }

    @Override
    public int hashCode() {
         return toString().hashCode();
    }
}
