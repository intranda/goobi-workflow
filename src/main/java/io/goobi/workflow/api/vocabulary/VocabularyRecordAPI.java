package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyRecordPageResult;
import io.goobi.workflow.api.vocabulary.jsfwrapper.JSFVocabularyRecord;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class VocabularyRecordAPI {
    private static final String IN_VOCABULARY_RECORDS_ENDPOINT = "/api/v1/vocabularies/{{0}}/records";
    private static final String IN_VOCABULARY_ALL_RECORDS_ENDPOINT = "/api/v1/vocabularies/{{0}}/records/all";
    private static final String IN_VOCABULARY_SEARCH_ENDPOINT = "/api/v1/vocabularies/{{0}}/records/search";
    private static final String METADATA_ENDPOINT = "/api/v1/vocabularies/{{0}}/metadata";
    private static final String INSTANCE_ENDPOINT = "/api/v1/records/{{0}}";

    private final RESTAPI restApi;

    public VocabularyRecordAPI(String host, int port) {
        this.restApi = new RESTAPI(host, port);
    }

    public List<JSFVocabularyRecord> all(long vocabularyId) {
        return all(vocabularyId, Optional.empty());
    }

    public List<JSFVocabularyRecord> all(long vocabularyId, Optional<String> sorting) {
        VocabularySchema schema = VocabularyAPIManager.getInstance().vocabularySchemas().get(VocabularyAPIManager.getInstance().vocabularies().get(vocabularyId).getSchemaId());
        List<Object> urlParameters = new ArrayList<>(1 + (sorting.isPresent() ? 1 : 0));
        urlParameters.add(vocabularyId);
        sorting.ifPresent(s -> urlParameters.add("sort=" + s));
        List<JSFVocabularyRecord> result = restApi.get(IN_VOCABULARY_ALL_RECORDS_ENDPOINT, VocabularyRecordPageResult.class, urlParameters.toArray()).getContent();
        result.forEach(r -> r.load(schema));
        return result;
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
        VocabularySchema schema = VocabularyAPIManager.getInstance().vocabularySchemas().get(VocabularyAPIManager.getInstance().vocabularies().get(vocabularyId).getSchemaId());
        VocabularyRecordPageResult result = restApi.get(IN_VOCABULARY_RECORDS_ENDPOINT + params, VocabularyRecordPageResult.class, vocabularyId);
        result.getContent().forEach(r -> r.load(schema));
        return result;
    }

    public JSFVocabularyRecord get(long id) {
        JSFVocabularyRecord result = restApi.get(INSTANCE_ENDPOINT, JSFVocabularyRecord.class, id);
        VocabularySchema schema = VocabularyAPIManager.getInstance().vocabularySchemas().get(VocabularyAPIManager.getInstance().vocabularies().get(result.getVocabularyId()).getSchemaId());
        result.load(schema);
        return result;
    }

    public VocabularyRecordPageResult search(long vocabularyId, String query) {
        return search(vocabularyId, query, Optional.empty());
    }

    public VocabularyRecordPageResult search(long vocabularyId, String query, Optional<String> sorting) {
        VocabularySchema schema = VocabularyAPIManager.getInstance().vocabularySchemas().get(VocabularyAPIManager.getInstance().vocabularies().get(vocabularyId).getSchemaId());
        List<Object> urlParameters = new ArrayList<>(2 + (sorting.isPresent() ? 1 : 0));
        urlParameters.add(vocabularyId);
        urlParameters.add("query=" + query);
        sorting.ifPresent(s -> urlParameters.add("sort=" + s));
        VocabularyRecordPageResult result = restApi.get(IN_VOCABULARY_SEARCH_ENDPOINT, VocabularyRecordPageResult.class, urlParameters.toArray());
        result.getContent().forEach(r -> r.load(schema));
        return result;
    }

    public VocabularyRecord create(VocabularyRecord vocabularyRecord) {
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
        return newRecord;
    }

    public VocabularyRecord change(VocabularyRecord vocabularyRecord) {
        long id = vocabularyRecord.getId();
        vocabularyRecord.setId(null);
        VocabularyRecord newRecord = restApi.put(INSTANCE_ENDPOINT, VocabularyRecord.class, vocabularyRecord, id);
        vocabularyRecord.setId(id);
        return newRecord;
    }

    public void delete(VocabularyRecord vocabularyRecord) {
        restApi.delete(INSTANCE_ENDPOINT, VocabularyRecord.class, vocabularyRecord.getId());
    }

    public JSFVocabularyRecord getMetadata(Long id) {
        JSFVocabularyRecord result = restApi.get(METADATA_ENDPOINT, JSFVocabularyRecord.class, id);
        VocabularySchema schema = VocabularyAPIManager.getInstance().vocabularySchemas().get(VocabularyAPIManager.getInstance().vocabularies().get(result.getVocabularyId()).getMetadataSchemaId());
        result.load(schema);
        return result;
    }
}
