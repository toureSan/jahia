package com.ocsin.jaya.security;

import com.ocsin.jaya.domain.Clock;
import com.ocsin.jaya.domain.RateLimitedException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Limite les échecs d'authentification par adresse cliente.
 */
public final class FailureThrottle {

    private final int maxFailures;
    private final Duration window;
    private final Clock clock;
    private final ConcurrentHashMap<String, Deque<Instant>> failures = new ConcurrentHashMap<>();

    public FailureThrottle(int maxFailures, Duration window, Clock clock) {
        if (maxFailures < 1) {
            throw new IllegalArgumentException("Le seuil de limitation doit être positif");
        }
        this.maxFailures = maxFailures;
        this.window = window;
        this.clock = clock;
    }

    public void check(String key) {
        Deque<Instant> deque = queue(key);
        synchronized (deque) {
            prune(deque, clock.now());
            if (deque.size() >= maxFailures) {
                throw new RateLimitedException();
            }
        }
    }

    public void recordFailure(String key) {
        Deque<Instant> deque = queue(key);
        synchronized (deque) {
            Instant now = clock.now();
            prune(deque, now);
            deque.addLast(now);
        }
    }

    public void reset(String key) {
        failures.remove(key);
    }

    private Deque<Instant> queue(String key) {
        return failures.computeIfAbsent(key, ignored -> new ArrayDeque<>());
    }

    private void prune(Deque<Instant> deque, Instant now) {
        Instant limit = now.minus(window);
        while (!deque.isEmpty() && deque.peekFirst().isBefore(limit)) {
            deque.removeFirst();
        }
    }
}
