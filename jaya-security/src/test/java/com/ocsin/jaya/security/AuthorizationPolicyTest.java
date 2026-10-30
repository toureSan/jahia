package com.ocsin.jaya.security;

import com.ocsin.jaya.domain.Action;
import com.ocsin.jaya.domain.ForbiddenException;
import com.ocsin.jaya.domain.Principal;
import com.ocsin.jaya.domain.Role;
import com.ocsin.jaya.domain.UserId;
import com.ocsin.jaya.domain.Visibility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthorizationPolicyTest {

    private final AuthorizationPolicy policy = new AuthorizationPolicy();

    @Test
    @DisplayName("un lecteur ne publie pas et ne voit pas les brouillons")
    void readerIsPublicOnly() {
        Principal reader = principal("reader", Role.READER);
        assertThat(policy.visibility(reader)).isEqualTo(Visibility.PUBLISHED_ONLY);
        assertThatThrownBy(() -> policy.check(reader, Action.PUBLISH)).isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> policy.check(reader, Action.DIAGNOSE)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("un éditeur rédige, un publisher publie, un admin diagnostique")
    void rolesAreSeparated() {
        assertThat(policy.allows(principal("editor", Role.EDITOR), Action.SAVE)).isTrue();
        assertThat(policy.allows(principal("editor", Role.EDITOR), Action.PUBLISH)).isFalse();
        assertThat(policy.allows(principal("publisher", Role.PUBLISHER), Action.PUBLISH)).isTrue();
        assertThat(policy.allows(principal("publisher", Role.PUBLISHER), Action.DIAGNOSE)).isFalse();
        assertThat(policy.allows(principal("admin", Role.ADMIN), Action.DIAGNOSE)).isTrue();
    }

    private static Principal principal(String user, Role role) {
        return new Principal(new UserId(user), EnumSet.of(role));
    }
}
