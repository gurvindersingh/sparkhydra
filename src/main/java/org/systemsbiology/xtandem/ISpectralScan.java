package org.systemsbiology.xtandem;

/**
 * org.systemsbiology.xtandem.ISpectralScan
 *    Holds data about the mass spec run -
 *  needed but not while scoring
 * @author Steve Lewis
  */
public interface ISpectralScan
{
    public static ISpectralScan[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ISpectralScan.class;

    public String getId();

    public int getIndex();

     public int getMsLevel();

    public  int getScanEvent();

    public  ScanTypeEnum getScanType();

     public ScanPolarity getPolarity();

     public String getRetentionTime();

    public FragmentationMethod getActivationMethod();

    public double getLowMz();

    public double getHighMz();

    public double getBasePeakMz();

    public double getBasePeakIntensity();

    public double getTotIonCurrent();

    public double getCompensationVoltage();

    public String getInstrumentId();
}
