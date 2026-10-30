package com.ocsin.jaya.rest;

import com.ocsin.jaya.application.EditorialService;
import com.ocsin.jaya.application.SearchCriteria;
import com.ocsin.jaya.application.view.ArticleView;
import com.ocsin.jaya.application.view.SearchView;
import com.ocsin.jaya.domain.ArticleId;
import com.ocsin.jaya.domain.PublicationStatus;
import com.ocsin.jaya.domain.ValidationException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.Locale;

@Path("/api/v1/articles")
public final class ArticleResource {

    private final EditorialService editorial;

    public ArticleResource(EditorialService editorial) {
        this.editorial = editorial;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SearchView search(@Context ContainerRequestContext request,
                             @QueryParam("q") String term,
                             @QueryParam("status") String status,
                             @QueryParam("locale") String locale,
                             @QueryParam("limit") Integer limit,
                             @QueryParam("offset") Integer offset) {
        return SearchView.of(editorial.search(CurrentUser.from(request), new SearchCriteria(
                term,
                parseStatus(status),
                locale,
                limit == null ? 20 : limit,
                offset == null ? 0 : offset)));
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ArticleView find(@Context ContainerRequestContext request, @PathParam("id") String id) {
        return ArticleView.of(editorial.find(CurrentUser.from(request), ArticleId.parse(id)));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ArticleView save(@Context ContainerRequestContext request, SaveArticleRequest body) {
        if (body == null) {
            throw new ValidationException("Le corps de la requête est vide");
        }
        return ArticleView.of(editorial.save(CurrentUser.from(request), body.toCommand()));
    }

    @POST
    @Path("/{id}/submit")
    @Produces(MediaType.APPLICATION_JSON)
    public ArticleView submit(@Context ContainerRequestContext request, @PathParam("id") String id) {
        return ArticleView.of(editorial.submit(CurrentUser.from(request), ArticleId.parse(id)));
    }

    @POST
    @Path("/{id}/publish")
    @Produces(MediaType.APPLICATION_JSON)
    public ArticleView publish(@Context ContainerRequestContext request, @PathParam("id") String id) {
        return ArticleView.of(editorial.publish(CurrentUser.from(request), ArticleId.parse(id)));
    }

    @POST
    @Path("/{id}/reject")
    @Produces(MediaType.APPLICATION_JSON)
    public ArticleView reject(@Context ContainerRequestContext request, @PathParam("id") String id) {
        return ArticleView.of(editorial.reject(CurrentUser.from(request), ArticleId.parse(id)));
    }

    @POST
    @Path("/{id}/sync")
    @Produces(MediaType.APPLICATION_JSON)
    public ArticleView sync(@Context ContainerRequestContext request, @PathParam("id") String id) {
        return ArticleView.of(editorial.sync(CurrentUser.from(request), ArticleId.parse(id)));
    }

    private static PublicationStatus parseStatus(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return PublicationStatus.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ValidationException("Statut inconnu");
        }
    }
}
