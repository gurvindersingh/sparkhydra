package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.systemsbiology.xtandem.ionization.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.sax.IonScoreHandler
 *
 * @author Steve Lewis
 * @date Dec 23, 2010
 */
public class IonScoreHandler extends AbstractXTandemElementSaxHandler<IonUseScore> {
    public static IonScoreHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = IonScoreHandler.class;

    public static final String TAG = "IonScore";

    public static final String SAMPLE =
         "<IonScore  A_count=\"0\" A_score=\"0.0\" B_count=\"8\" B_score=\"0.31470590317621827\" C_count=\"0\" C_score=\"0.0\" Y_count=\"6\" Y_score=\"0.45595267112366855\" X_count=\"0\" X_score=\"0.0\" Z_count=\"0\" Z_score=\"0.0\" />";

    private final IonUseScore m_Score = new IonUseScore();

    public IonScoreHandler(IElementHandler parent) {
        super(TAG,parent);
    }


    public IonUseScore getScore() {
        return m_Score;
    }

    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {
        for(IonType type : IonType.values())  {
            addTypeData(type,attr);
        }
          return;
    }

    protected void addTypeData(final IonType pType, final Attributes attr) {
        String scoreStr = pType.toString() +  "_score";
        double score = XTandemSaxUtilities.getRequiredDoubleAttribute(scoreStr, attr);
        if(score == 0)
            return;
        IonUseScore score1 = getScore();
        String countStr = pType.toString() +  "_count";
        int count = XTandemSaxUtilities.getRequiredIntegerAttribute(countStr, attr);
        score1.setCount(pType,count);

        score1.setScore(pType, score);
      }


    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        // finish building the object
           setElementObject(m_Score);

    }
}
