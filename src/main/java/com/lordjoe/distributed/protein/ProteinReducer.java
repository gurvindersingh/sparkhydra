package com.lordjoe.distributed.protein;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.tandem.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

import javax.annotation.*;

/**
 * org.systemsbiology.xtandem.protein.ProteinReducer
 * Used for generating a fragment library
 * User: Steve
 * Date: 9/24/2014
 */
public class ProteinReducer extends AbstractTandemFunction implements IReducerFunction<String, String,String, String > {

    public ProteinReducer(final XTandemMain pMain) {
        super(pMain);
    }

    /**
     * this is what a reducer does
     *
     * @param key
     * @param values
     * @param consumer @return iterator over mapped key values
     */
    @Nonnull
    @Override
    public void handleValues(@Nonnull final String sequence, @Nonnull final Iterable<String> values, final IKeyValueConsumer<String, String>... consumer) {
        IPolypeptide pp = Polypeptide.fromString(sequence);

        StringBuilder sb = new StringBuilder();
        // should work even if we use a combiner

        int numberNonDecoy = 0;
        int numberDecoy = 0;

        for (String val : values) {
            if (sb.length() > 0)
                sb.append(";");

            String str = val.toString();
            if (str.startsWith("DECOY"))
                numberDecoy++;
            else
                numberNonDecoy++;

            sb.append(str);
        }

        if (numberDecoy > 0) {
            // same peptide is in decoy and non-decoy
            if (numberNonDecoy > 0) {
                // todo handle mixed decoy/non-decoy peptide
            }

        }

        String proteins = sb.toString();

        XTandemMain application = getApplication();
        MassType massType = application.getMassType();
        String keytr;
        String peptideString;
        switch (massType) {
            case monoisotopic:

                double mMass = pp.getMatchingMass();
                int monomass = XTandemUtilities.getDefaultConverter().asInteger(mMass);
                keytr = String.format("%06d", monomass);
                peptideString = sequence + "," + String.format("%10.4f", mMass) + "," + monomass + "," + proteins;
                break;
            case average:
                double aMass = pp.getMatchingMass();
                int avgmass = XTandemUtilities.getDefaultConverter().asInteger(aMass);
                keytr = String.format("%06d", avgmass);
                peptideString = sequence + "," + String.format("%10.4f", aMass) + "," + avgmass + "," + proteins;
                break;
            default:
                return;
        }

        for (int i = 0; i < consumer.length; i++) {
              consumer[i].consume(new KeyValueObject<String, String>(keytr, peptideString));

        }


    }


}
