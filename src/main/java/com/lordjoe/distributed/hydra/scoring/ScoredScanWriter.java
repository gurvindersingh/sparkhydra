package com.lordjoe.distributed.hydra.scoring;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.scoring.*;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.scoring.ScoredScanWriter
 * User: Steve
 * Date: 10/20/2014
 */
public interface ScoredScanWriter extends Serializable {

    /**
     * write the start of a file
     * @param out where to append
     * @param app appliaction data
     */
    public void appendHeader( Appendable out,XTandemMain app) ;

    /**
     * write the end of a file
     * @param out where to append
     * @param app appliaction data
     */
    public void appendFooter( Appendable out,XTandemMain app) ;


    /**
     * write one scan
     * @param out where to append
     * @param app appliaction data
     * @param scan one scan
     */
    public void appendScan( Appendable out,XTandemMain app,IScoredScan scan) ;

}
