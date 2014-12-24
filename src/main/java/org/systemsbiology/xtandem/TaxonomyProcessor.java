package org.systemsbiology.xtandem;

import org.systemsbiology.hadoop.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.TaxonomyProcessor
 *   hold any modifications on the mass of an amino acid
 * @author Steve Lewis
  * class for processing taxonomies
 */
public class TaxonomyProcessor  implements Serializable
{
    public static TaxonomyProcessor[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = TaxonomyProcessor.class;

    private final Map<FastaAminoAcid, MassModification> m_Modifications = new HashMap<FastaAminoAcid, MassModification>();

    public static enum MassModificationType
    {
         isValue,addValue,subtractValue
    }

    /**
     * modification of a specific residue
     */
    public static class MassModification
    {
        private final double m_Value;
        private final MassModificationType m_ModificationType;
        private final FastaAminoAcid m_Residue;

        public MassModification(double pValue, MassModificationType pType,
                                FastaAminoAcid pResidue)
        {
            m_Value = pValue;
            m_ModificationType = pType;
            m_Residue = pResidue;
        }

        public MassModification(double pValue, MassModificationType pType,
                                String pResidue)
        {
            this(pValue, pType, FastaAminoAcid.valueOf(pResidue));
        }

        public double getValue()
        {
            return m_Value;
        }

        public MassModificationType getModificationType()
        {
            return m_ModificationType;
        }

        public FastaAminoAcid getResidue()
        {
            return m_Residue;
        }
    }

    public MassModification getModification(FastaAminoAcid residue)  {
        return  m_Modifications.get(residue);
    }

    public void addModification(MassModification mod)  {
           m_Modifications.put(mod.getResidue(),mod);
    }


    public void configure(IParameterHolder params)
    {
  //        The format of this parameter is m@X, where m is the modfication
//		mass in Daltons and X is the appropriate residue to modify. Lists of
//		modifications are separated by commas. For example, to modify M and C
//		with the addition of 16.0 Daltons, the parameter line would be
//		+16.0@M,+16.0@C
        String mods = params.getParameter("residue, modification mass");
        if(mods != null) {
             String[] values = mods.split(",");
            for (int i = 0; i < values.length; i++) {
                String value = values[i];

             }
        }
    }

    /**
     * take a string line 1234@Q or +16@G and make a modification
     * @param s
     */
    protected void buildMassModification(String s) {
          MassModificationType type = MassModificationType.isValue;
        if(s.startsWith("+")) {
            s = s.substring(1);
            type = MassModificationType.addValue;
        }
        if(s.startsWith("-")) {
            s = s.substring(1);
            type = MassModificationType.subtractValue;
        }
        String[] items = s.split("@");
        if(items.length != 2)
            throw new IllegalArgumentException("must be of the form 1234@A");
        FastaAminoAcid residue = FastaAminoAcid.valueOf(items[1].toUpperCase());
        double mass = Double.parseDouble(items[0]);

        addModification(new MassModification(mass,type,residue));
    }
    

}
