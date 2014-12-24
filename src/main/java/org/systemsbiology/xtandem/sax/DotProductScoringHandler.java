package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.testing.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.sax.DotProductScoringHandler
 * User: steven
 * Date: 6/22/11
 */
public class DotProductScoringHandler extends AbstractXTandemElementSaxHandler<TheoreticalIonsScoring> implements ITopLevelSaxHandler {
    public static final DotProductScoringHandler[] EMPTY_ARRAY = {};


    public static final String TAG = "dot_product";

    private final ScanScoringReport m_ParentReport;
    private IScanScoring m_Scoring;
    private IonType m_Type;
    private int m_Charge;
    private int m_Mass;

    public DotProductScoringHandler(IElementHandler parent, ScanScoringReport parentReport) {
        super(TAG, parent);
        m_ParentReport = parentReport;
    }


    public IonType getType() {
        return m_Type;
    }

    public void setType(final IonType pType) {
        m_Type = pType;
    }

    public int getMass() {
        return m_Mass;
    }

    public void setMass(final int pMass) {
        m_Mass = pMass;
    }

    public int getCharge() {
        return m_Charge;
    }

    public void setCharge(final int pCharge) {
        m_Charge = pCharge;
    }

    public ScanScoringReport getParentReport() {
        return m_ParentReport;
    }

    public IScanScoring getScoring() {
        return m_Scoring;
    }

    public void setScoring(final IScanScoring pScoring) {
        m_Scoring = pScoring;
    }

    @Override
    public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        String value;
        value = attributes.getValue("id");
        IScanScoring score = getParentReport().getScanScoringMap(value);
        setScoring(score);

        String ionType = attributes.getValue("type");
        IonType type = null;
        if (ionType != null)
            type = IonType.valueOf(ionType);
        setType(type);

        String mz = attributes.getValue("mz");
        double mzRatio = 0;
        if (mz != null)
            mzRatio = Double.parseDouble(mz);


        String chargeStr = attributes.getValue("charge");
        if (chargeStr != null)
            setCharge(Integer.parseInt(chargeStr));

        value = attributes.getValue("sequence");
        ITheoreticalScoring scoring = score.getScoring(value);
        if (getType() != null) {
            ScanScoringIdentifier key = new ScanScoringIdentifier(value, getCharge(), getType());
            TheoreticalIonsScoring realScore = scoring.getIonScoring(key);
            realScore.setMZRatio(mzRatio);
            setElementObject(realScore);
        }
        else {
            String YCountStr = attributes.getValue("Ycount");
             if (YCountStr != null) {
                 int ycount = XTandemSaxUtilities.getRequiredIntegerAttribute("Ycount", attributes);
                 ScanScoringIdentifier key = new ScanScoringIdentifier(value, getCharge(), IonType.Y);
                TheoreticalIonsScoring realScore = scoring.getIonScoring(key);
                realScore.setMZRatio(mzRatio);
                double bscore = XTandemSaxUtilities.getRequiredDoubleAttribute("Yscore", attributes);
                realScore.setScore(bscore);
                double bcscore = XTandemSaxUtilities.getRequiredDoubleAttribute("Y_Cscore", attributes);
                realScore.setCScore(bcscore);


            }
            String BCountStr = attributes.getValue("Bcount");
             if (BCountStr != null) {
                 int bcount = XTandemSaxUtilities.getRequiredIntegerAttribute("Bcount", attributes);
                 ScanScoringIdentifier key = new ScanScoringIdentifier(value, getCharge(), IonType.B);
                TheoreticalIonsScoring realScore = scoring.getIonScoring(key);
                realScore.setMZRatio(mzRatio);
                double bscore = XTandemSaxUtilities.getRequiredDoubleAttribute("Bscore", attributes);
                realScore.setScore(bscore);
                double bcscore = XTandemSaxUtilities.getRequiredDoubleAttribute("B_Cscore", attributes);
                realScore.setCScore(bcscore);

            }



            String scoreStr = attributes.getValue("total_score");
            if (scoreStr != null)    {
                double total_score = Double.parseDouble(scoreStr);
                ((TheoreticalScoring)scoring).setTotalKScore(total_score);
            }


        }


        super.handleAttributes(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
        if ("match_at_mass".equals(qName)) {
            TheoreticalIonsScoring myScore = getElementObject();
            int offset = Integer.parseInt(attributes.getValue("offset"));
            int ion = Integer.parseInt(attributes.getValue("ion"));
            double added = Double.parseDouble(attributes.getValue("added"));
            DebugMatchPeak dp = new DebugMatchPeak(offset, added, ion, getType());
            myScore.addScoringMass(dp);
            return;
        }
        if ("scores".equals(qName)) {
            return;
        }
        if ("measured_ions".equals(qName)) {
            IScanScoring scoring = getScoring();
            MeasuredSpectrumIonsHandler handler = new MeasuredSpectrumIonsHandler(this, scoring);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, qName, attributes);
            return;
        }
        if ("theoretical_ions".equals(qName)) {
            ITheoreticalIonsScoring myScore = getElementObject();
            IScanScoring scoring = getScoring();
            TheoreticalSpectrumHandler handler = new TheoreticalSpectrumHandler(this, scoring);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, qName, attributes);
            return;
        }
        super.startElement(uri, localName, qName, attributes);    //To change body of overridden methods use File | Settings | File Templates.
    }


    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {

        if ("match_at_mass".equals(el)) {     //ignore for now
            return;
        }
        if ("scores".equals(el)) {     //ignore for now
            return;
        }
        if ("measured_ions".equals(el)) {
            ISaxHandler handler1 = getHandler().popCurrentHandler();
            if (handler1 instanceof MeasuredSpectrumIonsHandler) {
                MeasuredSpectrumIonsHandler handler = (MeasuredSpectrumIonsHandler) handler1;
                IMeasuredSpectrum measured = handler.getElementObject();
                TheoreticalIonsScoring scoring = getElementObject();
                scoring.setMeasuredSpectrum(measured);
            }
            return;
        }

        if ("theoretical_ions".equals(el)) {
            ISaxHandler handler1 = getHandler().popCurrentHandler();
            if (handler1 instanceof TheoreticalSpectrumHandler) {
                TheoreticalSpectrumHandler handler = (TheoreticalSpectrumHandler) handler1;
                ITheoreticalSpectrum theory = handler.getElementObject();
                TheoreticalIonsScoring scoring = getElementObject();
                scoring.setTheoreticalSpectrum(theory);
            }
            return;
        }

        super.endElement(elx, localName, el);    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {

    }
}
