package com.ocsin.jaya.runtime;

import com.ocsin.jaya.application.port.ExternalPublisher;
import com.ocsin.jaya.domain.Article;
import com.ocsin.jaya.domain.IntegrationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Notifie un système externe sans suivre les redirections ni relire un corps non borné.
 */
public final class HttpExternalPublisher implements ExternalPublisher {

    private static final Logger log = LoggerFactory.getLogger(HttpExternalPublisher.class);

    private final URI endpoint;
    private final String token;
    private final HttpClient client;
    private final Duration timeout;

    public HttpExternalPublisher(URI endpoint, String token, Duration timeout) {
        this.endpoint = endpoint;
        this.token = token;
        this.timeout = timeout;
        this.client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }

    @Override
    public String publish(Article article) {
        exchange("POST", article);
        return "ext-" + article.id().value();
    }

    @Override
    public void retract(Article article) {
        exchange("DELETE", article);
    }

    private void exchange(String method, Article article) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(endpoint)
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "jaya/1.0")
                .header("Idempotency-Key", article.id().value());
        if (token != null && !token.isBlank()) {
            builder.header("Authorization", "Bearer " + token);
        }
        if ("DELETE".equals(method)) {
            builder.DELETE();
        } else {
            builder.POST(HttpRequest.BodyPublishers.ofString(body(article)));
        }
        HttpRequest request = builder.build();
        IOException last = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    return;
                }
                if ("DELETE".equals(method) && status == 404) {
                    return;
                }
                if (status != 502 && status != 503 && status != 504) {
                    throw new IntegrationException("Le système externe a répondu " + status);
                }
                log.warn("Tentative {} refusée par le système externe ({})", attempt, status);
            } catch (IOException ex) {
                last = ex;
                log.warn("Tentative {} impossible vers le système externe", attempt);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new IntegrationException("Synchronisation interrompue", ex);
            }
            if (attempt < 3) {
                sleep(Duration.ofMillis(50L * attempt));
            }
        }
        throw new IntegrationException("Le système externe est injoignable", last);
    }

    private static String body(Article article) {
        return "{"
                + "\"id\":" + JsonStrings.quote(article.id().value())
                + ",\"title\":" + JsonStrings.quote(article.title())
                + ",\"summary\":" + JsonStrings.quote(article.summary())
                + ",\"locale\":" + JsonStrings.quote(article.locale())
                + ",\"status\":" + JsonStrings.quote(article.status().name())
                + "}";
    }

    private static void sleep(Duration delay) {
        try {
            Thread.sleep(delay.toMillis());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IntegrationException("Synchronisation interrompue", ex);
        }
    }
}
