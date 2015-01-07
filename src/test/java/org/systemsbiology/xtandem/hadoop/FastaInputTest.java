package org.systemsbiology.xtandem.hadoop;


import com.lordjoe.distributed.input.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;
import org.apache.hadoop.util.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

import java.io.*;


/**
 * org.systemsbiology.hadoop.FastaInputTest
 */
public class FastaInputTest  extends  ToolRunner  {

    public static final int MAX_TEST_PROTEINS = 2000;

    private static int gNumberMappedProteins;
    private static int gNumberReducedProteins;
    private static int gNumberDecoysProteins;

    public static int getNumberDecoysProteins() {
        return gNumberDecoysProteins;
    }

    public static void incrementNumberDecoysProteins() {
        gNumberDecoysProteins++;
    }


    public static int getNumberReducedProteins() {
        return gNumberReducedProteins;
    }

    public static void incrementNumberReducedProteins() {
        gNumberReducedProteins++;
    }

    public static int getNumberMappedProteins() {
        return gNumberMappedProteins;
    }

    public static void incrementNumberMappedProteins() {
        gNumberMappedProteins++;
    }


    public static final String MODIFICATIONS = "15.994915@M";




    public static class ProteinMapper extends Mapper<Writable, Text, Text, Text> {

        private   XTandemMain m_Application;
         private FastaHadoopLoader m_Loader;

        public FastaHadoopLoader getLoader() {
            return m_Loader;
        }

        /**
         * Called once at the beginning of the task.
         */
        @Override
        protected void setup(final Context context) throws IOException, InterruptedException {
            super.setup(context);
            IPeptideDigester digester = PeptideBondDigester.getDigester("Trypsin");
            digester.setSemiTryptic(true);
            if(true)
                throw new UnsupportedOperationException("Fix This"); // ToDo
   //         m_Application = XTandemHadoopUtilities.loadFromContext(context);
            m_Application.loadTaxonomy();
            m_Loader = new FastaHadoopLoader(m_Application);
            m_Loader.setModifications(PeptideModification.fromListString(MODIFICATIONS,PeptideModificationRestriction.Global,true));
         }

        public void map(Writable key, Text value, Context context
        ) throws IOException, InterruptedException {
            // for now skip early
            if(getNumberMappedProteins() > MAX_TEST_PROTEINS)
                return;

            String label = key.toString();
            String sequence = value.toString();
            String ucLabel = label.toUpperCase();
            if (ucLabel.contains("DECOY") || ucLabel.startsWith("RANDOM") )
                incrementNumberDecoysProteins();
            incrementNumberMappedProteins();
            FastaHadoopLoader loader = getLoader();
            loader.handleProtein(  label, sequence );
          //  context.write(key, value);

        }
    }


    public static class ProteinReducer
            extends Reducer<Text, Text, Text, Text> {

        private Text m_OnlyKey = new Text();
        private Text m_OnlyValue = new Text();

        public void reduce(Text key, Iterable<Text> values,
                           Context context
        ) throws IOException, InterruptedException {

            String sequence = key.toString();
            IPolypeptide pp = Polypeptide.fromString(sequence);
            StringBuilder sb = new StringBuilder();
            for (Text val : values) {
                if(sb.length() > 0)
                    sb.append(";");
                sb.append(val.toString());
            }
            double aMass = XTandemUtilities.getAverageMass(pp) ;
            int averagemass = XTandemUtilities.getDefaultConverter().asInteger(aMass);
             double mMass = XTandemUtilities.getMonoisotopicMass(pp) ;
             int monomass = XTandemUtilities.getDefaultConverter().asInteger(mMass);
             String proteins =  sb.toString();

            m_OnlyKey.set("avge-" + averagemass);
            m_OnlyValue.set(sequence + "," + aMass + "," + averagemass + "," + mMass + "," + monomass + "," + proteins );
            context.write(m_OnlyKey, m_OnlyValue);

            m_OnlyKey.set("mono-" + monomass);
            context.write(m_OnlyKey,m_OnlyValue);
           }
    }

    /**
      * Execute the command with the given arguments.
      *
      * @param args command specific arguments.
      * @return exit code.
      * @throws Exception
      */
     public int runJob(Configuration conf, final String[] args)  throws Exception   {
         String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
//        if (otherArgs.length != 2) {
//            System.err.println("Usage: wordcount <in> <out>");
//            System.exit(2);
//        }
           Job job = new Job(conf, "Fasta Format");
           conf = job.getConfiguration(); // NOTE JOB Copies the configuraton
            job.setJarByClass(FastaInputTest.class);
           job.setInputFormatClass(FastaInputFormat.class);
           job.setMapperClass(ProteinMapper.class);
           job.setReducerClass(ProteinReducer.class);

           job.setMapOutputKeyClass(Text.class);
           job.setMapOutputValueClass(Text.class);
           job.setOutputKeyClass(Text.class);
           job.setOutputValueClass(Text.class);


           // added Slewis
           job.setNumReduceTasks(HadoopUtilities.DEFAULT_REDUCE_TASKS);
           //    job.setPartitionerClass(MyPartitioner.class);

           if (otherArgs.length > 1) {
               String otherArg = otherArgs[0];
               FileInputFormat.addInputPath(job, new Path(otherArg));
           }

           // you must pass the output directory as the last argument
           String athString = otherArgs[otherArgs.length - 1];
           File out = new File(athString);
//        if (out.exists()) {
//            FileUtilities.expungeDirectory(out);
//            out.delete();
//        }

           Path outputDir = new Path(athString);

           FileSystem fileSystem = outputDir.getFileSystem(conf);
           XTandemHadoopUtilities.expunge(outputDir, fileSystem);    // make sure thia does not exist
           FileOutputFormat.setOutputPath(job, outputDir);


           boolean ans = job.waitForCompletion(true);
           int ret = ans ? 0 : 1;

           int numberDecoys = getNumberDecoysProteins();
           int numberMapped = getNumberMappedProteins();
           int numberReduced = getNumberReducedProteins();

       //    if (numberMapped != numberReduced)
        //       throw new IllegalStateException("problem"); // ToDo change

           return ret;

     }


//    /**
//     * Execute the command with the given arguments.
//     *
//     * @param args command specific arguments.
//     * @return exit code.
//     * @throws Exception
//     */
//    @Override
//    public int run(final String[] args) throws Exception {
//        if (args.length == 0)
//            throw new IllegalStateException("needs a file name");
//        Configuration conf = new Configuration();
//        return runJob(  conf,   args);
//      }
//
//    /**
//     * args[0] is input file
//     * args[1] is output directory
//     * @param args
//     * @throws Exception
//     */
//    public static void main(String[] args) throws Exception {
//         ToolRunner.run(new FastaInputTest(), args);
//    }
}