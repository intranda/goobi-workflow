package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.Identifiable;

import java.util.Optional;

public abstract class CRUDAPI<InstanceType extends Identifiable, PageResultType> {
    protected final RESTAPI restApi;
    private final Class<InstanceType> instanceTypeClass;
    private final Class<PageResultType> pageResultTypeClass;
    private final String commonEndpoint;
    private final String instanceEndpoint;

    protected CRUDAPI(String host, int port, Class<InstanceType> instanceTypeClass, Class<PageResultType> pageResultTypeClass, String commonEndpoint, String instanceEndpoint) {
        this.restApi = new RESTAPI(host, port);
        this.instanceTypeClass = instanceTypeClass;
        this.pageResultTypeClass = pageResultTypeClass;
        this.commonEndpoint = commonEndpoint;
        this.instanceEndpoint = instanceEndpoint;
    }

    public PageResultType list() {
        return restApi.get(commonEndpoint, pageResultTypeClass);
    }

    @Deprecated(since = "24.07", forRemoval = true)
    public PageResultType list(Optional<Integer> size, Optional<Integer> page) {
        return list(size, page, Optional.empty());
    }

    public PageResultType list(Optional<Integer> size, Optional<Integer> page, Optional<String> sort) {
        String params = "";
        if (size.isPresent()) {
            params += params.isEmpty() ? "?" : "&";
            params += "size=" + size.get();
        }
        if (page.isPresent()) {
            params += params.isEmpty() ? "?" : "&";
            params += "page=" + page.get();
        }
        if (sort.isPresent()) {
            params += params.isEmpty() ? "?" : "&";
            params += "sort=" + sort.get();
        }
        return restApi.get(commonEndpoint + params, pageResultTypeClass);
    }

    public InstanceType get(long id) {
        return restApi.get(instanceEndpoint, instanceTypeClass, id);
    }

    public InstanceType create(InstanceType obj) {
        return restApi.post(commonEndpoint, instanceTypeClass, obj);
    }

    public InstanceType change(InstanceType obj) {
        long id = obj.getId();
        obj.setId(null);
        InstanceType newObj = restApi.put(instanceEndpoint, instanceTypeClass, obj, id);
        obj.setId(id);
        return newObj;
    }

    public void delete(InstanceType obj) {
        restApi.delete(instanceEndpoint, instanceTypeClass, obj.getId());
    }
}
