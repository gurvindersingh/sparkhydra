package org.systemsbiology.xtandem.ionization;

import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;

/**
 * org.systemsbiology.xtandem.ionization.IonType
 * User: steven
 * Date: Jan 10, 2011
 */
public enum IonType {
    A("CO",AminoTerminalType.N),
    B("",AminoTerminalType.N),
    C("NH3",AminoTerminalType.N),
    Y("H2O",AminoTerminalType.C),
    X("CO2",AminoTerminalType.C),
    Z("NH2",AminoTerminalType.C);


    public static final IonType[] A_ION_TYPES = {IonType.A};
    public static final IonType[] B_ION_TYPES = {IonType.B};
    public static final IonType[] BY_ION_TYPES = {IonType.B,IonType.Y};
    public static final IonType[] C_ION_TYPES = {IonType.C};
    public static final int NUMBER_ION_TYPES = IonType.values().length;
    /**
     * convert to an index
     * @param in  !null value
     * @return  0.. n - 1
     */
    public static int asIndex(IonType in)  {
        switch(in)  {
            case A:
                 return 0;
            case B:
                 return 1;
            case C:
                 return 2;
            case X:
                 return 3;
            case Y:
                 return 4;
            case Z:
                 return 5;
         }
         throw new IllegalStateException("Never get here");
    }

    public static final IonType[] EMPTY_ARRAY = {};

    private final String m_AtomicOffset;
     private final AminoTerminalType m_Terminal;
    private Double m_MassOffset;
  
    IonType(final String pAtomicOffset,AminoTerminalType term) {
        m_AtomicOffset = pAtomicOffset;
        m_Terminal = term;
    }

    public AminoTerminalType getTerminal() {
        return m_Terminal;
    }

    public String getAtomicOffset() {
        return m_AtomicOffset;
    }

    public double getMassOffset() {
        if(m_MassOffset == null)
              m_MassOffset = MassCalculator.getDefaultCalculator( ).calcMass(getAtomicOffset());
         return m_MassOffset;
    }

    /**
     * A-X, B-Y, C-Z are difference sides of the same cleaved bond
     *
     * @return
     */
    public IonType getPair() {
        switch (this) {
            case A:
                return X;
            case B:
                return Y;
            case C:
                return Z;
            case X:
                return A;
            case Y:
                return B;
            case Z:
                return C;
        }
        throw new UnsupportedOperationException("Never get here");
    }

}
