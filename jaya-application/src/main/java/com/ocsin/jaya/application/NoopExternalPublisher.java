package com.ocsin.jaya.application;

import com.ocsin.jaya.application.port.ExternalPublisher;
import com.ocsin.jaya.domain.Article;

/**
 * Publication locale lorsque aucun système externe n'est configuré.
 */
public final class NoopExternalPublisher implements ExternalPublisher {

    @Override
    public String publish(Article article) {
        return "local-" + article.id().value();
    }

    @Override
    public void retract(Article article) {
        // Rien à retirer hors du repository local.
    }
}
