package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.FieldType;
import io.goobi.vocabulary.exchange.Language;
import io.goobi.workflow.api.vocabulary.hateoas.FieldTypePageResult;
import io.goobi.workflow.api.vocabulary.hateoas.LanguagePageResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class VocabularyAPI {
    private static final String LANGUAGES_ENDPOINT = "/api/v1/languages";
    private static final String LANGUAGE_ENDPOINT = "/api/v1/languages/{{0}}";
    private static final String FIELD_TYPES_ENDPOINT = "/api/v1/types";
    private static final String FIELD_TYPE_ENDPOINT = "/api/v1/types/{{0}}";

    private final Client client = ClientBuilder.newClient();
    private String baseUrl;

    public VocabularyAPI(String host, int port) {
        baseUrl = "http://" + host + ":" + port;
    }

    private String generateUrl(String endpoint, Object... parameters) {
        String url = baseUrl + endpoint;
        for (int i = 0; i < parameters.length; i++) {
            url = url.replace("{{" + i + "}}", parameters[i].toString());
        }
        return url;
    }

    private <T> T get(String endpoint, Class<T> clazz, Object... parameters) {
        try (Response response = client
                .target(generateUrl(endpoint, parameters))
                .request(MediaType.APPLICATION_JSON)
                .get()) {
            if (response.getStatus() / 100 != 2) {
                throw new APIException(generateUrl(endpoint, parameters), "GET", response.getStatus(), response.readEntity(String.class));
            }
            return response.readEntity(clazz);
        }
    }

    private <T> T post(String endpoint, Class<T> clazz, T obj, Object... parameters) {
        try (Response response = client
                .target(generateUrl(endpoint, parameters))
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.json(obj))) {
            if (response.getStatus() / 100 != 2) {
                throw new APIException(generateUrl(endpoint, parameters), "POST", response.getStatus(), response.readEntity(String.class));
            }
            return response.readEntity(clazz);
        }
    }

    private <T> T put(String endpoint, Class<T> clazz, T obj, Object... parameters) {
        try (Response response = client
                .target(generateUrl(endpoint, parameters))
                .request(MediaType.APPLICATION_JSON)
                .put(Entity.json(obj))) {
            if (response.getStatus() / 100 != 2) {
                throw new APIException(generateUrl(endpoint, parameters), "PUT", response.getStatus(), response.readEntity(String.class));
            }
            return response.readEntity(clazz);
        }
    }

    private <T> T delete(String endpoint, Class<T> clazz, Object... parameters) {
        try (Response response = client
                .target(generateUrl(endpoint, parameters))
                .request(MediaType.APPLICATION_JSON)
                .delete()) {
            if (response.getStatus() / 100 != 2) {
                throw new APIException(generateUrl(endpoint, parameters), "DELETE", response.getStatus(), response.readEntity(String.class));
            }
            return response.readEntity(clazz);
        }
    }

    public LanguagePageResult listLanguages() {
        return get(LANGUAGES_ENDPOINT, LanguagePageResult.class);
    }

    public Language getLanguage(long id) {
        return get(LANGUAGE_ENDPOINT, Language.class, id);
    }

    public Language createLanguage(Language language) {
        return post(LANGUAGES_ENDPOINT, Language.class, language);
    }

    public Language changeLanguage(Language language) {
        long id = language.getId();
        language.setId(null);
        Language newLanguage = put(LANGUAGE_ENDPOINT, Language.class, language, id);
        language.setId(id);
        return newLanguage;
    }

    public void deleteLanguage(Language language) {
        delete(LANGUAGE_ENDPOINT, Language.class, language.getId());
    }

    public FieldTypePageResult listFieldTypes() {
        return get(FIELD_TYPES_ENDPOINT, FieldTypePageResult.class);
    }

    public FieldType getFieldType(long id) {
        return get(FIELD_TYPE_ENDPOINT, FieldType.class, id);
    }

    public FieldType createFieldType(FieldType fieldType) {
        return post(FIELD_TYPES_ENDPOINT, FieldType.class, fieldType);
    }

    public FieldType changeFieldType(FieldType fieldType) {
        long id = fieldType.getId();
        fieldType.setId(null);
        FieldType newFieldType = put(FIELD_TYPE_ENDPOINT, FieldType.class, fieldType, id);
        fieldType.setId(id);
        return newFieldType;
    }

    public void deleteFieldType(FieldType fieldType) {
        delete(FIELD_TYPE_ENDPOINT, FieldType.class, fieldType.getId());
    }
}
