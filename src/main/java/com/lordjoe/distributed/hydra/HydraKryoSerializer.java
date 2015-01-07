package com.lordjoe.distributed.hydra;

import com.esotericsoftware.kryo.*;
import org.apache.spark.serializer.*;

import javax.annotation.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.HydraKryoSerializer
 * User: Steve
 * Date: 10/28/2014
 */
public class HydraKryoSerializer implements KryoRegistrator, Serializable {

    /**
     * register a class indicated by name but only if not already registered
     *
     * @param kryo
     * @param s       name of a class - might not exist
     * @param handled Set of classes already handles
     */
    protected void doRegistration(@Nonnull Kryo kryo, @Nonnull String s, @Nonnull Set<Class> handled) {
        Class c;
        try {
            c = Class.forName(s);
        }
        catch (ClassNotFoundException e) {
            return;
        }

        // do not register interfaces
        if (c.isInterface())
            return;

        // do not register enums
        if (c.isEnum())
            return;

        // do not register abstract classes
        if (Modifier.isAbstract(c.getModifiers()))
            return;

       // validateClass(c);

        if (handled.contains(c))
            return;
        handled.add(c);
        if (kryo != null)
            kryo.register(c);
    }


    public static final Class[] EMPTY_ARGS = {};

    /**
     * make sure class is OK
     *
     * @param pC
     */
    private void validateClass(final Class pC) {
        Constructor declaredConstructor;
        try {
            declaredConstructor = pC.getDeclaredConstructor(EMPTY_ARGS);
            declaredConstructor.setAccessible(true);
        }
        catch (NoSuchMethodException e) {
            Constructor[] declaredConstructors = pC.getDeclaredConstructors();
            System.err.println("No Empty Constructor " + pC);
                return;
           // throw new IllegalArgumentException("No Empty Constructor " + pC);
        }

        if (!Serializable.class.isAssignableFrom(pC))
            throw new IllegalArgumentException("Not Serializable " + pC);

        try {
            ObjectOutputStream os = new ObjectOutputStream(new ByteArrayOutputStream());
            Object o = declaredConstructor.newInstance();
            os.writeObject(o);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Cant Serialize " + pC);
        }
        catch (InstantiationException e) {
            throw new IllegalArgumentException("Cant instantiate " + pC);

        }
        catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Cant access " + pC);
        }
        catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Cant access " + pC);
        }

    }


    @Override
    public void registerClasses(@Nonnull Kryo kryo) {
        Set<Class> handler = new HashSet<Class>();
        doRegistration(kryo,"com.lordjoe.distributed.SparkUtilities",handler);
        doRegistration(kryo,"com.lordjoe.distributed.SparkUtilities$1",handler);
        doRegistration(kryo,"com.lordjoe.distributed.SparkUtilities$IdentityFunction",handler);
        doRegistration(kryo,"com.lordjoe.distributed.SparkUtilities$TupleValues",handler);
        doRegistration(kryo,"com.lordjoe.distributed.hydra.SparkFileOpener",handler);
        doRegistration(kryo,"com.lordjoe.distributed.hydra.SparkXTandemMain",handler);
        doRegistration(kryo,"com.lordjoe.distributed.hydra.protein.PolypeptideCombiner$MergePolyPeptides",handler);
        doRegistration(kryo,"com.lordjoe.distributed.input.FastaInputFormat",handler);
        doRegistration(kryo,"com.lordjoe.distributed.protein.DigestProteinFunction",handler);
        doRegistration(kryo,"com.lordjoe.distributed.spark.IdentityFunction",handler);
        doRegistration(kryo,"com.lordjoe.distributed.spark.LongAccumulableParam",handler);
        doRegistration(kryo,"com.lordjoe.distributed.spark.MachineUseAccumulator",handler);
        doRegistration(kryo,"com.lordjoe.distributed.spark.MachineUseAccumulator$MachineUseAccumulableParam",handler);
        doRegistration(kryo,"com.lordjoe.distributed.spark.SparkAccumulators",handler);
        doRegistration(kryo,"com.lordjoe.distributed.tandem.LibraryBuilder",handler);
        doRegistration(kryo,"com.lordjoe.distributed.tandem.LibraryBuilder$1",handler);
        doRegistration(kryo,"com.lordjoe.distributed.tandem.LibraryBuilder$MapPolyPeptideToSequenceKeys",handler);
        doRegistration(kryo,"com.lordjoe.distributed.tandem.LibraryBuilder$ParsedProteinToProtein",handler);
        doRegistration(kryo,"com.lordjoe.distributed.tandem.LibraryBuilder$PeptideByStringToByMass",handler);
        doRegistration(kryo,"com.lordjoe.distributed.tandem.LibraryBuilder$ProcessByKey",handler);
        doRegistration(kryo,"org.slf4j.impl.Log4jLoggerAdapter",handler);
        doRegistration(kryo,"org.systemsbiology.hadoop.DelegatingFileStreamOpener",handler);
        doRegistration(kryo,"org.systemsbiology.hadoop.FileStreamOpener",handler);
        doRegistration(kryo,"org.systemsbiology.hadoop.HadoopMajorVersion",handler);
        doRegistration(kryo,"org.systemsbiology.hadoop.StreamOpeners$ResourceStreamOpener",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.BadAminoAcidException",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.FastaAminoAcid",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.MassCalculator",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.MassCalculator$MassPair",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.MassSpecRun",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.MassType",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.RawPeptideScan",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.SequenceUtilities",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.SpectrumCondition",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.TandemKScoringAlgorithm",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.TandemKScoringAlgorithm$KScoringConverter",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.TaxonomyProcessor",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.XTandemMain",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.ionization.IonType",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.ionization.IonUseCounter",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.ionization.IonUseScore",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.peptide.AminoTerminalType",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.peptide.ModifiedPolypeptide",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.peptide.PeptideBondDigester",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.peptide.PeptideBondDigester$LysineC",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.peptide.PeptideBondDigester$Trypsin",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.peptide.PeptideModification",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.peptide.PeptideModificationRestriction",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.peptide.Polypeptide",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.scoring.Scorer",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.scoring.ScoringModifications",handler);
        doRegistration(kryo,"org.systemsbiology.xtandem.taxonomy.Taxonomy",handler);
        doRegistration(kryo,"com.lordjoe.distributed.database.PeptideDatabase",handler);
     }


    public static void main(String[] args) {
        // Does not work and I am not sure we need it
        // this will fail if there are issues with any registered classes
        new HydraKryoSerializer().registerClasses(null);
    }

}


