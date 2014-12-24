package org.systemsbiology.xtandem.taxonomy;

import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.sax.*;
import org.systemsbiology.xtandem.scoring.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.taxonomy.Taxonomy
 *
 * @author Steve Lewis
 * @date Jan 7, 2011
 */
public class Taxonomy implements ITaxonomy {
    public static ITaxonomy[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = Taxonomy.class;

    private final String m_Organism;
    private final String m_DescriptiveFile;
    private IPeptideDigester m_Digester;
    private final List<IProtein> m_Proteins = new ArrayList<IProtein>();
    private Map<String, IProtein> m_IdToProtein = new HashMap<String, IProtein>();
  //  private TaxonomyTranch m_Tranch = TaxonomyTranch.NULL_TRANCH; // accept all

    private final IParameterHolder m_Tandem;
    private String[] m_TaxomonyFiles;

    public Taxonomy(IParameterHolder tandem, String pOrganism, String pDescriptiveFile) {
        m_Tandem = tandem;
        m_Organism = pOrganism;
        m_DescriptiveFile = pDescriptiveFile;
        TaxonHandler taxonHandler = new TaxonHandler(null, "peptide", pOrganism);
        // might be null in test code
        if (m_DescriptiveFile != null) {

            InputStream is = tandem.open(m_DescriptiveFile);
            if(is == null)
                throw new IllegalStateException("cannot parse taxonomy file " + m_DescriptiveFile);
            String[] files = XTandemUtilities.parseFile(is, taxonHandler, m_DescriptiveFile);
            setTaxomonyFiles(files);

        }
        else {
            String[] files = { pOrganism };
            setTaxomonyFiles(files);
        }
    }

    @Override
    public void setDigester(final IPeptideDigester digester) {
         m_Digester = digester;
    }

    @Override
    public IPeptideDigester getDigester() {
        return m_Digester;
    }

//    public TaxonomyTranch getTranch() {
//        return m_Tranch;
//    }
//
//    public void setTranch(final TaxonomyTranch pTranch) {
//        m_Tranch = pTranch;
//    }

    public IParameterHolder getTandem() {
        return m_Tandem;
    }


    @Override
    public IPolypeptide[] getPeptidesOfMass(final double scanmass) {
        return  getPeptidesOfMass(scanmass,false);
    }

    /**
     * retrieve all peptides matching a specific mass
     *
     * @param scanmass
     * @param isSemi   if true get semitryptic masses
     * @return
     */
    @Override
    public IPolypeptide[] getPeptidesIntegerOfMZ(final int scanmass, final boolean isSemi) {
        if(isSemi)
              throw new UnsupportedOperationException("Currently we only support tyrptic in the database");
       IPolypeptide[] pps = getPeptidesOfMass(  scanmass,  isSemi);
       List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (int i = 0; i < pps.length; i++) {
            IPolypeptide pp = pps[i];
            if((int)pp.getMatchingMass() == scanmass)
                 holder.add(pp);
            // todo handle other charge states
       }
       IPolypeptide[] ret = new IPolypeptide[holder.size()];
       holder.toArray(ret);
       return ret;
    }

    /**
     * retrieve all peptides matching a specific mass
     *
     * @param scanmass
     * @return
     */
    @Override
    public IPolypeptide[] getPeptidesOfMass(final double scanmass,boolean isSemi) {
        if(isSemi)
            throw new UnsupportedOperationException("Currently we only support tyrptic in the database");
        //here for backward compatability
        if(getTandem() instanceof XTandemMain ) {
            Scorer scorer = ((XTandemMain) getTandem()).getScoreRunner();
            return scorer.getPeptidesOfMass( scanmass );
        }
        throw new UnsupportedOperationException("Bad State");  
    }


    /**
     * retrieve all peptides matching a specific mass
     *
     * @param scanmass
     * @param isSemi   if true get semitryptic masses
     * @return
     */
    @Override
    public IPolypeptide[] getPeptidesOfExactMass(final int scanmass, final boolean isSemi) {
        if(isSemi)
              throw new UnsupportedOperationException("Currently we only support tyrptic in the database");
          //here for backward compatability
          if(getTandem() instanceof XTandemMain ) {
              Scorer scorer = ((XTandemMain) getTandem()).getScoreRunner();
              return scorer.getPeptidesOfMass( scanmass );
          }
          throw new UnsupportedOperationException("Bad State");
     }


    /**
     * retrieve all peptides matching a specific mass
     *
     * @param scanmass
     * @param isSemi   if true get semitryptic masses
     * @return
     */
    @Override
    public IPolypeptide[] getPeptidesOfExactMass(final MassPeptideInterval interval, final boolean isSemi) {
        if(isSemi)
              throw new UnsupportedOperationException("Currently we only support tyrptic in the database");
          //here for backward compatability
          if(getTandem() instanceof XTandemMain ) {
              Scorer scorer = ((XTandemMain) getTandem()).getScoreRunner();
              IPolypeptide[] peptidesOfMass = scorer.getPeptidesOfMass(interval.getMass() );
              if(interval.isUnlimited())
                  return peptidesOfMass;
              List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
              for (int i = interval.getStart(); i < Math.min(peptidesOfMass.length,interval.getEnd()); i++) {
                  IPolypeptide peptide = peptidesOfMass[i];
                   holder.add(peptide);
              }
              IPolypeptide[] ret = new IPolypeptide[holder.size()];
              holder.toArray(ret);
              return ret;
             }
          throw new UnsupportedOperationException("Bad State");
     }

    /**
     * find the first protien with this sequence and return the correcponding id
     *
     * @param sequence
     * @return
     */
    public String sequenceToID(String sequence) {
        final String[] strings = seqenceToIDs(sequence);
        if (strings.length > 0)
            return strings[0];
        return null;
    }

    /**
     * find the first protien with this sequwence and return the correcponding id
     *
     * @param sequence
     * @return
     */
    public String[] seqenceToIDs(String sequence) {
        final IProtein[] proteins = getProteins();
        List<String> holder = new ArrayList<String>();
        for (int i = 0; i < proteins.length; i++) {
            IProtein protein = proteins[i];
            String s = proteinToId(protein, sequence);
            if (s != null)
                holder.add(s);
        }
        String[] ret = new String[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    protected String proteinToId(IProtein pProtein, String pSequence) {
        final String proteinSequence = pProtein.getSequence();
        final int index = proteinSequence.indexOf(pSequence);
        if (index == -1)
            return null;
        return pProtein.getSequenceId(index, pSequence.length());
    }


    public void addProtein(IProtein p) {
        m_Proteins.add(p);
        String id = p.getId();
        // do we want this
        m_IdToProtein.put(id, p);
    }

    /**
     * given an id get the corresponding protein
     *
     * @param key
     * @return
     */
    @Override
    public IProtein getProteinById(String key) {
        return m_IdToProtein.get(key);
    }

    /**
     * given an id get the corresponding IPolypeptide
     *
     * @param key
     * @return
     */
    @Override
    public IPolypeptide getPeptideById(String key) {
        if (!key.contains(IPolypeptide.KEY_SEPARATOR))
            return getProteinById(key);
        String[] split = key.split(IPolypeptide.KEY_SEPARATOR);
        IProtein prot = getProteinById(split[0]);

        if (prot == null)
            return null; // maybe we do not care
        if (!(prot instanceof IProtein))
            return null; // maybe we do not care

        String[] split2 = split[split.length - 1].split("\\(");
        int start = Integer.parseInt(split2[0]);
        int length = Integer.parseInt(split2[1].replace(")", ""));
        IPolypeptide fragment = ((Protein)prot).fragment(start, length);
        return fragment;
    }


    public String[] getTaxomonyFiles() {
   //     if(m_TaxomonyFiles == null)

         return m_TaxomonyFiles;
    }

    public void setTaxomonyFiles(String[] pTaxomonyFiles) {
         m_TaxomonyFiles = pTaxomonyFiles;
    }


    @Override
    public IProtein[] getProteins() {
        if(m_Proteins.isEmpty()) {
            throw new UnsupportedOperationException("Fix This"); // ToDo
         //   loadTaxonomyFiles();
        }

        return m_Proteins.toArray(IProtein.EMPTY_ARRAY);
    }

    @Override
    public IProtein[] getValidProteins() {
        IProtein[] prots = getProteins();
        List<IProtein> holder = new ArrayList<IProtein>();
        for (int i = 0; i < prots.length; i++) {
            IProtein prot = prots[i];
            if (!prot.isValid())
                continue;
            holder.add(prot);
        }
        IProtein[] ret = new IProtein[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    @Override
    public String getOrganism() {
        return m_Organism;
    }

    public String getDescriptiveFile() {
        return m_DescriptiveFile;
    }

//    public void loadTaxonomyFiles() {
//        String[] taxomonyFiles = getTaxomonyFiles();
//        if(taxomonyFiles == null)
//            return; // nothing to do
//        for (String f : taxomonyFiles) {
//            readFasta(f);
//        }
//    }

//    public void readFasta(String is) {
//        IParameterHolder tandem = getTandem();
//        InputStream describedStream = tandem.open(is);
//        // might list lots of data we do not have
//        if (describedStream != null) {
//            readFasta(describedStream, is);
//        }
//        else {
//            throw new IllegalArgumentException("bad Fasta file " + is);
//        }
//    }
//
//    public void readFasta(InputStream is, String url) {
//        LineNumberReader inp = new LineNumberReader(new InputStreamReader(is));
//        StringBuilder sb = new StringBuilder();
//        String annotation = null;
//        int proteinIndex = 0;
//        TaxonomyTranch taxonomyTranch = getTranch();
//            try {
//            String line = inp.readLine();
//            if (line.startsWith("xbang-pro-fasta-format")) {
//                readFastaPro(inp);
//            }
//            while (line != null) {
//                if (line.startsWith(">")) {
//                     if (taxonomyTranch.isAccepted(proteinIndex++)) {
//                        annotation = line.substring(1); // annotation is the rest o fthe line
//                        buildAndAddProtein(annotation, sb, url);
//                    }
//                    else {    // protein is not in this tranch
//                          sb.setLength(0);  // clear sequence
//                     }
//
//                }
//                else {
//                    sb.append(line.trim()); // add to sequence
//                }
//                line = inp.readLine();
//            }
//            if (taxonomyTranch.isAccepted(proteinIndex++))
//                buildAndAddProtein(annotation, sb, url);
//        }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//        finally {
//            try {
//                is.close();
//            }
//            catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//    }

    public void readFastaPro(LineNumberReader inp) {
        try {
            String line = inp.readLine();
            while (line != null) {
                buildAndAddFastaProProtein(line);
                line = inp.readLine();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            try {
                inp.close();
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

    protected void buildAndAddFastaProProtein(String line) {
        if (line.length() == 0)
            return;
        String[] items = line.split("\t");
        for (int i = 0; i < items.length; i++) {
            String item = items[i];
            item = null;
        }
        //  addProtein(new Protein(this, annotation, sb.toString()));
    }


    protected void buildAndAddProtein(String annotation, StringBuilder sb, String url) {
        if (sb.length() == 0)
            return;
        addProtein(Protein.getProtein(annotation,  annotation, sb.toString(), url));
        sb.setLength(0);  // clear sequence
    }

}
