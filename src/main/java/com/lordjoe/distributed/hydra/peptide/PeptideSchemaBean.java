package com.lordjoe.distributed.hydra.peptide;


import com.lordjoe.distributed.database.*;
import com.lordjoe.distributed.hydra.fragment.*;
import org.apache.spark.api.java.function.*;
import org.systemsbiology.xtandem.peptide.*;
import org.apache.spark.sql.Row;
/**
 * com.lordjoe.distributed.hydra.peptide.PeptideSchemaBean
 * A simple bean representing a polypeptide
 * User: Steve
 * Date: 10/15/2014
 */
public class PeptideSchemaBean implements IDatabaseBean {

    public static final String PROTEIN_POSITION_SEPARATOR = ":";
    public static IProteinPosition[] getProteinPositions(PeptideSchemaBean bean, IPolypeptide pp) {
        String[] split = bean.getProteinsString().split(PROTEIN_POSITION_SEPARATOR);
        IProteinPosition[] ret = new IProteinPosition[split.length];
        for (int i = 0; i < split.length; i++) {

                 ret[i] = new ProteinPosition(pp,split[i]);

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
            ret.setSequenceString(sequence1);
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
            return bean.asPeptide();
        }
    };



    private String sequenceString;
    private double mass;
    private int massBin;
    private String proteinsString;


    public PeptideSchemaBean() {
    }

    public PeptideSchemaBean(String line) {
        String[] parts = line.split(",");
        int index = 0;
        setSequenceString(parts[index++]);
        setMass(Double.parseDouble(parts[index++].trim()));
        setMassBin(Integer.parseInt(parts[index++].trim()));
        setProteinsString(parts[index++]);
    }

    public PeptideSchemaBean(IPolypeptide pp) {
        int index = 0;
        setSequenceString(pp.toString());
        setMass(pp.getMass());
        setMassBin((BinChargeKey.mzAsInt(pp.getMatchingMass())));
        IProteinPosition[] proteinPositions = pp.getProteinPositions();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < proteinPositions.length; i++) {
            IProteinPosition ppx = proteinPositions[i];
            if (i > 0) sb.append(PROTEIN_POSITION_SEPARATOR);
            sb.append(ppx.toString());
        }
        setProteinsString(sb.toString());
    }

    public IPolypeptide asPeptide() {
        Polypeptide ret =  Polypeptide.fromString(getSequenceString());
        ret.setMass(getMass());
         ret.setContainedInProteins(getProteinPositions(this,ret));
        return ret;
    }


    public String getSequenceString() {
        return sequenceString;
    }

    public void setSequenceString(final String pSequence) {
        sequenceString = pSequence;
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
        sb.append(getSequenceString());
        sb.append(",");
        sb.append(String.format("%10.4f", getMass()));
        sb.append(",");
        sb.append(Integer.toString(getMassBin()));
        sb.append(",");
        sb.append(getProteinsString());

        return sb.toString();
    }
}
