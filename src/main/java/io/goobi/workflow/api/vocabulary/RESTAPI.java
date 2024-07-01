package io.goobi.workflow.api.vocabulary;

import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.glassfish.jersey.media.multipart.file.StreamDataBodyPart;

import javax.servlet.http.Part;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RESTAPI {
    private final Client client = ClientBuilder.newBuilder()
            .register(MultiPartFeature.class)
            .register(FileDataBodyPart.class)
            .build();
    private String baseUrl;

    protected RESTAPI(String host, int port) {
        baseUrl = "http://" + host + ":" + port;
    }

    private String generateUrl(String endpoint, Object... parameters) {
        String url = baseUrl + endpoint;
        List<String> queryParams = new ArrayList<>();
        for (int i = 0; i < parameters.length; i++) {
            if (url.contains("{{" + i + "}}")) {
                url = url.replace("{{" + i + "}}", parameters[i].toString());
            } else {
                queryParams.add(parameters[i].toString());
            }
        }
        if (!queryParams.isEmpty()) {
            url = url + "?" + String.join("&", queryParams);
        }
        return url;
    }

    public <T> T get(String endpoint, Class<T> clazz, Object... parameters) {
        try {
            try (Response response = client
                    .target(generateUrl(endpoint, parameters))
                    .request(MediaType.APPLICATION_JSON)
                    .get()) {
                if (response.getStatus() / 100 != 2) {
                    throw new APIException(generateUrl(endpoint, parameters), "GET", response.getStatus(), response.readEntity(String.class));
                }
                return response.readEntity(clazz);
            }
        } catch (RuntimeException e) {
            throw new APIException(generateUrl(endpoint, parameters), "GET", -1, e.getMessage());
        }
    }

    public <T> T post(String endpoint, Class<T> clazz, T obj, Object... parameters) {
        try {
            try (Response response = client
                    .target(generateUrl(endpoint, parameters))
                    .request(MediaType.APPLICATION_JSON)
                    .post(Entity.json(obj))) {
                if (response.getStatus() / 100 != 2) {
                    throw new APIException(generateUrl(endpoint, parameters), "POST", response.getStatus(), response.readEntity(String.class));
                }
                return response.readEntity(clazz);
            }
        } catch (RuntimeException e) {
            throw new APIException(generateUrl(endpoint, parameters), "POST", -1, e.getMessage());
        }
    }

    public <T> T put(String endpoint, Class<T> clazz, T obj, Object... parameters) {
        try {
            try (Response response = client
                    .target(generateUrl(endpoint, parameters))
                    .request(MediaType.APPLICATION_JSON)
                    .put(Entity.json(obj))) {
                if (response.getStatus() / 100 != 2) {
                    throw new APIException(generateUrl(endpoint, parameters), "PUT", response.getStatus(), response.readEntity(String.class));
                }
                return response.readEntity(clazz);
            }
        } catch (RuntimeException e) {
            throw new APIException(generateUrl(endpoint, parameters), "PUT", -1, e.getMessage());
        }
    }

    public Response put(String endpoint, Part part, Object... parameters) {
        try {
            StreamDataBodyPart body = new StreamDataBodyPart("file", part.getInputStream());
            try (MultiPart multiPart = new FormDataMultiPart()) {
                multiPart.bodyPart(body);
                try (Response response = client
                        .target(generateUrl(endpoint, parameters))
                        .request(MediaType.MULTIPART_FORM_DATA)
                        .put(Entity.entity(multiPart, multiPart.getMediaType()), Response.class)) {
                    if (response.getStatus() / 100 != 2) {
                        throw new APIException(generateUrl(endpoint, parameters), "PUT", response.getStatus(), response.readEntity(String.class));
                    }
                    return response.readEntity(Response.class);
                }
            }
        } catch (RuntimeException | IOException e) {
            throw new APIException(generateUrl(endpoint, parameters), "PUT", -1, e.getMessage());
        }
    }

    public Response post(String endpoint, Part part, Object... parameters) {
        try {
            StreamDataBodyPart body = new StreamDataBodyPart("file", part.getInputStream());
            try (MultiPart multiPart = new FormDataMultiPart()) {
                multiPart.bodyPart(body);
                try (Response response = client
                        .target(generateUrl(endpoint, parameters))
                        .request(MediaType.MULTIPART_FORM_DATA)
                        .post(Entity.entity(multiPart, multiPart.getMediaType()), Response.class)) {
                    if (response.getStatus() / 100 != 2) {
                        throw new APIException(generateUrl(endpoint, parameters), "POST", response.getStatus(), response.readEntity(String.class));
                    }
                    return response.readEntity(Response.class);
                }
            }
        } catch (RuntimeException | IOException e) {
            throw new APIException(generateUrl(endpoint, parameters), "POST", -1, e.getMessage());
        }
    }

    public <T> T delete(String endpoint, Class<T> clazz, Object... parameters) {
        try {
            try (Response response = client
                    .target(generateUrl(endpoint, parameters))
                    .request(MediaType.APPLICATION_JSON)
                    .delete()) {
                if (response.getStatus() / 100 != 2) {
                    throw new APIException(generateUrl(endpoint, parameters), "DELETE", response.getStatus(), response.readEntity(String.class));
                }
                return response.readEntity(clazz);
            }
        } catch (RuntimeException e) {
            throw new APIException(generateUrl(endpoint, parameters), "DELETE", -1, e.getMessage());
        }
    }
}
