package com.lordjoe.distributed.hydra.scoring;

import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
* com.lordjoe.distributed.hydra.scoring.ScoredScanComparator
 * sort scores highest first
* User: Steve
* Date: 3/20/2015
*/
class ScoredScanComparator implements Comparator<ScoredScan> {
    @Override
    public int compare(final ScoredScan o1, final ScoredScan o2) {
        int ret = Double.compare(o2.getBestHyperscore(), o1.getBestHyperscore());
        if (ret != 0)
            return ret;
        return o1.getId().compareTo(o2.getId());
    }
}
