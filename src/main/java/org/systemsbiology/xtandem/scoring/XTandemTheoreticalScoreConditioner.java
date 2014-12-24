package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;

/**
 * org.systemsbiology.xtandem.scoring.XTandemTheoreticalScoreConditioner
 * User: steven
 * Date: 1/19/11
 */
public class XTandemTheoreticalScoreConditioner implements ITheoreticalPeakConditioner {
    public static final XTandemTheoreticalScoreConditioner[] EMPTY_ARRAY = {};


    public XTandemTheoreticalScoreConditioner() {
    }

    /**
     * return a modified peak
     *
     * @param peak      !null peak
     * @param addedData any additional data
     * @return possibly null conditioned peak - null says ignore
     */
    @Override
    public ITheoreticalPeak condition(final ITheoreticalPeak peak, final Object... addedData) {
        double mass = peak.getMassChargeRatio();
        float weight = peak.getPeak();
        IonType type = peak.getType();
        IPolypeptide peptide = peak.getPeptide();
        if (peak instanceof PeptideIon) {
            String sequence1 = peptide.getSequence();
            if (sequence1.length() > 0) {
                char second = sequence1.charAt(sequence1.length() - 1);
                float factor = TandemScoringAlgorithm.theoreticalPeakMultiplier(type, second);

                weight *= factor;

            }
            // see mscore::Add_b
            if (((PeptideIon) peak).getIndexInParent() == 1 && type == IonType.B) {
                if (sequence1.charAt(0) == 'P')
                    weight *= 10;
                else
                    weight *= 3;
            }

        }

        if(ScoringUtilities.SHOW_SCORING)
            XMLUtilities.outputLine(peptide + ":" + type + " mass:" + XTandemUtilities.formatDouble(mass, 2) + " weight:" + weight);

        return new TheoreticalPeak(mass, weight, peptide, type);
    }


}
