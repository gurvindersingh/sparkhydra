package org.systemsbiology.xtandem.testing;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.reporting.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.testing.DebugValues
 * a class for reading debug values
 * User: steven
 * Date: 3/3/11
 */
public class DebugValues
{
    public static final DebugValues[] EMPTY_ARRAY = {};

    private final XTandemMain m_Params;
    private final Map<String, Map<String, IMeasuredSpectrum>> m_MeasuredSpectrums = new HashMap<String, Map<String, IMeasuredSpectrum>>();
    private final Map<String, Map<String, ITheoreticalSpectrum>> m_TheoreticalSpectrums = new HashMap<String, Map<String, ITheoreticalSpectrum>>();
    private Map<String, DebugDotProduct> m_DotProductMap = new HashMap<String, DebugDotProduct>();


    public DebugValues(final XTandemMain pParams)
    {
        m_Params = pParams;
    }

    public XTandemMain getParams()
    {
        return m_Params;
    }

    public boolean loadDebugValues()
    {
        InputStream degugFile = BiomlReporter.buildDebugFileStream(m_Params);

        try {
            return readInputStream(degugFile);
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public boolean loadDebugValues(String fileName)
    {
        try {
            if (fileName.startsWith("res://")) {
                InputStream degugFile = XTandemUtilities.getDescribedStream(fileName);
                return readInputStream(degugFile);
            }
            else {
                InputStream degugFile = new FileInputStream(fileName);
                return readInputStream(degugFile);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public DebugDotProduct getValuesWithId(String id)
    {
        final DebugDotProduct[] dps = getDebugDotProductsWithId(id);
        if (dps.length == 0)
            return null;
        DebugDotProduct dp = new DebugDotProduct(dps[0]);
        for (int i = 1; i < dps.length; i++) {
            dp.add(dps[i]);

        }
        return dp;
    }

    protected boolean readInputStream(final InputStream pDegugFile) throws IOException
    {
        LineNumberReader reader = new LineNumberReader(new InputStreamReader(pDegugFile));
        String line = reader.readLine();
        while (line != null) {
            line = handleLine(line, reader);

        }
        return true;
    }

    public String handleLine(String line, LineNumberReader reader)
    {

        try {
            if (line.startsWith("<MeasuredSpectrum "))
                return handleMeasuredSpectrum(line, reader);
            if (line.startsWith("<dot_product "))
                return handleDotProduct(line, reader);
            return reader.readLine();
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    private String handleMeasuredSpectrum(final String pLine, final LineNumberReader pReader)
            throws IOException
    {
        AttributeParameterHolder ph = new AttributeParameterHolder(pLine);
        String id = ph.getParameter("id");
        String message = ph.getParameter("message");
        IMainData params = getParams();
        int realId = Integer.parseInt(id);
        int defaultCharge = 1;
        if (realId > 100000000) {
            realId -= 100000000;
            defaultCharge = 2;
            id = Integer.toString(realId);
        }
        if (realId < -100000000) {
            realId += 100000000;
            defaultCharge = 3;
            id = Integer.toString(realId);
        }

        RawPeptideScan rawScan = params.getRawScan(id);
        int precursorCharge = rawScan.getPrecursorCharge();

        MutableMeasuredSpectrum ret = new MutableMeasuredSpectrum(rawScan);
        if (precursorCharge == 0)
            ret.setPrecursorCharge(defaultCharge);

        String nowLine = readMeasuredSpectrum(ret, pReader, pLine);


        addMeasuredSpectrums(id, message, ret.asImmutable());

        if ("</MeasuredSpectrum>".equals(nowLine))
            nowLine = pReader.readLine();
        return nowLine;
    }


    private String handleDotProduct(final String pLine, final LineNumberReader pReader)
            throws IOException
    {
        AttributeParameterHolder ph = new AttributeParameterHolder(pLine);
        String id = ph.getParameter("id");
        String sequence = ph.getParameter("sequence");
        IonType type = ph.getEnumParameter("type", IonType.class);
        int charge = ph.getIntParameter("charge");

        XTandemMain params = getParams();
        int realId = Integer.parseInt(id);
        int defaultCharge = 1;
        if (realId > 100000000) {
            realId -= 100000000;
            defaultCharge = 2;
            id = Integer.toString(realId);
        }
        if (realId < -100000000) {
            realId += 100000000;
            defaultCharge = 3;
            id = Integer.toString(realId);
        }

        RawPeptideScan rawScan = params.getRawScan(id);
        int precursorCharge = rawScan.getPrecursorCharge();

        DebugDotProduct dp =  getDebugDotProduct(id, type, charge, params.seqenceToID(sequence));

        String nowLine = dp.readDotProduceEntries(pReader, pLine);

        // handle scores

        return nowLine;
    }


    public void addMeasuredSpectrums(IMeasuredSpectrum spec, String key, ISpectrumPeak[] added)
    {
        MutableMeasuredSpectrum ms = new MutableMeasuredSpectrum(spec);
        ms.setPeaks(added);
        addMeasuredSpectrums(ms.getId(), key, ms);
    }

    public void addMeasuredSpectrums(String key, String type, IMeasuredSpectrum added)
    {
        Map<String, IMeasuredSpectrum> themap = m_MeasuredSpectrums.get(key);
        if (themap == null) {
            themap = new HashMap<String, IMeasuredSpectrum>();
            m_MeasuredSpectrums.put(key, themap);
        }
        themap.put(type, added.asImmutable());
    }


    public Map<String, IMeasuredSpectrum> getMeasuredSpectrumMap(Integer key)
    {
        return m_MeasuredSpectrums.get(key);
    }

    public IMeasuredSpectrum getMeasuredSpectrum(String key, String type)
    {
        Map<String, IMeasuredSpectrum> themap = m_MeasuredSpectrums.get(key);
        if (themap == null)
            return null;
        return themap.get(type);
    }


    public void addTheoreticalSpectrums(String key, String type, ITheoreticalSpectrum added)
    {
        Map<String, ITheoreticalSpectrum> themap = m_TheoreticalSpectrums.get(key);
        if (themap == null) {
            themap = new HashMap<String, ITheoreticalSpectrum>();
            m_TheoreticalSpectrums.put(key, themap);
        }
        themap.put(type, added.asImmutable());
    }


    public Map<String, ITheoreticalSpectrum> getTheoreticalSpectrumMap(String key)
    {
        return m_TheoreticalSpectrums.get(key);
    }

    public ITheoreticalSpectrum getTheoreticalSpectrum(String key, String type)
    {
        Map<String, ITheoreticalSpectrum> themap = m_TheoreticalSpectrums.get(key);
        if (themap == null)
            return null;
        return themap.get(type);
    }

    public static String readMeasuredSpectrum(MutableMeasuredSpectrum ret, LineNumberReader reader,
                                              String currentLine)
    {
        try {
            ISpectralScan scan = null;

            List<ISpectrumPeak> holder = new ArrayList<ISpectrumPeak>();

            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("<total_peaks")) {
                    line = reader.readLine();
                    break;
                }
                ISpectrumPeak peak = readPeak(line);
                if (peak == null)
                    break;
                holder.add(peak);
                line = reader.readLine();
            }
            ISpectrumPeak[] peaks = new ISpectrumPeak[holder.size()];
            holder.toArray(peaks);
            ret.setPeaks(peaks);
            return line;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    /**
     * read a line like 481.215,2.75866 and make a peak
     *
     * @param line @null line
     * @return null if there is no peak - uauslly this is the end of the data
     */
    public static ISpectrumPeak readPeak(String line)
    {
        if (line == null || line.length() == 0)
            return null;
        AttributeParameterHolder ph = new AttributeParameterHolder(line);
        double mz = ph.getDoubleParameter("mz");
        double peak = ph.getDoubleParameter("score");
        return new SpectrumPeak(mz, (float) peak);
    }


    public void addDebugDotProduct(String key, DebugDotProduct added)
    {
        if(added == null)
            throw new IllegalArgumentException("problem"); // ToDo change
        final DebugDotProduct product = m_DotProductMap.get(key);
        if(product == null)
            m_DotProductMap.put(key, added);
        else
            throw new IllegalStateException("problem"); // ToDo change
    }

    public void addDebugDotProduct(IMeasuredSpectrum ms, ITheoreticalSpectrum sp,
                                   DebugDotProduct added)
    {
        final String key = buildKeyString(ms, sp);
        m_DotProductMap.put(key, added);
    }

    public DebugDotProduct getDebugDotProduct(IMeasuredSpectrum ms, ITheoreticalSpectrum sp)
    {
        final String key = buildKeyString(ms, sp);
        DebugDotProduct ret = m_DotProductMap.get(key);
        if (ret == null) {
            ret = new DebugDotProduct(ms.getId(), IonType.Y,sp.getCharge(), sp.getPeptide().getId());
            addDebugDotProduct(key, ret);
        }
        return ret;
    }

    public static String buildKeyString(IMeasuredSpectrum ms, ITheoreticalSpectrum sp)
    {
        String id = ms.getId();
        int charge = sp.getCharge();
     //   id += XTandemUtilities.CHARGE_ID_OFFSET[charge];
        return buildKeyString(id,IonType.Y,charge);
    }


    public static String buildKeyString(final String pScanId, IonType type,int charge)
    {

        final String key = pScanId  + ":" + type + ":" + charge;
        return key;
    }

    public DebugDotProduct getDebugDotProduct(final String pScanId, IonType type,int charge, final String pPeptideId)
    {
        String key = buildKeyString(    pScanId,   type,  charge);
        DebugDotProduct ret = m_DotProductMap.get(key);
        if (ret == null) {
            ret = new DebugDotProduct( pScanId,   type,  charge, pPeptideId);
            addDebugDotProduct(key, ret);
        }
        return m_DotProductMap.get(key);
    }

    public DebugDotProduct getDebugDotProduct(String key)
    {
        return m_DotProductMap.get(key);
    }

    public DebugDotProduct[] getDebugDotProducts()
    {
        return XTandemUtilities.getSortedValues(m_DotProductMap, DebugDotProduct.class);
    }

    public DebugDotProduct[] getDebugDotProductsWithId(String id)
    {
        DebugDotProduct[] dps = getDebugDotProducts();
        List<DebugDotProduct> holder = new ArrayList<DebugDotProduct>();
        for (int i = 0; i < dps.length; i++) {
            DebugDotProduct dp = dps[i];
            if (dp.getScanId().equals(id))
                holder.add(dp);
        }
        DebugDotProduct[] ret = new DebugDotProduct[holder.size()];
        holder.toArray(ret);
        return ret;
    }


}
