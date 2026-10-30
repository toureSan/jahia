package com.ocsin.jaya.application;

import com.ocsin.jaya.application.perf.OperationMonitor;
import com.ocsin.jaya.application.port.ArticleRepository;
import com.ocsin.jaya.application.port.ArticleSearch;
import com.ocsin.jaya.application.port.AuditLog;
import com.ocsin.jaya.application.port.CacheStore;
import com.ocsin.jaya.application.port.ExternalPublisher;
import com.ocsin.jaya.application.port.Ids;
import com.ocsin.jaya.domain.Action;
import com.ocsin.jaya.domain.Article;
import com.ocsin.jaya.domain.ArticleId;
import com.ocsin.jaya.domain.Clock;
import com.ocsin.jaya.domain.IntegrationException;
import com.ocsin.jaya.domain.InvalidTransitionException;
import com.ocsin.jaya.domain.NotFoundException;
import com.ocsin.jaya.domain.Principal;
import com.ocsin.jaya.domain.PublicationStatus;
import com.ocsin.jaya.domain.Visibility;
import com.ocsin.jaya.security.AuthorizationPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Orchestre le workflow, le cache et la synchronisation externe.
 * Le verrou couvre la lecture-modification-écriture pour éviter un cache périmé.
 */
public final class DefaultEditorialService implements EditorialService {

    private static final Logger log = LoggerFactory.getLogger(DefaultEditorialService.class);

    private final ArticleRepository repository;
    private final ArticleSearch search;
    private final ExternalPublisher publisher;
    private final CacheStore cache;
    private final OperationMonitor monitor;
    private final AuthorizationPolicy authorization;
    private final InputGuard input;
    private final AuditLog audit;
    private final Clock clock;
    private final Ids ids;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public DefaultEditorialService(ArticleRepository repository,
                                   ArticleSearch search,
                                   ExternalPublisher publisher,
                                   CacheStore cache,
                                   OperationMonitor monitor,
                                   AuthorizationPolicy authorization,
                                   InputGuard input,
                                   AuditLog audit,
                                   Clock clock,
                                   Ids ids) {
        this.repository = repository;
        this.search = search;
        this.publisher = publisher;
        this.cache = cache;
        this.monitor = monitor;
        this.authorization = authorization;
        this.input = input;
        this.audit = audit;
        this.clock = clock;
        this.ids = ids;
    }

    @Override
    public Article find(Principal principal, ArticleId id) {
        requirePrincipal(principal);
        Objects.requireNonNull(id, "id");
        authorization.check(principal, Action.READ_PUBLISHED);
        return monitor.time("article.find", () -> underRead(() -> loadVisible(principal, id)));
    }

    @Override
    public SearchResult search(Principal principal, SearchCriteria raw) {
        requirePrincipal(principal);
        authorization.check(principal, Action.READ_PUBLISHED);
        SearchCriteria criteria = input.sanitize(raw);
        Visibility visibility = authorization.visibility(principal);
        if (visibility == Visibility.PUBLISHED_ONLY
                && criteria.status() != null
                && criteria.status() != PublicationStatus.PUBLISHED) {
            return SearchResult.empty();
        }
        String key = searchKey(visibility, criteria);
        return monitor.time("article.search", () -> underRead(() -> {
            Optional<SearchResult> cached = cache.get(key, SearchResult.class);
            if (cached.isPresent()) {
                return cached.get();
            }
            SearchResult result = monitor.time("jcr.search", () -> search.search(criteria, visibility));
            cache.put(key, result);
            return result;
        }));
    }

    @Override
    public Article save(Principal principal, SaveArticle command) {
        requirePrincipal(principal);
        authorization.check(principal, Action.SAVE);
        SaveArticle safe = input.sanitize(command);
        SaveOutcome outcome = monitor.time("article.save", () -> underWrite(() -> doSave(principal, safe)));
        if (outcome.retract() == null) {
            return outcome.article();
        }
        return retract(principal, outcome);
    }

    @Override
    public Article submit(Principal principal, ArticleId id) {
        requirePrincipal(principal);
        authorization.check(principal, Action.SUBMIT);
        return monitor.time("article.submit", () -> underWrite(() ->
                transition(principal, id, PublicationStatus.DRAFT, PublicationStatus.IN_REVIEW, Action.SUBMIT)));
    }

    @Override
    public Article publish(Principal principal, ArticleId id) {
        requirePrincipal(principal);
        authorization.check(principal, Action.PUBLISH);
        Article published = monitor.time("article.publish", () -> underWrite(() ->
                transition(principal, id, PublicationStatus.IN_REVIEW, PublicationStatus.PUBLISHED, Action.PUBLISH)));
        return push(principal, published);
    }

    @Override
    public Article reject(Principal principal, ArticleId id) {
        requirePrincipal(principal);
        authorization.check(principal, Action.REJECT);
        return monitor.time("article.reject", () -> underWrite(() ->
                transition(principal, id, PublicationStatus.IN_REVIEW, PublicationStatus.DRAFT, Action.REJECT)));
    }

    @Override
    public Article sync(Principal principal, ArticleId id) {
        requirePrincipal(principal);
        authorization.check(principal, Action.SYNC);
        Article current = underWrite(() -> required(id));
        if (current.status() != PublicationStatus.PUBLISHED) {
            throw new InvalidTransitionException("Seul un article publié peut être synchronisé");
        }
        return monitor.time("article.sync", () -> push(principal, current));
    }

    private Article loadVisible(Principal principal, ArticleId id) {
        Visibility visibility = authorization.visibility(principal);
        String key = "article:" + visibility + ":" + id.value();
        Optional<Article> cached = cache.get(key, Article.class);
        if (cached.isPresent()) {
            return visible(cached.get(), visibility);
        }
        Article article = visible(required(id), visibility);
        cache.put(key, article);
        return article;
    }

    private Article visible(Article article, Visibility visibility) {
        if (visibility == Visibility.PUBLISHED_ONLY && article.status() != PublicationStatus.PUBLISHED) {
            throw new NotFoundException("Article introuvable");
        }
        return article;
    }

    private SaveOutcome doSave(Principal principal, SaveArticle command) {
        Instant now = clock.now();
        ArticleId id = command.id() == null ? ids.newArticleId() : command.id();
        Optional<Article> existing = repository.findById(id);
        Article retract = null;
        Article saved;
        if (existing.isEmpty()) {
            saved = new Article(id, command.title(), command.summary(), command.body(),
                    PublicationStatus.DRAFT, command.locale(), command.tags(), now, principal.userId(), null, null);
        } else {
            Article current = existing.get();
            if (current.status() == PublicationStatus.PUBLISHED && current.externalId() != null) {
                retract = current;
            }
            PublicationStatus status = current.status() == PublicationStatus.PUBLISHED
                    ? PublicationStatus.DRAFT
                    : current.status();
            saved = new Article(id, command.title(), command.summary(), command.body(), status,
                    command.locale(), command.tags(), now, principal.userId(), null, null);
        }
        repository.save(saved);
        invalidate();
        audit.record(principal, Action.SAVE, id, true);
        return new SaveOutcome(saved, retract);
    }

    private Article transition(Principal principal, ArticleId id, PublicationStatus expected,
                               PublicationStatus next, Action action) {
        Article current = required(id);
        if (current.status() != expected) {
            throw new InvalidTransitionException("Transition impossible depuis l'état " + current.status());
        }
        String externalId = next == PublicationStatus.PUBLISHED ? current.externalId() : null;
        Article updated = current.withWorkflow(next, clock.now(), principal.userId(), externalId, null);
        repository.save(updated);
        invalidate();
        audit.record(principal, action, id, true);
        return updated;
    }

    private Article push(Principal principal, Article published) {
        try {
            String externalId = monitor.time("integration.publish", () -> publisher.publish(published));
            return underWrite(() -> storeSync(published, externalId, null, principal, true));
        } catch (IntegrationException ex) {
            log.warn("Synchronisation externe échouée pour {} : {}", published.id().value(), ex.getMessage());
            return underWrite(() -> storeSync(published, null, ex.getMessage(), principal, false));
        }
    }

    private Article storeSync(Article published, String externalId, String error, Principal principal, boolean success) {
        Optional<Article> fresh = repository.findById(published.id());
        if (fresh.isEmpty()) {
            return published;
        }
        Article current = fresh.get();
        if (!current.updatedAt().equals(published.updatedAt()) || current.status() != PublicationStatus.PUBLISHED) {
            return current;
        }
        Article marked = current.withIntegration(success ? externalId : current.externalId(), error);
        repository.save(marked);
        invalidate();
        audit.record(principal, Action.SYNC, current.id(), success);
        return marked;
    }

    private Article retract(Principal principal, SaveOutcome outcome) {
        try {
            monitor.time("integration.retract", () -> publisher.retract(outcome.retract()));
            return outcome.article();
        } catch (IntegrationException ex) {
            log.warn("Retrait externe échoué pour {} : {}", outcome.article().id().value(), ex.getMessage());
            return underWrite(() -> {
                Article fresh = repository.findById(outcome.article().id()).orElse(outcome.article());
                if (!fresh.updatedAt().equals(outcome.article().updatedAt())) {
                    return fresh;
                }
                Article marked = fresh.withIntegration(fresh.externalId(), ex.getMessage());
                repository.save(marked);
                invalidate();
                audit.record(principal, Action.SAVE, fresh.id(), false);
                return marked;
            });
        }
    }

    private Article required(ArticleId id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Article introuvable"));
    }

    private void invalidate() {
        cache.invalidatePrefix("article:");
        cache.invalidatePrefix("search:");
    }

    private static String searchKey(Visibility visibility, SearchCriteria criteria) {
        return "search:" + visibility
                + ":" + criteria.limit()
                + ":" + criteria.offset()
                + ":" + criteria.status()
                + ":" + criteria.locale()
                + ":" + criteria.term();
    }

    private <T> T underRead(Supplier<T> work) {
        lock.readLock().lock();
        try {
            return work.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    private <T> T underWrite(Supplier<T> work) {
        lock.writeLock().lock();
        try {
            return work.get();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static void requirePrincipal(Principal principal) {
        Objects.requireNonNull(principal, "principal");
    }

    private record SaveOutcome(Article article, Article retract) {
    }
}
