package com.ocsin.jaya.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenAuthenticatorTest {

    @Test
    @DisplayName("seul le jeton exact ouvre le rôle correspondant")
    void matchesTheRightToken() {
        TokenDirectory directory = TokenDirectory.of("r".repeat(32), "e".repeat(32), "p".repeat(32), "a".repeat(32));
        TokenAuthenticator authenticator = directory.authenticator();

        assertThat(authenticator.authenticate("e".repeat(32))).contains(directory.authenticator().authenticate("e".repeat(32)).orElseThrow());
        assertThat(authenticator.authenticate("e".repeat(31) + "x")).isEmpty();
        assertThat(authenticator.authenticate("short")).isEmpty();
        assertThat(authenticator.authenticate(null)).isEmpty();
    }
}
