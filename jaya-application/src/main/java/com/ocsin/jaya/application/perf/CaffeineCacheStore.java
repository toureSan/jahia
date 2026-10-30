package com.ocsin.jaya.application.perf;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ocsin.jaya.application.port.CacheStore;

import java.time.Duration;
import java.util.Optional;

/**
 * Cache local avec statistiques. Une écriture invalide les préfixes article et search.
 */
public final class CaffeineCacheStore implements CacheStore {

    private final Cache<String, Object> cache;

    public CaffeineCacheStore(Duration ttl, long maximumSize) {
        this.cache = Caffeine.newBuilder()
                .expireAfterWrite(ttl)
                .maximumSize(maximumSize)
                .recordStats()
                .build();
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        Object value = cache.getIfPresent(key);
        if (!type.isInstance(value)) {
            return Optional.empty();
        }
        return Optional.of(type.cast(value));
    }

    @Override
    public void put(String key, Object value) {
        cache.put(key, value);
    }

    @Override
    public void invalidatePrefix(String prefix) {
        cache.asMap().keySet().removeIf(key -> key.startsWith(prefix));
    }

    @Override
    public CacheStatistics statistics() {
        com.github.benmanes.caffeine.cache.stats.CacheStats stats = cache.stats();
        double hitRate = stats.hitRate();
        if (Double.isNaN(hitRate)) {
            hitRate = 0d;
        }
        return new CacheStatistics(stats.hitCount(), stats.missCount(), hitRate, stats.evictionCount());
    }
}
