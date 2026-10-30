package com.ocsin.jaya.security;

import com.ocsin.jaya.domain.Principal;
import com.ocsin.jaya.domain.Role;
import com.ocsin.jaya.domain.UserId;

import java.security.SecureRandom;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Jetons de démo ou d'intégration. Ils ne sont jamais écrits dans les journaux.
 */
public final class TokenDirectory {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final String reader;
    private final String editor;
    private final String publisher;
    private final String admin;
    private final Map<String, Principal> byToken;

    private TokenDirectory(String reader, String editor, String publisher, String admin, Map<String, Principal> byToken) {
        this.reader = reader;
        this.editor = editor;
        this.publisher = publisher;
        this.admin = admin;
        this.byToken = Map.copyOf(byToken);
    }

    public static TokenDirectory generate() {
        return of(randomToken(), randomToken(), randomToken(), randomToken());
    }

    public static TokenDirectory of(String reader, String editor, String publisher, String admin) {
        validate(reader);
        validate(editor);
        validate(publisher);
        validate(admin);
        Map<String, Principal> tokens = new LinkedHashMap<>();
        tokens.put(reader, principal("reader", Role.READER));
        tokens.put(editor, principal("editor", Role.EDITOR));
        tokens.put(publisher, principal("publisher", Role.PUBLISHER));
        tokens.put(admin, principal("admin", Role.ADMIN));
        return new TokenDirectory(reader, editor, publisher, admin, tokens);
    }

    public static TokenDirectory inactive() {
        return new TokenDirectory(null, null, null, null, Map.of());
    }

    public TokenAuthenticator authenticator() {
        return new TokenAuthenticator(byToken);
    }

    public String reader() {
        return reader;
    }

    public String editor() {
        return editor;
    }

    public String publisher() {
        return publisher;
    }

    public String admin() {
        return admin;
    }

    private static Principal principal(String user, Role role) {
        return new Principal(new UserId(user), EnumSet.of(role));
    }

    private static void validate(String token) {
        if (token == null || token.length() < 32) {
            throw new IllegalArgumentException("Un jeton doit contenir au moins 32 caractères");
        }
        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (c <= ' ' || c == 0x7f) {
                throw new IllegalArgumentException("Un jeton contient des caractères interdits");
            }
        }
    }

    private static String randomToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return HexFormat.of().formatHex(bytes);
    }
}
