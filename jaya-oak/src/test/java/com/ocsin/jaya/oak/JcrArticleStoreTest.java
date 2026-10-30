package com.ocsin.jaya.oak;

import com.ocsin.jaya.application.SearchCriteria;
import com.ocsin.jaya.application.SearchResult;
import com.ocsin.jaya.domain.Article;
import com.ocsin.jaya.domain.ArticleId;
import com.ocsin.jaya.domain.PublicationStatus;
import com.ocsin.jaya.domain.UserId;
import com.ocsin.jaya.domain.Visibility;
import com.ocsin.jaya.jcr.ContentModelMigrator;
import com.ocsin.jaya.jcr.JcrArticleStore;
import com.ocsin.jaya.jcr.ModuleBootstrap;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JcrArticleStoreTest {

    private OakSessionProvider oak;
    private JcrArticleStore store;

    @BeforeEach
    void openRepository() {
        oak = OakSessionProvider.memory();
        ModuleBootstrap.standalone().start(oak);
        store = new JcrArticleStore(oak);
    }

    @AfterEach
    void closeRepository() {
        if (oak != null) {
            oak.close();
        }
    }

    @Test
    @DisplayName("la recherche publiée ignore les brouillons et neutralise les jokers")
    void searchesPublishedArticles() {
        Article published = article("11111111-1111-1111-1111-111111111111", "Indexation Oak", "Le plan évite le parcours", PublicationStatus.PUBLISHED, 2);
        Article draft = article("22222222-2222-2222-2222-222222222222", "Indexation secrète", "Brouillon", PublicationStatus.DRAFT, 3);
        store.save(published);
        store.save(draft);

        SearchResult result = store.search(new SearchCriteria("indexation", null, "fr", 10, 0), Visibility.PUBLISHED_ONLY);
        assertThat(result.items()).extracting(Article::title).containsExactly("Indexation Oak");
        assertThat(result.total()).isEqualTo(1);

        store.save(article("55555555-5555-5555-5555-555555555555", "100%", "taux", PublicationStatus.PUBLISHED, 4));
        SearchResult literalPercent = store.search(new SearchCriteria("%", null, "fr", 10, 0), Visibility.PUBLISHED_ONLY);
        assertThat(literalPercent.items()).extracting(Article::title).containsExactly("100%");

        SearchResult injected = store.search(new SearchCriteria("%' OR '1'='1", null, "fr", 10, 0), Visibility.ALL);
        assertThat(injected.total()).isZero();
        assertThat(store.lastExplainPlan()).isNotBlank();
    }

    @Test
    @DisplayName("le modèle v2 ajoute le résumé et rattrape le champ de recherche")
    void migratesFromV1() throws RepositoryException {
        oak.close();
        oak = OakSessionProvider.memory();
        ContentModelMigrator migrator = new ContentModelMigrator();
        oak.inSession(session -> {
            migrator.register(session, "cnd/jaya-v1.cnd");
            Node articles = session.getRootNode().addNode("articles", "nt:unstructured");
            Node node = articles.addNode("33333333-3333-3333-3333-333333333333", "jaya:article");
            node.setProperty("jaya:id", "33333333-3333-3333-3333-333333333333");
            node.setProperty("jaya:title", "Ancien");
            node.setProperty("jaya:titleLower", "ancien");
            node.setProperty("jaya:body", "Contenu historique");
            node.setProperty("jaya:status", "PUBLISHED");
            node.setProperty("jaya:locale", "fr");
            node.setProperty("jaya:updatedAt", 10L);
            node.setProperty("jaya:updatedBy", "editor");
            session.save();
            return null;
        });

        ModuleBootstrap.standalone().start(oak);

        oak.inSession(session -> {
            assertThat(migrator.hasProperty(session, "jaya:summary")).isTrue();
            Node node = session.getRootNode().getNode("articles/33333333-3333-3333-3333-333333333333");
            assertThat(node.getProperty("jaya:bodyLower").getString()).isEqualTo("contenu historique");
            assertThat(session.getRootNode().getNode("oak:index").hasNode("jayaStatus")).isTrue();
            return null;
        });
    }

    @Test
    void fileRepositoryKeepsArticles() throws Exception {
        Path directory = Files.createTempDirectory("jaya-oak");
        Article article = article("44444444-4444-4444-4444-444444444444", "Durable", "Sur disque", PublicationStatus.PUBLISHED, 4);
        OakSessionProvider first = OakSessionProvider.file(directory);
        try {
            ModuleBootstrap.standalone().start(first);
            new JcrArticleStore(first).save(article);
        } finally {
            first.close();
        }
        OakSessionProvider second = OakSessionProvider.file(directory);
        try {
            assertThat(new JcrArticleStore(second).findById(article.id())).isPresent();
        } finally {
            second.close();
            delete(directory);
        }
    }

    private static Article article(String id, String title, String body, PublicationStatus status, long epoch) {
        return new Article(new ArticleId(id), title, null, body, status, "fr", List.of("jcr"),
                Instant.ofEpochMilli(epoch), new UserId("editor"), null, null);
    }

    private static void delete(Path directory) throws Exception {
        if (!Files.exists(directory)) {
            return;
        }
        try (var walk = Files.walk(directory)) {
            walk.sorted(Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {
                    // Windows peut garder un fichier Oak ouvert une fraction de seconde.
                }
            });
        }
    }
}
