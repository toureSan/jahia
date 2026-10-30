package com.ocsin.jaya.domain;

/**
 * Erreur métier exposable à un client, sans détail d'implémentation.
 */
public abstract class DomainException extends RuntimeException {

    private final int status;
    private final String code;

    protected DomainException(int status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public int status() {
        return status;
    }

    public String code() {
        return code;
    }
}
