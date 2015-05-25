package com.lordjoe.distributed.hydra.comet;

import org.systemsbiology.xtandem.ionization.IonType;

/**
 * com.lordjoe.distributed.hydra.comet.TestBinChargeIonIndex
 *
 * @author Steve Lewis
 * @date 5/25/2015
 */
public class TestBinChargeIonIndex extends BinnedChargeIonIndex {

    public final double mass;
    public TestBinChargeIonIndex(int pIndex, int pCharge, IonType pType, int pPeptidePosition,double pmass) {

        super(pIndex, pCharge, pType, pPeptidePosition);
        mass = pmass;
    }
}
