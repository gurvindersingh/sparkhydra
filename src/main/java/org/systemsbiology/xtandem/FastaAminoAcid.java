package org.systemsbiology.xtandem;

import java.util.*;

/**
 * org.systemsbiology.xtandem.FastaAminoAcid
 *
 * @author Steve Lewis
 * @date Jan 7, 2011
 */
public enum FastaAminoAcid {
    A("alanine",41,9.69,6.00),
    B("aspartate or asparagine",-28,8.80,5.41),
    C("cystine",49,10.28,5.07),
    D("aspartate",-55,8.80,2.77),
    E("glutamate",-31,9.67,3.22),
    F("phenylalanine",100,9.13,5.48),
    G("glycine",0,9.60,5.97),
    H("histidine",8,9.17,7.59),
    I("isoleucine",99,9.60,6.02),
    K("lysine",-23,8.95,9.78),
    L("leucine",97,9.60,5.98),
    M("methionine",74,9.21,5.74),
    N("asparagine",-28,8.80,5.41),
    P("proline",-46,10.60,6.30),
    Q("glutamine",-10,9.13,5.65),
    R("arginine",-14,9.04,10.76),
    S("serine",-5,9.15,5.68),
    T("threonine",13,9.1,5.600),
    V("valine",76,9.62,5.96),
    W("tryptophan",97,9.39,5.89),
    Y("tyrosine",63,9.11,5.66),
    Z("glutamate or glutamine",-10,1,5.65),
    X("any",0,1,1),
    UNKNOWN("unknown",0,1,1)

    ;

    public static final Random RND = new Random();
    public static final FastaAminoAcid[] UNIQUE_AMINO_ACIDS = { A,C,D,E,F,G,H,I,K,L,M,N,P,Q,R,S,T,V,W,Y };

    /**
     * choose a random unique Amino acid - this is used primarily for testing
     * @return
     */
    public static FastaAminoAcid randomAminoAcid() {
        return FastaAminoAcid.UNIQUE_AMINO_ACIDS[RND.nextInt(FastaAminoAcid.UNIQUE_AMINO_ACIDS.length)];
    }


    public static final double MAX_HYDROPHOBICITY = 100;
    public static final double MIN_HYDROPHOBICITY = -46;

    public static final int hydroPhobicityRank(FastaAminoAcid fa,int scale)
    {
        double hydrophobicity = fa.getHydrophobicity();
        double  fraction = (hydrophobicity - MIN_HYDROPHOBICITY) / (MAX_HYDROPHOBICITY - MIN_HYDROPHOBICITY);
        return Math.max(0,Math.min(scale - 1,(int)(scale * fraction)));
    }
    /**
     * convert the first character of a string to an anminoacid - bad values are null
     *
     * @param in !null value
     * @return 0.. n - 1
     */
    public static FastaAminoAcid asAminoAcidOrNull(String s) {
        if (s == null)
            return null;
        if (s.trim().length() != 1)
            return null;
        char in = s.charAt(0);
        return asAminoAcidOrNull(in);
    }

    /**
     * convert the first character of a string to an anminoacid - bad values are null
     *
     * @param in !null value
     * @return 0.. n - 1
     */
    public static FastaAminoAcid[] asAminoAcids(String s) {
        List<FastaAminoAcid> holder = new ArrayList<FastaAminoAcid>();
        for(int i = 0; i < s.length(); i++) {
            FastaAminoAcid e = null;
            try {
                e = FastaAminoAcid.fromChar(s.charAt(i));
                holder.add(e);
            }
            catch (BadAminoAcidException e1) {
                holder.add(FastaAminoAcid.UNKNOWN);

            }
        }
        FastaAminoAcid[] ret = new FastaAminoAcid[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * convert the first character of a string to an anminoacid - bad values are null
     *
     * @param in a character
     * @return possibly null animoacid
     */
    public static FastaAminoAcid asAminoAcidOrNull(char in) {
        switch (Character.toUpperCase(in)) {
            case 'A':
                return A;
            case 'C':
                return C;
            case 'D': //("aspartate"),
                return D;
            case 'E':  //"glutamate"),
                return E;
            case 'F':  //"phenylalanine"),
                return F;
            case 'G':  //"glycine"),
                return G;
            case 'H':  //"histidine"),
                return H;
            case 'I':  //"isoleucine"),
                return I;
            case 'K':  //"lysine"),
                return K;
            case 'L':  //"leucine"),
                return L;
            case 'M':  //"methionine"),
                return M;
            case 'N':  //"asparagine"),
                return N;
            case 'P':  //"proline"),
                return P;
            case 'Q':  //"glutamine"),
                return Q;
            case 'R':  //"arginine"),
                return R;
            case 'S':  //"serine"),
                return S;
            case 'T':  //"threonine"),
                return T;
            case 'V':  //"valine"),
                return V;
            case 'W':  //"tryptophan"),
                return W;
            case 'Y':  //"tyrosine"),
                return Y;
            case 'Z':  //"glutamate or glutamine"),
                return Z;
            case 'B':  // "aspartate or asparagine"
                return B;
            default:
                return null;
        }

    }

    /**
     * convert to an index
     *
     * @param in !null value
     * @return 0.. n - 1
     */
    public static FastaAminoAcid fromAbbreviation(String in) {
        in = in.toUpperCase();
        char c = in.charAt(0);
        switch (c) {
            case 'A':
                if ("ALA".equalsIgnoreCase(in))
                    return A;
                if ("ARG".equalsIgnoreCase(in))
                    return R;
                if ("ASP".equalsIgnoreCase(in))
                     return D;
                if ("ASN".equalsIgnoreCase(in))
                     return N;
                return UNKNOWN;
              //  throw new IllegalArgumentException("Bad amino acid abbreviation " + in);
            case 'C':
                if ("CYS".equalsIgnoreCase(in))
                    return C;
                return UNKNOWN;
              //  throw new IllegalArgumentException("Bad amino acid abbreviation " + in);
             case 'G':  //"glycine"),
                if ("GLY".equalsIgnoreCase(in))
                    return G;
                if ("GLU".equalsIgnoreCase(in))
                    return E;
                if ("GLN".equalsIgnoreCase(in))
                    return Q;
                 return UNKNOWN;
               //  throw new IllegalArgumentException("Bad amino acid abbreviation " + in);
             case 'H':  //"histidine"),
                if ("HIS".equalsIgnoreCase(in))
                    return H;
                return null;
              //  throw new IllegalArgumentException("Bad amino acid abbreviation " + in);
             case 'I':  //"isoleucine"),
                 if ("ILE".equalsIgnoreCase(in))
                     return I;
                 if ("IPA".equalsIgnoreCase(in))
                      return UNKNOWN;
                 if ("IAA".equalsIgnoreCase(in))
                        return UNKNOWN;
                  return UNKNOWN;
             //    throw new IllegalArgumentException("Bad amino acid abbreviation " + in);
            case 'L':  //"leucine"),
                if ("LYS".equalsIgnoreCase(in))
                    return K;
                if ("LEU".equalsIgnoreCase(in))
                    return L;
                return UNKNOWN;
              //  throw new IllegalArgumentException("Bad amino acid abbreviation " + in);
            case 'M':  //"methionine"),
                if ("MET".equalsIgnoreCase(in))
                    return M;
                return UNKNOWN;
              //  throw new IllegalArgumentException("Bad amino acid abbreviation " + in);
             case 'P':  //"proline"),
                if ("PRO".equalsIgnoreCase(in))
                    return P;
                if ("PHE".equalsIgnoreCase(in))
                    return F;
                return UNKNOWN;
              //  throw new IllegalArgumentException("Bad amino acid abbreviation " + in);
            case 'S':  //"serine"),
                if ("SER".equalsIgnoreCase(in))
                    return S;
                return null;
              //  throw new IllegalArgumentException("Bad amino acid abbreviation " + in);
            case 'T':  //"threonine"),
                if ("THR".equalsIgnoreCase(in))
                    return T;
                if ("TRP".equalsIgnoreCase(in))
                    return W;
                if ("TYR".equalsIgnoreCase(in))
                    return Y;
                return null;
              //  throw new IllegalArgumentException("Bad amino acid abbreviation " + in);
            case 'V':  //"valine"),
                if ("VAL".equalsIgnoreCase(in))
                    return V;
                return UNKNOWN;
              //  throw new IllegalArgumentException("Bad amino acid abbreviation " + in);
            default:
                return UNKNOWN;

        }

    //    throw new IllegalStateException("No Amino acid for string " + in);
    }

    /**
     * convert to an index
     *
     * @param in !null value
     * @return 0.. n - 1
     */
    public static FastaAminoAcid fromChar(char in) throws BadAminoAcidException
    {
        switch (in) {
            case 'A':
                return A;
            case 'C':
                return C;
            case 'D': //("aspartate"),
                return D;
            case 'E':  //"glutamate"),
                return E;
            case 'F':  //"phenylalanine"),
                return F;
            case 'G':  //"glycine"),
                return G;
            case 'H':  //"histidine"),
                return H;
            case 'I':  //"isoleucine"),
                return I;
            case 'K':  //"lysine"),
                return K;
            case 'L':  //"leucine"),
                return L;
            case 'M':  //"methionine"),
                return M;
            case 'N':  //"asparagine"),
                return N;
            case 'P':  //"proline"),
                return P;
            case 'Q':  //"glutamine"),
                return Q;
            case 'R':  //"arginine"),
                return R;
            case 'S':  //"serine"),
                return S;
            case 'T':  //"threonine"),
                return T;
            case 'V':  //"valine"),
                return V;
            case 'W':  //"tryptophan"),
                return W;
            case 'Y':  //"tyrosine"),
                return Y;
            case 'Z':  //"glutamate or glutamine"),
                return Z;
            case 'B':  // "aspartate or asparagine"
                return B;
            case 'X':  // "aspartate or asparagine"
                return X;
            case 'U':  // "aspartate or asparagine"
                 return UNKNOWN;
        }
        if (!Character.isLetter(in))
            throw new BadAminoAcidException( in);

        if (!Character.isUpperCase(in))
            return fromChar(Character.toUpperCase(in));

        throw new BadAminoAcidException( in);
    }


    /**
     * if true in represents a unique amino acid
     *
     * @param in as above
     * @return as above
     */
    public static boolean representsUnigueAminoacid(char in) {
        switch (Character.toUpperCase(in)) {
            case 'A':
            case 'C':
            case 'D': //("aspartate"),
            case 'E':  //"glutamate"),
            case 'F':  //"phenylalanine"),
            case 'G':  //"glycine"),
            case 'H':  //"histidine"),
            case 'I':  //"isoleucine"),
            case 'K':  //"lysine"),
            case 'L':  //"leucine"),
            case 'M':  //"methionine"),
            case 'N':  //"asparagine"),
            case 'P':  //"proline"),
            case 'Q':  //"glutamine"),
            case 'R':  //"arginine"),
            case 'S':  //"serine"),
            case 'T':  //"threonine"),
            case 'V':  //"valine"),
            case 'W':  //"tryptophan"),
            case 'Y':  //"tyrosine"),
                return true;
            default:
                return false;
        }
    }


    /**
      * convert to an index
      *
      * @param in !null value
      * @return 0.. n - 1
      */
     public static int asIndex(FastaAminoAcid in) {
         switch (in) {
             case A:
                 return 0;
             case C:
                 return 1;
             case D: //("aspartate"),
                 return 2;
             case E:  //"glutamate"),
                 return 3;
             case F:  //"phenylalanine"),
                 return 4;
             case G:  //"glycine"),
                 return 5;
             case H:  //"histidine"),
                 return 6;
             case I:  //"isoleucine"),
                 return 7;
             case K:  //"lysine"),
                 return 8;
             case L:  //"leucine"),
                 return 9;
             case M:  //"methionine"),
                 return 10;
             case N:  //"asparagine"),
                 return 11;
             case P:  //"proline"),
                 return 12;
             case Q:  //"glutamine"),
                 return 13;
             case R:  //"arginine"),
                 return 14;
             case S:  //"serine"),
                 return 15;
             case T:  //"threonine"),
                 return 16;
             case V:  //"valine"),
                 return 17;
             case W:  //"tryptophan"),
                 return 18;
             case Y:  //"tyrosine"),
                 return 19;
             case Z:  //"glutamate or glutamine"),
                 return 20;
             case B:  // "aspartate or asparagine"
                 return 22;
             case UNKNOWN:
             case X:
                 return -1;   // todo id this right
             default:  // "aspartate or asparagine"
                 throw new IllegalStateException("Never get here");

         }
      }
    /**
      * convert to an index
      *
      * @param in !null value
      * @return 0.. n - 1
      */
     public static FastaAminoAcid fromIndex(int in) {
         switch (in) {
             case 0:
                 return A;
             case 1:
                 return C;
             case 2: //("aspartate"),
                 return D;
             case 3:  //"glutamate"),
                 return E;
             case 4:  //"phenylalanine"),
                 return F;
             case 5:  //"glycine"),
                 return G;
             case 6:  //"histidine"),
                 return H;
             case 7:  //"isoleucine"),
                 return I;
             case 8:  //"lysine"),
                 return K;
             case 9:  //"leucine"),
                 return L;
             case 10:  //"methionine"),
                 return M;
             case 11:  //"asparagine"),
                 return N;
             case 12:  //"proline"),
                 return P;
             case 13:  //"glutamine"),
                 return Q;
             case 14:  //"arginine"),
                 return R;
             case 15:  //"serine"),
                 return S;
             case 16:  //"threonine"),
                 return T;
             case 17:  //"valine"),
                 return V;
             case 18:  //"tryptophan"),
                 return W;
             case 19:  //"tyrosine"),
                 return Y;
             case 20:   //"glutamate or glutamine"),
             case 22:   // "aspartate or asparagine"
             case -1:   // "aspartate or asparagine"
                    return null;
//             case Z:  //"glutamate or glutamine"),
//                 return 20;
//             case B:  // "aspartate or asparagine"
//                 return 22;
//             case X:  // "aspartate or asparagine"
//                 return -1;   // todo id this right
         }
         throw new IllegalStateException("Never get here");
     }


    public static FastaAminoAcid[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = FastaAminoAcid.class;

    private final String m_Name;
    private final double m_Hydrophobicity;
    private final double m_PkB;
    private final double m_PHIsoelectric;

    FastaAminoAcid(String pName,double hydrophobicity, double pkb,double pI) {
        m_Name = pName;
        m_Hydrophobicity = hydrophobicity;
        m_PkB = pkb;
        m_PHIsoelectric = pI;
    }

    public String getName() {
        return m_Name;
    }

    public double getHydrophobicity() {
        return m_Hydrophobicity;
    }

    public double getPkB() {
        return m_PkB;
    }

    /**
     * three letter abbreviation
     *
     * @return
     */
    public String getAbbreviation() {
        switch (this) {
            case I:
                return "ILE";
            case N:
                return "ASN";
            case Q:
                return "GLN";
            case W:
                return "TRP";
            case B:
                return "ASX";
            case Z:
                return "GLX";

        }
        // all others are first three letters
        return getName().substring(0, 3).toUpperCase();
    }

    @Override
    public String toString() {
        if(UNKNOWN == this)
            return "X";
        return super.toString();
    }

}
