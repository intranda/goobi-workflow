package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exception.VocabularyException;
import lombok.Setter;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

import javax.servlet.http.Part;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static io.goobi.workflow.api.vocabulary.VocabularyAPIManager.setupBearerTokenAuthenticationIfPresent;

public class RESTAPI {
    @Setter
    private static Client client = ClientBuilder.newBuilder()
            .register(MultiPartFeature.class)
            .register(FileDataBodyPart.class)
            .build();
    private String baseUrl;

    protected RESTAPI(String address) {
        baseUrl = address;
    }

    private String generateUrl(String endpoint, Object... parameters) {
        String url = endpoint;
        if (!url.startsWith(baseUrl)) {
            url = baseUrl + url;
        }
        List<String> queryParams = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            if (url.contains("{{" + i + "}}")) {
                url = url.replace("{{" + i + "}}", parameters[i].toString());
            } else {
                queryParams.add(URLEncoder.encode(parameters[i].toString(), StandardCharsets.UTF_8));
            }
        }
        if (!queryParams.isEmpty()) {
            url = url + "?" + String.join("&", queryParams);
        }
        return url;
    }

    public <T> T get(String endpoint, Class<T> clazz, Object... parameters) {
        try {
            Invocation.Builder builder = client
                    .target(generateUrl(endpoint, parameters))
                    .request(MediaType.APPLICATION_JSON);
            builder = setupBearerTokenAuthenticationIfPresent(builder);
            try (Response response = builder.get()) {
                if (response.getStatus() / 100 != 2) {
                    throw new APIException(generateUrl(endpoint, parameters), "GET", response.getStatus(), "Vocabulary server error", response.readEntity(VocabularyException.class), null);
                }
                return response.readEntity(clazz);
            }
        } catch (APIException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new APIException(generateUrl(endpoint, parameters), "GET", -1, e.getMessage(), null, e);
        }
    }

    public <T> T post(String endpoint, Class<T> clazz, T obj, Object... parameters) {
        try {
            Invocation.Builder builder = client
                    .target(generateUrl(endpoint, parameters))
                    .request(MediaType.APPLICATION_JSON);
            builder = setupBearerTokenAuthenticationIfPresent(builder);
            try (Response response = builder.post(Entity.json(obj))) {
                if (response.getStatus() / 100 != 2) {
                    throw new APIException(generateUrl(endpoint, parameters), "POST", response.getStatus(), "Vocabulary server error", response.readEntity(VocabularyException.class), null);
                }
                return response.readEntity(clazz);
            }
        } catch (APIException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new APIException(generateUrl(endpoint, parameters), "POST", -1, e.getMessage(), null, e);
        }
    }

    public <T> T put(String endpoint, Class<T> clazz, T obj, Object... parameters) {
        try {
            Invocation.Builder builder = client
                    .target(generateUrl(endpoint, parameters))
                    .request(MediaType.APPLICATION_JSON);
            builder = setupBearerTokenAuthenticationIfPresent(builder);
            try (Response response = builder.put(Entity.json(obj))) {
                if (response.getStatus() / 100 != 2) {
                    throw new APIException(generateUrl(endpoint, parameters), "PUT", response.getStatus(), "Vocabulary server error", response.readEntity(VocabularyException.class), null);
                }
                return response.readEntity(clazz);
            }
        } catch (APIException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new APIException(generateUrl(endpoint, parameters), "PUT", -1, e.getMessage(), null, e);
        }
    }

    public Response put(String endpoint, Part part, Object... parameters) {
        try {
            StreamDataBodyPart body = new StreamDataBodyPart("file", part.getInputStream());
            try (MultiPart multiPart = new FormDataMultiPart()) {
                multiPart.bodyPart(body);
                Invocation.Builder builder = client
                        .target(generateUrl(endpoint, parameters))
                        .request(MediaType.APPLICATION_JSON);
                builder = setupBearerTokenAuthenticationIfPresent(builder);
                try (Response response = builder.put(Entity.entity(multiPart, multiPart.getMediaType()), Response.class)) {
                    if (response.getStatus() / 100 != 2) {
                        throw new APIException(generateUrl(endpoint, parameters), "PUT", response.getStatus(), "Vocabulary server error", response.readEntity(VocabularyException.class), null);
                    }
                    return response.readEntity(Response.class);
                }
            }
        } catch (APIException e) {
            throw e;
        } catch (RuntimeException | IOException e) {
            throw new APIException(generateUrl(endpoint, parameters), "PUT", -1, e.getMessage(), null, e);
        }
    }

    public Response post(String endpoint, Part part, Object... parameters) {
        try {
            StreamDataBodyPart body = new StreamDataBodyPart("file", part.getInputStream());
            try (MultiPart multiPart = new FormDataMultiPart()) {
                multiPart.bodyPart(body);
                Invocation.Builder builder = client
                        .target(generateUrl(endpoint, parameters))
                        .request(MediaType.APPLICATION_JSON);
                builder = setupBearerTokenAuthenticationIfPresent(builder);
                try (Response response = builder.post(Entity.entity(multiPart, multiPart.getMediaType()), Response.class)) {
                    if (response.getStatus() / 100 != 2) {
                        throw new APIException(generateUrl(endpoint, parameters), "POST", response.getStatus(), "Vocabulary server error", response.readEntity(VocabularyException.class), null);
                    }
                    return response.readEntity(Response.class);
                }
            }
        } catch (APIException e) {
            throw e;
        } catch (RuntimeException | IOException e) {
            throw new APIException(generateUrl(endpoint, parameters), "POST", -1, e.getMessage(), null, e);
        }
    }

    public <T> T delete(String endpoint, Class<T> clazz, Object... parameters) {
        try {
            Invocation.Builder builder = client
                    .target(generateUrl(endpoint, parameters))
                    .request(MediaType.APPLICATION_JSON);
            builder = setupBearerTokenAuthenticationIfPresent(builder);
            try (Response response = builder.delete()) {
                if (response.getStatus() / 100 != 2) {
                    throw new APIException(generateUrl(endpoint, parameters), "DELETE", response.getStatus(), "Vocabulary server error", response.readEntity(VocabularyException.class), null);
                }
                return response.readEntity(clazz);
            }
        } catch (APIException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new APIException(generateUrl(endpoint, parameters), "DELETE", -1, e.getMessage(), null, e);
        }
    }
}
