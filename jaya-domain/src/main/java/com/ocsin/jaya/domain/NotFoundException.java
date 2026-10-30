package com.ocsin.jaya.domain;

public final class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(404, "not_found", message);
    }
}
