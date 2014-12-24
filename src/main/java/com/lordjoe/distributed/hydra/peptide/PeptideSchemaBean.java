package com.lordjoe.distributed.hydra.peptide;


import com.lordjoe.distributed.database.*;
import com.lordjoe.distributed.hydra.fragment.*;
import org.apache.spark.api.java.function.*;
import org.systemsbiology.xtandem.peptide.*;
import org.apache.spark.sql.api.java.Row;
/**
 * com.lordjoe.distributed.hydra.peptide.PeptideSchemaBean
 * A simple bean representing a polypeptide
 * User: Steve
 * Date: 10/15/2014
 */
public class PeptideSchemaBean implements IDatabaseBean {

    public static IProteinPosition[] getProteinPositions(PeptideSchemaBean bean) {
        String[] split = bean.getProteinsString().split(":");
        IProteinPosition[] ret = new IProteinPosition[split.length];
        for (int i = 0; i < split.length; i++) {
            throw new UnsupportedOperationException("Fix This"); // ToDo Build a Protein position from ID
            //      ret[i] = new ProteinPosition();

        }
        return ret;
    }

    /**
     * function to convert IPolypeptide to PeptideSchemaBeans
     */
    public static final Function<IPolypeptide, PeptideSchemaBean> TO_BEAN = new Function<IPolypeptide, PeptideSchemaBean>() {
        @Override
        public PeptideSchemaBean call(final IPolypeptide pp) throws Exception {
            return new PeptideSchemaBean(pp);
        }
    };

    /**
     * function to convert PeptideSchemaBeans to IPolypeptide
     */
    public static final Function<Row, PeptideSchemaBean> FROM_ROW = new Function<Row, PeptideSchemaBean>() {
        @Override
        public PeptideSchemaBean call(final Row row) throws Exception {
            PeptideSchemaBean ret = new PeptideSchemaBean();
            double mass1 = row.getDouble(0);
            ret.setMass(mass1);
            String sequence1 = row.getString(3);
            ret.setSequence(sequence1);
            int scanMass = row.getInt(1);
            ret.setMassBin(scanMass);
            String protein = row.getString(2);
            ret.setProteinsString(protein);
            return ret;
        }
    };

    /**
     * function to convert PeptideSchemaBeans to IPolypeptide
     */
    public static final Function<PeptideSchemaBean, IPolypeptide> FROM_BEAN = new Function<PeptideSchemaBean, IPolypeptide>() {
        @Override
        public IPolypeptide call(final PeptideSchemaBean bean) throws Exception {
            Polypeptide polypeptide = Polypeptide.fromString(bean.getSequence());
          //  IProteinPosition[] pps = getProteinPositions(bean);
         //   polypeptide.setContainedInProteins(pps);
            return polypeptide;
        }
    };



    private String sequence;
    private double mass;
    private int massBin;
    private String proteinsString;


    public PeptideSchemaBean() {
    }

    public PeptideSchemaBean(String line) {
        String[] parts = line.split(",");
        int index = 0;
        setSequence(parts[index++]);
        setMass(Double.parseDouble(parts[index++].trim()));
        setMassBin(Integer.parseInt(parts[index++].trim()));
        setProteinsString(parts[index++]);
    }

    public PeptideSchemaBean(IPolypeptide pp) {
        int index = 0;
        setSequence(pp.getSequence());
        setMass(pp.getMass());
        setMassBin((BinChargeKey.mzAsInt(pp.getMatchingMass())));
        IProteinPosition[] proteinPositions = pp.getProteinPositions();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < proteinPositions.length; i++) {
            IProteinPosition ppx = proteinPositions[i];
            if (i > 0) sb.append(",");
            sb.append(ppx.getProteinId());
        }
        setProteinsString(sb.toString());
    }

    public IPolypeptide asPeptide() {
        Polypeptide ret = new Polypeptide(getSequence());
        ret.setMass(getMass());

        //      ret.setContainedInProteins(getProteinPositions());
        return ret;
    }


    public String getSequence() {
        return sequence;
    }

    public void setSequence(final String pSequence) {
        sequence = pSequence;
    }

    public double getMass() {
        return mass;
    }

    public void setMass(final double pMass) {
        mass = pMass;
    }

    public int getMassBin() {
        return massBin;
    }

    public void setMassBin(final int pMassBin) {
        massBin = pMassBin;
    }

    public String getProteinsString() {
        if (proteinsString == null)
            return "";
        return proteinsString;
    }

    public void setProteinsString(final String pProteinsString) {
        proteinsString = pProteinsString;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getSequence());
        sb.append(",");
        sb.append(String.format("%10.4f", getMass()));
        sb.append(",");
        sb.append(Integer.toString(getMassBin()));
        sb.append(",");
        sb.append(getProteinsString());

        return sb.toString();
    }
}
