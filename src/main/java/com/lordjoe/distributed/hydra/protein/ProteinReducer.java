package com.lordjoe.distributed.hydra.protein;

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
public class ProteinReducer extends AbstractTandemFunction implements IReducerFunction<String, String, String, String> {

    public ProteinReducer(final XTandemMain pMain) {
        super(pMain);
    }

    /**
     * this is what a reducer does
     *
     * @param sequence
     * @param values
     * @param consumer @return iterator over mapped key values
     */

    @SuppressWarnings("NullableProblems")
    @Override
    public void handleValues(@Nonnull final String sequence, @Nonnull final Iterable<String> values, final IKeyValueConsumer<String, String>... consumer) {
        IPolypeptide pp = Polypeptide.fromString(sequence);

        StringBuilder sb = new StringBuilder();
        // should work even if we use a combiner

        int numberNonDecoy = 0;
        //noinspection UnusedDeclaration
        int numberDecoy = 0;

        for (String val : values) {
            if (sb.length() > 0)
                sb.append(";");

            //noinspection RedundantStringToString
            String str = val.toString();
            if (str.startsWith("DECOY"))
                numberDecoy++;
            else
                numberNonDecoy++;

            sb.append(str);
        }

        // same peptide is in decoy and non-decoy
        //noinspection StatementWithEmptyBody
        if (numberNonDecoy > 0) {
            // todo handle mixed decoy/non-decoy peptide
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

        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < consumer.length; i++) {
            consumer[i].consume(new KeyValueObject<String, String>(keytr, peptideString));
        }


    }


}
