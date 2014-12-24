package org.systemsbiology.xtandem.testing;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.scoring.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.testing.DebugDotProduct
 * User: steven
 * Date: 3/3/11
 */
public class DebugDotProduct  implements Comparable<DebugDotProduct> {
    public static final DebugDotProduct[] EMPTY_ARRAY = {};

    public final String m_ScanId;
    private final IonType m_Type;
    private final int m_Charge;
    private final String m_PeptideId;
    private final IonUseScore m_Score = new IonUseScore();
    private final List<DebugMatchPeak> m_MatchedPeaks = new ArrayList<DebugMatchPeak>();
    private final List<ISpectrumPeak> m_MeasuredPeak = new ArrayList<ISpectrumPeak>();
    private final List<ISpectrumPeak> m_TheoreticalPeak = new ArrayList<ISpectrumPeak>();

    public DebugDotProduct(final String pScanId, IonType type,int charge, final String pPeptideId) {
        m_ScanId = pScanId;
        m_PeptideId = pPeptideId;
        m_Type = type;
        m_Charge = charge;
        if(type == IonType.B && charge > 1)
            throw new IllegalArgumentException("this is a k_score problem - drop the test when solved slewis");
    }

    public DebugDotProduct(DebugDotProduct d1, DebugDotProduct... others) {
        m_ScanId = d1.getScanId();
        m_Charge = d1. getCharge();
          m_PeptideId = d1.getPeptideId();
        m_Type = d1.getType();
        m_Score.add(d1.m_Score);
        add(d1);
        for (int i = 0; i < others.length; i++) {
            DebugDotProduct other = others[i];
            add(other);
        }
    }

    public int getCharge() {
        return m_Charge;
    }

    public void add(DebugDotProduct d1) {

        m_Score.add(d1.m_Score);
        m_MatchedPeaks.addAll(d1.m_MatchedPeaks);
        m_MeasuredPeak.addAll(d1.m_MeasuredPeak);
        m_TheoreticalPeak.addAll(d1.m_TheoreticalPeak);
    }


    public void addTheoreticalPeak(ISpectrumPeak added) {
        m_TheoreticalPeak.add(added);
    }

    public ISpectrumPeak[] getTheoreticalPeaks() {
        return m_TheoreticalPeak.toArray(new ISpectrumPeak[0]);
    }


    public void addMeasuredPeak(ISpectrumPeak added) {
        if((int)added.getMassChargeRatio() == 324)
            XTandemUtilities.breakHere();
        m_MeasuredPeak.add(added);
    }




    public ISpectrumPeak[] getMeasuredPeaks() {
        return m_MeasuredPeak.toArray(new ISpectrumPeak[0]);
    }


    public String readDotProduceEntries(LineNumberReader reader, String currentLine) {
        try {
            ISpectralScan scan = null;


            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("</dot_product>")) {
                    line = reader.readLine();
                    break;

                }
                else if (line.startsWith("<match_at_mass")) {
                    handleMatchLine(line, getType());
                }
                else if (line.startsWith("<theoretical_ions>")) {
                       handleIons(reader,"theoretical_ions");

                   }
                else if (line.startsWith("<measured_ions>")) {
                       handleMeasuredIons(reader,"measured_ions");

                   }
                   else if (line.startsWith("<scores")) {
                    handleScoresLine(line);

                }
                else {
                    ISpectrumPeak peak = DebugValues.readPeak(line);
                    if (peak == null)
                        break;
                    addMeasuredPeak(peak);

                }
                line = reader.readLine();
            }
            return line;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public String getScanId() {
        return m_ScanId;
    }

    public IonType getType() {
        return m_Type;
    }

    public String getPeptideId() {
        return m_PeptideId;
    }

    public IonUseScore getScore()
    {
        return m_Score;
    }


    public void setScore(IonUseScore score)
    {
          m_Score.set(score);
    }


    @Override
    public int compareTo(final DebugDotProduct o) {
        if(this == o)
            return 0;
       int ret =  getScanId().compareTo(o.getScanId());
       if(ret != 0)
           return ret;
        ret =  getPeptideId().compareTo(o.getPeptideId()) ;
        if(ret != 0)
            return ret;
        ret =  getType().compareTo(o.getType()) ;
        if(ret != 0)
            return ret;
        return 0;
     }

    private void handleIons(final LineNumberReader pReader,String start) throws IOException {
        String nowLine = pReader.readLine();
        List<ITheoreticalPeak> holder = new ArrayList<ITheoreticalPeak>();

        while (!("</" + start + ">").equals(nowLine)) {
            AttributeParameterHolder params = new AttributeParameterHolder(nowLine);
            int ion = params.getIntParameter("mz");
            ITheoreticalPeak sp = new TheoreticalPeak(ion, 1.0F, null, getType());
            addTheoreticalPeak(sp);
            nowLine = pReader.readLine();
        }
        ITheoreticalPeak[] peaks = new ITheoreticalPeak[holder.size()];
        holder.toArray(peaks);
        ITheoreticalSpectrum sp = new ScoringSpectrum(0, null, peaks);
    }

    private void handleMeasuredIons(final LineNumberReader pReader,String start) throws IOException {
        String nowLine = pReader.readLine();
        List<ITheoreticalPeak> holder = new ArrayList<ITheoreticalPeak>();

        while (!("</" + start + ">").equals(nowLine)) {
            if(nowLine.startsWith("<total_peaks")) {
                nowLine = pReader.readLine();
                continue;
            }
            AttributeParameterHolder params = new AttributeParameterHolder(nowLine);
            int ion = params.getIntParameter("mz");
            double score = params.getDoubleParameter( "score");
             ISpectrumPeak sp = new SpectrumPeak(ion, (float)score);
            addMeasuredPeak(sp);
            nowLine = pReader.readLine();
        }
        ITheoreticalPeak[] peaks = new ITheoreticalPeak[holder.size()];
        holder.toArray(peaks);
        ITheoreticalSpectrum sp = new ScoringSpectrum(0, null, peaks);
    }


    public void addMatchedPeaks(DebugMatchPeak added) {
        m_MatchedPeaks.add(added);
    }


    public DebugMatchPeak[] getMatchedPeakss() {
        return m_MatchedPeaks.toArray(new DebugMatchPeak[0]);
    }


    private void handleMatchLine(String pLine, IonType type) {
        AttributeParameterHolder params = new AttributeParameterHolder(pLine);
        int offset = params.getIntParameter("offset");
        int ion = params.getIntParameter("ion");
        double added = params.getDoubleParameter("added");
        DebugMatchPeak dp = new DebugMatchPeak(offset, added, ion, type);
        addMatchedPeaks(dp);
    }


    private void handleScoresLine(String pLine) {
        AttributeParameterHolder params = new AttributeParameterHolder(pLine);
        int totsl = params.getIntParameter("total_calls");
        int count = params.getIntParameter("count");
        double score = params.getDoubleParameter("score");
        final IonUseScore useScore = getScore();
        final IonType type = getType();
        useScore.setScore(type,score);
        useScore.setCount(type,count);

    }

}
