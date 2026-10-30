package com.ocsin.jaya.application.perf;

public final class CacheStatistics {

    private final long hitCount;
    private final long missCount;
    private final double hitRate;
    private final long evictionCount;

    public CacheStatistics(long hitCount, long missCount, double hitRate, long evictionCount) {
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.hitRate = hitRate;
        this.evictionCount = evictionCount;
    }

    public long getHitCount() {
        return hitCount;
    }

    public long getMissCount() {
        return missCount;
    }

    public double getHitRate() {
        return hitRate;
    }

    public long getEvictionCount() {
        return evictionCount;
    }
}
