package com.ocsin.jaya.application.testing;

import com.ocsin.jaya.application.DefaultEditorialService;
import com.ocsin.jaya.application.InputGuard;
import com.ocsin.jaya.application.NoopExternalPublisher;
import com.ocsin.jaya.application.perf.CaffeineCacheStore;
import com.ocsin.jaya.application.perf.OperationMonitor;
import com.ocsin.jaya.application.port.AuditLog;
import com.ocsin.jaya.application.port.ExternalPublisher;
import com.ocsin.jaya.application.port.Ids;
import com.ocsin.jaya.domain.Action;
import com.ocsin.jaya.domain.ArticleId;
import com.ocsin.jaya.domain.Clock;
import com.ocsin.jaya.domain.Principal;
import com.ocsin.jaya.domain.Role;
import com.ocsin.jaya.domain.UserId;
import com.ocsin.jaya.security.AuthorizationPolicy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class Fixtures {

    public final MutableClock clock = new MutableClock(Instant.parse("2026-10-27T08:00:00Z"));
    public final InMemoryArticleStore store = new InMemoryArticleStore();
    public final RecordingAudit audit = new RecordingAudit();
    public final DefaultEditorialService service;

    private Fixtures(ExternalPublisher publisher) {
        this.service = new DefaultEditorialService(
                store,
                store,
                publisher,
                new CaffeineCacheStore(Duration.ofMinutes(5), 100),
                new OperationMonitor(Duration.ofHours(1), 20, clock),
                new AuthorizationPolicy(),
                new InputGuard(),
                audit,
                clock,
                new SequenceIds());
    }

    public static Fixtures create() {
        return new Fixtures(new NoopExternalPublisher());
    }

    public static Fixtures create(ExternalPublisher publisher) {
        return new Fixtures(publisher);
    }

    public static Principal reader() {
        return principal("reader", Role.READER);
    }

    public static Principal editor() {
        return principal("editor", Role.EDITOR);
    }

    public static Principal publisher() {
        return principal("publisher", Role.PUBLISHER);
    }

    public static Principal admin() {
        return principal("admin", Role.ADMIN);
    }

    private static Principal principal(String user, Role role) {
        return new Principal(new UserId(user), EnumSet.of(role));
    }

    public static final class MutableClock implements Clock {
        private Instant now;

        public MutableClock(Instant now) {
            this.now = now;
        }

        @Override
        public Instant now() {
            return now;
        }

        public void plusSeconds(long seconds) {
            now = now.plusSeconds(seconds);
        }
    }

    public static final class RecordingAudit implements AuditLog {
        public final List<Action> actions = new ArrayList<>();

        @Override
        public void record(Principal principal, Action action, ArticleId target, boolean success) {
            actions.add(action);
        }
    }

    public static final class SequenceIds implements Ids {
        private int n;

        @Override
        public ArticleId newArticleId() {
            n++;
            return new ArticleId(String.format("00000000-0000-0000-0000-%012d", n));
        }
    }
}
