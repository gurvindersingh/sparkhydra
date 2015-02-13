package com.lordjoe.distributed.hydra.protein;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.tandem.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import javax.annotation.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.spark.ProteinMapper
 * Used for generating a fragment library
 * User: Steve
 * Date: 9/24/2014
 */
public class ProteinMapper extends AbstractTandemFunction  implements  IMapperFunction<String, String, String, String> {


    private long m_FragmentIndex;
    private List<PeptideModification> m_Modifications = new ArrayList<PeptideModification>();
    private boolean m_GenerateDecoysForModifiedPeptides;


    public ProteinMapper(XTandemMain app) {
         super(app);


        ScoringModifications scoringMods = app.getScoringMods();

        PeptideModification[] modifications = scoringMods.getModifications();
        m_Modifications.addAll(Arrays.asList(modifications));

        setGenerateDecoysForModifiedPeptides(app.getBooleanParameter(XTandemUtilities.CREATE_DECOY_FOR_MODIFIED_PEPTIDES_PROPERTY, Boolean.FALSE));

        // hard code modifications of cystein
    }

    public boolean isGenerateDecoysForModifiedPeptides() {
        return m_GenerateDecoysForModifiedPeptides;
    }

    public void setGenerateDecoysForModifiedPeptides(boolean generateDecoysForModifiedPeptides) {
        m_GenerateDecoysForModifiedPeptides = generateDecoysForModifiedPeptides;
    }

    public DigesterDescription getDescription() {
        DigesterDescription ret = new DigesterDescription();
        ret.setVersion(DigesterDescription.CURRENT_DIGESTER_VERSION);
        ret.setDigester((PeptideBondDigester) getDigester());
        PeptideModification[] modifications = getModifications();
        if (modifications != null && modifications.length > 0) {
            for (int i = 0; i < modifications.length; i++) {
                PeptideModification mod = modifications[i];
                ret.addModification(modifications[i]);
            }
        }
        // if true we better have decoys in the database
        boolean hasDecoys = getApplication().getBooleanParameter(XTandemUtilities.CREATE_DECOY_PEPTIDES_PROPERTY, Boolean.FALSE);
        ret.setHasDecoys(hasDecoys);

        return ret;
    }


    public String asXMLString() {
        return getDescription().asXMLString();
    }

    public PeptideModification[] getModifications() {
        return m_Modifications.toArray(PeptideModification.EMPTY_ARRAY);
    }

    public void setModifications(final PeptideModification[] pModifications) {
        m_Modifications.clear();
        m_Modifications.addAll(Arrays.asList(pModifications));
    }

    //
//    public static PrintWriter buildRemoteWriter(IFileSystem fs, String remoteDirectory, File localFile) {
//        String name = remoteDirectory + "/" + localFile.getName();
//        OutputStream os = fs.openFileForWrite(name);
//        return new PrintWriter(new OutputStreamWriter(os));
//    }
//
//
    public IPeptideDigester getDigester() {
        return getApplication().getDigester();
    }

    public long getFragmentIndex() {
        return m_FragmentIndex;
    }

    public long getAndIncrementFragmentIndex() {
        return m_FragmentIndex++;
    }

    /**
     * this is what a Mapper does
     *
     * @param keyin
     * @param valuein
     * @return iterator over mapped key values
     */
    @Nonnull
    @Override
    public Iterable<KeyValueObject<String, String>> mapValues(@Nonnull final String annotation, @Nonnull final String sequence) {

        List<KeyValueObject<String, String>> holder = new ArrayList<KeyValueObject<String, String>>();


        IPeptideDigester digester = getDigester();
        IProtein prot = Protein.getProtein(annotation, annotation, sequence, null);

        // do a boolean for a peptide belonging to a decoy protein, but use the public isDecoy boolean/method in Protein class

        boolean isDecoy = prot.isDecoy();

        IPolypeptide[] pps = digester.digest(prot);
        PeptideModification[] modifications1 = getModifications();
        for (int i = 0; i < pps.length; i++) {
            IPolypeptide pp = pps[i];

            if (!pp.isValid())
                continue;

            // hadoop write intermediate seq finder
            writePeptide(pp, holder);

            //   if(isDecoy)
            //       continue; // skip the rest of the loop

            // if it is decoy, don't add modifications to it
            if (!isDecoy || isGenerateDecoysForModifiedPeptides()) {
                //  generate modified peptides and add to the output
                IModifiedPeptide[] modifications = ModifiedPolypeptide.buildModifications(pp, modifications1);
                for (int m = 0; m < modifications.length; m++) {
                    IModifiedPeptide modification = modifications[m];
                    writePeptide(modification, holder);

                }
            }

        }

        boolean semiTryptic = digester.isSemiTryptic();
        if (semiTryptic) {
            IPolypeptide[] semipps = digester.addSemiCleavages(prot);
            for (int j = 0; j < semipps.length; j++) {
                IPolypeptide semipp = semipps[j];
                if (!semipp.isValid())
                    continue;
                writePeptide(semipp, holder);
                IModifiedPeptide[] modifications = ModifiedPolypeptide.buildModifications(semipp, modifications1);
                for (int k = 0; k < modifications.length; k++) {
                    IModifiedPeptide modification = modifications[k];
                    writePeptide(modification, holder);

                }
            }
        }
         return holder;
     }


    protected void writePeptide(IPolypeptide pp,List<KeyValueObject<String,String>> holder) {
        long numberFragments = getAndIncrementFragmentIndex();

        String str = pp.toString();
        IProteinPosition[] proteinPositions = pp.getProteinPositions();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < proteinPositions.length; i++) {
            IProteinPosition ppx = proteinPositions[i];
            if(i > 0)
                sb.append(",");
            sb.append(pp.getId());
        }

        holder.add(new KeyValueObject<String, String>(str, sb.toString()));
    }

}
