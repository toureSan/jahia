package com.ocsin.jaya.application.perf;

import java.time.Instant;

public final class SlowOperation {

    private final String name;
    private final long durationMillis;
    private final Instant at;

    public SlowOperation(String name, long durationMillis, Instant at) {
        this.name = name;
        this.durationMillis = durationMillis;
        this.at = at;
    }

    public String getName() {
        return name;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public String getAt() {
        return at.toString();
    }
}
