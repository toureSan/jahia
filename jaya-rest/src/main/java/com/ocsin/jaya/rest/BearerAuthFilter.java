package com.ocsin.jaya.rest;

import com.ocsin.jaya.domain.AuthenticationException;
import com.ocsin.jaya.domain.Principal;
import com.ocsin.jaya.security.TokenAuthenticator;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import java.util.Optional;

/**
 * Jeton Bearer stateless. L'absence de jeton ouvre une session lecteur anonyme.
 */
public final class BearerAuthFilter implements ContainerRequestFilter {

    private final TokenAuthenticator authenticator;

    public BearerAuthFilter(TokenAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public void filter(ContainerRequestContext request) {
        String header = request.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (header == null || header.isBlank()) {
            request.setProperty(CurrentUser.PRINCIPAL, Principal.anonymous());
            return;
        }
        if (!header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            throw new AuthenticationException();
        }
        String token = header.substring(7).trim();
        Optional<Principal> principal = authenticator.authenticate(token);
        if (principal.isEmpty()) {
            throw new AuthenticationException();
        }
        request.setProperty(CurrentUser.PRINCIPAL, principal.get());
        request.setProperty(CurrentUser.AUTHENTICATED, Boolean.TRUE);
    }
}
