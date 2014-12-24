package org.systemsbiology.xtandem.scoring;

import com.lordjoe.utilities.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.scoring.BoundedMatchSet
 * User: steven
 * Date: 12/5/11
 */
public class BoundedMatchSet /*extends BoundedTreeSet<ISpectralMatch>  */ implements Serializable {
    public static final Comparator<ISpectralMatch> COOMPARER = new SpectralMatchComparator();


    public static class SpectralMatchComparator implements Comparator<ISpectralMatch>, Serializable {
        @Override
        public int compare(final ISpectralMatch o1, final ISpectralMatch o2) {
            if (o1 == o2)
                return 0;
            double s1 = o1.getHyperScore();
            double s2 = o2.getHyperScore();
            if (s1 != s2) {
                return s1 < s2 ? 1 : -1;
            }
            return Util.objectCompareTo(o1, o2);

        }

    }


    public BoundedMatchSet() {

    }

    public BoundedMatchSet(Collection<? extends ISpectralMatch> col) {
        this();
        for (ISpectralMatch match : col) {
            addMatch(match);
        }
    }

    public void addMatch(ISpectralMatch added) {
        if (best == null) {
            best = added;
            return;
        }
        double bestHS = best.getHyperScore();
        double addedHS = added.getHyperScore();
        if (addedHS > bestHS) {
            ISpectralMatch temp = added;
            best = added;
            added = temp;
        }
        if (nextbest == null) {
            nextbest = added;
            return;
        }
        double nextBestHS = nextbest.getHyperScore();
        addedHS = added.getHyperScore();
        if (addedHS > nextBestHS) {
            nextbest = added;
        }
    }


    private ISpectralMatch best;
    private ISpectralMatch nextbest;

    public int size() {
        if (best == null)
            return 0;
        if (nextbest == null)
            return 1;

        return 2;

    }


    public ISpectralMatch getBest() {
        return best;
    }

    public ISpectralMatch getNextbest() {
        return nextbest;
    }

    public ISpectralMatch[] getMatches() {
        List<ISpectralMatch> holder = new ArrayList<ISpectralMatch>();
        if (best != null)
            holder.add(best);
        if (nextbest != null)
            holder.add(nextbest);

        ISpectralMatch[] ret = new ISpectralMatch[holder.size()];
        holder.toArray(ret);
        return ret;
    }


    // ================================
    // Old Code
    // =======================
//    public BoundedMatchSet( ) {
//         super(COOMPARER, XTandemHadoopUtilities.getNumberCarriedMatches());
//     }
//
//
//    public BoundedMatchSet(Collection<? extends ISpectralMatch> col) {
//         this();
//         addAll(col);
//     }
//
//    public ISpectralMatch nextBest()
//    {
//             return nthBest(1);
//
//    }


}
