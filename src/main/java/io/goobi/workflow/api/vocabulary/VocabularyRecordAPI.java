package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.VocabularyRecord;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyRecordPageResult;

public class VocabularyRecordAPI {
    private static final String IN_VOCABULARY_RECORDS_ENDPOINT = "/api/v1/vocabularies/{{0}}/records";
    private static final String IN_VOCABULARY_SEARCH_ENDPOINT = "/api/v1/vocabularies/{{0}}/records/search";
    private static final String INSTANCE_ENDPOINT = "/api/v1/records/{{0}}";

    private final RESTAPI restApi;

    public VocabularyRecordAPI(String host, int port) {
        this.restApi = new RESTAPI(host, port);
    }

    public VocabularyRecordPageResult list(long vocabularyId) {
        return restApi.get(IN_VOCABULARY_RECORDS_ENDPOINT, VocabularyRecordPageResult.class, vocabularyId);
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
