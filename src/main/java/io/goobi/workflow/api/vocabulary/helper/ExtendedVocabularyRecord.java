package io.goobi.workflow.api.vocabulary.helper;

import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.FieldValue;
import io.goobi.vocabulary.exchange.TranslationInstance;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import lombok.Getter;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class ExtendedVocabularyRecord extends VocabularyRecord {
    private Function<Long, VocabularyRecord> recordResolver = VocabularyAPIManager.getInstance().vocabularyRecords()::get;

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
    }

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
                .findAny().orElseThrow(() -> new RuntimeException("Record has no main value defined"));
    }

    // TODO: Perform logic here
    private void prepareEmptyFieldsForEditing(VocabularyRecord record) {
//        List<Long> existingFields = record.getFields().stream()
//                .map(FieldInstance::getDefinitionId)
//                .collect(Collectors.toList());
//        List<Long> missingFields = definitionsIdMap.keySet()
//                .stream().filter(i -> !existingFields.contains(i))
//                .collect(Collectors.toList());
//        missingFields.forEach(d -> {
//            FieldInstance field = new FieldInstance();
//            field.setRecordId(record.getId());
//            field.setDefinitionId(d);
//            record.getFields().add(field);
//        });
//        record.getFields().forEach(f -> {
//            if (f.getValues().isEmpty()) {
//                f.getValues().add(new FieldValue());
//            }
//            FieldDefinition definition = definitionsIdMap.get(f.getDefinitionId());
//            f.getValues().forEach(v -> {
//                if (!definition.getTranslationDefinitions().isEmpty()) {
//                    definition.getTranslationDefinitions().stream()
//                            .filter(t -> v.getTranslations().stream().noneMatch(t2 -> t2.getLanguage().equals(t.getLanguage())))
//                            .forEach(t -> {
//                                TranslationInstance translation = new TranslationInstance();
//                                translation.setLanguage(t.getLanguage());
//                                translation.setValue("");
//                                v.getTranslations().add(translation);
//                            });
//                } else if (v.getTranslations().isEmpty()) {
//                    TranslationInstance translation = new TranslationInstance();
//                    translation.setValue("");
//                    v.getTranslations().add(translation);
//                }
//            });
//        });
    }

    private int calculateLevel(VocabularyRecord record) {
        if (record.getParentId() == null) {
            return 0;
        }
        return calculateLevel(recordResolver.apply(record.getParentId())) + 1;
    }
}
