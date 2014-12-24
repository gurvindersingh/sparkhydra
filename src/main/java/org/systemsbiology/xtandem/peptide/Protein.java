package org.systemsbiology.xtandem.peptide;

import org.systemsbiology.xtandem.hadoop.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.peptide.Protein
 *
 * @author Steve Lewis
 * @date Jan 7, 2011
 */
public class Protein extends Polypeptide implements IProtein {
    public static Protein[] EMPTY_ARRAY = {};

//    private static int gNextId = 1;
//    public static synchronized int getNextId() {
//         return gNextId++;
//    }
//    public static synchronized void resetNextId() {
//          gNextId = 1;
//        synchronized (gKnownProteins)   {
//           gKnownProteins.clear();
//        }
//    }

//    private static Map<Integer,Protein> gKnownProteins = new HashMap<Integer,Protein>();

    /**
     * find an id in an annotation
     * sp|Q6GZW6|009L_FRG3G Putative helicase 009L OS=Frog virus 3 (isolate Goorha) GN=FV3-009L PE=4 SV=1
     *
     * @param pAnnotation
     * @return
     */
    public static String idFromAnnotation( String pAnnotation) {
        if(pAnnotation.startsWith(">"))
            pAnnotation = pAnnotation.substring(1);
        //  sp|Q6GZW6|009L_FRG3G Putative helicase 009L OS=Frog virus 3 (isolate Goorha) GN=FV3-009L PE=4 SV=1
        if (pAnnotation.startsWith("sp|")) {
            String ss = pAnnotation.substring("sp|".length());
            int index = ss.indexOf("|");
            if (index > -1)
                return ss.substring(0, index);
        }
        return annotationToId(pAnnotation);
     }


    public static Protein getProtein(String uuid) {
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        synchronized (gKnownProteins)   {
//            Protein ret = gKnownProteins.get(uuid);
//            if(ret == null)     {
//                ret = new Protein(uuid);
//                gKnownProteins.put(uuid,ret);
//            }
//               return ret;
//        }

    }

    /**
     * turn a list of proteins into a map with id as the key
     *
     * @param prots !null protein list
     * @return !null populated map;
     */
    public static Map<String, IProtein> asIdMap(IProtein[] prots) {
        Map<String, IProtein> ret = new HashMap<String, IProtein>();
        for (int i = 0; i < prots.length; i++) {
            IProtein prot = prots[i];
            ret.put(prot.getId(), prot);
        }
        return ret;
    }

    /**
     * true if there is at least one modification
     *
     * @return
     */
    @Override
    public boolean isModified() {
        return false;
    }

    /**
     * return a list of contained proteins
     * whixch is just this starting at 0
     *
     * @return !null array
     */
    @Override
    public IProteinPosition[] getProteinPositions() {
        IProteinPosition[] ret = {new ProteinPosition(this)};
        return ret;
    }


    /**
     * !null validity may be unknown
     *
     * @return
     */
    public PeptideValidity getValidity() {
        return PeptideValidity.fromString(getId());
    }


    //    /**
//     * tests may need this
//     */
//    public static void clearProteinCache()
//     {
//         synchronized (gKnownProteins)   {
//             gKnownProteins.clear();
//         }
//
//     }
//
//
    public static Protein getProtein(String id, String pAnnotation, String pSequence, String url) {
        return buildProtein(id, pAnnotation, pSequence, url);
//        synchronized (gKnownProteins)   {
//            Protein ret = gKnownProteins.get(uuid);
//            if(ret == null)     {
//                ret = new Protein(uuid);
//                gKnownProteins.put(uuid,ret);
//            }
//            ret.setSequence(pSequence);
//            ret.setAnnotation(pAnnotation);
//            ret.setURL(url);
//               return ret;
//        }


    }

    private static int gLastResortProteinIdx = 1;

    private static synchronized String getNextId() {
        return "Prot" + gLastResortProteinIdx++;
    }

    /**
     * used when we do not want to caahe proteins
     *
     * @param uuid
     * @param pAnnotation
     * @param pSequence
     * @param url
     * @return
     */
    public static Protein buildProtein(String id, String pAnnotation, String pSequence, String url) {
        if (pAnnotation == null)
            pAnnotation = "";
        if (pAnnotation.startsWith(">"))
            pAnnotation = pAnnotation.substring(1); // from a fasta file

        if (id.contains(" ")) {
            id = annotationToId(id);
        }


        if (id == null || id.length() == 0)
            id = getNextId();
        //     synchronized (gKnownProteins)   {
        Protein ret = new Protein(id, pAnnotation);
        ret.setSequence(pSequence);
        //       ret.setAnnotation(pAnnotation);
        ret.setURL(url);
        return ret;
        //    }


    }

    public static String annotationToId(String id) {
        id = id.trim();
        if (id.length() == 0)
            return getNextId();
        if (id.startsWith(">"))
            id = id.substring(1);
        int spIndex = id.indexOf(" ");   // better ne > 0
        if (spIndex > -1) {
            id = id.substring(0, spIndex);
        }

        StringBuilder sb = new StringBuilder();
        char startPunct = 0;
        for (int i = 0; i < id.length(); i++) {
            char c = id.charAt(i);
            if (Character.isLetterOrDigit(c)) {
                sb.append((char) Character.toUpperCase(c));
                continue;
            }
            if (Character.isDigit(c)) {
                sb.append(c);
                continue;
            }
            if (sb.length() == 0) {
                continue;
            }
            sb.append("_");  // anything else becomes _

        }

        if (sb.length() == 0) {
            return getNextId();
        }
        return sb.toString();
    }

    //   private final ITaxonomy m_Taxonomy;
    private final String m_Id;
    private final String m_AnnotationX;
    private String m_URL;
    private double m_ExpectationFactor = 1;

//    public Protein( String pAnnotation, String pSequence,String url)
//    {
//        this( getNextId(),  pAnnotation,   pSequence,  url );
//
//    }

    private Protein(String uuid, String annotation) {
        super();
        m_Id = uuid;
        m_AnnotationX = annotation;
    }

//    private Protein( int uuid,String pAnnotation, String pSequence,String url)
//    {
//        super(pSequence,0,null,0);
//        m_Annotation = pAnnotation;
//  //      m_Taxonomy = tax;
//        m_URL = url;
//        m_UUid = uuid;
//
//    }

    @Override
    public boolean isProtein() {
        return true;
    }

    /**
     * true is the polypaptide is known to be a decoy
     *
     * @return
     */
    @Override
    public boolean isDecoy() {

        if (getId().startsWith(XTandemHadoopUtilities.DEFAULT_DECOY_PREFIX)) {
            return true;
        }

        return false;
    }

    @Override
    public IPolypeptide asDecoy() {
        throw new UnsupportedOperationException("Fix This"); // ToDo
    }


    /**
     * convert position to id
     *
     * @param start
     * @param length
     * @return
     */
    @Override
    public String getSequenceId(int start, int length) {
        return getId() + KEY_SEPARATOR + start + ":" + length;
    }

    @Override
    public String getId() {
        return m_Id;
    }


//    @Override
//    public ITaxonomy getTaxonomy() {
//        return m_Taxonomy;
//    }

    /**
     * source file
     *
     * @return
     */
    @Override
    public String getURL() {
        return m_URL;
    }

    public void setURL(final String pURL) {
        if (m_URL != null) {
            if (m_URL.equals(pURL))
                return;
            throw new IllegalStateException("cannot reset url");
        }
        m_URL = pURL;
    }

//    public void setAnnotation(final String pAnnotation) {
//        if(m_Annotation != null )  {
//            if(m_Annotation.equals(pAnnotation))
//                return;
//            throw new IllegalStateException("cannot reset annotation");
//        }
//        m_Annotation = pAnnotation;
//    }

//    /**
//     * return the protein we are part of
//     *
//     * @return
//     */
//    @Override
//    public IProtein getParentProtein() {
//        return this;
//    }

    @Override
    public String getAnnotation() {
        return m_AnnotationX;
    }

    @Override
    public double getExpectationFactor() {
        return m_ExpectationFactor;
    }

    public void setExpectationFactor(final double pExpectationFactor) {
        m_ExpectationFactor = pExpectationFactor;
    }

}
