package com.ocsin.jaya.application.port;

import com.ocsin.jaya.domain.Action;
import com.ocsin.jaya.domain.ArticleId;
import com.ocsin.jaya.domain.Principal;

public interface AuditLog {

    void record(Principal principal, Action action, ArticleId target, boolean success);
}
