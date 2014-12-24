package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.ScoringUtilities
 *
 * @author Steve Lewis
 * @date Jan 17, 2011
 */
public class ScoringUtilities
{
    public static ScoringUtilities[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ScoringUtilities.class;

    public static final boolean SHOW_SCORING = false;

    /**
     * pass peaks less then the given mass
     */
    private static class MassLessThanFilter implements IPeakFilter
    {
        private final double m_Mass;

        private MassLessThanFilter(double pMass)
        {
            m_Mass = pMass;
        }

        /**
         * decide whether to keep a peal or not
         *
         * @param peak      !null peak
         * @param addedData any additional data
         * @return true if the peak is to be used
         */
        @Override
        public boolean isPeakRetained(ISpectrumPeak peak, Object... addedData)
        {
            return peak.getMassChargeRatio() < m_Mass;
        }
    } // end class MassLessThanFilter


    /**
     * pass peaks less then the given mass
     */
    private static class MassGreaterThanFilter implements IPeakFilter
    {
        private final double m_Mass;

        private MassGreaterThanFilter(double pMass)
        {
            m_Mass = pMass;
        }

        /**
         * decide whether to keep a peal or not
         *
         * @param peak      !null peak
         * @param addedData any additional data
         * @return true if the peak is to be used
         */
        @Override
        public boolean isPeakRetained(ISpectrumPeak peak, Object... addedData)
        {
            return peak.getMassChargeRatio() > m_Mass;
        }
    } // end class MassGreaterThanFilter


    public static IPeakFilter buildMassLessThanFilter(double maxMass)
    {
        return new MassLessThanFilter(maxMass);
    }


    public static IPeakFilter buildMassGreaterThanFilter(double maxMass)
    {
        return new MassGreaterThanFilter(maxMass);
    }


    /**
     * build a subspectrum holding the peaks of the original
     *
     * @param spectrum  !null spectrum
     * @param filter    !null filter
     * @param addedData any added data the filter may need
     * @return !null spectrum filtered
     */
    public static IMeasuredSpectrum applyFilter(IMeasuredSpectrum spectrum, IPeakFilter filter,
                                                Object... addedData)
    {
        int precursorCharge = spectrum.getPrecursorCharge();
        final double mass = spectrum.getPrecursorMass();
        ISpectralScan scan = spectrum.getScanData();
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        for (ISpectrumPeak test : spectrum.getPeaks()) {
            if (filter.isPeakRetained(test, addedData))
                holder.add(test);
        }
        ISpectrumPeak[] peaks = new ISpectrumPeak[holder.size()];
        holder.toArray(peaks);

        return new ScoringMeasuredSpectrum(precursorCharge,mass, scan, peaks);
    }


    /**
     * build a subspectrum holding the peaks of the original
     *
     * @param spectrum  !null spectrum
     * @param filter    !null filter
     * @param addedData any added data the filter may need
     * @return !null spectrum filtered
     */
    public static void applyFilter(MutableMeasuredSpectrum spectrum, IPeakFilter filter,
                                                Object... addedData)
    {
        int precursorCharge = spectrum.getPrecursorCharge();
        final double mass = spectrum.getPrecursorMass();
        ISpectralScan scan = spectrum.getScanData();
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        for (ISpectrumPeak test : spectrum.getPeaks()) {
            if (filter.isPeakRetained(test, addedData))
                holder.add(test);
        }
        ISpectrumPeak[] peaks = new ISpectrumPeak[holder.size()];
        holder.toArray(peaks);

        spectrum.setPeaks(peaks);
    }


    /**
     * build a subspectrum holding the peaks of the original
     *
     * @param spectrum  !null spectrum
     * @param filter    !null filter
     * @param addedData any added data the filter may need
     * @return !null spectrum filtered
     */
    public static IMeasuredSpectrum applyConditioner(IMeasuredSpectrum spectrum,
                                                     IPeakConditioner filter, Object... addedData)
    {
        int precursorCharge = spectrum.getPrecursorCharge();
        final double mass = spectrum.getPrecursorMass();
        ISpectralScan scan = spectrum.getScanData();
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        for (ISpectrumPeak test : spectrum.getPeaks()) {
            final ISpectrumPeak pk = filter.condition(test, addedData);
            if (pk != null)
                holder.add(pk);
        }
        ISpectrumPeak[] peaks = new ISpectrumPeak[holder.size()];
        holder.toArray(peaks);

        return new ScoringMeasuredSpectrum(precursorCharge,mass, scan, peaks);
    }

    /**
     * build za modified theoretical spectrum
     *
     * @param spectrum  !null spectrum
     * @param filter    !null filter
     * @param addedData any added data the filter may need
     * @return !null spectrum conditioned
     */
    public static ITheoreticalSpectrum applyConditioner(ITheoreticalSpectrum spectrum,
                                                     ITheoreticalPeakConditioner filter, Object... addedData)
    {
        Integer charge = spectrum.getCharge();
        String parentSequence = spectrum.getPeptide().getSequence();
        ITheoreticalSpectrumSet spectrumSet = spectrum.getSpectrumSet();
        List<ITheoreticalPeak> holder = new ArrayList<ITheoreticalPeak>();
        for (ITheoreticalPeak test : spectrum.getTheoreticalPeaks()) {
            if(test.getPeptide().getSequence().length() >= parentSequence.length())
                continue;
            final ITheoreticalPeak pk = filter.condition(test, addedData);
            if (pk != null)
                holder.add(pk);
        }
        ITheoreticalPeak[] peaks = new ITheoreticalPeak[holder.size()];
        holder.toArray(peaks);
        Arrays.sort(peaks);
          return new ScoringSpectrum(charge,spectrumSet, peaks);
    }

    public static class AdjustTheoreticalWeights implements ITheoreticalPeakConditioner
    {
        public AdjustTheoreticalWeights() {
        }

        /**
         * return a modified peak
         *
         * @param peak      !null peak
         * @param addedData any additional data
         * @return possibly null conditioned peak - null says ignore
         */
        @Override
        public ITheoreticalPeak condition(final ITheoreticalPeak peak, final Object... addedData) {
            double mass = peak.getMassChargeRatio();
            float weight = peak.getPeak();
            IonType type = peak.getType();
            IPolypeptide peptide = peak.getPeptide();
            String sequence = peptide.getSequence();
            char first =  sequence.charAt(0);

            return new TheoreticalPeak(mass, weight,peptide, type);
        }
    }

    /**
     * build a new spectrum by multipying each peak by a normalizing factor
     *
     * @param spectrum !null original spectrum
     * @param factor   factor
     * @return !null normalized peaks
     */
    public static void getMaxNPeaks(MutableMeasuredSpectrum spectrum, int maxAllowedPeaks)
    {
        final ISpectrumPeak[] spectrumPeaks = spectrum.getPeaks();
        if(maxAllowedPeaks >= spectrumPeaks.length)
            return ;
        int precursorCharge = spectrum.getPrecursorCharge();
        final double mass = spectrum.getPrecursorMass();
        ISpectralScan scan = spectrum.getScanData();
        Arrays.sort(spectrumPeaks,PeakIntensityComparatorINSTANCE);
        List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
        int index = spectrumPeaks.length - 1;
        while(holder.size() < maxAllowedPeaks)  {
            holder.add(spectrumPeaks[index--]); // starting with the highest add peaks
        }

        ISpectrumPeak[] newPeaks = new ISpectrumPeak[holder.size()];
        holder.toArray(newPeaks);

        spectrum.setPeaks(newPeaks); 
 
    }


    public static int compareLongs(int f1,int f2)
    {
        int del = f1 - f2;
        if(del == 0)
            return 0;
        return del > 0 ? 1 : -1;
    }


    public static int compareInts(int f1,int f2)
    {
        int del = f1 - f2;
        if(del == 0)
            return 0;
        return del > 0 ? 1 : -1;
    }


    public static int compareFloats(float f1,float f2)
    {
        float del = f1 - f2;
        if(del == 0)
            return 0;
        return del > 0 ? 1 : -1;
    }


    public static int compareDoubles(double f1,double f2)
    {
        double del = f1 - f2;
        if(del == 0)
            return 0;
        return del > 0 ? 1 : -1;
    }

    /**
     * use to sort peaks by intensity
     */
    public static  Comparator<ISpectrumPeak> PeakIntensityComparatorINSTANCE = new PeakIntensityComparator();

    private static class PeakIntensityComparator implements Comparator<ISpectrumPeak>
    {
        private PeakIntensityComparator()
        {
        }

        @Override
        public int compare(ISpectrumPeak o1, ISpectrumPeak o2)
        {
            int ret = compareFloats(o1.getPeak(),o2.getPeak());
            if(ret == 0)
                return compareDoubles(o1.getMassChargeRatio(),o2.getMassChargeRatio());
             else
                return ret;
        }
    }
    /**
     * use to sort peaks by intensity
     */
    public static  Comparator<ISpectrumPeak> PeakIntensityComparatorHiToLowINSTANCE = new PeakIntensityHiToLowComparator();

    private static class PeakIntensityHiToLowComparator implements Comparator<ISpectrumPeak>
    {
        private PeakIntensityHiToLowComparator()
        {
        }

        @Override
        public int compare(ISpectrumPeak o1, ISpectrumPeak o2)
        {
            int ret = -compareFloats(o1.getPeak(),o2.getPeak());
            if(ret == 0)
                return compareDoubles(o1.getMassChargeRatio(),o2.getMassChargeRatio());
             else
                return ret;
        }
    }

    /**
     * use to sort peaks by mass
     */
    public static  Comparator<ISpectrumPeak> PeakMassComparatorINSTANCE = new PeakMassComparator();

    private static class PeakMassComparator implements Comparator<ISpectrumPeak>
    {
        private PeakMassComparator()
        {
        }

        @Override
        public int compare(ISpectrumPeak o1, ISpectrumPeak o2)
        {
            int ret = compareDoubles(o1.getMassChargeRatio(),o2.getMassChargeRatio());
            if(ret == 0)
                return compareFloats(o1.getPeak(),o2.getPeak());
             else
                return ret;
        }
    }


    /**
     * build a new spectrum by multipying each peak by a normalizing factor
     *
     * @param spectrum !null original spectrum
     * @param factor   factor
     * @return !null normalized peaks
     */
    public static IMeasuredSpectrum normalizePeaks(IMeasuredSpectrum spectrum, float factor)
    {
        return applyConditioner(spectrum, MultiplyPeaksINSTANCE, new Float(factor));
    }

    private static MultiplyPeaks MultiplyPeaksINSTANCE = new MultiplyPeaks();

    /**
     * multiply peak by a factor passed into the call
     */
    private static class MultiplyPeaks implements IPeakConditioner
    {

        private MultiplyPeaks()
        {
        }

        /**
         * decide whether to keep a peal or not
         *
         * @param peak      !null peak
         * @param addedData any additional data
         * @return possibly null conditioned peak - null says ignore
         */
        @Override
        public ISpectrumPeak condition(ISpectrumPeak peak, Object... addedData)
        {
            return new SpectrumPeak(peak.getMassChargeRatio(), peak.getPeak() * (Float) addedData[0]);
        }
    } // end class MassGreaterThanFilter

}
