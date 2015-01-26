package org.systemsbiology.xtandem.fdr;

/**
 * org.systemsbiology.xtandem.fdr.IDoscoveryDataHolder
 *
 * @author Steve Lewis
 * @date 09/05/13
 */
public interface IDiscoveryDataHolder {
    public static IDiscoveryDataHolder[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IDiscoveryDataHolder.class;


    /**
     * drop all data
     */
    public void clear( ) ;

    /**
     * add a discovery from true data
     * @param score  score - should be > 0
     */
    public void addTrueDiscovery(double score) ;

    /**
     * add a discovery from decoy data
     * @param score  score - should be > 0
     */
    public void addFalseDiscovery(double score) ;

    /**
     * return FDR
     * @param score  score - should be > 0
     */
    public double computeFDR(double score) ;

    /**
     * number of true samples above score
     * @param score  score - should be > 0
     */
    public int getNumberTruePositivesAbove(double score) ;

    /**
     * number of false samples above score
     * @param score  score - should be > 0
     */
    public int getNumberFalsePositivesAbove(double score) ;

    /**
     * return the lowest score with values
     * Note this may be an approximation due to binning
     * @return
     */
    public double getFirstScore();

    /**
     * return the hihgest score with values
     * Note this may be an approximation due to binning
     * @return
     */
    public double getLastScore();

}
