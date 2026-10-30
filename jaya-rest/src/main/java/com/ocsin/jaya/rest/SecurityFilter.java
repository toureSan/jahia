package com.ocsin.jaya.rest;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import java.util.List;
import java.util.UUID;

/**
 * En-têtes OWASP et origine CORS explicite. Pas de joker.
 */
public final class SecurityFilter implements ContainerResponseFilter {

    private final List<String> allowedOrigins;

    public SecurityFilter(List<String> allowedOrigins) {
        this.allowedOrigins = List.copyOf(allowedOrigins);
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        Object requestId = request.getProperty("jaya.requestId");
        if (requestId == null) {
            requestId = UUID.randomUUID().toString();
        }
        response.getHeaders().putSingle("X-Request-Id", requestId.toString());
        response.getHeaders().putSingle("X-Content-Type-Options", "nosniff");
        response.getHeaders().putSingle("X-Frame-Options", "DENY");
        response.getHeaders().putSingle("Referrer-Policy", "no-referrer");
        response.getHeaders().putSingle("Cache-Control", "no-store");
        response.getHeaders().putSingle("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'");
        String origin = request.getHeaderString("Origin");
        if (origin != null && allowedOrigins.contains(origin)) {
            response.getHeaders().putSingle("Access-Control-Allow-Origin", origin);
            response.getHeaders().putSingle("Vary", "Origin");
            response.getHeaders().putSingle("Access-Control-Allow-Headers", "Authorization, Content-Type, X-Request-Id");
            response.getHeaders().putSingle("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            response.getHeaders().putSingle("Access-Control-Max-Age", "600");
        }
    }
}
