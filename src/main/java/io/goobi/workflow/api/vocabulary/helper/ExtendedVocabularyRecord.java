package io.goobi.workflow.api.vocabulary.helper;

import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class ExtendedVocabularyRecord extends VocabularyRecord {
    private Function<Long, VocabularyRecord> recordResolver = VocabularyAPIManager.getInstance().vocabularyRecords()::get;
    private Function<VocabularyRecord, VocabularySchema> schemaResolver = VocabularyAPIManager.getInstance().vocabularySchemas()::get;

    private int level;
    private String mainValue;
    private List<String> titleValues;
    private List<ExtendedFieldInstance> extendedFields;

    public ExtendedVocabularyRecord(VocabularyRecord orig) {
        // TODO: Make generic solution for this
        setId(orig.getId());
        setParentId(orig.getParentId());
        setVocabularyId(orig.getVocabularyId());
        setMetadata(orig.getMetadata());
        setFields(orig.getFields());
        setChildren(orig.getChildren());

        postInit();
        prepareEmpty();
    }

    // TODO: Think about recreation / updating these values in case the record changes..
    private void postInit() {
        this.level = calculateLevel(this);
        this.extendedFields = getFields().stream()
                .map(ExtendedFieldInstance::new)
                .collect(Collectors.toList());
        this.titleValues = this.extendedFields.stream()
                .sorted(Comparator.comparingLong(FieldInstance::getDefinitionId))
                .filter(f -> Boolean.TRUE.equals(f.getDefinition().getTitleField()))
                .map(ExtendedFieldInstance::getFieldValue)
                .collect(Collectors.toList());
        this.mainValue = this.extendedFields.stream()
                .filter(f -> Boolean.TRUE.equals(f.getDefinition().getMainEntry()))
                .map(ExtendedFieldInstance::getFieldValue)
                .findAny()
                .orElse("");
        prepareEmpty();
    }

    private void prepareEmpty() {
        List<Long> existingFields = getFields().stream()
                .map(FieldInstance::getDefinitionId)
                .collect(Collectors.toList());
        List<Long> missingFields = schemaResolver.apply(this).getDefinitions().stream()
                .map(FieldDefinition::getId)
                .filter(i -> !existingFields.contains(i))
                .collect(Collectors.toList());
        missingFields.forEach(d -> {
            FieldInstance field = new FieldInstance();
            field.setRecordId(getId());
            field.setDefinitionId(d);
            getFields().add(field);
        });
        if (!missingFields.isEmpty()) {
            postInit();
        }
    }

    private int calculateLevel(VocabularyRecord record) {
        if (record.getParentId() == null) {
            return 0;
        }
        return calculateLevel(recordResolver.apply(record.getParentId())) + 1;
    }
}
