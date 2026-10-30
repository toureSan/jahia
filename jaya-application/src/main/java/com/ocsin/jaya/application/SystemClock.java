package com.ocsin.jaya.application;

import com.ocsin.jaya.domain.Clock;

import java.time.Instant;

public final class SystemClock implements Clock {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
