package com.ocsin.jaya.rest;

import com.ocsin.jaya.application.SaveArticle;
import com.ocsin.jaya.domain.ArticleId;

import java.util.List;

public final class SaveArticleRequest {

    private String id;
    private String title;
    private String summary;
    private String body;
    private String locale;
    private List<String> tags;

    public SaveArticle toCommand() {
        return new SaveArticle(id == null || id.isBlank() ? null : ArticleId.parse(id), title, summary, body, locale, tags);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
