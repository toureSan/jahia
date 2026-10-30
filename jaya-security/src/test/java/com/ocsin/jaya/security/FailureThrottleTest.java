package com.ocsin.jaya.security;

import com.ocsin.jaya.domain.Clock;
import com.ocsin.jaya.domain.RateLimitedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FailureThrottleTest {

    @Test
    @DisplayName("la limite se lève après la fenêtre, et un succès remet le compteur à zéro")
    void blocksThenExpires() {
        MutableClock clock = new MutableClock(Instant.parse("2026-10-27T08:00:00Z"));
        FailureThrottle throttle = new FailureThrottle(2, Duration.ofMinutes(1), clock);

        throttle.recordFailure("10.0.0.8");
        throttle.recordFailure("10.0.0.8");
        assertThatThrownBy(() -> throttle.check("10.0.0.8")).isInstanceOf(RateLimitedException.class);

        clock.plus(Duration.ofMinutes(2));
        throttle.check("10.0.0.8");

        throttle.recordFailure("10.0.0.9");
        throttle.reset("10.0.0.9");
        throttle.check("10.0.0.9");
    }

    private static final class MutableClock implements Clock {
        private Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        @Override
        public Instant now() {
            return now;
        }

        private void plus(Duration duration) {
            now = now.plus(duration);
        }
    }
}
