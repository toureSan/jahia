package com.ocsin.jaya.application.port;

import com.ocsin.jaya.domain.Article;
import com.ocsin.jaya.domain.ArticleId;

import java.util.Optional;

public interface ArticleRepository {

    Optional<Article> findById(ArticleId id);

    void save(Article article);
}
