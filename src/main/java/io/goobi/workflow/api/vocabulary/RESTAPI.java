package io.goobi.workflow.api.vocabulary;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class RESTAPI {
    private final Client client = ClientBuilder.newClient();
    private String baseUrl;

    protected RESTAPI(String host, int port) {
        baseUrl = "http://" + host + ":" + port;
    }

    private String generateUrl(String endpoint, Object... parameters) {
        String url = baseUrl + endpoint;
        for (int i = 0; i < parameters.length; i++) {
            url = url.replace("{{" + i + "}}", parameters[i].toString());
        }
        return url;
    }

    public <T> T get(String endpoint, Class<T> clazz, Object... parameters) {
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

    public <T> T post(String endpoint, Class<T> clazz, T obj, Object... parameters) {
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

    public <T> T put(String endpoint, Class<T> clazz, T obj, Object... parameters) {
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

    public <T> T delete(String endpoint, Class<T> clazz, Object... parameters) {
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
}
