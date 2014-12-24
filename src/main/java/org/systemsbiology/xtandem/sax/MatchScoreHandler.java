package org.systemsbiology.xtandem.sax;


import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.scoring.*;
import org.systemsbiology.xtandem.taxonomy.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.sax.MatchScoreHandler
 *
 * @author Steve Lewis
 * @date Dec 23, 2010
 */
public class MatchScoreHandler extends AbstractXTandemElementSaxHandler<SpectralMatch> {
    public static MatchScoreHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = MatchScoreHandler.class;

    public static final String TAG = "match";

    //            "<match  peak=\"VNG0675C VNG0675C:25(19)\" score=\"173.526783956915\" hyperscore=\"173.526783956915\">\n" +

    private String m_Id;
    private String m_PeptideId;
    private double m_Score;
    private double m_HyperScore;
    private double m_RawScore;
    private SpectralPeakUsage m_Usage;
    private IPolypeptide m_Peptide;
      private List<IProteinPosition> m_Positions = new ArrayList<IProteinPosition>();

    private final IonUseScore m_IonScore = new IonUseScore();


    public MatchScoreHandler(String id, IElementHandler parent) {
        super(TAG, parent);
        m_Id = id;
    }


    public String getId() {
        return m_Id;
    }


    public String getPeptideId() {
        return m_PeptideId;
    }



    public void setPeptideId(final String pPeptideId) {
        if (m_Peptide != null || m_PeptideId != null)
            throw new IllegalStateException("Cannot reset peptide"); // ToDo change
        m_PeptideId = pPeptideId;
        IMainData main = getMainData();
        if (main != null) {
            //   RawPeptideScan rawScan = main.getRawScan(getId());
            //    pMeasured = rawScan;

            ITaxonomy taxonomy = main.getTaxonomy();
            if (taxonomy != null) {
                String peptideId = getPeptideId();
                m_Peptide = taxonomy.getPeptideById(peptideId);
                if (m_Peptide == null)
                    m_Peptide = Polypeptide.fromString(peptideId); // key may be all we need

            }
        }
        else {
            m_Peptide = Polypeptide.fromString(getPeptideId());
        }
        if (m_Peptide != null) {
            IPeptideDigester digester = PeptideBondDigester.getDefaultDigester();
            int missedCleavages = digester.probableNumberMissedCleavages(m_Peptide);
            ((Polypeptide) m_Peptide).setMissedCleavages(missedCleavages);
        }
    }

    public double getScore() {
        return m_Score;
    }

    public void setScore(final double pScore) {
        m_Score = pScore;
    }

    public double getHyperScore() {
        return m_HyperScore;
    }

    public void setHyperScore(final double pHyperScore) {
        m_HyperScore = pHyperScore;
    }

    public double getRawScore() {
        return m_RawScore;
    }

    public void setRawScore(final double pRawScore) {
        m_RawScore = pRawScore;
    }

    public IonUseScore getIonScore() {
        return m_IonScore;
    }

    public boolean isDecoy() {
        if (m_Peptide != null)
            return m_Peptide.isDecoy();
        else return false;
    }

    public void setDecoy(boolean isso) {
        if (m_Peptide == null)
            return;
        if (m_Peptide.isDecoy() == isso)
            return;

        if (isso)
            m_Peptide = m_Peptide.asDecoy();
    }

    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {
        setPeptideId(attr.getValue("peak")); // num="1"
        setScore(XTandemSaxUtilities.getRequiredDoubleAttribute("score", attr));
        setRawScore(XTandemSaxUtilities.getRequiredDoubleAttribute("raw_score", attr));
        setHyperScore(XTandemSaxUtilities.getRequiredDoubleAttribute("hyperscore", attr));
        boolean decoy = XTandemSaxUtilities.getBooleanAttribute("decoy", attr, false);
        if(decoy)
            setDecoy(decoy);
        return;
    }

    /**
     * Receive notification of the start of an element.
     * <p/>
     * <p>By default, do nothing.  Application writers may override this
     * method in a subclass to take specific actions at the start of
     * each element (such as allocating a new tree node or writing
     * output to a file).</p>
     *
     * @param uri        The Namespace URI, or the empty string if the
     *                   element has no Namespace URI or if Namespace
     *                   processing is not being performed.
     * @param localName  The local name (without prefix), or the
     *                   empty string if Namespace processing is not being
     *                   performed.
     * @param el         The qualified name (with prefix), or the
     *                   empty string if qualified names are not available.
     * @param attributes The attributes attached to the element.  If
     *                   there are no attributes, it shall be an empty
     *                   Attributes object.
     * @throws org.xml.sax.SAXException Any SAX exception, possibly
     *                                  wrapping another exception.
     * @see org.xml.sax.ContentHandler#startElement
     */
    @Override
    public void startElement(String uri, String localName, String el, Attributes attributes)
            throws SAXException {
        if ("IonScore".equals(el)) {
            IonScoreHandler handler = new IonScoreHandler(this);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, el, attributes);
            return;
        }
        if ("ProteinPosition".equals(el)) {
            ProteinPositionHandler handler = new ProteinPositionHandler(this, m_Peptide);
            getHandler().pushCurrentHandler(handler);
            handler.handleAttributes(uri, localName, el, attributes);
            return;
        }
        if ("usage".equals(el)) {
            clearIncludedText();
            return;
        }
        super.startElement(uri, localName, el, attributes);

    }

    @Override
    public void endElement(String elx, String localName, String el) throws SAXException {
        if ("IonScore".equals(el)) {
            IonScoreHandler handler = (IonScoreHandler) getHandler().popCurrentHandler();
            m_IonScore.set(handler.getElementObject());
            return;
        }
        if ("ProteinPosition".equals(el)) {
            ProteinPositionHandler handler = (ProteinPositionHandler) getHandler().popCurrentHandler();
            m_Positions.add(handler.getElementObject());
            return;
        }
        if ("usage".equals(el)) {
            String txt = getIncludedText();
            m_Usage = SpectralPeakUsage.deserializeUsage(txt);
            return;
        }

        super.endElement(elx, localName, el);
    }


    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        IMeasuredSpectrum pMeasured = null;

        final double pScore = getScore();
        final double pHyperScore = getHyperScore();
        final double rawScore = getRawScore();
        IonTypeScorer scorer = getIonScore();

        IProteinPosition[] containedInProteins = m_Positions.toArray(IProteinPosition.EMPTY_ARRAY);
        ((Polypeptide) m_Peptide).setContainedInProteins(containedInProteins);
        SpectralMatch scan = new SpectralMatch(m_Peptide, pMeasured, pScore, pHyperScore, rawScore, scorer, null);

        if (m_Usage != null)
            scan.getUsage().addTo(m_Usage);

        setElementObject(scan);
    }


}
