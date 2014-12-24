package org.systemsbiology.xtandem.peptide;

/**
 * org.systemsbiology.xtandem.peptide.PeptideModificationRestriction
 * User: steven
 * Date: 7/5/11
 */
public enum PeptideModificationRestriction {
    Global,NTerminal,CTerminal;

    public static final String NTERMINAL_STRING = "[";
    public static final String CTERMINAL_STRING = "]";

    public String getRestrictionString() {
        switch(this)  {
            case NTerminal:
                  return NTERMINAL_STRING ;
            case CTerminal:
                  return CTERMINAL_STRING ;
            default:
                return "";
          }
    }
}
