package com.ocsin.jaya.jcr;

import com.ocsin.jaya.application.SearchCriteria;
import com.ocsin.jaya.domain.PublicationStatus;
import com.ocsin.jaya.domain.Visibility;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Requête JCR-SQL2 paramétrée. Le terme utilisateur n'est jamais concaténé.
 */
public final class ArticleQuery {

    private final String select;
    private final String count;
    private final Map<String, String> bindings;

    private ArticleQuery(String select, String count, Map<String, String> bindings) {
        this.select = select;
        this.count = count;
        this.bindings = Map.copyOf(bindings);
    }

    public static ArticleQuery build(SearchCriteria criteria, Visibility visibility) {
        StringBuilder where = new StringBuilder();
        Map<String, String> bindings = new LinkedHashMap<>();
        if (visibility == Visibility.PUBLISHED_ONLY) {
            constraint(where, "a.[jaya:status] = $status");
            bindings.put("status", PublicationStatus.PUBLISHED.name());
        } else if (criteria.status() != null) {
            constraint(where, "a.[jaya:status] = $status");
            bindings.put("status", criteria.status().name());
        }
        if (criteria.locale() != null) {
            constraint(where, "a.[jaya:locale] = $locale");
            bindings.put("locale", criteria.locale());
        }
        if (criteria.term() != null) {
            constraint(where, "(a.[jaya:titleLower] LIKE $term OR a.[jaya:bodyLower] LIKE $term)");
            bindings.put("term", LikePatterns.contains(criteria.term()));
        }
        String filter = where.toString();
        String select = "SELECT * FROM [jaya:article] AS a" + filter
                + " ORDER BY a.[jaya:updatedAt] DESC, a.[jaya:id] ASC";
        String count = "SELECT a.[jaya:id] FROM [jaya:article] AS a" + filter;
        return new ArticleQuery(select, count, bindings);
    }

    public String select() {
        return select;
    }

    public String count() {
        return count;
    }

    public Map<String, String> bindings() {
        return bindings;
    }

    private static void constraint(StringBuilder where, String clause) {
        where.append(where.isEmpty() ? " WHERE " : " AND ").append(clause);
    }
}
