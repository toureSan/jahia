package com.ocsin.jaya.application;

import com.ocsin.jaya.domain.Article;
import com.ocsin.jaya.domain.ArticleId;
import com.ocsin.jaya.domain.Principal;

/**
 * Façade des cas d'usage éditoriaux, indépendante de JCR, GraphQL et REST.
 */
public interface EditorialService {

    Article find(Principal principal, ArticleId id);

    SearchResult search(Principal principal, SearchCriteria criteria);

    Article save(Principal principal, SaveArticle command);

    Article submit(Principal principal, ArticleId id);

    Article publish(Principal principal, ArticleId id);

    Article reject(Principal principal, ArticleId id);

    Article sync(Principal principal, ArticleId id);
}
