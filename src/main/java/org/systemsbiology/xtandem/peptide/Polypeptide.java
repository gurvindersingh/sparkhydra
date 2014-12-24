package org.systemsbiology.xtandem.peptide;

import org.systemsbiology.xtandem.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.peptide.Polypeptide
 * User: steven
 * Date: Jan 10, 2011
 */
public class Polypeptide implements IPolypeptide, Comparable<IPolypeptide> {
    public static final Polypeptide[] EMPTY_ARRAY = {};

    public static final Random RND = new Random();

    /**
     * fot testing only
     * @return  maka a random peptide
     */
    public static IPolypeptide randomPeptide()
    {
        int length = 6 + RND.nextInt(20) ;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <  length; i++) {
            sb.append(FastaAminoAcid.randomAminoAcid().toString());

        }
        return new Polypeptide(sb.toString()) ;
    }

    public static final Comparator<IPolypeptide> SEQUENCE_COMPARATOR = new SequenceComparator();

    /**
     * reverse the sequence - used in creating Decoys
     * @param p  !null polypeptide
     * @return
     */
    public static String getReversedSequence(IPolypeptide p) {
        StringBuilder sb = new StringBuilder();
        String sequence = p.getSequence();
        for (int i = sequence.length(); i > 0; i--)
            sb.append(sequence.substring(i - 1, i));
        return sb.toString();
    }


    /**
     * make a decoy by reversing a non-decoy polypaptide
     * @param p
     * @return
     */
    public static IPolypeptide asDecoy(IPolypeptide p) {
        if (p.isDecoy())
            return p;
        return new DecoyPolyPeptide(p);

    }

    /**
     * make protein positions for decoy peptides
     * @param pps normal peptide positions
     * @return array of one position marked as a decoy
     */
    public static IProteinPosition[] asDecoyPositions(IPolypeptide pp,IProteinPosition[] pps)
    {
        if(pps == null  )
              return null;
        if( pps.length == 0)
              return pps;
          IProteinPosition[] ret = {  ProteinPosition.asDecoy(pp,(ProteinPosition)pps[0]) };
        return ret;
    }

    /**
     * used
     */
    private static class DecoyPolyPeptide extends Polypeptide implements IDecoyPeptide {
        private DecoyPolyPeptide(IPolypeptide pp) {
            super(getReversedSequence(pp));
            IProteinPosition[] pps = pp.getProteinPositions();
            if(pps != null )
                setContainedInProteins(asDecoyPositions(this, pps));
        }
        private DecoyPolyPeptide(String sequence) {
             super(sequence,0);
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
            return new Polypeptide(getReversedSequence(this));
        }
    }

    public static class SequenceComparator implements Comparator<IPolypeptide> {
        private SequenceComparator() {
        }

        @Override
        public int compare(final IPolypeptide o1, final IPolypeptide o2) {
            int ret = o1.getSequence().compareTo(o2.getSequence());
            if (ret != 0) return ret;
            return ((Comparable<IPolypeptide>) o1).compareTo(o2);
        }
    }

    public static final Comparator<IPolypeptide> STRING_COMPARATOR = new StringComparator();

    public static class StringComparator implements Comparator<IPolypeptide> {
        private StringComparator() {
        }

        @Override
        public int compare(final IPolypeptide o1, final IPolypeptide o2) {
            int ret = o1.toString().compareTo(o2.toString());
            return ret;
        }
    }

    // ===================================
    // Really Important
    //====================================
    /**
     * create a peptide from a sequebnce string
     *  liek STGHKKED or     ST[18.5]GHKK[15.6]ED
     *  the later is a modified peptide
     * @param s
     * @return
     */
    public static Polypeptide fromString(String s) {
        s = s.trim();
        // has modifications so make a modified peptide
        if (s.contains("["))
            return ModifiedPolypeptide.fromModifiedString(s);
        // mot modified - make from sequence
        Polypeptide ret = new Polypeptide(s);
        ret.setMissedCleavages(PeptideBondDigester.getDefaultDigester().probableNumberMissedCleavages(ret));
        return ret;
    }

    /**
     * this is useful hwen reading deecoys from XML
     * @param sequence !null sequence - this is already reversed
     * @return
     */
    public static IPolypeptide asAlreadyDecoy(String s) {
        s = s.trim();
        if (s.contains("["))
            return ModifiedPolypeptide.asAlreadyDecoy(s);
        else
            return new DecoyPolyPeptide(s);
     }


    private double m_Mass;
    private double m_MatchingMass;
    private String m_Sequence;
    private int m_SequenceLength;
    private double m_RetentionTime;
    private PeptideValidity m_Validity;
    //    private IProtein m_ParentProtein;   // not final since proteins need to set later
    //   private   int m_StartPosition = -1;
    private int m_MissedCleavages;
    private IProteinPosition[] m_ContainedInProteins;

    protected Polypeptide() {
    }

    public Polypeptide(String pSequence,/* int startPos, IProtein m_ParentStream, */int missedCleavages) {
        m_Sequence = pSequence;
        m_SequenceLength = m_Sequence.length();
        m_MissedCleavages = missedCleavages;
    }

    public Polypeptide(String pSequence) {
        this(pSequence, findMissedCleavages(pSequence));
    }

    public static int findMissedCleavages(String pSequence) {
        int length = pSequence.length();
        if (length < 2)
            return 0;
        String test = pSequence.substring(0, length - 1); // drop last position
        int index = test.indexOf("K");
        while (index > -1) {
            if (pSequence.charAt(index + 1) != 'P')
                return 1; // missed
            index = test.indexOf("K", index + 1);
        }
        index = test.indexOf("R");
        while (index > -1) {
            if (pSequence.charAt(index + 1) != 'P')
                return 1; // missed
            index = test.indexOf("R", index + 1);
        }
        return 0;
    }


    /**
     * !null validity may be unknown
     *
     * @return
     */
    public PeptideValidity getValidity() {
        if (m_Validity == null)
            return PeptideValidity.Unknown;
        return m_Validity;
    }

    public void setValidity(final PeptideValidity pValidity) {
        m_Validity = pValidity;
    }

    /**
     * return a list of contained proteins
     *
     * @return !null array
     */
    @Override
    public IProteinPosition[] getProteinPositions() {
        if (m_ContainedInProteins == null)
            return IProteinPosition.EMPTY_ARRAY;
        return m_ContainedInProteins;
    }

    public void setContainedInProteins(final IProteinPosition[] pContainedInProteins) {
        Set<String> stx = new HashSet<String>();
        List<IProteinPosition> holder = new ArrayList<IProteinPosition>();
        for (int i = 0; i < pContainedInProteins.length; i++) {
            IProteinPosition tst = pContainedInProteins[i];
            if(tst == null)
                continue;
            String protein = tst.getProtein();
            if(protein == null)
                continue;
            if (!stx.contains(protein)) {
                stx.add(protein);
                holder.add(tst);
            }
            else {
                continue;
            }
        }
        IProteinPosition[] ret = new IProteinPosition[holder.size()];
        holder.toArray(ret);
        m_ContainedInProteins = ret;
    }

    /**
     * true if there is at least one modification
     *
     * @return
     */
    @Override
    public boolean isModified() {
        return false;
    }

    public double getRetentionTime() {
        return m_RetentionTime;
    }

    public void setRetentionTime(final double pRetentionTime) {
        m_RetentionTime = pRetentionTime;
    }

    /**
     * count the occurrance of an amino acid in the sequence
     *
     * @param aa !null amino acid
     * @return count of presence
     */
    public int getAminoAcidCount(FastaAminoAcid aa) {
        return getAminoAcidCount(aa.toString());
    }

    /**
     * count the occurrance of an amino acid in the sequence
     *
     * @param aa !null amino acid
     * @return count of presence
     */
    @Override
    public boolean hasAminoAcid(FastaAminoAcid aa) {
        return getSequence().contains(aa.toString());
    }

    /**
     * return the N Terminal amino acid
     *
     * @return
     */
    @Override
    public FastaAminoAcid getNTerminal() {
        int sequenceLength = getSequenceLength();
        if(sequenceLength == 0)
            return null;
        String aas = getSequence().substring(1) ;
         return FastaAminoAcid.valueOf(aas);
    }

    /**
     * return the C Terminal amino acid
     *
     * @return
     */
    @Override
    public FastaAminoAcid getCTerminal() {
        int sequenceLength = getSequenceLength();
        if(sequenceLength == 0)
            return null;
         String aas = getSequence().substring(sequenceLength -1, sequenceLength) ;
        return FastaAminoAcid.valueOf(aas);
    }

    /**
     * count the occurrance of an unmodified amino acid in the sequence
     *
     * @param aa !null amino acid
     * @return count of presence
     */
    @Override
    public boolean hasUnmodifiedAminoAcid(FastaAminoAcid aa) {
        return hasAminoAcid(aa);
    }


    /**
     * count the occurrance of an amino acid in the sequence
     *
     * @param aa !null amino acid  letter
     * @return count of presence
     */
    public int getAminoAcidCount(String aa) {
        String s = getSequence();
        int count = 0;
        int start = 0;
        int index = -1;
        while ((index = s.indexOf(aa, start)) > -1) {
            count++;
            if (index >= s.length() - 1)
                break;
            start = index + 1;
        }
        return count;
    }


    @Override
    public boolean isProtein() {
        return false;
    }

    /**
     * true is the polypaptide is known to be a decoy
     *
     * @return
     */
    @Override
    public boolean isDecoy() {
        return false;
    }

    @Override
    public IPolypeptide asDecoy() {
        return new DecoyPolyPeptide(this);
    }


    /**
     * true if the peptide is SewmiTryptic but may
     * miss instance where K or R is followed by aP which
     * are semitryptic
     *
     * @return
     */
    public boolean isProbablySemiTryptic() {
        String sequence = getSequence();
        char c = sequence.charAt(sequence.length() - 1);
        switch (c) {
            case 'r':
            case 'R':
            case 'k':
            case 'K':
                return false; // tryptic unless followed by a P
            default:
                return true;
        }
    }


    @Override
    public String getId() {
        return getSequence();
        //     return getParentProtein().getId() + KEY_SEPARATOR  + getStartPosition()  + "("  + getSequenceLength() + ")";
    }

//    public int getStartPosition()
//    {
//        return m_StartPosition;
//    }

    /**
     * return the number of missed cleavages
     *
     * @return as above
     */
    @Override
    public int getMissedCleavages() {
        return m_MissedCleavages;
    }

    public void setMass(final double pMass) {
        m_Mass = pMass;
    }

    public void setMatchingMass(final double pMatchingMass) {
        m_MatchingMass = pMatchingMass;
    }

    public void setMissedCleavages(final int pMissedCleavages) {
        m_MissedCleavages = pMissedCleavages;
    }

    public double getMass() {
        if (m_Mass == 0) {
            MassCalculator calculator = MassCalculator.getDefaultCalculator();

            String sequence = getSequence();
            double mass = calculator.getSequenceMass(this);
            setMass(mass);
        }
        return m_Mass;
    }

    public void setSequence(final String pSequence) {
        if (m_Sequence != null) {
            if (m_Sequence.equals(pSequence))
                return;
            throw new IllegalStateException("cannot reset pSequence");
        }
        m_Sequence = pSequence;
        m_SequenceLength = m_Sequence.length();
        int missedCleavages = 0;
        for (int i = 0; i < m_Sequence.length() - 1; i++) {
            char c = m_Sequence.charAt(i);
            if (c == 'R' || c == 'K') {
                if ('P' != m_Sequence.charAt(i))
                    missedCleavages++;
            }
        }
        setMissedCleavages(missedCleavages);
    }

//    public void setParentProtein(final IProtein pParentProtein) {
//        if(m_ParentProtein != null )  {
//            if(m_ParentProtein == pParentProtein)
//                return;
//            throw new IllegalStateException("cannot reset ParentProtein ");
//        }
//        m_ParentProtein = pParentProtein;
//    }
//
//    public void setStartPosition(final int pStartPosition) {
//        if(m_StartPosition != -1 )  {
//            if(m_StartPosition == pStartPosition)
//                return;
//            throw new IllegalStateException("cannot reset m_StartPosition ");
//        }
//        m_StartPosition = pStartPosition;
//    }
//
//    public void setMissedCleavages(final int pMissedCleavages) {
//        m_MissedCleavages = pMissedCleavages;
//    }

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(IPolypeptide test) {
        if (test == this)
            return true;

        if (!getSequence().equals(test.getSequence()))
            return false;
        if (!getId().equals(test.getId()))
            return false;

        return true;
    }

    /**
     * mass used to see if scoring rowks
     *
     * @return
     */
    public double getMatchingMass() {
        if (m_MatchingMass == 0) {
            double paptideMass = getMass();
            double mass = XTandemUtilities.calculateMatchingMass(paptideMass);
            setMatchingMass(mass);
        }
        return m_MatchingMass;
    }


    /**
     * check fo r common errors like * in AA seqience
     *
     * @return
     */
    @Override
    public boolean isValid() {
        final String s = getSequence();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case 'A': // ("alanine"),
                case 'B': // ("aspartate or asparagine"),
                case 'C': // ("cystine"),
                case 'D': // ("aspartate"),
                case 'E': // ("glutamate"),
                case 'F': // ("phenylalanine"),
                case 'G': // ("glycine"),
                case 'H': // ("histidine"),
                case 'I': // ("isoleucine"),
                case 'K': // ("lysine"),
                case 'L': // ("leucine"),
                case 'M': // ("methionine"),
                case 'N': // ("asparagine"),
                case 'P': // ("proline"),
                case 'Q': // ("glutamine"),
                case 'R': // ("arginine"),
                case 'S': // ("serine"),
                case 'T': // ("threonine"),
                case 'V': // ("valine"),
                case 'W': // ("tryptophan"),
                case 'Y': // ("tyrosine"),
                case 'Z': // ("glutamate or glutamine"),
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    /**
     * check for ambiguous peptides like *
     *
     * @return
     */
    @Override
    public boolean isUnambiguous() {
        final String s = getSequence();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case 'A': // ("alanine"),
                case 'C': // ("cystine"),
                case 'D': // ("aspartate"),
                case 'E': // ("glutamate"),
                case 'F': // ("phenylalanine"),
                case 'G': // ("glycine"),
                case 'H': // ("histidine"),
                case 'I': // ("isoleucine"),
                case 'K': // ("lysine"),
                case 'L': // ("leucine"),
                case 'M': // ("methionine"),
                case 'N': // ("asparagine"),
                case 'P': // ("proline"),
                case 'Q': // ("glutamine"),
                case 'R': // ("arginine"),
                case 'S': // ("serine"),
                case 'T': // ("threonine"),
                case 'V': // ("valine"),
                case 'W': // ("tryptophan"),
                case 'Y': // ("tyrosine"),
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    public static boolean isCharacterUnambiguous(Character c) {
        switch (Character.toUpperCase(c)) {
            case 'A': // ("alanine"),
            case 'C': // ("cystine"),
            case 'D': // ("aspartate"),
            case 'E': // ("glutamate"),
            case 'F': // ("phenylalanine"),
            case 'G': // ("glycine"),
            case 'H': // ("histidine"),
            case 'I': // ("isoleucine"),
            case 'K': // ("lysine"),
            case 'L': // ("leucine"),
            case 'M': // ("methionine"),
            case 'N': // ("asparagine"),
            case 'P': // ("proline"),
            case 'Q': // ("glutamine"),
            case 'R': // ("arginine"),
            case 'S': // ("serine"),
            case 'T': // ("threonine"),
            case 'V': // ("valine"),
            case 'W': // ("tryptophan"),
            case 'Y': // ("tyrosine"),
                return true;
            default:
                return false;
        }

    }

//    /**
//     * return the protein we are part of
//     *
//     * @return
//     */
//
//    public IProtein getParentProtein()
//    {
//        return m_ParentProtein;
//    }

    /**
     * return the length of the sequence
     *
     * @return !null String
     */
    @Override
    public int getSequenceLength() {
        return m_SequenceLength;
    }

    /**
     * return the sequence as a set of characters
     *
     * @return !null String
     */
    @Override
    public String getSequence() {
        return m_Sequence;
    }


    @Override
    public IPolypeptide getUnModified() {
        return this;
    }


//    /**
//     * return the sequence before the start of length maxLength or less
//     *
//     * @return !null String
//     */
//    @Override
//    public String getPreSequence(int maxLength)
//    {
//        int start = getStartPosition();
//        if(start == 0)
//            return "";
//        final IProtein prot = getParentProtein();
//        int begin = Math.max(0,start - maxLength);
//        return prot.getSequence().substring(begin,start);
//    }
//
//    /**
//     * return the sequence after the end of length maxLength or less
//     *
//     * @return !null String
//     */
//    @Override
//    public String getPostSequence(int maxLength)
//    {
//        int end = getStartPosition() + getSequenceLength();
//          final IProtein prot = getParentProtein();
//          int psEnd = Math.min(prot.getSequenceLength(),end + maxLength);
//          return prot.getSequence().substring(end,psEnd);
//      }

    /**
     * return the number of bionds in the sequence
     *
     * @return as above
     */
    @Override
    public int getNumberPeptideBonds() {
        return m_Sequence.length() - 1;
    }

    /**
     * return the amino acids as chars on the N and C sides of the bond
     *
     * @param bond
     * @return
     */
    @Override
    public char[] getBondPeptideChars(int bond) {
        char[] ret = new char[2];
        ret[0] = m_Sequence.charAt(bond);
        ret[1] = m_Sequence.charAt(bond + 1);
        return ret;
    }


    @Override
    public int compareTo(final IPolypeptide o) {
        if (this == o)
            return 0;
        int ret = getId().compareTo(o.getId());
        if (ret == 0)
            ret = getSequence().compareTo(o.getSequence());
        return ret;
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
    public IPolypeptide[] cleave(int bond) throws IndexOutOfBoundsException {
        IPolypeptide[] ret = new Polypeptide[2];

        ret[0] = new Polypeptide(m_Sequence.substring(0, bond + 1),
                /* getStartPosition(), getParentProtein()  , */0);
        ret[1] = new Polypeptide(m_Sequence.substring(bond + 1),
                /* getStartPosition(), getParentProtein()  , */0);
        IProteinPosition[] proteinPositions = this.getProteinPositions();

        if (proteinPositions == null)
            XTandemUtilities.breakHere();

        // add proteins and before and after AAN
        ((Polypeptide) ret[0]).setContainedInProteins(ProteinPosition.buildPeptidePositions(ret[0], 0, proteinPositions));
        ((Polypeptide) ret[1]).setContainedInProteins(ProteinPosition.buildPeptidePositions(ret[1], bond + 1, proteinPositions));
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
    public IPolypeptide fragment(int start, int length) throws IndexOutOfBoundsException {

        return new Polypeptide(m_Sequence.substring(start, start + length),
                /* start, getParentProtein(),  */
                0);

    }

    /**
     * build a polypeptide by putting the two peptides together
     *
     * @param added !null added sequence
     * @return !null merged peptide
     */
    @Override
    public IPolypeptide concat(final IPolypeptide added) {
        String merged = getSequence() + added.getSequence();
        Polypeptide ret = new Polypeptide(merged,
                /*  getStartPosition(), getParentProtein(),   */
                getMissedCleavages() + 1);
        ret.setContainedInProteins(ProteinPosition.mergePeptidePositions(ret, getProteinPositions(), added.getProteinPositions()));
        return ret;
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
        return new Polypeptide(m_Sequence.substring(start, end),
                /*  getStartPosition() + start,
               getParentProtein(), */
                getMissedCleavages());
    }


    /**
     * get the number of modified peptides
     *
     * @return
     */
    public int getNumberModifications() {
        return 0;
    }


    @Override
    public String toString() {
        return getSequence();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof Polypeptide)) return false;

        final Polypeptide that = (Polypeptide) o;

//        if (m_StartPosition != that.m_StartPosition) return false;
//        if (m_ParentProtein != null ? !m_ParentProtein.equals(that.m_ParentProtein) : that.m_ParentProtein != null)
//            return false;
        if (m_Sequence != null ? !m_Sequence.equals(that.m_Sequence) : that.m_Sequence != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = m_Sequence != null ? m_Sequence.hashCode() : 0;
//        result = 31 * result + (m_ParentProtein != null ? m_ParentProtein.hashCode() : 0);
//        result = 31 * result + m_StartPosition;
        return result;
    }
}
