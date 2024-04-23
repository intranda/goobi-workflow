package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.VocabularySchema;
import io.goobi.workflow.api.vocabulary.hateoas.VocabularySchemaPageResult;

public class VocabularySchemaAPI extends BaseAPI<VocabularySchema, VocabularySchemaPageResult> {
    private static final String COMMON_ENDPOINT = "/api/v1/schemas";
    private static final String INSTANCE_ENDPOINT = COMMON_ENDPOINT + "/{{0}}";

    public VocabularySchemaAPI(String host, int port) {
        super(host, port, VocabularySchema.class, VocabularySchemaPageResult.class, COMMON_ENDPOINT, INSTANCE_ENDPOINT);
    }
}
