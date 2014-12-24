package org.systemsbiology.xtandem.bioml.sax;

import org.systemsbiology.sax.*;
import org.systemsbiology.xtandem.*;
import org.systemsbiology.xtandem.bioml.*;
import org.systemsbiology.xtandem.ionization.*;
import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.sax.*;
import org.xml.sax.*;

import java.util.*;

/**
 * org.systemsbiology.xtandem.bioml.sax.BiomlSaxHandler
 * User: steven
 * Date: 8/22/11
 */
public class ProteinXtandemReportHandler extends AbstractXTandemElementSaxHandler<XTandemReportProtein> {
    public static final ProteinXtandemReportHandler[] EMPTY_ARRAY = {};

    private List<LocatedPeptideModification> m_Modification = new ArrayList<LocatedPeptideModification>();


    public ProteinXtandemReportHandler(final IElementHandler pParent) {
        super("protein", pParent);
        setElementObject(new XTandemReportProtein());
    }


    public void addModification(LocatedPeptideModification added) {
        m_Modification.add(added);
    }


    public LocatedPeptideModification[] getModifications() {
        return m_Modification.toArray(new LocatedPeptideModification[0]);
    }


    @Override
    public void handleAttributes(final String uri, final String localName, final String qName, final Attributes attr) throws SAXException {
        XTandemReportProtein protein = getElementObject();
        protein.setId(attr.getValue("id"));
        protein.setUid(attr.getValue("uid"));
        protein.setLabel(attr.getValue("label"));
        String value = XTandemSaxUtilities.getRequiredAttribute("sumI",attr);
        // this value is sometines like -1.#
         try {
              protein.setSumI(Double.parseDouble(value));
          }
         catch (NumberFormatException e) {
          }

         protein.setExpect(XTandemSaxUtilities.getRequiredDoubleAttribute("expect", attr));

        super.handleAttributes(uri, localName, qName, attr);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attr) throws SAXException {
        if ("file".equals(qName)) {
            XTandemReportProtein protein = getElementObject();
            protein.setFile(attr.getValue("URL"));
            return;
        }
        if ("peptide".equals(qName)) {
            XTandemReportProtein protein = getElementObject();
            protein.setPeptideLength(XTandemSaxUtilities.getRequiredIntegerAttribute("end", attr));
            return;
        }
        if ("domain".equals(qName)) {
            handleDomain(uri, localName, qName, attr);
            return;
        }
        if ("aa".equals(qName)) {  // amino acid modification - handled in the startTag
            handleAmimoAcidModification(uri, localName, qName, attr);
            return;
        }
        super.startElement(uri, localName, qName, attr);
    }

    private void handleAmimoAcidModification(final String uri, final String localName, final String QName, final Attributes attr) {
        String type = attr.getValue("type");
        if("[".equals(type))
            return;
        FastaAminoAcid aa = null;
        try {
            aa = FastaAminoAcid.valueOf(type);
        }
        catch (IllegalArgumentException e) {
            throw e;  // should not happen

        }
        int location = XTandemSaxUtilities.getRequiredIntegerAttribute("at", attr);
        if(location > 0) // we write at 1 based so need to decrement
            location--;
        String massChange = attr.getValue("modified" );
        PeptideModification pm = PeptideModification.fromString(massChange + "@" + aa,PeptideModificationRestriction.Global,false  );
        LocatedPeptideModification lpm = new LocatedPeptideModification(pm, location);
        addModification(lpm);
    }

    private void handleDomain(final String uri, final String localName, final String qName, final Attributes attr) throws SAXException {
        XTandemReportProtein protein = getElementObject();
        int missed_cleavages = XTandemSaxUtilities.getRequiredIntegerAttribute("missed_cleavages", attr);
        String sequence = attr.getValue("seq");



        IPolypeptide pp = new Polypeptide(sequence, missed_cleavages);
        protein.setPolypeptide(pp);

        double expect = XTandemSaxUtilities.getDoubleAttribute("expect", attr,0);
        protein.setExpect(expect);
         double hyperscore = XTandemSaxUtilities.getDoubleAttribute("hyperscore", attr,0);
        protein.setHyperscore(hyperscore);
        double nextscore = XTandemSaxUtilities.getDoubleAttribute("nextscore", attr,0);
        protein.setNextScore(nextscore);
         double mh = XTandemSaxUtilities.getDoubleAttribute("mh", attr,0);
        protein.setMH(mh);

         double delta = XTandemSaxUtilities.getDoubleAttribute("delta", attr,0);
        protein.setDelta(delta);

        IonUseScore  score = buildScore(attr);
        protein.setScore(score);

        //     id="13.1.1" start="935" end="948" expect="3.2e+000" mh="1472.698" delta="0.768" hyperscore="333" nextscore="332" y_score="0" y_ions="4" b_score="1" b_ions="4" c_score="0" c_ions="0" z_score="0" z_ions="0" a_score="0" a_ions="0" x_score="0" x_ions="0" pre="DTMK" post="LKLD" seq="GEQVNGEKPDNASK" missed_cleavages="0"
    }


    public IonUseScore buildScore(  Attributes attr)
            throws SAXException {
        IonUseScore ret = new  IonUseScore();
        for(IonType type : IonType.values())  {
            addTypeData(ret,type,attr);
        }
          return ret;
    }

    protected void addTypeData(IonUseScore score1 ,final IonType pType, final Attributes attr) {
         String countStr = (pType.toString() +  "_ions").toLowerCase() ;
        int count = XTandemSaxUtilities.getIntegerAttribute(countStr, attr,0);
        score1.setCount(pType,count);

        String scoreStr = (pType.toString() +  "_score").toLowerCase() ;
        double score = XTandemSaxUtilities.getDoubleAttribute(scoreStr, attr,0);
        score1.setScore(pType, score);
      }



    private void handleAssignment(final String uri, final String localName, final String qName, final Attributes attr) throws SAXException {
        XTandemReportProtein protein = getElementObject();

        int start = XTandemSaxUtilities.getIntegerAttribute("start", attr, 0);
        int end = XTandemSaxUtilities.getIntegerAttribute("end", attr, 0);
        String pre = attr.getValue("pre");
        String post = attr.getValue("post");
        String annotation = this.getNote("label");

        ProteinAssignment pp = new ProteinAssignment(start, end, pre, post, annotation);
        protein.setAssignment(pp);


        //      id="13.1.1" start="935" end="948" expect="3.2e+000" mh="1472.698" delta="0.768" hyperscore="333" nextscore="332" y_score="0" y_ions="4" b_score="1" b_ions="4" c_score="0" c_ions="0" z_score="0" z_ions="0" a_score="0" a_ions="0" x_score="0" x_ions="0" pre="DTMK" post="LKLD" seq="GEQVNGEKPDNASK" missed_cleavages="0"
    }


    @Override
    public void endElement(final String elx, final String localName, final String el) throws SAXException {
        if ("file".equals(el)) {
            return;
        }
        if ("peptide".equals(el)) {
            return;
        }
        if ("domain".equals(el)) {
            return;
        }
        if ("aa".equals(el)) {  // amino acid modification - handled in the startTag
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
