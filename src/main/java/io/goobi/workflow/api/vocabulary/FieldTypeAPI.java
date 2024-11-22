package io.goobi.workflow.api.vocabulary;

import io.goobi.vocabulary.exchange.FieldType;
import io.goobi.workflow.api.vocabulary.hateoas.FieldTypePageResult;
import io.goobi.workflow.api.vocabulary.helper.CachedLookup;

public class FieldTypeAPI extends CRUDAPI<FieldType, FieldTypePageResult> {
    private static final String COMMON_ENDPOINT = "/api/v1/types";
    private static final String INSTANCE_ENDPOINT = COMMON_ENDPOINT + "/{{0}}";

    private final CachedLookup<Long, FieldType> singleLookupCache;

    public FieldTypeAPI(String address) {
        super(address, FieldType.class, FieldTypePageResult.class, COMMON_ENDPOINT, INSTANCE_ENDPOINT);
        this.singleLookupCache = new CachedLookup<>(super::get);
    }

    @Override
    public FieldType get(long id) {
        return this.singleLookupCache.getCached(id);
    }
}
