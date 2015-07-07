package com.lordjoe.distributed.protein;

import org.systemsbiology.xtandem.peptide.*;
import org.systemsbiology.xtandem.taxonomy.*;

import java.io.*;
import java.util.*;

/**
 * com.lordjoe.distributed.protein.ProteinParser
 *
 * @author Steve Lewis
 * @date 5/24/2015
 */
public class ProteinParser {

    public static List<IProtein> getProteinsFromFile(String filename) {
        try {
            InputStream inputStream = new FileInputStream(filename);
            return getProteinsFromStream(inputStream,filename);
        } catch (FileNotFoundException e) {
            throw new UnsupportedOperationException(e);
        }

    }

    public static List<IProtein> getProteinsFromResource(String resource) {
        InputStream inputStream = ProteinParser.class.getResourceAsStream(resource);
        return getProteinsFromStream(inputStream,resource);
    }


    public static List<IPolypeptide> getPeptidesFromResource(String resource,IPeptideDigester digester) {
        List<IProtein> proteins  = getProteinsFromResource( resource);
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (IProtein protein : proteins) {
            IPolypeptide[] digested = digester.digest(protein);
              holder.addAll(Arrays.asList(digested));
        }
        return holder;
    }


    public static List<IPolypeptide> getPeptidesFromResource(String resource,
                                                             IPeptideDigester digester,PeptideModification[] mods) {
        List<IProtein> proteins  = getProteinsFromResource( resource);
        List<IPolypeptide> holder = new ArrayList<IPolypeptide>();
        for (IProtein protein : proteins) {
            List<IPolypeptide> digested = DigestProteinFunction.digestWithModifications(protein,digester,mods);
            holder.addAll(digested);
        }
        return holder;
    }



    public static List<IProtein> getProteinsFromStream(InputStream is,String url) {
        ProteinFastaHandler handler = new ProteinFastaHandler();
        FastaParser fp = new FastaParser();
        fp.addHandler(handler);
          fp.parseFastaFile(is, url);

        return handler.getProteins();
    }

    public static class ProteinFastaHandler implements IFastaHandler {
        private final List<IProtein> m_proteins = new ArrayList<IProtein>();

        public ProteinFastaHandler( ) {
          }

        @Override
        public void handleProtein(final String annotation, final String sequence) {
            handleProtein(annotation, sequence, "");
        }


        @Override
        public void handleProtein(final String annotation, final String sequence, String url) {
            Protein protein = Protein.getProtein(annotation, annotation, sequence, url);
            m_proteins.add(protein);
        }

        public List<IProtein> getProteins() {
            return m_proteins;
        }
    }

}



