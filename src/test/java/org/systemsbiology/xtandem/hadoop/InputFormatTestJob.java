package org.systemsbiology.xtandem.hadoop;

import com.lordjoe.utilities.*;
import org.apache.hadoop.conf.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.*;
import org.apache.hadoop.mapreduce.lib.output.*;

import java.io.*;
import java.util.*;

/**
 *  org.systemsbiology.hadoop.InputFormatTestJob
 */
public class InputFormatTestJob {

    private static final List<String>  gRecords = new ArrayList<String>();

    public static String[] getRecords()
    {
        return gRecords.toArray(new String[0]);
    }
    public static void clearRecords()
    {
          gRecords.clear();
    }
    public static void addRecord(String s)
    {
          gRecords.add(s);
    }

    public static class RecordRecordingMapper
            extends Mapper<Object, Text, Text, IntWritable> {


        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
              addRecord(value.toString() );
        }
    }


    public static class NullReducer
            extends Reducer<Text, IntWritable, Text, IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
         }
    }


    public static void runJob(String[] args) throws Exception {
        Configuration conf = new Configuration();
         Job job = new Job(conf, "test");
        conf = job.getConfiguration(); // NOTE JOB Copies the configuraton
        job.setJarByClass(InputFormatTestJob.class);
        job.setMapperClass(RecordRecordingMapper.class);
        job.setReducerClass(NullReducer.class);

        Class<?> aClass = Class.forName(args[0]);
        job.setInputFormatClass(( Class<? extends org.apache.hadoop.mapreduce.InputFormat>) aClass);
        job.setMapOutputKeyClass(Text.class);
           job.setMapOutputValueClass(Text.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.setInputPaths(job,new Path(args[1]) );

        
        // added Slewis
        job.setNumReduceTasks(1);
    //    job.setPartitionerClass(MyPartitioner.class);

       String athString = args[2];
        // you must pass the output directory as the last argument
         File out = new File(args[2]);
        if (out.exists()) {
            FileUtilities.expungeDirectory(out);
            out.delete();
        }

        Path outputDir = new Path(athString);


        FileOutputFormat.setOutputPath(job, outputDir);


        boolean ans = job.waitForCompletion(true);
        int ret = ans ? 0 : 1;
     }
}