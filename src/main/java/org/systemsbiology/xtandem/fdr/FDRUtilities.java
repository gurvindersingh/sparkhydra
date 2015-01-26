package org.systemsbiology.xtandem.fdr;

/**
 * org.systemsbiology.xtandem.fdr.FDRUtilities
 *
 * @author Steve Lewis
 * @date 09/05/13
 */
public class FDRUtilities {
    public static FDRUtilities[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = FDRUtilities.class;

    public static final int NUMBER_BINS = 3200;

    /**
     * we can hard code a reasonable range of values
     */
    public static final double MINIMUM_SCORE = 1.0e-008;
    public static final double MAXIMUM_SCORE = 1.0e008;
    private static final double MINIMUM_SCORE_LOG = Math.log10(MINIMUM_SCORE);
    private static final double MAXIMUM_SCORE_LOG = Math.log10(MAXIMUM_SCORE);
    private static final double DEL_LOG = (MAXIMUM_SCORE_LOG - MINIMUM_SCORE_LOG) / NUMBER_BINS;


    /**
      * convert a score into an index NUMBER_BINS
      * @param score
      * @return   the index >= 0 <  NUMBER_BINS
      */
    public static int asBin(double score)
    {
        if(score <= MINIMUM_SCORE)
            return 0;
        if(score >= MAXIMUM_SCORE)
            return NUMBER_BINS - 1;
        double scoreLog =  Math.log10(score);
        scoreLog -=  MINIMUM_SCORE_LOG;
        int ret = (int)(scoreLog / DEL_LOG);
        return ret;
    }

    /**
     * convert - approximately from a bin number to a  score
     * @param index   bin number
     * @return
     */
    public static double fromBin(int index) {
        if(index <= 0)
            return MINIMUM_SCORE;
        if(index >= NUMBER_BINS)
            return MAXIMUM_SCORE;
        return MINIMUM_SCORE * Math.pow(10,DEL_LOG * index);
     }

    /**
     * return a discovery holder for default algorithm and direction
     *
     * @return !null   IDiscoveryDataHolder
     */
    public static IDiscoveryDataHolder getDiscoveryDataHolder() {
    //    return new StupidDiscoveryDataHolder();
        return getDiscoveryDataHolder(null);
    }

    /**
     * return a discovery holder for default algorithm and direction
     *
     * @param algorithmName POSSIBLY NULL ALGORITHM NAME
        * @return
     */
    public static IDiscoveryDataHolder getDiscoveryDataHolder(String algorithmName) {
         return getDiscoveryDataHolder(algorithmName, true);

    }

    /**
     * return a discovery holder for default algorithm and direction
     *
     * @param algorithmName POSSIBLY NULL ALGORITHM NAME
     * @param direction     true is ascending is good
     * @return
     */
    public static IDiscoveryDataHolder getDiscoveryDataHolder(String algorithmName, boolean direction) {
        return new FalseDiscoveryDataHolder(algorithmName,   direction );
    }

    /**
     * come up with a list of FDRs in the hiolder
     * @param hd !null holder
     * @return  strung with all values
     */
    public static String listFDR(IDiscoveryDataHolder hd) {
        double start = hd.getFirstScore();
        double last = hd.getLastScore();
        StringBuilder sb = new StringBuilder();

         for (double score = start; score <= last; score *= 1.1) {
             String scoreStr = String.format("%10.3e",score);
             final double v = hd.computeFDR(score);
             String fdrStr = String.format("%10.4e", v);
             sb.append(scoreStr + "," + fdrStr);
             sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * comu up with a list of FDRs in the hiolder
     * @param hd !null holder
     * @return  strung with all values
     */
    public static String listFDRAndCount(IDiscoveryDataHolder hd) {
        double start = hd.getFirstScore();
        double last = hd.getLastScore();
        StringBuilder sb = new StringBuilder();

         for (double score = start; score <= last; score *= 1.1) {
             String scoreStr = String.format("%10.3e",score);
             final double v = hd.computeFDR(score);
             String fdrStr = String.format("%10.4f", v);
             String count = Integer.toString(hd.getNumberTruePositivesAbove(score));
             String fcount = Integer.toString(hd.getNumberFalsePositivesAbove(score));
              sb.append(scoreStr + "," + fdrStr + "," + count+ "," + fcount);
             sb.append("\n");
        }
        return sb.toString();
    }


    /**
     * comu up with a list of scores, FDRs, TPs, FPs plus TP rate (TP(score)/Total TP) and FP rate (FP(score)/total FP) in the holder
     * @param hd !null holder
     * @return  string with all values
     */
    public static String listFDRAndRates(IDiscoveryDataHolder hd) {
        double start = hd.getFirstScore();
        double last = hd.getLastScore();
        StringBuilder sb = new StringBuilder();

         for (double score = start; score <= last; score *= 1.05) {
             String scoreStr = String.format("%10.3e",score);
             final double v = hd.computeFDR(score);
             String fdrStr = String.format("%10.4f", v);
             String count = Integer.toString(hd.getNumberTruePositivesAbove(score));
             String fcount = Integer.toString(hd.getNumberFalsePositivesAbove(score));
             // calculating TP rate (TP(score)/Total TP) for ROC curve
             // for Total TP use getFirstScore() working with doubles, giving doubles as result
             double numberTruePositivesAboveFirstScore = hd.getNumberTruePositivesAbove(start);
             double tpr = hd.getNumberTruePositivesAbove(score) / numberTruePositivesAboveFirstScore;
             // format double up to 4 decimals before printing out
             String tprStr = String.format("%10.4f", tpr);
             // String tprate = Double.toString(tpr);

             // calculating FP rate (FP(score)/total FP) for ROC curve
             double numberFalsePositivesAboveFirstScore = hd.getNumberFalsePositivesAbove(start);
             double fpr = hd.getNumberFalsePositivesAbove(score) / numberFalsePositivesAboveFirstScore;
             String fprStr = String.format("%10.4f", fpr);
             //String fprate = Double.toString(fpr);
              sb.append(scoreStr + "," + fdrStr + "," + count+ "," + fcount + "," + tprStr + "," + fprStr);
             sb.append("\n");
        }
        return sb.toString();
    }


}
