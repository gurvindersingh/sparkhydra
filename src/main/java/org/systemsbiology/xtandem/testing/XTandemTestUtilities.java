package org.systemsbiology.xtandem.testing;

import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;

/**
 * org.systemsbiology.xtandem.testing.XTandemTestUtilities
 * User: steven
 * Date: 3/16/11
 */
public class XTandemTestUtilities {
    public static final XTandemTestUtilities[] EMPTY_ARRAY = {};

    private static double gMassChargeRatioTolerance = 0.01;
    private static double gPeakTolerance = 0.002;

    public static double getMassChargeRatioTolerance() {
        return gMassChargeRatioTolerance;
    }

    public static void setMassChargeRatioTolerance(final double pMassChargeRatioTolerance) {
        gMassChargeRatioTolerance = pMassChargeRatioTolerance;
    }

    public static double getPeakTolerance() {
        return gPeakTolerance;
    }

    public static void setPeakTolerance(final double pPeakTolerance) {
        gPeakTolerance = pPeakTolerance;
    }

    public static boolean equivalent(IMeasuredSpectrum m1,IMeasuredSpectrum m2)  {
        if(m1.getId() != m2.getId())
            return false;
        return  equivalentSpectra(m1,m2);
    }


    public static boolean equivalentSpectra(ISpectrum m1,ISpectrum m2)  {
        ISpectrumPeak[] peaks1 = m1.getPeaks();
        ISpectrumPeak[] peaks2 = m2.getPeaks();
        if(peaks1.length != peaks2.length)  {
            listMissingPeaks(peaks1,peaks2);
            return false;
        }
        for (int i = 0; i < peaks2.length; i++) {
            ISpectrumPeak sp1 = peaks1[i];
            ISpectrumPeak sp2 = peaks2[i];
            if(!equivalentSpectrumPeaks(sp1,sp2)) {
                equivalentSpectrumPeaks(sp1,sp2); // break here
                return false;
            }
        }
        return true;
    }

    private static void listMissingPeaks2(final ISpectrumPeak[] pPeaks1, final ISpectrumPeak[] pPeaks2) {
        int index1 = 0;
        int index2 = 0;
        boolean missing1 =   pPeaks1.length  < pPeaks2.length;
        while(index1 < pPeaks1.length && index2 < pPeaks2.length)  {
            int mz1 = (int)(pPeaks1[index1].getMassChargeRatio() + 0.5);
            int mz2 = (int)(pPeaks2[index2].getMassChargeRatio() + 0.5);
           if( mz1 == mz2)  {
               index1++;
               index2++;
               continue;
           }
           if(mz1 < mz2)  {
               XMLUtilities.outputLine("Theirs Missing " + mz2);
               index1++;
           }
            else {
               XMLUtilities.outputLine("Mine Missing " + mz1);
                index2++;

           }
        }


    }
    private static void listMissingPeaks(final ISpectrumPeak[] pPeaks1, final ISpectrumPeak[] pPeaks2) {
         ISpectrumPeak[] masses1 = populateMassArray(  pPeaks1);
        ISpectrumPeak[] masses2 = populateMassArray(  pPeaks2);
        for (int i = 0; i < masses2.length; i++) {
            ISpectrumPeak sp1 = masses1[i];
            ISpectrumPeak sp2 = masses2[i];
            if(sp1 == null)  {
                if(sp2 != null) {
                    XMLUtilities.outputLine("Theirs Missing " + sp2);
                    if( masses1[i -1] == null && masses1[i +1] == null)
                        XMLUtilities.outputLine("Really missing " + sp2);
                }
                else {
                    continue;
                }
            }
            if(sp2 == null)  {
                if(sp1 != null) {
                    XMLUtilities.outputLine("Mine Missing " + sp1);
                    if( masses2[i -1] == null && masses2[i +1] == null)
                        XMLUtilities.outputLine("Really missing " + sp1);
                }
                else {
                    continue;
                }
            }

        }

    }

    private  static ISpectrumPeak[] populateMassArray(final ISpectrumPeak[] pPeaks1)    {
        ISpectrumPeak[] masses1 = new  ISpectrumPeak[TandemKScoringAlgorithm.MAX_MASS];
          for (int i = 0; i < pPeaks1.length; i++) {
            ISpectrumPeak sp = pPeaks1[i];
            int mz1 = (int)(sp.getMassChargeRatio() /*+ 0.5 */);
            if(mz1 < masses1.length)
                masses1[mz1]  = sp;
        }
        return masses1;
    }

    public static boolean equivalentSpectrumPeaks(ISpectrumPeak m1,ISpectrumPeak m2)  {

        if (!testMassTolerance(m1, m2))
            return false;
        double  del = Math.abs( m1.getPeak() - m2.getPeak()  );
        if(del > getPeakTolerance())
            return false;
        if(m1 instanceof ITheoreticalPeak)  {
           return equivalentTheoreticalSpectrumPeaks((ITheoreticalPeak)m1,(ITheoreticalPeak)m2);
        }
        return true; // all ok
     }

    private static boolean testMassTolerance(final ISpectrumPeak m1, final ISpectrumPeak m2) {
        // sometimes this is just an int
        if(XTandemUtilities.isInteger(m1.getMassChargeRatio()) || XTandemUtilities.isInteger(m2.getMassChargeRatio())) {
            if(m1.getMassChargeRatio() == m2.getMassChargeRatio())
                return true;
            int i1 = (int) (TandemKScoringAlgorithm.massChargeRatioAsInt(m1) );
            int i2 = (int) (TandemKScoringAlgorithm.massChargeRatioAsInt(m2) );
            if (i1 == i2)
                return true;
              i1 = (int) (m1.getMassChargeRatio() );
              i2 = (int) (m2.getMassChargeRatio() );
            if( i1 == i2);
                return true;
        }
        double del1 = Math.abs( m1.getMassChargeRatio() - m2.getMassChargeRatio()  );
        if(del1 < getMassChargeRatioTolerance())
            return true;
        return false;
    }

    protected static boolean equivalentTheoreticalSpectrumPeaks(ITheoreticalPeak m1,ITheoreticalPeak m2)  {
         if(m1.getType()!= m2.getType())
            return false;
        if(!equivalentPeptide(m1.getPeptide(),m2.getPeptide()))
            return false;

        return true; // all ok
     }

    public static boolean equivalentPeptide(IPolypeptide m1,IPolypeptide m2)  {
        if(!m1.getSequence().equals(m2.getSequence()))
           return false;
        if(!m1.getId().equals(m2.getId()))
           return false;

         return true; // all ok
      }


}
