package com.ocsin.jaya.app;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ocsin.jaya.application.port.ExternalPublisher;
import com.ocsin.jaya.domain.Article;
import com.ocsin.jaya.runtime.JayaConfig;
import com.ocsin.jaya.runtime.JsonStrings;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class JayaApplicationTest {

    private final ObjectMapper json = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @Test
    @DisplayName("le parcours éditorial protège les brouillons et borne GraphQL")
    void editorialFlow() throws Exception {
        int port;
        try (ServerSocket socket = new ServerSocket(0)) {
            port = socket.getLocalPort();
        }
        AtomicInteger pushes = new AtomicInteger();
        ExternalPublisher publisher = new ExternalPublisher() {
            @Override
            public String publish(Article article) {
                pushes.incrementAndGet();
                return "ext-flow";
            }

            @Override
            public void retract(Article article) {
            }
        };
        JayaConfig config = JayaConfig.forTests(port);
        JayaHandle handle = JayaApplication.boot(config, publisher);
        try {
            HttpResult health = send(port, "GET", "/api/v1/health", null, null);
            assertThat(health.status).isEqualTo(200);
            assertThat(health.header("X-Content-Type-Options")).isEqualTo("nosniff");
            assertThat(health.header("X-Frame-Options")).isEqualTo("DENY");

            HttpResult tokens = send(port, "GET", "/api/v1/dev/tokens", null, null);
            assertThat(tokens.status).isEqualTo(200);
            assertThat(tokens.body).contains(config.tokens().editor());

            HttpResult created = send(port, "POST", "/api/v1/articles", config.tokens().editor(),
                    "{\"title\":\"Contrat visible\",\"summary\":\"API\",\"body\":\"Texte stable\",\"locale\":\"fr\",\"tags\":[\"api\"]}");
            assertThat(created.status).isEqualTo(200);
            String id = json.readTree(created.body).get("id").asText();

            HttpResult hidden = send(port, "GET", "/api/v1/articles/" + id, config.tokens().reader(), null);
            assertThat(hidden.status).isEqualTo(404);
            assertThat(hidden.body).doesNotContain("at com.ocsin");

            HttpResult empty = send(port, "GET", "/api/v1/articles?q=contrat", config.tokens().reader(), null);
            assertThat(json.readTree(empty.body).get("total").asInt()).isZero();

            assertThat(send(port, "POST", "/api/v1/articles/" + id + "/submit", config.tokens().editor(), null).status)
                    .isEqualTo(200);
            HttpResult published = send(port, "POST", "/api/v1/articles/" + id + "/publish", config.tokens().publisher(), null);
            assertThat(published.status).isEqualTo(200);
            assertThat(json.readTree(published.body).get("externalId").asText()).isEqualTo("ext-flow");
            assertThat(pushes).hasValue(1);

            HttpResult visible = send(port, "GET", "/api/v1/articles/" + id, null, null);
            assertThat(visible.status).isEqualTo(200);

            HttpResult found = send(port, "POST", "/graphql", null,
                    "{\"query\":" + JsonStrings.quote("query { search(term: \"contrat\") { total items { title } } }") + "}");
            assertThat(found.status).isEqualTo(200);
            JsonNode data = json.readTree(found.body);
            assertThat(data.get("errors")).isNull();
            assertThat(data.get("data").toString()).contains("Contrat visible");

            StringBuilder fat = new StringBuilder("query {");
            for (int i = 0; i < 30; i++) {
                fat.append(" a").append(i).append(": search { total items { title } }");
            }
            fat.append(" }");
            HttpResult rejected = send(port, "POST", "/graphql", null,
                    "{\"query\":" + JsonStrings.quote(fat.toString()) + "}");
            assertThat(json.readTree(rejected.body).get("errors")).isNotNull();

            assertThat(send(port, "GET", "/api/v1/diagnostics", config.tokens().reader(), null).status).isEqualTo(403);
            HttpResult diagnostics = send(port, "GET", "/api/v1/diagnostics", config.tokens().admin(), null);
            assertThat(diagnostics.status).isEqualTo(200);
            assertThat(diagnostics.body).contains("hitCount");

            HttpResult invalid = send(port, "GET", "/api/v1/articles", "x".repeat(32), null);
            assertThat(invalid.status).isEqualTo(401);
            assertThat(invalid.body).doesNotContain("at com.ocsin");

            String oversizedBody = "{\"title\":\"" + "a".repeat(70_000) + "\"}";
            HttpResult oversized = send(port, "POST", "/api/v1/articles", config.tokens().editor(), oversizedBody);
            assertThat(oversized.status).isEqualTo(413);
            assertThat(oversized.body).contains("64 Ko");

            HttpRequest preflight = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/v1/articles"))
                    .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
                    .header("Origin", "http://127.0.0.1:5173")
                    .header("Access-Control-Request-Method", "POST")
                    .build();
            HttpResponse<String> cors = client.send(preflight, HttpResponse.BodyHandlers.ofString());
            assertThat(cors.statusCode()).isEqualTo(204);
            assertThat(cors.headers().firstValue("Access-Control-Allow-Origin")).contains("http://127.0.0.1:5173");
        } finally {
            handle.close();
        }
    }

    private HttpResult send(int port, String method, String path, String token, String body) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path))
                .timeout(Duration.ofSeconds(10));
        if (token != null) {
            builder.header("Authorization", "Bearer " + token);
        }
        if (body != null) {
            builder.header("Content-Type", "application/json");
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
        } else if ("POST".equals(method)) {
            builder.method(method, HttpRequest.BodyPublishers.noBody());
        } else {
            builder.GET();
        }
        HttpResponse<String> response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        return new HttpResult(response.statusCode(), response.body(), response.headers());
    }

    private record HttpResult(int status, String body, java.net.http.HttpHeaders headers) {
        private String header(String name) {
            return headers.firstValue(name).orElse("");
        }
    }
}
