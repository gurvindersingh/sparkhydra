package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.sax.*;
import org.systemsbiology.xtandem.peptide.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.sax.IonScoreHandler
 *
 * @author Steve Lewis
 * @date Dec 23, 2010
 */
public class ProteinPositionHandler extends AbstractXTandemElementSaxHandler<IProteinPosition> {
    public static ProteinPositionHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = ProteinPositionHandler.class;

    public static final String TAG = "ProteinPosition";

    public static final String SAMPLE =
            " <ProteinPosition  id=\"\" before_aa=\"E\" after_aa=\"E\" start=\"1\" />";

    private final IPolypeptide m_Peptide;

    public ProteinPositionHandler(IElementHandler parent, IPolypeptide peptide) {
        super(TAG, parent);
        m_Peptide = peptide;
    }


    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {
        String protein = attr.getValue("id");
        FastaAminoAcid before = FastaAminoAcid.asAminoAcidOrNull(attr.getValue("before_aa"));
        FastaAminoAcid after = FastaAminoAcid.asAminoAcidOrNull(attr.getValue("after_aa"));
         int pStartPosition = Integer.parseInt(attr.getValue("start"));

        ProteinPosition elementObject = new ProteinPosition(protein, m_Peptide, before, after, pStartPosition);
        setElementObject(elementObject);


        return;
    }


    /**
     * finish handling and set up the enclosed object
     * Usually called when the end tag is seen
     */
    @Override
    public void finishProcessing() {
        // finish building the object

    }
}
