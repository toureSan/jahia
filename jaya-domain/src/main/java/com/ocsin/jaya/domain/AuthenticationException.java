package com.ocsin.jaya.domain;

public final class AuthenticationException extends DomainException {

    public AuthenticationException() {
        super(401, "unauthenticated", "Jeton d'authentification invalide");
    }
}
