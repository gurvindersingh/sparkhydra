package org.systemsbiology.xtandem.taxonomy;

/**
 * org.systemsbiology.xtandem.taxonomy.IFastaHandler
 * implemented by a piece of code to handle proteins coming off a fasta file
 * User: Steve
 * Date: Apr 11, 2011
 */
public interface IFastaHandler {

    /**
     * do whatever you what with the porotein
     * @param annotation !null annotation - should be unique
     * @param sequence   !null sequence
     */
    public void handleProtein(String annotation,String sequence);
    /**
     * do whatever you what with the porotein
     * @param annotation !null annotation - should be unique
     * @param sequence   !null sequence
     * @param url   !null url
      */
    public void handleProtein(String annotation,String sequence,String url);

}
