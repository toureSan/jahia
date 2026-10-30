package com.ocsin.jaya.application;

import com.ocsin.jaya.application.port.ExternalPublisher;
import com.ocsin.jaya.application.testing.Fixtures;
import com.ocsin.jaya.domain.Action;
import com.ocsin.jaya.domain.Article;
import com.ocsin.jaya.domain.ForbiddenException;
import com.ocsin.jaya.domain.IntegrationException;
import com.ocsin.jaya.domain.InvalidTransitionException;
import com.ocsin.jaya.domain.NotFoundException;
import com.ocsin.jaya.domain.PublicationStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DefaultEditorialServiceTest {

    @Test
    @DisplayName("un lecteur ne voit pas un brouillon, même en connaissant l'identifiant")
    void readerCannotSeeDraft() {
        Fixtures fixtures = Fixtures.create();
        Article draft = fixtures.service.save(Fixtures.editor(), command(null, "Brouillon secret", "Corps interne"));

        assertThatThrownBy(() -> fixtures.service.find(Fixtures.reader(), draft.id()))
                .isInstanceOf(NotFoundException.class);
        assertThat(fixtures.service.search(Fixtures.reader(), search("secret")).items()).isEmpty();
        assertThat(fixtures.service.find(Fixtures.editor(), draft.id()).title()).isEqualTo("Brouillon secret");
    }

    @Test
    @DisplayName("la publication notifie le système externe et invalide le cache")
    void publishSyncsAndRefreshesCache() {
        Fixtures fixtures = Fixtures.create();
        Article draft = fixtures.service.save(Fixtures.editor(), command(null, "Index JCR", "Le plan d'exécution"));
        fixtures.service.find(Fixtures.editor(), draft.id());
        int findsBefore = fixtures.store.finds();
        fixtures.service.find(Fixtures.editor(), draft.id());
        assertThat(fixtures.store.finds()).isEqualTo(findsBefore);

        fixtures.clock.plusSeconds(2);
        Article review = fixtures.service.submit(Fixtures.editor(), draft.id());
        assertThat(review.status()).isEqualTo(PublicationStatus.IN_REVIEW);
        assertThatThrownBy(() -> fixtures.service.publish(Fixtures.editor(), draft.id()))
                .isInstanceOf(ForbiddenException.class);

        Article published = fixtures.service.publish(Fixtures.publisher(), draft.id());
        assertThat(published.status()).isEqualTo(PublicationStatus.PUBLISHED);
        assertThat(published.externalId()).startsWith("local-");
        assertThat(fixtures.service.search(Fixtures.reader(), search("index")).total()).isEqualTo(1);
        assertThat(fixtures.audit.actions).contains(Action.PUBLISH, Action.SYNC);
    }

    @Test
    @DisplayName("une panne externe laisse l'article publié et mémorise l'erreur")
    void externalFailureDoesNotRollback() {
        AtomicInteger calls = new AtomicInteger();
        ExternalPublisher flaky = new ExternalPublisher() {
            @Override
            public String publish(Article article) {
                if (calls.incrementAndGet() == 1) {
                    throw new IntegrationException("indisponible");
                }
                return "ext-42";
            }

            @Override
            public void retract(Article article) {
            }
        };
        Fixtures fixtures = Fixtures.create(flaky);
        Article draft = fixtures.service.save(Fixtures.editor(), command(null, "API", "Contrat REST"));
        fixtures.service.submit(Fixtures.editor(), draft.id());

        Article published = fixtures.service.publish(Fixtures.publisher(), draft.id());
        assertThat(published.status()).isEqualTo(PublicationStatus.PUBLISHED);
        assertThat(published.syncError()).isEqualTo("indisponible");

        Article synced = fixtures.service.sync(Fixtures.publisher(), draft.id());
        assertThat(synced.externalId()).isEqualTo("ext-42");
        assertThat(synced.syncError()).isNull();
    }

    @Test
    @DisplayName("modifier un article publié le repasse en brouillon et retire la copie externe")
    void editUnpublishes() {
        AtomicInteger retracts = new AtomicInteger();
        ExternalPublisher publisher = new ExternalPublisher() {
            @Override
            public String publish(Article article) {
                return "ext-1";
            }

            @Override
            public void retract(Article article) {
                retracts.incrementAndGet();
            }
        };
        Fixtures fixtures = Fixtures.create(publisher);
        Article draft = fixtures.service.save(Fixtures.editor(), command(null, "Cache", "Invalidation"));
        fixtures.service.submit(Fixtures.publisher(), draft.id());
        fixtures.service.publish(Fixtures.publisher(), draft.id());
        fixtures.clock.plusSeconds(1);

        Article edited = fixtures.service.save(Fixtures.editor(), command(draft.id(), "Cache v2", "Invalidation totale"));
        assertThat(edited.status()).isEqualTo(PublicationStatus.DRAFT);
        assertThat(edited.externalId()).isNull();
        assertThat(retracts).hasValue(1);
        assertThatThrownBy(() -> fixtures.service.publish(Fixtures.publisher(), draft.id()))
                .isInstanceOf(InvalidTransitionException.class);
    }

    private static SaveArticle command(com.ocsin.jaya.domain.ArticleId id, String title, String body) {
        return new SaveArticle(id, title, null, body, "fr", List.of("jcr"));
    }

    private static SearchCriteria search(String term) {
        return new SearchCriteria(term, null, null, 20, 0);
    }
}
