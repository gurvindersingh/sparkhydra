package org.systemsbiology.xtandem.peptide;

import org.systemsbiology.xtandem.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.peptide.PeptideBondDigester
 * User: steven
 * Date: Jan 10, 2011
 */
public class PeptideBondDigester extends PeptideDigester {
    public static final PeptideBondDigester[] EMPTY_ARRAY = {};

    public static final int MAX_NUMBER_MISSED_CLEAVAGES = 5;
    public static final int NUMBER_LETTERS = 26;

    public static final PeptideBondDigester TRYPSIN  = new Trypsin();
    public static final PeptideBondDigester TRYPSIN1  = new Trypsin(1);
    public static final PeptideBondDigester TRYPSIN2  = new Trypsin(2);

    private static IPeptideDigester gDefaultDigester = TRYPSIN;

    public static IPeptideDigester getDefaultDigester() {
        return gDefaultDigester;
    }

    public static void setDefaultDigester(final IPeptideDigester pDefaultDigester) {
        gDefaultDigester = pDefaultDigester;
    }

    public static final String TRYPSIN_LOGIC = "[RK]|{P}";

    // adding LysC Specificity: Serine protease that specifically hydrolyzes amide, ester, and peptide bonds at the carboxylic side of Lys
    public static final String LYSC_LOGIC = "[K]";

    private int m_NumberMissedCleavages;
    private boolean m_SemiTryptic;
    private String m_LogicString;
    /**
     * only public way to get a digester
     *
     * @param logic
     * @return !null digester
     */
    public static PeptideBondDigester getDigester(String logic) {
        if ("trypsin".equalsIgnoreCase(logic)
            //            || "[KR]|{P}".equalsIgnoreCase(logic)
            //           ||  "[RK]|{P}".equalsIgnoreCase(logic)
                )

            return new Trypsin();
        if ("lysC".equalsIgnoreCase(logic)
            //            || "[KR]|{P}".equalsIgnoreCase(logic)
            //           ||  "[RK]|{P}".equalsIgnoreCase(logic)
                )

            return new LysineC();
        if (TRYPSIN_LOGIC.equalsIgnoreCase(logic))
           return new Trypsin();
        if (LYSC_LOGIC.equalsIgnoreCase(logic)) {
            LysineC lysineC = new LysineC();
            setDefaultDigester(lysineC);
            return lysineC;
        }

        PeptideBondDigester ret = new PeptideBondDigester();
        ret.setLogic(logic);
        return ret;
    }

    /**
     * allow sequences where only one end is at the boundary
     *
     * @return
     */
    @Override
    public boolean isSemiTryptic() {
        return m_SemiTryptic;
    }

    /**
     * allow sequences where only one end is at the boundary
     *
     * @return
     */
    @Override
    public void setSemiTryptic(final boolean isSo) {
        m_SemiTryptic = isSo;
    }

    public int getNumberMissedCleavages() {
        return m_NumberMissedCleavages;
    }

    /**
     * according to the rules return the number of missed cleavages seen - may miss a few because
     * of terminal conditions
     *
     * @param in !null peptide
     * @return as above
     */
    @Override
    public int probableNumberMissedCleavages(IPolypeptide in) {
         int missed = 0;
        for (int i = 0; i < in.getNumberPeptideBonds(); i++) {
            char[] aminos = in.getBondPeptideChars(i);
            if (canCleave(aminos[0], aminos[1])) {
                missed++;
            }
        }
        return missed;
    }

    public void setNumberMissedCleavages(final int pNumberMissedCleavages) {
        if (pNumberMissedCleavages < 0)
            throw new IllegalArgumentException("NumberMissedCleavages must be positive");
        if (pNumberMissedCleavages > MAX_NUMBER_MISSED_CLEAVAGES)
            throw new UnsupportedOperationException("Only 0-" + MAX_NUMBER_MISSED_CLEAVAGES + " missed cleavage supported");

        m_NumberMissedCleavages = pNumberMissedCleavages;
    }

    protected static int asIndex(char c) {
        int i = Character.toUpperCase(c) - 'A';
        if (i < 0 || i >= NUMBER_LETTERS)
            throw new IllegalArgumentException("must ba a letter");
        return i;
    }

    protected PeptideBondDigester() {
    }


    @Override
    public String toString() {
        return m_LogicString;
    }

    private BitSet m_Logic = new BitSet(NUMBER_LETTERS * NUMBER_LETTERS);

    public void setLogic(String logic) {
        m_LogicString = logic;
        String[] items = logic.split("\\|");
        String positive = items[0].replace("[", "").replace("]", "");
        String negative = "";
        if (items.length > 1)
            negative = items[1].replace("{", "").replace("}", "");
        for (int i = 0; i < positive.length(); i++) {
            int index = asIndex(positive.charAt(i));
            for (int j = 0; j < NUMBER_LETTERS; j++) {
                int bitIndex = NUMBER_LETTERS * index + j;
                m_Logic.set(bitIndex, true);  // cleave here

            }
        }
        for (int i = 0; i < negative.length(); i++) {
            int index = asIndex(negative.charAt(i));
            for (int j = 0; j < NUMBER_LETTERS; j++) {
                int bitIndex = NUMBER_LETTERS * j + index;
                m_Logic.set(bitIndex, false);  // do not cleave here

            }
        }
    }

    @Override
    public IPolypeptide[] digest(final IPolypeptide in, final Object... addedData) {
        IPolypeptide[] pps = digestWithoutFilter( in,  addedData) ;
         return filterIgnoredPeptides(pps);
    }

    public static IPolypeptide[] filterIgnoredPeptides(IPolypeptide[] in) {
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (int i = 0; i < in.length; i++) {
            IPolypeptide pp = in[i];
            if (XTandemUtilities.ignorePeptide(pp))
                continue;
            holder.add(pp);
        }
        IPolypeptide[] ret = new IPolypeptide[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * add peptides assuming that cleavages are missed
     *
     * @param noSemi
     * @return
     */
    @Override
    public IPolypeptide[] addSemiCleavages(final IPolypeptide in, final Object... addedData) {
        if (!isSemiTryptic())
            throw new IllegalStateException("should not be here");
        IPolypeptide[] noSemi = digestWithoutFilter(in, addedData);
        Set<IPolypeptide> allpolys = new HashSet<IPolypeptide>(Arrays.asList(noSemi));
        List<IPolypeptide> local = new ArrayList<IPolypeptide>(Arrays.asList(noSemi));

        IPolypeptide[] current = local.toArray(IPolypeptide.EMPTY_ARRAY);
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (int i = 0; i < current.length ; i++) {
            IPolypeptide pp = noSemi[i ];

            // a little debug code
            String seq = pp.getSequence();

//            if (seq.contains("AIQFLEI" ))
//                 XTandemUtilities.breakHere();
//            if (seq.contains("YREERNADSGLC" ))
//                 XTandemUtilities.breakHere();

             addSemiCleavages(allpolys, holder, pp);

        }
        allpolys.addAll(holder);
        local = holder;

        IPolypeptide[] ret = allpolys.toArray(IPolypeptide.EMPTY_ARRAY);
        ret = filterIgnoredPeptides(ret);
        Arrays.sort(ret, Polypeptide.SEQUENCE_COMPARATOR);
        return ret;
    }

    protected IPolypeptide[] digestWithoutFilter(final IPolypeptide in, final Object[] addedData) {
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
         IPolypeptide[] tryptic = digestFragment(holder, in, 1, addedData);
         int numberMissedCleavages = getNumberMissedCleavages();
         if (numberMissedCleavages == 0)
             return filterIgnoredPeptides(tryptic);
         IPolypeptide[] pps = addMissedCleavages(tryptic, numberMissedCleavages);
        return pps;
    }

    protected void addSemiCleavages(final Set<IPolypeptide> pAllpolys, final List<IPolypeptide> holder, final IPolypeptide pp) {
        Set<String> handled = new HashSet<String>();
        for (int i = 0; i < pp.getSequenceLength() - 1; i++) {
             IPolypeptide[] frags = pp.cleave(i);
             maybeAddAndRecord(holder, handled, frags );
              }
        // peal from the other end
        for (int i = pp.getSequenceLength() - 3; i > 3; i--) {
             IPolypeptide[] frags = pp.cleave(i);
            maybeAddAndRecord(holder, handled, frags );
         }
     }

    protected void maybeAddAndRecord(final List<IPolypeptide> holder, final Set<String> pHandled, final IPolypeptide[] pFrag) {
        for (int i = 0; i < pFrag.length; i++) {
            IPolypeptide frag = pFrag[i];
            maybeAddAndRecord(holder, pHandled, frag );

        }
    }


    protected void maybeAddAndRecord(final List<IPolypeptide> holder, final Set<String> pHandled, final IPolypeptide pFrag) {
        if (!XTandemUtilities.ignorePeptide(pFrag))     {
             if(!pHandled.contains(pFrag.getSequence()))  {
                 pHandled.add(pFrag.getSequence());
                holder.add(pFrag);
             }
             else {
                 // do I care about repeats
                 XTandemUtilities.breakHere();
             }
    //         if(frags[0].getSequence().contains("AGLNRPAGISFGVLT"))
    //             XTandemUtilities.breakHere();
         }
    }

    /**
     * add peptides assuming that cleavages are missed
     *
     * @param noMissed
     * @return
     */
    protected IPolypeptide[] addMissedCleavages(final IPolypeptide[] noMissed, int numberMissed) {
        Set<IPolypeptide> allpolys = new HashSet<IPolypeptide>(Arrays.asList(noMissed));
        // build missed cleavages
        for (int i = 0; i < numberMissed; i++) {
             addMissedCleavagesAtNumber(allpolys,noMissed, i + 1);
         }

        IPolypeptide[] ret = allpolys.toArray(IPolypeptide.EMPTY_ARRAY);
        Arrays.sort(ret, Polypeptide.SEQUENCE_COMPARATOR);
        return ret;
    }


    /**
     * add peptides assuming that cleavages are missed
     *
     * @param noMissed
     * @return
     */
    protected void addMissedCleavagesAtNumber(Set<IPolypeptide> allpolys,final IPolypeptide[] noMissed, int numberMissed) {
         // build missed cleavages = only 1 supported
        int offset = numberMissed;
         for (int i = 0; i < noMissed.length - offset; i++) {
            IPolypeptide missedCleavage = noMissed[i];
            for (int j = i + 1; j <= i + numberMissed; j++) {
                missedCleavage = missedCleavage.concat(noMissed[j]);

            }

            // debug stuff
//            String seq = missedCleavage.getSequence();
//            if (seq.contains("HAFYQSANVPAGLLDYQHR")) //"LLAGVAGGTAATAAANRLV".equals(seq))
//                XTandemUtilities.breakHere();

            allpolys.add(missedCleavage);

        }

     }


    protected IPolypeptide digestFragment1(List<IPolypeptide> holder, final IPolypeptide in, int level,
                                           final Object... addedData) {
        int sequenceLength = in.getNumberPeptideBonds();
        if (sequenceLength < 1) {
            holder.add(in);
            return null;  // done
        }
//        if (sequenceLength > 4000)
//            XTandemUtilities.breakHere();
//        if (level > 150)
//            XTandemUtilities.breakHere();

        for (int i = 0; i < in.getNumberPeptideBonds(); i++) {
            char[] aminos = in.getBondPeptideChars(i);
            if (canCleave(aminos[0], aminos[1])) {
                IPolypeptide[] polypeptides = in.cleave(i);
                if (polypeptides[0].isValid())
                    holder.add(polypeptides[0]);

//                // I have no odea why I need to add a one lower peptide
//                if (i > 1 && XTandemUtilities.isDoneForUnknownReasaon()) {
//                    IPolypeptide[] polypeptidesM1 = in.cleave(i - 1);
//                    final IPolypeptide pp1 = polypeptidesM1[0];
//                    String seq = pp1.getSequence();
//                    if (seq.startsWith("LLAGVAGGTAATA")) //"LLAGVAGGTAATAAANRLV".equals(seq))
//                        XTandemUtilities.breakHere();
//                    holder.add(pp1);
//                }
                return polypeptides[1];
            }
        }
        // nothing to digest so add this
        if (in.isValid())
            holder.add(in);
        return null;
    }


    protected IPolypeptide[] digestFragment(List<IPolypeptide> holder, final IPolypeptide in, int level,
                                            final Object... addedData) {
        int sequenceLength = in.getNumberPeptideBonds();

        IPolypeptide remainder = digestFragment1(holder, in, level, addedData);
        while (remainder != null) {
            remainder = digestFragment1(holder, remainder, level, addedData);
        }
        IPolypeptide[] ret = new IPolypeptide[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    protected IPolypeptide[] digestFragmenX(List<IPolypeptide> holder, final IPolypeptide in, int level,
                                            final Object... addedData) {
        int sequenceLength = in.getNumberPeptideBonds();

        for (int i = 0; i < in.getNumberPeptideBonds(); i++) {
            char[] aminos = in.getBondPeptideChars(i);
            if (canCleave(aminos[0], aminos[1])) {
                IPolypeptide[] polypeptides = in.cleave(i);
                holder.add(polypeptides[0]);

//                // I have no odea why I need to add a one lower peptide
//                if (i > 1 && XTandemUtilities.isDoneForUnknownReasaon()) {
//                    IPolypeptide[] polypeptidesM1 = in.cleave(i - 1);
//                    final IPolypeptide pp1 = polypeptidesM1[0];
//                    String seq = pp1.getSequence();
//                    if (seq.startsWith("LLAGVAGGTAATA")) //"LLAGVAGGTAATAAANRLV".equals(seq))
//                        XTandemUtilities.breakHere();
//                    holder.add(pp1);
//                }
                return digestFragment(holder, polypeptides[1], level + 1, addedData);
            }
        }
        holder.add(in);   // nothing to cleave so add this
        IPolypeptide[] ret = new IPolypeptide[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    /**
     * if true this digester can cleave the bond
     *
     * @param nTerminal nterminal AA as char
     * @param cTerminal cterminal AA as char
     * @return true of the bond can be cleaved
     */
    protected boolean canCleave(char nTerminal, char cTerminal) {
        if (!Polypeptide.isCharacterUnambiguous(nTerminal))
            return false;
        if (!Polypeptide.isCharacterUnambiguous(cTerminal))
            return false;
        int bitIndex = asIndex(nTerminal) * NUMBER_LETTERS + asIndex(cTerminal);
        return m_Logic.get(bitIndex);
    }

    /**
     * for speed and because it is the most common case the logic for trypsin is
     * hard coded
     */
    public static class Trypsin extends PeptideBondDigester {

        public Trypsin() {
            setLogic("[RK]|{P}");
        }

        public Trypsin(int numberMissed) {
            this();
            setNumberMissedCleavages(numberMissed);
        }

        @Override
        protected boolean canCleave(final char nTerminal, final char cterminal) {
            if (cterminal == 'P')
                return false;
            switch (nTerminal) {
                case 'R':
                    return true;
                case 'K':
                    return true;
                default:
                    return false;
            }
        }

    }

    /**
     * for speed and because it is the most common case the logic for trypsin is
     * hard coded
     */
    public static class LysineC extends PeptideBondDigester {

        public LysineC() {
            setLogic("[K]");
        }

        public LysineC(int numberMissed) {
            this();
            setNumberMissedCleavages(numberMissed);
        }

        @Override
        protected boolean canCleave(final char nTerminal, final char cterminal) {
              switch (nTerminal) {
                 case 'K':
                    return true;
                default:
                    return false;
            }
        }

    }
}
