package com.ocsin.jaya.domain;

public final class InvalidTransitionException extends DomainException {

    public InvalidTransitionException(String message) {
        super(409, "invalid_transition", message);
    }
}
