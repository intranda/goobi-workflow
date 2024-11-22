package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.Language;
import io.goobi.workflow.api.vocabulary.hateoas.LanguagePageResult;
import io.goobi.workflow.api.vocabulary.helper.CachedLookup;

public class LanguageAPI extends CRUDAPI<Language, LanguagePageResult> {
    private static final String COMMON_ENDPOINT = "/api/v1/languages";
    private static final String FIND_INSTANCE_ENDPOINT = "/api/v1/languages/by-abbreviation/{{0}}";
    private static final String INSTANCE_ENDPOINT = COMMON_ENDPOINT + "/{{0}}";

    private final CachedLookup<String, Language> singleLookupCache;

    public LanguageAPI(String address) {
        super(address, Language.class, LanguagePageResult.class, COMMON_ENDPOINT, INSTANCE_ENDPOINT);
        this.singleLookupCache = new CachedLookup<>(abbreviation -> restApi.get(FIND_INSTANCE_ENDPOINT, Language.class, abbreviation));
    }

    public Language findByAbbreviation(String abbreviation) {
        return this.singleLookupCache.getCached(abbreviation);
    }
}
