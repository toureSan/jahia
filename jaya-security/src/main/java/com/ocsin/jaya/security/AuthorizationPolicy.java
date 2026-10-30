package com.ocsin.jaya.security;

import com.ocsin.jaya.domain.Action;
import com.ocsin.jaya.domain.ForbiddenException;
import com.ocsin.jaya.domain.Principal;
import com.ocsin.jaya.domain.Role;
import com.ocsin.jaya.domain.Visibility;

/**
 * Droits applicatifs. Le repository JCR reste une seconde barrière via ses ACL.
 */
public final class AuthorizationPolicy {

    public void check(Principal principal, Action action) {
        if (!allows(principal, action)) {
            throw new ForbiddenException();
        }
    }

    public boolean allows(Principal principal, Action action) {
        if (principal.has(Role.ADMIN)) {
            return true;
        }
        return switch (action) {
            case READ_PUBLISHED -> true;
            case READ_ALL, SAVE, SUBMIT -> principal.hasAny(Role.EDITOR, Role.PUBLISHER);
            case PUBLISH, REJECT, SYNC -> principal.has(Role.PUBLISHER);
            case DIAGNOSE -> false;
        };
    }

    public Visibility visibility(Principal principal) {
        return allows(principal, Action.READ_ALL) ? Visibility.ALL : Visibility.PUBLISHED_ONLY;
    }
}
