package org.systemsbiology.xtandem.scoring;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.SpectrumHistogram
 *
 * @author Steve Lewis
 * @date Dec 21, 2010
 */
/*
 *	The mhistogram class is used for storing information about scoring in the mspectrum class.
 * Two types of histogram classes are used: the mhistogram which stores information about
 * convolution and hyper scores and the count_mhistogram class which is used to store the number
 * of specific ion types (e.g. y or b ions). The mhistgram class converts the scores into base-10 logs
 * to make the histogram more compact and to linearize the extreme value distribution, which
 * governs the scoring distribution. In order to calculate expectation values from these distributions,
 * they are first converted to survival functions (by the survival methdod), the outlying non-random
 * scores are removed from the distribution and the high scoring tail of the distribution is fitted
 * to a log-log linear distribution using least-squares in the model method.
 * mhistogram is included in the mspectrum.h file only
 */


public class SpectrumHistogram {
    public static SpectrumHistogram[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = SpectrumHistogram.class;

    private double m_dProteinFactor = 1; // a weighting factor
    private double m_fA0 = 4.8; // the intercept of the least-squares fit performed in model
    private double m_fA1 = -0.28; // the slope of the least-squares fit performed in model
    private final List<Integer> m_vlSurvive = new ArrayList<Integer>();  // the survival function array, reduced to [96] from [256]
    private final List<Short> m_pList = new ArrayList<Short>(); // doubling buffer
    private long m_lSum;
    private boolean m_IsDirty;

    public SpectrumHistogram() {
    }


    /*
    * expect uses the equation derived in model to convert scores into expectation values
    * survival and model must be called before expect will return reasonable values
    */

    public double expect(double _f) {
        return Math.pow(10.0, (m_fA0 + m_fA1 * _f));
    }
/*
 * list returns a specified value from the stochastic distribution of the histogram
 */

    public int list(int _l) {
        if (_l >= m_pList.size())
            return 0;
        return m_pList.get(_l);
    }

    /*
 * survive returns a specified value from the stochastic distribution of the survival function
 */

    public int survive(final int _l) {
        return m_vlSurvive.get(_l);
    }

    public long sum() {
        return m_lSum;
    }
/*
 * length returns the maximum length of the histogram
 */

    public int length() {
        return m_pList.size();
    }
/*
 * a0 returns the first parameter in the linear fit to the survival function
 */

    public double a0() {
        return m_fA0;
    }
/*
 * a1 returns the first parameter in the linear fit to the survival function
 */

    public double a1() {
        return m_fA1;
    }
/*
 * expect_protein allows for a modified expectation value to be used for proteins
 */

    public double expect_protein(final double _f) {
        return Math.pow(10.0, (m_fA0 + m_fA1 * _f)) * m_dProteinFactor;
    }
/*
 * set_protein_factor simply sets the value for m_dProteinFactor
 */

    public boolean set_protein_factor(final double _d) {
        if (_d <= 0.0)
            return false;
        m_dProteinFactor = _d;
        return true;
    }
/*
 * reset zeros the histogram array m_pList
 */

    public boolean clear() {
        m_pList.clear();
        //    m_ulCount = 0;
        return true;
    }

/*
 * add increments the appropriate value in m_pList for a score value
 */

    public long add(final float _f) {
        int lValue = (int) (_f + 0.5);
//
//        // If buffer too small, double its size.
//        // Buffer must have empty bucket at end to output
//        // histogram correctly for reports.
//        if (lValue >= m_lLength - 1) {
//            int lLengthNew = 32;
//            while (lLengthNew - 1 <= lValue)
//                lLengthNew *= 2;
//
//            short[] pListNew = new short[lLengthNew];
//
//            if (m_pList != null) {
//                memcpy(pListNew, m_pList, m_lLength * sizeof(short));
//                delete[] m_pList;
//            }
//
//            m_pList = pListNew;
//            m_lLength = lLengthNew;
//        }
//
//        if (m_pList[lValue] < 0xFFFE) {
//            m_pList[lValue]++;
//        }
//        m_ulCount++;
        //       return lValue;
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }


/*
 * survival converts the scoring histogram in m_pList into a survival function
 */

    public void survival() {

        int a = length() - 1;
        long lSum = 0;
        long[] plValues = new long[length()];
        throw new UnsupportedOperationException("Fix This"); // ToDo
///*
//* first calculate the raw survival function
//*/
//        while (a > -1) {
//            lSum += m_pList[a];
//            plValues[a] = lSum;
//            a--;
//        }
//        a = 0;
///*
//* determine the value of the survival function that is 1/5 of the maximum
//*/
//        final long lPos = plValues[0] / 5;
//        while (a < m_lLength && plValues[a] > lPos) {
//            a++;
//        }
//        final long lMid = a;
//        a = m_lLength - 1;
//        while (a > -1 && plValues[a] == 0) {
//            a--;
//        }
//        lSum = 0;
//        long lValue = 0;
///*
//* remove potentially valid scores from the stochastic distribution
//*/
//        while (a > 0) {
//            if (plValues[a] == plValues[a - 1]
//                    && plValues[a] != plValues[0]
//                    && a > lMid) {
//                lSum = plValues[a];
//                lValue = plValues[a];
//                a--;
//                while (plValues[a] == lValue) {
//                    plValues[a] -= lSum;
//                    a--;
//                }
//            }
//            else {
//                plValues[a] -= lSum;
//                a--;
//            }
//        }
//        plValues[a] -= lSum;
//        a = 0;
///*
//* replace the scoring distribution with the survival function
//*/
//        m_vlSurvive.clear();
//        while (a < m_lLength) {
//            m_vlSurvive.push_back(plValues[a]);
//            a++;
//        }
//        delete plValues;
//        m_lSum = m_vlSurvive[0];

    }

    boolean clear_survive() {
        m_vlSurvive.clear();
        return true;
    }
/*
 * model performs a least-squares fit to the log score vs log survival function.
 * survival must be called prior to using model
 */

    boolean model() {
        survival();
        m_fA0 = (float) 3.5;
        m_fA1 = (float) -0.18;
///*
// * use default values if the statistics in the survival function is too meager
// */
//        if (length() == 0 || m_vlSurvive[0] < 200) {
//            return false;
//        }
//        float[] pfX = new float[length()];
//        float[] pfT = new float[length()];
//        long a = 1;
//        long lMaxLimit = (long) (0.5 + m_vlSurvive[0] / 2.0);
//        final long lMinLimit = 10;
///*
// * find non zero points to use for the fit
// */
//        a = 0;
//        while (a < m_lLength && m_vlSurvive[a] > lMaxLimit) {
//            a++;
//        }
//        long b = 0;
//        while (a < m_lLength - 1 && m_vlSurvive[a] > lMinLimit) {
//            pfX[b] = (float) a;
//            pfT[b] = (float) log10((double) m_vlSurvive[a]);
//            b++;
//            a++;
//        }
///*
// * calculate the fit
// */
//        double dSumX = 0.0;
//        double dSumT = 0.0;
//        double dSumXX = 0.0;
//        double dSumXT = 0.0;
//        long iMaxValue = 0;
//        double dMaxT = 0.0;
//        long iValues = b;
//        a = 0;
//        while (a < iValues) {
//            if (pfT[a] > dMaxT) {
//                dMaxT = pfT[a];
//                iMaxValue = a;
//            }
//            a++;
//        }
//        a = iMaxValue;
//        while (a < iValues) {
//            dSumX += pfX[a];
//            dSumXX += pfX[a] * pfX[a];
//            dSumXT += pfX[a] * pfT[a];
//            dSumT += pfT[a];
//            a++;
//        }
//        iValues -= iMaxValue;
//        double dDelta = (double) iValues * dSumXX - dSumX * dSumX;
//        if (dDelta == 0.0) {
//             return false;
//        }
//        m_fA0 = (float) ((dSumXX * dSumT - dSumX * dSumXT) / dDelta);
//        m_fA1 = (float) (((double) iValues * dSumXT - dSumX * dSumT) / dDelta);
//         m_vlSurvive.clear();
//        return true;
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }


}
/*
 * count_mhistogram stores information about the number of specific ion-types
 * discovered while modeling mspectrum object against a sequence collection.
 * model and survival are not used
 * starting in version 2004.04.01, this class is no longer a public mhistogram
 * this change was made to reduce memory usage, by eliminating the 
 * m_pSurvive array & reducing the size of the m_pList array
 */

