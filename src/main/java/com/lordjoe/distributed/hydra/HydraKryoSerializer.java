package com.lordjoe.distributed.hydra;

import com.esotericsoftware.kryo.*;
import org.apache.spark.serializer.*;

import javax.annotation.*;
import java.io.*;
import java.lang.reflect.*;

/**
 * com.lordjoe.distributed.hydra.HydraKryoSerializer
 * User: Steve
 * Date: 10/28/2014
 */
public class HydraKryoSerializer implements KryoRegistrator, Serializable {


    public HydraKryoSerializer() {
    }

    /**
     * register a class indicated by name
     *
     * @param kryo
     * @param s       name of a class - might not exist
     * @param handled Set of classes already handles
     */
    protected void doRegistration(@Nonnull Kryo kryo, @Nonnull String s) {
        Class c;
        try {
            c = Class.forName(s);
            doRegistration(kryo, c);
        }
        catch (ClassNotFoundException e) {
            return;
        }
    }

    /**
     * register a class
     *
     * @param kryo
     * @param s       name of a class - might not exist
     * @param handled Set of classes already handles
     */
    protected void doRegistration(final Kryo kryo, final Class pC) {
        if (kryo != null) {
            kryo.register(pC);
            // also register arrays of that class
            Class arrayType = Array.newInstance(pC, 0).getClass();
            kryo.register(arrayType);
        }
    }


    /**
     * do the real work of registering all classes
     *
     * @param kryo
     */
    @Override
    public void registerClasses(@Nonnull Kryo kryo) {
        kryo.register(Object[].class);
        kryo.register(scala.Tuple2[].class);
        kryo.register(scala.Tuple3[].class);
        kryo.register(int[].class);
        kryo.register(double[].class);
        kryo.register(short[].class);
        kryo.register(long[].class);
        kryo.register(byte[].class);
        kryo.register(Class[].class);
        kryo.register(long[].class);
        kryo.register(boolean[].class);
        kryo.register(String[].class);
        kryo.register(String[].class);
        Class cls = scala.reflect.ClassTag.class;
        kryo.register(cls);
        //    kryo.register(scala.reflect.ClassTag$$anon$1.class);

//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
//        doRegistration(kryo,"XXX");
        doRegistration(kryo, "org.apache.spark.util.collection.CompactBuffer");
        doRegistration(kryo, "scala.collection.mutable.WrappedArray$ofRef");
        doRegistration(kryo, "org.systemsbiology.xtandem.scoring.VariableStatistics");
        doRegistration(kryo, "org.systemsbiology.xtandem.scoring.SpectralPeakUsage$PeakUsage");
        doRegistration(kryo, "org.systemsbiology.xtandem.scoring.SpectralPeakUsage");
        doRegistration(kryo, "org.systemsbiology.xtandem.ionization.TheoreticalPeak");
        doRegistration(kryo, "org.systemsbiology.xtandem.ionization.ITheoreticalPeak");
        doRegistration(kryo, "org.systemsbiology.xtandem.scoring.ScoringSpectrum");
        doRegistration(kryo, "org.systemsbiology.xtandem.ionization.ITheoreticalSpectrum");
        doRegistration(kryo, "org.systemsbiology.xtandem.ionization.TheoreticalSpectrumSet");
        doRegistration(kryo, "org.systemsbiology.xtandem.testing.DebugMatchPeak");
        doRegistration(kryo, "org.systemsbiology.xtandem.testing.ScanScoringIdentifier");
        doRegistration(kryo, "org.systemsbiology.xtandem.testing.TheoreticalIonsScoring");
        doRegistration(kryo, "org.systemsbiology.xtandem.testing.ITheoreticalIonsScoring");
        doRegistration(kryo, "org.systemsbiology.xtandem.scoring.ExtendedSpectralMatch");
        doRegistration(kryo, "org.systemsbiology.xtandem.scoring.BoundedMatchSet");
        doRegistration(kryo, "org.systemsbiology.xtandem.ionization.IonUseCounter");
        doRegistration(kryo, "org.systemsbiology.xtandem.scoring.HyperScoreStatistics");
        doRegistration(kryo, "org.systemsbiology.xtandem.ScoringMeasuredSpectrum");
        doRegistration(kryo, "org.systemsbiology.xtandem.scoring.OriginatingScoredScan");
        doRegistration(kryo, "org.systemsbiology.xtandem.ScanTypeEnum");
        doRegistration(kryo, "org.systemsbiology.xtandem.ScanPrecursorMz");
        doRegistration(kryo, "org.systemsbiology.xtandem.ScanPolarity");
        doRegistration(kryo, "rg.systemsbiology.xtandem.ScanPolarity");
        doRegistration(kryo, "org.systemsbiology.xtandem.SpectrumPeak");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.fragment.BinChargeKey");
        doRegistration(kryo, "org.systemsbiology.xtandem.ISpectrumPeak");
        doRegistration(kryo, "java.util.HashMap");
        doRegistration(kryo, "org.systemsbiology.xtandem.FragmentationMethod");
        doRegistration(kryo, "org.apache.spark.scheduler.CompressedMapStatus");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.PeptideModification");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.PeptideValidity");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.Polypeptide");
        doRegistration(kryo, "org.systemsbiology.xtandem.FastaAminoAcid");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.ProteinPosition");
        doRegistration(kryo, "java.util.ArrayList");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.ModifiedPolypeptide");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.IProteinPosition");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.Protein");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities$1");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities$IdentityFunction");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities$TupleValues");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.SparkFileOpener");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.SparkXTandemMain");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.protein.PolypeptideCombiner$MergePolyPeptides");
        doRegistration(kryo, "com.lordjoe.distributed.input.FastaInputFormat");
        doRegistration(kryo, "com.lordjoe.distributed.protein.DigestProteinFunction");
        doRegistration(kryo, "com.lordjoe.distributed.spark.IdentityFunction");
        doRegistration(kryo, "com.lordjoe.distributed.spark.LongAccumulableParam");
        doRegistration(kryo, "com.lordjoe.distributed.spark.MachineUseAccumulator");
        doRegistration(kryo, "com.lordjoe.distributed.spark.MachineUseAccumulator$MachineUseAccumulableParam");
        doRegistration(kryo, "com.lordjoe.distributed.spark.SparkAccumulators");
        doRegistration(kryo, "com.lordjoe.distributed.tandem.LibraryBuilder");
        doRegistration(kryo, "com.lordjoe.distributed.tandem.LibraryBuilder$1");
        doRegistration(kryo, "com.lordjoe.distributed.tandem.LibraryBuilder$MapPolyPeptideToSequenceKeys");
        doRegistration(kryo, "com.lordjoe.distributed.tandem.LibraryBuilder$ParsedProteinToProtein");
        doRegistration(kryo, "com.lordjoe.distributed.tandem.LibraryBuilder$PeptideByStringToByMass");
        doRegistration(kryo, "com.lordjoe.distributed.tandem.LibraryBuilder$ProcessByKey");
        doRegistration(kryo, "org.slf4j.impl.Log4jLoggerAdapter");
        doRegistration(kryo, "org.systemsbiology.hadoop.DelegatingFileStreamOpener");
        doRegistration(kryo, "org.systemsbiology.hadoop.FileStreamOpener");
        doRegistration(kryo, "org.systemsbiology.hadoop.HadoopMajorVersion");
        doRegistration(kryo, "org.systemsbiology.hadoop.StreamOpeners$ResourceStreamOpener");
        doRegistration(kryo, "org.systemsbiology.xtandem.BadAminoAcidException");
        doRegistration(kryo, "org.systemsbiology.xtandem.FastaAminoAcid");
        doRegistration(kryo, "org.systemsbiology.xtandem.MassCalculator");
        doRegistration(kryo, "org.systemsbiology.xtandem.MassCalculator$MassPair");
        doRegistration(kryo, "org.systemsbiology.xtandem.MassSpecRun");
        doRegistration(kryo, "org.systemsbiology.xtandem.MassType");
        doRegistration(kryo, "org.systemsbiology.xtandem.RawPeptideScan");
        doRegistration(kryo, "org.systemsbiology.xtandem.SequenceUtilities");
        doRegistration(kryo, "org.systemsbiology.xtandem.SpectrumCondition");
        doRegistration(kryo, "org.systemsbiology.xtandem.TandemKScoringAlgorithm");
        doRegistration(kryo, "org.systemsbiology.xtandem.TandemKScoringAlgorithm$KScoringConverter");
        doRegistration(kryo, "org.systemsbiology.xtandem.TaxonomyProcessor");
        doRegistration(kryo, "org.systemsbiology.xtandem.XTandemMain");
        doRegistration(kryo, "org.systemsbiology.xtandem.ionization.IonType");
        doRegistration(kryo, "org.systemsbiology.xtandem.ionization.IonUseCounter");
        doRegistration(kryo, "org.systemsbiology.xtandem.ionization.IonUseScore");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.AminoTerminalType");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.ModifiedPolypeptide");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.PeptideBondDigester");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.PeptideBondDigester$LysineC");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.PeptideBondDigester$Trypsin");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.PeptideModification");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.PeptideModificationRestriction");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.scoring.AppendScanStringToWriter");

        doRegistration(kryo, "com.lordjoe.distributed.hydra.AddIndexToSpectrum");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.scoring.SortByIndex");
        doRegistration(kryo, "org.systemsbiology.xtandem.peptide.Polypeptide");
        doRegistration(kryo, "org.systemsbiology.xtandem.scoring.Scorer");
        doRegistration(kryo, "org.systemsbiology.xtandem.scoring.ScoringModifications");
        doRegistration(kryo, "org.systemsbiology.xtandem.taxonomy.Taxonomy");
        doRegistration(kryo, "com.lordjoe.distributed.database.PeptideDatabase");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities$1");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities$IdentityFunction");
        doRegistration(kryo, "com.lordjoe.distributed.SparkUtilities$TupleValues");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.scoring.SparkMapReduceScoringHandler");
        doRegistration(kryo, "com.lordjoe.distributed.spark.SparkAccumulators");
        doRegistration(kryo, "com.lordjoe.utilities.ElapsedTimer");
        doRegistration(kryo, "org.systemsbiology.xtandem.XTandemMain");
        doRegistration(kryo, "org.systemsbiology.xtandem.reporting.BiomlReporter");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.scoring.ScoredScanWriter");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.scoring.ToIndexTuple");

        // Comet classes
        doRegistration(kryo, "com.lordjoe.distributed.hydra.comet.CometScoredScan");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.comet.BinnedChargeIonIndex");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.comet.SpectrinBinnedScore");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.comet.CometParameterTests");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.comet.XCorrUsedData");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.comet.CometUtilities");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.comet.CometTheoreticalBinnedSet");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.comet.CometScoringAlgorithm");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.comet.CometPeak");
        doRegistration(kryo, "com.lordjoe.distributed.hydra.comet.BinnedChargeIonIndex");



    }


}


