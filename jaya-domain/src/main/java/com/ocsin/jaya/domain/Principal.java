package com.ocsin.jaya.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Utilisateur authentifié, ou visiteur anonyme limité à la lecture publique.
 */
public record Principal(UserId userId, Set<Role> roles) {

    public Principal {
        if (userId == null) {
            throw new IllegalArgumentException("Utilisateur obligatoire");
        }
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("Au moins un rôle est obligatoire");
        }
        roles = Set.copyOf(roles);
    }

    public boolean has(Role role) {
        return roles.contains(role);
    }

    public boolean hasAny(Role... candidates) {
        for (Role role : candidates) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public static Principal anonymous() {
        return new Principal(new UserId("anonymous"), EnumSet.of(Role.READER));
    }

    public static Principal system() {
        return new Principal(new UserId("jaya-system"), EnumSet.allOf(Role.class));
    }
}
