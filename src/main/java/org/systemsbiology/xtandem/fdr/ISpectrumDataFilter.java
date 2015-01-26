package org.systemsbiology.xtandem.fdr;

/**
 * org.systemsbiology.xtandem.fdr.IScanHitFilter
 *
 * @author Steve Lewis
 * @date 8/26/13
 */
public interface ISpectrumDataFilter {

    /**
     * if true we use the scan - if not it is discarded
     * @return
     */
    public boolean isSpectrumKept(SpectrumData spec);
}
