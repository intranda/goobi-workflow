package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.FieldType;
import io.goobi.workflow.api.vocabulary.hateoas.FieldTypePageResult;

public class FieldTypeAPI extends CRUDAPI<FieldType, FieldTypePageResult> {
    private static final String COMMON_ENDPOINT = "/api/v1/types";
    private static final String INSTANCE_ENDPOINT = COMMON_ENDPOINT + "/{{0}}";

    public FieldTypeAPI(String host, int port) {
        super(host, port, FieldType.class, FieldTypePageResult.class, COMMON_ENDPOINT, INSTANCE_ENDPOINT);
    }
}
