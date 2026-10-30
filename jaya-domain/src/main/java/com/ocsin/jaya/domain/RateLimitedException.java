package com.ocsin.jaya.domain;

public final class RateLimitedException extends DomainException {

    public RateLimitedException() {
        super(429, "rate_limited", "Trop d'échecs d'authentification. Réessayez dans une minute.");
    }
}
