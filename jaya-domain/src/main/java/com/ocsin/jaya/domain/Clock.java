package com.ocsin.jaya.domain;

import java.time.Instant;

/**
 * Horloge injectable pour figer le temps dans les tests et dater les révisions.
 */
public interface Clock {

    Instant now();
}
