package com.ocsin.jaya.rest;

import com.ocsin.jaya.domain.DomainException;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public final class DomainExceptionMapper implements ExceptionMapper<DomainException> {

    @Override
    public Response toResponse(DomainException exception) {
        Response.ResponseBuilder builder = Response.status(exception.status())
                .type("application/problem+json")
                .entity(ProblemBody.of(exception));
        if (exception.status() == 429) {
            builder.header("Retry-After", "60");
        }
        return builder.build();
    }
}
