package com.lordjoe.distributed.tandem;

import com.lordjoe.distributed.*;
import org.systemsbiology.xtandem.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.tandem.LibraryBuilder
 * User: Steve
 * Date: 9/24/2014
 */
public class JavaLibraryBuilder {

    private final XTandemMain application;

    public JavaLibraryBuilder(File congiguration) {
        application = new XTandemMain(congiguration);
    }


    public static List<KeyValueObject<String, String>> parseFastaFile(String fasta) {
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(fasta));
            List<KeyValueObject<String, String>> holder = new ArrayList<KeyValueObject<String, String>>();
            String key = null;
           String line = rdr.readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null) {
                if (line.startsWith(">")) {
                    if (key != null && sb.length() > 0) {
                        holder.add(new KeyValueObject<String, String>(key, sb.toString()));
                        key = null;
                    }
                    sb.setLength(0);
                    if(line.length() > 0)
                         key = line.substring(1);
                    else
                        key = "BLANK";
                }
                else {
                    sb.append(line);

                }
                line = rdr.readLine();
            }

            if (key != null && key.length() > 0 && sb.length() > 0) {
                holder.add(new KeyValueObject<String, String>(key, sb.toString()));
            }

            return holder;
        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }
    }

    public XTandemMain getApplication() {
        return application;
    }

//    public static void main(String[] args) {
//        if (args.length == 0) {
//            System.out.println("usage configFile fastaFile");
//            return;
//        }
//        File config = new File(args[0]);
//        String fasta = args[1];
//        JavaLibraryBuilder lb = new JavaLibraryBuilder(config);
//
//         List<KeyValueObject<String, String>> proteins =  parseFastaFile(fasta);
//
//        ProteinMapper pm = new ProteinMapper(lb.getApplication());
//        ProteinReducer pr = new ProteinReducer(lb.getApplication());
//
//        JavaMapReduce handler = new JavaMapReduce(pm, pr, IPartitionFunction.HASH_PARTITION);
//
//        handler.mapReduceSource(proteins);
//
//        Iterable<KeyValueObject<String, String>> list = handler.collect();
//
//
//
//        int index = 0;
//        for (KeyValueObject<String, String> keyValueObject : list) {
//            System.out.println(keyValueObject);
//            if(index++ > 1000)
//                break;
//        }
//
//
//    }
}
