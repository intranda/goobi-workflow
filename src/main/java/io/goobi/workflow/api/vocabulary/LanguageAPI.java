package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.Language;
import io.goobi.workflow.api.vocabulary.hateoas.LanguagePageResult;

public class LanguageAPI extends BaseAPI<Language, LanguagePageResult> {
    private static final String LANGUAGES_ENDPOINT = "/api/v1/languages";
    private static final String LANGUAGE_ENDPOINT = "/api/v1/languages/{{0}}";

    public LanguageAPI(String host, int port) {
        super(host, port, Language.class, LanguagePageResult.class, LANGUAGES_ENDPOINT, LANGUAGE_ENDPOINT);
    }
}
