package org.systemsbiology.xtandem.peptide;

import org.systemsbiology.sax.*;
import org.systemsbiology.xml.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.hadoop.*;
import org.systemsbiology.xtandem.sax.*;

import java.io.*;
import java.util.*;

/**
 * org.systemsbiology.xtandem.peptide.DigesterDescription
 * User: Steve
 * Date: 9/20/11
 */
public class DigesterDescription implements IEquivalent<DigesterDescription> {
    public static final DigesterDescription[] EMPTY_ARRAY = {};
    public static final String TAG = "digester";

    public static final String CURRENT_DIGESTER_VERSION = "1";

    public static DigesterDescription fromApplication(XTandemMain app)
    {
       return  new FastaHadoopLoader(app).getDescription();
    }

    private String m_Version = "0";
    private boolean m_HasDecoys;
    private PeptideBondDigester m_Digester;
    private final List<PeptideModification> m_Modification = new ArrayList<PeptideModification>();

    public DigesterDescription() {
    }

    public DigesterDescription(String str) {
        this(XMLUtilities.stringToInputStream(str));
    }

    public DigesterDescription(InputStream is) {
        DelegatingSaxHandler handler = new DelegatingSaxHandler();
        final DigesterDescriptorHandler handler1 = new DigesterDescriptorHandler(handler, this);
        handler.pushCurrentHandler(handler1);
        handler.parseDocument(is);
    }

    public boolean isHasDecoys() {
        return m_HasDecoys;
    }

    public void setHasDecoys(boolean hasDecoys) {
        m_HasDecoys = hasDecoys;
    }

    public void addModification(PeptideModification added) {
        m_Modification.add(added);
    }


    public PeptideModification[] getModifications() {
        return m_Modification.toArray(new PeptideModification[0]);
    }

    public PeptideBondDigester getDigester() {
        return m_Digester;
    }

    public void setDigester(final PeptideBondDigester pDigester) {
        m_Digester = pDigester;
    }

    public boolean isSemitryptic() {
        return getDigester().isSemiTryptic();
    }

    public String getVersion() {
        return m_Version;
    }

    public void setVersion(final String pVersion) {
        if(pVersion == null) {
           m_Version = "0";
        }
        m_Version = pVersion;
    }

    public void serializeAsString(IXMLAppender ap) {
        IPeptideDigester digester = getDigester();
        String digestRules = digester.toString();
        ap.openTag(TAG);
        ap.appendAttribute("version", getVersion());
        ap.appendAttribute("hasDecoys", Boolean.toString(isHasDecoys()));
        ap.appendAttribute("rules", digestRules);
        ap.appendAttribute("semityptic", digester.isSemiTryptic());
        ap.appendAttribute("numberMissedCleavages", digester.getNumberMissedCleavages());
        ap.endTag();
        ap.cr();
        PeptideModification[] modifications = getModifications();
        if (modifications != null && modifications.length > 0) {
            ap.openEmptyTag("modifications");
            ap.cr();
            for (int i = 0; i < modifications.length; i++) {
                PeptideModification mod = modifications[i];
                mod.serializeAsString(ap);
            }
            ap.closeTag("modifications");
        }
        ap.closeTag(TAG);
    }

    public String asXMLString() {
        StringBuilder sb = new StringBuilder();
        XMLAppender ap = new XMLAppender(sb);
        serializeAsString(ap);
        return sb.toString();
    }

    /**
     * return true if this and o are 'close enough'
     *
     * @param o !null test object
     * @return as above
     */
    @Override
    public boolean equivalent(final DigesterDescription o) {

        if(!getVersion().equals(o.getVersion()))
            return false;
        PeptideBondDigester d1 = getDigester();
        PeptideBondDigester d2 = o.getDigester();
        // we do not have the same rules
        if (!d2.toString().equals(d1.toString()))
            return false;


        // we do not have enough miss cleavages
        if (d2.getNumberMissedCleavages() < d1.getNumberMissedCleavages())
            return false;

        // we do not have semitryptic
        if (!d2.isSemiTryptic() && d1.isSemiTryptic())
            return false;
         Set<PeptideModification> myMods = new HashSet<PeptideModification>(Arrays.asList(getModifications()));
        Set<PeptideModification> theirMods = new HashSet<PeptideModification>(Arrays.asList(o.getModifications()));
        // new modification
        if (!theirMods.containsAll(myMods))
            return false;

        if(isHasDecoys() != o.isHasDecoys())
            return false;

        return true; // I can use this database
    }
}
