package com.lordjoe.distributed.hydra.test;

import com.lordjoe.distributed.spectrum.*;
import com.lordjoe.utilities.*;
import org.systemsbiology.xtandem.*;
import scala.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.test.ParseMGFTest
 * User: Steve
 * Date: 12/10/2014
 */
public class ParseMGFTest {

    private static String readOneSpectrum(final LineNumberReader pRdr) throws IOException {
        StringBuilder sb = new StringBuilder();

        String line = pRdr.readLine();
        while (line != null && !line.startsWith("BEGIN IONS")) {
            line = pRdr.readLine();
        }
        if (line == null)
            return null;
        while (line != null && !line.startsWith("END IONS")) {
            sb.append(line);
            sb.append("\n");
            line = pRdr.readLine();
        }
        if (line.startsWith("END IONS"))
            return sb.toString();
        return null;
    }

    public static void main(String[] args) throws Exception {
        ElapsedTimer timer = new ElapsedTimer();

        File mgfFile = new File(args[0]);
        int numberSpectra = Integer.parseInt(args[1]);

        LineNumberReader rdr = new LineNumberReader(new FileReader(mgfFile));

        List<Tuple2<String, String>> holder = new ArrayList<Tuple2<String, String>>();
        int index = 1;
        while (holder.size() < numberSpectra) {
            String spectrum = readOneSpectrum(rdr);
            if (spectrum == null)
                break;
            holder.add(new Tuple2(Integer.toString(index++), spectrum));
        }
        timer.showElapsed("read " + holder.size() + " spectra");
        List<IMeasuredSpectrum> specHolder = new ArrayList<IMeasuredSpectrum>();
        MGFStringTupleToSpectrumTuple func = new MGFStringTupleToSpectrumTuple(null);
        for (Tuple2<String, String> s : holder) {
            Iterable<Tuple2<String, IMeasuredSpectrum>> call = func.call(s);
            for (Tuple2<String, IMeasuredSpectrum> value : call) {
                specHolder.add(value._2());
            }

        }

        timer.showElapsed("parsed  " + specHolder.size() + " spectra");

    }

}
