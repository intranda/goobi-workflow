package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.Language;
import io.goobi.workflow.api.vocabulary.hateoas.LanguagePageResult;

public class LanguageAPI extends BaseAPI<Language, LanguagePageResult> {
    private static final String COMMON_ENDPOINT = "/api/v1/languages";
    private static final String INSTANCE_ENDPOINT = COMMON_ENDPOINT + "/{{0}}";

    public LanguageAPI(String host, int port) {
        super(host, port, Language.class, LanguagePageResult.class, COMMON_ENDPOINT, INSTANCE_ENDPOINT);
    }
}
