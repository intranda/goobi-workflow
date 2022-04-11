package org.goobi.vocabulary.helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.goobi.vocabulary.Definition;
import org.goobi.vocabulary.Field;
import org.goobi.vocabulary.VocabRecord;
import org.goobi.vocabulary.Vocabulary;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class ImportJsonVocabulary {

    public static List<VocabRecord> convertJsonVocabulary(Vocabulary vocabulary, Path jsonFile) {
        List<VocabRecord> recordList = null;
        InputStream in = null;
        try {
            in = Files.newInputStream(jsonFile);
            Object oldVocabulary = Configuration.defaultConfiguration().jsonProvider().parse(in, "UTF-8");

            // run through all records and create new ones
            List<Object> records = JsonPath.read(oldVocabulary, "records");
            recordList = new ArrayList<>(records.size());

            for (Object record : records) {
                VocabRecord rec = new VocabRecord();
                List<Field> fieldList = new ArrayList<>();
                for (Definition definition : vocabulary.getStruct()) {
                    Field field = new Field(definition.getLabel(), definition.getLanguage(), "", definition);
                    fieldList.add(field);
                }
                rec.setFields(fieldList);
                vocabulary.getRecords().add(rec);
                recordList.add(rec);

                List<Object> fields = JsonPath.read(record, "fields");

                for (Object field : fields) {
                    String label = JsonPath.read(field, "label");
                    String value = JsonPath.read(field, "value");
                    // check if field was defined before
                    boolean found = false;
                    for (Definition definition : vocabulary.getStruct()) {
                        if (definition.getLabel().equals(label)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        // define it
                        Definition definition = new Definition();
                        definition.setLabel(label);
                        definition.setMainEntry(vocabulary.getStruct().isEmpty());
                        definition.setLanguage("");
                        definition.setValidation("");
                        definition.setType("input");
                        vocabulary.getStruct().add(definition);
                        Field f = new Field(definition.getLabel(), definition.getLanguage(), "", definition);
                        fieldList.add(f);
                    }

                    for (Field f : fieldList) {
                        if (f.getLabel().equals(label)) {
                            f.setValue(value);
                        }
                    }

                }

            }

        } catch (IOException e) {
            log.error(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }
        return recordList;
    }

}
