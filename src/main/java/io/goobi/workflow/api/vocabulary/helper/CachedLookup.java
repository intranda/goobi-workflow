package io.goobi.workflow.api.vocabulary.helper;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CachedLookup<T> {
    private static final long MAX_DATA_AGE_IN_MS = 5000L;

    @Data
    @AllArgsConstructor
    private class AgingData {
        private final long timestamp;
        private final T data;

        public long getAge() {
            return System.currentTimeMillis() - getTimestamp();
        }
    }

    private Map<Long, AgingData> cache = new HashMap<>();
    private Function<Long, T> lookupFunction;

    public CachedLookup(Function<Long, T> lookupFunction) {
        this.lookupFunction = lookupFunction;
    }

    public synchronized T getCached(Long id) {
        if (!cache.containsKey(id)) {
            return insert(id);
        }
        AgingData agingData = cache.get(id);
        if (agingData.getAge() > MAX_DATA_AGE_IN_MS) {
            return insert(id);
        }
        return agingData.getData();
    }

    public synchronized T update(Long id, T item) {
        return insert(id, item);
    }

    public synchronized void invalidate(Long id) {
        this.cache.remove(id);
    }

    private T insert(Long id) {
        return insert(id, this.lookupFunction.apply(id));
    }

    private T insert(Long id, T data) {
        cache.put(id, new AgingData(System.currentTimeMillis(), data));
        return data;
    }
}
