package com.ocsin.jaya.application;

import com.ocsin.jaya.application.port.Ids;
import com.ocsin.jaya.domain.ArticleId;

public final class RandomIds implements Ids {

    @Override
    public ArticleId newArticleId() {
        return ArticleId.generate();
    }
}
