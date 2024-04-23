package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.Vocabulary;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularyPageResult;

public class VocabularyAPI extends CRUDAPI<Vocabulary, VocabularyPageResult> {
    private static final String COMMON_ENDPOINT = "/api/v1/vocabularies";
    private static final String INSTANCE_ENDPOINT = COMMON_ENDPOINT + "/{{0}}";

    public VocabularyAPI(String host, int port) {
        super(host, port, Vocabulary.class, VocabularyPageResult.class, COMMON_ENDPOINT, INSTANCE_ENDPOINT);
    }
}
