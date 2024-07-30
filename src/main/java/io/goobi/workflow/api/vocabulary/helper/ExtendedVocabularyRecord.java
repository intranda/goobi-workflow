package io.goobi.workflow.api.vocabulary.helper;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.goobi.vocabulary.exchange.FieldDefinition;
import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.VocabularyAPIManager;
import lombok.Getter;
import ugh.dl.Metadata;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class ExtendedVocabularyRecord extends VocabularyRecord {
    private Function<Long, ExtendedVocabulary> vocabularyResolver = VocabularyAPIManager.getInstance().vocabularies()::get;
    private Function<Long, ExtendedVocabularyRecord> recordResolver = VocabularyAPIManager.getInstance().vocabularyRecords()::get;
    private Function<VocabularyRecord, VocabularySchema> schemaResolver = VocabularyAPIManager.getInstance().vocabularySchemas()::getSchema;
    private Function<VocabularyRecord, Optional<VocabularySchema>> metadataSchemaResolver = VocabularyAPIManager.getInstance().vocabularySchemas()::getMetadataSchema;

    private int level;
    private String mainValue;
    private List<String> titleValues;
    private List<ExtendedFieldInstance> extendedFields;
    private List<ExtendedVocabularyRecord> parents;

    public ExtendedVocabularyRecord(VocabularyRecord orig) {
        // TODO: Make generic solution for this
        setId(orig.getId());
        setParentId(orig.getParentId());
        setVocabularyId(orig.getVocabularyId());
        setMetadata(orig.getMetadata());
        setFields(orig.getFields());
        setChildren(orig.getChildren());
        set_links(orig.get_links());

        postInit();
        prepareEmpty();
    }

    // TODO: Think about recreation / updating these values in case the record changes..
    private void postInit() {
        initParents();
        this.level = this.parents.size();
        this.extendedFields = getFields().stream()
                .sorted(Comparator.comparingLong(FieldInstance::getDefinitionId))
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

    @JsonIgnore
    public String getURI() {
        return get_links().get("self").getHref();
    }

    public Optional<String> getFieldValueForDefinition(FieldDefinition definition) {
        return getExtendedFields().stream()
                .filter(f -> f.getDefinitionId().equals(definition.getId()))
                .findFirst()
                .map(ExtendedFieldInstance::getFieldValue);
    }

    public Optional<String> getFieldValueForDefinition(FieldDefinition definition, String language) {
        return getExtendedFields().stream()
                .filter(f -> f.getDefinitionId().equals(definition.getId()))
                .findFirst()
                .map(f -> f.getFieldValue(language));
    }

    public Optional<String> getFieldValueForDefinitionName(String definitionName) {
        return getExtendedFields().stream()
                .filter(f -> f.getDefinition().getName().equals(definitionName))
                .findFirst()
                .map(ExtendedFieldInstance::getFieldValue);
    }

    public Optional<String> getFieldValueForDefinitionName(String definitionName, String language) {
        return getExtendedFields().stream()
                .filter(f -> f.getDefinition().getName().equals(definitionName))
                .findFirst()
                .map(f -> f.getFieldValue(language));
    }

    public void writeReferenceMetadata(Metadata meta) {
        ExtendedVocabulary vocabulary = vocabularyResolver.apply(getVocabularyId());
        meta.setValue(getMainValue());
        meta.setAuthorityFile(vocabulary.getName(), vocabulary.get_links().get("self").getHref(), get_links().get("self").getHref());
    }

    private void initParents() {
        if (this.getParentId() != null) {
            ExtendedVocabularyRecord parent = recordResolver.apply(this.getParentId());
            this.parents = new LinkedList<>(parent.getParents());
            this.parents.add(parent);
        } else {
            this.parents = Collections.emptyList();
        }
    }

    private void prepareEmpty() {
        List<Long> existingFields = getFields().stream()
                .map(FieldInstance::getDefinitionId)
                .collect(Collectors.toList());
        Optional<VocabularySchema> schema = Boolean.TRUE.equals(this.getMetadata()) ? metadataSchemaResolver.apply(this) : Optional.of(schemaResolver.apply(this));
        List<Long> missingFields = schema.map(s -> s.getDefinitions().stream()
                .map(FieldDefinition::getId)
                .filter(i -> !existingFields.contains(i))
                .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
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
}
