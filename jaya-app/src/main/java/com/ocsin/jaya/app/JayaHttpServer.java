package com.ocsin.jaya.app;

import com.ocsin.jaya.application.SystemClock;
import com.ocsin.jaya.graphql.GraphQLProvider;
import com.ocsin.jaya.graphql.GraphQLResource;
import com.ocsin.jaya.rest.ArticleResource;
import com.ocsin.jaya.rest.BearerAuthFilter;
import com.ocsin.jaya.rest.DevTokenResource;
import com.ocsin.jaya.rest.DiagnosticsResource;
import com.ocsin.jaya.rest.DomainExceptionMapper;
import com.ocsin.jaya.rest.HealthResource;
import com.ocsin.jaya.rest.SecurityFilter;
import com.ocsin.jaya.rest.UnexpectedExceptionMapper;
import com.ocsin.jaya.runtime.JayaConfig;
import com.ocsin.jaya.runtime.JayaRuntime;
import com.ocsin.jaya.security.FailureThrottle;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

/**
 * Serveur HTTP local. Jahia expose les mêmes ressources via CXF lorsque le bundle est installé.
 */
public final class JayaHttpServer implements AutoCloseable {

    private final HttpServer server;

    private JayaHttpServer(HttpServer server) {
        this.server = server;
    }

    public static JayaHttpServer start(JayaRuntime runtime, JayaConfig config) {
        ResourceConfig resources = new ResourceConfig();
        resources.register(new ArticleResource(runtime.editorial()));
        resources.register(new DiagnosticsResource(runtime.diagnostics()));
        resources.register(new HealthResource());
        resources.register(new DevTokenResource(config.devTokens(), config.tokens()));
        resources.register(new GraphQLResource(new GraphQLProvider(
                runtime.editorial(),
                runtime.diagnostics(),
                runtime.monitor(),
                config.graphqlMaxComplexity(),
                config.graphqlMaxDepth())));
        resources.register(new ThrottledAuthFilter(
                new BearerAuthFilter(runtime.authenticator()),
                new FailureThrottle(20, Duration.ofMinutes(1), new SystemClock())));
        resources.register(new SecurityFilter(config.corsOrigins()));
        resources.register(DomainExceptionMapper.class);
        resources.register(UnexpectedExceptionMapper.class);
        resources.register(JsonContext.class);
        resources.register(JacksonFeature.class);
        resources.register(new BodyLimit());
        URI base = URI.create("http://" + config.host() + ":" + config.port() + "/");
        HttpServer http = GrizzlyHttpServerFactory.createHttpServer(base, resources, false);
        try {
            http.start();
        } catch (IOException ex) {
            throw new IllegalStateException("Démarrage HTTP impossible sur " + base, ex);
        }
        return new JayaHttpServer(http);
    }

    @Override
    public void close() {
        server.shutdownNow();
    }
}
