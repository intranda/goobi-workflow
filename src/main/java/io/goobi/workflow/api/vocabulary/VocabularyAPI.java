package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.Language;
import io.goobi.workflow.api.vocabulary.hateoas.LanguagePageResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

public class VocabularyAPI {
    private final Client client = ClientBuilder.newClient();

    public LanguagePageResult listLanguages() {
        return client
                .target("http://localhost:8080/api/v1/languages")
                .request(MediaType.APPLICATION_JSON)
                .get(LanguagePageResult.class);
    }

    public Language getLanguage(long id) {
        return client
                .target("http://localhost:8080/api/v1/languages/" + id)
                .request(MediaType.APPLICATION_JSON)
                .get(Language.class);
    }
}
