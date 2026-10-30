package com.ocsin.jaya.application;

import com.ocsin.jaya.domain.ArticleId;

import java.util.List;

public record SaveArticle(
        ArticleId id,
        String title,
        String summary,
        String body,
        String locale,
        List<String> tags) {
}
