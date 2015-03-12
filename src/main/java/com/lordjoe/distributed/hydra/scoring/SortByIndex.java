package com.lordjoe.distributed.hydra.scoring;

import com.lordjoe.distributed.*;
import org.systemsbiology.xtandem.scoring.*;

/**
*  com.lordjoe.distributed.hydra.scoring.SortByIndex
*/
public class SortByIndex extends AbstractLoggingFunction<IScoredScan, Integer> {
   @Override
   public Integer doCall(final IScoredScan v1) throws Exception {
       return v1.getRaw().getIndex();
   }
}
