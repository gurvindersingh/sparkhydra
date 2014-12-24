package org.systemsbiology.xtandem;

import java.io.*;

/**
 * org.systemsbiology.xtandem.TaxonomyTranch
 * User: Steve
 * Date: Apr 1, 2011
 */
public class TaxonomyTranch implements Serializable {
    public static final TaxonomyTranch[] EMPTY_ARRAY = {};

    /**
     * accepts all
     */
    public static final TaxonomyTranch NULL_TRANCH = new TaxonomyTranch(null,null);


    private final Integer m_StartProtein;
    private final Integer m_EndProtein;
    private final Integer m_Index;
    private final Integer m_Repeat;

    public TaxonomyTranch(final Integer pIndex, final Integer pRepeat) {
        m_Index = pIndex;
        m_Repeat = pRepeat;
        m_StartProtein = null; // use entire range
        m_EndProtein = null; // use entire range
    }

    public TaxonomyTranch(final Integer pIndex, final Integer pRepeat,int start,int end) {
        m_Index = pIndex;
        m_Repeat = pRepeat;
        m_StartProtein = start; // use entire range
        m_EndProtein = end; // use entire range
    }

    public boolean isAccepted(int index)
    {
        if(m_StartProtein != null)
            if(index < m_StartProtein)
                return false;
        if(m_EndProtein != null)
            if(index > m_EndProtein)
                return false;
        if(m_Repeat == null)
            return true;
        boolean keep = index % m_Repeat == m_Index;
        if(!keep)
            return false;
        return keep; // might break here this is an interesting case
    }
}
