package com.lordjoe.distributed.test;

/**
 * com.lordjoe.distributed.test.CometSpectralScoring
 * User: Steve
 * Date: 4/14/2015
 */
public class CometSpectralScoring {
    public final int id;
    public final String peptide;
    public final double mass;
    public final double score;

    public CometSpectralScoring(String line) {
        String[] items = line.split("\t");
        id = new Integer(items[0]);
        peptide = items[1];
        mass = Double.parseDouble(items[2]);
        score = Double.parseDouble(items[3]);

    }

    public String getIdString() {
         StringBuilder sb = new StringBuilder();
        sb.append(id);
        sb.append(":");
         boolean inBrackets = false;
        for (char c : peptide.toCharArray()) {
            if(Character.isLetter(c))
                sb.append(c);
        }

         return sb.toString();
    }


    @Override
    public String toString() {
          return getIdString();
    }

}
