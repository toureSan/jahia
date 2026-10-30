package com.ocsin.jaya.domain;

public final class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(400, "validation", message);
    }
}
