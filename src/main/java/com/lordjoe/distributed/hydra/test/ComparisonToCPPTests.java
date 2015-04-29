package com.lordjoe.distributed.hydra.test;

import com.lordjoe.distributed.hydra.scoring.*;
import org.systemsbiology.xtandem.peptide.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.hydra.test.ComparisonToCPPTests
 * User: Steve
 * Date: 4/19/2015
 */
public class ComparisonToCPPTests {

    public static final Integer INTERESTING_KEY = 8852;

    private final Map<Integer, List<CometScoredResult>> cppResultsByKey = new HashMap<Integer, List<CometScoredResult>>();
    private final Map<Integer, List<CometScoredResult>> hydraResultsByKey = new HashMap<Integer, List<CometScoredResult>>();

    public ComparisonToCPPTests() {
    }

    public void readCppResults(File file) {
        try {
            LineNumberReader rdr = new LineNumberReader(new FileReader(file));
            String line = rdr.readLine();
            while (line != null) {
                if (!line.startsWith("#") || line.length() == 0) {
                    CometScoredResult result = new CometScoredResult(line);
                    List<CometScoredResult> holder;
                    if (cppResultsByKey.containsKey(result.id)) {
                        holder = cppResultsByKey.get(result.id);
                    }
                    else {
                        holder = new ArrayList<CometScoredResult>();
                        cppResultsByKey.put(result.id, holder);
                    }
                    holder.add(result);
                }
                line = rdr.readLine();
            }

        }
        catch (IOException e) {
            throw new RuntimeException(e);

        }

    }
    public void readHydraResults(File file) {
         try {
             LineNumberReader rdr = new LineNumberReader(new FileReader(file));
             String line = rdr.readLine();
             while (line != null) {
                 if (!line.startsWith("#") || line.length() == 0) {
                     String[] split = line.split("\t");
                       int index = 0;
                     Integer  id = new Integer(split[index++]);
                     String  peptide = split[index++];
                    Double   mass = new Double(split[index++]);
                     index++; // ignore second mass
                     Double  score = new Double(split[index++]);

                     CometScoredResult result = new CometScoredResult(id,peptide,mass,score);
                     List<CometScoredResult> holder;
                     if (hydraResultsByKey.containsKey(result.id)) {
                         holder = hydraResultsByKey.get(result.id);
                     }
                     else {
                         holder = new ArrayList<CometScoredResult>();
                         hydraResultsByKey.put(result.id, holder);
                     }
                     holder.add(result);
                 }
                 line = rdr.readLine();
             }

         }
         catch (IOException e) {
             throw new RuntimeException(e);

         }

     }

    public static String cleanUpPeptideString(String paptide)
    {
         return paptide.replace("[15]","");
    }
    public  Set<String> getPolypeptides() {
         Set<String> peptides = new HashSet<String>();
         for (Integer key : cppResultsByKey.keySet()) {
             if(INTERESTING_KEY != key)
                 continue; // hard code one case
             final List<CometScoredResult> cometScoredResults = cppResultsByKey.get(key);
             for (CometScoredResult cometScoredResult : cometScoredResults) {
                peptides.add(cleanUpPeptideString(cometScoredResult.peptide));
             }
         }
       return peptides;
      }


    @SuppressWarnings("UnusedDeclaration")
    public Map<Integer, List<CometScoredResult>> getCppResultsByKey() {
        return cppResultsByKey;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Map<Integer, List<CometScoredResult>> getHydraResultsByKey() {
        return hydraResultsByKey;
    }

    public static void main(String[] args) throws Exception {
        ComparisonToCPPTests tests = new ComparisonToCPPTests();
        File f1 = new File(args[0]);
        File f2 = new File(args[1]);
         tests.readCppResults(f1);
        tests.readHydraResults(f2);

        final List<CometScoredResult> cppResults = tests.cppResultsByKey.get(INTERESTING_KEY);
        final List<CometScoredResult> HydraResults = tests.hydraResultsByKey.get(INTERESTING_KEY);

        Collections.sort(cppResults, new CometScoredResultComparator());
        Collections.sort(HydraResults, new CometScoredResultComparator());

        for (CometScoredResult c : cppResults) {
            System.out.println(c);
        }
        System.out.println("==================================");
        for (CometScoredResult c : HydraResults) {
             System.out.println(c);
         }


                Set < String > pepList = tests.getPolypeptides();
        Set<IProtein> proteins = new HashSet<IProtein>();
   //     GoodSampleConstructor.peptidesToProteins("contaminants_concatenated_target_decoy.fasta",pepList,proteins);

      //  GoodSampleConstructor.writeProteins("SmallSampleProteins.fasta",proteins);
    }


    private static class CometScoredResultComparator implements Comparator<CometScoredResult> {
        @Override
        public int compare(CometScoredResult o1, CometScoredResult o2) {
            return o1.peptide.compareTo(o2.peptide);
        }
    }
}
