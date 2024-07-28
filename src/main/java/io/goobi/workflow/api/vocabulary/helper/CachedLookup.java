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

    public synchronized T getCached(Long id) {
        if (!cache.containsKey(id)) {
            cache.put(id, this.lookupFunction.apply(id));
        }
        return cache.get(id);
    }

    public synchronized void invalidate(Long id) {
        this.cache.remove(id);
    }
}
