package io.goobi.workflow.api.vocabulary.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CachedLookup<T> {
    private Map<Long, T> cache = new HashMap<>();
    private Function<Long, T> lookupFunction;

    public CachedLookup(Function<Long, T> lookupFunction) {
        this.lookupFunction = lookupFunction;
    }

    public T getCached(Long id) {
        return cache.computeIfAbsent(id, this.lookupFunction);
    }
}
