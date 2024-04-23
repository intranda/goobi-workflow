package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.Identifiable;
import io.goobi.vocabulary.exchange.Language;
import io.goobi.workflow.api.vocabulary.hateoas.LanguagePageResult;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public abstract class BaseAPI<InstanceType extends Identifiable, PageResultType> {
    private final Client client = ClientBuilder.newClient();
    private final Class<InstanceType> instanceTypeClass;
    private final Class<PageResultType> pageResultTypeClass;
    private String baseUrl;
    private String commonEndpoint;
    private String instanceEndpoint;

    protected BaseAPI(String host, int port, Class<InstanceType> instanceTypeClass, Class<PageResultType> pageResultTypeClass, String commonEndpoint, String instanceEndpoint) {
        baseUrl = "http://" + host + ":" + port;
        this.instanceTypeClass = instanceTypeClass;
        this.pageResultTypeClass = pageResultTypeClass;
        this.commonEndpoint = commonEndpoint;
        this.instanceEndpoint = instanceEndpoint;
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

    public PageResultType list() {
        return get(commonEndpoint, pageResultTypeClass);
    }

    public InstanceType get(long id) {
        return get(instanceEndpoint, instanceTypeClass, id);
    }

    public InstanceType create(InstanceType obj) {
        return post(commonEndpoint, instanceTypeClass, obj);
    }

    public InstanceType change(InstanceType obj) {
        long id = obj.getId();
        obj.setId(null);
        InstanceType newObj = put(instanceEndpoint, instanceTypeClass, obj, id);
        obj.setId(id);
        return newObj;
    }

    public void delete(InstanceType obj) {
        delete(instanceEndpoint, instanceTypeClass, obj.getId());
    }
}
