/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - https://goobi.io
 *          - https://www.intranda.com
 *          - https://github.com/intranda/goobi-workflow
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
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

            for (Object recordObject : records) {
                VocabRecord rec = new VocabRecord();
                List<Field> fieldList = new ArrayList<>();
                for (Definition definition : vocabulary.getStruct()) {
                    Field field = new Field(definition.getLabel(), definition.getLanguage(), "", definition);
                    fieldList.add(field);
                }
                rec.setFields(fieldList);
                vocabulary.getRecords().add(rec);
                recordList.add(rec);

                List<Object> fields = JsonPath.read(recordObject, "fields");

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
