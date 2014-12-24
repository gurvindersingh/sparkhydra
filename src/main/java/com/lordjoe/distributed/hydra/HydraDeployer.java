package com.lordjoe.distributed.hydra;

import com.lordjoe.distributed.*;

/**
 * com.lordjoe.distributed.hydra.HydraDeployer
 * User: Steve
 * Date: 10/9/2014
 */
public class HydraDeployer {

    public static void main(String[] args) {
        SparkDeployer.main(args); // this makes sure we pick up hydra path and dependencies
    }

}
