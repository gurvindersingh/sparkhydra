package org.systemsbiology.xtandem.fdr;

import java.util.*;

/**
 * org.systemsbiology.xtandem.fdr.OrSpectrumDataFilter
 *
 * @author Steve Lewis
 * @date 8/26/13
 */
public class OrSpectrumDataFilter implements  ISpectrumDataFilter {

    private final List<ISpectrumDataFilter> m_Filters = new ArrayList<ISpectrumDataFilter>();

    public OrSpectrumDataFilter(ISpectrumDataFilter... filters) {
        m_Filters.addAll(Arrays.asList(filters)) ;
    }

    public void addFilter(ISpectrumDataFilter added) {
        m_Filters.add(added);
    }

    /**
     * if true we use the scan - if not it is discarded
     *
     * @return
     */
    @Override
    public boolean isSpectrumKept(SpectrumData spec) {
        if(m_Filters.isEmpty())
            return true;
        for (ISpectrumDataFilter filter : m_Filters) {
           if(filter.isSpectrumKept(spec))
               return true;
        }
        return false;
    }
}
