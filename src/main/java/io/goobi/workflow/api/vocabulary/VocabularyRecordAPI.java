package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.FieldValue;
import io.goobi.vocabulary.exchange.TranslationInstance;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyRecordPageResult;
import io.goobi.workflow.api.vocabulary.helper.CachedLookup;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import lombok.extern.log4j.Log4j2;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Log4j2
public class VocabularyRecordAPI {
    private static final String IN_VOCABULARY_RECORDS_ENDPOINT = "/api/v1/vocabularies/{{0}}/records";
    private static final String IN_VOCABULARY_ALL_RECORDS_ENDPOINT = "/api/v1/vocabularies/{{0}}/records/all";
    private static final String METADATA_ENDPOINT = "/api/v1/vocabularies/{{0}}/metadata";
    private static final String INSTANCE_ENDPOINT = "/api/v1/records/{{0}}";

    private final RESTAPI restApi;
    private final CachedLookup<ExtendedVocabularyRecord> singleLookupCache;

    public VocabularyRecordAPI(String host, int port) {
        this.restApi = new RESTAPI(host, port);
        this.singleLookupCache = new CachedLookup<>(id -> new ExtendedVocabularyRecord(restApi.get(INSTANCE_ENDPOINT, VocabularyRecord.class, id)));
    }

    public List<ExtendedVocabularyRecord> all(long vocabularyId) {
        return all(vocabularyId, Optional.empty());
    }

    public List<ExtendedVocabularyRecord> all(long vocabularyId, Optional<String> sorting) {
         List<Object> urlParameters = new ArrayList<>(1 + (sorting.isPresent() ? 1 : 0));
        urlParameters.add(vocabularyId);
        sorting.ifPresent(s -> urlParameters.add("sort=" + s));
        return restApi.get(IN_VOCABULARY_ALL_RECORDS_ENDPOINT, VocabularyRecordPageResult.class, urlParameters.toArray()).getContent().stream()
                .map(ExtendedVocabularyRecord::new)
                .collect(Collectors.toList());
    }

    public VocabularyRecordPageResult list(long vocabularyId) {
        return list(vocabularyId, Optional.empty(), Optional.empty());
    }

    public VocabularyRecordPageResult list(long vocabularyId, Optional<Integer> size, Optional<Integer> page) {
        return list(vocabularyId, size, page, Optional.empty());
    }

    public VocabularyRecordPageResult list(long vocabularyId, Optional<Integer> size, Optional<Integer> page, Optional<String> sorting) {
        String params = "";
        if (size.isPresent()) {
            params += params.isEmpty() ? "?" : "&";
            params += "size=" + size.get();
        }
        if (page.isPresent()) {
            params += params.isEmpty() ? "?" : "&";
            params += "page=" + page.get();
        }
        if (sorting.isPresent()) {
            params += params.isEmpty() ? "?" : "&";
            params += "sort=" + sorting.get();
        }
        return restApi.get(IN_VOCABULARY_RECORDS_ENDPOINT + params, VocabularyRecordPageResult.class, vocabularyId);
    }

    public ExtendedVocabularyRecord get(long id) {
        return singleLookupCache.getCached(id);
    }

    public ExtendedVocabularyRecord get(String url) {
        return new ExtendedVocabularyRecord(restApi.get(url, VocabularyRecord.class));
    }

    public VocabularyRecordPageResult search(long vocabularyId, String query) {
        return search(vocabularyId, query, Optional.empty());
    }

    public VocabularyRecordPageResult search(long vocabularyId, String query, Optional<String> sorting) {
        List<Object> urlParameters = new ArrayList<>(2 + (sorting.isPresent() ? 1 : 0));
        urlParameters.add(vocabularyId);
        urlParameters.add("search=" + query);
        sorting.ifPresent(s -> urlParameters.add("sort=" + s));
        return restApi.get(IN_VOCABULARY_RECORDS_ENDPOINT, VocabularyRecordPageResult.class, urlParameters.toArray());
    }

    public VocabularyRecord save(VocabularyRecord vocabularyRecord) {
        cleanUpRecord(vocabularyRecord);
        if (Boolean.TRUE.equals(vocabularyRecord.getMetadata())) {
            return changeMetadata(vocabularyRecord);
        }
        if (vocabularyRecord.getId() != null) {
            return change(vocabularyRecord);
        } else {
            return create(vocabularyRecord);
        }
    }

    private VocabularyRecord create(VocabularyRecord vocabularyRecord) {
        long vocabularyId = vocabularyRecord.getVocabularyId();
        vocabularyRecord.setVocabularyId(null);
        VocabularyRecord newRecord;
        if (vocabularyRecord.getParentId() == null) {
            newRecord = restApi.post(IN_VOCABULARY_RECORDS_ENDPOINT, VocabularyRecord.class, vocabularyRecord, vocabularyId);

        } else {
            long parentId = vocabularyRecord.getParentId();
            vocabularyRecord.setParentId(null);
            newRecord = restApi.post(INSTANCE_ENDPOINT, VocabularyRecord.class, vocabularyRecord, parentId);
            vocabularyRecord.setParentId(parentId);
        }
        vocabularyRecord.setId(vocabularyId);
        this.singleLookupCache.invalidate(vocabularyRecord.getId());
        return newRecord;
    }

    private VocabularyRecord change(VocabularyRecord vocabularyRecord) {
        long id = vocabularyRecord.getId();
        vocabularyRecord.setId(null);
        VocabularyRecord newRecord = restApi.put(INSTANCE_ENDPOINT, VocabularyRecord.class, vocabularyRecord, id);
        vocabularyRecord.setId(id);
        this.singleLookupCache.invalidate(vocabularyRecord.getId());
        return newRecord;
    }

    public void delete(VocabularyRecord vocabularyRecord) {
        restApi.delete(INSTANCE_ENDPOINT, VocabularyRecord.class, vocabularyRecord.getId());
        this.singleLookupCache.invalidate(vocabularyRecord.getId());
        if (vocabularyRecord.getParentId() != null) {
            this.singleLookupCache.invalidate(vocabularyRecord.getParentId());
        }
    }

    public ExtendedVocabularyRecord getMetadata(Long id) {
        // TODO: Make this more elegant
        try {
            return new ExtendedVocabularyRecord(restApi.get(METADATA_ENDPOINT, VocabularyRecord.class, id));
        } catch (APIException e) {
            return null;
        }
    }

    private VocabularyRecord changeMetadata(VocabularyRecord metadataRecord) {
        if (!Boolean.TRUE.equals(metadataRecord.getMetadata())) {
            throw new IllegalArgumentException("Cannot perform changeMetadata on normal record");
        }
        long id = metadataRecord.getId();
        long vocabularyId = metadataRecord.getVocabularyId();
        metadataRecord.setId(null);
        metadataRecord.setVocabularyId(null);
        metadataRecord.setMetadata(null);
        VocabularyRecord newRecord = restApi.put(METADATA_ENDPOINT, VocabularyRecord.class, metadataRecord, vocabularyId);
        metadataRecord.setId(id);
        metadataRecord.setVocabularyId(vocabularyId);
        metadataRecord.setMetadata(true);
        return newRecord;
    }

    private void cleanUpRecord(VocabularyRecord currentRecord) {
        for (FieldInstance field : currentRecord.getFields()) {
            for (FieldValue value : field.getValues()) {
                value.getTranslations().removeIf(this::translationIsEmpty);
            }
            field.getValues().removeIf(this::valueIsEmpty);
        }
        currentRecord.getFields().removeIf(this::fieldIsEmpty);
    }

    private boolean translationIsEmpty(TranslationInstance translationInstance) {
        return translationInstance.getValue().isEmpty();
    }

    private boolean valueIsEmpty(FieldValue fieldValue) {
        return fieldValue.getTranslations().isEmpty();
    }

    private boolean fieldIsEmpty(FieldInstance fieldInstance) {
        return fieldInstance.getValues().isEmpty();
    }

}
