package com.ocsin.jaya.security;

import com.ocsin.jaya.domain.Principal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Optional;

/**
 * Compare les jetons après hachage pour ne pas s'arrêter à la première différence.
 */
public final class TokenAuthenticator {

    private final Map<String, Principal> byToken;

    public TokenAuthenticator(Map<String, Principal> byToken) {
        this.byToken = Map.copyOf(byToken);
    }

    public Optional<Principal> authenticate(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        byte[] presented = sha256(token);
        Principal found = null;
        for (Map.Entry<String, Principal> entry : byToken.entrySet()) {
            if (MessageDigest.isEqual(presented, sha256(entry.getKey()))) {
                found = entry.getValue();
            }
        }
        return Optional.ofNullable(found);
    }

    private static byte[] sha256(String value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 indisponible", ex);
        }
    }
}
