package com.ocsin.jaya.domain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Article éditorial immuable. Chaque transition renvoie une nouvelle révision.
 */
public record Article(
        ArticleId id,
        String title,
        String summary,
        String body,
        PublicationStatus status,
        String locale,
        List<String> tags,
        Instant updatedAt,
        UserId updatedBy,
        String externalId,
        String syncError) {

    public Article {
        if (id == null || title == null || title.isBlank() || body == null || body.isBlank()) {
            throw new IllegalArgumentException("Article incomplet");
        }
        if (status == null || locale == null || locale.isBlank() || updatedAt == null || updatedBy == null) {
            throw new IllegalArgumentException("Article incomplet");
        }
        updatedAt = updatedAt.truncatedTo(ChronoUnit.MILLIS);
        tags = sanitizeTags(tags);
        title = title.trim();
        body = body.trim();
        locale = locale.trim();
    }

    public Article withWorkflow(PublicationStatus nextStatus, Instant when, UserId actor,
                                String nextExternalId, String nextSyncError) {
        return new Article(id, title, summary, body, nextStatus, locale, tags, when, actor,
                nextExternalId, nextSyncError);
    }

    public Article withIntegration(String nextExternalId, String nextSyncError) {
        return new Article(id, title, summary, body, status, locale, tags, updatedAt, updatedBy,
                nextExternalId, nextSyncError);
    }

    private static List<String> sanitizeTags(List<String> raw) {
        if (raw == null || raw.isEmpty()) {
            return List.of();
        }
        List<String> copy = new ArrayList<>();
        for (String tag : raw) {
            if (tag == null || tag.isBlank()) {
                throw new IllegalArgumentException("Tag vide");
            }
            copy.add(tag);
        }
        return List.copyOf(copy);
    }
}
