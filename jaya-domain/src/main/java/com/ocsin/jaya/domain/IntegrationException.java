package com.ocsin.jaya.domain;

public final class IntegrationException extends DomainException {

    public IntegrationException(String message) {
        super(502, "integration", message);
    }

    public IntegrationException(String message, Throwable cause) {
        super(502, "integration", message);
        if (cause != null) {
            initCause(cause);
        }
    }
}
