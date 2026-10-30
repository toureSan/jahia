package com.ocsin.jaya.domain;

import java.util.Locale;
import java.util.UUID;

/**
 * Identifiant opaque d'un article. Le format UUID empêche les chemins JCR forgés.
 */
public record ArticleId(String value) {

    public ArticleId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Identifiant article obligatoire");
        }
        try {
            UUID.fromString(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Identifiant article invalide");
        }
        value = value.toLowerCase(Locale.ROOT);
    }

    public static ArticleId generate() {
        return new ArticleId(UUID.randomUUID().toString());
    }

    public static ArticleId parse(String raw) {
        try {
            return new ArticleId(raw);
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Identifiant article invalide");
        }
    }
}
