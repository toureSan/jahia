package com.ocsin.jaya.graphql;

import com.ocsin.jaya.domain.ValidationException;
import com.ocsin.jaya.rest.CurrentUser;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Map;

@Path("/graphql")
public final class GraphQLResource {

    private final GraphQLProvider graphql;

    public GraphQLResource(GraphQLProvider graphql) {
        this.graphql = graphql;
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> execute(@Context ContainerRequestContext request, GraphQLRequest body) {
        if (body == null || body.getQuery() == null || body.getQuery().isBlank()) {
            throw new ValidationException("La requête GraphQL est vide");
        }
        if (body.getQuery().length() > 8_000) {
            throw new ValidationException("La requête GraphQL est trop volumineuse");
        }
        return graphql.execute(body.getQuery(), body.getOperationName(), body.getVariables(), CurrentUser.from(request));
    }
}
