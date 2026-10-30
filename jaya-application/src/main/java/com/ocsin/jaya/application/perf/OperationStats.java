package com.ocsin.jaya.application.perf;

public final class OperationStats {

    private final String name;
    private final long count;
    private final long averageMillis;

    public OperationStats(String name, long count, long averageMillis) {
        this.name = name;
        this.count = count;
        this.averageMillis = averageMillis;
    }

    public String getName() {
        return name;
    }

    public long getCount() {
        return count;
    }

    public long getAverageMillis() {
        return averageMillis;
    }
}
