package com.ocsin.jaya.application;

import com.ocsin.jaya.application.port.AuditLog;
import com.ocsin.jaya.domain.Action;
import com.ocsin.jaya.domain.ArticleId;
import com.ocsin.jaya.domain.Principal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Journal d'audit sans corps d'article ni jeton.
 */
public final class Slf4jAuditLog implements AuditLog {

    private static final Logger log = LoggerFactory.getLogger("com.ocsin.jaya.audit");

    @Override
    public void record(Principal principal, Action action, ArticleId target, boolean success) {
        log.info("principal={} action={} target={} outcome={}",
                principal.userId().value(),
                action.name(),
                target.value(),
                success ? "success" : "failure");
    }
}
