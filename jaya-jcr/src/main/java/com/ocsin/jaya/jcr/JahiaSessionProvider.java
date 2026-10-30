package com.ocsin.jaya.jcr;

import com.ocsin.jaya.application.ContentAccessException;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Réutilise la session de la requête Jahia. Point de branchement vers JCRSessionFactory.
 */
public final class JahiaSessionProvider implements SessionProvider {

    @Override
    public <T> T inSession(SessionWork<T> work) {
        try {
            return work.doInSession(currentSession());
        } catch (RepositoryException ex) {
            throw new ContentAccessException("Accès au repository Jahia impossible", ex);
        }
    }

    @Override
    public void close() {
        // La session appartient au cycle de requête Jahia.
    }

    private Session currentSession() {
        try {
            Class<?> factoryType = Class.forName("org.jahia.services.content.JCRSessionFactory");
            Object factory = factoryType.getMethod("getInstance").invoke(null);
            Object session = factoryType.getMethod("getCurrentUserSession").invoke(factory);
            if (!(session instanceof Session)) {
                throw new ContentAccessException("La session Jahia courante n'implémente pas javax.jcr.Session");
            }
            return (Session) session;
        } catch (ClassNotFoundException ex) {
            throw new ContentAccessException(
                    "Jahia n'est pas disponible. Utilisez JahiaSessionProvider dans Jahia DX, ou OakSessionProvider en local.",
                    ex);
        } catch (ReflectiveOperationException ex) {
            throw new ContentAccessException("Impossible d'obtenir la session Jahia courante", ex);
        }
    }
}
