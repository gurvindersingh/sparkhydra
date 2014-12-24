package org.systemsbiology.xtandem.hadoop;

import org.apache.hadoop.io.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.hadoop.FastaHadoopLoader
 * Code called by hadoop - probably a reducer to generate peptides
 * User: Steve
 * Date: Apr 11, 2011
 */
public class FastaHadoopLoader {
    public static final FastaHadoopLoader[] EMPTY_ARRAY = {};


    public static final String[] INTERESTING_SEQUENCES =
            {
//                    "ETGELEELLEPILAN",
//                    "IVSTEGNVPQTLAPVPYETFI",
//                    "SGGSVQGALDSLLNGDV",
//                    "FQDVLDNFLQG",
//                    "EHTKLLHS",

            };

    public static final Set<String> INTERESTING_SEQUENCE_SET = new HashSet<String>(Arrays.asList(INTERESTING_SEQUENCES));

    public static boolean isInterestingSequence(IPolypeptide peptide) {
        // debugging code  to see why some sequences are not scored
        if (INTERESTING_SEQUENCES.length > 0) {
            String sequence = peptide.getSequence();
            if (INTERESTING_SEQUENCE_SET.contains(sequence)) {
                double matchingMass = peptide.getMatchingMass();
                XTandemUtilities.breakHere();
                return true;
            }
        }
        return false;
    }


    public static boolean containsInterestingSequence(String sequence) {
        // debugging code  to see why some sequences are not scored
        if (INTERESTING_SEQUENCES.length > 0) {
            for (String test : INTERESTING_SEQUENCE_SET) {
                if (sequence.contains(test)) {
                    return true;
                }
            }
        }
        return false;
    }


    private final IPeptideDigester m_Digester;
    private final XTandemMain m_Application;
    private int m_ProteinIndex;
    private long m_FragmentIndex;
    private PeptideModification[] m_Modifications = PeptideModification.EMPTY_ARRAY;
    private Text m_OnlyKey = new Text();
    private Text m_OnlyValue = new Text();
    private boolean m_GenerateDecoysForModifiedPeptides;


    public FastaHadoopLoader(XTandemMain app) {
        m_Application = app;
        m_Digester = m_Application.getDigester();


        ScoringModifications scoringMods = m_Application.getScoringMods();

        PeptideModification[] modifications = scoringMods.getModifications();
        m_Modifications = modifications;

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
        boolean hasDecoys = m_Application.getBooleanParameter(XTandemUtilities.CREATE_DECOY_PEPTIDES_PROPERTY, Boolean.FALSE);
        ret.setHasDecoys(hasDecoys);

        return ret;
    }


    public String asXMLString() {
        return getDescription().asXMLString();
    }

    public PeptideModification[] getModifications() {
        return m_Modifications;
    }

    public void setModifications(final PeptideModification[] pModifications) {
        m_Modifications = pModifications;
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
        return m_Digester;
    }

    public long getFragmentIndex() {
        return m_FragmentIndex;
    }

    public long getAndIncrementFragmentIndex() {
        return m_FragmentIndex++;
    }

    public void handleProtein(String annotation, final String sequence )
            {

        // no duplicates


        IPeptideDigester digester = getDigester();
        //      digester.setNumberMissedCleavages(NUMBER_MISSED_CLEAVAGES);
        // debugging - why aren't we catagorizing some sequences
        if (containsInterestingSequence(sequence))
            XTandemUtilities.breakHere();

        IProtein prot = Protein.getProtein(annotation, annotation, sequence, null);

        // do a boolean for a peptide belonging to a decoy protein, but use the public isDecoy boolean/method in Protein class

        boolean isDecoy = prot.isDecoy();

        IPolypeptide[] pps = digester.digest(prot);
        PeptideModification[] modifications1 = getModifications();
        for (int i = 0; i < pps.length; i++) {
            IPolypeptide pp = pps[i];

            if (isInterestingSequence(pp))
                XTandemUtilities.breakHere();

            if (!pp.isValid())
                continue;

            // hadoop write intermediate seq finder
            writePeptide(pp);

            //   if(isDecoy)
            //       continue; // skip the rest of the loop

            // if it is decoy, don't add modifications to it
            if (!isDecoy || isGenerateDecoysForModifiedPeptides()) {
                //  generate modified peptides and add to the output
                IModifiedPeptide[] modifications = ModifiedPolypeptide.buildModifications(pp, modifications1);
                for (int m = 0; m < modifications.length; m++) {
                    IModifiedPeptide modification = modifications[m];
                    writePeptide(modification );

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
                writePeptide(semipp );
                IModifiedPeptide[] modifications = ModifiedPolypeptide.buildModifications(semipp, modifications1);
                for (int k = 0; k < modifications.length; k++) {
                    IModifiedPeptide modification = modifications[k];
                    writePeptide(modification );

                }
            }
        }


    }


    protected void writePeptide(final IPolypeptide pp )

    {
        long numberFragments = getAndIncrementFragmentIndex();
        // see if the writes are killing us
        //  if( numberFragments % 20 != 0)
        //     return; // todo take out this is only for testing

        String str = pp.toString();
        if (str.startsWith("AVLEFTPETPSPLIGILENK"))
            XTandemUtilities.breakHere();
        m_OnlyKey.set(str);
        IProteinPosition[] proteinPositions = pp.getProteinPositions();
        if (proteinPositions == null)
            throw new IllegalStateException("proteinPositions undefined");
        if (proteinPositions.length != 1)
            throw new IllegalStateException("should be only one protein here");


        m_OnlyValue.set(proteinPositions[0].asPeptidePosition()); // I guess we can pass in the protein
        throw new UnsupportedOperationException("Fix This"); // ToDo
      //       context.write(m_OnlyKey, m_OnlyValue);
    }


//
//    public static void main(String[] args) {
//        String pFileName = args[0];
//        String host = args[1];
//        String database = args[2];
//        boolean isCompleteLoad = args.length > 3 && "clean".equals(args[3]);
//        /**
//         * specify clean to reload the database
//         */
//        if (isCompleteLoad)
//            SpringJDBCUtilities.dropDatabase(host, database);
//
//        SpringJDBCUtilities.guaranteeDatabase(host, database);
//        IPeptideDigester digester = PeptideBondDigester.getDigester("trypsin");
//        digester.setNumberMissedCleavages(2);
//
//        digester.setSemiTryptic(true); // surely needed to test semitryptic
//
//        IMainData main = buildMain(host, database);
//        JDBCTaxonomy tax = new JDBCTaxonomy(main);
//        File proteins = new File(pFileName.replace(".fasta", "proteins.sql"));
//        File fragments = new File(pFileName.replace(".fasta", "fragments.sql"));
//        File semifragments = new File(pFileName.replace(".fasta", "semifragments.sql"));
//        String path = semifragments.getAbsolutePath();
//
//        if (isCompleteLoad) {
//            proteins.delete();
//            fragments.delete();
//            semifragments.delete();
//            if (proteins.exists() || fragments.exists() || semifragments.exists())
//                throw new IllegalStateException("cannot clean files ");
//        }
//
//        FastaHadoopLoader fs = new FastaHadoopLoader(digester, proteins, fragments, semifragments);
//
//        fs.setModifications("15.994915@M");
//
//        SimpleJdbcTemplate template = tax.getTemplate();
//        TaxonomyDatabase tdb = new TaxonomyDatabase(template);
//        tdb.createDatabase();
//        ElapsedTimer et = new ElapsedTimer();
//        File theFile = new File(pFileName);
//        if (!theFile.exists())
//            throw new IllegalArgumentException("file " + theFile + " does not exist");
//
//        if (!fragments.exists()) {
//            fs.parse(theFile);
//            et.showElapsed(System.out);
//            et.reset();
//        }
//        fs.loadDatabase(template);
//        et.showElapsed(System.out);
//
//
//    }


}
