package org.systemsbiology.xtandem.taxonomy;

//import org.springframework.jdbc.core.simple.*;
//import org.systemsbiology.xtandem.*;
//
//import java.util.*;
//
///**
// * org.systemsbiology.xtandem.taxonomy.TaxonomyDatabase
// * statements to create and delete a database
// * User: Steve
// * Date: Apr 12, 2011
// */
//public class TaxonomyDatabase {
//    public static final TaxonomyDatabase[] EMPTY_ARRAY = {};
//
//    public static final int MAX_KEY_LENGTH = 767;
//
//    public static final String[] TABLES =
//            {
//                    "proteins",
//                    "average_mz_to_fragments",
//                    "mono_mz_to_fragments",
//                    "fragment_protein",
//                    "load_fragments",
//                    "semi_average_mz_to_fragments",
//                    "semi_mono_mz_to_fragments",
//                    "semi_fragment_protein",
//                    "semi_load_fragments",
//                    "semi_mono_modified_mz_to_fragments",
//                    "semi_average_modified_mz_to_fragments"
//                };
//
//    public static final String[] CREATE_STATEMENTS =
//            {
//                    "CREATE TABLE IF NOT EXISTS `proteins`  (\n" +
//                            "  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,\n" +
//                            "  `annotation` varchar(767) NOT NULL,\n" +
//                            "  `sequence` text NOT NULL,\n" +
//                            "  PRIMARY KEY (`id`),\n" +
//                            "  UNIQUE KEY `annotation_UNIQUE` (`annotation`)\n" +
//                            ")  ",
//                    "CREATE TABLE  IF NOT EXISTS `average_mz_to_fragments` (\n" +
//                            "  `mz` int(11) NOT NULL,\n" +
//                            "  `sequence` varchar(" + XTandemUtilities.MAXIMUM_SEQUENCE_PEPTIDES + ") NOT NULL,\n" +
//                            "  `real_mass` double default NULL,\n" +
//                            "  `missed_cleavages` int(11) DEFAULT NULL,\n" +
//                            "  PRIMARY KEY  (`mz`,`sequence`)\n" +
//                            ")",
//                    "CREATE TABLE IF NOT EXISTS `mono_mz_to_fragments`  (\n" +
//                            "  `mz` int(11) NOT NULL,\n" +
//                            "  `sequence` varchar(" + XTandemUtilities.MAXIMUM_SEQUENCE_PEPTIDES + ") NOT NULL,\n" +
//                            "  `real_mass` double DEFAULT NULL,\n" +
//                            "  `missed_cleavages` int(11) DEFAULT NULL,\n" +
//                            "  PRIMARY KEY  (`mz`,`sequence`)\n" +
//                            ")",
//                    "CREATE TABLE IF NOT EXISTS `fragment_protein` (\n" +
//                            "  `sequence` varchar(255) NOT NULL,\n" +
//                            "  `protein_id` int(11) NOT NULL,\n" +
//                            "  PRIMARY KEY (`sequence`,`protein_id`)\n" +
//                            ")",
//                    "CREATE TABLE  IF NOT EXISTS `load_fragments` (\n" +
//                            "  `id` int(11) NOT NULL,\n" +
//                            "  `sequence` varchar(" + XTandemUtilities.MAXIMUM_SEQUENCE_PEPTIDES + ") NOT NULL,\n" +
//                            "  `protein_id` int(11) NOT NULL,\n" +
//                            "  `start_location` int(11) NOT NULL,\n" +
//                            "  `average_mass` double DEFAULT NULL,\n" +
//                            "  `iaverage_mass` int(11) DEFAULT NULL,\n" +
//                            "  `mono_mass` double DEFAULT NULL,\n" +
//                            "  `imono_mass` int(11) DEFAULT NULL,\n" +
//                            "  `missed_cleavages` int(11) DEFAULT NULL,\n" +
//                            "  PRIMARY KEY (`id`)\n" +
//                            ")",
//                    "CREATE TABLE  IF NOT EXISTS `semi_average_mz_to_fragments` (\n" +
//                            "  `mz` int(11) NOT NULL,\n" +
//                            "  `sequence` varchar(" + XTandemUtilities.MAXIMUM_SEQUENCE_PEPTIDES + ") NOT NULL,\n" +
//                            "  `real_mass` double default NULL,\n" +
//                            "  `missed_cleavages` int(11) DEFAULT NULL,\n" +
//                            "  PRIMARY KEY  (`mz`,`sequence`)\n" +
//                            ")",
//                    "CREATE TABLE IF NOT EXISTS `semi_mono_mz_to_fragments`  (\n" +
//                            "  `mz` int(11) NOT NULL,\n" +
//                            "  `sequence` varchar(" + XTandemUtilities.MAXIMUM_SEQUENCE_PEPTIDES + ") NOT NULL,\n" +
//                            "  `real_mass` double DEFAULT NULL,\n" +
//                            "  `missed_cleavages` int(11) DEFAULT NULL,\n" +
//                            "  PRIMARY KEY  (`mz`,`sequence`)\n" +
//                            ")",
//                    "CREATE TABLE IF NOT EXISTS `semi_fragment_protein` (\n" +
//                            "  `sequence` varchar(255) NOT NULL,\n" +
//                            "  `protein_id` int(11) NOT NULL,\n" +
//                            "  PRIMARY KEY (`sequence`,`protein_id`)\n" +
//                            ")",
//                    "CREATE TABLE  IF NOT EXISTS `semi_load_fragments` (\n" +
//                            "  `id` int(11) NOT NULL,\n" +
//                            "  `sequence` varchar(" + XTandemUtilities.MAXIMUM_SEQUENCE_PEPTIDES + ") NOT NULL,\n" +
//                            "  `protein_id` int(11) NOT NULL,\n" +
//                            "  `start_location` int(11) NOT NULL,\n" +
//                            "  `average_mass` double DEFAULT NULL,\n" +
//                            "  `iaverage_mass` int(11) DEFAULT NULL,\n" +
//                            "  `mono_mass` double DEFAULT NULL,\n" +
//                            "  `imono_mass` int(11) DEFAULT NULL,\n" +
//                            "  `missed_cleavages` int(11) DEFAULT NULL,\n" +
//                            "  PRIMARY KEY (`id`)\n" +
//                            ")",
//                    "CREATE TABLE IF NOT EXISTS `semi_mono_modified_mz_to_fragments`  (\n" +
//                            "  `mz` int(11) NOT NULL,\n" +
//                            "  `modification` varchar(256) NOT NULL,\n" +
//                            "  `sequence` varchar(" + XTandemUtilities.MAXIMUM_SEQUENCE_PEPTIDES + ") NOT NULL,\n" +
//                            "  `modified_sequence` varchar(256) NOT NULL,\n" +
//                            "  `real_mass` double DEFAULT NULL,\n" +
//                            "  `missed_cleavages` int(11) DEFAULT NULL \n" +
//                         //   "  ,PRIMARY KEY  (`mz`,`sequence`,`modification`)\n" +
//                            ")",
//                    "CREATE TABLE IF NOT EXISTS `semi_average_modified_mz_to_fragments`  (\n" +
//                            "  `mz` int(11) NOT NULL,\n" +
//                            "  `modification` varchar(256) NOT NULL,\n" +
//                            "  `sequence` varchar(" + XTandemUtilities.MAXIMUM_SEQUENCE_PEPTIDES + ") NOT NULL,\n" +
//                            "  `modified_sequence` varchar(" + XTandemUtilities.MAXIMUM_SEQUENCE_PEPTIDES + ") NOT NULL,\n" +
//                            "  `real_mass` double DEFAULT NULL,\n" +
//                            "  `missed_cleavages` int(11) DEFAULT NULL,\n" +
//                            "  PRIMARY KEY  (`mz`,`sequence`)\n" +
//                            ")",
//            };
//
//
//    private final SimpleJdbcTemplate m_Template;
//    private final Map<String, String> m_NameToCreateStatement = new HashMap<String, String>();
//
//    public TaxonomyDatabase(final SimpleJdbcTemplate pTemplate) {
//        m_Template = pTemplate;
//        for (int i = 0; i < TABLES.length; i++) {
//            m_NameToCreateStatement.put(TABLES[i], CREATE_STATEMENTS[i]);
//
//        }
//    }
//
//    public SimpleJdbcTemplate getTemplate() {
//        return m_Template;
//    }
////
////    /**
////     * crate a table if it does not exist
////     *
////     * @param tableName name of a known table
////     */
////    public void guaranteeTable(String tableName) {
////        String creator = m_NameToCreateStatement.get(tableName);
////        if (creator == null)
////            throw new IllegalArgumentException("cannot create table " + tableName);
////        SimpleJdbcTemplate template = getTemplate();
////        SpringJDBCUtilities.guaranteeTable(template, tableName, creator);
////    }
////
//
//    /**
//     * drop all data
//     */
//    public void createDatabase() {
//
//        SimpleJdbcTemplate template = getTemplate();
//        //    template.update("DROP SCHEMA IF EXISTS " + SCHEMA_NAME);
//        //     template.update("CREATE SCHEMA  " + SCHEMA_NAME);
//
//        for (int i = 0; i < CREATE_STATEMENTS.length; i++) {
//            String stmt = CREATE_STATEMENTS[i];
//            template.update(stmt);
//
//        }
//    }
//
//
//    /**
//     * drop all data
//     */
//    public void clearDatabase() {
//        SimpleJdbcTemplate template = getTemplate();
//        for (int i = 0; i < TABLES.length; i++) {
//            String table = TABLES[i];
//            template.update("delete from  " + table);
//
//        }
//    }
//
//    /**
//     * drop all tables
//     */
//    public void expungeDatabase() {
//        SimpleJdbcTemplate template = getTemplate();
//        for (int i = 0; i < TABLES.length; i++) {
//            String table = TABLES[i];
//            template.update("drop table " + table);
//
//        }
//    }
//}
