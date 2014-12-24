package org.systemsbiology.xtandem.scoring;

import org.systemsbiology.xtandem.ionization.*;

import java.io.*;

/**
 * org.systemsbiology.xtandem.scoring.SpectrumGenerationListener
 * User: steven
 * Date: 10/27/11
 */
public interface SpectrumGenerationListener extends Serializable {
    public static final SpectrumGenerationListener[] EMPTY_ARRAY = {};


    public void onSpectrumGeneration(ITheoreticalSpectrumSet spec);

}
