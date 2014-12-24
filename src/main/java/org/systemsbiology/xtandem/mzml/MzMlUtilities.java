package org.systemsbiology.xtandem.mzml;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;

import java.nio.*;
import java.util.*;
import java.util.zip.*;

//import org.proteios.io.*;
//import org.systemsbiology.sax.*;
//import org.systemsbiology.xtandem.*;
//import org.systemsbiology.xtandem.sax.*;
//
//import java.util.*;
//import java.util.zip.*;
//
///**
// * org.systemsbiology.xtandem.mzml.MzMlUtilities
// * code for handling mzml including converters from Proteos representation to mine
// * User: Steve
// * Date: Apr 26, 2011
// */
public class MzMlUtilities {
    public static final MzMlUtilities[] EMPTY_ARRAY = {};

//    /**
//     * convert a proteos representation of a spectrum into mine
//     *
//     * @param inp !null proteos spectrum
//     * @return !null RawPeptideScan
//     */
//    public static RawPeptideScan buildSpectrum(ExtendedSpectrumImpl inp) {
//
//        RawPeptideScan ret = new RawPeptideScan(inp.getId(), null);
//        ret.setScanNumber(inp.getScanNumber());
//        ret.setMsLevel(inp.getMSLevel());
//  //      ret.setScanNumber(inp.);
//        double[] masses = inp.listMass();
//        double[] intensities = inp.listIntensities();
//        setRawScanPeaks(ret, masses, intensities);
//
//
//        List<StringPairInterface> dataList = inp.getExtraDataList();
//        for (StringPairInterface sp : dataList) {
//            handleAddedData(ret, sp.getName(), sp.getValue());
//        }
//
//        List<SpectrumPrecursor> precursors = inp.getPrecursors();
//        if(precursors != null)  {
//            SpectrumPrecursor[] pcrs = precursors.toArray(new SpectrumPrecursor[0]);
//             SpectrumPrecursor pre = pcrs[0];
//             double precursorIntensity = pre.getIntensity() != null ? pre.getIntensity() : ret.getBasePeakIntensity();
//             int pPrecursorCharge = pre.getCharge();
//             double precursorMassChargeRatio =   pre.getMassToChargeRatio() != null ? pre.getMassToChargeRatio() : getScanPrecursorMZ_2(pre);
//             FragmentationMethod method = fromFragmentationType(pre.getFragmentationType());
//             IScanPrecursorMZ precursor = new ScanPrecursorMz(precursorIntensity, pPrecursorCharge, precursorMassChargeRatio, method);
//             ret.setPrecursorMz(precursor);
//            ret.setActivationMethod(fromFragmentationType(pre.getFragmentationType()));
//        }
//        else {
//            double precursorIntensity = ret.getBasePeakIntensity();
//            double precursorMassChargeRatio = ret.getPrecursorMassChargeRatio();
//            if(precursorMassChargeRatio == 0)
//                precursorMassChargeRatio = ret.getBasePeakMz();
//            int pPrecursorCharge = 0;
//            FragmentationMethod method = FragmentationMethod.CID;
//             IScanPrecursorMZ precursor = new ScanPrecursorMz(precursorIntensity, pPrecursorCharge, precursorMassChargeRatio, method);
//            ret.setPrecursorMz(precursor);
//            ret.setActivationMethod(method);
//        }
//
//         return ret;
//    }

//    protected static double getScanPrecursorMZ_2( SpectrumPrecursor pre)      {
//        List<StringPairInterface> ed   = pre.getExtraDataList();
//        double mz = 0;
//        for(StringPairInterface sp  : ed)  {
//            if("m/z".equals(sp.getName())) {
//                double ret = Double.parseDouble(sp.getValue());
//                pre.setMassToChargeRatio(ret);
//                return ret;
//            }
//
//        }
//        throw new IllegalStateException("no m/z set");
//    }

    public static void setRawScanPeaks(final RawPeptideScan pRet, final double[] pMasses, final double[] pIntensity) {
        List<SpectrumPeak> holder = new ArrayList<SpectrumPeak>();

        for (int i = 0; i < pMasses.length; i++) {
            double intensity = pIntensity[i];
            if (intensity <= 0)
                continue; // no need to handle peaks with 0 or negative intensity
            SpectrumPeak peak = new SpectrumPeak(pMasses[i], (float) intensity);
            holder.add(peak);
        }
        SpectrumPeak[] peaks = new SpectrumPeak[holder.size()];
        holder.toArray(peaks);
        pRet.setPeaks(peaks);
    }

//    protected static FragmentationMethod fromFragmentationType(SpectrumPrecursor.FragmentationType inp) {
//        switch (inp) {
//            case CID:
//                return FragmentationMethod.CID;
//            case ECD:
//                return FragmentationMethod.ECD;
//            case ETD:
//                return FragmentationMethod.ETD;
//            case HCD:
//                return FragmentationMethod.HCD;
//            case PQD:
//                return FragmentationMethod.PQD;
//            case PSD:
//                return FragmentationMethod.PSD;
//            case UNKNOWN:   // todo fix
//                return FragmentationMethod.CID;
//        }
//        throw new UnsupportedOperationException("FragmentationType " + inp + " Unhandled");
//    }

    public static final String DEFAULT_CV_LABEL = "MS";
    public static final String CV_PARAM_TAG = "cvParam";
    public static final String USER_PARAM_TAG = "userParam";

    /**
     * special adder for cvParam does this
     * <cvParam cvLabel=\"MS\" accession=\"MS:1000127\" name=\"centroid mass spectrum\" value=\"\"/>\n
     *
     * @param accession
     * @param name
     * @param value
     * @param cvLabel
     */
    public static void appendCVParam(IXMLAppender appender, String accession, String name, String value, String cvLabel) {
        appender.openTag(CV_PARAM_TAG);
        appender.appendAttribute("cvLabel", cvLabel);
        appender.appendAttribute("accession", accession);
        appender.appendAttribute("name", name);
        if (value == null)
            value = "";
        appender.appendAttribute("value", value);
        appender.closeTag(CV_PARAM_TAG);
        //  appender.cr();
    }

    /**
     * special adder for cvParam does this
     * <cvParam cvLabel=\"MS\" accession=\"MS:1000127\" name=\"centroid mass spectrum\" value=\"\"/>\n
     *
     * @param accession
     * @param name
     * @param value
     * @param cvLabel
     */
    public static void appendUserParam(IXMLAppender appender, String name, String value) {
        appender.openTag(USER_PARAM_TAG);
        appender.appendAttribute("name", name);
        if (value == null)
            value = "";
        appender.appendAttribute("value", value);
        appender.closeTag(USER_PARAM_TAG);
        //  appender.cr();
    }


    /**
     * special
     *
     * @param accession
     * @param name
     * @param value
     * @param cvLabel
     */
    public static void appendCVParam(IXMLAppender appender, String accession, String name, String value) {
        appendCVParam(appender, accession, name, value, DEFAULT_CV_LABEL);
    }

    /**
     * append a param with no value - this is pretty common
     *
     * @param accession
     * @param name
     */
    public static void appendCVParam(IXMLAppender appender, String accession, String name) {
        appendCVParam(appender, accession, name, "");
    }

      public static String encode2(double[] data)
    {
        String ret = XTandemUtilities.encodeData64(data);
        return ret;
    }

    public static  String filterBase64(String dataBase64Raw)
    {
        if(dataBase64Raw == null)
            return null;
	/*
		 * Remove any line break characters from
		 * the base64-encoded block.
		 */
		StringBuffer dataBase64RawStrBuf = new StringBuffer(dataBase64Raw);
		StringBuffer dataBase64StrBuf = new StringBuffer("");
		int nChars = 0;
		int nLines = 0;
		int lineLength = 0;
		boolean newLineFlag = true;
		if (dataBase64Raw != null) {
			if (dataBase64Raw.length() > 0) {
				for (int i = 0; i < dataBase64RawStrBuf.length(); i++) {
					char c = dataBase64RawStrBuf.charAt(i);
					if (c == '\r' || c == '\n') {
						newLineFlag = true;
					} else {
						dataBase64StrBuf.append(c);
						nChars++;
						if (newLineFlag) {
							nLines++;
						}
						if (nLines == 1) {
							lineLength++;
						}
						newLineFlag = false;
					}
				}
			}
		}
		String ret = dataBase64StrBuf.toString();
        return ret;
    }


    public static double[] decodeDataString(final String pDataString,boolean is32Bit) {
        int len = pDataString.length();
        boolean isMultipleOf4 = (len % 4) == 0;
        boolean doublePrecision = !is32Bit;
        boolean bigEndian = false;
        List<Double> dataList =  decode(doublePrecision, bigEndian, pDataString);
        int size = dataList.size();
        double[] realData = new double[size];
        for (int i = 0; i < size; i++) {
            realData[i] = dataList.get(i);
            //XTandemUtilities.outputLine("" + realData[i] + ",");
        }
        return realData;
    }

    public static double[] decodeDataString2(final String pDataString) {
        boolean doublePrecision = true;
        boolean bigEndian = false;
        List<Double> dataList =  decode(doublePrecision, bigEndian, pDataString);
        int size = dataList.size();
        double[] realData = new double[size];
        for (int i = 0; i < size; i++) {
            realData[i] = dataList.get(i);
            //XTandemUtilities.outputLine("" + realData[i] + ",");
        }
        return realData;
    }

    public static String encode(double[] data) {
        List<Double> dataList = new ArrayList<Double>();
        for (int q = 0; q < data.length; q++) {
            double v = data[q];
            dataList.add(v);
        }
        boolean doublePrecision = true;
        boolean bigEndian = false;
        return encode(doublePrecision, bigEndian, dataList);

    }

    /**
     * habdle name value pairs for added data - NOTE look for todo for required fixes
     * Will throw UnsupportedOperationException
     *
     * @param raw    data to set
     * @param pName
     * @param pValue
     */
    protected static void handleAddedData(final RawPeptideScan raw, final String pName, final String pValue) {

        raw.addAddedValue(pName,pValue);
        String tag = "ms level";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setMsLevel(Integer.parseInt(pValue));
            return;
        }
        tag = "MSn spectrum";   // todo Fix
        if (tag.equalsIgnoreCase(pName)) {
            if ("".equals(pValue))
                return;
            throw new UnsupportedOperationException("Cannot understand " + tag);
        }
        tag = "positive scan";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setPolarity(ScanPolarity.plus);
            return;
        }
        tag = "negative scan";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setPolarity(ScanPolarity.minus);
            return;
        }
        tag = "centroid spectrum";  // todo Fix
        if (tag.equalsIgnoreCase(pName)) {
            if ("".equals(pValue))
                return;
            throw new UnsupportedOperationException("Cannot understand " + tag);
        }
        tag = "base peak m/z";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setBasePeakMz(Double.parseDouble(pValue));
              return;
        }
        tag = "base peak intensity";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setBasePeakIntensity(Double.parseDouble(pValue));
            return;
        }
        tag = "total ion current";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setTotIonCurrent(Double.parseDouble(pValue));
            return;
        }
        tag = "lowest observed m/z";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setLowMz(Double.parseDouble(pValue));
            return;
        }
        tag = "highest observed m/z";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setHighMz(Double.parseDouble(pValue));
            return;
        }
        tag = "no combination";        // todo Fix
        if (tag.equalsIgnoreCase(pName)) {
            if ("".equals(pValue))
                return;
            throw new UnsupportedOperationException("Cannot understand " + tag);
        }
        tag = "filter string";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setFilterLine(pValue);
            return;
        }
        tag = "preset scan configuration";     // todo Fix
        if (tag.equalsIgnoreCase(pName)) {
            if ("".equals(pValue))
                return;
            int value = Integer.parseInt(pValue);
            return;
        }
        tag = "ion injection time";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setIonInjectionTime(Double.parseDouble(pValue));
            return;
        }
        tag = "scan window lower limit";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setScanWindowLowerLimit(Double.parseDouble(pValue));
            return;
        }
        tag = "scan window upper limit";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setScanWindowUpperLimit(Double.parseDouble(pValue));
            return;
        }
        tag = "profile spectrum";   // todo Fix
        if (tag.equalsIgnoreCase(pName)) {
            if ("".equals(pValue))
                return;
            throw new UnsupportedOperationException("Cannot understand " + tag);
        }
        tag = "centroid mass spectrum";   // todo Fix
         if (tag.equalsIgnoreCase(pName)) {
             if ("".equals(pValue))
                 return;
             throw new UnsupportedOperationException("Cannot understand " + tag);
         }
        tag = "MS1 spectrum";   // todo Fix
         if (tag.equalsIgnoreCase(pName)) {
             if ("".equals(pValue))
                 return;
          }
        tag = "lowest m/z value";
        if (tag.equalsIgnoreCase(pName)) {
            raw.setLowMz((Double.parseDouble(pValue)));
            return;
        }
        tag = "highest m/z value";
          if (tag.equalsIgnoreCase(pName)) {
              raw.setHighMz((Double.parseDouble(pValue)));
              return;
          }

        tag = "scan m/z lower limit";
           if (tag.equalsIgnoreCase(pName)) {
               raw.setLowMz((Double.parseDouble(pValue)));
               return;
           }
        tag = "scan m/z upper limit";
            if (tag.equalsIgnoreCase(pName)) {
                raw.setHighMz((Double.parseDouble(pValue)));
                return;
            }

       throw new UnsupportedOperationException("Cannot handle data of type " + pName);
    }

   //
    // Code stolen from proteoc Base64Util class to allow dropi

    public static List<Double> decode(boolean doublePrecision, boolean bigEndian, String dataString) {
        boolean zLibCompression = false;
        List resultArray = decode(doublePrecision, bigEndian, zLibCompression, dataString);
        return resultArray;
    }

    public static List<Double> decode(boolean doublePrecision, boolean bigEndian, boolean zLibCompression, String dataString) {
        byte[] dataByteArray = Base64Coder.decode(dataString.toCharArray());
        int dataLength;
        if (zLibCompression) {
            Inflater dataByteBuffer = new Inflater();
            dataByteBuffer.setInput(dataByteArray, 0, dataByteArray.length);
            byte[] arraySize = new byte[3 * dataByteArray.length];
            int bytesPerValue = 0;

            try {
                bytesPerValue = dataByteBuffer.inflate(arraySize);
            }
            catch (DataFormatException var13) {
                throw new RuntimeException(var13);
            }

            dataByteBuffer.end();
            dataByteArray = new byte[bytesPerValue];

            for (dataLength = 0; dataLength < bytesPerValue; ++dataLength) {
                dataByteArray[dataLength] = arraySize[dataLength];
            }
        }

        ByteBuffer var14 = ByteBuffer.wrap(dataByteArray);
        int var15 = dataByteArray.length;
        byte var16 = 4;
        if (doublePrecision) {
            var16 = 8;
        }

        dataLength = var15 / var16;
        ArrayList resultArray = new ArrayList(1);
        if (!bigEndian) {
            var14.order(ByteOrder.LITTLE_ENDIAN);
        }

        int i;
        if (!doublePrecision) {
            for (i = 0; i < dataLength; ++i) {
                float value = var14.getFloat();
                resultArray.add(i, Double.valueOf((new Float(value)).doubleValue()));
            }
        }
        else {
            for (i = 0; i < dataLength; ++i) {
                double var17 = var14.getDouble();
                resultArray.add(i, Double.valueOf(var17));
            }
        }

        return resultArray;
    }

    public static String encode(boolean doublePrecision, boolean bigEndian, List<? extends Number> inDataList) {
        int dataLength = inDataList.size();
        byte bytesPerValue = 4;
        if (doublePrecision) {
            bytesPerValue = 8;
        }

        int arraySize = bytesPerValue * dataLength;
        byte[] dataByteArray = new byte[arraySize];
        ByteBuffer dataByteBuffer = ByteBuffer.wrap(dataByteArray);
        if (!bigEndian) {
            dataByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        int dataCharArray;
        Double doubleVal;
        if (!doublePrecision) {
            for (dataCharArray = 0; dataCharArray < dataLength; ++dataCharArray) {
                if (inDataList.get(dataCharArray) instanceof Double) {
                    doubleVal = (Double) inDataList.get(dataCharArray);
                    dataByteBuffer.putFloat(doubleVal.floatValue());
                }
                else if (inDataList.get(dataCharArray) instanceof Integer) {
                    Integer var11 = (Integer) inDataList.get(dataCharArray);
                    dataByteBuffer.putInt(var11.intValue());
                }
            }
        }
        else {
            for (dataCharArray = 0; dataCharArray < dataLength; ++dataCharArray) {
                doubleVal = (Double) inDataList.get(dataCharArray);
                dataByteBuffer.putDouble(doubleVal.doubleValue());
            }
        }

        char[] var10 = Base64Coder.encode(dataByteBuffer.array());
        return new String(var10);
    }

    public static class Base64Coder {
        private static char[] map1 = new char[64];
        private static byte[] map2;

        public Base64Coder() {
        }

        public static String encode(String s) {
            return new String(encode((byte[]) s.getBytes()));
        }

        public static char[] encode(byte[] in) {
            int iLen = in.length;
            int oDataLen = (iLen * 4 + 2) / 3;
            int oLen = (iLen + 2) / 3 * 4;
            char[] out = new char[oLen];
            int ip = 0;

            for (int op = 0; ip < iLen; ++op) {
                int i0 = in[ip++] & 255;
                int i1 = ip < iLen ? in[ip++] & 255 : 0;
                int i2 = ip < iLen ? in[ip++] & 255 : 0;
                int o0 = i0 >>> 2;
                int o1 = (i0 & 3) << 4 | i1 >>> 4;
                int o2 = (i1 & 15) << 2 | i2 >>> 6;
                int o3 = i2 & 63;
                out[op++] = map1[o0];
                out[op++] = map1[o1];
                out[op] = op < oDataLen ? map1[o2] : 61;
                ++op;
                out[op] = op < oDataLen ? map1[o3] : 61;
            }

            return out;
        }

        public static String decode(String s) {
            return new String(decode((char[]) s.toCharArray()));
        }

        public static byte[] decode(char[] in) {
            int iLen = in.length;
            if (iLen % 4 != 0) {
                throw new IllegalArgumentException("Length of Base64 encoded input string is not a multiple of 4.");
            }
            else {
                while (iLen > 0 && in[iLen - 1] == 61) {
                    --iLen;
                }

                int oLen = iLen * 3 / 4;
                byte[] out = new byte[oLen];
                int ip = 0;
                int op = 0;

                while (ip < iLen) {
                    char i0 = in[ip++];
                    char i1 = in[ip++];
                    char i2 = ip < iLen ? in[ip++] : 65;
                    char i3 = ip < iLen ? in[ip++] : 65;
                    if (i0 <= 127 && i1 <= 127 && i2 <= 127 && i3 <= 127) {
                        byte b0 = map2[i0];
                        byte b1 = map2[i1];
                        byte b2 = map2[i2];
                        byte b3 = map2[i3];
                        if (b0 >= 0 && b1 >= 0 && b2 >= 0 && b3 >= 0) {
                            int o0 = b0 << 2 | b1 >>> 4;
                            int o1 = (b1 & 15) << 4 | b2 >>> 2;
                            int o2 = (b2 & 3) << 6 | b3;
                            out[op++] = (byte) o0;
                            if (op < oLen) {
                                out[op++] = (byte) o1;
                            }

                            if (op < oLen) {
                                out[op++] = (byte) o2;
                            }
                            continue;
                        }

                        throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                    }

                    throw new IllegalArgumentException("Illegal character in Base64 encoded data.");
                }

                return out;
            }
        }

        static {
            int i = 0;

            char c;
            for (c = 65; c <= 90; map1[i++] = c++) {
                ;
            }

            for (c = 97; c <= 122; map1[i++] = c++) {
                ;
            }

            for (c = 48; c <= 57; map1[i++] = c++) {
                ;
            }

            map1[i++] = 43;
            map1[i++] = 47;
            map2 = new byte[128];

            for (i = 0; i < map2.length; ++i) {
                map2[i] = -1;
            }

            for (i = 0; i < 64; ++i) {
                map2[map1[i]] = (byte) i;
            }

        }
    }
}
