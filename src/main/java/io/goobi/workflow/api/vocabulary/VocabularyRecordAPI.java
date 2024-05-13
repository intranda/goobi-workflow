package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyRecordPageResult;

import java.util.Optional;

public class VocabularyRecordAPI {
    private static final String IN_VOCABULARY_RECORDS_ENDPOINT = "/api/v1/vocabularies/{{0}}/records";
    private static final String IN_VOCABULARY_SEARCH_ENDPOINT = "/api/v1/vocabularies/{{0}}/records/search";
    private static final String INSTANCE_ENDPOINT = "/api/v1/records/{{0}}";

    private final RESTAPI restApi;

    public VocabularyRecordAPI(String host, int port) {
        this.restApi = new RESTAPI(host, port);
    }

    public VocabularyRecordPageResult list(long vocabularyId) {
        return list(vocabularyId, Optional.empty(), Optional.empty());
    }

    public VocabularyRecordPageResult list(long vocabularyId, Optional<Integer> size, Optional<Integer> page) {
        String params = "";
        if (size.isPresent()) {
            params += params.isEmpty() ? "?" : "&";
            params += "size=" + size.get();
        }
        if (page.isPresent()) {
            params += params.isEmpty() ? "?" : "&";
            params += "page=" + page.get();
        }
        VocabularySchema schema = VocabularyAPIManager.getInstance().vocabularySchemas().get(VocabularyAPIManager.getInstance().vocabularies().get(vocabularyId).getSchemaId());
        VocabularyRecordPageResult result = restApi.get(IN_VOCABULARY_RECORDS_ENDPOINT + params, VocabularyRecordPageResult.class, vocabularyId);
        result.getContent().forEach(r -> r.load(schema));
        return result;
    }

    public VocabularyRecord get(long id) {
        return restApi.get(INSTANCE_ENDPOINT, VocabularyRecord.class, id);
    }

    public VocabularyRecordPageResult search(long vocabularyId, String query) {
        VocabularySchema schema = VocabularyAPIManager.getInstance().vocabularySchemas().get(VocabularyAPIManager.getInstance().vocabularies().get(vocabularyId).getSchemaId());
        VocabularyRecordPageResult result = restApi.get(IN_VOCABULARY_SEARCH_ENDPOINT, VocabularyRecordPageResult.class, vocabularyId, "query=" + query);
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
}
