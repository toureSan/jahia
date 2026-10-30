package com.ocsin.jaya.application;

import com.ocsin.jaya.application.perf.DiagnosticsSnapshot;
import com.ocsin.jaya.application.perf.OperationMonitor;
import com.ocsin.jaya.application.port.CacheStore;
import com.ocsin.jaya.domain.Action;
import com.ocsin.jaya.domain.Principal;
import com.ocsin.jaya.security.AuthorizationPolicy;

public final class DiagnosticsService {

    private final AuthorizationPolicy authorization;
    private final CacheStore cache;
    private final OperationMonitor monitor;

    public DiagnosticsService(AuthorizationPolicy authorization, CacheStore cache, OperationMonitor monitor) {
        this.authorization = authorization;
        this.cache = cache;
        this.monitor = monitor;
    }

    public DiagnosticsSnapshot snapshot(Principal principal) {
        authorization.check(principal, Action.DIAGNOSE);
        return new DiagnosticsSnapshot(cache.statistics(), monitor.slowOperations(), monitor.operations());
    }
}
