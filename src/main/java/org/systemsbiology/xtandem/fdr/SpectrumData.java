package org.systemsbiology.xtandem.fdr;

/**
* org.systemsbiology.xtandem.fdr.SpectrumData
*
* @author Steve Lewis
* @date 8/26/13
*/
public class SpectrumData {
    private final double expectedValue;
    private final double hyperScoreValue;
    private final double retentionTime;
    private final boolean trueHit;
    private final boolean modified;

    public SpectrumData(double expectedValue, double hyperScoreValue, boolean trueHit, boolean modified,double retentionTime) {
        this.expectedValue = expectedValue;
        this.hyperScoreValue = hyperScoreValue;
        this.trueHit = trueHit;
        this.modified = modified;
        this.retentionTime = retentionTime;
     }

    public double getExpectedValue() {
        return expectedValue;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean isTrueHit() {
        return trueHit;
    }

    public double getHyperScoreValue() {
        return hyperScoreValue;
    }

    public double getRetentionTime() {
        return retentionTime;
    }
}
