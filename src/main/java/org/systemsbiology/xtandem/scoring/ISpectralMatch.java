package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.ISpectralMatch
 * User: steven
 * Date: 1/28/11
 */
public interface ISpectralMatch extends IonTypeScorer,IAddable<ISpectralMatch>,IEquivalent<ISpectralMatch>,Comparable<ISpectralMatch>
{
    public static final ISpectralMatch[] EMPTY_ARRAY = {};

    /**
     * ion usage used to prevent
     * @return  usage structure
     */
     public SpectralPeakUsage getUsage();



    /**
      * make a form suitable to
      *    1) reconstruct the original given access to starting conditions
      *
      * @param adder !null where to put the data
      */
     public void serializeAsString(IXMLAppender adder);


    public boolean isActive();

     public void setActive(final boolean pActive);

    public IPolypeptide getPeptide();

    public ITheoreticalSpectrumSet getTheory();

     public IMeasuredSpectrum getMeasured();

     public double getScore();

     public double getHyperScore();



    public static final Comparator<ISpectralMatch> SCORE_COMPARATOR = new SpectralMatchScoreComparator();
    public static class SpectralMatchScoreComparator implements Comparator<ISpectralMatch> {
         private SpectralMatchScoreComparator() {
         }

         @Override
         public int compare(ISpectralMatch o1, ISpectralMatch o2) {
             if(o1 == o2)
                 return 0;
             double hs1 = o1.getHyperScore();
             double hs2 = o2.getHyperScore();
             if(hs1 == hs2)
                 return 0;
             return hs1 > hs2 ? -1 : 1;
         }
     }


}
