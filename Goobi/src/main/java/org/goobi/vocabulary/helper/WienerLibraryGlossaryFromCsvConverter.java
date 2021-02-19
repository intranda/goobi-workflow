//package org.goobi.vocabulary.helper;
//
//import java.io.File;
//import java.io.FileReader;
//import java.io.Reader;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.commons.csv.CSVFormat;
//import org.apache.commons.csv.CSVRecord;
//import org.goobi.vocabulary.Field;
//import org.goobi.vocabulary.VocabRecord;
//import org.goobi.vocabulary.VocabularyManagerOld;
//
//import lombok.extern.log4j.Log4j;
//
//@Log4j
//public class WienerLibraryGlossaryFromCsvConverter {
//    public static void main(String[] args) {
//        try {
//            // initialise the vocabulary
//
//            VocabularyManagerOld vm = new VocabularyManagerOld();
//            vm.loadVocabulary("Wiener Library Glossary");
//
//            Reader in = new FileReader(new File("/opt/digiverso/goobi/import/WienerLibrary.Glossary.Export.TabSepearated.csv"));
//            Iterable<CSVRecord> rows = CSVFormat.newFormat('\t').withFirstRecordAsHeader().parse(in);
//            for (CSVRecord csv : rows) {
//                // read csv information
//                String term = csv.get(0);
//                String description = csv.get(1);
//                String source = csv.get(2);
//
//                // create a new vocabulary record
//                List <Field> fields = new ArrayList<>();
//                fields.add(new Field("Title", term, null));
//                fields.add(new Field("Keywords", term, null));
//                fields.add(new Field("Description", description, null));
//                fields.add(new Field("Source", source, null));
//                vm.getVocabulary().getRecords().add(new VocabRecord(null, fields));
//            }
//            // save vocabulary at the end
//            vm.saveVocabulary();
//        } catch (Exception e) {
//            log.error("Exception while reading csv file: " + e.getMessage());
//        }
//    }
//}
