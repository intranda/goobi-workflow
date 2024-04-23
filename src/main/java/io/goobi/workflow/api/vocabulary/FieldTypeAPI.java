package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.FieldType;
import io.goobi.workflow.api.vocabulary.hateoas.FieldTypePageResult;

public class FieldTypeAPI extends BaseAPI<FieldType, FieldTypePageResult> {
    private static final String FIELD_TYPES_ENDPOINT = "/api/v1/types";
    private static final String FIELD_TYPE_ENDPOINT = "/api/v1/types/{{0}}";

    public FieldTypeAPI(String host, int port) {
        super(host, port, FieldType.class, FieldTypePageResult.class, FIELD_TYPES_ENDPOINT, FIELD_TYPE_ENDPOINT);
    }
}
