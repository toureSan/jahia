package com.ocsin.jaya.domain;

/**
 * Identifiant d'utilisateur sûr à écrire dans un journal d'audit.
 */
public record UserId(String value) {

    public UserId {
        if (value == null || !value.matches("[A-Za-z0-9._:-]{1,80}")) {
            throw new IllegalArgumentException("Identifiant utilisateur invalide");
        }
    }
}
