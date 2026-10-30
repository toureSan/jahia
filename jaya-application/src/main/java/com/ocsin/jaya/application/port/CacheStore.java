package com.ocsin.jaya.application.port;

import com.ocsin.jaya.application.perf.CacheStatistics;

import java.util.Optional;

public interface CacheStore {

    <T> Optional<T> get(String key, Class<T> type);

    void put(String key, Object value);

    void invalidatePrefix(String prefix);

    CacheStatistics statistics();
}
