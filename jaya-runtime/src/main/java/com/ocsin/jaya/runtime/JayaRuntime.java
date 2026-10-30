package com.ocsin.jaya.runtime;

import com.ocsin.jaya.application.DefaultEditorialService;
import com.ocsin.jaya.application.DiagnosticsService;
import com.ocsin.jaya.application.EditorialService;
import com.ocsin.jaya.application.InputGuard;
import com.ocsin.jaya.application.NoopExternalPublisher;
import com.ocsin.jaya.application.RandomIds;
import com.ocsin.jaya.application.Slf4jAuditLog;
import com.ocsin.jaya.application.SystemClock;
import com.ocsin.jaya.application.perf.CaffeineCacheStore;
import com.ocsin.jaya.application.perf.OperationMonitor;
import com.ocsin.jaya.application.port.CacheStore;
import com.ocsin.jaya.application.port.ExternalPublisher;
import com.ocsin.jaya.domain.Clock;
import com.ocsin.jaya.jcr.JahiaSessionProvider;
import com.ocsin.jaya.jcr.JcrArticleStore;
import com.ocsin.jaya.jcr.MigratingSessionProvider;
import com.ocsin.jaya.jcr.ModuleBootstrap;
import com.ocsin.jaya.jcr.SessionProvider;
import com.ocsin.jaya.security.AuthorizationPolicy;
import com.ocsin.jaya.security.TokenAuthenticator;

import java.time.Duration;

/**
 * Assemble le service éditorial, le cache et l'accès JCR.
 */
public final class JayaRuntime implements AutoCloseable {

    private final SessionProvider sessions;
    private final EditorialService editorial;
    private final DiagnosticsService diagnostics;
    private final TokenAuthenticator authenticator;
    private final AuthorizationPolicy authorization;
    private final CacheStore cache;
    private final OperationMonitor monitor;

    private JayaRuntime(SessionProvider sessions,
                        EditorialService editorial,
                        DiagnosticsService diagnostics,
                        TokenAuthenticator authenticator,
                        AuthorizationPolicy authorization,
                        CacheStore cache,
                        OperationMonitor monitor) {
        this.sessions = sessions;
        this.editorial = editorial;
        this.diagnostics = diagnostics;
        this.authenticator = authenticator;
        this.authorization = authorization;
        this.cache = cache;
        this.monitor = monitor;
    }

    public static JayaRuntime standalone(SessionProvider sessions, JayaConfig config, ExternalPublisher publisher) {
        return wire(new MigratingSessionProvider(sessions, ModuleBootstrap.standalone()),
                publisher == null ? external(config) : publisher,
                config);
    }

    public static JayaRuntime jahia(JayaConfig config) {
        return wire(new MigratingSessionProvider(new JahiaSessionProvider(), ModuleBootstrap.jahia()),
                external(config),
                config);
    }

    public void warmup() {
        sessions.inSession(session -> null);
    }

    public EditorialService editorial() {
        return editorial;
    }

    public DiagnosticsService diagnostics() {
        return diagnostics;
    }

    public TokenAuthenticator authenticator() {
        return authenticator;
    }

    public AuthorizationPolicy authorization() {
        return authorization;
    }

    public CacheStore cache() {
        return cache;
    }

    public OperationMonitor monitor() {
        return monitor;
    }

    @Override
    public void close() {
        sessions.close();
    }

    private static JayaRuntime wire(SessionProvider sessions, ExternalPublisher publisher, JayaConfig config) {
        Clock clock = new SystemClock();
        CacheStore cache = new CaffeineCacheStore(Duration.ofSeconds(30), 500);
        OperationMonitor monitor = new OperationMonitor(Duration.ofMillis(config.slowThresholdMillis()), 20, clock);
        AuthorizationPolicy authorization = new AuthorizationPolicy();
        JcrArticleStore store = new JcrArticleStore(sessions, Duration.ofMillis(config.slowThresholdMillis()));
        EditorialService editorial = new DefaultEditorialService(
                store,
                store,
                publisher,
                cache,
                monitor,
                authorization,
                new InputGuard(),
                new Slf4jAuditLog(),
                clock,
                new RandomIds());
        DiagnosticsService diagnostics = new DiagnosticsService(authorization, cache, monitor);
        return new JayaRuntime(sessions, editorial, diagnostics, config.tokens().authenticator(),
                authorization, cache, monitor);
    }

    private static ExternalPublisher external(JayaConfig config) {
        if (config.externalUrl() == null) {
            return new NoopExternalPublisher();
        }
        return new HttpExternalPublisher(config.externalUrl(), config.externalToken(), config.externalTimeout());
    }
}
