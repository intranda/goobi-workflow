package io.goobi.workflow.api.vocabulary;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import javax.faces.model.SelectItem;

import io.goobi.vocabulary.exception.VocabularyException;
import io.goobi.vocabulary.exchange.FieldInstance;
import io.goobi.vocabulary.exchange.FieldValue;
import io.goobi.vocabulary.exchange.TranslationInstance;
import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyRecordPageResult;
import io.goobi.workflow.api.vocabulary.helper.CachedLookup;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabulary;
import io.goobi.workflow.api.vocabulary.helper.ExtendedVocabularyRecord;
import io.goobi.workflow.api.vocabulary.helper.RecordListRequest;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class VocabularyRecordAPI {
    private static final String IN_VOCABULARY_RECORDS_ENDPOINT = "/api/v1/vocabularies/{{0}}/records";
    private static final String METADATA_ENDPOINT = "/api/v1/vocabularies/{{0}}/metadata";
    private static final String INSTANCE_ENDPOINT = "/api/v1/records/{{0}}";

    private final RESTAPI restApi;
    private final CachedLookup<Long, ExtendedVocabularyRecord> singleLookupCache;
    private final CachedLookup<RecordListRequest, VocabularyRecordPageResult> listLookupCache;

    public class VocabularyRecordQueryBuilder {
        private final long vocabularyId;
        private Optional<Integer> page = Optional.empty();
        private Optional<Integer> pageSize = Optional.empty();
        private Optional<String> sorting = Optional.empty();
        private Optional<String> search = Optional.empty();
        private Optional<Boolean> all = Optional.empty();

        private VocabularyRecordQueryBuilder(long vocabularyId) {
            this.vocabularyId = vocabularyId;
        }

        public VocabularyRecordQueryBuilder page(int page) {
            this.page = Optional.of(page);
            return this;
        }

        public VocabularyRecordQueryBuilder page(Optional<Integer> page) {
            this.page = page;
            return this;
        }

        public VocabularyRecordQueryBuilder pageSize(int pageSize) {
            this.pageSize = Optional.of(pageSize);
            return this;
        }

        public VocabularyRecordQueryBuilder pageSize(Optional<Integer> pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public VocabularyRecordQueryBuilder sorting(String sorting) {
            this.sorting = Optional.of(sorting);
            return this;
        }

        public VocabularyRecordQueryBuilder sorting(Optional<String> sorting) {
            this.sorting = sorting;
            return this;
        }

        public VocabularyRecordQueryBuilder search(String search) {
            this.search = Optional.of(search);
            return this;
        }

        public VocabularyRecordQueryBuilder search(Optional<String> search) {
            this.search = search;
            return this;
        }

        public VocabularyRecordQueryBuilder all() {
            this.all = Optional.of(true);
            return this;
        }

        public VocabularyRecordPageResult request() {
            return list(this.vocabularyId, this.pageSize, this.page, this.sorting, this.search, this.all);
        }
    }

    public VocabularyRecordAPI(String address) {
        this.restApi = new RESTAPI(address);
        this.singleLookupCache = new CachedLookup<>(id -> new ExtendedVocabularyRecord(restApi.get(INSTANCE_ENDPOINT, VocabularyRecord.class, id)));
        this.listLookupCache = new CachedLookup<>(req -> {
            VocabularyRecordPageResult result = restApi.get(IN_VOCABULARY_RECORDS_ENDPOINT + req.getUrlParams(), VocabularyRecordPageResult.class, req.getVocabularyId());
            result.getContent().forEach(r -> this.singleLookupCache.update(r.getId(), r));
            return result;
        });
    }

    public VocabularyRecordQueryBuilder list(long vocabularyId) {
        return new VocabularyRecordQueryBuilder(vocabularyId);
    }

    private VocabularyRecordPageResult list(long vocabularyId, Optional<Integer> size, Optional<Integer> page, Optional<String> sorting,
            Optional<String> search, Optional<Boolean> all) {
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
        if (search.isPresent()) {
            params += params.isEmpty() ? "?" : "&";
            params += "search=" + URLEncoder.encode(search.get(), StandardCharsets.UTF_8);
        }
        if (all.isPresent() && Boolean.TRUE.equals(all.get())) {
            params += params.isEmpty() ? "?" : "&";
            params += "all=1";
        }

        return this.listLookupCache.getCached(new RecordListRequest(params, vocabularyId));
    }

    public ExtendedVocabularyRecord get(long id) {
        return singleLookupCache.getCached(id);
    }

    public ExtendedVocabularyRecord get(String url) {
        return new ExtendedVocabularyRecord(restApi.get(url, VocabularyRecord.class));
    }

    VocabularyRecord getPrimitive(long id) {
        return restApi.get(INSTANCE_ENDPOINT, VocabularyRecord.class, id);
    }

    public ExtendedVocabularyRecord save(VocabularyRecord vocabularyRecord) {
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

    private ExtendedVocabularyRecord create(VocabularyRecord vocabularyRecord) {
        long vocabularyId = vocabularyRecord.getVocabularyId();
        Long parentId = vocabularyRecord.getParentId();
        ExtendedVocabularyRecord newRecord;
        try {
            vocabularyRecord.setVocabularyId(null);
            vocabularyRecord.setParentId(null);
            if (parentId == null) {
                newRecord = new ExtendedVocabularyRecord(
                        restApi.post(IN_VOCABULARY_RECORDS_ENDPOINT, VocabularyRecord.class, vocabularyRecord, vocabularyId));
            } else {
                newRecord = new ExtendedVocabularyRecord(restApi.post(INSTANCE_ENDPOINT, VocabularyRecord.class, vocabularyRecord, parentId));
                this.singleLookupCache.invalidate(parentId);
            }
            this.singleLookupCache.update(newRecord.getId(), newRecord);
        } finally {
            vocabularyRecord.setVocabularyId(vocabularyId);
            vocabularyRecord.setParentId(parentId);
        }
        return newRecord;
    }

    private ExtendedVocabularyRecord change(VocabularyRecord vocabularyRecord) {
        long id = vocabularyRecord.getId();
        ExtendedVocabularyRecord newRecord;
        try {
            vocabularyRecord.setId(null);
            newRecord = new ExtendedVocabularyRecord(restApi.put(INSTANCE_ENDPOINT, VocabularyRecord.class, vocabularyRecord, id));
            this.singleLookupCache.update(newRecord.getId(), newRecord);
        } finally {
            vocabularyRecord.setId(id);
        }
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
            // Throw all exceptions that are not missing metadata, ignore missing metadata and just continue with empty record creation
            if (e.getVocabularyCause() == null || e.getVocabularyCause().getErrorType() != VocabularyException.ErrorCode.EntityNotFound) {
                throw e;
            }
        }
        ExtendedVocabulary vocabulary = VocabularyAPIManager.getInstance().vocabularies().get(id);
        return createEmptyRecord(vocabulary.getId(), null, true);
    }

    private ExtendedVocabularyRecord changeMetadata(VocabularyRecord metadataRecord) {
        if (!Boolean.TRUE.equals(metadataRecord.getMetadata())) {
            throw new IllegalArgumentException("Cannot perform changeMetadata on normal record");
        }
        Long id = metadataRecord.getId();
        long vocabularyId = metadataRecord.getVocabularyId();
        metadataRecord.setId(null);
        metadataRecord.setVocabularyId(null);
        metadataRecord.setMetadata(null);
        ExtendedVocabularyRecord newRecord;
        if (id == null) {
            newRecord = new ExtendedVocabularyRecord(restApi.post(METADATA_ENDPOINT, VocabularyRecord.class, metadataRecord, vocabularyId));
        } else {
            newRecord = new ExtendedVocabularyRecord(restApi.put(METADATA_ENDPOINT, VocabularyRecord.class, metadataRecord, vocabularyId));
        }
        metadataRecord.setId(id);
        metadataRecord.setVocabularyId(vocabularyId);
        metadataRecord.setMetadata(true);
        return newRecord;
    }

    public ExtendedVocabularyRecord createEmptyRecord(long vocabularyId, Long parentId, boolean metadata) {
        VocabularyRecord record = new VocabularyRecord();
        record.setVocabularyId(vocabularyId);
        record.setParentId(parentId);
        record.setMetadata(metadata);
        record.setFields(new HashSet<>());
        return new ExtendedVocabularyRecord(record);
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
        return translationInstance.getValue().isEmpty() ||
                translationInstance.getLanguage() == null && "null".equals(translationInstance.getValue());
    }

    private boolean valueIsEmpty(FieldValue fieldValue) {
        return fieldValue.getTranslations().isEmpty();
    }

    private boolean fieldIsEmpty(FieldInstance fieldInstance) {
        return fieldInstance.getValues().isEmpty();
    }

    public List<String> getRecordMainValues(long vocabularyId) {
        return getRecordMainValues(list(vocabularyId));
    }

    public List<String> getRecordMainValues(VocabularyRecordQueryBuilder query) {
        return query
                .all()
                .request()
                .getContent()
                .stream()
                .map(ExtendedVocabularyRecord::getMainValue)
                .sorted()
                .collect(Collectors.toList());
    }

    public List<SelectItem> getRecordSelectItems(long vocabularyId) {
        return getRecordSelectItems(list(vocabularyId));
    }

    public List<SelectItem> getRecordSelectItems(VocabularyRecordQueryBuilder query) {
        return getAllHierarchicalRecords(query)
                .stream()
                .map(r -> new SelectItem(String.valueOf(r.getId()), r.getSelectItemLabel()))
                .toList();
    }

    public List<ExtendedVocabularyRecord> getAllHierarchicalRecords(long vocabularyId) {
        return getAllHierarchicalRecords(list(vocabularyId));
    }

    public List<ExtendedVocabularyRecord> getAllHierarchicalRecords(VocabularyRecordQueryBuilder query) {
        List<ExtendedVocabularyRecord> topRecords = query
                .all()
                .request()
                .getContent();
        topRecords.sort(Comparator.comparing(ExtendedVocabularyRecord::getMainValue));
        return fullyLoadChildren(topRecords);
    }

    public List<ExtendedVocabularyRecord> fullyLoadChildren(List<ExtendedVocabularyRecord> records) {
        List<ExtendedVocabularyRecord> allRecords = new LinkedList<>();
        recursivelyPopulateRecordList(allRecords, records);
        return allRecords;
    }

    private void recursivelyPopulateRecordList(List<ExtendedVocabularyRecord> list, Collection<ExtendedVocabularyRecord> records) {
        for (ExtendedVocabularyRecord r : records) {
            list.add(r);

            List<ExtendedVocabularyRecord> children = Optional.ofNullable(r.getChildren())
                    .map(l -> l.stream()
                            .map(id -> VocabularyAPIManager.getInstance().vocabularyRecords().get(id))
                            .sorted(Comparator.comparing(ExtendedVocabularyRecord::getMainValue))
                            .toList())
                    .orElse(Collections.emptyList());
            recursivelyPopulateRecordList(list, children);
        }
    }
}
