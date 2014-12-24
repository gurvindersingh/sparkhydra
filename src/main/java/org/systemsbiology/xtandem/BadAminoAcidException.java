package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.BadAminoAcidException
 * User: steven
 * Date: 8/27/12
 */
public class BadAminoAcidException  extends RuntimeException{
    public static final BadAminoAcidException[] EMPTY_ARRAY = {};

    public BadAminoAcidException(String in) {
        super("No Amino acid for char " + in);
    }

    public BadAminoAcidException(char in) {
        super("No Amino acid for char " + in);
    }
}
