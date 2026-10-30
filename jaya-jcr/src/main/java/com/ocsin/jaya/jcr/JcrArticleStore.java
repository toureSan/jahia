package com.ocsin.jaya.jcr;

import com.ocsin.jaya.application.ContentAccessException;
import com.ocsin.jaya.application.SearchCriteria;
import com.ocsin.jaya.application.SearchResult;
import com.ocsin.jaya.application.port.ArticleRepository;
import com.ocsin.jaya.application.port.ArticleSearch;
import com.ocsin.jaya.domain.Article;
import com.ocsin.jaya.domain.ArticleId;
import com.ocsin.jaya.domain.PublicationStatus;
import com.ocsin.jaya.domain.UserId;
import com.ocsin.jaya.domain.Visibility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Persiste les articles sous /articles/{id} et les retrouve en JCR-SQL2.
 */
public final class JcrArticleStore implements ArticleRepository, ArticleSearch {

    private static final Logger log = LoggerFactory.getLogger(JcrArticleStore.class);
    private static final int MAX_COUNT = 5_000;

    private final SessionProvider sessions;
    private final long slowQueryNanos;
    private volatile String lastExplainPlan = "";

    public JcrArticleStore(SessionProvider sessions) {
        this(sessions, Duration.ofMillis(50));
    }

    public JcrArticleStore(SessionProvider sessions, Duration slowQuery) {
        this.sessions = sessions;
        this.slowQueryNanos = slowQuery.toNanos();
    }

    @Override
    public Optional<Article> findById(ArticleId id) {
        return sessions.inSession(session -> {
            Node parent = articles(session, false);
            if (parent == null || !parent.hasNode(id.value())) {
                return Optional.empty();
            }
            return Optional.of(map(parent.getNode(id.value())));
        });
    }

    @Override
    public void save(Article article) {
        sessions.inSession(session -> {
            Node parent = articles(session, true);
            Node node = parent.hasNode(article.id().value())
                    ? parent.getNode(article.id().value())
                    : parent.addNode(article.id().value(), "jaya:article");
            node.setProperty("jaya:id", article.id().value());
            node.setProperty("jaya:title", article.title());
            node.setProperty("jaya:titleLower", TextNorm.searchable(article.title()));
            node.setProperty("jaya:body", article.body());
            node.setProperty("jaya:bodyLower", TextNorm.searchable(article.body()));
            node.setProperty("jaya:status", article.status().name());
            node.setProperty("jaya:locale", article.locale());
            node.setProperty("jaya:updatedAt", article.updatedAt().toEpochMilli());
            node.setProperty("jaya:updatedBy", article.updatedBy().value());
            writeOptional(node, "jaya:summary", article.summary());
            writeOptional(node, "jaya:externalId", article.externalId());
            writeOptional(node, "jaya:syncError", article.syncError());
            if (article.tags().isEmpty()) {
                if (node.hasProperty("jaya:tags")) {
                    node.getProperty("jaya:tags").remove();
                }
            } else {
                node.setProperty("jaya:tags", article.tags().toArray(new String[0]), PropertyType.STRING);
            }
            session.save();
            return null;
        });
    }

    @Override
    public SearchResult search(SearchCriteria criteria, Visibility visibility) {
        int limit = Math.min(Math.max(criteria.limit(), 1), 50);
        int offset = Math.min(Math.max(criteria.offset(), 0), 1_000);
        return sessions.inSession(session -> {
            if (articles(session, false) == null) {
                return SearchResult.empty();
            }
            ArticleQuery built = ArticleQuery.build(criteria, visibility);
            QueryManager queries = session.getWorkspace().getQueryManager();
            long start = System.nanoTime();
            Query page = bind(queries.createQuery(built.select(), Query.JCR_SQL2), session, built);
            page.setLimit(limit);
            page.setOffset(offset);
            List<Article> items = new ArrayList<>();
            NodeIterator nodes = page.execute().getNodes();
            while (nodes.hasNext()) {
                items.add(map(nodes.nextNode()));
            }
            long elapsed = System.nanoTime() - start;
            String plan = explain(queries, session, built);
            lastExplainPlan = plan;
            if (elapsed >= slowQueryNanos) {
                log.warn("Recherche lente ({} ms). Plan : {}", elapsed / 1_000_000L, plan);
            }
            int total = count(queries, session, built);
            return new SearchResult(items, total);
        });
    }

    public String lastExplainPlan() {
        return lastExplainPlan;
    }

    private int count(QueryManager queries, Session session, ArticleQuery built) throws RepositoryException {
        Query count = bind(queries.createQuery(built.count(), Query.JCR_SQL2), session, built);
        RowIterator rows = count.execute().getRows();
        int total = 0;
        while (rows.hasNext() && total < MAX_COUNT) {
            rows.nextRow();
            total++;
        }
        return total;
    }

    private String explain(QueryManager queries, Session session, ArticleQuery built) {
        try {
            Query explain = bind(queries.createQuery("explain " + built.select(), Query.JCR_SQL2), session, built);
            QueryResult result = explain.execute();
            RowIterator rows = result.getRows();
            if (!rows.hasNext()) {
                return "plan vide";
            }
            Value[] values = rows.nextRow().getValues();
            if (values.length == 0) {
                return "plan vide";
            }
            return values[0].getString();
        } catch (RepositoryException ex) {
            return "plan indisponible";
        }
    }

    private static Query bind(Query query, Session session, ArticleQuery built) throws RepositoryException {
        for (var entry : built.bindings().entrySet()) {
            query.bindValue(entry.getKey(), session.getValueFactory().createValue(entry.getValue()));
        }
        return query;
    }

    private static Node articles(Session session, boolean create) throws RepositoryException {
        Node root = session.getRootNode();
        if (root.hasNode("articles")) {
            return root.getNode("articles");
        }
        if (!create) {
            return null;
        }
        return root.addNode("articles", "nt:unstructured");
    }

    private static void writeOptional(Node node, String name, String value) throws RepositoryException {
        if (value == null || value.isBlank()) {
            if (node.hasProperty(name)) {
                node.getProperty(name).remove();
            }
            return;
        }
        node.setProperty(name, value);
    }

    private static Article map(Node node) throws RepositoryException {
        try {
            return new Article(
                    ArticleId.parse(node.getProperty("jaya:id").getString()),
                    node.getProperty("jaya:title").getString(),
                    optional(node, "jaya:summary"),
                    node.getProperty("jaya:body").getString(),
                    PublicationStatus.valueOf(node.getProperty("jaya:status").getString()),
                    node.getProperty("jaya:locale").getString(),
                    tags(node),
                    Instant.ofEpochMilli(node.getProperty("jaya:updatedAt").getLong()),
                    new UserId(node.getProperty("jaya:updatedBy").getString()),
                    optional(node, "jaya:externalId"),
                    optional(node, "jaya:syncError"));
        } catch (IllegalArgumentException ex) {
            throw new ContentAccessException("Article JCR illisible : " + node.getPath(), ex);
        }
    }

    private static String optional(Node node, String name) throws RepositoryException {
        if (!node.hasProperty(name)) {
            return null;
        }
        String value = node.getProperty(name).getString();
        return value.isBlank() ? null : value;
    }

    private static List<String> tags(Node node) throws RepositoryException {
        if (!node.hasProperty("jaya:tags")) {
            return List.of();
        }
        Property property = node.getProperty("jaya:tags");
        if (!property.isMultiple()) {
            return List.of(property.getString());
        }
        List<String> values = new ArrayList<>();
        for (Value value : property.getValues()) {
            values.add(value.getString());
        }
        return List.copyOf(values);
    }
}
