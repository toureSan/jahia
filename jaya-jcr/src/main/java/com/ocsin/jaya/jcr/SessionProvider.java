package com.ocsin.jaya.jcr;

/**
 * Ouvre une session JCR pour la durée d'une opération.
 * Oak ferme la session ; Jahia laisse la session de requête au conteneur.
 */
public interface SessionProvider extends AutoCloseable {

    <T> T inSession(SessionWork<T> work);

    @Override
    void close();
}
