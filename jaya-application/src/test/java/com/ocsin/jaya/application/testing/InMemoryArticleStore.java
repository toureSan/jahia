package com.ocsin.jaya.application.testing;

import com.ocsin.jaya.application.SearchCriteria;
import com.ocsin.jaya.application.SearchResult;
import com.ocsin.jaya.application.port.ArticleRepository;
import com.ocsin.jaya.application.port.ArticleSearch;
import com.ocsin.jaya.domain.Article;
import com.ocsin.jaya.domain.ArticleId;
import com.ocsin.jaya.domain.PublicationStatus;
import com.ocsin.jaya.domain.Visibility;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public final class InMemoryArticleStore implements ArticleRepository, ArticleSearch {

    private final Map<ArticleId, Article> articles = new ConcurrentHashMap<>();
    private final AtomicInteger finds = new AtomicInteger();

    @Override
    public Optional<Article> findById(ArticleId id) {
        finds.incrementAndGet();
        return Optional.ofNullable(articles.get(id));
    }

    @Override
    public void save(Article article) {
        articles.put(article.id(), article);
    }

    @Override
    public SearchResult search(SearchCriteria criteria, Visibility visibility) {
        List<Article> matched = new ArrayList<>();
        for (Article article : articles.values()) {
            if (visibility == Visibility.PUBLISHED_ONLY && article.status() != PublicationStatus.PUBLISHED) {
                continue;
            }
            if (criteria.status() != null && article.status() != criteria.status()) {
                continue;
            }
            if (criteria.locale() != null && !article.locale().equals(criteria.locale())) {
                continue;
            }
            if (criteria.term() != null) {
                String haystack = (article.title() + "\n" + article.body()).toLowerCase(Locale.ROOT);
                if (!haystack.contains(criteria.term().toLowerCase(Locale.ROOT))) {
                    continue;
                }
            }
            matched.add(article);
        }
        matched.sort(Comparator.comparing(Article::updatedAt).reversed().thenComparing(article -> article.id().value()));
        int from = Math.min(criteria.offset(), matched.size());
        int to = Math.min(from + criteria.limit(), matched.size());
        return new SearchResult(matched.subList(from, to), matched.size());
    }

    public int finds() {
        return finds.get();
    }
}
