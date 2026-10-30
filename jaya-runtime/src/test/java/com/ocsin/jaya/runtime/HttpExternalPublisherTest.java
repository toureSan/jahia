package com.ocsin.jaya.runtime;

import com.ocsin.jaya.domain.Article;
import com.ocsin.jaya.domain.ArticleId;
import com.ocsin.jaya.domain.IntegrationException;
import com.ocsin.jaya.domain.PublicationStatus;
import com.ocsin.jaya.domain.UserId;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HttpExternalPublisherTest {

    @Test
    @DisplayName("un 503 est retenté, un 400 ne l'est pas, une redirection n'est pas suivie")
    void retriesOnlyTemporaryFailures() throws Exception {
        AtomicInteger hits = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/hooks", exchange -> {
            int hit = hits.incrementAndGet();
            byte[] body = exchange.getRequestBody().readAllBytes();
            assertThat(new String(body, StandardCharsets.UTF_8)).contains("Index");
            assertThat(exchange.getRequestHeaders().getFirst("Idempotency-Key")).isNotBlank();
            if (hit == 1) {
                exchange.sendResponseHeaders(503, -1);
            } else {
                exchange.sendResponseHeaders(204, -1);
            }
            exchange.close();
        });
        server.start();
        try {
            HttpExternalPublisher publisher = publisher(server, "/hooks");
            String externalId = publisher.publish(sample());
            assertThat(externalId).startsWith("ext-");
            assertThat(hits).hasValue(2);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void doesNotRetryClientErrors() throws Exception {
        AtomicInteger hits = new AtomicInteger();
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/bad", exchange -> {
            hits.incrementAndGet();
            exchange.sendResponseHeaders(400, -1);
            exchange.close();
        });
        server.start();
        try {
            assertThatThrownBy(() -> publisher(server, "/bad").publish(sample()))
                    .isInstanceOf(IntegrationException.class);
            assertThat(hits).hasValue(1);
        } finally {
            server.stop(0);
        }
    }

    @Test
    void escapesJson() {
        assertThat(JsonStrings.quote("a\"b\\c\n")).isEqualTo("\"a\\\"b\\\\c\\n\"");
    }

    private static HttpExternalPublisher publisher(HttpServer server, String path) {
        int port = server.getAddress().getPort();
        return new HttpExternalPublisher(java.net.URI.create("http://127.0.0.1:" + port + path), "t".repeat(32), Duration.ofSeconds(2));
    }

    private static Article sample() {
        return new Article(ArticleId.generate(), "Index", null, "Corps", PublicationStatus.PUBLISHED, "fr",
                List.of(), Instant.parse("2026-10-27T08:00:00Z"), new UserId("publisher"), null, null);
    }
}
