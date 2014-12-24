package org.systemsbiology.xtandem;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.SequenceUtilities
 *
 * @author Steve Lewis
 * @date Dec 21, 2010
 */
public class SequenceUtilities  implements Serializable {
    public static SequenceUtilities[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = SequenceUtilities.class;

    private MassCalculator m_calc;

    private boolean m_bPotential; // true if potential modifications are to be used
    private boolean m_bComplete; // true if complete modifications are to be used

    private double m_dAmmonia; // mass of ammonia
    private double m_dProton; // mass of proton
    private double m_dWater; // mass of water

    private float m_fNT; // mass to modify the N-terminal amino acid in a protein
    private float m_fCT; // mass to modify the C-terminal amino acid in a protein
    private double m_dA; // mass to add to a peptide mass to obtain an a ion mass
    private double m_dB; // mass to add to a peptide mass to obtain a b ion mass
    private double m_dC; // mass to add to a peptide mass to obtain a c ion mass
    private double m_dX; // mass to add to a peptide mass to obtain an x ion mass
    private double m_dY; // mass to add to a peptide mass to obtain a y ion mass
    private double m_dZ; // mass to add to a peptide mass to obtain a z ion mass
  //  private double m_dCleaveN; // mass added to the N-terminal residue by a cleavage reaction
  //  private double m_dCleaveC; // mass added to the C-terminal residue by a cleavage reaction
    private double m_dCleaveNdefault; // 1 (hydrolysis)
    private double m_dCleaveCdefault; // 17 (hydrolysis)

    public static final int NUMBER_ARRAY_POSITIONS = 128;
    private final float[] m_pfAaMass = new float[NUMBER_ARRAY_POSITIONS]; // array of residue masses (less water), indexed by residue character code
    private double[] m_pdAaMass = new double[NUMBER_ARRAY_POSITIONS]; // array of residue masses (less water), indexed by residue character code
    private double[] m_pdAaMod = new double[NUMBER_ARRAY_POSITIONS]; // array of residue modification masses, indexed by residue character code
    private double[] m_pdAaPrompt = new double[NUMBER_ARRAY_POSITIONS]; // array of residue modification masses, indexed by residue character code
    private double[] m_pdAaFullMod = new double[NUMBER_ARRAY_POSITIONS]; // array of residue modification masses, indexed by residue character code
    private float[] m_pfAScore = new float[NUMBER_ARRAY_POSITIONS]; // array of residue scores for a ions, indexed by residue character code
    private float[] m_pfBScore = new float[NUMBER_ARRAY_POSITIONS]; // array of residue scores for b ions, indexed by residue character code
    private float[] m_pfYScore = new float[NUMBER_ARRAY_POSITIONS]; // array of residue scores for y ions, indexed by residue character code
    private float[] m_pfXScore = new float[NUMBER_ARRAY_POSITIONS]; // array of residue scores for x ions, indexed by residue character code
    private float[] m_pfA17Score = new float[NUMBER_ARRAY_POSITIONS]; // array of residue scores for a-17 ions, indexed by residue character code
    private float[] m_pfB17Score = new float[NUMBER_ARRAY_POSITIONS]; // array of residue scores for b-17 ions, indexed by residue character code
    private float[] m_pfY17Score = new float[NUMBER_ARRAY_POSITIONS]; // array of residue scores for y-17 ions, indexed by residue character code
    private float[] m_pfX17Score = new float[NUMBER_ARRAY_POSITIONS];  // array of residue scores for x-17 ions, indexed by residue character code
    private float[] m_pfA18Score = new float[NUMBER_ARRAY_POSITIONS]; // array of residue scores for a-18 ions, indexed by residue character code
    private float[] m_pfB18Score = new float[NUMBER_ARRAY_POSITIONS]; // array of residue scores for b-18 ions, indexed by residue character code
    private float[] m_pfY18Score = new float[NUMBER_ARRAY_POSITIONS]; // array of residue scores for y-18 ions, indexed by residue character code
    private float[] m_pfX18Score = new float[NUMBER_ARRAY_POSITIONS]; // array of residue scores for x-18 ions, indexed by residue character code


   // private List<ProteinMofif> m_vMotifs;
    private Map<Integer, Integer> m_mapMotifs;
    private Map<Character, Integer> m_mapMotifMods;
    private Map<Integer, Double> m_mapMods;
    private boolean m_bPotentialMotif;
    private boolean m_bSequenceMods;
    private boolean m_bIsModified;
    private String m_strDefaultMaybe;

    public SequenceUtilities(IParameterHolder params) {
         this(MassCalculator.getDefaultCalculator(),params);
     }

    protected  SequenceUtilities(MassCalculator mc,IParameterHolder params)  {
        m_calc =  mc;
        // all of these are scoring factors - default is 1
        for (int i = 0; i < NUMBER_ARRAY_POSITIONS; i++) {
            m_pfAScore[i] = 1.0F;
            m_pfBScore[i] = 1.0F;
            m_pfYScore[i] = 1.0F;
            m_pfXScore[i] = 1.0F;
            // plus a hydroxly residue
            m_pfA17Score[i] = 1.0F;
            m_pfB17Score[i] = 1.0F;
            m_pfY17Score[i] = 1.0F;
            m_pfX17Score[i] = 1.0F;
            // plus a hydroxly residue
            m_pfA18Score[i] = 1.0F;
            m_pfB18Score[i] = 1.0F;
            m_pfY18Score[i] = 1.0F;
            m_pfX18Score[i] = 1.0F;

        }
        m_dCleaveCdefault = m_calc.calcMass("OH");
        m_dCleaveNdefault = m_calc.calcMass("H");
        XTandemUtilities.setCleaveCMass(m_dCleaveCdefault);
        XTandemUtilities.setCleaveNMass(m_dCleaveNdefault);

        initializeMasses();
        config(params);
        if(XTandemUtilities.getCleaveNMass() == 0)
            throw new IllegalStateException("problem"); // ToDo change

    }

    public SequenceUtilities(MassType mc,IParameterHolder params) {
        this(MassCalculator.getCalculator(mc),params);
    }

    public void config(IParameterHolder params) {
        String strValue;
        strValue = params.getParameter("residue, potential modification mass");
        if (strValue != null && strValue.length() > 0) {
            modify_maybe(strValue);
        }
        strValue = params.getParameter("residue, potential modification motif");
        if (strValue != null && strValue.length() > 0) {
            throw new UnsupportedOperationException("Fix This"); // ToDo
            //modify_motif(strValue);
        }
        Float fValue = params.getFloatParameter("protein, N-terminal residue modification mass");
        if (fValue != null) {
            modify_n(fValue);
        }
        fValue = params.getFloatParameter("protein, C-terminal residue modification mass");
        if (fValue != null) {
            modify_c(fValue);
        }
        fValue = params.getFloatParameter("protein, cleavage N-terminal mass change",1.007825F);
        if (fValue != null) {
            XTandemUtilities.setCleaveNMass(fValue);
          }
        fValue = params.getFloatParameter("protein, cleavage C-terminal mass change",17.002735F);
        if (fValue != null) {
            XTandemUtilities.setCleaveCMass(fValue);
        }

        /**
         * adjust the scoring of certain opns
         */
        if (params.getBooleanParameter("refine, spectrum synthesis", false)) {
            set_y('P', 5.0F);
            set_b('D', 5.0F);
            set_b('N', 2.0F);
            set_b('V', 3.0F);
            set_b('E', 3.0F);
            set_b('Q', 2.0F);
            set_b('I', 3.0F);
            set_b('L', 3.0F);

        }
    }

    /*
    * blur takes an SMap object and adds values to it adjacent to the
    * values in the existing map. this process makes the conversion from
    * floating point m/z values to integer SMap index values more accurate.
    * the half-width of the distribution around each initial value is set
    * by the m_fWidth value. For example,
    *
    * if there is an intensity value I at index value N,
    * then with a width of 2, the following new values are created,
    * I(N-2),I(N-1),I(N+1),I(N+2)
    *
    * the value for the width has an impact on the performance of the
    * protein modeling session: the wider the width, the more memory that is required to hold
    * the SMaps for the spectra. as that memory expands, the ability of
    * the various processor caches to hold the SMaps will change. keeping
    * the SMaps in the fastest cache speeds up the calculation considerably.
    */

    public IMeasuredSpectrum blur(IMeasuredSpectrum in) {
        /*
         * fConvert and fFactor are only used if the m_lErrorType uses ppm for fragment ion mass errors
         * if ppm is used, the width at m/z = 200.0 is taken as the base width for blurring & the
         * width is scaled by the measured m/z.
         * NOTE: the m_fErr value used in the ppm case is: (the error in ppm) x 200
         */
        // copy the original peaks
        final ISpectrumPeak[] spectrumPeaks = in.getPeaks();
        ISpectrumPeak[] peaks = new ISpectrumPeak[spectrumPeaks.length];
        for (int i = 0; i < spectrumPeaks.length; i++) {
            peaks[i] = new SpectrumPeak(spectrumPeaks[i]);

        }
        //           throw new UnsupportedOperationException("Fix This"); // ToDo
//        float fConvert = m_fErr / m_fWidth;
//        float fFactor = (float) 200.0 / fConvert;
//        for(ISpectrumPeak peak : in.getPeaks()) {
//             if(peak.getPeak() > 0.5) {
//
//             }
//        }
//        if (_s[tCount].m_fI > 0.5) {
//            lValue = (long) (_s[tCount].m_fM / fConvert);
//            a = -1 * w;
//            if (m_lErrorType & T_FRAGMENT_PPM) {
//                a = (long) ((float) a * (float) lValue / fFactor - 0.5);
//                if (a > -1 * w)
//                    a = -1 * w;
//            }
//            while (a <= w) {
//                if (uType.m_lM == lValue + a) {
//                    if (uType.m_fI < _s[tCount].m_fI) {
//                        vType.back().m_fI = _s[tCount].m_fI;
//                    }
//                }
//                else {
//                    uType.m_lM = lValue + a;
//                    uType.m_fI = _s[tCount].m_fI;
//                    vType.push_back(uType);
//                }
//                a++;
//            }
//        }
        return new ScoringMeasuredSpectrum(in.getPrecursorCharge(),
                in.getPrecursorMassChargeRatio(), in.getScanData(), peaks);


    }

    public MassCalculator getCalc() {
        return m_calc;
    }

    /**
     * return the mass of an amino acid
     *
     * @param aa
     * @return
     */
    public double getAnimoAcidMass(char aa) {
        MassCalculator calc = getCalc();
        return calc.getAminoAcidMass(aa);
      //  return m_pdAaMass[aa & 0x7F];
    }

    public double getAddedMass(AminoTerminalType type) {
        switch (type) {
            case N:
                return   XTandemUtilities.getCleaveNMass();
            case C:
                return XTandemUtilities.getCleaveCMass();
        }
        throw new UnsupportedOperationException("Cannot get here");
    }

    /**
     * what mass is needed to add for a specific ion type
     * this will add specifics for the AmoinAcid terminal -
     * N -> A,B,C
     * C -> X,Y,Z
     *
     * @param type !null type
     * @return mass to add
     */
    public double getAddedMass(IonType type) {
        double ret = getAddedMass(type.getTerminal());
        switch (type) {
            case A:
                return ret + m_dA;
            case B:
                return ret + m_dB;
            case C:
                return ret + m_dC;
            case X:
                return ret + m_dX;
            case Y:
                return ret + m_dY;
            case Z:
                return ret + m_dZ;
        }
        throw new UnsupportedOperationException("Cannot get here");
    }

    /**
     * return the madd of a polypeptide
     *
     * @param pp
     * @return
     */
    public double getSequenceMass(IPolypeptide pp) {
        String seq = pp.getSequence().toUpperCase();
        double ret = 0;
        for (int i = 0; i < seq.length(); i++) {
            char c = seq.charAt(i);
            ret += getAnimoAcidMass(c);
        }
        return ret;
    }

    public double mztomh(double mz, float z) {
        return (mz - m_dProton) * z + m_dProton;
    }

    public double getAaMass(char c, int t) {
        double dValue = m_pdAaMass[c];
        dValue += m_pdAaMod[c];
        dValue += m_pdAaFullMod[c];
        dValue += m_pdAaPrompt[c];

        if (m_bSequenceMods) {
            Double dx = m_mapMods.get(t);
            if (dx != null)
                dValue += dx;
//            SMap::iterator itSeq = m_mapMods.find(t);
//            if (itSeq != m_mapMods.end())
//                dValue += itSeq - > second;
        }

        return dValue;
    }

    boolean add_mod(final char _c, final int _v) {
        m_mapMotifMods.put(_c, _v);
        return true;
    }

    boolean clear_motifs(final boolean _b) {
//        map<char, int>::iterator itValue = m_mapMotifMods.begin();
//        while (itValue != m_mapMotifMods.end()) {
//            m_pdAaMod[itValue - > first + 32] = 0.0;
//            m_pdAaPrompt[itValue - > first + 32] = 0.0;
//            itValue++;
//        }
        if (_b) {
            m_mapMotifMods.clear();
        }
        return true;
    }

    public boolean set_motifs() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        map<char, int>::iterator itValue = m_mapMotifMods.begin();
//        while (itValue != m_mapMotifMods.end()) {
//            m_pdAaMod[itValue - > first + 32] = m_vMotifs[itValue - > second].m_fMass;
//            m_pdAaPrompt[itValue - > first + 32] = m_vMotifs[itValue - > second].m_fMassPrompt;
//            itValue++;
//        }
//        return true;
    }

    public boolean synthesis(final boolean _b) {
        if (_b) {
            set_y('P', 5.0F);
            set_b('D', 5.0F);
            set_b('N', 2.0F);
            set_b('V', 3.0F);
            set_b('E', 3.0F);
            set_b('Q', 2.0F);
            set_b('I', 3.0F);
            set_b('L', 3.0F);
        }
        else {
            char a = 0;
            while (a < 127) {
                set_y(a, 1.0F);
                set_b(a, 1.0F);
                a++;
            }
        }
        return true;
    }

/*
 * imports a list of complete residue modifications, using a string with the following format:
 * m1@r1,m2@r2,m3@r3, ...
 * where mX is the mass of the modification, in Daltons, and rX is the single letter code for the
 * modified residue. there is no limit to the length of the string (but there are only 20 amino acids)
 * NOTE: lower case index values are reserved for potential modifications and may not be used here
 */

    boolean modify_all(String _s) {
        int tStart = 'A';
        while (tStart < 'Z' + 1) {
            m_pdAaFullMod[tStart] = 0.0;
            tStart++;
        }
        tStart = 'a';
        while (tStart < 'z' + 1) {
            m_pdAaFullMod[tStart] = 0.0;
            tStart++;
        }
        m_pdAaFullMod[']'] = 0.0;
        m_pdAaFullMod['['] = 0.0;
        tStart = 0;
        if (_s.length() == 0)
            return false;
        int tAt = 0;
        double fValue;
        String strValue = _s.substring(tStart, _s.length() - tStart);
        fValue = Float.parseFloat(strValue);
        while (fValue != 0.0) {
            throw new UnsupportedOperationException("Fix This"); // ToDo
//            m_bComplete = true;
//            tAt = _s.find('@', tStart);
//            if (tAt == _s.npos)
//                break;
//            tAt++;
//            if (isalpha(_s[tAt])) {
//                m_pdAaFullMod[_s[tAt]] = fValue;
//                m_pdAaFullMod[_s[tAt] + 32] = fValue;
//            }
//            else {
//                m_pdAaFullMod[_s[tAt]] = fValue;
//            }
//            tStart = _s.find(',', tAt);
//            if (tStart == _s.npos)
//                break;
//            tStart++;
//            strValue = _s.substring(tStart, _s.length() - tStart);
//            fValue = Float.parseFloat(strValue);
        }
        return true;
    }
/*
 * imports a list of potential residue modifications, using a String with the following format:
 * m1@r1,m2@r2,m3@r3, ...
 * where mX is the mass of the modification, in Daltons, and rX is the single letter code for the
 * modified residue. there is no limit to the length of the String (but there are only 20 amino acids)
 * NOTE: lower case index values are reserved for potential modifications
 */

    boolean modify_maybe(String _s) {
        int tStart = 'a';
        while (tStart < 'z' + 1) {
            m_pdAaMod[tStart] = 0.0;
            m_pdAaPrompt[tStart] = 0.0;
            tStart++;
        }
        m_pdAaMod['['] = 0.0;
        m_pdAaMod[']'] = 0.0;

        m_bPotential = false;
        if (_s.length() == 0)
            return false;
        tStart = 0;
        int tAt = 0;
        int tColon = 0;
        double fValue = 0.0;
        double fPrompt = 0.0;
        String strValue = _s.substring(tStart, _s.length() - tStart);
        m_strDefaultMaybe = strValue;
        String[] items = m_strDefaultMaybe.split(",");
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
//            fValue = Float.parseFloat(item);
//            char cRes = '\0';
//            while (fValue != 0.0) {
//                m_bPotential = true;
//                throw new UnsupportedOperationException("Fix This"); // ToDo
//            tAt = _s.find('@', tStart);
//            if (tAt == _s.npos)
//                break;
//            tColon = _s.find(':', tStart);
//            fPrompt = 0.0;
//            if (tColon != _s.npos && tColon < tAt) {
//                fPrompt = atof(_s.substring(tColon + 1, tAt - tColon).c_str());
//            }
//            tAt++;
//            cRes = _s[tAt];
//            if (cRes >= 'A' && cRes <= 'Z') {
//                cRes += 32;
//            }
//            m_pdAaMod[cRes] = fValue;
//            m_pdAaPrompt[cRes] = fPrompt;
//            tStart = _s.find(',', tAt);
//            if (tStart == _s.npos)
//                break;
//            tStart++;
//            strValue = _s.substring(tStart, _s, length() - tStart);
//            fValue = atof(strValue.c_str());
//            }

        }
        return true;
    }

    boolean modify_annotation(String _s) {
        int tStart = 'a';
        final int tEnd = 'z' + 1;
        while (tStart < tEnd) {
            m_pdAaMod[tStart] = 0.0;
            m_pdAaPrompt[tStart] = 0.0;
            tStart++;
        }
        m_pdAaMod['['] = 0.0;
        m_pdAaMod[']'] = 0.0;

        m_bPotential = false;
        tStart = 0;
        int tAt = 0;
        int tColon = 0;
        double fValue = 0.0;
        double fPrompt = 0.0;
        String strValue = _s;
        if (_s.length() != 0) {
            strValue += ",";
        }
        strValue += m_strDefaultMaybe;
        if (strValue.length() == 0) {
            return false;
        }
        String strV = strValue;
        fValue = Float.parseFloat(strV);
        char cRes = '\0';
        while (fValue != 0.0) {
            m_bPotential = true;
            throw new UnsupportedOperationException("Fix This"); // ToDo
//            tAt = strValue.find('@', tStart);
//            if (tAt == strValue.npos)
//                break;
//            tColon = strValue.find(':', tStart);
//            fPrompt = 0.0;
//            if (tColon != strValue.npos && tColon < tAt) {
//                fPrompt = atof(strValue.substring(tColon + 1, tAt - tColon).c_str());
//            }
//            tAt++;
//            cRes = strValue.charAt(tAt);
//            if (cRes >= 'A' && cRes <= 'Z') {
//                cRes += 32;
//            }
//            m_pdAaMod[cRes] = fValue;
//            m_pdAaPrompt[cRes] = fPrompt;
//            tStart = strValue.find(',', tAt);
//            if (tStart == strValue.npos)
//                break;
//            tStart++;
//            strV = strValue.substring(tStart, _s, length() - tStart);
//            fValue = Float.parseFloat(strV );
        }
        return true;
    }


    boolean motif_set(final ProteinSequence _s) {
        m_mapMods.clear();
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        m_mapMods = _s.m_mapMods;
//        if (!m_mapMods.length() == 0) {
//            m_bSequenceMods = true;
//        }
//        else {
//            m_bSequenceMods = false;
//        }
//        if (m_vMotifs.size() == 0) {
//            return false;
//        }
//        m_mapMotifs.clear();
//        char*pString = new char[_s.m_strSeq, length() + 1];
//        strcpy(pString, _s.m_strSeq.c_str());
//        char*pValue = pString;
//        int a = 0;
//        int b = 0;
//        int tPos;
//        final int tMotifs = m_vMotifs, length
//        ();
//        while (*pValue != '\0'){
//        a = 0;
//        while (a < tMotifs) {
//            if (m_vMotifs[a].check(pValue, tPos)) {
//                m_mapMotifs[pValue - pString + tPos] = a;
//            }
//            a++;
//        }
//        pValue++;
//   }
//          return true;
    }

//    boolean modify_motif(final String _s) {
//        m_vMotifs.clear();
//        m_bPotentialMotif = false;
//        if (_s.length() == 0) {
//            return false;
//        }
//        int tStart = 0;
//        int tAt = 0;
//        float fValue;
//        throw new UnsupportedOperationException("Fix This"); // ToDo
////        String strValue = _s.substring(tStart, _s, length() - tStart);
////        fValue = (float) atof(strValue.c_str());
////        char*pOut = new char[1024];
////        ProteinMofif motValue;
////        while (fValue != 0.0) {
////            tAt = _s.find('@', tStart);
////            if (tAt == _s.npos)
////                break;
////            tAt = tStart;
////            tStart = _s.find(',', tAt);
////            if (tStart == _s.npos) {
////                strValue = _s.substring(tAt, tStart - tAt);
////                strcpy(pOut, strValue.c_str());
////                motValue.initialize();
////                if (motValue.set(pOut)) {
////                    m_vMotifs.push_back(motValue);
////                }
////                break;
////            }
////            else {
////                strValue = _s.substring(tAt, _s, length() - tAt);
////                strcpy(pOut, strValue.c_str());
////                motValue.initialize();
////                if (motValue.set(pOut)) {
////                    m_vMotifs.push_back(motValue);
////                }
////            }
////            tStart++;
////            strValue = _s.substring(tStart, _s, length() - tStart);
////            fValue = (float) atof(strValue.c_str());
////        }
////        if (!m_vMotifs.length() == 0) {
////            m_bPotential = true;
////            m_bPotentialMotif = true;
////        }
////        return true;
//    }

/*
 * sets the mass of the c-terminal residue modification for a protein
 */

    boolean modify_c(final float _f) {
        m_fCT = _f;
        return true;
    }
/*
 * sets the mass of the n-terminal residue modification for a protein
 */

    boolean modify_n(final float _f) {
        m_fNT = _f;
        return true;
    }

    protected void initializeMasses() {
        set_aa();
        setdAmmonia(m_calc.calcMass("H3N"));
        setdProton(m_calc.calcMass("H"));
        setdWater(m_calc.calcMass("H20"));
       setdY(2 * getdProton());   // todo why is this needed
       setdX(2 * getdProton());  // todo why is this needed
       setdZ(2 * getdProton());  // todo why is this needed

    }

/*
 * initializes the mass of amino acid residues (less water)
 *     chemical formulas found at http://haven.isb-sib.ch/tools/isotopident/htdocs/aa-list.html
 */

    boolean set_aa() {
        float[] pValue = m_pfAaMass;
        if (pValue == null)
            return false;
        double[] pdValue = m_pdAaMass;
        if (pdValue == null)
            return false;

        if (m_calc.getMassType() == MassType.monoisotopic) {
            pdValue['a'] = pdValue['A'] = m_calc.calcMass("C3H5ON");
            pValue['a'] = pValue['A'] = (float) pdValue['A'];

            pdValue['b'] = pdValue['B'] = m_calc.calcMass("C4H6O2N2");    // Same as N
            pValue['b'] = pValue['B'] = (float) pdValue['B'];

            pdValue['c'] = pdValue['C'] = m_calc.calcMass("C3H5ONS");
            pValue['c'] = pValue['C'] = (float) pdValue['C'];

            pdValue['d'] = pdValue['D'] = m_calc.calcMass("C4H5O3N");
            pValue['d'] = pValue['D'] = (float) pdValue['D'];

            pdValue['e'] = pdValue['E'] = m_calc.calcMass("C5H7O3N");
            pValue['e'] = pValue['E'] = (float) pdValue['E'];

            pdValue['f'] = pdValue['F'] = m_calc.calcMass("C9H9ON");
            pValue['f'] = pValue['F'] = (float) pdValue['F'];

            pdValue['g'] = pdValue['G'] = m_calc.calcMass("C2H3ON");
            pValue['g'] = pValue['G'] = (float) pdValue['G'];

            pdValue['h'] = pdValue['H'] = m_calc.calcMass("C6H7ON3");
            pValue['h'] = pValue['H'] = (float) pdValue['H'];

            pdValue['i'] = pdValue['I'] = m_calc.calcMass("C6H11ON");
            pValue['i'] = pValue['I'] = (float) pdValue['I'];

            pdValue['j'] = pdValue['J'] = 0.0;
            pValue['j'] = pValue['J'] = (float) pdValue['J'];

            pdValue['k'] = pdValue['K'] = m_calc.calcMass("C6H12ON2");
            pValue['k'] = pValue['K'] = (float) pdValue['K'];

            pdValue['l'] = pdValue['L'] = m_calc.calcMass("C6H11ON");
            pValue['l'] = pValue['L'] = (float) pdValue['L'];

            pdValue['m'] = pdValue['M'] = m_calc.calcMass("C5H9ONS");
            pValue['m'] = pValue['M'] = (float) pdValue['M'];

            pdValue['n'] = pdValue['N'] = m_calc.calcMass("C4H6O2N2");
            pValue['n'] = pValue['N'] = (float) pdValue['N'];

            pdValue['o'] = pdValue['O'] = m_calc.calcMass("C4H6O2N2");    // Same as N
            pValue['o'] = pValue['O'] = (float) pdValue['O'];

            pdValue['p'] = pdValue['P'] = m_calc.calcMass("C5H7ON");
            pValue['p'] = pValue['P'] = (float) pdValue['P'];

            pdValue['q'] = pdValue['Q'] = m_calc.calcMass("C5H8O2N2");
            pValue['q'] = pValue['Q'] = (float) pdValue['Q'];

            pdValue['r'] = pdValue['R'] = m_calc.calcMass("C6H12ON4");
            pValue['r'] = pValue['R'] = (float) pdValue['R'];

            pdValue['s'] = pdValue['S'] = m_calc.calcMass("C3H5O2N");
            pValue['s'] = pValue['S'] = (float) pdValue['S'];

            pdValue['t'] = pdValue['T'] = m_calc.calcMass("C4H7O2N");
            pValue['t'] = pValue['T'] = (float) pdValue['T'];

            pdValue['u'] = pdValue['U'] = 150.953640;    // Why?
            pValue['u'] = pValue['U'] = (float) pdValue['U'];

            pdValue['v'] = pdValue['V'] = m_calc.calcMass("C5H9ON");
            pValue['v'] = pValue['V'] = (float) pdValue['V'];

            pdValue['w'] = pdValue['W'] = m_calc.calcMass("C11H10ON2");
            pValue['w'] = pValue['W'] = (float) pdValue['W'];

            pdValue['x'] = pdValue['X'] = 111.060000;    // Why?
            pValue['x'] = pValue['X'] = (float) pdValue['X'];

            pdValue['y'] = pdValue['Y'] = m_calc.calcMass("C9H9O2N");
            pValue['y'] = pValue['Y'] = (float) pdValue['Y'];

            pdValue['z'] = pdValue['Z'] = m_calc.calcMass("C5H8O2N2");    // Same as Q
            pValue['z'] = pValue['Z'] = (float) pdValue['Z'];
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
            pValue['a'] = pValue['A'] = (float) pdValue['A'];

            pdValue['b'] = pdValue['B'] = 114.1038;    // Same as N
            pValue['b'] = pValue['B'] = (float) pdValue['B'];

            pdValue['c'] = pdValue['C'] = 103.1388;
            pValue['c'] = pValue['C'] = (float) pdValue['C'];

            pdValue['d'] = pdValue['D'] = 115.0886;
            pValue['d'] = pValue['D'] = (float) pdValue['D'];

            pdValue['e'] = pdValue['E'] = 129.1155;
            pValue['e'] = pValue['E'] = (float) pdValue['E'];

            pdValue['f'] = pdValue['F'] = 147.1766;
            pValue['f'] = pValue['F'] = (float) pdValue['F'];

            pdValue['g'] = pdValue['G'] = 57.0519;
            pValue['g'] = pValue['G'] = (float) pdValue['G'];

            pdValue['h'] = pdValue['H'] = 137.1411;
            pValue['h'] = pValue['H'] = (float) pdValue['H'];

            pdValue['i'] = pdValue['I'] = 113.1594;
            pValue['i'] = pValue['I'] = (float) pdValue['I'];

            pdValue['j'] = pdValue['J'] = 0.0;
            pValue['j'] = pValue['J'] = (float) pdValue['J'];

            pdValue['k'] = pdValue['K'] = 128.1741;
            pValue['k'] = pValue['K'] = (float) pdValue['K'];

            pdValue['l'] = pdValue['L'] = 113.1594;
            pValue['l'] = pValue['L'] = (float) pdValue['L'];

            pdValue['m'] = pdValue['M'] = 131.1926;
            pValue['m'] = pValue['M'] = (float) pdValue['M'];

            pdValue['n'] = pdValue['N'] = 114.1038;
            pValue['n'] = pValue['N'] = (float) pdValue['N'];

            pdValue['o'] = pdValue['O'] = 114.1038;    // Same as N
            pValue['o'] = pValue['O'] = (float) pdValue['O'];

            pdValue['p'] = pdValue['P'] = 97.1167;
            pValue['p'] = pValue['P'] = (float) pdValue['P'];

            pdValue['q'] = pdValue['Q'] = 128.1307;
            pValue['q'] = pValue['Q'] = (float) pdValue['Q'];

            pdValue['r'] = pdValue['R'] = 156.1875;
            pValue['r'] = pValue['R'] = (float) pdValue['R'];

            pdValue['s'] = pdValue['S'] = 87.0782;
            pValue['s'] = pValue['S'] = (float) pdValue['S'];

            pdValue['t'] = pdValue['T'] = 101.1051;
            pValue['t'] = pValue['T'] = (float) pdValue['T'];

            pdValue['u'] = pdValue['U'] = 0.0;    // Why?
            pValue['u'] = pValue['U'] = (float) pdValue['U'];

            pdValue['v'] = pdValue['V'] = 99.1326;
            pValue['v'] = pValue['V'] = (float) pdValue['V'];

            pdValue['w'] = pdValue['W'] = 186.2132;
            pValue['w'] = pValue['W'] = (float) pdValue['W'];

            pdValue['x'] = pdValue['X'] = 113.1594;    // Why?
            pValue['x'] = pValue['X'] = (float) pdValue['X'];

            pdValue['y'] = pdValue['Y'] = 163.1760;
            pValue['y'] = pValue['Y'] = (float) pdValue['Y'];

            pdValue['z'] = pdValue['Z'] = 128.1307;    // Same as Q
            pValue['z'] = pValue['Z'] = (float) pdValue['Z'];
        }

        return true;
    }
/*
 * sets the score for a particular a ion
 */

    boolean set_a(int _c, final float _f) {
        _c &= 0x7f;
        m_pfAScore[_c] = _f;
        m_pfA18Score[_c] = _f;
        m_pfA17Score[_c] = _f;
        return true;
    }
/*
 * sets the score for a particular b ion
 */

    boolean set_b(int _c, final float _f) {
        _c &= 0x7f;
        m_pfBScore[_c] = _f;
        m_pfB18Score[_c] = _f;
        m_pfB17Score[_c] = _f;
        return true;
    }
/*
 * sets the score for a particular x ion
 */

    boolean set_x(int _c, final float _f) {
        _c &= 0x7f;
        m_pfXScore[_c] = _f;
        m_pfX18Score[_c] = _f;
        m_pfX17Score[_c] = _f;
        return true;
    }
/*
 * sets the score for a particular y ion
 */

    boolean set_y(int _c, final float _f) {
        _c &= 0x7f;
        m_pfYScore[_c] = _f;
        m_pfY18Score[_c] = _f;
        m_pfY17Score[_c] = _f;
        return true;
    }

    boolean set_aa_file(String _p) {
        m_bIsModified = false;
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        ifstream ifIn;
//        ifIn.open(_p.c_str());
//        if (ifIn.fail()) {
//            return false;
//        }
//        vector<String> vstrValues;
//        String strValue;
//        char*pLine = new char[1024];
//        while (ifIn.good() && !ifIn.eof()) {
//            ifIn.getline(pLine, 1023);
//            strValue = pLine;
//            vstrValues.push_back(strValue);
//        }
//        ifIn.close();
//        int a = 0;
//        int tStart;
//        int tEnd;
//        double dMass = 0.0;
//        char cAa = '\0';
//        final int tLength = vstrValues, length
//        ();
//        String strType;
//        String strLine;
//        while (a < tLength) {
//            cAa = '\0';
//            strType = " ";
//            dMass = -1.0;
//            strLine = vstrValues[a];
//            if (strLine.find("<aa ") != strLine.npos) {
//                tStart = strLine.find("mass=\"");
//                if (tStart != strLine.npos) {
//                    tStart = strLine.find("\"", tStart);
//                    tStart++;
//                    dMass = atof(strLine.substring(tStart, strLine, length() - tStart).c_str());
//                }
//                tStart = strLine.find("type=\"");
//                if (tStart != strLine.npos) {
//                    tStart = strLine.find("\"", tStart);
//                    tStart++;
//                    cAa = strLine[tStart];
//                }
//                if (cAa != '\0' && dMass >= 0.0) {
//                    if (cAa > 'Z') {
//                        cAa -= 32;
//                    }
//                    if (isalpha(cAa)) {
//                        m_bIsModified = true;
//                        m_pdAaMass[cAa + 32] = m_pdAaMass[cAa] = dMass;
//                        m_pfAaMass[cAa + 32] = m_pfAaMass[cAa] = (float) dMass;
//                    }
//                }
//            }
//            if (strLine.find("<molecule ") != strLine.npos) {
//                tStart = strLine.find("mass=\"");
//                if (tStart != strLine.npos) {
//                    tStart = strLine.find("\"", tStart);
//                    tStart++;
//                    dMass = atof(strLine.substring(tStart, strLine, length() - tStart).c_str());
//                }
//                tStart = strLine.find("type=\"");
//                if (tStart != strLine.npos) {
//                    tStart = strLine.find("\"", tStart);
//                    tStart++;
//                    tEnd = strLine.find("\"", tStart);
//                    if (tEnd != strLine.npos) {
//                        strType = strLine.substring(tStart, tEnd - tStart);
//                    }
//                }
//                if (dMass >= 0.0 && strType != " ") {
//                    if (strType == "NH3") {
//                        m_bIsModified = true;
//                        m_dAmmonia = dMass;
//                    }
//                    if (strType == "H2O") {
//                        m_bIsModified = true;
//                        m_dWater = dMass;
//                    }
//                }
//            }
//            a++;
//        }
//        m_dC = m_dAmmonia;
//        m_dY = m_dWater;
//        return true;
    }

    boolean is_modified() {
        return m_bIsModified;
    }

    boolean set_modified(final boolean _b) {
        m_bIsModified = _b;
        return m_bIsModified;
    }

    public boolean isbPotential() {
        return m_bPotential;
    }

    public void setbPotential(boolean pBPotential) {
        m_bPotential = pBPotential;
    }

    public boolean isbComplete() {
        return m_bComplete;
    }

    public void setbComplete(boolean pBComplete) {
        m_bComplete = pBComplete;
    }

    public double getdAmmonia() {
        return m_dAmmonia;
    }

    public void setdAmmonia(double pDAmmonia) {
        m_dAmmonia = pDAmmonia;
    }

    public double getdProton() {
        return m_dProton;
    }

    public void setdProton(double pDProton) {
        m_dProton = pDProton;
    }

    public double getdWater() {
        return m_dWater;
    }

    public void setdWater(double pDWater) {
        m_dWater = pDWater;
    }

    public float getfNT() {
        return m_fNT;
    }

    public void setfNT(float pFNT) {
        m_fNT = pFNT;
    }

    public float getfCT() {
        return m_fCT;
    }

    public void setfCT(float pFCT) {
        m_fCT = pFCT;
    }

    public double getdA() {
        return m_dA;
    }

    public void setdA(double pDA) {
        m_dA = pDA;
    }

    public double getdB() {
        return m_dB;
    }

    public void setdB(double pDB) {
        m_dB = pDB;
    }

    public double getdC() {
        return m_dC;
    }

    public void setdC(double pDC) {
        m_dC = pDC;
    }

    public double getdX() {
        return m_dX;
    }

    public void setdX(double pDX) {
        m_dX = pDX;
    }

    public double getdY() {
        return m_dY;
    }

    public void setdY(double pDY) {
        m_dY = pDY;
    }

    public double getdZ() {
        return m_dZ;
    }

    public void setdZ(double pDZ) {
        m_dZ = pDZ;
    }

  
    public double getdCleaveNdefault() {
        return m_dCleaveNdefault;
    }

    public void setdCleaveNdefault(double pDCleaveNdefault) {
        m_dCleaveNdefault = pDCleaveNdefault;
    }

    public double getdCleaveCdefault() {
        return m_dCleaveCdefault;
    }

    public void setdCleaveCdefault(double pDCleaveCdefault) {
        m_dCleaveCdefault = pDCleaveCdefault;
    }

    public float[] getPfAaMass() {
        return m_pfAaMass;
    }

//    public void setPfAaMass(float[] pPfAaMass)
//    {
//        m_pfAaMass = pPfAaMass;
//    }

    public double[] getPdAaMass() {
        return m_pdAaMass;
    }

    public void setPdAaMass(double[] pPdAaMass) {
        m_pdAaMass = pPdAaMass;
    }

    public double[] getPdAaMod() {
        return m_pdAaMod;
    }

    public void setPdAaMod(double[] pPdAaMod) {
        m_pdAaMod = pPdAaMod;
    }

    public double[] getPdAaPrompt() {
        return m_pdAaPrompt;
    }

    public void setPdAaPrompt(double[] pPdAaPrompt) {
        m_pdAaPrompt = pPdAaPrompt;
    }

    public double[] getPdAaFullMod() {
        return m_pdAaFullMod;
    }

    public void setPdAaFullMod(double[] pPdAaFullMod) {
        m_pdAaFullMod = pPdAaFullMod;
    }

    public float[] getPfAScore() {
        return m_pfAScore;
    }

    public void setPfAScore(float[] pPfAScore) {
        m_pfAScore = pPfAScore;
    }

    public float[] getPfBScore() {
        return m_pfBScore;
    }

    public void setPfBScore(float[] pPfBScore) {
        m_pfBScore = pPfBScore;
    }

    public float[] getPfYScore() {
        return m_pfYScore;
    }

    public void setPfYScore(float[] pPfYScore) {
        m_pfYScore = pPfYScore;
    }

    public float[] getPfXScore() {
        return m_pfXScore;
    }

    public void setPfXScore(float[] pPfXScore) {
        m_pfXScore = pPfXScore;
    }

    public float[] getPfA17Score() {
        return m_pfA17Score;
    }

    public void setPfA17Score(float[] pPfA17Score) {
        m_pfA17Score = pPfA17Score;
    }

    public float[] getPfB17Score() {
        return m_pfB17Score;
    }

    public void setPfB17Score(float[] pPfB17Score) {
        m_pfB17Score = pPfB17Score;
    }

    public float[] getPfY17Score() {
        return m_pfY17Score;
    }

    public void setPfY17Score(float[] pPfY17Score) {
        m_pfY17Score = pPfY17Score;
    }

    public float[] getPfX17Score() {
        return m_pfX17Score;
    }

    public void setPfX17Score(float[] pPfX17Score) {
        m_pfX17Score = pPfX17Score;
    }

    public float[] getPfA18Score() {
        return m_pfA18Score;
    }

    public void setPfA18Score(float[] pPfA18Score) {
        m_pfA18Score = pPfA18Score;
    }

    public float[] getPfB18Score() {
        return m_pfB18Score;
    }

    public void setPfB18Score(float[] pPfB18Score) {
        m_pfB18Score = pPfB18Score;
    }

    public float[] getPfY18Score() {
        return m_pfY18Score;
    }

    public void setPfY18Score(float[] pPfY18Score) {
        m_pfY18Score = pPfY18Score;
    }

    public float[] getPfX18Score() {
        return m_pfX18Score;
    }

    public void setPfX18Score(float[] pPfX18Score) {
        m_pfX18Score = pPfX18Score;
    }

//    public List<ProteinMofif> getvMotifs() {
//        return m_vMotifs;
//    }
//
//    public void setvMotifs(List<ProteinMofif> pVMotifs) {
//        m_vMotifs = pVMotifs;
//    }

    public Map<Integer, Integer> getMapMotifs() {
        return m_mapMotifs;
    }

    public void setMapMotifs(Map<Integer, Integer> pMapMotifs) {
        m_mapMotifs = pMapMotifs;
    }

    public Map<Character, Integer> getMapMotifMods() {
        return m_mapMotifMods;
    }

    public void setMapMotifMods(Map<Character, Integer> pMapMotifMods) {
        m_mapMotifMods = pMapMotifMods;
    }

    public Map<Integer, Double> getMapMods() {
        return m_mapMods;
    }

    public void setMapMods(Map<Integer, Double> pMapMods) {
        m_mapMods = pMapMods;
    }

    public boolean isbPotentialMotif() {
        return m_bPotentialMotif;
    }

    public void setbPotentialMotif(boolean pBPotentialMotif) {
        m_bPotentialMotif = pBPotentialMotif;
    }

    public boolean isbSequenceMods() {
        return m_bSequenceMods;
    }

    public void setbSequenceMods(boolean pBSequenceMods) {
        m_bSequenceMods = pBSequenceMods;
    }

    public boolean isbIsModified() {
        return m_bIsModified;
    }

    public void setbIsModified(boolean pBIsModified) {
        m_bIsModified = pBIsModified;
    }

    public String getStrDefaultMaybe() {
        return m_strDefaultMaybe;
    }

    public void setStrDefaultMaybe(String pStrDefaultMaybe) {
        m_strDefaultMaybe = pStrDefaultMaybe;
    }
}
