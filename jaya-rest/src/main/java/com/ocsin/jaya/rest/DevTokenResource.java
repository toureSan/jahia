package com.ocsin.jaya.rest;

import com.ocsin.jaya.domain.NotFoundException;
import com.ocsin.jaya.security.TokenDirectory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/api/v1/dev/tokens")
public final class DevTokenResource {

    private final boolean enabled;
    private final TokenDirectory tokens;

    public DevTokenResource(boolean enabled, TokenDirectory tokens) {
        this.enabled = enabled;
        this.tokens = tokens;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> tokens() {
        if (!enabled) {
            throw new NotFoundException("Ressource introuvable");
        }
        Map<String, String> body = new LinkedHashMap<>();
        body.put("reader", tokens.reader());
        body.put("editor", tokens.editor());
        body.put("publisher", tokens.publisher());
        body.put("admin", tokens.admin());
        return body;
    }
}
