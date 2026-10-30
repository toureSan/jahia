package com.ocsin.jaya.rest;

import com.ocsin.jaya.application.DiagnosticsService;
import com.ocsin.jaya.application.perf.DiagnosticsSnapshot;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

@Path("/api/v1/diagnostics")
public final class DiagnosticsResource {

    private final DiagnosticsService diagnostics;

    public DiagnosticsResource(DiagnosticsService diagnostics) {
        this.diagnostics = diagnostics;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public DiagnosticsSnapshot snapshot(@Context ContainerRequestContext request) {
        return diagnostics.snapshot(CurrentUser.from(request));
    }
}
