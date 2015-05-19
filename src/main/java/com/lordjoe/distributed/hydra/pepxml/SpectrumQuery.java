package com.lordjoe.distributed.hydra.pepxml;

import org.systemsbiology.xtandem.IEquivalent;
import org.systemsbiology.xtandem.peptide.Polypeptide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * com.lordjoe.distributed.hydra.pepxml.SpectrumQuery
 *
 * @author Steve Lewis
 * @date 5/18/2015
 */
public class SpectrumQuery  implements IEquivalent<SpectrumQuery> {
    public final String spectrum;
    public final double mass;
    public final int charge;
    public final double retentionTime;
    private final List<SpectrumHit> hits = new ArrayList<SpectrumHit>();

    public SpectrumQuery(String spectrum,
                         double mass,
                         int charge,
                         double rewtentionTime
    ) {
        this.spectrum = spectrum;
        this.mass = mass;
        this.charge = charge;
        this.retentionTime = rewtentionTime;
    }

    public void addSpectrumHit(SpectrumHit hit) {
        hits.add(hit);
    }

    public int getHitsCount() {
        return hits.size();
    }

    public List<SpectrumHit> getHits() {
        return Collections.unmodifiableList(hits);
    }


    @Override
    public boolean equivalent(SpectrumQuery o) {
        if(getHitsCount() !=  o.getHitsCount())
            return false;

        boolean totallyEquivalent = true;

        for (int i = 0; i < hits.size(); i++) {
            SpectrumHit hit1 = hits.get(i);
            SpectrumHit hit2 = o.hits.get(i);
            if(!hit1.equivalent(hit2))
                totallyEquivalent = false;
        }

        if(totallyEquivalent)
            return true;


        List<SpectrumHit> cpHits = new ArrayList<SpectrumHit>(hits);
        List<SpectrumHit> cpoHits = new ArrayList<SpectrumHit>(o.hits);
        for (int i = 0; i < hits.size(); i++) {
            SpectrumHit hit1 = hits.get(i);
            SpectrumHit hit2 = o.hits.get(i);
            if(hit1.equivalent(hit2))   {
                cpHits.remove(hit1);
                cpoHits.remove(hit2);
            }
         }

        boolean equivalent = true;
        for (int i = 0; i < cpHits.size(); i++) {
            SpectrumHit hit1 = cpHits.get(i);
            for (SpectrumHit hit2 : cpoHits) {
                equivalent &= Polypeptide.equivalentSpectrum(hit1.peptide,hit2.peptide);
               }
        }

        return equivalent;
    }


}
