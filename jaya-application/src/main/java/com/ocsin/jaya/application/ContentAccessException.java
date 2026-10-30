package com.ocsin.jaya.application;

/**
 * Panne technique du repository. Le message client reste générique.
 */
public final class ContentAccessException extends RuntimeException {

    public ContentAccessException(String message) {
        super(message);
    }

    public ContentAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
