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
    private static final double MINIMUM_MZ = 150;
    private static final int MAX_PEAKS = 100;

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

        // no zero peaks
        if (isPeaksNormalized(intensityReadings))
            return intensityReadings;


        double totalIntensity = 0;
        List<ISpectrumPeak> ret = new ArrayList<ISpectrumPeak>();

        for (int i = 0; i < intensityReadings.size(); i++) {
            i = extractNextPeak(intensityReadings, ret, i);
            if (i == -1)
                break; // we gave up

        }

        Collections.sort(ret, SpectrumPeak.INTENSITY_COMPARATOR);

        List<ISpectrumPeak> pruned = new ArrayList<ISpectrumPeak>();
        for (ISpectrumPeak peak : ret) {
            if (peak.getMassChargeRatio() < MINIMUM_MZ)
                continue;
            pruned.add(peak);

            if (pruned.size() >= MAX_PEAKS)
                break;
        }
        Collections.sort(pruned);

        return pruned;
    }

    public  static int extractNextPeak(final List<ISpectrumPeak> peaks, final List<ISpectrumPeak> holder, int index) {
        double sumIntensity = 0;
        double sumMzIntensity = 0;
        double lastMz = 0;
        if (holder.size() > 0)
            lastMz = holder.get(holder.size() - 1).getMassChargeRatio();

         for (; index < peaks.size(); index++) {
            ISpectrumPeak peak = peaks.get(index++);
            float intensity = peak.getPeak();
            double mz = peak.getMassChargeRatio();
            if (mz < lastMz - 10)
                return -1; // this is bad and should be ignored
            if (intensity == 0) {
                if (sumIntensity > 0)
                    break;
            }
            else {
                sumIntensity += intensity;
                sumMzIntensity += intensity * mz;
            }
        }

        ISpectrumPeak added = new SpectrumPeak(sumMzIntensity / sumIntensity, sumIntensity);
        holder.add(added);
        return index;

    }

    public static List<ISpectrumPeak> findRealPeaksOld(List<ISpectrumPeak> intensityReadings) {
        // no zero peaks
        if (isPeaksNormalized(intensityReadings))
            return intensityReadings;

        List<List<ISpectrumPeak>> peakGroups = buildPeakGroups(intensityReadings);

        double totalIntensity = 0;
        List<ISpectrumPeak> ret = new ArrayList<ISpectrumPeak>();
        for (List<ISpectrumPeak> peakGroup : peakGroups) {
            ISpectrumPeak peak = findSinglePeak(peakGroup);
            totalIntensity += peak.getPeak();
            ret.add(peak);
        }

        Collections.sort(ret, SpectrumPeak.INTENSITY_COMPARATOR);

        List<ISpectrumPeak> pruned = new ArrayList<ISpectrumPeak>();
        for (ISpectrumPeak peak : ret) {
            if (peak.getMassChargeRatio() < MINIMUM_MZ)
                continue;
            pruned.add(peak);

            if (pruned.size() >= MAX_PEAKS)
                break;
        }
        Collections.sort(ret);

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
            if (ret.size() > 8)
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
            if (diff > 0)
                ret = Math.min(ret, diff);
            else
                diff = mz - lastMz; // break here

            lastMz = mz;
        }
        return ret;
    }

}
