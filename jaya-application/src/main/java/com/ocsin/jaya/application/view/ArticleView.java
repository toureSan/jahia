package com.ocsin.jaya.application.view;

import com.ocsin.jaya.domain.Article;

import java.util.List;

public final class ArticleView {

    private final String id;
    private final String title;
    private final String summary;
    private final String body;
    private final String status;
    private final String locale;
    private final List<String> tags;
    private final String updatedAt;
    private final String updatedBy;
    private final String externalId;
    private final String syncError;

    private ArticleView(Article article) {
        this.id = article.id().value();
        this.title = article.title();
        this.summary = article.summary();
        this.body = article.body();
        this.status = article.status().name();
        this.locale = article.locale();
        this.tags = article.tags();
        this.updatedAt = article.updatedAt().toString();
        this.updatedBy = article.updatedBy().value();
        this.externalId = article.externalId();
        this.syncError = article.syncError();
    }

    public static ArticleView of(Article article) {
        return new ArticleView(article);
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSummary() {
        return summary;
    }

    public String getBody() {
        return body;
    }

    public String getStatus() {
        return status;
    }

    public String getLocale() {
        return locale;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getExternalId() {
        return externalId;
    }

    public String getSyncError() {
        return syncError;
    }
}
