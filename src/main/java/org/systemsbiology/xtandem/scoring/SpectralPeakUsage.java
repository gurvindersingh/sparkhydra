package org.systemsbiology.xtandem.scoring;

import com.lordjoe.utilities.*;
import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.SpectralPeakUsage
 * User: Steve
 * Date: 9/4/11
 */
public class SpectralPeakUsage implements IEquivalent<SpectralPeakUsage>,IAddable<SpectralPeakUsage>  {
    public static final SpectralPeakUsage[] EMPTY_ARRAY = {};

    public static SpectralPeakUsage deserializeUsage(String s ) {
        s = s.trim();
        s = s.replace("<usage  >","");
        s = s.replace("</usage>","");
         SpectralPeakUsage ret = new SpectralPeakUsage( );
         ret.setDataFromBin64(s);
        return ret;
    }

    public static final String TAG = "usage";

    private final IMZToInteger m_Converter;
    private final Map<Integer, PeakUsage> m_Usage = new HashMap<Integer, PeakUsage>();

    public SpectralPeakUsage( ) {
       this(XTandemUtilities.getDefaultConverter());
    }

    public boolean isEmpty() {
        return m_Usage.isEmpty();
    }

    public SpectralPeakUsage(final IMZToInteger pConverter) {
        m_Converter = pConverter;
    }

    public IMZToInteger getConverter() {
        return m_Converter;
    }

    /**
     * add usage - mz and score to usage object
     *
     * @param mz            mz value
     * @param originalAdded usage
     * @return actual usage
     */
    public double getAddedAfterUsageCorrection(double mz, double originalAdded) {
        int imz = getConverter().asInteger(mz);
        return getAddedAfterIntegerUsageCorrection(mz, originalAdded, imz);
    }

    public double getAddedAfterIntegerUsageCorrection(final double mz, final double originalAdded, final int pImz) {
        PeakUsage usage = m_Usage.get(pImz);
        if (usage == null) {  // no usage use all
            usage = new PeakUsage(mz, originalAdded);
            m_Usage.put(pImz, usage);
            return originalAdded;
        }
        else {    // in use so only add excess scoring
            double ret = originalAdded - usage.getUsed();
            if (ret <= 0)
                return 0;
            usage.addUsed(ret);
            return ret;
        }
    }

    /**
      * combine two scores
      *
      * @param added
      */
     public void addTo(SpectralPeakUsage added) {
         PeakUsage[] usages = added.getUsages();
         for (int i = 0; i < usages.length; i++) {
             PeakUsage usage = usages[i];
             getAddedAfterUsageCorrection(usage.getMZ(),usage.getUsed()); //
         }
     }

    /**
     * add usage - mz and score to usage object
     *
     * @param mz            mz value
     * @param originalAdded usage
     * @return actual usage
     */
    public double getUsage(double mz) {
        int imz = getConverter().asInteger(mz);
        PeakUsage usage = m_Usage.get(imz);
        if (usage == null) {  // no usage use all
            return 0;
        }
        else {    // in use so only add excess scoring
            return usage.getUsed();
        }
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        PeakUsage[] peakUsages = getUsages();
        for (int i = 0; i < peakUsages.length; i++) {
            PeakUsage peakUsage = peakUsages[i];
            if (sb.length() > 0)
                sb.append(",");
            sb.append(getConverter().asInteger(peakUsage.getMZ()) + ":" +
                    XTandemUtilities.formatDouble(peakUsage.getUsed(), 4));
        }
        return sb.toString();

    }

    protected PeakUsage[] getUsages() {
        Collection<PeakUsage> values = m_Usage.values();
        PeakUsage[] peakUsages = values.toArray(new PeakUsage[values.size()]);
        Arrays.sort(peakUsages);
        return peakUsages;
    }

    protected String getDataAsBin64() {
        if (m_Usage.isEmpty())
            return "";
        float[] values = new float[m_Usage.size() * 2];
        PeakUsage[] peakUsages = getUsages();
        for (int i = 0; i < peakUsages.length; i++) {
            PeakUsage peakUsage = peakUsages[i];
            values[2 * i] = (float) peakUsage.getMZ();
            values[2 * i + 1] = (float) peakUsage.getUsed();
        }
        return Base64Float.encodeFloatsAsString(values);
    }


    protected void setDataFromBin64(String data) {
        if (data.length() == 0)
            return;
        float[] fdata = Base64Float.decodeFloats(data);
        for (int i = 0; i < fdata.length; i += 2) {
             getAddedAfterUsageCorrection(fdata[i],fdata[i + 1]);
        }
     }


    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    public void serializeAsString(IXMLAppender adder) {
        String tag = TAG;
        adder.openEmptyTag(tag);
        PeakUsage[] usages = getUsages();
        float[] data = new float[usages.length * 2];
        for (int i = 0; i < usages.length; i++) {
            PeakUsage usage = usages[i];
            data[ 2 * i]     = (float)usage.getMZ();
            data[ 2 * i + 1] = (float)usage.getUsed();
              
        }
        String name = Base64Float.encodeFloatsAsString(data);
        adder.appendText(name);
        adder.closeTag(tag);
    }

    /**
     * return true if this and o are 'close enough'
     *
     * @param o !null test object
     * @return as above
     */
    @Override
    public boolean equivalent(final SpectralPeakUsage o) {
        if (o == this)
            return true;
        PeakUsage[] usages = getUsages();
        PeakUsage[] ousages = o.getUsages();
        if (usages.length != ousages.length)
            return false;
        for (int i = 0; i < ousages.length; i++) {
            PeakUsage ousage = ousages[i];
            PeakUsage usage = usages[i];
            if (!usage.equivalent(ousage))
                return false;
        }
        return true;
    }

    private class PeakUsage implements IEquivalent<PeakUsage>, Comparable<PeakUsage> {
        private final double m_MZ;
        private double m_Used;

        public PeakUsage(final double pMZ, final double pUsed) {
            m_MZ = pMZ;
            m_Used = pUsed;
        }

        public PeakUsage(final double pMZ) {
            this(pMZ, 0);
        }

        public double getMZ() {
            return m_MZ;
        }

        public double getUsed() {
            return m_Used;
        }

        public void addUsed(double added) {
            m_Used += added;
        }


        @Override
        public String toString() {
            return getMZ() + ":" + getUsed();
        }

        /**
         * order by mz values - should NEVER happen with 2 with the same
         *
         * @param o
         * @return
         */
        @Override
        public int compareTo(final PeakUsage o) {
            if (this == o)
                return 0;
            return Double.compare(getMZ(), o.getMZ());
        }

        /**
         * return true if this and o are 'close enough'
         *
         * @param o !null test object
         * @return as above
         */
        @Override
        public boolean equivalent(final PeakUsage o) {
            if (Math.abs(getMZ() - o.getMZ()) > 0.0001)
                return false;
            //noinspection SimplifiableIfStatement,PointlessBooleanExpression,ConstantConditions,RedundantIfStatement
            if (Math.abs(getUsed() - o.getUsed()) > 0.0001)
                return false;
            return true;
        }
    }
}
