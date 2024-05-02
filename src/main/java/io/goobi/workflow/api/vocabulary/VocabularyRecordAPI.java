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

    public VocabularyRecord create(VocabularyRecord vocabularyRecord) {
        if (vocabularyRecord.getParentId() == null) {
            return restApi.post(IN_VOCABULARY_RECORDS_ENDPOINT, VocabularyRecord.class, vocabularyRecord);
        } else {
            return restApi.post(INSTANCE_ENDPOINT, VocabularyRecord.class, vocabularyRecord, vocabularyRecord.getParentId());
        }
    }

    public void delete(VocabularyRecord vocabularyRecord) {
        restApi.delete(INSTANCE_ENDPOINT, VocabularyRecord.class, vocabularyRecord.getId());
    }
}
