package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyPageResult;

public class VocabularyAPI extends CRUDAPI<Vocabulary, VocabularyPageResult> {
    private static final String COMMON_ENDPOINT = "/api/v1/vocabularies";
    private static final String FIND_INSTANCE_ENDPOINT = "/api/v1/vocabularies/find/{{0}}";
    private static final String INSTANCE_ENDPOINT = COMMON_ENDPOINT + "/{{0}}";

    public VocabularyAPI(String host, int port) {
        super(host, port, Vocabulary.class, VocabularyPageResult.class, COMMON_ENDPOINT, INSTANCE_ENDPOINT);
    }

    public Vocabulary findByName(String name) {
        return restApi.get(FIND_INSTANCE_ENDPOINT, Vocabulary.class, name);
    }
}
