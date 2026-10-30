package com.ocsin.jaya.domain;

public final class ForbiddenException extends DomainException {

    public ForbiddenException() {
        super(403, "forbidden", "Action non autorisée");
    }
}
