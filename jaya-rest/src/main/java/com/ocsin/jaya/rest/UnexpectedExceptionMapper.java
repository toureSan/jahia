package com.ocsin.jaya.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public final class UnexpectedExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger log = LoggerFactory.getLogger(UnexpectedExceptionMapper.class);

    @Override
    public Response toResponse(Throwable exception) {
        if (exception instanceof WebApplicationException web) {
            return web.getResponse();
        }
        log.error("Erreur non gérée", exception);
        return Response.status(500)
                .type("application/problem+json")
                .entity(ProblemBody.internal())
                .build();
    }
}
