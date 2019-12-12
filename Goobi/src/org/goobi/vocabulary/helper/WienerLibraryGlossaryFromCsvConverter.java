package org.goobi.vocabulary.helper;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.tree.xpath.XPathExpressionEngine;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import org.goobi.vocabulary.Field;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.VocabularyManager;

public class WienerLibraryGlossaryFromCsvConverter {
    public static void main(String[] args) {
        try {
            // initialise the vocabulary
            String configfile = "plugin_intranda_administration_vocabulary.xml";
            XMLConfiguration config = new XMLConfiguration("/opt/digiverso/goobi/config/" + configfile);
            config.setListDelimiter('&');
            config.setExpressionEngine(new XPathExpressionEngine());
            VocabularyManager vm = new VocabularyManager(config);
            vm.loadVocabulary("Wiener Library Glossary");
           
            Reader in = new FileReader(new File("/opt/digiverso/goobi/import/WienerLibrary.Glossary.Export.TabSepearated.csv"));
            Iterable<CSVRecord> rows = CSVFormat.newFormat('\t').withFirstRecordAsHeader().parse(in);
            for (CSVRecord csv : rows) {    
                // read csv information
                String term = csv.get(0);
                String description = csv.get(1);
                String source = csv.get(2);
                System.out.println(term);
                System.out.println(description);
                System.out.println(source);
                // create a new vocabulary record
                List <Field> fields = new ArrayList<>();
                fields.add(new Field("Title", term, null));
                fields.add(new Field("Keywords", term, null));
                fields.add(new Field("Description", description, null));
                fields.add(new Field("Source", source, null));
                vm.getVocabulary().getRecords().add(new VocabRecord("", fields));
                System.out.println("-----------------------------------------------------------");
            }
            // save vocabulary at the end
            vm.saveVocabulary();
        } catch (Exception e) {
            System.err.println("Exception while reading csv file: " + e.getMessage());
        }
    }
}
