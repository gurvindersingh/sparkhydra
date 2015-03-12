package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.ScoringUtilities
 * User: steven
 * Date: 1/14/11
 */
public class ScoringTestUtilities {
    public static final ScoringTestUtilities[] EMPTY_ARRAY = {};

    public static final int NUMBER_PEAKS = 20;
    public static final int MASS_START = 50;
    public static final int MASS_DEL = 30;

    public static IMeasuredSpectrum buildTestSpectrum(int nPeaks) {
        return new TestMeasuredSpectrum(nPeaks);
    }


    public static ITheoreticalSpectrum buildTheoreticalSpectrum(int nPeaks) {
        return new TestTheoreticalSpectrum(nPeaks);
    }


    public static class TestTheoreticalSpectrum implements ITheoreticalSpectrum {
        private final ITheoreticalPeak[] m_Peaks;
        private double m_SumIntensity;
        private double m_MaxIntensity;

        public TestTheoreticalSpectrum(int npeaks) {
            IonType[] ionTypes = IonType.values();
            int numberIonTypes = ionTypes.length;
            m_Peaks = new ITheoreticalPeak[npeaks];
            for (int i = 0; i < m_Peaks.length; i++) {
                double mass = MASS_START + i * MASS_DEL;
                m_Peaks[i] = new TheoreticalPeak(mass, 1, null, ionTypes[i % numberIonTypes]);

            }
            for (int i = 0; i < m_Peaks.length; i++) {
                ISpectrumPeak pk = m_Peaks[i];
                float peak = pk.getPeak();
                m_MaxIntensity = Math.max(peak, m_MaxIntensity);
                m_SumIntensity += peak;

            }

        }

        @Override
        public double getMaxIntensity() {
            return m_MaxIntensity;
        }

        /**
         * as stated
         *
         * @return
         */
        @Override
        public double getSumIntensity() {
            return m_SumIntensity;

        }


        /**
         * weak test for equality
         *
         * @param test !null test
         * @return true if equivalent
         */
        @Override
        public boolean equivalent(ITheoreticalSpectrum test) {
            if (!equivalent((ISpectrum) test))
                return false;
            if (!getPeptide().equivalent(test.getPeptide()))
                return false;
            if (!getSpectrumSet().equivalent(test.getSpectrumSet()))
                return false;
            final ISpectrumPeak[] s1 = getPeaks();
            final ISpectrumPeak[] s2 = test.getPeaks();
            if (s1.length != s2.length)
                return false;
            for (int i = 0; i < s2.length; i++) {
                ISpectrumPeak st1 = s1[i];
                ISpectrumPeak st2 = s2[i];
                if (st1.equivalent(st2))
                    return false;

            }
            return true;
        }


        /**
         * if the spectrum is not immutable build an immutable version
         * Otherwise return this
         *
         * @return as above
         */
        @Override
        public ITheoreticalSpectrum asImmutable() {
            return new ScoringSpectrum(this);
        }

        /**
         * if the spectrum is not  mutable build an  mutable version
         * Otherwise return this
         *
         * @return as above
         */
        @Override
        public MutableScoringSpectrum asMutable() {
            return new MutableScoringSpectrum(this);
        }


        /**
         * get the set of all spectra at any charge from this peptide
         *
         * @return !null set
         */
        @Override
        public ITheoreticalSpectrumSet getSpectrumSet() {
            if (true) throw new UnsupportedOperationException("Fix This");
            return null;
        }

        /**
         * return the assumed charge
         *
         * @return
         */
        @Override
        public int getCharge() {
            if (true) throw new UnsupportedOperationException("Fix This");
            return 0;
        }


        /**
         * return the generating peptide before fragmentation
         *
         * @return
         */
        @Override
        public IPolypeptide getPeptide() {
            return null;
        }

        /**
         * theoretical spectra have more and different information in their peaks
         *
         * @return
         */
        @Override
        public ITheoreticalPeak[] getTheoreticalPeaks() {
            return m_Peaks;
        }


        /**
         * get all peaks with non-zero intensity
         *
         * @return
         */
        @Override
        public ISpectrumPeak[] getNonZeroPeaks() {
            List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
            ISpectrumPeak[] peaks = getPeaks();
            for (int i = 0; i < peaks.length; i++) {
                ISpectrumPeak peak = peaks[i];
                if (peak.getPeak() > 0)
                    holder.add(peak);
            }
            ISpectrumPeak[] ret = new ISpectrumPeak[holder.size()];
            holder.toArray(ret);
            return ret;

        }


        /**
         * get the number of peaks without returning the peaks
         *
         * @return as above
         */
        @Override
        public int getPeaksCount() {
            return m_Peaks.length;
        }

        /**
         * spectrum - this might have been adjusted
         *
         * @return
         */
        @Override
        public ISpectrumPeak[] getPeaks() {
            return m_Peaks;
        }

        /**
         * return true if this and o are 'close enough'
         *
         * @param !null o
         * @return as above
         */
        @Override
        public boolean equivalent(ISpectrum o) {
            if (this == o)
                return true;
            ISpectrum realO = o;
            final ISpectrumPeak[] peaks1 = getPeaks();
            final ISpectrumPeak[] peaks2 = realO.getPeaks();
            if (peaks1.length != peaks2.length)
                return false;
            for (int i = 0; i < peaks2.length; i++) {
                ISpectrumPeak p1 = peaks1[i];
                ISpectrumPeak p2 = peaks2[i];
                if (!p1.equivalent(p2))
                    return false;


            }
            return true;
        }
    }


    public static class TestMeasuredSpectrum implements IMeasuredSpectrum {
        private final ISpectrumPeak[] m_Peaks;
        private double m_SumIntensity;
        private double m_MaxIntensity;

        public TestMeasuredSpectrum(int npeaks) {
            m_Peaks = new ISpectrumPeak[npeaks];
            for (int i = 0; i < m_Peaks.length; i++) {
                double mass = MASS_START + i * MASS_DEL;
                m_Peaks[i] = new SpectrumPeak(mass, 1);

            }
            for (int i = 0; i < m_Peaks.length; i++) {
                ISpectrumPeak pk = m_Peaks[i];
                float peak = pk.getPeak();
                m_MaxIntensity = Math.max(peak, m_MaxIntensity);
                m_SumIntensity += peak;

            }

        }

        @Override
        public double getMaxIntensity() {
            return m_MaxIntensity;
        }

        /**
         * as stated
         *
         * @return
         */
        @Override
        public double getSumIntensity() {
            return m_SumIntensity;

        }


        /**
         * make a form suitable to
         * 1) reconstruct the original given access to starting conditions
         *
         * @param adder !null where to put the data
         */
        @Override
        public void serializeAsString(final IXMLAppender adder) {
            adder.openTag("MeasuredSpectrum");
            adder.appendAttribute("PrecursorCharge", getPrecursorCharge());
            adder.appendAttribute("PrecursorMZ", XTandemUtilities.formatDouble(getPrecursorMassChargeRatio(), 3));
            adder.endTag();
            ISpectrumPeak[] peaks = getPeaks();
            for (int i = 0; i < peaks.length; i++) {
                adder.openTag("Peak");
                ISpectrumPeak peak = peaks[i];
                adder.appendAttribute("massChargeRatio", XTandemUtilities.formatDouble(peak.getMassChargeRatio(), 3));
                adder.appendAttribute("peak", XTandemUtilities.formatDouble(peak.getPeak(), 5));
                adder.closeTag("Peak");
            }
            adder.closeTag("MeasuredSpectrum");
        }


        /**
         * weak test for equality
         *
         * @param test !null test
         * @return true if equivalent
         */
        @Override
        public boolean equivalent(IMeasuredSpectrum test) {
            if (!equivalent((ISpectrum) test))
                return false;
            if (!XTandemUtilities.equivalentDouble(getPrecursorCharge(), test.getPrecursorCharge()))
                return false;
            if (!XTandemUtilities.equivalentDouble(getPrecursorMass(), test.getPrecursorMass()))
                return false;
            final ISpectrumPeak[] s1 = getPeaks();
            final ISpectrumPeak[] s2 = test.getPeaks();
            if (s1.length != s2.length)
                return false;
            for (int i = 0; i < s2.length; i++) {
                ISpectrumPeak st1 = s1[i];
                ISpectrumPeak st2 = s2[i];
                if (st1.equivalent(st2))
                    return false;

            }
            return true;
        }


        /**
         * if the spectrum is not  mutable build an  mutable version
         * Otherwise return this
         *
         * @return as above
         */
        @Override
        public MutableMeasuredSpectrum asMmutable() {
            return new MutableMeasuredSpectrum(this);
        }

        /**
         * get run identifier
         *
         * @return
         */
        @Override
        public String getId() {
            return "1234";
        }

        /**
         * get index - return 0 if unknown
         *
         * @return as above
         */
        @Override
        public int getIndex() {
            return 0;
        }

        /**
         * return true if the spectrum is immutable
         *
         * @return
         */
        @Override
        public boolean isImmutable() {
            if (true) throw new UnsupportedOperationException("Fix This");
            return false;
        }

        /**
         * get the mz of the spectrum precursor
         *
         * @return as above
         */
        @Override
        public double getPrecursorMassChargeRatio() {
            return 0;
        }

        /**
         * if the spectrum is not immutable build an immutable version
         * Otherwise return this
         *
         * @return as above
         */
        @Override
        public IMeasuredSpectrum asImmutable() {
            if (true) throw new UnsupportedOperationException("Fix This");
            return null;
        }

        /**
         * Mass spec characteristics
         *
         * @return
         */
        @Override
        public ISpectralScan getScanData() {
            return null;
        }

        /**
         * get the charge of the spectrum precursor
         *
         * @return as above
         */
        @Override
        public int getPrecursorCharge() {
            if (true) throw new UnsupportedOperationException("Fix This");
            return 0;
        }

        /**
         * get the mass of the spectrum precursor
         *
         * @return as above
         */
        @Override
        public double getPrecursorMass() {
            if (true) throw new UnsupportedOperationException("Fix This");
            return 0;
        }

        /**
         * get the number of peaks without returning the peaks
         *
         * @return as above
         */
        @Override
        public int getPeaksCount() {
            return m_Peaks.length;
        }

        /**
         * spectrum - this might have been adjusted
         *
         * @return
         */
        @Override
        public ISpectrumPeak[] getPeaks() {
            return m_Peaks;
        }

        /**
         * get all peaks with non-zero intensity
         *
         * @return
         */
        @Override
        public ISpectrumPeak[] getNonZeroPeaks() {
            List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();
            ISpectrumPeak[] peaks = getPeaks();
            for (int i = 0; i < peaks.length; i++) {
                ISpectrumPeak peak = peaks[i];
                if (peak.getPeak() > 0)
                    holder.add(peak);
            }
            ISpectrumPeak[] ret = new ISpectrumPeak[holder.size()];
            holder.toArray(ret);
            return ret;

        }


        /**
         * return true if this and o are 'close enough'
         *
         * @param !null o
         * @return as above
         */
        @Override
        public boolean equivalent(ISpectrum o) {
            if (this == o)
                return true;
            ISpectrum realO = o;
            final ISpectrumPeak[] peaks1 = getPeaks();
            final ISpectrumPeak[] peaks2 = realO.getPeaks();
            if (peaks1.length != peaks2.length)
                return false;
            for (int i = 0; i < peaks2.length; i++) {
                ISpectrumPeak p1 = peaks1[i];
                ISpectrumPeak p2 = peaks2[i];
                if (!p1.equivalent(p2))
                    return false;


            }
            return true;
        }
    }

}
