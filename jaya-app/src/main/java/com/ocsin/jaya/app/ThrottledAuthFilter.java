package com.ocsin.jaya.app;

import com.ocsin.jaya.domain.AuthenticationException;
import com.ocsin.jaya.rest.BearerAuthFilter;
import com.ocsin.jaya.rest.CurrentUser;
import com.ocsin.jaya.security.FailureThrottle;
import org.glassfish.grizzly.http.server.Request;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;

/**
 * Compte les jetons refusés par adresse cliente et laisse passer le prévol CORS.
 */
public final class ThrottledAuthFilter implements ContainerRequestFilter {

    private final BearerAuthFilter delegate;
    private final FailureThrottle throttle;

    public ThrottledAuthFilter(BearerAuthFilter delegate, FailureThrottle throttle) {
        this.delegate = delegate;
        this.throttle = throttle;
    }

    @Override
    public void filter(ContainerRequestContext request) {
        if ("OPTIONS".equals(request.getMethod())) {
            request.abortWith(Response.noContent().build());
            return;
        }
        String client = clientAddress(request);
        throttle.check(client);
        try {
            delegate.filter(request);
        } catch (AuthenticationException ex) {
            throttle.recordFailure(client);
            throw ex;
        }
        if (Boolean.TRUE.equals(request.getProperty(CurrentUser.AUTHENTICATED))) {
            throttle.reset(client);
        }
    }

    private static String clientAddress(ContainerRequestContext request) {
        Object value = request.getProperty(Request.class.getName());
        if (value instanceof Request grizzly) {
            String address = grizzly.getRemoteAddr();
            if (address != null && !address.isBlank()) {
                return address;
            }
        }
        return "local";
    }
}
