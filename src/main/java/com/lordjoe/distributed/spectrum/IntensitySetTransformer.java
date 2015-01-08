package com.lordjoe.distributed.spectrum;

import org.systemsbiology.xtandem.*;

import java.util.*;

/**
 * com.lordjoe.distributed.spectrum.IntensitySetTransformer
 * User: Steve
 * Date: 1/6/2015
 */
public class IntensitySetTransformer {

    public static final double MIN_PEAK_DIFFERENCE = 0.05;

    public static IMeasuredSpectrum findSpectrum(IMeasuredSpectrum inp) {
        List<ISpectrumPeak> peaks = Arrays.asList(inp.getPeaks());
        if (isPeaksNormalized(peaks))
            return inp;
        List<ISpectrumPeak> realPeaks = findRealPeaks(peaks);
        RawPeptideScan ret = new RawPeptideScan(inp.getId(), "");
        IScanPrecursorMZ mz = new ScanPrecursorMz(inp.getSumIntensity(), inp.getPrecursorCharge(), inp.getPrecursorMassChargeRatio(),
                FragmentationMethod.CID);
        ret.setPrecursorMz(mz);

        ret.setPeaks(realPeaks.toArray(new ISpectrumPeak[realPeaks.size()]));
        return ret;
    }

    private static boolean isPeaksNormalized(final List<ISpectrumPeak> pPeaks) {
        double lastMz = 0;
        for (ISpectrumPeak peak : pPeaks) {
            if (peak.getPeak() == 0)
                return false;
            double mz = peak.getMassChargeRatio();
            double del = mz - lastMz;
            if (del < MIN_PEAK_DIFFERENCE)
                return false;
            lastMz = mz;
        }
        return true;
    }


    public static List<ISpectrumPeak> findRealPeaks(List<ISpectrumPeak> intensityReadings) {

        if (isPeaksNormalized(intensityReadings))
               return intensityReadings;

         List<List<ISpectrumPeak>> peakGroups = buildPeakGroups(intensityReadings);

        List<ISpectrumPeak> ret = new ArrayList<ISpectrumPeak>();
        for (List<ISpectrumPeak> peakGroup : peakGroups) {
            ISpectrumPeak peak = findSinglePeak(peakGroup);
            ret.add(peak);
        }
        return ret;
    }

    private static ISpectrumPeak findSinglePeak(final List<ISpectrumPeak> peaks) {
        double sumIntensity = 0;
        double sumMzIntensity = 0;
        for (ISpectrumPeak peak : peaks) {
            sumIntensity += peak.getPeak();
            sumMzIntensity += peak.getPeak() * peak.getMassChargeRatio();
        }

        return new SpectrumPeak(sumMzIntensity / sumIntensity, sumIntensity);
    }

    private static List<List<ISpectrumPeak>> buildPeakGroups(final List<ISpectrumPeak> peaks) {
        double minPeakDifference = findMinimumPeakDifference(peaks);
        List<List<ISpectrumPeak>> ret = new ArrayList<List<ISpectrumPeak>>();
        List<ISpectrumPeak> current = new ArrayList<ISpectrumPeak>();
        boolean intensitySeen = false;
        double lastMz = 0;
        int index = 0;
        for (ISpectrumPeak peak : peaks) {
            if (isEndPeak(peak, lastMz, minPeakDifference)) {
                if (intensitySeen) {
                    ret.add(new ArrayList<ISpectrumPeak>(current));
                    current.clear();
                    intensitySeen = false;
                }
            }
            else {
                intensitySeen = true;
                current.add(peak);
            }
            if(ret.size() > 8)
                XTandemUtilities.breakHere();
            lastMz = peak.getMassChargeRatio();
            index++;
        }
        if (intensitySeen)
            ret.add(new ArrayList<ISpectrumPeak>(current));
        return ret;
    }

    private static boolean isEndPeak(final ISpectrumPeak peak, double lastMz, double minPeakDifference) {
        if (peak.getPeak() == 0)
            return true;
        // too widely separated
    //    if (peak.getMassChargeRatio() - lastMz > 5 * minPeakDifference)
    //        return true;
        return false;
    }

    private static double findMinimumPeakDifference(final List<ISpectrumPeak> peaks) {
        double ret = Double.MAX_VALUE;
        double lastMz = 0;
        for (ISpectrumPeak peak : peaks) {
            double mz = peak.getMassChargeRatio();
            double diff = mz - lastMz;
            ret = Math.min(ret, diff);
            lastMz = mz;
        }
        return ret;
    }

}
