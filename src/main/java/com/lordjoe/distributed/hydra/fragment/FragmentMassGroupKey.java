package com.lordjoe.distributed.hydra.fragment;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.fragment.FragmentMassGroupKey
 * User: Steve
 * Date: 10/30/2014
 */
public class FragmentMassGroupKey implements Serializable {

    private double startMass;
    private double endMass;
    private int group;

    // Kryo wants this
    private FragmentMassGroupKey() {}

    public FragmentMassGroupKey(final double pStartMass, final double pEndMass ) {
        this(pStartMass,pEndMass,0);
    }
    public FragmentMassGroupKey(final double pStartMass, final double pEndMass, final int pGroup) {
          startMass = pStartMass;
          endMass = pEndMass;
          group = pGroup;
      }

    public double getStartMass() {
        return startMass;
    }

    public int getGroup() {
        return group;
    }

    public double getEndMass() {
        return endMass;
    }

    public boolean isMassMatching(double mass) {
        if(mass < startMass)
            return false;
        if(mass >= endMass)
              return false;
        return true;
     }
}
