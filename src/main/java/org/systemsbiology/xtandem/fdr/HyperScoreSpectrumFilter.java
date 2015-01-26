package org.systemsbiology.xtandem.fdr;

/**
 * org.systemsbiology.xtandem.fdr.KScoreSpectrumFilter
 *
 * @author Steve Lewis
 * @date 8/26/13
 */
public class HyperScoreSpectrumFilter implements  ISpectrumDataFilter {

    private  final double m_MinimumKScore;
    private  final double m_MaximumKScore;

    public HyperScoreSpectrumFilter(double minimumKScore) {
        this(minimumKScore,Double.MAX_VALUE) ;
    }

    public HyperScoreSpectrumFilter(double minimumKScore, double maximumKScore) {
        m_MinimumKScore = minimumKScore;
        m_MaximumKScore = maximumKScore;
    }

    /**
     * if true we use the scan - if not it is discarded
     *
     * @return
     */
    @Override
    public boolean isSpectrumKept(SpectrumData spec) {
        double hyperScoreValue = spec.getHyperScoreValue();
        if(hyperScoreValue < m_MinimumKScore)
            return false;
        //noinspection SimplifiableIfStatement,RedundantIfStatement
        if(hyperScoreValue > m_MaximumKScore)
            return false;
        return true;
    }
}
