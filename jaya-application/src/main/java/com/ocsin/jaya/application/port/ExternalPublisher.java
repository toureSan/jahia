package com.ocsin.jaya.application.port;

import com.ocsin.jaya.domain.Article;
import com.ocsin.jaya.domain.IntegrationException;

/**
 * Système externe notifié après publication. L'échec ne doit pas annuler le contenu local.
 */
public interface ExternalPublisher {

    String publish(Article article) throws IntegrationException;

    void retract(Article article) throws IntegrationException;
}
