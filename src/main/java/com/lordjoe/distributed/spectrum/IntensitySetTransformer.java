package com.lordjoe.distributed.spectrum;

import org.systemsbiology.xtandem.*;

import java.util.*;

/**
 * com.lordjoe.distributed.spectrum.IntensitySetTransformer
 * User: Steve
 * Date: 1/6/2015
 */
public class IntensitySetTransformer {

    public static List<ISpectrumPeak> findRealPeaks(List<ISpectrumPeak> intensityReadings) {
        double minPeakDifference = findMinimumPeakDifference(intensityReadings);

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

        return new SpectrumPeak(sumMzIntensity /sumIntensity, sumIntensity );
    }

    private static List<List<ISpectrumPeak>> buildPeakGroups(final List<ISpectrumPeak> peaks) {
        List<List<ISpectrumPeak>> ret = new ArrayList<List<ISpectrumPeak>>();
        List<ISpectrumPeak> current = new ArrayList<ISpectrumPeak>();
        for (ISpectrumPeak peak : peaks) {
            if(true)
              throw new UnsupportedOperationException("Fix This"); // ToDo

         }
        return ret;
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
