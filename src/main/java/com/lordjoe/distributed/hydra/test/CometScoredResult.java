package com.lordjoe.distributed.hydra.test;

/**
 * com.lordjoe.distributed.hydra.comet.CometScoredResult
 * User: Steve
 * Date: 4/19/2015
 */
public class CometScoredResult  {
    public final Integer id;
    public final String peptide;
    public final Double mass;
    public final Double score;

    @SuppressWarnings("UnusedDeclaration")
    public CometScoredResult(final Integer pId, final String pPeptide, final double pMass, final double pScore) {
        id = pId;
        peptide = pPeptide;
        mass = pMass;
        score = pScore;
    }

    public CometScoredResult(String line) {
        String[] split = line.split("\t");
        int index = 0;
        id = new Integer(split[index++]);
        peptide = split[index++];
        mass = new Double(split[index++]);
        score = new Double(split[index++]);
        ;
    }

    @Override
    public String toString() {
        return "id=" + id + "\t" + peptide + '\'' + "\t" + mass + "\t" + score + "\t";
    }
}
