package org.systemsbiology.xtandem.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.peptide.*;
import org.xml.sax.*;

/**
 * org.systemsbiology.xtandem.sax.SaxMzXMLHandler
 *
 * @author Steve Lewis
 * @date Dec 23, 2010
 */
public class DigesterDescriptorHandler extends AbstractElementSaxHandler<DigesterDescription> implements IMainDataHolder {
    public static DigesterDescriptorHandler[] EMPTY_ARRAY = {};
    public static Class THIS_CLASS = DigesterDescriptorHandler.class;

    public static final String TAG = DigesterDescription.TAG;


    public DigesterDescriptorHandler(DelegatingSaxHandler parent, DigesterDescription obj) {
        super(TAG, parent);
        setElementObject(obj);
    }


    @Override
    public void handleAttributes(String elx, String localName, String el, Attributes attr)
            throws SAXException {
        String rules = attr.getValue("rules");
        DigesterDescription dig = getElementObject();
        PeptideBondDigester digester = PeptideBondDigester.getDigester(rules);
        dig.setDigester(digester);

        boolean semi = XTandemSaxUtilities.getRequiredBooleanAttribute("semityptic", attr);
        digester.setSemiTryptic(semi);
        String version = attr.getValue("version");
        if (version != null)
            getElementObject().setVersion(version);
        int missedCleavages = XTandemSaxUtilities.getRequiredIntegerAttribute("numberMissedCleavages", attr);
        digester.setNumberMissedCleavages(missedCleavages);

        boolean hasDecoys = XTandemSaxUtilities.getBooleanAttribute("hasDecoys", attr, false);
        dig.setHasDecoys(hasDecoys);

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
        if (TAG.equals(el)) {
            handleAttributes(uri, localName, el, attributes);
            return;
        }
        if ("modifications".equals(el)) {
            return;
        }
        if ("modification".equals(el)) {
            PeptideModificationRestriction restrict = XTandemSaxUtilities.getRequiredEnumAttribute("restriction", attributes, PeptideModificationRestriction.class);
            FastaAminoAcid fs;

            switch (restrict) {
                case NTerminal:
                case CTerminal:
                    fs = XTandemSaxUtilities.getEnumAttribute("aminoacid", attributes, FastaAminoAcid.class, (FastaAminoAcid) null);
                    break;
                default:
                    fs = XTandemSaxUtilities.getRequiredEnumAttribute("aminoacid", attributes, FastaAminoAcid.class);
            }
            String masschange = attributes.getValue("massChange");
            PeptideModification pm;
            if(fs == null)
                pm = PeptideModification.fromString(masschange + "@" + restrict.getRestrictionString(), restrict, false);
            else
                pm = PeptideModification.fromString(masschange + "@" + fs, restrict, false);

            getElementObject().addModification(pm);
            return;
        }
        super.startElement(uri, localName, el, attributes);

    }

    @Override
    public void endElement(String elx, String localName, String el) throws SAXException {
        if (TAG.equals(el)) {
            finishProcessing();
            return;
        }
        if ("modifications".equals(el)) {
            return;
        }
        if ("modification".equals(el)) {
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
    }


}
