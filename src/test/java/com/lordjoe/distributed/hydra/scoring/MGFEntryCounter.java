package com.lordjoe.distributed.hydra.scoring;

import java.io.*;

/**
 * com.lordjoe.distributed.hydra.scoring.MGFEntryCounter
 * User: Steve
 * Date: 1/23/2015
 */
public class MGFEntryCounter {

    private static void countMGFEntries(final String fileName) throws Exception {
          LineNumberReader rdr = new LineNumberReader(new FileReader(fileName));
          String line = rdr.readLine();
          int count = 0;
          while(line != null) {
              if(line.contains("BEGIN IONS"))
                  count++;
              line = rdr.readLine();
          }
          System.out.println(fileName + " contains " + count + " spectra");

      }

    public static void main(String[] args) throws Exception {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            countMGFEntries(arg);
        }
    }


}
