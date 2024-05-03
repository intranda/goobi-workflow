/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * <p>
 * Visit the websites for more information.
 * - https://goobi.io
 * - https://www.intranda.com
 * - https://github.com/intranda/goobi-workflow
 * <p>
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * <p>
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.goobi.managedbeans;

import de.sub.goobi.helper.Helper;
import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.FieldType;
import io.goobi.vocabulary.exchange.FieldValue;
import io.goobi.vocabulary.exchange.TranslationDefinition;
import io.goobi.vocabulary.exchange.TranslationInstance;
import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.APIException;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import io.goobi.workflow.api.vocabulary.hateoas.HATEOASPaginator;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyRecordPageResult;
import io.goobi.workflow.api.vocabulary.jsfwrapper.JSFVocabularyRecord;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.apache.deltaspike.core.api.scope.WindowScoped;

import javax.inject.Named;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Named
@WindowScoped
@Log4j2
public class VocabularyRecordsBean implements Serializable {
    private static final long serialVersionUID = 5672948572345L;

    private static final String RETURN_PAGE_OVERVIEW = "vocabulary_records";

    private static final VocabularyAPIManager api = VocabularyAPIManager.getInstance();

    @Getter
    private transient Paginator<JSFVocabularyRecord> paginator;

    @Getter
    private transient Vocabulary vocabulary;

    private transient VocabularySchema schema;

    @Getter
    private transient JSFVocabularyRecord currentRecord;

    @Getter
    private transient List<FieldDefinition> mainFields;
    @Getter
    private transient List<FieldDefinition> titleFields;

    @Getter
    private transient List<FieldDefinition> definitions;
    private transient Map<Long, FieldDefinition> definitionsIdMap;
    private transient Map<Long, FieldType> typeIdMap;
    private final String language = transformToThreeCharacterAbbreviation(Helper.getSessionLocale().getLanguage());

    public String load(Vocabulary vocabulary) {
        this.vocabulary = vocabulary;

        loadPaginator();
        loadSchema();
        loadFirstRecord();

        return RETURN_PAGE_OVERVIEW;
    }

    public void edit(JSFVocabularyRecord record) {
        this.currentRecord = record;
        prepareEmptyFieldsForEditing(record);
    }

    public void createEmpty() {
        JSFVocabularyRecord record = new JSFVocabularyRecord();
        record.setVocabularyId(vocabulary.getId());
        record.setFields(new HashSet<>());
        loadRecord(record);
        this.currentRecord = record;
        prepareEmptyFieldsForEditing(record);
    }

    public void deleteRecord() {
        try {
            api.vocabularyRecords().delete(currentRecord);
            paginator.reload();
            loadFirstRecord();
        } catch (APIException e) {
            Helper.setFehlerMeldung(e);
        }
    }

    public void saveRecord() {
        // TODO: Remove this verbose test
        System.err.println(currentRecord);
        // TODO: Maybe replace current record
        try {
            VocabularyRecord newRecord;
            if (currentRecord.getId() != null) {
                newRecord = api.vocabularyRecords().change(currentRecord);
            } else {
                newRecord = api.vocabularyRecords().create(currentRecord);
            }
            paginator.reload();
            paginator.getItems().stream()
                    .filter(r -> Objects.equals(r.getId(), newRecord.getId()))
                    .findFirst().ifPresent(r -> currentRecord = r);
        } catch (APIException e) {
            Helper.setFehlerMeldung(e);
        }
    }

    public FieldDefinition getDefinition(FieldInstance field) {
        return definitionsIdMap.get(field.getDefinitionId());
    }

    public FieldType getType(FieldInstance field) {
        return typeIdMap.get(definitionsIdMap.get(field.getDefinitionId()).getTypeId());
    }

//    public String getValue(FieldInstance field) {
//        return field.getValues().stream()
//                .flatMap(v -> v.getTranslations().values().stream())
//                .collect(Collectors.joining(" :: "));
//    }
//
//    public String getValue(FieldInstance field, String language) {
//        return field.getValues().stream()
//                .flatMap(v -> v.getTranslations().entrySet().stream()
//                        .filter(t -> t.getKey().equals(language))
//                        .map(Map.Entry::getValue)
//                )
//                .collect(Collectors.joining("|"));
//    }

    public List<String> getLanguages(FieldDefinition definition) {
        return definition.getTranslationDefinitions().stream()
                .map(TranslationDefinition::getLanguage)
                .collect(Collectors.toList());
    }

    private void loadPaginator() {
        // TODO: Unclean to have static Helper access to user here..
        this.paginator = new HATEOASPaginator<>(
                VocabularyRecordPageResult.class,
                api.vocabularyRecords().list(
                        this.vocabulary.getId(),
                        Optional.of(Helper.getLoginBean().getMyBenutzer().getTabellengroesse()),
                        Optional.empty()
                ),
                this::loadRecord
        );
    }

    private void loadRecord(JSFVocabularyRecord record) {
        record.load(schema);
        record.setLanguage(language);
    }

    private void prepareEmptyFieldsForEditing(JSFVocabularyRecord record) {
        List<Long> existingFields = record.getFields().stream()
                .map(FieldInstance::getDefinitionId)
                .collect(Collectors.toList());
        List<Long> missingFields = definitionsIdMap.keySet()
                .stream().filter(i -> !existingFields.contains(i))
                .collect(Collectors.toList());
        missingFields.forEach(d -> {
            FieldInstance field = new FieldInstance();
            FieldValue value = new FieldValue();
            FieldDefinition fieldDefinition = definitionsIdMap.get(d);
            if (!fieldDefinition.getTranslationDefinitions().isEmpty()) {
                fieldDefinition.getTranslationDefinitions().forEach(t -> {
                    TranslationInstance translation = new TranslationInstance();
                    translation.setLanguage(t.getLanguage());
                    translation.setValue("");
                    value.getTranslations().add(translation);
                });
            } else {
                TranslationInstance translation = new TranslationInstance();
                translation.setValue("");
                value.getTranslations().add(translation);
            }
            field.setDefinitionId(d);
            field.getValues().add(value);
            record.getFields().add(field);
        });
    }

    private void loadSchema() {
        this.schema = api.vocabularySchemas().get(this.vocabulary.getSchemaId());
        loadFieldDefinitions();
        loadTypes();
    }

    private void loadFieldDefinitions() {
        this.definitions = this.schema.getDefinitions();
        this.mainFields = this.definitions.stream()
                .filter(d -> Boolean.TRUE.equals(d.getMainEntry()))
                .collect(Collectors.toList());
        this.titleFields = this.definitions.stream()
                .filter(d -> Boolean.TRUE.equals(d.getTitleField()))
                .collect(Collectors.toList());
        this.definitionsIdMap = new HashMap<>();
        for (FieldDefinition d : this.definitions) {
            this.definitionsIdMap.put(d.getId(), d);
        }
    }


    private void loadTypes() {
        this.typeIdMap = new HashMap<>();
        this.definitions.forEach(d -> this.typeIdMap.put(d.getTypeId(), api.fieldTypes().get(d.getTypeId())));
    }

    private String transformToThreeCharacterAbbreviation(String language) {
        switch (language) {
            case "en":
                return "eng";
            case "de":
                return "ger";
            case "fr":
                return "fre";
            default:
                throw new IllegalArgumentException("Unknown language: \"" + language + "\"");
        }
    }

    private void loadFirstRecord() {
        // TODO: Fix if empty
        this.currentRecord = this.paginator.getItems().get(0);
    }
}
