package io.goobi.workflow.api.vocabulary.helper;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CachedLookup<K, T> {
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

    private Map<K, AgingData> cache = new HashMap<>();
    private Function<K, T> lookupFunction;

    public CachedLookup(Function<K, T> lookupFunction) {
        this.lookupFunction = lookupFunction;
    }

    public synchronized T getCached(K key) {
        if (!cache.containsKey(key)) {
            return insert(key);
        }
        AgingData agingData = cache.get(key);
        if (agingData.getAge() > MAX_DATA_AGE_IN_MS) {
            return insert(key);
        }
        return agingData.getData();
    }

    public synchronized T update(K key, T item) {
        return insert(key, item);
    }

    public synchronized void invalidate(K key) {
        this.cache.remove(key);
    }

    private T insert(K key) {
        return insert(key, this.lookupFunction.apply(key));
    }

    private T insert(K key, T data) {
        cache.put(key, new AgingData(System.currentTimeMillis(), data));
        return data;
    }
}
