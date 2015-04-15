package org.systemsbiology.xtandem.ionization;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

/**
 * org.systemsbiology.xtandem.ionization.PeptideIon
 * User: steven
 * Date: Jan 10, 2011
 */
public class PeptideIon implements IPeptideIon, ITheoreticalPeak {
    public static final PeptideIon[] EMPTY_ARRAY = {};


    private final org.systemsbiology.xtandem.ionization.IonType m_Type;
    private final IPolypeptide m_Peptide;
    private final int m_Charge;
    private final int m_IndexInParent;
    private final double m_MassChargeRatio;
    private final int m_BinnedIndexInParent;

    public PeptideIon(final IPolypeptide pPetide, final org.systemsbiology.xtandem.ionization.IonType pType, int charge, double massChargeRatio, int index, int binnedIndex) {
        m_Peptide = pPetide;
        m_Type = pType;
        m_Charge = charge;
        m_MassChargeRatio = massChargeRatio;
        m_IndexInParent = index;
        m_BinnedIndexInParent = binnedIndex;
   //     if (index >= pPetide.getSequenceLength())
     //       throw new IllegalStateException("problem"); // ToDo change      m_Peptide = pPetide;

    }

    public int getIndexInParent() {
        return m_IndexInParent;
    }

    public int getBinnedIndexInParent() {
        return m_BinnedIndexInParent;
    }

    /**
     * return true if the spectrum is immutable
     *
     * @return
     */
    @Override
    public boolean isImmutable() {
        return true;
    }

    @Override
    public String getSequence() {
        return getPeptide().getSequence();
    }

    /**
     * return type A,B,C,X,Y,Z
     *
     * @return
     */
    @Override
    public IonType getType() {
        return m_Type;
    }

    @Override
    public AminoTerminalType getTerminalType() {
        return getType().getTerminal();
    }

    @Override
    public IPolypeptide getPeptide() {
        return m_Peptide;
    }

    @Override
    public int getCharge() {
        return m_Charge;
    }

    public double getMass() {
        return m_MassChargeRatio * m_Charge;
    }

    @Override
    public double getMassChargeRatio() {
        return m_MassChargeRatio;
    }

    /**
     * by definition all peaks are 1 - change this in scoring algorithm
     *
     * @return
     */
    @Override
    public float getPeak() {
        return 1;
    }

    /**
     * return as an immutble peak
     *
     * @return as above
     */
    @Override
    public ISpectrumPeak asImmutable() {
        return this;   // we are immutable
    }

    @Override
    public int compareTo(final ISpectrumPeak o) {
        if (this == o)
            return 0;
        double m1 = getMassChargeRatio();
        double m2 = o.getMassChargeRatio();
        if (m1 != m2)
            return m1 < m2 ? -1 : 1;

        m1 = getPeak();
        m2 = o.getPeak();
        if (m1 != m2)
            return m1 < m2 ? -1 : 1;
        return 0;
    }

    /**
     * return true if this and o are 'close enough'
     *
     * @param !null o
     * @return as above
     */
    @Override
    public boolean equivalent(ISpectrumPeak o) {
        if (this == o)
            return true;
        double m1 = getMassChargeRatio();
        double m2 = o.getMassChargeRatio();
        if (!XTandemUtilities.equivalentDouble(m1, m2))
            return false;

        m1 = getPeak();
        m2 = o.getPeak();
        if (!XTandemUtilities.equivalentDouble(m1, m2))
            return false;
        return true;
    }


    /**
     * return the mass - which depends on charge
     *
     * @param su     - calculator - has monoisotopic or average
     * @param charge positive charge
     * @return as above
     */
    @Override
    public double getMass(SequenceUtilities su, int charge) {
        double sequenceMass = su.getSequenceMass(m_Peptide);
        IonType type = getType();
        double added = su.getAddedMass(type);
        sequenceMass += added;
        // every charge adds the mass of a proton
        sequenceMass += (charge - 1) * su.getdProton();
        return sequenceMass;
    }

    /**
     * return the mass/ charge ratio
     *
     * @param su     - calculator - has monoisotopic or average
     * @param charge positive charge
     * @return mass charge ratio
     */
    @Override
    public double getMassChargeRatio(SequenceUtilities su, int charge) {
        return getMass(su, charge) / charge;
    }


    @Override
    public String toString() {
        return getType().toString() + ":" + getPeptide().getSequence() + " " + (int) getMassChargeRatio();
    }
}
