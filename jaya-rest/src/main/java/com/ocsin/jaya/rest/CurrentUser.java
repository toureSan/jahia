package com.ocsin.jaya.rest;

import com.ocsin.jaya.domain.Principal;

import javax.ws.rs.container.ContainerRequestContext;

public final class CurrentUser {

    public static final String PRINCIPAL = "jaya.principal";
    public static final String AUTHENTICATED = "jaya.authenticated";

    private CurrentUser() {
    }

    public static Principal from(ContainerRequestContext request) {
        Object value = request.getProperty(PRINCIPAL);
        if (value instanceof Principal principal) {
            return principal;
        }
        return Principal.anonymous();
    }
}
