package com.lordjoe.distributed.tandem;

import com.lordjoe.distributed.*;
import com.lordjoe.distributed.output.*;
import org.apache.spark.api.java.*;
import org.apache.spark.api.java.function.*;
import org.systemsbiology.hadoop.*;
import org.systemsbiology.xtandem.peptide.*;

import java.io.*;

/**
 * com.lordjoe.distributed.tandem.LibraryWriter
 * User: Steve
 * Date: 10/14/2014
 */
public class LibraryWriter implements Serializable {

    public static JavaPairRDD<Integer, WriterObject> writeDatabase(JavaPairRDD<Integer, IPolypeptide> peptides) {
        final PolypeptideToFileName peptideToFileName = new PolypeptideToFileName();

        Function<IPolypeptide, WriterObject> createAcc = new Function<IPolypeptide, WriterObject>() {
            @Override
            public WriterObject call(IPolypeptide pp) {
                WriterObject a = new WriterObject(peptideToFileName, pp);
                a.write(pp);
                return a;
            }
        };
        Function2<WriterObject, IPolypeptide, WriterObject> addAndCount =
                new Function2<WriterObject, IPolypeptide, WriterObject>() {
                    @Override
                    public WriterObject call(WriterObject a, IPolypeptide pp) {
                        a.write(pp);
                        return a;
                    }
                };
        Function2<WriterObject, WriterObject, WriterObject> combine =
                new Function2<WriterObject, WriterObject, WriterObject>() {
                    @Override
                    public WriterObject call(WriterObject a, WriterObject b) {
                        a.append(b);
                        return a;
                    }
                };
        return peptides.combineByKey(createAcc, addAndCount, combine);
    }

    public static class WriterObject extends AbstractKeyWriter<IPolypeptide> {

        public WriterObject(PolypeptideToFileName peptideToFileName, IPolypeptide pp) {
                 super(peptideToFileName.doCall(pp));
                write(pp);
             }


        public void write(IPolypeptide pp) {
            StringBuilder sb = new StringBuilder();
            sb.append(pp.getSequence());
            sb.append(",");
            double mass = pp.getMass();
            sb.append(String.format("%10.4f", mass));
              sb.append(",");
            int matchingMass = (int)pp.getMatchingMass();
            sb.append( matchingMass);
            sb.append(",");
            IProteinPosition[] proteinPositions = pp.getProteinPositions();
            sb.append(proteinPositions) ;
            getWriter().println(sb.toString());

        }

        public void append(WriterObject pp) {
            PrintWriter writer = getWriter();

            throw new UnsupportedOperationException("Fix This"); // ToDo
        }

        @Override
        protected String buildOutPath(final IPolypeptide pp) {
            int mass = (int) pp.getMatchingMass(); // todo should resolution be finer
             return HadoopUtilities.buildFileNameFromMass(mass);
        }
    }

    private static class PolypeptideToFileName  extends AbstractLoggingFunction<IPolypeptide, String> {
           @Override
        public String doCall(IPolypeptide pp) {
             int mass = (int) pp.getMatchingMass(); // todo should resolution be finer
             return HadoopUtilities.buildFileNameFromMass(mass);
        }
    }
}
