package org.systemsbiology.xtandem;

import com.lordjoe.distributed.hydra.test.*;
import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.mzml.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.RawPeptideScan
 * Class of measured spectrum representing data directly off the mass spectrometer
 * This is read fromm the <scan or <spectrum tag - After normalization it will
 * be converted to a different object Much of the information here is there because it is in the
 * xml
 * User: steven
 */
public class RawPeptideScan implements IMeasuredSpectrum, ISpectralScan, Comparable<ISpectralScan> {
    public static final RawPeptideScan[] EMPTY_ARRAY = {};

    public static final String TAG = "scan";
    public static final String MZML_TAG = "spectrum";


    private String m_Id;
    private int m_ScanNumber;
    private String m_Url;
    private String m_Label;
    private String m_FilterLine;
    private int m_MsLevel;
    private int m_ScanEvent;
    private ScanTypeEnum m_ScanType;
    private ScanPolarity m_Polarity;
    private String m_RetentionTime;
    private FragmentationMethod m_ActivationMethod;
    private double m_LowMz;
    private double m_HighMz;
    private double m_BasePeakMz;
    private double m_BasePeakIntensity;
    private double m_TotIonCurrent;
    private double m_IonInjectionTime;
    private double m_ScanWindowLowerLimit;
    private double m_ScanWindowUpperLimit;
    private double m_CompensationVoltage;
    private String m_InstrumentId;
    private ISpectrumPeak[] m_Peaks;
    private IScanPrecursorMZ m_PrecursorMz;
    private double m_SumIntensity;
    private double m_MaxIntensity;
    private double m_FI;
    private final Map<String, String> m_AddedValues = new HashMap<String, String>();


    public static final int ID_LENGTH = 12; // pad ids to make sure integer IDS sort properly
    public RawPeptideScan(String pId, String url) {

        if (pId != null && !"".equals(pId)) {
            while(pId.length() < ID_LENGTH)
                pId = "0"  + pId;

            setId(pId);
            m_Label = fromRestOfId(pId);
        }
        m_Url = url;
    }


    public int getScanNumber() {
        return m_ScanNumber;
    }

    public void setScanNumber(final int pScanNumber) {
        m_ScanNumber = pScanNumber;
    }




    public void addAddedValue(String key, String added) {
        m_AddedValues.put(key, added);
    }


    public void removeAddedValue(String removed) {
        m_AddedValues.remove(removed);
    }

    public String[] getAddedValues() {
        Collection<String> values = m_AddedValues.values();
        return values.toArray(new String[values.size()]);
    }

    public String getAddedValue(String key) {
        return m_AddedValues.get(key);
    }

    public void setId( String pId) {
        if (m_Id != null)
            throw new IllegalStateException("id can only be set once");
        if ("".equals(pId))
            return; // do nit try with empty string
        m_Id = toNumericId(pId);
        m_Label = fromRestOfId(pId);

        if(TestUtilities.isInterestingSpectrum(this))
             pId = null; // break here
    }

    /**
     * construct an MZML fragment from the scan
     *
     * @return !null fragment
     */
    public String toMzMLFragment() {
        StringBuilder sb = new StringBuilder();
        XMLAppender appender = new XMLAppender(sb);
        serializeAsMzMLString(appender);
        return sb.toString();
    }

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    public void serializeAsMzMLString(IXMLAppender adder) {
        int indent = 0;
        String tag = MZML_TAG;
        adder.openTag(tag);
        adder.appendAttribute("id", getId());
        adder.appendAttribute("scanNumber", getScanNumber());
        adder.appendAttribute("mslevel", getMsLevel());
        adder.endTag();
        adder.cr();
        MzMlUtilities.appendCVParam(adder, "MS:1000580", "MSn spectrum");
        if (getUrl() != null)
            MzMlUtilities.appendUserParam(adder, "url", getUrl());
        appendBaseParams(adder);
        appendPeaks(adder);
//        adder.appendAttribute("peaksCount", getPeaksCount());
//        adder.appendAttribute("polarity", getPolarity());
//        adder.appendAttribute("scantype", getScanType());
//        adder.appendAttribute("filterLine", getFilterLine());
//        adder.appendAttribute("retentionTime", getRetentionTimeString());
//        adder.appendAttribute("lowMz", XTandemUtilities.formatDouble(getLowMz(), 3));
//        adder.appendAttribute("highMz", XTandemUtilities.formatDouble(getHighMz(), 3));
//        adder.appendAttribute(
//                " basePeakMz", XTandemUtilities.formatDouble(getBasePeakMz(), 3));
//        adder.appendAttribute(
//                " basePeakIntensity", XTandemUtilities.formatDouble(getBasePeakIntensity(),
//                        3));
//        adder.appendAttribute("totIonCurrent", XTandemUtilities.formatDouble(getTotIonCurrent(),
//                3));
//        adder.appendAttribute("collisionEnergy", getCompensationVoltage());
//        adder.endTag();
//
//        getPrecursorMz().serializeAsString(adder);
//
//        adder.openTag("peaks");
//        adder.appendAttribute("precision ", "32 ");
//        adder.appendAttribute("byteOrder ", "network ");
//        adder.appendAttribute("contentType ", "m/z-int ");
//        adder.appendAttribute("compressionType ", "none ");
//        adder.appendAttribute("compressedLen ", "0");
//        adder.endTag();
//
//        String peakString = XTandemUtilities.encodePeaks32(getPeaks());
//        peakString = XTandemHadoopUtilities.cleanXML(peakString); // we find bad characters i.e 0
//        adder.appendText(peakString);
//
//        adder.closeTag("peaks");
        adder.closeTag(tag);

    }

    /**
     * write out the data as an MGF file
     *
     * @param out place to append
     */
    public void appendAsMGF(Appendable out) {
        int indent = 0;

        try {
            out.append("BEGIN IONS");
            out.append("\n");

            out.append("TITLE=" + getId());
            out.append("\n");

            int precursorCharge = getPrecursorCharge();
            IScanPrecursorMZ precursorMz = getPrecursorMz();
            double massChargeRatio = precursorMz.getMassChargeRatio();

            out.append("RTINSECONDS=" + getRetentionTime());
            out.append("\n");


            double precursorMass = getPrecursorMass();
            out.append("PEPMASS=" + massChargeRatio);
            out.append("\n");

            out.append("CHARGE=" + precursorCharge);
            if (precursorCharge > 0)
                out.append("+");
            out.append("\n");

            appendPeaks(out);
            out.append("END IONS");
            out.append("\n");
            out.append("\n"); // add one more blank line
         }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }


    /**
     * appent the peaks as an MGF file would do so
     *
     * @param out hte output appendable
     * @throws IOException because it must
     */
    protected void appendPeaks(final Appendable out) throws IOException {
        ISpectrumPeak[] spectrumPeaks = getPeaks();
        for (int i = 0; i < spectrumPeaks.length; i++) {
            ISpectrumPeak sp = spectrumPeaks[i];

            String item = String.format("%10.5f", sp.getMassChargeRatio()).trim();
            String item2 = String.format("%8.2f", sp.getPeak()).trim();
            out.append(item + "\t" + item2);
            out.append("\n");
        }

    }


    protected void appendPrecursor(final IXMLAppender adder) {
        String tag = "precursorList";
        adder.openTag(tag);
        adder.appendAttribute("count", 1);
        adder.endTag();
        adder.cr();
        adder.openTag("precursor");
        adder.endTag();
        adder.cr();
        appendIonSelection(adder);
        appendActivation(adder);
        adder.closeTag("precursor");
        adder.closeTag(tag);
    }

    protected void appendIonSelection(final IXMLAppender adder) {
        adder.openEmptyTag("ionSelection");
        adder.cr();
        MzMlUtilities.appendCVParam(adder, "MS:1000040", "m/z", XTandemUtilities.formatDouble(getPrecursorMassChargeRatio(), 2));
        MzMlUtilities.appendCVParam(adder, "MS:1000041", "charge state", Integer.toString(getPrecursorCharge()));
        adder.closeTag("ionSelection");
    }

    protected void appendActivation(final IXMLAppender adder) {
        adder.openEmptyTag("activation");
        adder.cr();
        switch (getActivationMethod()) {
            case CID:
                MzMlUtilities.appendCVParam(adder, "MS:1000133", "collision-induced dissociation");
                break;
            case ECD:
                MzMlUtilities.appendCVParam(adder, "MS:1000133", "electron capture dissociation");
                break;
            case ETD:
                MzMlUtilities.appendCVParam(adder, "MS:1000133", "electron transfer dissociation");
                break;
            case HCD:
                MzMlUtilities.appendCVParam(adder, "MS:1000133", "high-energy collision-induced dissociation");
                break;
            case PQD:
                MzMlUtilities.appendCVParam(adder, "MS:1000133", "pulsed-q dissociation");
                break;
            case PSD:
                MzMlUtilities.appendCVParam(adder, "MS:1000133", "post-source decay");
                break;
            default:
                throw new UnsupportedOperationException("Not sure hoiw to handle others"); // ToDo
        }
        adder.closeTag("activation");
    }

    protected void appendBaseParams(final IXMLAppender adder) {
        String tag = "spectrumDescription";
        adder.openEmptyTag(tag);
        adder.cr();
        appendPrecursor(adder);
        adder.closeTag(tag);
    }

    protected void appendPeaks(final IXMLAppender adder) {
        ISpectrumPeak[] spectrumPeaks = getPeaks();
        double[] mzs = new double[spectrumPeaks.length];
        double[] intensities = new double[spectrumPeaks.length];
        for (int i = 0; i < spectrumPeaks.length; i++) {
            ISpectrumPeak sp = spectrumPeaks[i];
            mzs[i] = sp.getMassChargeRatio();
            intensities[i] = sp.getPeak();
        }
        appendBinaryData(adder, mzs, "m/z array");
        appendBinaryData(adder, intensities, "intensity array");
    }


    protected static void appendBinaryData(final IXMLAppender adder, double[] data, String name) {
        //  String encoded = MzMlUtilities.encode(data);
        String peakString = MzMlUtilities.encode(data);
        peakString = XTandemHadoopUtilities.cleanXML(peakString); // we find bad characters i.e 0
        adder.openTag("binaryDataArray");
        adder.appendAttribute("arrayLength ", data.length);
        adder.appendAttribute("encodedLength ", peakString.length());
        adder.appendAttribute("dataProcessingRef ", "Xcalibur Processing");
        adder.endTag();
        adder.cr();
        MzMlUtilities.appendCVParam(adder, "MS:1000523", "64-bit float");
        MzMlUtilities.appendCVParam(adder, "MS:1000576", "no compression");
        if ("m/z array".equals(name))
            MzMlUtilities.appendCVParam(adder, "MS:1000514", name);
        if ("intensity array".equals(name))
            MzMlUtilities.appendCVParam(adder, "MS:1000515", name);
        adder.openEmptyTag("binary");
        adder.appendText(peakString);
        adder.closeTag("binary");

        adder.closeTag("binaryDataArray");

    }


    public static String toNumericId(final String pId) {
        int index = pId.indexOf("scan=");
        if (index == -1)
            return pId;
        index += "scan=".length();
        int endIndex = pId.indexOf(" ", index);
        if (endIndex == -1)
            return new String(pId.substring(index));
        //noinspection RedundantStringConstructorCall
        return new String(pId.substring(index, endIndex));
    }

    public static String fromRestOfId(final String pId) {
        int index = pId.indexOf("scan=");
        if (index == -1)
            return pId;
         //noinspection RedundantStringConstructorCall
        return new String(pId.substring(index));
    }

    @Override
    public String getId() {
        return m_Id;
    }

    /**
     * get index - return 0 if unknown
     *
     * @return as above
     */
    @Override
    public int getIndex() {
        return m_ScanNumber;
    }

    /**
     * weak test for equality
     *
     * @param test !null test
     * @return true if equivalent
     */
    @Override
    public boolean equivalent(IMeasuredSpectrum test) {
        if (this == test)
            return true;
        if (!getId().equals(test.getId()))
            return false;
        if (!equivalentData((ISpectrum) test))
            return false;
        int pc1 = getPrecursorCharge();
        int pc2 = test.getPrecursorCharge();
        if (!XTandemUtilities.equivalentDouble(pc1, pc2))
            return false;
        double pm1 = getPrecursorMass();
        double pm2 = test.getPrecursorMass();
        //noinspection RedundantIfStatement
        if (!XTandemUtilities.equivalentDouble(pm1, pm2))
            return false;
        return true;
    }

    /*
  <scan num="7858"
   msLevel="2"
   peaksCount="345"
   polarity="+"
   scanType="Full"
   filterLine="ITMS + c NSI d Full ms2 1096.63@cid35.00 [290.00-2000.00]"
   retentionTime="PT2897.2S"
   lowMz="324.828"
   highMz="1998.61"
   basePeakMz="1231.62"
   basePeakIntensity="143.081"
   totIonCurrent="3862.91"
   collisionEnergy="35" >
   <precursorMz precursorIntensity="69490.9" activationMethod="CID" >1096.63</precursorMz>
   <peaks precision="32"
    byteOrder="network"
    contentType="m/z-int"
    compressionType="none"
    compressedLen="0" >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</peaks>
 </scan>

    */

    /**
     * make a form suitable to
     * 1) reconstruct the original given access to starting conditions
     *
     * @param adder !null where to put the data
     */
    public void serializeAsString(IXMLAppender adder) {
        int indent = 0;
        String tag = TAG;
        String url = getUrl();
        adder.openTag(tag);
        adder.appendAttribute("num", getId());
        if (url != null) {
            adder.appendAttribute("url", url);
        }
        adder.appendAttribute("mslevel", getMsLevel());
        adder.appendAttribute("peaksCount", getPeaksCount());
        adder.appendAttribute("polarity", getPolarity());
        adder.appendAttribute("scantype", getScanType());
        adder.appendAttribute("filterLine", getFilterLine());
        adder.appendAttribute("retentionTime", getRetentionTime());
        adder.appendAttribute("lowMz", XTandemUtilities.formatDouble(getLowMz(), 3));
        adder.appendAttribute("highMz", XTandemUtilities.formatDouble(getHighMz(), 3));
        adder.appendAttribute(
                " basePeakMz", XTandemUtilities.formatDouble(getBasePeakMz(), 3));
        adder.appendAttribute(
                " basePeakIntensity", XTandemUtilities.formatDouble(getBasePeakIntensity(),
                3));
        adder.appendAttribute("totIonCurrent", XTandemUtilities.formatDouble(getTotIonCurrent(),
                3));
        adder.appendAttribute("collisionEnergy", getCompensationVoltage());
        adder.endTag();

//        if (url != null) {
//            adder.openTag("nameValue");
//            adder.appendAttribute("name", "url");
//            adder.appendAttribute("value", url);
//            adder.closeTag("nameValue");
//        }
        getPrecursorMz().serializeAsString(adder);

        adder.openTag("peaks");
        adder.appendAttribute("precision ", "32 ");
        adder.appendAttribute("byteOrder ", "network ");
        adder.appendAttribute("contentType ", "m/z-int ");
        adder.appendAttribute("compressionType ", "none ");
        adder.appendAttribute("compressedLen ", "0");
        adder.endTag();

        String peakString = XTandemUtilities.encodePeaks32(getPeaks());
        peakString = XTandemHadoopUtilities.cleanXML(peakString); // we find bad characters i.e 0
        adder.appendText(peakString);

        adder.closeTag("peaks");
        adder.closeTag(tag);

    }


    public String getUrl() {
        return m_Url;
    }

    public void setUrl(String url) {
        m_Url = url;
    }

    @Override
    public double getSumIntensity() {
        if (m_SumIntensity == 0)
            buildStatistics();
        return m_SumIntensity;
    }

    protected void buildStatistics() {
        double sum = 0;
        double max = Double.MIN_VALUE;
        double fi = Double.MIN_VALUE;

        for (int i = 0; i < m_Peaks.length; i++) {
            ISpectrumPeak pk = m_Peaks[i];
            double intensity = pk.getPeak();
            sum += intensity;
            max = Math.max(intensity, max);
            fi = intensity; // toto fix
        }
        synchronized (this) {
            m_SumIntensity = sum;
            m_MaxIntensity = max;
            m_FI = fi;
        }
    }


    @Override
    public double getMaxIntensity() {
        if (m_MaxIntensity == 0)
            buildStatistics();
        return m_MaxIntensity;
    }

    public double getFI() {
        if (m_FI == 0)
            buildStatistics();
        return m_FI;
    }

    public String getLabel() {
        return m_Label;
    }

    public void setLabel(String pLabel) {
        m_Label = pLabel;
    }

    public String getFilterLine() {
        return m_FilterLine;
    }

    public void setFilterLine(String pFilterLine) {
        m_FilterLine = pFilterLine;
    }

    /**
     * get the charge of the spectrum precursor
     *
     * @return
     */
    @Override
    public int getPrecursorCharge() {
        IScanPrecursorMZ precursorMz = getPrecursorMz();
        if (precursorMz == null)
            return 0;
        return precursorMz.getPrecursorCharge();
    }


    /**
     * return true if a mass such as that of a throretical peak is
     * within the range to scpre
     *
     * @param mass positive testMass
     * @return as above
     */
    public boolean isMassWithinRange(double mass, int charge, IScoringAlgorithm scorer) {
        final IScanPrecursorMZ mz = getPrecursorMz();
        return mz.isMassWithinRange(mass, charge, scorer);
    }


    /**
     * get the mass of the spectrum precursor
     *
     * @return as above
     */
    @Override
    public double getPrecursorMass() {

        IScanPrecursorMZ precursorMz = getPrecursorMz();
        if (precursorMz == null)
            return getBasePeakMz();
        return precursorMz.getPrecursorMass();
    }

    /**
     * get the mass of the spectrum precursor
     *
     * @return as above
     */
    @Override
    public double getPrecursorMassChargeRatio() {
        IScanPrecursorMZ precursorMz = getPrecursorMz();
        if (precursorMz == null)
            return getBasePeakMz();

        return precursorMz.getMassChargeRatio();
    }

    /**
     * get the mass of the spectrum precursor
     *
     * @return as above
     */
    public double getPrecursorMass(int charge) {

        return getPrecursorMz().getPrecursorMass(charge);
    }

    /**
     * Mass spec characteristics
     *
     * @return
     */
    public ISpectralScan getScanData() {
        return this;
    }


    /**
     * return true if the spectrum is immutable
     *
     * @return
     */
    @Override
    public boolean isImmutable() {
        return false;
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
        return this;
    }

    /**
     * if the spectrum is not  mutable build an  mutable version
     * Otherwise return this
     *
     * @return as above
     */
    @Override
    public MutableMeasuredSpectrum asMmutable() {
        if (true) throw new UnsupportedOperationException("Fix This");
        return null;
    }

    @Override
    public int getMsLevel() {
        return m_MsLevel;
    }

    public void setMsLevel(final int pMsLevel) {
        m_MsLevel = pMsLevel;
    }

    @Override
    public int getScanEvent() {
        return m_ScanEvent;
    }

    public void setScanEvent(final int pScanEvent) {
        m_ScanEvent = pScanEvent;
    }

    @Override
    public ScanTypeEnum getScanType() {
        return m_ScanType;
    }

    public void setScanType(final ScanTypeEnum pScanType) {
        m_ScanType = pScanType;
    }

    public int getPeaksCount() {
        return m_Peaks.length;
    }


    @Override
    public ScanPolarity getPolarity() {
        return m_Polarity;
    }

    public void setPolarity(final ScanPolarity pPolarity) {
        m_Polarity = pPolarity;
    }

    @Override
    public String getRetentionTime() {
        return m_RetentionTime;
    }

    public void setRetentionTime(final String pRetentionTime) {
        m_RetentionTime = pRetentionTime;
    }

    @Override
    public FragmentationMethod getActivationMethod() {
        return m_ActivationMethod;
    }

    public void setActivationMethod(final FragmentationMethod pActivationMethod) {
        m_ActivationMethod = pActivationMethod;
    }

    @Override
    public double getLowMz() {
        return m_LowMz;
    }

    public void setLowMz(final double pLowMz) {
        m_LowMz = pLowMz;
    }

    @Override
    public double getHighMz() {
        return m_HighMz;
    }

    public void setHighMz(final double pHighMz) {
        m_HighMz = pHighMz;
    }

    @Override
    public double getBasePeakMz() {
        return m_BasePeakMz;
    }

    public void setBasePeakMz(final double pBasePeakMz) {
        m_BasePeakMz = pBasePeakMz;
    }

    @Override
    public double getBasePeakIntensity() {
        return m_BasePeakIntensity;
    }

    public void setBasePeakIntensity(final double pBasePeakIntensity) {
        m_BasePeakIntensity = pBasePeakIntensity;
    }

    public double getIonInjectionTime() {
        return m_IonInjectionTime;
    }

    public void setIonInjectionTime(final double pIonInjectionTime) {
        m_IonInjectionTime = pIonInjectionTime;
    }

    public double getScanWindowLowerLimit() {
        return m_ScanWindowLowerLimit;
    }

    public void setScanWindowLowerLimit(final double pScanWindowLowerLimit) {
        m_ScanWindowLowerLimit = pScanWindowLowerLimit;
    }

    public double getScanWindowUpperLimit() {
        return m_ScanWindowUpperLimit;
    }

    public void setScanWindowUpperLimit(final double pScanWindowUpperLimit) {
        m_ScanWindowUpperLimit = pScanWindowUpperLimit;
    }

    @Override
    public double getTotIonCurrent() {
        return m_TotIonCurrent;
    }

    public void setTotIonCurrent(final double pTotIonCurrent) {
        m_TotIonCurrent = pTotIonCurrent;
    }

    @Override
    public double getCompensationVoltage() {
        return m_CompensationVoltage;
    }

    public void setCompensationVoltage(final double pCompensationVoltage) {
        m_CompensationVoltage = pCompensationVoltage;
    }

    @Override
    public String getInstrumentId() {
        return m_InstrumentId;
    }

    public void setInstrumentId(final String pInstrumentId) {
        m_InstrumentId = pInstrumentId;
    }


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
     * sort peaks by mass
     */
    public static final Comparator<ISpectrumPeak> BY_MZ = new Comparator<ISpectrumPeak>() {

        @Override
        public int compare(final ISpectrumPeak o1, final ISpectrumPeak o2) {
            double m1 = o1.getMassChargeRatio();
            double m2 = o2.getMassChargeRatio();
            if (m1 == m2) {
                double p1 = o1.getPeak();
                double p2 = o2.getPeak();
                if (p1 == p2)
                    return 0;
                return p1 < p2 ? -1 : 1;

            }
            return m1 < m2 ? -1 : 1;
        }
    };


    public void setPeaks(final ISpectrumPeak[] pPeaks) {
        if (pPeaks != null)
            Arrays.sort(pPeaks, BY_MZ);
        // slewis added a filter to take out o height peaks
        List<ISpectrumPeak> filtered = new ArrayList<ISpectrumPeak>(pPeaks.length);
        for (int i = 0; i < pPeaks.length; i++) {
            ISpectrumPeak peak = pPeaks[i];
            if(peak.getPeak() > 0)
                filtered.add(peak);
        }
        m_Peaks = filtered.toArray(new ISpectrumPeak[filtered.size()]);
    }

    public IScanPrecursorMZ getPrecursorMz() {
        if (m_PrecursorMz == null) {
            String filterLine = getFilterLine();
            if (filterLine != null)
                m_PrecursorMz = new PresumptiveScanPrecursorMz(filterLine);
        }
        return m_PrecursorMz;
    }

    public void setPrecursorMz(final IScanPrecursorMZ pPrecursorMz) {
        m_PrecursorMz = pPrecursorMz;
    }

    public void validate() {
        if (getPrecursorMz() == null)
            throw new IllegalStateException("problem"); // ToDo change
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


    /**
     * return true if this and o are 'close enough'
     *
     * @param !null o
     * @return as above
     */

    public boolean equivalentData(ISpectrum o) {
        if (this == o)
            return true;
        ISpectrum realO = o;
        final ISpectrumPeak[] peaks1 = getNonZeroPeaks();
        final ISpectrumPeak[] peaks2 = realO.getNonZeroPeaks();
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


    @Override
    public int compareTo(final ISpectralScan o) {
        if (o == this)
            return 0;
        return XTandemUtilities.compareAsNumbers(getId(), o.getId());
    }


    /**
     * Returns a string representation of the object. In general, the
     * <code>toString</code> method returns a string that
     * "textually represents" this object. The result should
     * be a concise but informative representation that is easy for a
     * person to read.
     * It is recommended that all subclasses override this method.
     * <p/>
     * The <code>toString</code> method for class <code>Object</code>
     * returns a string consisting of the name of the class of which the
     * object is an instance, the at-sign character `<code>@</code>', and
     * the unsigned hexadecimal representation of the hash code of the
     * object. In other words, this method returns a string equal to the
     * value of:
     * <blockquote>
     * <pre>
     * getClass().getName() + '@' + Integer.toHexString(hashCode())
     * </pre></blockquote>
     *
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "RawScan id=" + getId() +

                " mass " + getPrecursorMz().getPrecursorMass();
    }

    /**
     * append as an MGF File
     *
     * @param addTo
     */
    public void serializeMGF(Appendable addTo) {
        try {
            addTo.append("BEGIN IONS\n");
            addTo.append("TITLE=" + getId() + "\n");
            addTo.append("PEPMASS=" + String.format("%10.4f", getPrecursorMass()).trim() + "\n");
            addTo.append("CHARGE=" + getPrecursorCharge() + "+\n");
            ISpectrumPeak[] peaks = getPeaks();
            for (int i = 0; i < peaks.length; i++) {
                ISpectrumPeak peak = peaks[i];
                String mz = String.format("%10.5f", peak.getMassChargeRatio()).trim();
                String pk = String.format("%8.2f", peak.getPeak()).trim();
                addTo.append(mz + "\t" + pk + "\n");

            }
            addTo.append("END IONS\n");
            addTo.append("\n");
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }
}
