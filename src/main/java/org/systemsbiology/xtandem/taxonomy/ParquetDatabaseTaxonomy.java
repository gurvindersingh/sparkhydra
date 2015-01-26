package org.systemsbiology.xtandem.taxonomy;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.hydra.peptide.*;
import org.apache.hadoop.fs.*;
import org.apache.spark.api.java.*;
import org.apache.spark.sql.api.java.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
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
public class ParquetDatabaseTaxonomy implements ITaxonomy {
    private final String m_Organism;
    private IPeptideDigester m_Digester;
    private Map<String, IProtein> m_IdToProtein = new HashMap<String, IProtein>();

    private final IMainData m_Tandem;

    public ParquetDatabaseTaxonomy(IMainData tandem, String pOrganism) {
        m_Tandem = tandem;
        m_Organism = pOrganism;
        TaxonHandler taxonHandler = new TaxonHandler(null, "peptide", pOrganism);
    }


    @Override
    public void setDigester(final IPeptideDigester digester) {
        m_Digester = digester;
    }

    @Override
    public IPeptideDigester getDigester() {
        if (m_Digester != null)
            return m_Digester;
        return PeptideBondDigester.getDefaultDigester();
    }


    public IMainData getTandem() {
        return m_Tandem;
    }


    @Override
    public IPolypeptide[] getPeptidesOfMass(final double scanmass) {
        return getPeptidesOfMass(scanmass, false);
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
        IPolypeptide[] pps = getPeptidesOfMass(scanmass, isSemi);
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (int i = 0; i < pps.length; i++) {
            IPolypeptide pp = pps[i];
            if ((int) pp.getMatchingMass() == scanmass)
                holder.add(pp);
            // todo handle other charge states
        }
        IPolypeptide[] ret = new IPolypeptide[holder.size()];
        holder.toArray(ret);
        return ret;
    }

    /**
     * given an id get the corresponding protein
     *
     * @param key
     * @return
     */
    @Override
    public IProtein getProteinById(final String key) {
        if (true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        return null;
    }

    /**
     * find the first protein with this sequence and return the corresponding id
     *
     * @param sequence
     * @return
     */
    @Override
    public String sequenceToID(final String sequence) {
        if (true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        return null;
    }

    /**
     * retrieve all peptides matching a specific mass
     *
     * @param scanmass
     * @return
     */
    @Override
    public IPolypeptide[] getPeptidesOfMass(final double scanmass, boolean isSemi) {
         throw new UnsupportedOperationException("Fix This"); // ToDo
//        //here for backward compatability
//        if (getTandem() instanceof XTandemMain) {
//            Scorer scorer = ((XTandemMain) getTandem()).getScoreRunner();
//            return scorer.getPeptidesOfMass(scanmass);
//        }
//        throw new UnsupportedOperationException("Bad State");
    }


    protected String buildDatabaseName() {
        String fasta = getTandem().getDatabaseName();
        Path defaultPath = XTandemHadoopUtilities.getDefaultPath();
        return defaultPath.toString() + "/" + fasta + ".parquet";
    }

    /**
     * retrieve all peptides matching a specific mass
     *
     * @param scanmass
     * @param isSemi   if true get semitryptic masses
     * @return
     */
    @Override
    public synchronized IPolypeptide[] getPeptidesOfExactMass(final int scanmass, final boolean isSemi) {
        try {
            JavaSparkContext sc = SparkUtilities.getCurrentContext();
            // Read in the Parquet file created above.  Parquet files are self-describing so the schema is preserved.
            // The result of loading a parquet file is also a JavaSchemaRDD.
            String dbName = buildDatabaseName();

            JavaSQLContext sqlContext = SparkUtilities.getCurrentSQLContext();
            JavaSchemaRDD parquetFile = sqlContext.parquetFile(dbName);
            //Parquet files can also be registered as tables and then used in SQL statements.
            parquetFile.registerAsTable("peptides");
     //       JavaSchemaRDD binCounts = sqlContext.sql("SELECT * FROM " + "peptides" );
            JavaSchemaRDD binCounts = sqlContext.sql("SELECT * FROM " + "peptides" + " Where  massBin =" + scanmass);

            JavaRDD<PeptideSchemaBean> beancounts = binCounts.map(PeptideSchemaBean.FROM_ROW);
            JavaRDD<IPolypeptide> counts = beancounts.map(PeptideSchemaBean.FROM_BEAN);

            List<IPolypeptide> collect = counts.collect();
            return collect.toArray(new IPolypeptide[collect.size()]);
        }
        catch (Exception e) {
            return null; // not found
        }

    }

    /**
     * retrieve all peptides matching a specific mass
     *
     * @param scanmass
     * @param isSemi   if true get semitryptic masses
     * @return
     */
    public IPolypeptide[] getPeptidesOfLimitedMass(final int scanmass, final boolean isSemi, MassPeptideInterval interval) {
        if (true)
            throw new UnsupportedOperationException("Fix This"); // ToDo
        try {
            JavaSparkContext sc = SparkUtilities.getCurrentContext();
            JavaSQLContext sqlContext = SparkUtilities.getCurrentSQLContext();
            // Read in the Parquet file created above.  Parquet files are self-describing so the schema is preserved.
            // The result of loading a parquet file is also a JavaSchemaRDD.
            String dbName = buildDatabaseName();

            JavaSchemaRDD parquetFile = sqlContext.parquetFile(dbName);
            //Parquet files can also be registered as tables and then used in SQL statements.
            parquetFile.registerAsTable("peptides");
            JavaSchemaRDD binCounts = sqlContext.sql("SELECT * FROM " + "peptides" + " Where  massBin =" + scanmass);

            JavaRDD<PeptideSchemaBean> beancounts = binCounts.map(PeptideSchemaBean.FROM_ROW);
            JavaRDD<IPolypeptide> counts = beancounts.map(PeptideSchemaBean.FROM_BEAN);

            List<IPolypeptide> collect = counts.collect();
            return collect.toArray(new IPolypeptide[collect.size()]);
        }
        catch (Exception e) {
            return null; // not found
        }
    }

    public IPolypeptide buildPeptideFromDatabaseRow(Row row) {
        String sequence = row.getString(0);
        if (sequence.contains("-"))
            XTandemUtilities.breakHere();

        Polypeptide pp = (Polypeptide) Polypeptide.fromString(sequence);
        int missedCleavages = getDigester().probableNumberMissedCleavages(pp);
        pp.setMissedCleavages(missedCleavages);
        double mass = row.getDouble(1);
        pp.setMatchingMass(mass);
        String proteinStr = row.getString(3);
        String[] proteinIds = proteinStr.split(";");
        IProteinPosition[] positions = new IProteinPosition[proteinIds.length];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = new ProteinPosition(pp, proteinIds[i]);

        }
        pp.setContainedInProteins(positions);
        return pp;
    }


    /**
     * retrieve all peptides matching a specific mass
     *
     * @param scanmass
     * @return
     */
    @Override
    public IPolypeptide[] getPeptidesOfExactMass(MassPeptideInterval interval, boolean isSemi) {
        int scanmass = interval.getMass();
        if (interval.isUnlimited())
            return getPeptidesOfExactMass(scanmass, isSemi);
        else
            return getPeptidesOfLimitedMass(scanmass, isSemi, interval);

    }


    /**
     * given an id get the corresponding IPolypeptide
     *
     * @param key
     * @return
     */
    @Override
    public IPolypeptide getPeptideById(String key) {
        return Polypeptide.fromString(key);
    }


    @Override
    public IProtein[] getProteins() {
//        if (true)
        throw new UnsupportedOperationException("Fix This"); // ToDo
//        if(m_Proteins.isEmpty())
//             loadTaxonomyFiles();
//
//        return m_Proteins.toArray(IProtein.EMPTY_ARRAY);
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
//
//    public void loadTaxonomyFiles() {
//        String[] taxomonyFiles = getTaxomonyFiles();
//        if(taxomonyFiles == null)
//            return; // nothing to do
//        for (String f : taxomonyFiles) {
//            readFasta(f);
//        }
//    }
//
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
//        try {
//            String line = inp.readLine();
//            if (line.startsWith("xbang-pro-fasta-format")) {
//                readFastaPro(inp);
//            }
//            while (line != null) {
//                if (line.startsWith(">")) {
//                    annotation = line.substring(1); // annotation is the rest o fthe line
//                    buildAndAddProtein(annotation, sb, url);
//                }
//                else {
//                    sb.append(line.trim()); // add to sequence
//                }
//                line = inp.readLine();
//            }
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


//    protected void buildAndAddProtein(String annotation, StringBuilder sb, String url) {
//        if (sb.length() == 0)
//            return;
//        addProtein(Protein.getProtein(annotation, sb.toString(), url));
//        sb.setLength(0);  // clear sequence
//    }

}
