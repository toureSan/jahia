package com.ocsin.jaya.application;

import com.ocsin.jaya.domain.PublicationStatus;

public record SearchCriteria(
        String term,
        PublicationStatus status,
        String locale,
        int limit,
        int offset) {
}
