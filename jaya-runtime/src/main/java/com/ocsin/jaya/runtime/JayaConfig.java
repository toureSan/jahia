package com.ocsin.jaya.runtime;

import com.ocsin.jaya.security.TokenDirectory;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Configuration lue depuis l'environnement. Les jetons de production sont obligatoires.
 */
public record JayaConfig(
        String environment,
        String host,
        int port,
        Path dataDir,
        boolean devTokens,
        boolean seed,
        URI externalUrl,
        String externalToken,
        Duration externalTimeout,
        long slowThresholdMillis,
        List<String> corsOrigins,
        int graphqlMaxComplexity,
        int graphqlMaxDepth,
        boolean graphqlIntrospection,
        TokenDirectory tokens) {

    public JayaConfig {
        if (host == null || host.isBlank()) {
            throw new IllegalStateException("JAYA_HOST est obligatoire");
        }
        if (port < 0 || port > 65535) {
            throw new IllegalStateException("JAYA_PORT est invalide");
        }
        if (externalUrl != null) {
            String scheme = externalUrl.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new IllegalStateException("JAYA_EXTERNAL_URL doit utiliser http ou https");
            }
        }
        corsOrigins = List.copyOf(corsOrigins);
        if (devTokens && (tokens.reader() == null || tokens.editor() == null
                || tokens.publisher() == null || tokens.admin() == null)) {
            throw new IllegalStateException("Les jetons locaux sont activés mais incomplets");
        }
    }

    public static JayaConfig fromEnvironment() {
        return from(System.getenv());
    }

    public static JayaConfig from(Map<String, String> env) {
        String environment = value(env, "JAYA_ENV", "local");
        boolean production = "production".equalsIgnoreCase(environment);
        boolean devTokens = Boolean.parseBoolean(value(env, "JAYA_DEV_TOKENS", Boolean.toString(!production)));
        boolean seed = Boolean.parseBoolean(value(env, "JAYA_SEED", Boolean.toString(!production)));
        return build(env, environment, devTokens, seed, true);
    }

    public static JayaConfig jahia(Map<String, String> env) {
        return build(env, value(env, "JAYA_ENV", "jahia"), false, false, false);
    }

    public static JayaConfig forTests(int port) {
        return new JayaConfig(
                "test",
                "127.0.0.1",
                port,
                null,
                true,
                false,
                null,
                null,
                Duration.ofSeconds(2),
                50,
                List.of("http://127.0.0.1:5173", "http://localhost:5173"),
                40,
                8,
                false,
                TokenDirectory.generate());
    }

    private static JayaConfig build(Map<String, String> env, String environment, boolean devTokens, boolean seed, boolean requireTokens) {
        String rawDir = env.get("JAYA_DATA_DIR");
        Path dataDir = null;
        if (rawDir == null || rawDir.isBlank()) {
            dataDir = Path.of("data", "jaya");
        } else if (!"memory".equalsIgnoreCase(rawDir)) {
            dataDir = Path.of(rawDir);
        }
        String rawUrl = env.get("JAYA_EXTERNAL_URL");
        URI externalUrl = rawUrl == null || rawUrl.isBlank() ? null : URI.create(rawUrl.trim());
        TokenDirectory tokens = requireTokens ? tokens(env, devTokens) : TokenDirectory.inactive();
        return new JayaConfig(
                environment,
                value(env, "JAYA_HOST", "127.0.0.1"),
                Integer.parseInt(value(env, "JAYA_PORT", "8080")),
                dataDir,
                devTokens && requireTokens,
                seed,
                externalUrl,
                blankToNull(env.get("JAYA_EXTERNAL_TOKEN")),
                Duration.ofMillis(Long.parseLong(value(env, "JAYA_EXTERNAL_TIMEOUT_MS", "2000"))),
                Long.parseLong(value(env, "JAYA_SLOW_THRESHOLD_MS", "50")),
                origins(env.get("JAYA_CORS_ORIGINS")),
                Integer.parseInt(value(env, "JAYA_GRAPHQL_MAX_COMPLEXITY", "80")),
                Integer.parseInt(value(env, "JAYA_GRAPHQL_MAX_DEPTH", "8")),
                Boolean.parseBoolean(value(env, "JAYA_GRAPHQL_INTROSPECTION", "false")),
                tokens);
    }

    private static TokenDirectory tokens(Map<String, String> env, boolean devTokens) {
        if (!devTokens) {
            return TokenDirectory.of(
                    required(env, "JAYA_TOKEN_READER"),
                    required(env, "JAYA_TOKEN_EDITOR"),
                    required(env, "JAYA_TOKEN_PUBLISHER"),
                    required(env, "JAYA_TOKEN_ADMIN"));
        }
        return TokenDirectory.of(
                orRandom(env, "JAYA_TOKEN_READER"),
                orRandom(env, "JAYA_TOKEN_EDITOR"),
                orRandom(env, "JAYA_TOKEN_PUBLISHER"),
                orRandom(env, "JAYA_TOKEN_ADMIN"));
    }

    private static String orRandom(Map<String, String> env, String name) {
        String value = env.get(name);
        if (value == null || value.isBlank()) {
            return TokenDirectory.generate().reader();
        }
        return value.trim();
    }

    private static String required(Map<String, String> env, String name) {
        String value = env.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " est obligatoire quand JAYA_DEV_TOKENS=false");
        }
        return value.trim();
    }

    private static List<String> origins(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of("http://127.0.0.1:5173", "http://localhost:5173");
        }
        List<String> origins = new ArrayList<>();
        for (String part : raw.split(",")) {
            if (!part.isBlank()) {
                origins.add(part.trim());
            }
        }
        return origins;
    }

    private static String value(Map<String, String> env, String name, String fallback) {
        String value = env.get(name);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
