package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.Language;
import io.goobi.workflow.api.vocabulary.hateoas.LanguagePageResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

public class VocabularyAPI {
    private static final String LANGUAGES_ENDPOINT = "/api/v1/languages";
    private static final String LANGUAGE_ENDPOINT = "/api/v1/languages/{{0}}";

    private final Client client = ClientBuilder.newClient();
    private String baseUrl;

    public VocabularyAPI(String host, int port) {
        baseUrl = "http://" + host + ":" + port;
    }

    private <T> T get(String endpoint, Class<T> clazz, Object... parameters) {
        String url = baseUrl + endpoint;
        for (int i = 0; i < parameters.length; i++) {
            url = url.replace("{{" + i + "}}", parameters[i].toString());
        }
        return client
                .target(url)
                .request(MediaType.APPLICATION_JSON)
                .get(clazz);
    }

    public LanguagePageResult listLanguages() {
        return get(LANGUAGES_ENDPOINT, LanguagePageResult.class);
    }

    public Language getLanguage(long id) {
        return get(LANGUAGE_ENDPOINT, Language.class, id);
    }
}
