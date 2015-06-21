package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.sax.IXMLAppender;
import org.systemsbiology.xtandem.*;

/**
 * org.systemsbiology.xtandem.scoring.LowMemoryIdOnlySpectrum
 *  Spectrum used when generating a ppepxml result file when only
 *  id and mass charge ratio is needed
 * @author Steve Lewis
 * @date 6/21/2015
 */
public class LowMemoryIdOnlySpectrum implements IMeasuredSpectrum {

    private final String id;
    private final int precursoCharge;
    private final int peaksCount;
    private final double precursoMass;
    private final double precursoMassChargeRatio;

    public LowMemoryIdOnlySpectrum(IMeasuredSpectrum spec) {
        id = spec.getId();
        precursoCharge = spec.getPrecursorCharge();
        precursoMass = spec.getPrecursorMass();
        precursoMassChargeRatio = spec.getPrecursorMassChargeRatio();
        peaksCount = spec.getPeaksCount();
    }

    @Override
    public void serializeAsString(IXMLAppender adder) {
        if (true) throw new UnsupportedOperationException("Unspported by Low Memory Class");

    }

    @Override
    public boolean equivalent(IMeasuredSpectrum test) {
        if (true) throw new UnsupportedOperationException("Unspported by Low Memory Class");
        return false;
    }

    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public IMeasuredSpectrum asImmutable() {
        return this;
    }

    @Override
    public MutableMeasuredSpectrum asMmutable() {
        if (true) throw new UnsupportedOperationException("Unspported by Low Memory Class");
        return null;
    }

    @Override
    public int getPrecursorCharge() {
        return precursoCharge;
    }

    @Override
    public double getPrecursorMass() {
        return precursoMass;
    }

    @Override
    public double getPrecursorMassChargeRatio() {
          return precursoMassChargeRatio;
    }

    @Override
    public String getId() {
         return id;
    }

    @Override
    public int getIndex() {
        if (true) throw new UnsupportedOperationException("Unspported by Low Memory Class");
        return 0;
    }

    @Override
    public ISpectralScan getScanData() {
        if (true) throw new UnsupportedOperationException("Unspported by Low Memory Class");
        return null;
    }

    @Override
    public int getPeaksCount() {
          return peaksCount;
    }

    @Override
    public ISpectrumPeak[] getPeaks() {
        if (true) throw new UnsupportedOperationException("Unspported by Low Memory Class");
        return new ISpectrumPeak[0];
    }

    @Override
    public ISpectrumPeak[] getNonZeroPeaks() {
        if (true) throw new UnsupportedOperationException("Unspported by Low Memory Class");
        return new ISpectrumPeak[0];
    }

    @Override
    public double getMaxIntensity() {
        if (true) throw new UnsupportedOperationException("Unspported by Low Memory Class");
        return 0;
    }

    @Override
    public double getSumIntensity() {
        if (true) throw new UnsupportedOperationException("Unspported by Low Memory Class");
        return 0;
    }

    @Override
    public boolean equivalent(ISpectrum o) {
        if (true) throw new UnsupportedOperationException("Unspported by Low Memory Class");
        return false;
    }
}
