package com.lordjoe.distributed.protein;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.test.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
* com.lordjoe.distributed.protein.DigestProteinFunction
* User: Steve
* Date: 10/14/2014
*/
public class DigestProteinFunction extends AbstractLoggingFlatMapFunction<IProtein,  IPolypeptide> {
    private  final XTandemMain app;
    private  final IPeptideDigester digester;
    private  final PeptideModification[] peptideModifications;
    private  final  boolean m_GenerateDecoysForModifiedPeptides;

    public DigestProteinFunction(XTandemMain pApp) {
        app = pApp;
        digester = app.getDigester();
        ScoringModifications scoringMods = app.getScoringMods();
        peptideModifications = scoringMods.getModifications();
        m_GenerateDecoysForModifiedPeptides = app.getBooleanParameter(XTandemUtilities.CREATE_DECOY_FOR_MODIFIED_PEPTIDES_PROPERTY, Boolean.FALSE);

     }

    public boolean isGenerateDecoysForModifiedPeptides() {
        return m_GenerateDecoysForModifiedPeptides;
    }

    @Override
    public Iterable<IPolypeptide> doCall(final IProtein prot)  {
        List holder = new ArrayList<IPolypeptide>();
            // do a boolean for a peptide belonging to a decoy protein, but use the public isDecoy boolean/method in Protein class

        if(TestUtilities.isInterestingProtein(prot))
            TestUtilities.breakHere();

        IPolypeptide[] pps = digester.digest(prot);

        boolean isDecoy = prot.isDecoy();

           for (int i = 0; i < pps.length; i++) {
            IPolypeptide pp = pps[i];

            if (!pp.isValid())
                continue;

            // hadoop write intermediate seq finder
             holder.add(pp);

            //   if(isDecoy)
            //       continue; // skip the rest of the loop

            // if it is decoy, don't add peptideModifications to it
            if (!isDecoy || isGenerateDecoysForModifiedPeptides()) {
                //  generate modified peptides and add to the output
                  IModifiedPeptide[] modifications = ModifiedPolypeptide.buildModifications(pp, peptideModifications);
                for (int m = 0; m < modifications.length; m++) {
                    IModifiedPeptide modification = modifications[m];
                    holder.add(modification);

                }
            }

        }

// All this should happen at the digest step
//        boolean semiTryptic = digester.isSemiTryptic();
//        if (semiTryptic) {
//            IPolypeptide[] semipps = digester.addSemiCleavages(prot);
//            for (int j = 0; j < semipps.length; j++) {
//                IPolypeptide semipp = semipps[j];
//                if (!semipp.isValid())
//                    continue;
//                holder.add(semipp);
//                IModifiedPeptide[] modifications = ModifiedPolypeptide.buildModifications(semipp, peptideModifications);
//                for (int k = 0; k < modifications.length; k++) {
//                    IModifiedPeptide modification = modifications[k];
//                    holder.add(modification);
//                  }
//            }
//        }

         return holder;
      }
}
