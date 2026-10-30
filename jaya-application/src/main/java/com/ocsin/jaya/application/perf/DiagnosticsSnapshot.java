package com.ocsin.jaya.application.perf;

import java.util.List;

public final class DiagnosticsSnapshot {

    private final CacheStatistics cache;
    private final List<SlowOperation> slowOperations;
    private final List<OperationStats> operations;

    public DiagnosticsSnapshot(CacheStatistics cache, List<SlowOperation> slowOperations, List<OperationStats> operations) {
        this.cache = cache;
        this.slowOperations = List.copyOf(slowOperations);
        this.operations = List.copyOf(operations);
    }

    public CacheStatistics getCache() {
        return cache;
    }

    public List<SlowOperation> getSlowOperations() {
        return slowOperations;
    }

    public List<OperationStats> getOperations() {
        return operations;
    }
}
