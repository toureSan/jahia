package com.ocsin.jaya.domain;

/**
 * Cycle de vie d'un article : brouillon, relecture, puis publication.
 */
public enum PublicationStatus {
    DRAFT,
    IN_REVIEW,
    PUBLISHED
}
